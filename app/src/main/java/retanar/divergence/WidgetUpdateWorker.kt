package retanar.divergence

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import retanar.divergence.util.DI
import retanar.divergence.util.logd
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        DivergenceWidget.updateWidgetsWithRandomDivergence(applicationContext)
        logd { "Divergence updated with worker" }

        return Result.success()
    }

    companion object {
        private const val UPDATE_WORKER_NAME = "widget_update_worker"
        private val workManager get() = DI.workManager

        fun enqueueWork() {
            val interval = DI.preferences.getWidgetUpdateInterval()
            logd { "Worker asked to enqueue work at interval $interval" }

            if (interval == Duration.ZERO) {
                stopWork()
                return
            }

            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                repeatInterval = interval.toJavaDuration()
            ).build()

            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = UPDATE_WORKER_NAME,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = workRequest
            )
        }

        fun stopWork() {
            workManager.cancelUniqueWork(UPDATE_WORKER_NAME)
            logd { "Worker stopped work" }
        }

        fun getStatus() = workManager.getWorkInfosForUniqueWorkFlow(UPDATE_WORKER_NAME)
    }
}
