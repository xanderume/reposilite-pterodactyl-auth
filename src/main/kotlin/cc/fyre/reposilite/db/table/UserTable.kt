package cc.fyre.reposilite.db.table

import org.jetbrains.exposed.sql.*

object UserTable : Table(name = "users") {

    val id: Column<Int> = integer("id")
    val email: Column<String> = varchar("email",length = 191)
    val username: Column<String> = varchar("username",length = 191)
    val password: Column<String> = text("password")
    val admin: Column<Boolean> = bool("root_admin")

    override val primaryKey = PrimaryKey(id,name = "PRIMARY")

    fun findAll():Query {
        return UserTable.selectAll()
    }

    fun findByUsernameOrEmail(value: String):ResultRow? {
        return UserTable.select(id, email, username, password, admin).where{
            username eq value or(email eq value)
        }.singleOrNull()
    }

}