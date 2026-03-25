package cc.webbiii.app.openplural.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import cc.webbiii.app.openplural.helper.LocalStorage
import cc.webbiii.app.openplural.helper.local.syncDirtyResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebSyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            LocalStorage(applicationContext).syncDirtyResources()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.failure()
        }
        return@withContext Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val channel = NotificationChannelCompat.Builder(
            "web_sync_service_notification",
            NotificationManager.IMPORTANCE_LOW
        )
            .setSound(null, null)
            .setVibrationEnabled(false)
            .setLightsEnabled(false)
            .build()

        val notification = NotificationCompat.Builder(
            applicationContext,
            channel.id
        ).build()

        return ForegroundInfo(
            100,
            notification
        )
    }
}