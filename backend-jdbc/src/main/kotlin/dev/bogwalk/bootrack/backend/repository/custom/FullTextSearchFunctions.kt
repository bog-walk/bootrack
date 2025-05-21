package dev.bogwalk.bootrack.backend.repository.custom

import org.jetbrains.exposed.v1.core.ComparisonOp
import org.jetbrains.exposed.v1.core.CustomFunction
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.FloatColumnType
import org.jetbrains.exposed.v1.core.TextColumnType
import org.jetbrains.exposed.v1.core.stringParam

/**
 * Converts the specified [query] string to PG-specific `tsquery` type,
 * with normalization according to the default 'english' configuration.
 */
internal fun toTSQuery(query: String): ToTSQuery = ToTSQuery(stringParam(query))

/**
 * Representation of the FTS function that converts the specified [query] string to PG-specific `tsquery` type,
 * with normalization according to the default 'english' configuration.
 */
internal class ToTSQuery(
    query: Expression<String>
) : CustomFunction<String>(
    functionName = "to_tsquery",
    columnType = TextColumnType(),
    expr = arrayOf(query)
)

/** Checks whether [this] vector expression matches the specified [other] expression. */
internal infix fun <T : String?> ExpressionWithColumnType<T>.tsMatches(
    other: Expression<T>
): TSMatchOp<T> = TSMatchOp(this, other)

/** Representation of the FTS function that checks whether text or vectors match each other. */
internal class TSMatchOp<T : String?>(
    expr1: Expression<T>,
    expr2: Expression<T>
) : ComparisonOp(
    expr1 = expr1,
    expr2 = expr2,
    opSign = "@@"
)

/** Computes a score ranking how well this vector expression matches the specified [query]. */
internal fun <T : String?> ExpressionWithColumnType<T>.tsRank(
    query : Expression<T>
): TSRank<T> = TSRank(this, query)

/** Representation of the FTS function that computes a score ranking how well the specified [vector] matches the specified [query]. */
internal class TSRank<T : String?>(
    vector: Expression<T>,
    query: Expression<T>
) : CustomFunction<Float>(
    functionName = "ts_rank",
    columnType = FloatColumnType(),
    expr = arrayOf(vector, query)
)
