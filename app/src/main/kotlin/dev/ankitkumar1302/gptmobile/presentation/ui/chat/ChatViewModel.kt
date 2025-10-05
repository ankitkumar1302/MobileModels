package dev.ankitkumar1302.gptmobile.presentation.ui.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ankitkumar1302.gptmobile.data.database.entity.ChatRoom
import dev.ankitkumar1302.gptmobile.data.database.entity.Message
import dev.ankitkumar1302.gptmobile.data.dto.ApiState
import dev.ankitkumar1302.gptmobile.data.model.ApiType
import dev.ankitkumar1302.gptmobile.data.repository.ChatRepository
import dev.ankitkumar1302.gptmobile.data.repository.SettingRepository
import dev.ankitkumar1302.gptmobile.util.handleStates
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {
    sealed class LoadingState {
        data object Idle : LoadingState()
        data object Loading : LoadingState()
    }

    data class ChatUiState(
        val chatRoom: ChatRoom = ChatRoom(id = -1, title = "", enabledPlatform = emptyList()),
        val messages: List<Message> = emptyList(),
        val currentQuestion: String = "",
        val isIdle: Boolean = true,
        val isLoaded: Boolean = false,
        val isChatTitleDialogOpen: Boolean = false,
        val isEditQuestionDialogOpen: Boolean = false,
        val editedQuestion: Message? = null,
        val enabledPlatformsInApp: List<ApiType> = emptyList(),
        val platformLoadingStates: Map<ApiType, LoadingState> = emptyMap(),
        val activeUserMessage: Message? = null,
        val activePlatformMessages: Map<ApiType, Message> = emptyMap(),
        val geminiNanoMessage: Message? = null,
        val geminiNanoLoadingState: LoadingState = LoadingState.Idle
    )

    private val chatRoomId: Int = savedStateHandle.get<Int>("chatRoomId") ?: 0
    private val enabledPlatformString: String = savedStateHandle.get<String>("enabledPlatforms") ?: ""
    val enabledPlatformsInChat: List<ApiType> = if (enabledPlatformString.isNotBlank()) {
        enabledPlatformString.split(',')
            .mapNotNull { s ->
                try {
                    ApiType.valueOf(s.trim())
                } catch (e: IllegalArgumentException) {
                    // Log error and skip invalid enum values
                    android.util.Log.w("ChatViewModel", "Invalid ApiType value: $s", e)
                    null
                }
            }
    } else {
        emptyList()
    }
    private val currentTimeStamp: Long
        get() = System.currentTimeMillis() / 1000

    private val _uiState = MutableStateFlow(
        ChatUiState(
            chatRoom = ChatRoom(id = -1, title = "", enabledPlatform = enabledPlatformsInChat),
            platformLoadingStates = ApiType.entries.associateWith { LoadingState.Idle },
            activePlatformMessages = ApiType.entries.associateWith { 
                Message(chatId = chatRoomId, content = "", platformType = it) 
            },
            activeUserMessage = Message(chatId = chatRoomId, content = "", platformType = null),
            editedQuestion = Message(chatId = chatRoomId, content = "", platformType = null),
            geminiNanoMessage = Message(chatId = chatRoomId, content = "", platformType = null)
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Backward compatibility properties - delegate to uiState
    val chatRoom: StateFlow<ChatRoom> = uiState.map { it.chatRoom }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), ChatRoom(id = -1, title = "", enabledPlatform = enabledPlatformsInChat))
    val isChatTitleDialogOpen: StateFlow<Boolean> = uiState.map { it.isChatTitleDialogOpen }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), false)
    val isEditQuestionDialogOpen: StateFlow<Boolean> = uiState.map { it.isEditQuestionDialogOpen }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), false)
    val enabledPlatformsInApp: StateFlow<List<ApiType>> = uiState.map { it.enabledPlatformsInApp }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), emptyList())
    val messages: StateFlow<List<Message>> = uiState.map { it.messages }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), emptyList())
    val question: StateFlow<String> = uiState.map { it.currentQuestion }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), "")
    val editedQuestion: StateFlow<Message> = uiState.map { it.editedQuestion ?: Message(chatId = chatRoomId, content = "", platformType = null) }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), Message(chatId = chatRoomId, content = "", platformType = null))
    val openaiLoadingState: StateFlow<LoadingState> = uiState.map { it.platformLoadingStates[ApiType.OPENAI] ?: LoadingState.Idle }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), LoadingState.Idle)
    val anthropicLoadingState: StateFlow<LoadingState> = uiState.map { it.platformLoadingStates[ApiType.ANTHROPIC] ?: LoadingState.Idle }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), LoadingState.Idle)
    val googleLoadingState: StateFlow<LoadingState> = uiState.map { it.platformLoadingStates[ApiType.GOOGLE] ?: LoadingState.Idle }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), LoadingState.Idle)
    val groqLoadingState: StateFlow<LoadingState> = uiState.map { it.platformLoadingStates[ApiType.GROQ] ?: LoadingState.Idle }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), LoadingState.Idle)
    val ollamaLoadingState: StateFlow<LoadingState> = uiState.map { it.platformLoadingStates[ApiType.OLLAMA] ?: LoadingState.Idle }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), LoadingState.Idle)
    val geminiNanoLoadingState: StateFlow<LoadingState> = uiState.map { it.geminiNanoLoadingState }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), LoadingState.Idle)
    val isIdle: StateFlow<Boolean> = uiState.map { it.isIdle }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), true)
    val isLoaded: StateFlow<Boolean> = uiState.map { it.isLoaded }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), false)
    val userMessage: StateFlow<Message> = uiState.map { it.activeUserMessage ?: Message(chatId = chatRoomId, content = "", platformType = null) }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), Message(chatId = chatRoomId, content = "", platformType = null))
    val openAIMessage: StateFlow<Message> = uiState.map { it.activePlatformMessages[ApiType.OPENAI] ?: Message(chatId = chatRoomId, content = "", platformType = ApiType.OPENAI) }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), Message(chatId = chatRoomId, content = "", platformType = ApiType.OPENAI))
    val anthropicMessage: StateFlow<Message> = uiState.map { it.activePlatformMessages[ApiType.ANTHROPIC] ?: Message(chatId = chatRoomId, content = "", platformType = ApiType.ANTHROPIC) }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), Message(chatId = chatRoomId, content = "", platformType = ApiType.ANTHROPIC))
    val googleMessage: StateFlow<Message> = uiState.map { it.activePlatformMessages[ApiType.GOOGLE] ?: Message(chatId = chatRoomId, content = "", platformType = ApiType.GOOGLE) }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), Message(chatId = chatRoomId, content = "", platformType = ApiType.GOOGLE))
    val groqMessage: StateFlow<Message> = uiState.map { it.activePlatformMessages[ApiType.GROQ] ?: Message(chatId = chatRoomId, content = "", platformType = ApiType.GROQ) }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), Message(chatId = chatRoomId, content = "", platformType = ApiType.GROQ))
    val ollamaMessage: StateFlow<Message> = uiState.map { it.activePlatformMessages[ApiType.OLLAMA] ?: Message(chatId = chatRoomId, content = "", platformType = ApiType.OLLAMA) }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), Message(chatId = chatRoomId, content = "", platformType = ApiType.OLLAMA))
    val geminiNanoMessage: StateFlow<Message> = uiState.map { it.geminiNanoMessage ?: Message(chatId = chatRoomId, content = "", platformType = null) }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), Message(chatId = chatRoomId, content = "", platformType = null))

    // Flows for assistant message streams
    private val openAIFlow = MutableSharedFlow<ApiState>()
    private val anthropicFlow = MutableSharedFlow<ApiState>()
    private val googleFlow = MutableSharedFlow<ApiState>()
    private val groqFlow = MutableSharedFlow<ApiState>()
    private val ollamaFlow = MutableSharedFlow<ApiState>()
    private val geminiNanoFlow = MutableSharedFlow<ApiState>()

    init {
        Log.d("ViewModel", "$chatRoomId")
        Log.d("ViewModel", "$enabledPlatformsInChat")
        fetchChatRoom()
        viewModelScope.launch { fetchMessages() }
        fetchEnabledPlatformsInApp()
        observeFlow()
    }

    fun askQuestion() {
        Log.d("Question: ", _uiState.value.currentQuestion)
        _uiState.update { 
            it.copy(
                activeUserMessage = it.activeUserMessage?.copy(
                    content = it.currentQuestion, 
                    createdAt = currentTimeStamp
                ),
                currentQuestion = ""
            )
        }
        completeChat()
    }

    fun closeChatTitleDialog() {
        _uiState.update { it.copy(isChatTitleDialogOpen = false) }
    }

    fun closeEditQuestionDialog() {
        _uiState.update { 
            it.copy(
                editedQuestion = Message(chatId = chatRoomId, content = "", platformType = null),
                isEditQuestionDialogOpen = false
            )
        }
    }

    fun editQuestion(q: Message) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.filter { message -> message.id < q.id && message.createdAt < q.createdAt },
                activeUserMessage = state.activeUserMessage?.copy(content = q.content, createdAt = currentTimeStamp)
            )
        }
        completeChat()
    }

    fun openChatTitleDialog() {
        _uiState.update { it.copy(isChatTitleDialogOpen = true) }
    }

    fun openEditQuestionDialog(question: Message) {
        _uiState.update { 
            it.copy(
                editedQuestion = question,
                isEditQuestionDialogOpen = true
            )
        }
    }

    fun generateDefaultChatTitle(): String? = chatRepository.generateDefaultChatTitle(_uiState.value.messages)

    fun generateAIChatTitle() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    geminiNanoLoadingState = LoadingState.Loading,
                    geminiNanoMessage = state.geminiNanoMessage?.copy(content = "")
                )
            }
        }
    }

    fun retryQuestion(message: Message) {
        val state = _uiState.value
        val latestQuestionIndex = state.messages.indexOfLast { it.platformType == null }

        if (latestQuestionIndex != -1 && state.isIdle) {
            val previousAnswers = enabledPlatformsInChat.mapNotNull { apiType -> 
                state.messages.lastOrNull { it.platformType == apiType } 
            }

            _uiState.update { currentState ->
                val newMessages = currentState.messages - setOf(currentState.messages[latestQuestionIndex]) - previousAnswers.toSet()
                val updatedPlatformMessages = currentState.activePlatformMessages.toMutableMap()
                
                // Restore messages that are not retrying
                enabledPlatformsInChat.forEach { apiType ->
                    if (apiType != message.platformType) {
                        previousAnswers.firstOrNull { it.platformType == apiType }?.let { msg ->
                            updatedPlatformMessages[apiType] = msg
                        }
                    }
                }

                currentState.copy(
                    activeUserMessage = currentState.messages[latestQuestionIndex],
                    messages = newMessages,
                    activePlatformMessages = updatedPlatformMessages
                )
            }
        }
        
        message.platformType?.let { apiType ->
            updateLoadingState(apiType, LoadingState.Loading)
            _uiState.update { state ->
                state.copy(
                    activePlatformMessages = state.activePlatformMessages.toMutableMap().apply {
                        this[apiType] = this[apiType]?.copy(id = message.id, content = "", createdAt = currentTimeStamp)
                            ?: Message(chatId = chatRoomId, content = "", platformType = apiType)
                    }
                )
            }
        }

        when (message.platformType) {
            ApiType.OPENAI -> completeOpenAIChat()
            ApiType.ANTHROPIC -> completeAnthropicChat()
            ApiType.GOOGLE -> completeGoogleChat()
            ApiType.GROQ -> completeGroqChat()
            ApiType.OLLAMA -> completeOllamaChat()
            else -> {}
        }
    }

    fun updateChatTitle(title: String) {
        // Should be only used for changing chat title after the chatroom is created.
        if (_uiState.value.chatRoom.id > 0) {
            _uiState.update { it.copy(chatRoom = it.chatRoom.copy(title = title)) }
            viewModelScope.launch {
                chatRepository.updateChatTitle(_uiState.value.chatRoom, title)
            }
        }
    }

    fun exportChat(): Pair<String, String> {
        // Build the chat history in Markdown format
        val chatHistoryMarkdown = buildString {
            appendLine("# Chat Export: \"${chatRoom.value.title}\"")
            appendLine()
            appendLine("**Exported on:** ${formatCurrentDateTime()}")
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Chat History")
            appendLine()
            messages.value.forEach { message ->
                val sender = if (message.platformType == null) "User" else "Assistant"
                appendLine("**$sender:**")
                appendLine(message.content)
                appendLine()
            }
        }

        // Save the Markdown file
        val fileName = "export_${chatRoom.value.title}_${System.currentTimeMillis()}.md"
        return Pair(fileName, chatHistoryMarkdown)
    }

    private fun formatCurrentDateTime(): String {
        val currentDate = java.util.Date()
        val format = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault())
        return format.format(currentDate)
    }

    fun updateQuestion(q: String) {
        _uiState.update { it.copy(currentQuestion = q) }
    }

    private fun addMessage(message: Message) {
        _uiState.update { it.copy(messages = it.messages + listOf(message)) }
    }

    private fun clearQuestionAndAnswers() {
        _uiState.update { state ->
            state.copy(
                activeUserMessage = state.activeUserMessage?.copy(id = 0, content = ""),
                activePlatformMessages = state.activePlatformMessages.mapValues { (_, msg) ->
                    msg.copy(id = 0, content = "")
                }
            )
        }
    }

    private fun completeChat() {
        enabledPlatformsInChat.forEach { apiType -> updateLoadingState(apiType, LoadingState.Loading) }
        val enabledPlatforms = enabledPlatformsInChat.toSet()

        if (ApiType.OPENAI in enabledPlatforms) {
            completeOpenAIChat()
        }

        if (ApiType.ANTHROPIC in enabledPlatforms) {
            completeAnthropicChat()
        }

        if (ApiType.GOOGLE in enabledPlatforms) {
            completeGoogleChat()
        }

        if (ApiType.GROQ in enabledPlatforms) {
            completeGroqChat()
        }

        if (ApiType.OLLAMA in enabledPlatforms) {
            completeOllamaChat()
        }
    }

    private fun completeAnthropicChat() {
        viewModelScope.launch {
            val state = _uiState.value
            state.activeUserMessage?.let { userMsg ->
                val chatFlow = chatRepository.completeAnthropicChat(question = userMsg, history = state.messages)
                chatFlow.collect { chunk -> anthropicFlow.emit(chunk) }
            }
        }
    }

    private fun completeGoogleChat() {
        viewModelScope.launch {
            val state = _uiState.value
            state.activeUserMessage?.let { userMsg ->
                val chatFlow = chatRepository.completeGoogleChat(question = userMsg, history = state.messages)
                chatFlow.collect { chunk -> googleFlow.emit(chunk) }
            }
        }
    }

    private fun completeGroqChat() {
        viewModelScope.launch {
            val state = _uiState.value
            state.activeUserMessage?.let { userMsg ->
                val chatFlow = chatRepository.completeGroqChat(question = userMsg, history = state.messages)
                chatFlow.collect { chunk -> groqFlow.emit(chunk) }
            }
        }
    }

    private fun completeOllamaChat() {
        viewModelScope.launch {
            val state = _uiState.value
            state.activeUserMessage?.let { userMsg ->
                val chatFlow = chatRepository.completeOllamaChat(question = userMsg, history = state.messages)
                chatFlow.collect { chunk -> ollamaFlow.emit(chunk) }
            }
        }
    }

    private fun completeOpenAIChat() {
        viewModelScope.launch {
            val state = _uiState.value
            state.activeUserMessage?.let { userMsg ->
                val chatFlow = chatRepository.completeOpenAIChat(question = userMsg, history = state.messages)
                chatFlow.collect { chunk -> openAIFlow.emit(chunk) }
            }
        }
    }

    private suspend fun fetchMessages() {
        // If the room isn't new
        if (chatRoomId != 0) {
            val messages = chatRepository.fetchMessages(chatRoomId)
            _uiState.update { it.copy(messages = messages, isLoaded = true) }
            return
        }

        // When message id should sync after saving chats
        if (_uiState.value.chatRoom.id != 0) {
            val messages = chatRepository.fetchMessages(_uiState.value.chatRoom.id)
            _uiState.update { it.copy(messages = messages) }
            return
        }
    }

    private fun fetchChatRoom() {
        viewModelScope.launch {
            val chatRoom = if (chatRoomId == 0) {
                ChatRoom(id = 0, title = "Untitled Chat", enabledPlatform = enabledPlatformsInChat)
            } else {
                chatRepository.fetchChatList().firstOrNull { it.id == chatRoomId }
                    ?: ChatRoom(id = chatRoomId, title = "Chat Not Found", enabledPlatform = enabledPlatformsInChat)
            }
            _uiState.update { it.copy(chatRoom = chatRoom) }
            Log.d("ViewModel", "chatroom: $chatRoom")
        }
    }

    private fun fetchEnabledPlatformsInApp() {
        viewModelScope.launch {
            val enabled = settingRepository.fetchPlatforms().filter { it.enabled }.map { it.name }
            _uiState.update { it.copy(enabledPlatformsInApp = enabled) }
        }
    }

    private fun observeFlow() {
        // Create message flow wrappers that update state
        val messageFlowWrappers = mapOf(
            ApiType.OPENAI to MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = ApiType.OPENAI)),
            ApiType.ANTHROPIC to MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = ApiType.ANTHROPIC)),
            ApiType.GOOGLE to MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = ApiType.GOOGLE)),
            ApiType.GROQ to MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = ApiType.GROQ)),
            ApiType.OLLAMA to MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = ApiType.OLLAMA))
        )

        viewModelScope.launch {
            openAIFlow.handleStates(
                messageFlow = messageFlowWrappers[ApiType.OPENAI]!!,
                onLoadingComplete = { updateLoadingState(ApiType.OPENAI, LoadingState.Idle) }
            )
        }

        viewModelScope.launch {
            anthropicFlow.handleStates(
                messageFlow = messageFlowWrappers[ApiType.ANTHROPIC]!!,
                onLoadingComplete = { updateLoadingState(ApiType.ANTHROPIC, LoadingState.Idle) }
            )
        }

        viewModelScope.launch {
            googleFlow.handleStates(
                messageFlow = messageFlowWrappers[ApiType.GOOGLE]!!,
                onLoadingComplete = { updateLoadingState(ApiType.GOOGLE, LoadingState.Idle) }
            )
        }

        viewModelScope.launch {
            groqFlow.handleStates(
                messageFlow = messageFlowWrappers[ApiType.GROQ]!!,
                onLoadingComplete = { updateLoadingState(ApiType.GROQ, LoadingState.Idle) }
            )
        }

        viewModelScope.launch {
            ollamaFlow.handleStates(
                messageFlow = messageFlowWrappers[ApiType.OLLAMA]!!,
                onLoadingComplete = { updateLoadingState(ApiType.OLLAMA, LoadingState.Idle) }
            )
        }

        // Update state from message flow wrappers
        messageFlowWrappers.forEach { (apiType, flow) ->
            viewModelScope.launch {
                flow.collect { message ->
                    _uiState.update { state ->
                        state.copy(
                            activePlatformMessages = state.activePlatformMessages.toMutableMap().apply {
                                this[apiType] = message
                            }
                        )
                    }
                }
            }
        }

        val geminiNanoMessageFlow = MutableStateFlow(Message(chatId = chatRoomId, content = "", platformType = null))
        viewModelScope.launch {
            geminiNanoFlow.handleStates(
                messageFlow = geminiNanoMessageFlow,
                onLoadingComplete = { 
                    _uiState.update { it.copy(geminiNanoLoadingState = LoadingState.Idle) }
                }
            )
        }

        viewModelScope.launch {
            geminiNanoMessageFlow.collect { message ->
                _uiState.update { it.copy(geminiNanoMessage = message) }
            }
        }

        viewModelScope.launch {
            _uiState.map { it.isIdle }.collect { status ->
                if (status) {
                    val state = _uiState.value
                    Log.d("status", "val: ${state.activeUserMessage}")
                    if (state.chatRoom.id != -1 && state.activeUserMessage?.content?.isNotBlank() == true) {
                        syncQuestionAndAnswers()
                        Log.d("message", "${state.messages}")
                        val updatedRoom = chatRepository.saveChat(state.chatRoom, state.messages)
                        _uiState.update { it.copy(chatRoom = updatedRoom) }
                        fetchMessages() // For syncing message ids
                    }
                    clearQuestionAndAnswers()
                }
            }
        }
    }

    private fun syncQuestionAndAnswers() {
        val state = _uiState.value
        state.activeUserMessage?.let { addMessage(it) }
        
        val enabledPlatforms = enabledPlatformsInChat.toSet()
        enabledPlatforms.forEach { apiType ->
            state.activePlatformMessages[apiType]?.let { message ->
                addMessage(message)
            }
        }
    }

    private fun updateLoadingState(apiType: ApiType, loadingState: LoadingState) {
        _uiState.update { state ->
            val updatedStates = state.platformLoadingStates.toMutableMap().apply {
                this[apiType] = loadingState
            }
            
            // Calculate isIdle based on all enabled platforms
            val allIdle = enabledPlatformsInChat.all { 
                updatedStates[it] is LoadingState.Idle
            }
            
            state.copy(
                platformLoadingStates = updatedStates,
                isIdle = allIdle
            )
        }
    }
}
