package dev.bogwalk.bootrack.backend.repository

import dev.bogwalk.bootrack.backend.repository.custom.createAutoUpdateTriggers
import dev.bogwalk.bootrack.backend.schema.tables.*
import io.ktor.server.config.*
import io.r2dbc.spi.ConnectionFactoryOptions
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.io.InputStream

internal interface DatabaseFactory {
    suspend fun connect(
        config: ApplicationConfig,
    ) {
        val dbConfig = config.config("database")
        val dbDriver = dbConfig.property("driverClassName").getString()
        val dbHost = dbConfig.property("host").getString()
        val dbName = dbConfig.property("databaseName").getString()
        val dbSampleDataPath = dbConfig.property("sampleData").getString()

        val db = R2dbcDatabase.connect {
            connectionFactoryOptions {
                option(ConnectionFactoryOptions.DRIVER, dbDriver)
                option(ConnectionFactoryOptions.HOST, dbHost)
                option(ConnectionFactoryOptions.DATABASE, dbName)
                option(ConnectionFactoryOptions.USER, dbConfig.property("user").getString())
                option(ConnectionFactoryOptions.PASSWORD, dbConfig.property("password").getString())
            }
        }

        suspendTransaction(db = db) {
            val allTables = arrayOf(
                Projects, Users, Issues, Comments,
                NotificationTypes, Notifications, SessionNotifications
            )

            SchemaUtils.drop(tables = allTables)
            SchemaUtils.create(tables = allTables)

            createAutoUpdateTriggers()
        }

        suspendTransaction(db = db) {
            val stream = javaClass.classLoader.getResourceAsStream(dbSampleDataPath)
                ?: error("Could not find .sql file at: $dbSampleDataPath")
            loadSampleData(stream)
        }
    }
}

private suspend fun R2dbcTransaction.loadSampleData(input: InputStream) {
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
