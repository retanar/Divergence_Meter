package retanar.divergence

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch
import retanar.divergence.databinding.ActivityMainBinding
import retanar.divergence.logic.ALL_RANGE
import retanar.divergence.logic.DivergenceMeter.getDivergenceOrGenerate
import retanar.divergence.logic.MILLION
import retanar.divergence.logic.PREFS_CURRENT_DIVERGENCE
import retanar.divergence.logic.UNDEFINED_DIVERGENCE
import timber.log.Timber
import kotlin.math.round

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val prefs get() = DI.preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        setupViews()
        setupListeners()
    }

    private fun setupViews() = with(binding) {
        setDivergenceText()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                WidgetUpdateWorker.getStatus()
                    .collect { workInfos ->
                        if (workInfos.isEmpty()) return@collect
                        val info = workInfos.first().state.name.lowercase()
                        workerStatus.text = getString(R.string.worker_status, info)
                    }
            }
        }
    }

    private fun setupListeners() = with(binding) {
        changeDivergenceButton.setOnClickListener { changeDivergence() }
        stopWorkerUpdates.setOnClickListener { WidgetUpdateWorker.stopWork() }
        startWorkerUpdates.setOnClickListener { WidgetUpdateWorker.enqueueWork() }
    }

    private fun setDivergenceText() {
        val div = prefs.getDivergenceOrGenerate()
        binding.currentDivergence.text =
            getString(R.string.current_divergence, div / MILLION.toFloat())
    }

    private fun changeDivergence() {
        val userDiv = binding.userDivergence.text.toString()
        if (userDiv.isBlank()) {
            if (updateWidgets())
                Toast.makeText(this, "Autoupdate!", Toast.LENGTH_SHORT).show()
            return
        }

        val userDivNumber = round(userDiv.toDouble() * MILLION).toInt()
        if (userDivNumber !in ALL_RANGE) {
            Toast.makeText(
                this,
                "Wrong value. Should be in (%.6f;%.6f)".format(
                    ALL_RANGE.range.first / MILLION.toFloat(),
                    (ALL_RANGE.range.last + 1) / MILLION.toFloat(),
                ),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (updateWidgets(userDivNumber))
            Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
    }

    /** @return `false` if there are no widgets, `true` otherwise */
    private fun updateWidgets(divergence: Int = UNDEFINED_DIVERGENCE): Boolean {
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, DivergenceWidget::class.java)
        )
        if (ids.isEmpty()) {
            Toast.makeText(
                this,
                "There are no widgets, please add one before changing the divergence",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        if (divergence == UNDEFINED_DIVERGENCE)
            DivergenceWidget.updateWidgetsWithRandomDivergence(applicationContext)
        else
            DivergenceWidget.updateWidgetsWithSpecificDivergence(applicationContext, divergence)

        return true
    }

    // Using field so it won't be garbage collected
    private val onDivergenceChangeListener = OnSharedPreferenceChangeListener { _, key ->
        if (key == PREFS_CURRENT_DIVERGENCE)
            setDivergenceText()
    }

    override fun onStart() {
        super.onStart()
        prefs.registerOnSharedPreferenceChangeListener(onDivergenceChangeListener)
    }

    override fun onStop() {
        super.onStop()
        prefs.unregisterOnSharedPreferenceChangeListener(onDivergenceChangeListener)
    }

    //<editor-fold desc="Menu">

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings_menu -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //</editor-fold>
}
