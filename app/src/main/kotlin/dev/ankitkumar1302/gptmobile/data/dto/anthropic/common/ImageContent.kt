package dev.ankitkumar1302.gptmobile.data.dto.anthropic.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("image")
data class ImageContent(

    @SerialName("source")
    val source: ImageSource
) : MessageContent()
