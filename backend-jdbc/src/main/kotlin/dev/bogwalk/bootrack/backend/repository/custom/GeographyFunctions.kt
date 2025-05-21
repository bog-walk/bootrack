package dev.bogwalk.bootrack.backend.repository.custom

import dev.bogwalk.bootrack.backend.schema.custom.GeographyColumnType
import dev.bogwalk.bootrack.model.Location
import org.jetbrains.exposed.v1.core.ComplexExpression
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.QueryParameter
import org.jetbrains.exposed.v1.core.append

/** Returns the specified [Location] value as a query parameter value. */
internal fun locationParam(
    value: Location
): Expression<Location> = QueryParameter(value, GeographyColumnType())

/**
 * Checks whether this stored [Location] is within [maxDistance] to the [other] location.
 *
 * @param maxDistance Maximum distance in kilometers in which [other] must be positioned to return `true`.
 * This value will be automatically converted to meters during SQL generation.
 */
internal fun Expression<Location?>.withinTravelDistanceTo(
    other: Location,
    maxDistance: Int
): StDWithin {
    return StDWithin(this, locationParam(other), maxDistance * 1000)
}

/**
 * Representation of the Postgis `ST_DWithin` function that returns whether 2 geography points
 * are within the specified [meters] distance from each other.
 */
internal class StDWithin(
    val expression1: Expression<Location?>,
    val expression2: Expression<Location>,
    val meters: Int
) : Op<Boolean>(), ComplexExpression {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            append("ST_DWITHIN(")
            append(expression1, ", ", expression2, ", ", meters.toString())
            append(")")
        }
    }
}
