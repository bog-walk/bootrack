package dev.bogwalk.bootrack.backend.schema.custom

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.Function

/** Concatenates the text representation of [this] expression to the [other] expression, without specifying a separator. */
internal infix fun <T : String?> ExpressionWithColumnType<T>.concat(
    other: ExpressionWithColumnType<T>
): ConcatOp = ConcatOp("", this, other)

/** Representation of the string concatenation operator, which, unlike `CONCAT()`, does not ignore `null` values. */
internal class ConcatOp(
    val separator: String,
    vararg val expr: Expression<*>
) : Function<String>(TsvectorColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        if (separator == "") {
            expr.toList().appendTo(queryBuilder, separator = " || ") { +it }
        } else {
            expr.toList().appendTo(queryBuilder, separator = " || '$separator' || ") { +it }
        }
    }
}
