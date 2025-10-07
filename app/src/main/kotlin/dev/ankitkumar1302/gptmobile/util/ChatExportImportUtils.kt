package dev.ankitkumar1302.gptmobile.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import dev.ankitkumar1302.gptmobile.data.database.entity.ChatRoom
import dev.ankitkumar1302.gptmobile.data.database.entity.Message
import dev.ankitkumar1302.gptmobile.data.model.ChatTranscript
import dev.ankitkumar1302.gptmobile.data.model.ChatRoomTranscript
import dev.ankitkumar1302.gptmobile.data.model.MessageTranscript
import dev.ankitkumar1302.gptmobile.data.model.ApiType
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Storage Access Framework helpers for chat export/import functionality
 */
object ChatExportImportUtils {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Converts a ChatRoom and its messages to a ChatTranscript for export
     */
    fun createChatTranscript(chatRoom: ChatRoom, messages: List<Message>): ChatTranscript {
        val chatRoomTranscript = ChatRoomTranscript(
            title = chatRoom.title,
            enabledPlatforms = chatRoom.enabledPlatform,
            createdAt = chatRoom.createdAt
        )

        val messageTranscripts = messages.map { message ->
            MessageTranscript(
                content = message.content,
                imageData = message.imageData,
                platformType = message.platformType,
                createdAt = message.createdAt,
                isUserMessage = message.platformType == null
            )
        }

        return ChatTranscript(
            chatRoom = chatRoomTranscript,
            messages = messageTranscripts
        )
    }

    /**
     * Converts a ChatTranscript back to ChatRoom and Messages for import
     */
    fun parseChatTranscript(transcript: ChatTranscript): Pair<ChatRoom, List<Message>> {
        val chatRoom = ChatRoom(
            title = transcript.chatRoom.title,
            enabledPlatform = transcript.chatRoom.enabledPlatforms,
            createdAt = transcript.chatRoom.createdAt
        )

        val messages = transcript.messages.mapIndexed { index, messageTranscript ->
            Message(
                chatId = 0, // Will be set when saving to database
                content = messageTranscript.content,
                imageData = messageTranscript.imageData,
                linkedMessageId = if (messageTranscript.isUserMessage) 0 else index,
                platformType = messageTranscript.platformType,
                createdAt = messageTranscript.createdAt
            )
        }

        return Pair(chatRoom, messages)
    }

    /**
     * Exports chat transcript to JSON file using Storage Access Framework
     */
    fun exportChatToJson(
        context: Context,
        transcript: ChatTranscript,
        createDocumentLauncher: ActivityResultLauncher<String>
    ) {
        try {
            val jsonContent = json.encodeToString(ChatTranscript.serializer(), transcript)
            val fileName = "chat_export_${transcript.chatRoom.title}_${System.currentTimeMillis()}.json"

            // Create a temporary file to hold the data
            val tempFile = File(context.cacheDir, fileName)
            tempFile.writeText(jsonContent)

            // Use SAF to let user choose where to save
            createDocumentLauncher.launch(fileName)

        } catch (e: Exception) {
            Timber.e(e, "Failed to export chat to JSON")
            throw e
        }
    }

    /**
     * Imports chat transcript from JSON file using Storage Access Framework
     */
    fun importChatFromJson(
        context: Context,
        uri: Uri
    ): ChatTranscript {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: throw IllegalStateException("Could not read file content")

            return json.decodeFromString(ChatTranscript.serializer(), content)

        } catch (e: Exception) {
            Timber.e(e, "Failed to import chat from JSON")
            throw e
        }
    }

    /**
     * Shares chat transcript as JSON using ACTION_SEND
     */
    fun shareChatTranscript(context: Context, transcript: ChatTranscript) {
        try {
            val jsonContent = json.encodeToString(ChatTranscript.serializer(), transcript)
            val fileName = "chat_${transcript.chatRoom.title}.json"

            // Create a temporary file for sharing
            val tempFile = File(context.cacheDir, fileName)
            tempFile.writeText(jsonContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Chat Export: ${transcript.chatRoom.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Chat")
            context.startActivity(chooser)

        } catch (e: Exception) {
            Timber.e(e, "Failed to share chat transcript")
            throw e
        }
    }

    /**
     * Creates a document creation launcher for export
     * Note: This should be used within a Composable with rememberLauncherForActivityResult
     */
    fun handleExportResult(
        context: Context,
        transcript: ChatTranscript,
        uri: Uri?
    ) {
        if (uri != null) {
            try {
                val jsonContent = json.encodeToString(ChatTranscript.serializer(), transcript)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonContent.toByteArray())
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to write chat transcript to document")
                throw e
            }
        }
    }
}
