package dev.bogwalk.bootrack.backend.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.bogwalk.bootrack.backend.repository.custom.createAutoUpdateTriggers
import dev.bogwalk.bootrack.backend.schema.tables.*
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.InputStream

internal interface DatabaseFactory {
    fun createHikariDataSource(
        driver: String,
        host: String,
        database: String,
        user: String,
        password: String,
    ): HikariDataSource = HikariDataSource(
        HikariConfig().apply {
            driverClassName = driver
            jdbcUrl = "jdbc:${driver.split('.')[1]}://$host/$database?user=$user&password=$password"
            maximumPoolSize = 3
            isAutoCommit = false

            validate()
        }
    )

    fun connect(
        connectionPool: HikariDataSource,
        sampleDataPath: String,
    ) {
        val db = Database.connect(
            datasource = connectionPool,
            databaseConfig = DatabaseConfig {
                defaultMaxAttempts = 1
            }
        )

        transaction(db) {
            val allTables = arrayOf(
                Projects, Users, Issues, Comments,
                NotificationTypes, Notifications, SessionNotifications
            )

            SchemaUtils.drop(tables = allTables)
            SchemaUtils.create(tables = allTables)

            createAutoUpdateTriggers()
        }

        transaction(db) {
            val stream = javaClass.classLoader.getResourceAsStream(sampleDataPath)
                ?: error("Could not find .sql file at: $sampleDataPath")
            loadSampleData(stream)
        }
    }
}

private fun JdbcTransaction.loadSampleData(input: InputStream) {
    val commentPattern = Regex("""^--\s*""")
    val testDataSql = mutableListOf<List<String>>()
    var batchedStatements = mutableListOf<String>()
    val statement = StringBuilder()

    input
        .bufferedReader()
        .forEachLine { line ->
            val newLine = line.trim()
            when {
                newLine.isEmpty() -> {}
                commentPattern.containsMatchIn(newLine) -> {
                    batchedStatements
                        .takeUnless { it.isEmpty() }
                        ?.let {
                            testDataSql.add(it)
                            batchedStatements = mutableListOf()
                            statement.setLength(0)
                        }
                }
                newLine.endsWith(';') -> {
                    statement.append("$newLine ")
                    batchedStatements.add(statement.toString())
                    statement.setLength(0)
                }
                else -> statement.append("$newLine ")
            }
        }

    if (batchedStatements.isNotEmpty()) {
        testDataSql.add(batchedStatements)
    }

    testDataSql.forEach { batch ->
        execInBatch(batch)
    }
}
