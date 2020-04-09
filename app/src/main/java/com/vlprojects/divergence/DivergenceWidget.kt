package com.vlprojects.divergence

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import kotlin.random.Random

class DivergenceWidget : android.appwidget.AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    companion object {
        @SuppressLint("ResourceType")
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager, appWidgetId: Int
        ) {
            val nixie = context.resources.obtainTypedArray(R.array.nixieImage)
            val tube = context.resources.obtainTypedArray(R.array.widgetTube)

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.divergence_widget)
            views.setImageViewResource(R.id.tubeDot, R.drawable.nixie_dot)
            val divergenceDigits = splitIntegerToDigits(generateRandomDivergence())
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

        fun generateRandomDivergence() =
            Random.nextInt(-1000000, 2000000)

        // Some shit code: Fix it please
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
}