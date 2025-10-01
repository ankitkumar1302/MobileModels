package dev.ankitkumar1302.gptmobile.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ankitkumar1302.gptmobile.data.database.entity.ChatRoom
import dev.ankitkumar1302.gptmobile.data.dto.Platform
import dev.ankitkumar1302.gptmobile.data.repository.ChatRepository
import dev.ankitkumar1302.gptmobile.data.repository.SettingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    data class HomeUiState(
        val chats: List<ChatRoom> = emptyList(),
        val isSelectionMode: Boolean = false,
        val selected: List<Boolean> = emptyList(),
        val platforms: List<Platform> = emptyList(),
        val showSelectModelDialog: Boolean = false,
        val showDeleteWarningDialog: Boolean = false
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Backward compatibility - delegate to uiState
    val chatListState: StateFlow<ChatListState> = uiState.map { state ->
        ChatListState(
            chats = state.chats,
            isSelectionMode = state.isSelectionMode,
            selected = state.selected
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), ChatListState())

    val platformState: StateFlow<List<Platform>> = uiState.map { it.platforms }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), emptyList())

    val showSelectModelDialog: StateFlow<Boolean> = uiState.map { it.showSelectModelDialog }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), false)

    val showDeleteWarningDialog: StateFlow<Boolean> = uiState.map { it.showDeleteWarningDialog }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), false)

    data class ChatListState(
        val chats: List<ChatRoom> = emptyList(),
        val isSelectionMode: Boolean = false,
        val selected: List<Boolean> = emptyList()
    )

    fun updateCheckedState(platform: Platform) {
        val index = _uiState.value.platforms.indexOf(platform)
        if (index >= 0) {
            _uiState.update { state ->
                state.copy(
                    platforms = state.platforms.mapIndexed { i, p ->
                        if (index == i) p.copy(selected = !p.selected) else p
                    }
                )
            }
        }
    }

    fun openDeleteWarningDialog() {
        _uiState.update { it.copy(showSelectModelDialog = false, showDeleteWarningDialog = true) }
    }

    fun closeDeleteWarningDialog() {
        _uiState.update { it.copy(showDeleteWarningDialog = false) }
    }

    fun openSelectModelDialog() {
        _uiState.update { state ->
            state.copy(
                showSelectModelDialog = true,
                isSelectionMode = false,
                selected = List(state.chats.size) { false }
            )
        }
    }

    fun closeSelectModelDialog() {
        _uiState.update { it.copy(showSelectModelDialog = false) }
    }

    fun deleteSelectedChats() {
        viewModelScope.launch {
            val state = _uiState.value
            val selectedChats = state.chats.filterIndexed { index, _ -> state.selected[index] }

            chatRepository.deleteChats(selectedChats)
            val chats = chatRepository.fetchChatList()
            _uiState.update {
                it.copy(
                    chats = chats,
                    selected = List(chats.size) { false },
                    isSelectionMode = false
                )
            }
        }
    }

    fun disableSelectionMode() {
        _uiState.update { state ->
            state.copy(
                selected = List(state.chats.size) { false },
                isSelectionMode = false
            )
        }
    }

    fun enableSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = true) }
    }

    fun fetchChats() {
        viewModelScope.launch {
            val chats = chatRepository.fetchChatList()
            _uiState.update {
                it.copy(
                    chats = chats,
                    selected = List(chats.size) { false },
                    isSelectionMode = false
                )
            }
            Log.d("chats", "${_uiState.value.chats}")
        }
    }

    fun fetchPlatformStatus() {
        viewModelScope.launch {
            val platforms = settingRepository.fetchPlatforms()
            _uiState.update { it.copy(platforms = platforms) }
        }
    }

    fun selectChat(chatRoomIdx: Int) {
        val state = _uiState.value
        if (chatRoomIdx < 0 || chatRoomIdx >= state.chats.size) return

        val newSelected = state.selected.mapIndexed { index, b ->
            if (index == chatRoomIdx) !b else b
        }

        _uiState.update {
            it.copy(
                selected = newSelected,
                isSelectionMode = newSelected.count { it } > 0
            )
        }
    }
}
