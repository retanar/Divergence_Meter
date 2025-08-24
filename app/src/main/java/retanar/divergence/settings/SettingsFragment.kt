package retanar.divergence.settings

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import retanar.divergence.BuildConfig
import retanar.divergence.R
import retanar.divergence.WidgetUpdateWorker
import retanar.divergence.settings.PreferenceRepository.Companion.SETTING_ATTRACTOR_COOLDOWN_HOURS
import retanar.divergence.settings.PreferenceRepository.Companion.SETTING_ATTRACTOR_NOTIFICATIONS
import retanar.divergence.settings.PreferenceRepository.Companion.SETTING_CHECK_FOR_UPDATES_MANUAL
import retanar.divergence.settings.PreferenceRepository.Companion.SETTING_WIDGET_UPDATE_MINUTES
import retanar.divergence.settings.PreferenceRepository.Companion.SETTING_WORLDLINE_NOTIFICATIONS
import retanar.divergence.util.DI
import retanar.divergence.util.NotificationUtils

class SettingsFragment : PreferenceFragmentCompat() {
    private val preferences get() = DI.preferences

    private val notificationPermissionLauncher = registerForActivityResult(
        RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            findPreference<SwitchPreference>(SETTING_ATTRACTOR_NOTIFICATIONS)?.isChecked = false
            findPreference<SwitchPreference>(SETTING_WORLDLINE_NOTIFICATIONS)?.isChecked = false
            Toast.makeText(context, "Notification permission not allowed", Toast.LENGTH_SHORT)
                .show()
        } else {
            NotificationUtils.createNotificationChannel(requireContext())
        }
    }

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

        findPreference<SwitchPreference>(SETTING_ATTRACTOR_NOTIFICATIONS)?.run {
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue == true) askNotificationPermission()
                true
            }
        }

        findPreference<SwitchPreference>(SETTING_WORLDLINE_NOTIFICATIONS)?.run {
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue == true) askNotificationPermission()
                true
            }
        }

        findPreference<Preference>(SETTING_CHECK_FOR_UPDATES_MANUAL)?.run {
            summary = "Current version: ${BuildConfig.VERSION_NAME}"
            intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://github.com/retanar/Divergence_Meter/releases")
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // drop one cause it's just getting an already stored value
                preferences.getWidgetUpdateIntervalFlow()
                    .drop(1)
                    .collect { _ ->
                        WidgetUpdateWorker.enqueueWork()
                    }
            }
        }
    }
}
