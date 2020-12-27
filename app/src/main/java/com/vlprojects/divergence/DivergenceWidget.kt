package com.vlprojects.divergence

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextInt

private const val DIVERGENCE_CHANGE_STEP = 100_000
private const val MAX_DIVERGENCE_COEFFICIENT = DIVERGENCE_CHANGE_STEP / 4

class DivergenceWidget : android.appwidget.AppWidgetProvider() {

    override fun onEnabled(context: Context?) {
        val prefs = context!!.getSharedPreferences(SHARED_FILENAME, 0)
        val currentDiv = prefs.getInt(SHARED_DIVERGENCE, Int.MIN_VALUE)
        Log.d("DivergenceWidget", "onEnabled() call. Current divergence = $currentDiv")

        if (currentDiv in ALL_RANGE)
            return

        with(prefs.edit()) {
            putInt(
                SHARED_DIVERGENCE,
                Random.nextInt(ALL_RANGE)
            )
            apply()
        }

        super.onEnabled(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(SHARED_FILENAME, 0)

        // Firstly, apply saved divergence to the widgets,
        // so that the divergence can be updated to a specific number
        val currentDiv = prefs.getInt(
            SHARED_DIVERGENCE,
            Random.nextInt(ALL_RANGE)
        )
        val currentDivDigits = splitIntegerToDigits(currentDiv)

        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it, currentDivDigits) }

        // Secondly, save new divergence to shared prefs
        val newDiv = generateRandomDivergence(currentDiv)
        with(prefs.edit()) {
            putInt(SHARED_DIVERGENCE, newDiv)
            apply()
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        divergenceDigits: IntArray
    ) {
        // TODO: this is not good if you have multiple widgets
        val nixie = context.resources.obtainTypedArray(R.array.nixieImage)
        val tube = context.resources.obtainTypedArray(R.array.widgetTube)

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.divergence_widget)
        views.setImageViewResource(R.id.tubeDot, R.drawable.nixie_dot)

        // Main logic
        for (i in 0..6) {
            views.setImageViewResource(
                tube.getResourceId(i, 0),
                if (divergenceDigits[i] >= 0)
                    nixie.getResourceId(divergenceDigits[i], 0)
                else
                    R.drawable.nixie_minus
            )
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)

        nixie.recycle()
        tube.recycle()
    }

    fun generateRandomDivergence(currentDiv: Int): Int {
        /* Coefficient needed to lower the chance of going to new worldline
         * How it works:
         *  - equalize the divergence to range [0;1_000_000)
         *  - subtract half of the maximum divergence to put the divergence in range [-500_000;+500_000)
         *  - divide by a specific number to create the coefficient */
        val coefficient = getCoefficient(currentDiv)

        var newDiv =
            currentDiv + Random.nextInt(-DIVERGENCE_CHANGE_STEP + coefficient, DIVERGENCE_CHANGE_STEP + coefficient)

        Log.d(
            "DivergenceWidget",
            """generateRandomDivergence() call.
              |Previous div: $currentDiv;
              |Step limits: (${-DIVERGENCE_CHANGE_STEP + coefficient} ; ${DIVERGENCE_CHANGE_STEP + coefficient});
              |Step: ${newDiv - currentDiv};
              |New div: $newDiv;""".trimMargin()
        )

        while (newDiv !in ALL_RANGE) {
            if (newDiv < ALL_RANGE.first)
                newDiv += Random.nextInt(DIVERGENCE_CHANGE_STEP)
            else if (newDiv > ALL_RANGE.last)
                newDiv -= Random.nextInt(DIVERGENCE_CHANGE_STEP)
        }

        return newDiv
    }

    private fun getCoefficient(currentDiv: Int): Int {
        return (when (currentDiv) {
            in BETA_RANGE -> -MILLION
            in OMEGA_RANGE -> +MILLION
            else -> 0
        } + currentDiv - 500_000) /
                -(MILLION / 2 / MAX_DIVERGENCE_COEFFICIENT)
    }

    // idk if I should somehow simplify this
    private fun splitIntegerToDigits(number: Int): IntArray {
        val digits = IntArray(7)
        var integer = number.absoluteValue

        for (i in digits.indices) {
            digits[i] = (integer % 10)
            integer /= 10
        }
        if (number < 0)
            digits[6] = -1

        return digits
    }
}