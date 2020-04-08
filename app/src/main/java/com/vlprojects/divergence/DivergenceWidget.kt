package com.vlprojects.divergence

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews

class DivergenceWidget : android.appwidget.AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        context.resources.obtainTypedArray(R.array.nixie)
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.divergence_widget)
            views.setImageViewResource(R.id.imageView0, R.drawable.nixie1)
            views.setImageViewResource(R.id.imageView1, R.drawable.nixie_dot)
            views.setImageViewResource(R.id.imageView2, R.drawable.nixie0)
            views.setImageViewResource(R.id.imageView3, R.drawable.nixie4)
            views.setImageViewResource(R.id.imageView4, R.drawable.nixie8)
            views.setImageViewResource(R.id.imageView5, R.drawable.nixie5)
            views.setImageViewResource(R.id.imageView6, R.drawable.nixie9)
            views.setImageViewResource(R.id.imageView7, R.drawable.nixie7)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}