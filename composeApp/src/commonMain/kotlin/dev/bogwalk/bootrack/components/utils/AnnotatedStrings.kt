package dev.bogwalk.bootrack.components.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import dev.bogwalk.bootrack.model.Comment

@Composable
internal fun annotatedStringWithMention(original: String): AnnotatedString = buildAnnotatedString {
    append(original)
    val regex = Regex("@[\\w.]+")
    regex.findAll(original).forEach { result ->
        addStyle(
            style = userMentionBaseStyle(),
            start = result.range.first,
            end = result.range.last + 1
        )
    }
}

@Composable
private fun userMentionBaseStyle(): SpanStyle = SpanStyle(
    color = MaterialTheme.colorScheme.tertiary,
    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
    fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
)

internal fun Comment.getNewMentions(): List<String> {
    val regex = Regex("@[\\w.]+")
    return regex.findAll(this.content).map { it.value.drop(1) }.toList()
}
