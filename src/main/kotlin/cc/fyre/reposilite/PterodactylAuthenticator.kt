package cc.fyre.reposilite

import at.favre.lib.crypto.bcrypt.BCrypt
import cc.fyre.reposilite.db.PterodactylDatabase
import cc.fyre.reposilite.db.table.TokenTable
import cc.fyre.reposilite.db.table.UserTable
import com.reposilite.auth.Authenticator
import com.reposilite.auth.api.Credentials
import com.reposilite.shared.ErrorResponse
import com.reposilite.shared.internalServerError
import com.reposilite.shared.notFoundError
import com.reposilite.shared.unauthorizedError
import com.reposilite.token.AccessTokenFacade
import com.reposilite.token.AccessTokenIdentifier
import com.reposilite.token.AccessTokenPermission
import com.reposilite.token.AccessTokenType
import com.reposilite.token.api.AccessTokenDto
import com.reposilite.token.api.CreateAccessTokenRequest
import org.jetbrains.exposed.sql.transactions.transaction
import panda.std.Result

class PterodactylAuthenticator(private val plugin: PterodactylAuthPlugin,private val accessTokenFacade: AccessTokenFacade) : Authenticator {

    private val bcrypt = BCrypt.verifyer()

    override fun priority(): Int = 10

    override fun enabled(): Boolean {
        return this.plugin.getSettingsReference().map{it.enabled}
    }

    override fun realm(): String {
        return "Pterodactyl"
    }

    override fun authenticate(credentials: Credentials): Result<AccessTokenDto, ErrorResponse> {
        return try {
            transaction(PterodactylDatabase.get()) {

                val user = UserTable.findByUsernameOrEmail(credentials.name)

                if (user == null) {

                    val pterodactylId: Int? = TokenTable.findPterodactylIdByUsername(credentials.name)

                    if (pterodactylId != null) {

                        // we lookup every token associated with this pterodactylId
                        for (token in TokenTable.findAllByPterodactylId(pterodactylId)) {

                            val identifier = token[TokenTable.accessTokenId]

                            try {
                                accessTokenFacade.deleteToken(AccessTokenIdentifier(
                                    type = AccessTokenType.PERSISTENT,
                                    value = identifier
                                ))
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }

                        }

                        TokenTable.deleteAllByPterodactylId(pterodactylId)
                    }

                    return@transaction notFoundError("User not found")
                }

                val hashed = user[UserTable.password]
                val result = bcrypt.verify(credentials.secret.toCharArray(),hashed)

                if (!result.verified) {
                    return@transaction unauthorizedError("Invalid authorization credentials")
                }

                val id = user[UserTable.id]
                val admin = user[UserTable.admin]

                val accessTokenName = "${PterodactylAuthCommons.NamePrefix}${id}"
                var token = accessTokenFacade.getAccessToken(accessTokenName)

                if (token == null) {
                    token = accessTokenFacade.createAccessToken(
                        CreateAccessTokenRequest(
                            type = AccessTokenType.PERSISTENT,
                            name = accessTokenName,
                            secret = credentials.secret,
                        )
                    ).accessToken

                    if (admin) {
                        accessTokenFacade.addPermission(token.identifier,DefaultPermission)
                    }

                    TokenTable.insertToken(name = credentials.name,pterodactylId = id,accessTokenId = token.identifier.value)
                } else {

                    if (admin && !accessTokenFacade.getPermissions(token.identifier).contains(DefaultPermission)) {
                        accessTokenFacade.addPermission(token.identifier,DefaultPermission)
                    }

                }

                return@transaction Result.ok(token)
            }
        } catch (ex: Exception) {
            PterodactylAuthPlugin.get().logger.exception(ex)
            internalServerError(ex.message)
        }

    }

    companion object {

        private val DefaultPermission = AccessTokenPermission.MANAGER

    }

}