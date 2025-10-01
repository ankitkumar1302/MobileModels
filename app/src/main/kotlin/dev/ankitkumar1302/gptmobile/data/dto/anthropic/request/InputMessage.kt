package dev.ankitkumar1302.gptmobile.data.dto.anthropic.request

import dev.ankitkumar1302.gptmobile.data.dto.anthropic.common.MessageContent
import dev.ankitkumar1302.gptmobile.data.dto.anthropic.common.MessageRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InputMessage(
    @SerialName("role")
    val role: MessageRole,

    @SerialName("content")
    val content: List<MessageContent>
)
