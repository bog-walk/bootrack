package dev.bogwalk.bootrack.backend.repository.custom

import kotlinx.datetime.Instant
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.Function

/** Computes the number of seconds difference between [this] `Instant` expression and the [other] expression. */
internal infix fun ExpressionWithColumnType<Instant>.subtract(
    other: Expression<Instant>
): InstantMinusOp = InstantMinusOp(this, other)

/** Representation of a combination of functions that computes the number of seconds between two `Instant` expressions. */
internal class InstantMinusOp(
    val expression1: Expression<Instant>,
    val expression2: Expression<Instant>,
) : Function<Long>(LongColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            append("EXTRACT(EPOCH FROM ", expression1, ")")
            append(" - ")
            append("EXTRACT(EPOCH FROM ", expression2, ")")
        }
    }
}
