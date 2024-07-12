package cc.fyre.reposilite

import cc.fyre.reposilite.command.PterodactylTokensCommand
import cc.fyre.reposilite.db.PterodactylDatabase
import cc.fyre.reposilite.settings.PterodactylSettings
import com.reposilite.auth.AuthenticationFacade
import com.reposilite.configuration.shared.SharedConfigurationFacade
import com.reposilite.configuration.shared.api.SharedSettings
import com.reposilite.console.api.CommandsSetupEvent
import com.reposilite.plugin.api.Facade
import com.reposilite.plugin.api.Plugin
import com.reposilite.plugin.api.ReposiliteDisposeEvent
import com.reposilite.plugin.api.ReposilitePlugin
import com.reposilite.plugin.event
import com.reposilite.token.AccessTokenFacade
import panda.std.reactive.MutableReference
import panda.std.reactive.Subscriber


@Plugin(name = "pterodactyl-auth", dependencies = ["authentication"], settings = PterodactylSettings::class)
class PterodactylAuthPlugin : ReposilitePlugin() {

    private lateinit var settingsReference: MutableReference<PterodactylSettings>

    override fun initialize(): Facade? {
        instance = this
        setupSettings()

        PterodactylDatabase.init(this)

        val accessTokenFacade = extensions().facade(AccessTokenFacade::class.java)

        this.settingsReference.subscribe{
            handleSetupChanges()
        }

        extensions().facade(AuthenticationFacade::class.java).registerAuthenticator(PterodactylAuthenticator(this,accessTokenFacade))

        event { event: CommandsSetupEvent ->
            event.registerCommand(PterodactylTokensCommand(accessTokenFacade))
        }

        event { event: ReposiliteDisposeEvent ->
            //TODO
        }

        return null
    }

    fun getSettingsReference():MutableReference<PterodactylSettings> {
        return this.settingsReference
    }

    companion object {

        private lateinit var instance: PterodactylAuthPlugin

        fun get():PterodactylAuthPlugin {
            return this.instance
        }

    }

    private fun setupSettings() {
        val sharedConfigurationFacade = extensions().facade(SharedConfigurationFacade::class.java)
        sharedConfigurationFacade.updateSharedSettings<SharedSettings>("pterodactyl-auth",PterodactylSettings())
        this.settingsReference = sharedConfigurationFacade.getDomainSettings(PterodactylSettings::class)
    }

    private fun handleSetupChanges() {
        PterodactylDatabase.init(this)
    }

}