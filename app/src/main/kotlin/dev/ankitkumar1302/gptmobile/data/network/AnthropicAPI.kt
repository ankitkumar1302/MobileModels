package dev.ankitkumar1302.gptmobile.data.network

import dev.ankitkumar1302.gptmobile.data.dto.anthropic.request.MessageRequest
import dev.ankitkumar1302.gptmobile.data.dto.anthropic.response.MessageResponseChunk
import kotlinx.coroutines.flow.Flow

interface AnthropicAPI {
    fun setToken(token: String?)
    fun setAPIUrl(url: String)
    fun streamChatMessage(messageRequest: MessageRequest): Flow<MessageResponseChunk>
}
