package com.kokoromi.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kokoromi.MainActivity
import com.kokoromi.R
import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class CheckInReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val experimentRepository: ExperimentRepository,
    private val dailyLogRepository: DailyLogRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val activeExperiments = experimentRepository.getAllExperiments().first()
            .filter { it.status == ExperimentStatus.ACTIVE }

        if (activeExperiments.isEmpty()) return Result.success()

        val allLogged = activeExperiments.all { experiment ->
            dailyLogRepository.getLogForDate(experiment.id, today) != null
        }

        if (!allLogged) {
            showNotification()
        }

        return Result.success()
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to check in")
            .setContentText("Log your experiments for today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(Constants.NOTIFICATION_ID, notification)
    }
}