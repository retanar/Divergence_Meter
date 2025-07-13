package retanar.divergence.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import retanar.divergence.DivergenceWidget

fun getWidgetIds(context: Context): IntArray = AppWidgetManager.getInstance(context)
    .getAppWidgetIds(ComponentName(context, DivergenceWidget::class.java))
