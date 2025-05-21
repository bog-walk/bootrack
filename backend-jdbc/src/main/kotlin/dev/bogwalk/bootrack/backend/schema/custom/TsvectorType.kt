package dev.bogwalk.bootrack.backend.schema.custom

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.TextColumnType
import org.jetbrains.exposed.v1.core.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

/** Registers a table column that accepts `String` values as input & stores them as PG-specific `tsvector` types. */
internal fun Table.tsvector(name: String): Column<String> {
    return registerColumn(name, TsvectorColumnType())
}

/** Custom character column for storing text as PG-specific `tsvector` type. */
internal class TsvectorColumnType : TextColumnType() {
    override fun sqlType(): String = "TSVECTOR"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val parameterValue: PGobject? = value?.let {
            PGobject().apply {
                type = sqlType()
                this.value = value as? String
            }
        }
        super.setParameter(stmt, index, parameterValue)
    }
}
