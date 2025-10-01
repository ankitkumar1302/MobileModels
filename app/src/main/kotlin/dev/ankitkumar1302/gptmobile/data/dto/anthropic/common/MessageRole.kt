package dev.ankitkumar1302.gptmobile.data.dto.anthropic.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {

    @SerialName("user")
    USER,

    @SerialName("assistant")
    ASSISTANT
}
