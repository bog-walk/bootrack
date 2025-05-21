package dev.bogwalk.bootrack.backend.repository.custom

import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

/** Database function that sets any "modified_at" column in the triggered table to store the current timestamp. */
internal const val SET_MODIFIED_AT_FUNCTION = """
    CREATE OR REPLACE FUNCTION set_modified_at()
    RETURNS trigger AS $$
        BEGIN
            NEW.modified_at = now();
            RETURN NEW;
        END;
    $$ LANGUAGE plpgsql;
"""

/** Database trigger for [dev.bogwalk.bootrack.backend.schema.tables.Comments] on every record update. */
internal const val ON_UPDATE_COMMENT_SET_MODIFIED_AT_TRIGGER = """
    CREATE OR REPLACE TRIGGER on_update_comment_set_modified_at
        BEFORE UPDATE ON "comments"
        FOR EACH ROW
        EXECUTE FUNCTION set_modified_at();
"""

internal fun JdbcTransaction.createAutoUpdateTriggers() {
    exec(SET_MODIFIED_AT_FUNCTION.trimIndent())

    exec(ON_UPDATE_COMMENT_SET_MODIFIED_AT_TRIGGER.trimIndent())
}
