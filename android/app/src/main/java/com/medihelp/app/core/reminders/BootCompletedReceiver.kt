package com.medihelp.app.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medihelp.app.core.database.dao.MedicationDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Exact alarms scheduled via AlarmManager are cleared on reboot, so every
 * active medication's reminders need to be re-armed once the device is back up.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var medicationDao: MedicationDao

    @Inject
    lateinit var alarmScheduler: MedicationAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                medicationDao.getActiveMedicationsOnce().forEach { medicationWithSchedules ->
                    alarmScheduler.scheduleAllForMedication(
                        medicationWithSchedules.medication.id,
                        medicationWithSchedules.medication.name,
                        medicationWithSchedules.schedules,
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
