package dev.ankitkumar1302.gptmobile.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.ankitkumar1302.gptmobile.data.database.dao.ChatRoomDao
import dev.ankitkumar1302.gptmobile.data.database.dao.MessageDao
import dev.ankitkumar1302.gptmobile.data.database.entity.APITypeConverter
import dev.ankitkumar1302.gptmobile.data.database.entity.ChatRoom
import dev.ankitkumar1302.gptmobile.data.database.entity.Message

@Database(entities = [ChatRoom::class, Message::class], version = 1)
@TypeConverters(APITypeConverter::class)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun messageDao(): MessageDao
}
