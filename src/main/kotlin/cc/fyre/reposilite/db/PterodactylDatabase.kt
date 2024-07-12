package cc.fyre.reposilite.db

import cc.fyre.reposilite.PterodactylAuthPlugin
import cc.fyre.reposilite.db.table.TokenTable
import cc.fyre.reposilite.settings.PterodactylSettings
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object PterodactylDatabase {

    private lateinit var database: Database

    fun init(plugin: PterodactylAuthPlugin) {
        plugin.logger.info("Pterodactyl-Auth: Connecting..")

        try {
            database = Database.connect(
                url = plugin.getSettingsReference().map{it.url},
                driver = plugin.getSettingsReference().map{it.driver},
                user = plugin.getSettingsReference().map{it.username},
                password = plugin.getSettingsReference().map{it.password}
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            return
        }

        this.setupTables()
        plugin.logger.info("Pterodactyl-Auth: Connected!")
    }

    fun get():Database {
        return database
    }

    private fun setupTables() = transaction(database) {

        // UserTable is created by pterodactyl
        val tables = arrayOf(
            TokenTable,
        )

        SchemaUtils.create(*tables)
        SchemaUtils.createMissingTablesAndColumns(*tables)
    }

}