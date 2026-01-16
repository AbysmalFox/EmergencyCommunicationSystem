package com.example.emergencycommunicationsystem.di

import com.example.emergencycommunicationsystem.data.repository.MessagingRepository
import com.example.emergencycommunicationsystem.data.repository.SettingsRepository

object ServiceLocator {
    @Volatile
    private var messagingRepository: MessagingRepository? = null

    @Volatile
    private var settingsRepository: SettingsRepository? = null

    fun provideMessagingRepository(): MessagingRepository {
        return messagingRepository ?: synchronized(this) {
            messagingRepository ?: MessagingRepository().also { messagingRepository = it }
        }
    }

    fun provideSettingsRepository(): SettingsRepository {
        return settingsRepository ?: synchronized(this) {
            settingsRepository ?: SettingsRepository().also { settingsRepository = it }
        }
    }
}
