package dev.bogwalk.bootrack.backend.schema.custom

import org.jetbrains.exposed.v1.core.CustomFunction
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.stringParam

/** Representation of the FTS function that assigns the specified [weight] to each element of the specified [vector]. */
internal class SetWeight<T : String?>(
    vector: Expression<T>,
    weight: Expression<T>,
) : CustomFunction<String>(
    functionName = "setweight",
    columnType = TsvectorColumnType(),
    expr = arrayOf(vector, weight)
)

/**
 * Representation of the FTS function that converts the specified text [document] to PG-specific `tsvector` type,
 * with normalization according to the default 'english' configuration.
 */
internal class ToTSVector<T : String?>(
    document: Expression<T>
) : CustomFunction<String>(
    functionName = "to_tsvector",
    columnType = TsvectorColumnType(),
    expr = arrayOf(stringParam("english"), document)
)
