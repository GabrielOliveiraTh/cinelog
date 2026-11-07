package com.gabriel.cinelog.sync.work.di

import com.gabriel.cinelog.sync.work.notifications.Notifier
import com.gabriel.cinelog.sync.work.notifications.SystemTrayNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface SyncModule {
    @Binds
    fun bindsNotifier(notifier: SystemTrayNotifier): Notifier
}
