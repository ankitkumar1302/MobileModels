package dev.ankitkumar1302.gptmobile.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ankitkumar1302.gptmobile.data.database.dao.ChatRoomDao
import dev.ankitkumar1302.gptmobile.data.database.dao.MessageDao
import dev.ankitkumar1302.gptmobile.data.network.AnthropicAPI
import dev.ankitkumar1302.gptmobile.data.repository.ChatRepository
import dev.ankitkumar1302.gptmobile.data.repository.ChatRepositoryImpl
import dev.ankitkumar1302.gptmobile.data.repository.SettingRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatRepositoryModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        @ApplicationContext appContext: Context,
        chatRoomDao: ChatRoomDao,
        messageDao: MessageDao,
        settingRepository: SettingRepository,
        anthropicAPI: AnthropicAPI
    ): ChatRepository = ChatRepositoryImpl(appContext, chatRoomDao, messageDao, settingRepository, anthropicAPI)
}
