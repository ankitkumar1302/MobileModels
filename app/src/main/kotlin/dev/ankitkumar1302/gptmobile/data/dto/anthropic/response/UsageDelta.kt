package dev.ankitkumar1302.gptmobile.data.dto.anthropic.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsageDelta(

    @SerialName("output_tokens")
    val outputTokens: Int
)
