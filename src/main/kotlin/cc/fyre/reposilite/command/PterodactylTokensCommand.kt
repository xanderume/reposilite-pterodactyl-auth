package cc.fyre.reposilite.command

import cc.fyre.reposilite.PterodactylAuthCommons
import cc.fyre.reposilite.db.PterodactylDatabase
import cc.fyre.reposilite.db.table.UserTable
import com.reposilite.console.CommandContext
import com.reposilite.console.api.ReposiliteCommand
import com.reposilite.token.AccessTokenFacade
import com.reposilite.token.api.AccessTokenDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.transaction
import picocli.CommandLine

@CommandLine.Command(name = "pteroq-tokens", description = ["List all pterodactyl tokens"])
internal class PterodactylTokensCommand(private val accessTokenFacade: AccessTokenFacade) : ReposiliteCommand {

    override fun execute(context: CommandContext) {

        val tokens = accessTokenFacade.getAccessTokens().filter{it.name.startsWith(PterodactylAuthCommons.NamePrefix)}
        val tokensToUser = hashMapOf<AccessTokenDto,ResultRow>()

        transaction(PterodactylDatabase.get()) {

            for (user in UserTable.findAll()) {

                val token = tokens.firstOrNull{user[UserTable.id] == it.name.replace(PterodactylAuthCommons.NamePrefix,"").toIntOrNull()}

                if (token == null) {
                    continue
                }

                tokensToUser[token] = user
            }

        }

        context.append("Pterodactyl Tokens (${tokens.size} [${tokens.size - tokensToUser.size} Invalid])")

        for ((token,user) in tokensToUser) {

            context.append("- ${token.name} [Pterodactyl: ${user[UserTable.username]}]: ${accessTokenFacade.getPermissions(token.identifier)}")
            val routes = accessTokenFacade.getRoutes(token.identifier)

            routes.groupBy { it.path }
                .forEach { (route, permissions) ->
                    context.append("  > $route : ${permissions.map { it.permission } }")
                }

            if (routes.isEmpty()) {
                context.append("  > ~ no routes ~")
            }

        }

    }

}