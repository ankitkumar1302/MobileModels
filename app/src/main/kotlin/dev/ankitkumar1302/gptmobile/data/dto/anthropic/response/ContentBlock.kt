package dev.ankitkumar1302.gptmobile.data.dto.anthropic.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentBlock(

    @SerialName("type")
    val type: ContentBlockType,

    @SerialName("text")
    val text: String
)
