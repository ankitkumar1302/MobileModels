package dev.ankitkumar1302.gptmobile.data.dto.anthropic.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDetail(

    @SerialName("type")
    val type: String,

    @SerialName("message")
    val message: String
)
