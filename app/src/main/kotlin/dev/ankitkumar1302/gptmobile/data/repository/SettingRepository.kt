package dev.ankitkumar1302.gptmobile.data.repository

import dev.ankitkumar1302.gptmobile.data.dto.Platform
import dev.ankitkumar1302.gptmobile.data.dto.ThemeSetting

interface SettingRepository {
    suspend fun fetchPlatforms(): List<Platform>
    suspend fun fetchThemes(): ThemeSetting
    suspend fun updatePlatforms(platforms: List<Platform>)
    suspend fun updateThemes(themeSetting: ThemeSetting)
}
