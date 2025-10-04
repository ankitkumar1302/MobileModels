package dev.ankitkumar1302.gptmobile.presentation.ui.chat

import android.text.util.Linkify
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ankitkumar1302.gptmobile.R
import dev.ankitkumar1302.gptmobile.data.model.ApiType
import dev.ankitkumar1302.gptmobile.presentation.theme.GPTMobileTheme
import dev.ankitkumar1302.gptmobile.util.getPlatformAPIBrandText
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun UserChatBubble(
    modifier: Modifier = Modifier,
    text: String,
    isLoading: Boolean,
    onEditClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        Card(
            modifier = modifier.semantics {
                contentDescription = "Your message: $text"
            },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            MarkdownText(
                modifier = Modifier.padding(16.dp),
                markdown = text,
                isTextSelectable = true,
                linkifyMask = Linkify.WEB_URLS
            )
        }
        Row {
            if (!isLoading) {
                EditTextChip(onEditClick)
                Spacer(modifier = Modifier.width(8.dp))
            }
            CopyTextChip(onCopyClick)
        }
    }
}

@Composable
fun OpponentChatBubble(
    modifier: Modifier = Modifier,
    canRetry: Boolean,
    isLoading: Boolean,
    isError: Boolean = false,
    apiType: ApiType,
    text: String,
    onCopyClick: () -> Unit = {},
    onRetryClick: () -> Unit = {}
) {
    val platformName = getPlatformAPIBrandText(apiType)
    val loadingText = stringResource(R.string.loading)
    val errorText = stringResource(R.string.error)
    val statusText = when {
        isLoading -> "$loadingText response from $platformName"
        isError -> "$errorText from $platformName"
        else -> "Response from $platformName"
    }

    Column(modifier = modifier.semantics {
        contentDescription = "$statusText: $text"
    }) {
        Column(horizontalAlignment = Alignment.End) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                MarkdownText(
                    modifier = Modifier.padding(24.dp),
                    markdown = text.trimIndent() + if (isLoading) "▊" else "",
                    isTextSelectable = true,
                    linkifyMask = Linkify.WEB_URLS
                )
                if (!isLoading) {
                    BrandText(apiType)
                }
            }

            if (!isLoading) {
                Row {
                    if (!isError) {
                        CopyTextChip(onCopyClick)
                    }
                    if (canRetry) {
                        Spacer(modifier = Modifier.width(8.dp))
                        RetryChip(onRetryClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditTextChip(onEditClick: () -> Unit) {
    val editQuestionText = stringResource(R.string.edit_question)
    AssistChip(
        onClick = onEditClick,
        label = { Text(editQuestionText) },
        leadingIcon = {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = null, // Icon is decorative when label is present
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        modifier = Modifier.semantics {
            contentDescription = editQuestionText
        }
    )
}

@Composable
private fun CopyTextChip(onCopyClick: () -> Unit) {
    val copyText = stringResource(R.string.copy_text)
    AssistChip(
        onClick = onCopyClick,
        label = { Text(copyText) },
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_copy),
                contentDescription = null, // Icon is decorative when label is present
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        modifier = Modifier.semantics {
            contentDescription = copyText
        }
    )
}

@Composable
private fun RetryChip(onRetryClick: () -> Unit) {
    val retryText = stringResource(R.string.retry)
    AssistChip(
        onClick = onRetryClick,
        label = { Text(retryText) },
        leadingIcon = {
            Icon(
                Icons.Rounded.Refresh,
                contentDescription = null, // Icon is decorative when label is present
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        modifier = Modifier.semantics {
            contentDescription = retryText
        }
    )
}

@Composable
private fun BrandText(apiType: ApiType) {
    Box(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterEnd),
            text = getPlatformAPIBrandText(apiType),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun UserChatBubblePreview() {
    val sampleText = """
        How can I print hello world
        in Python?
    """.trimIndent()
    GPTMobileTheme {
        UserChatBubble(text = sampleText, isLoading = false, onCopyClick = {}, onEditClick = {})
    }
}

@Preview
@Composable
fun OpponentChatBubblePreview() {
    val sampleText = """
        # Demo
    
        Emphasis, aka italics, with *asterisks* or _underscores_. Strong emphasis, aka bold, with **asterisks** or __underscores__. Combined emphasis with **asterisks and _underscores_**. [Links with two blocks, text in square-brackets, destination is in parentheses.](https://www.example.com). Inline `code` has `back-ticks around` it.
    
        1. First ordered list item
        2. Another item
            * Unordered sub-list.
        3. And another item.
            You can have properly indented paragraphs within list items. Notice the blank line above, and the leading spaces (at least one, but we'll use three here to also align the raw Markdown).
    
        * Unordered list can use asterisks
        - Or minuses
        + Or pluses
    """.trimIndent()
    GPTMobileTheme {
        OpponentChatBubble(
            text = sampleText,
            canRetry = true,
            isLoading = false,
            apiType = ApiType.OPENAI,
            onCopyClick = {},
            onRetryClick = {}
        )
    }
}
