package retanar.divergence

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import retanar.divergence.logic.SETTING_WIDGET_UPDATE_MINUTES
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        DivergenceWidget.updateWidgetsWithRandomDivergence(applicationContext)
        Timber.d("Divergence updated with worker")

        return Result.success()
    }

    companion object {
        private const val UPDATE_WORKER_NAME = "widget_update_worker"
        private val workManager get() = DI.workManager

        fun enqueueWork() {
            val interval = DI.settings.getString(SETTING_WIDGET_UPDATE_MINUTES, null)
                ?.toLongOrNull()
                ?: 120
            Timber.d("Worker asked to enqueue work at interval $interval")

            if (interval == 0L) {
                stopWork()
                return
            }

            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                repeatInterval = interval,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).build()

            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = UPDATE_WORKER_NAME,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = workRequest
            )
        }

        fun stopWork() {
            workManager.cancelUniqueWork(UPDATE_WORKER_NAME)
            Timber.d("Worker stopped work")
        }

        fun getStatus() = workManager.getWorkInfosForUniqueWorkFlow(UPDATE_WORKER_NAME)
    }
}
