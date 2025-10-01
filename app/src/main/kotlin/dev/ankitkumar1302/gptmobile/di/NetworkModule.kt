package dev.ankitkumar1302.gptmobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ankitkumar1302.gptmobile.data.network.AnthropicAPI
import dev.ankitkumar1302.gptmobile.data.network.AnthropicAPIImpl
import dev.ankitkumar1302.gptmobile.data.network.NetworkClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkClient(): NetworkClient = NetworkClient(OkHttp)

    @Provides
    @Singleton
    fun provideAnthropicAPI(): AnthropicAPI = AnthropicAPIImpl(provideNetworkClient())
}
