package retanar.divergence

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
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
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import retanar.divergence.util.DI
import retanar.divergence.util.NotificationUtils
import retanar.divergence.util.PreferenceRepository.Companion.SETTING_ATTRACTOR_COOLDOWN_HOURS
import retanar.divergence.util.PreferenceRepository.Companion.SETTING_ATTRACTOR_NOTIFICATIONS
import retanar.divergence.util.PreferenceRepository.Companion.SETTING_WIDGET_UPDATE_MINUTES
import retanar.divergence.util.PreferenceRepository.Companion.SETTING_WORLDLINE_NOTIFICATIONS

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
