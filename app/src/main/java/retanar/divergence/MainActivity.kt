package retanar.divergence

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import kotlinx.coroutines.launch
import retanar.divergence.databinding.ActivityMainBinding
import retanar.divergence.logic.ALL_RANGE
import retanar.divergence.logic.Divergence
import retanar.divergence.settings.SettingsActivity
import retanar.divergence.util.DI
import retanar.divergence.util.getWidgetIds

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val preferences get() = DI.preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        setupViews()
        setupListeners()
    }

    private fun setupViews() = with(binding) {
        // If worker was never started, then getStatus() flow will start with nothing
        workerStatus.text = getString(R.string.worker_status, "not started")

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Separate launches is a way to collect multiple flows in one repeatOnLifecycle
                launch {
                    WidgetUpdateWorker.getStatus()
                        .collect { workInfos ->
                            if (workInfos.isEmpty()) return@collect

                            val isFinished = workInfos.first().state.isFinished
                            workerStatus.text = getString(
                                R.string.worker_status,
                                if (isFinished) "not working" else "working"
                            )
                        }
                }
                launch {
                    preferences.getDivergenceFlow()
                        .collect(::setDivergenceText)
                }
            }
        }
    }

    private fun setupListeners() = with(binding) {
        changeDivergenceButton.setOnClickListener { changeDivergence() }
        stopWorkerUpdates.setOnClickListener { WidgetUpdateWorker.stopWork() }
        startWorkerUpdates.setOnClickListener {
            if (getWidgetIds(applicationContext).isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "There are no widgets, autoupdate shouldn't run",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            WidgetUpdateWorker.enqueueWork()
        }
        showInstructions.setOnClickListener {
            showInstructions.visibility = View.GONE
            toAddWidget.visibility = View.VISIBLE
            toResizeWidget.visibility = View.VISIBLE
        }
    }

    private fun setDivergenceText(divergence: Divergence) {
        binding.currentDivergence.text =
            getString(R.string.current_divergence, divergence.asString)
    }

    private fun changeDivergence() {
        if (getWidgetIds(applicationContext).isEmpty()) {
            Toast.makeText(
                this,
                "There are no widgets, add one before changing the divergence",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val userDiv = binding.userDivergence.text.trim().toString()
        if (userDiv.isEmpty()) {
            DivergenceWidget.updateWidgetsWithRandomDivergence(applicationContext)
            Toast.makeText(this, "Autoupdate!", Toast.LENGTH_SHORT).show()
            return
        }

        val userDivNumber = Divergence.fromString(userDiv)
        if (userDivNumber !in ALL_RANGE) {
            Toast.makeText(
                this,
                "Wrong value. Should be in (%s;%s)".format(
                    ALL_RANGE.start.asString,
                    ALL_RANGE.endExclusive.asString,
                ),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        DivergenceWidget.updateWidgetsWithSpecificDivergence(applicationContext, userDivNumber)
        Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
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
