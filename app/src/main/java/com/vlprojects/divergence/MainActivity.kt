package com.vlprojects.divergence

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewbinding.BuildConfig
import com.vlprojects.divergence.databinding.ActivityMainBinding
import com.vlprojects.divergence.logic.ALL_RANGE
import com.vlprojects.divergence.logic.DivergenceMeter.getDivergenceValuesOrGenerate
import com.vlprojects.divergence.logic.DivergenceMeter.saveDivergence
import com.vlprojects.divergence.logic.MILLION
import com.vlprojects.divergence.logic.SHARED_CURRENT_DIVERGENCE
import com.vlprojects.divergence.logic.SHARED_FILENAME
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        // TODO: Careful, delete first line and change next to false for release version
//        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply()
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        prefs = getSharedPreferences(SHARED_FILENAME, 0)
        setDivergenceText()

        binding.changeDivergenceButton.setOnClickListener { changeDivergence(prefs) }
        prefs.registerOnSharedPreferenceChangeListener(onDivergenceChangeListener)
    }

    private fun setDivergenceText() {
        val div = prefs.getDivergenceValuesOrGenerate()
        binding.currentDivergence.text = "%.6f".format(div.current / MILLION.toFloat())
        binding.nextDivergence.text = "%.6f".format(div.next / MILLION.toFloat())
    }

    private fun changeDivergence(prefs: SharedPreferences) {
        val userDiv = binding.userDivergence.text.toString()
        if (userDiv.isBlank()) {
            updateWidget()
            Toast.makeText(this, "Autoupdate!", Toast.LENGTH_SHORT).show()
            return
        }

        val userDivNumber = (userDiv.toDouble() * MILLION).toInt()
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

        prefs.saveDivergence(nextDiv = userDivNumber)
        updateWidget()

        Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
    }

    private fun updateWidget() {
        val intentUpdate = Intent(this, DivergenceWidget::class.java)
        intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, DivergenceWidget::class.java)
        )
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT)
        pendingIntent.send()
    }

    // Using field so it won't be garbage collected
    private val onDivergenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, tag ->
        if (tag == SHARED_CURRENT_DIVERGENCE)
            setDivergenceText()
    }

    /** Menu **/

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
}
