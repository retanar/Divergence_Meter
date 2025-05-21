package retanar.divergence

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import retanar.divergence.logic.SETTING_ATTRACTOR_COOLDOWN_HOURS
import retanar.divergence.logic.SETTING_WIDGET_UPDATE_MINUTES

class SettingsFragment : PreferenceFragmentCompat() {
    private val settingPrefs get() = DI.settings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // EditTexts to allow only numbers, length filter is random

        preferenceManager.findPreference<EditTextPreference>(
            SETTING_ATTRACTOR_COOLDOWN_HOURS
        )?.run {
            setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                editText.filters += InputFilter.LengthFilter(6)
            }
        }

        preferenceManager.findPreference<EditTextPreference>(SETTING_WIDGET_UPDATE_MINUTES)?.run {
            setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                editText.filters += InputFilter.LengthFilter(6)
            }
        }
    }

    private val onWidgetUpdateIntervalListener = OnSharedPreferenceChangeListener { _, key ->
        if (key == SETTING_WIDGET_UPDATE_MINUTES)
            WidgetUpdateWorker.enqueueWork()
    }

    override fun onStart() {
        super.onStart()
        settingPrefs.registerOnSharedPreferenceChangeListener(onWidgetUpdateIntervalListener)
    }

    override fun onStop() {
        super.onStop()
        settingPrefs.unregisterOnSharedPreferenceChangeListener(onWidgetUpdateIntervalListener)
    }
}
