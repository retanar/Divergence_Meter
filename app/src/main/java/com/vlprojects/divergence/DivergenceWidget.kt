package com.vlprojects.divergence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.vlprojects.divergence.DivergenceMeter.getDivergenceValuesOrGenerate
import com.vlprojects.divergence.DivergenceMeter.saveDivergence

class DivergenceWidget : android.appwidget.AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        val prefs = context.getSharedPreferences(SHARED_FILENAME, 0)
        val currentDiv = prefs.getInt(SHARED_CURRENT_DIVERGENCE, Int.MIN_VALUE)
        Log.d("DivergenceWidget", "onEnabled() call. Current divergence = $currentDiv")

        if (currentDiv !in ALL_RANGE)
            DivergenceMeter.saveRandomDivergences(prefs)

        createNotificationChannel(context)
    }

    // TODO: 0.4.0 cooldown for changing worldlines
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val prefs = context.getSharedPreferences(SHARED_FILENAME, 0)
        val (currentDiv, nextDiv) = prefs.getDivergenceValuesOrGenerate()
        val nextDivDigits = DivergenceMeter.splitIntegerToDigits(nextDiv)

        checkNotifications(context, currentDiv, nextDiv)

        // Firstly, apply saved next divergence to the widgets,
        // so that the divergence can be updated to a specific number
        appWidgetIds.forEach {
            updateAppWidget(
                context.packageName, appWidgetManager, it,
                nextDivDigits
            )
        }

        // Secondly, save new divergence to shared prefs
        val newDiv = DivergenceMeter.generateBalancedDivergence(nextDiv)
        prefs.saveDivergence(nextDiv, newDiv)
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

    /** Notifications **/

    private fun checkNotifications(context: Context, oldDiv: Int, newDiv: Int) {
        when (newDiv) {
            in OMEGA_RANGE -> if (oldDiv !in OMEGA_RANGE)
                sendNotification(context, "Welcome to Omega attractor field")
            in ALPHA_RANGE -> if (oldDiv !in ALPHA_RANGE)
                sendNotification(context, "Welcome to Alpha attractor field")
            in BETA_RANGE -> if (oldDiv !in BETA_RANGE)
                sendNotification(context, "Welcome to Beta attractor field")
        }
    }

    private fun sendNotification(context: Context, text: String) {
        Log.d("DivergenceWidget", "sendNotification() call with text = \"$text\"")
        val notifyManager = getNotificationManager(context)
        val builder = NotificationCompat.Builder(context, CHANGE_WORLDLINE_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Worldline change!")
            .setContentText(text)
            .setSound(null)
        notifyManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifyManager = getNotificationManager(context)
            val channel = NotificationChannel(
                CHANGE_WORLDLINE_NOTIFICATION_CHANNEL,
                "Change worldline notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            notifyManager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationManager(context: Context) =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
}