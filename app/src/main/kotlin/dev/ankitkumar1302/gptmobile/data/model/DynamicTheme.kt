package dev.ankitkumar1302.gptmobile.data.model

enum class DynamicTheme {
    ON,
    OFF;

    companion object {
        fun getByValue(value: Int) = entries.firstOrNull { it.ordinal == value }
    }
}
