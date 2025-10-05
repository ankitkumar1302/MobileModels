package dev.ankitkumar1302.gptmobile.data.database.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import dev.ankitkumar1302.gptmobile.data.model.ApiType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "chats")
data class ChatRoom(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "chat_id")
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "enabled_platform")
    val enabledPlatform: List<ApiType>,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000
) : Parcelable

class APITypeConverter {
    @TypeConverter
    fun fromString(value: String?): List<ApiType> {
        if (value.isNullOrBlank()) return emptyList()

        return value.split(',')
            .mapNotNull { s ->
                try {
                    ApiType.valueOf(s.trim())
                } catch (e: IllegalArgumentException) {
                    // Log error and skip invalid enum values
                    android.util.Log.w("APITypeConverter", "Invalid ApiType value: $s", e)
                    null
                }
            }
    }

    @TypeConverter
    fun fromList(value: List<ApiType>?): String = value?.joinToString(",") { v -> v.name } ?: ""
}
