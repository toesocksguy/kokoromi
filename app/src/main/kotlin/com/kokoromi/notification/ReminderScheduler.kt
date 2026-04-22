package com.kokoromi.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kokoromi.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedule(hour: Int, minute: Int) {
        val now = LocalTime.now()
        val target = LocalTime.of(hour, minute)
        val minutesUntilTarget = Duration.between(now, target).toMinutes().let {
            if (it <= 0) it + TimeUnit.DAYS.toMinutes(1) else it
        }

        val request = PeriodicWorkRequestBuilder<CheckInReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(minutesUntilTarget, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            Constants.REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(Constants.REMINDER_WORK_NAME)
    }
}