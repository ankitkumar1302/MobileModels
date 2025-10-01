package dev.ankitkumar1302.gptmobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ankitkumar1302.gptmobile.data.datastore.SettingDataSource
import dev.ankitkumar1302.gptmobile.data.repository.SettingRepository
import dev.ankitkumar1302.gptmobile.data.repository.SettingRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingRepositoryModule {

    @Provides
    @Singleton
    fun provideSettingRepository(
        settingDataSource: SettingDataSource
    ): SettingRepository = SettingRepositoryImpl(settingDataSource)
}
