package dev.bogwalk.bootrack.backend.repository.custom

import org.jetbrains.exposed.v1.core.Case
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.ExpressionWithColumnType
import org.jetbrains.exposed.v1.core.Function
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.IntegerColumnType
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.anyFrom
import org.jetbrains.exposed.v1.core.append
import org.jetbrains.exposed.v1.core.intParam

/** Removes any element that matches [element] from this array expression, or adds the element if a match is not found. */
internal fun <T : List<Int>?> ExpressionWithColumnType<T>.addOrRemove(
    element: Int
): ExpressionWithColumnType<T> = Case()
    .When(intParam(element) eq anyFrom(this), this.remove(element))
    .Else(this.add(element))

/** Removes any element that matches [element] from this array expression. */
internal fun <E, T : List<E>?> ExpressionWithColumnType<T>.remove(
    element: E
): ArrayRemove<E, T> = ArrayRemove(this, element, columnType)

/** Representation of the array function that removes any element equal to the specified [element] from the array [expression]. */
internal class ArrayRemove<E, T : List<E>?>(
    val expression: Expression<T>,
    val element: E,
    columnType: IColumnType<T & Any>
) : Function<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            append("array_remove(")
            append(expression, ",", element.toString())
            append(")")
        }
    }
}

/** Adds the specified [element] to the end of this array expression. */
internal fun <E, T : List<E>?> ExpressionWithColumnType<T>.add(
    element: E
): ArrayAdd<E, T> = ArrayAdd(this, element, columnType)

/** Representation of the array concatenation operator that adds an [element] onto the end of the array [expression]. */
internal class ArrayAdd<E, T : List<E>?>(
    val expression: Expression<T>,
    val element: E,
    columnType: IColumnType<T & Any>
) : Function<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            append(element.toString(), " || ", expression)
        }
    }
}

/** Returns the length of this array expression. */
internal fun <E, T : List<E>?> ExpressionWithColumnType<T>.arrayLength(): ArrayLength<E, T> = ArrayLength(this)

/** Representation of the function that returns the length of the 1-dimensional array [expression]. */
internal class ArrayLength<E, T : List<E>?>(
    val expression: Expression<T>,
) : Function<Int?>(IntegerColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            append("array_length(", expression, ", 1)")
        }
    }
}
