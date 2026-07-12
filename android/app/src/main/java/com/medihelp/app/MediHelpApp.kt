package com.medihelp.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.medihelp.app.core.notifications.NotificationChannels
import com.medihelp.app.core.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MediHelpApp : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.registerAll(this)
        SyncScheduler.enqueuePeriodicSync(this)
    }
}
