package com.vlprojects.divergence

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import kotlin.random.Random

const val SHARED_FILENAME = "divergence_widget"
const val SHARED_DIVERGENCE = "divergence_value"

class DivergenceWidget : android.appwidget.AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(SHARED_FILENAME, 0)
        val newDivergence = generateRandomDivergence(prefs.getInt(SHARED_DIVERGENCE, 0))
        val newDivergenceDigits = splitIntegerToDigits(newDivergence)

        appWidgetIds.forEach {
            updateAppWidget(context, appWidgetManager, it, newDivergenceDigits)
        }

        prefs.edit().putInt(SHARED_DIVERGENCE, newDivergence).apply()
    }

    fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        divergenceDigits: IntArray
    ) {
        val nixie = context.resources.obtainTypedArray(R.array.nixieImage)
        val tube = context.resources.obtainTypedArray(R.array.widgetTube)

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.divergence_widget)
        views.setImageViewResource(R.id.tubeDot, R.drawable.nixie_dot)

        // Main logic
        for (i in 0..6)
            views.setImageViewResource(
                tube.getResourceId(i, 0),
                if (divergenceDigits[i] >= 0)
                    nixie.getResourceId(divergenceDigits[i], 0)
                else R.drawable.nixie_minus
            )

        appWidgetManager.updateAppWidget(appWidgetId, views)
        nixie.recycle()
        tube.recycle()
    }

    fun generateRandomDivergence(currentDivergence: Int): Int {
        var newDivergence = currentDivergence + Random.nextInt(-200000, 200000)

        while (newDivergence < -1000000 || newDivergence > 2000000)
            if (newDivergence < -1000000)
                newDivergence += Random.nextInt(200000)
            else if (newDivergence > 2000000)
                newDivergence -= Random.nextInt(200000)

        return newDivergence
    }

    // Some shit code: Unfixable
    fun splitIntegerToDigits(int: Int): IntArray {
        val digits = IntArray(7)
        var integer = if (int >= 0) int else -int

        for (i in 0..6) {
            digits[i] = (integer % 10)
            integer /= 10
        }
        if (int <= 0)
            digits[6] = -1

        return digits
    }
}