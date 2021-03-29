package com.vlprojects.divergence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.vlprojects.divergence.logic.*
import com.vlprojects.divergence.logic.DivergenceMeter.getDivergenceValuesOrGenerate
import com.vlprojects.divergence.logic.DivergenceMeter.saveDivergence
import timber.log.Timber
import java.util.Date

class DivergenceWidget : android.appwidget.AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        createNotificationChannel(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val prefs = context.getSharedPreferences(SHARED_FILENAME, 0)
        val settings = PreferenceManager.getDefaultSharedPreferences(context)

        val div = prefs.getDivergenceValuesOrGenerate()
        val nextDivDigits = DivergenceMeter.splitIntegerToDigits(div.next)

        onDivergenceChange(context, div)

        // Firstly, apply saved next divergence to the widgets,
        // so that the divergence can be updated to a specific number
        appWidgetIds.forEach {
            updateAppWidget(context.packageName, appWidgetManager, it, nextDivDigits)
        }

        // Secondly, save new divergence to shared prefs
        val lastAttractorChange = prefs.getLong(SHARED_LAST_ATTRACTOR_CHANGE, 0)
        val newDiv = DivergenceMeter.generateBalancedDivergenceWithCooldown(
            div.next,
            lastAttractorChange,
            (settings.getString(SETTING_ATTRACTOR_COOLDOWN_HOURS, null)?.toLongOrNull() ?: 24L) * 60 * 60 * 1000
        )
        prefs.saveDivergence(div.next, newDiv)
    }

    private fun updateAppWidget(
        packageName: String,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        divergenceDigits: IntArray
    ) {
        val views = RemoteViews(packageName, R.layout.divergence_widget)
        views.setImageViewResource(R.id.tubeDot, R.drawable.nixie_dot)

        // Setting numbers in place
        for (i in 0..6) {
            views.setImageViewResource(
                tubeIds[i],
                if (divergenceDigits[i] >= 0)
                    nixieNumberDrawables[divergenceDigits[i]]
                else
                    R.drawable.nixie_minus
            )
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun onDivergenceChange(context: Context, divergences: DivergenceValues) {
        val prefs = context.getSharedPreferences(SHARED_FILENAME, 0)
        val settings = PreferenceManager.getDefaultSharedPreferences(context)

        DivergenceMeter.checkAttractorChange(divergences.current, divergences.next)?.let {
            if (settings.getBoolean(SETTING_ATTRACTOR_NOTIFICATIONS, true))
                sendNotification(context, "Attractor change", "Welcome to $it attractor field")

            prefs.edit()
                .putLong(SHARED_LAST_ATTRACTOR_CHANGE, Date().time)
                .apply()
        }

        worldlines.find { worldline ->
            worldline.divergence == divergences.next
        }?.let { worldline ->
            if (settings.getBoolean(SETTING_WORLDLINE_NOTOFICATIONS, true))
                sendNotification(context, "Worldline change", worldline.message)
        }
    }

    /** Notifications **/

    private fun sendNotification(context: Context, title: String, text: String) {
        Timber.d("sendNotification() call with text = \"$text\"")
        val notifyManager = getNotificationManager(context)
        val builder = NotificationCompat.Builder(context, CHANGE_WORLDLINE_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setSound(null)
        notifyManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifyManager = getNotificationManager(context)
            val channel = NotificationChannel(
                CHANGE_WORLDLINE_NOTIFICATION_CHANNEL,
                "Change worldline notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            notifyManager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationManager(context: Context) =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
}