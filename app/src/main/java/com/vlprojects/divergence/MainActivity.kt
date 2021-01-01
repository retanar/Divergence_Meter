package com.vlprojects.divergence

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

// TODO: replace kotlin synthetic with view binding
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: improve design in app
        val prefs = getSharedPreferences(SHARED_FILENAME, 0)
        var currentDiv = prefs.getInt(SHARED_DIVERGENCE, Int.MIN_VALUE)
        var nextDiv = prefs.getInt(SHARED_NEXT_DIVERGENCE, Int.MIN_VALUE)

        if (currentDiv !in ALL_RANGE) {
            DivergenceWidget.setRandomDivergence(prefs)
            currentDiv = prefs.getInt(SHARED_DIVERGENCE, Int.MIN_VALUE)
            nextDiv = prefs.getInt(SHARED_NEXT_DIVERGENCE, Int.MIN_VALUE)
        }
        currentDivergenceText.text = (currentDiv / MILLION.toFloat()).toString()
        nextDivergenceText.text = (nextDiv / MILLION.toFloat()).toString()

        changeDivergenceButton.setOnClickListener { changeDivergence(prefs) }
    }

    private fun changeDivergence(prefs: SharedPreferences) {
        val userDiv = userDivergence.text.toString()
        val userDivNumber = (userDiv.toDouble() * MILLION).roundToInt()

        if (userDiv.isBlank())
            return
        if (userDivNumber !in ALL_RANGE) {
            Toast.makeText(this, "Wrong value. Should be in (-1.000000;2.000000)", Toast.LENGTH_LONG).show()
            return
        }

        with(prefs.edit()) {
            putInt(SHARED_NEXT_DIVERGENCE, userDivNumber)
            apply()
        }

        updateWidget()

        Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
        recreate()
    }

    private fun updateWidget() {
        val intentUpdate = Intent(this, DivergenceWidget::class.java)
        intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, DivergenceWidget::class.java)
        )
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT
        )
        pendingIntent.send()
    }
}
