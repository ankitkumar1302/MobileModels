package dev.ankitkumar1302.gptmobile.data.dto.anthropic.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("message_stop")
data object MessageStopResponseChunk : MessageResponseChunk()
