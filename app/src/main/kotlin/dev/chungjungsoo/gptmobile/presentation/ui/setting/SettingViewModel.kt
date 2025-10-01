package dev.chungjungsoo.gptmobile.presentation.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.chungjungsoo.gptmobile.data.dto.Platform
import dev.chungjungsoo.gptmobile.data.model.ApiType
import dev.chungjungsoo.gptmobile.data.repository.SettingRepository
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
class SettingViewModel @Inject constructor(
    private val settingRepository: SettingRepository
) : ViewModel() {

    data class SettingUiState(
        val platforms: List<Platform> = emptyList(),
        val dialogState: DialogState = DialogState()
    )

    data class DialogState(
        val isThemeDialogOpen: Boolean = false,
        val isApiUrlDialogOpen: Boolean = false,
        val isApiTokenDialogOpen: Boolean = false,
        val isApiModelDialogOpen: Boolean = false,
        val isTemperatureDialogOpen: Boolean = false,
        val isTopPDialogOpen: Boolean = false,
        val isSystemPromptDialogOpen: Boolean = false
    )

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    // Backward compatibility - delegate to uiState
    val platformState: StateFlow<List<Platform>> = uiState.map { it.platforms }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), emptyList())

    val dialogState: StateFlow<DialogState> = uiState.map { it.dialogState }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), DialogState())

    init {
        fetchPlatformStatus()
    }

    fun toggleAPI(apiType: ApiType) {
        _uiState.update { state ->
            val index = state.platforms.indexOfFirst { it.name == apiType }
            if (index >= 0) {
                val updatedPlatforms = state.platforms.mapIndexed { i, p ->
                    if (index == i) p.copy(enabled = !p.enabled) else p
                }
                viewModelScope.launch {
                    settingRepository.updatePlatforms(updatedPlatforms)
                }
                state.copy(platforms = updatedPlatforms)
            } else state
        }
    }

    fun savePlatformSettings() {
        viewModelScope.launch {
            settingRepository.updatePlatforms(_uiState.value.platforms)
        }
    }

    fun updateURL(apiType: ApiType, url: String) {
        if (url.isBlank()) return
        _uiState.update { state ->
            state.copy(
                platforms = state.platforms.map { p ->
                    if (p.name == apiType) p.copy(apiUrl = url) else p
                }
            )
        }
    }

    fun updateToken(apiType: ApiType, token: String) {
        if (token.isBlank()) return
        _uiState.update { state ->
            state.copy(
                platforms = state.platforms.map { p ->
                    if (p.name == apiType) p.copy(token = token) else p
                }
            )
        }
    }

    fun updateModel(apiType: ApiType, model: String) {
        _uiState.update { state ->
            state.copy(
                platforms = state.platforms.map { p ->
                    if (p.name == apiType) p.copy(model = model) else p
                }
            )
        }
    }

    fun updateTemperature(apiType: ApiType, temperature: Float) {
        val modifiedTemperature = when (apiType) {
            ApiType.ANTHROPIC -> temperature.coerceIn(0F, 1F)
            else -> temperature.coerceIn(0F, 2F)
        }
        _uiState.update { state ->
            state.copy(
                platforms = state.platforms.map { p ->
                    if (p.name == apiType) p.copy(temperature = modifiedTemperature) else p
                }
            )
        }
    }

    fun updateTopP(apiType: ApiType, topP: Float) {
        val modifiedTopP = topP.coerceIn(0.1F, 1F)
        _uiState.update { state ->
            state.copy(
                platforms = state.platforms.map { p ->
                    if (p.name == apiType) p.copy(topP = modifiedTopP) else p
                }
            )
        }
    }

    fun updateSystemPrompt(apiType: ApiType, prompt: String) {
        if (prompt.isBlank()) return
        _uiState.update { state ->
            state.copy(
                platforms = state.platforms.map { p ->
                    if (p.name == apiType) p.copy(systemPrompt = prompt) else p
                }
            )
        }
    }

    fun openThemeDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isThemeDialogOpen = true)) }
    }

    fun openApiUrlDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isApiUrlDialogOpen = true)) }
    }

    fun openApiTokenDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isApiTokenDialogOpen = true)) }
    }

    fun openApiModelDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isApiModelDialogOpen = true)) }
    }

    fun openTemperatureDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isTemperatureDialogOpen = true)) }
    }

    fun openTopPDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isTopPDialogOpen = true)) }
    }

    fun openSystemPromptDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isSystemPromptDialogOpen = true)) }
    }

    fun closeThemeDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isThemeDialogOpen = false)) }
    }

    fun closeApiUrlDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isApiUrlDialogOpen = false)) }
    }

    fun closeApiTokenDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isApiTokenDialogOpen = false)) }
    }

    fun closeApiModelDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isApiModelDialogOpen = false)) }
    }

    fun closeTemperatureDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isTemperatureDialogOpen = false)) }
    }

    fun closeTopPDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isTopPDialogOpen = false)) }
    }

    fun closeSystemPromptDialog() {
        _uiState.update { it.copy(dialogState = it.dialogState.copy(isSystemPromptDialogOpen = false)) }
    }

    private fun fetchPlatformStatus() {
        viewModelScope.launch {
            val platforms = settingRepository.fetchPlatforms()
            _uiState.update { it.copy(platforms = platforms) }
        }
    }
}
