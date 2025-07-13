package retanar.divergence.util

import android.content.Context
import androidx.work.WorkManager
import retanar.divergence.settings.PreferenceRepository

object DI {
    lateinit var preferences: PreferenceRepository
        private set
    lateinit var workManager: WorkManager
        private set

    fun initialize(context: Context) {
        preferences = PreferenceRepository(context)
        workManager = WorkManager.getInstance(context)
    }
}
