package cc.fyre.reposilite.settings

import com.reposilite.configuration.shared.api.Doc
import com.reposilite.configuration.shared.api.SharedSettings
import io.javalin.openapi.JsonSchema

@JsonSchema
@Doc(title = "Pterodactyl", description = "Pterodactyl Authenticator settings")
class PterodactylSettings : SharedSettings {

    @get:Doc(title = "Enabled", description = "Pterodactyl Authenticator is enabled")
    val enabled: Boolean = false

    @get:Doc(title = "URL", description = "SQL database URL")
    val url: String = "jdbc:mariadb://localhost:3306/panel"

    @get:Doc(title = "Driver", description = "SQL Driver")
    val driver: String = "org.mariadb.jdbc.Driver"

    @get:Doc(title = "Username", description = "SQL Username")
    val username: String = "pterodactyl"

    @get:Doc(title = "Password", description = "SQL Password")
    val password: String = ""

}