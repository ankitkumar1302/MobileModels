package dev.ankitkumar1302.gptmobile.data.dto.anthropic.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContentBlockType {

    @SerialName("text")
    TEXT,

    @SerialName("text_delta")
    DELTA
}
