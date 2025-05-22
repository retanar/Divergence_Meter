package retanar.divergence.util

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import retanar.divergence.logic.DivergenceMeter
import retanar.divergence.logic.UNDEFINED_DIVERGENCE
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/** Combination of two preference sources, and a provider for default values. */
class PreferenceRepository(context: Context) {
    private val normalPreferences =
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
    private val settingPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /** Get stored divergence, or create random and save it. */
    fun getDivergenceOrCreate(): Int {
        var currentDiv = normalPreferences.getInt(PREFS_CURRENT_DIVERGENCE, UNDEFINED_DIVERGENCE)

        if (currentDiv == UNDEFINED_DIVERGENCE) {
            currentDiv = DivergenceMeter.generateRandomDivergence()
            setDivergence(currentDiv)
        }

        return currentDiv
    }

    fun getDivergenceFlow(): Flow<Int> = callbackFlow {
        send(getDivergenceOrCreate())

        val listener = OnSharedPreferenceChangeListener { _, key ->
            if (key == PREFS_CURRENT_DIVERGENCE)
                trySend(getDivergenceOrCreate())
        }
        normalPreferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { normalPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun setDivergence(divergence: Int) = normalPreferences.edit {
        putInt(PREFS_CURRENT_DIVERGENCE, divergence)
    }

    fun getLastAttractorChangeTime(): Long = normalPreferences.getLong(
        PREFS_LAST_ATTRACTOR_CHANGE_TIME,
        DEFAULT_ATTRACTOR_CHANGE_TIME_MS
    )

    fun setLastAttractorChangeTime(timeMs: Long) = normalPreferences.edit {
        putLong(PREFS_LAST_ATTRACTOR_CHANGE_TIME, timeMs)
    }

    fun getAttractorNotificationsEnabled(): Boolean = settingPreferences.getBoolean(
        SETTING_ATTRACTOR_NOTIFICATIONS,
        DEFAULT_ATTRACTOR_NOTIFICATIONS_ENABLED
    )

    fun getWorldlineNotificationsEnabled(): Boolean = settingPreferences.getBoolean(
        SETTING_WORLDLINE_NOTIFICATIONS,
        DEFAULT_WORLDLINE_NOTIFICATIONS_ENABLED
    )

    fun getAttractorCooldown(): Duration =
        (settingPreferences.getString(SETTING_ATTRACTOR_COOLDOWN_HOURS, null)
            ?.toLongOrNull()
            ?: DEFAULT_ATTRACTOR_COOLDOWN_HOURS)
            .hours

    fun getWidgetUpdateInterval(): Duration =
        (settingPreferences.getString(SETTING_WIDGET_UPDATE_MINUTES, null)
            ?.toLongOrNull()
            ?: DEFAULT_WIDGET_UPDATE_INTERVAL_MINUTES)
            .minutes

    fun getWidgetUpdateIntervalFlow(): Flow<Duration> = callbackFlow {
        send(getWidgetUpdateInterval())

        val listener = OnSharedPreferenceChangeListener { _, key ->
            if (key == SETTING_WIDGET_UPDATE_MINUTES)
                trySend(getWidgetUpdateInterval())
        }

        settingPreferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose { settingPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    companion object {
        private const val PREFS_FILENAME = "divergence_widget"
        private const val PREFS_CURRENT_DIVERGENCE = "divergence_value"
        private const val PREFS_LAST_ATTRACTOR_CHANGE_TIME = "last_attractor_change_time"

        // Settings' names and default values should be synchronized with preferences.xml

        const val SETTING_ATTRACTOR_NOTIFICATIONS = "attractor_notifications"
        const val SETTING_WORLDLINE_NOTIFICATIONS = "known_worldline_notifications"
        const val SETTING_ATTRACTOR_COOLDOWN_HOURS = "attractor_change_cooldown"
        const val SETTING_WIDGET_UPDATE_MINUTES = "widget_autoupdate_delay"

        private const val DEFAULT_ATTRACTOR_CHANGE_TIME_MS = 0L
        private const val DEFAULT_ATTRACTOR_NOTIFICATIONS_ENABLED = false
        private const val DEFAULT_WORLDLINE_NOTIFICATIONS_ENABLED = false
        private const val DEFAULT_ATTRACTOR_COOLDOWN_HOURS = 24L
        private const val DEFAULT_WIDGET_UPDATE_INTERVAL_MINUTES = 120L
    }
}
