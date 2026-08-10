package com.example.stepcount.worker

import android.content.Context
import androidx.work.*
import com.example.stepcount.core.di.AppContainer
import com.example.stepcount.domain.model.Resource
import java.util.concurrent.TimeUnit

/**
 * Background worker running periodically to upload unsynced step records when the device is online.
 */
class StepSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val container = AppContainer(applicationContext)

        return try {
            when (val result = container.syncPendingStepsUseCase()) {
                is Resource.Success -> {
                    Result.success()
                }
                is Resource.Error -> {
                    // Retry automatically on transient network error
                    Result.retry()
                }
                is Resource.Loading -> {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "StepSyncWorkerPeriodic"

        /**
         * Enqueues a periodic background sync worker running every 15 minutes whenever connected to a network.
         */
        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<StepSyncWorker>(
                15, TimeUnit.MINUTES, // 15-minute standard interval
                5, TimeUnit.MINUTES  // 5-minute flex interval
            )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        /**
         * Enqueues an immediate one-time sync request.
         */
        fun enqueueOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<StepSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
        }
    }
}
