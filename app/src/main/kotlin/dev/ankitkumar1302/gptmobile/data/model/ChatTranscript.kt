package dev.ankitkumar1302.gptmobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatTranscript(
    val version: String = "1.0",
    val appName: String = "Mobile Models",
    val exportTimestamp: Long = System.currentTimeMillis(),
    val chatRoom: ChatRoomTranscript,
    val messages: List<MessageTranscript>
)

@Serializable
data class ChatRoomTranscript(
    val title: String,
    val enabledPlatforms: List<ApiType>,
    val createdAt: Long
)

@Serializable
data class MessageTranscript(
    val content: String,
    val imageData: String? = null,
    val platformType: ApiType?,
    val createdAt: Long,
    val isUserMessage: Boolean
)
