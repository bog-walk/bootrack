package dev.bogwalk.bootrack.backend.schema.custom

import dev.bogwalk.bootrack.model.Location
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

/** Registers a table column that accepts `Location` values as input & stores them as spatial geography types. */
internal fun Table.geography(name: String): Column<Location> {
    return registerColumn(name, GeographyColumnType())
}

/** Custom character column for storing `Location` instances as spatial `geography` type. */
internal class GeographyColumnType : ColumnType<Location>() {
    override fun sqlType(): String = "GEOGRAPHY($POINT)"

    override fun valueFromDB(value: Any): Location? = when {
        value is String -> {
            value
                .takeIf { it.isNotEmpty() }
                ?.substringAfter(POINT)
                ?.trim('(', ')')
                ?.split(' ')
                ?.let { Location(it[0].toDouble(), it[1].toDouble()) }
        }
        else -> null
    }

    override fun nonNullValueToString(value: Location): String =
        "$POINT(${value.latitude} ${value.longitude})"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val parameterValue: PGobject? = value?.let {
            PGobject().apply {
                type = "GEOGRAPHY"
                this.value = "SRID=4326;${nonNullValueToString(it as Location)}"
            }
        }
        super.setParameter(stmt, index, parameterValue)
    }

    companion object {
        private const val POINT = "POINT"
    }
}
