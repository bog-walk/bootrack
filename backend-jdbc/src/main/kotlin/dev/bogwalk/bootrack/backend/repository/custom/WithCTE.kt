package dev.bogwalk.bootrack.backend.repository.custom

import org.jetbrains.exposed.v1.core.AbstractQuery
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.IExpressionAlias
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager

/**
 * Base class representing a common table expression or a temporary table, which exists for the purpose of a single query.
 *
 * @param query The auxiliary statement that is meant to be used as part of a larger query.
 */
internal open class With(
    name: String,
    val query: AbstractQuery<*>
) : Table(name) {
    // other database-specific optional view parameters

    final override val tableName: String = name

    override val fields: List<Expression<*>>
        get() = getQueryFields()

    fun prepareSQL(queryBuilder: QueryBuilder) {
        queryBuilder {
            append("WITH ")
            this@With.describe(TransactionManager.current(), queryBuilder)
            append(" AS (")
            query.prepareSQL(queryBuilder)
            append(") ")
        }
    }

    final override fun createStatement(): List<String> {
        error("CREATE statements are not supported by CTEs")
    }

    final override fun modifyStatement(): List<String> {
        error("ALTER statements are not supported by CTEs")
    }

    final override fun dropStatement(): List<String> {
        error("DROP statements are not supported by CTEs")
    }

    operator fun <T> get(delegate: Column<T>): Column<T> {
        return fields.firstOrNull { it is Column<*> && it.name == delegate.name } as? Column<T>
            ?: error("Column ${delegate.name} not found in query set")
    }

    operator fun <T> get(delegate: IExpressionAlias<T>): Expression<T> {
        return fields.firstOrNull { it is Expression<*> && it.toString() == delegate.alias } as? Expression<T>
            ?: error("Expression not found in query set")
    }

    protected fun getQueryFields(
        fields: List<Expression<*>> = query.set.fields
    ): List<Expression<*>> = fields.map { field ->
        when (field) {
            is Column<*> -> Column(this, field.name, field.columnType)
            is IExpressionAlias<*> -> field.aliasOnlyExpression()
            else -> field
        }
    }
}

/** Creates a custom `SELECT` query by selecting all fields from the prepended `WITH()` expression. */
internal fun With.selectAll(): CTEQuery = CTEQuery(this)

/** Custom class representing an SQL `SELECT` statement that is prepended by `WITH()` */
internal class CTEQuery(
    val cte: With
) : Query(cte.source, null) {

    override fun prepareSQL(builder: QueryBuilder): String {
        cte.prepareSQL(builder)
        return super.prepareSQL(builder)
    }

    override fun copy(): CTEQuery = CTEQuery(cte).also { copy ->
        copyTo(copy)
    }
}
