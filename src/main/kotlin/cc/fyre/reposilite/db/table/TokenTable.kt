package cc.fyre.reposilite.db.table

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object TokenTable : Table(name = "reposilite_tokens") {

    val username: Column<String> = varchar("username",length = 191)
    val pterodactylId: Column<Int> = integer("user_id")
    val accessTokenId: Column<Int> = integer("access_token_id")

    override val primaryKey = PrimaryKey(accessTokenId,name = "PRIMARY")

    fun insertToken(name: String,pterodactylId: Int,accessTokenId: Int) {
        this.insert{
            it[username] = name
            it[TokenTable.accessTokenId] = accessTokenId
            it[TokenTable.pterodactylId] = pterodactylId
        }
    }

    fun findPterodactylIdByUsername(username: String):Int? {
        return TokenTable.selectAll().where{ TokenTable.username eq username}.singleOrNull()?.get(pterodactylId)
    }

    fun findAllByPterodactylId(id: Int):Query {
        return TokenTable.selectAll().where{ pterodactylId eq id}
    }

    fun deleteAllByPterodactylId(id: Int) {
        deleteWhere{ pterodactylId eq id}
    }
}