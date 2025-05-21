package retanar.divergence

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import retanar.divergence.logic.PREFS_FILENAME

object DI {
    lateinit var preferences: SharedPreferences
        private set
    lateinit var settings: SharedPreferences
        private set
    lateinit var workManager: WorkManager
        private set

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        workManager = WorkManager.getInstance(context)
    }
}
