package com.reminderpay.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.reminderpay.app.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Reschedules all reminder notifications after: device boot or app update.
 * Uses a WorkManager job to do the DB work off the main thread.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
        if (intent.action !in validActions) return

        val work = OneTimeWorkRequestBuilder<BootRescheduleWorker>()
            .addTag(Constants.BOOT_RESCHEDULE_WORKER_TAG)
            .setInitialDelay(5, TimeUnit.SECONDS) // Brief delay to let system settle
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            Constants.BOOT_RESCHEDULE_WORKER_TAG,
            ExistingWorkPolicy.REPLACE,
            work
        )
    }
}
