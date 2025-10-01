package dev.ankitkumar1302.gptmobile.data.dto

import dev.ankitkumar1302.gptmobile.data.model.DynamicTheme
import dev.ankitkumar1302.gptmobile.data.model.ThemeMode

data class ThemeSetting(
    val dynamicTheme: DynamicTheme = DynamicTheme.OFF,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
