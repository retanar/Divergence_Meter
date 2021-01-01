package com.vlprojects.divergence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextInt

private const val DIVERGENCE_CHANGE_STEP = 100_000
private const val MAX_DIVERGENCE_COEFFICIENT = DIVERGENCE_CHANGE_STEP / 4

class DivergenceWidget : android.appwidget.AppWidgetProvider() {

    companion object {
        fun setRandomDivergence(preferences: SharedPreferences) {
            val randomDivergence = Random.nextInt(ALL_RANGE)
            Log.d("DivergenceWidget", "setRandomDivergence() call. Random divergence = $randomDivergence")

            with(preferences.edit()) {
                putInt(SHARED_DIVERGENCE, randomDivergence)
                putInt(SHARED_NEXT_DIVERGENCE, randomDivergence)
                apply()
            }
        }
    }

    // TODO: maybe delete this and use it only whenever I need it
    private lateinit var notifyManager: NotificationManager

    override fun onEnabled(context: Context) {
        val prefs = context.getSharedPreferences(SHARED_FILENAME, 0)
        val currentDiv = prefs.getInt(SHARED_DIVERGENCE, Int.MIN_VALUE)
        Log.d("DivergenceWidget", "onEnabled() call. Current divergence = $currentDiv")

        if (currentDiv !in ALL_RANGE)
            setRandomDivergence(prefs)

        setNotificationManager(context)
        createNotificationChannel()

        super.onEnabled(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(SHARED_FILENAME, 0)

        // Firstly, apply saved next divergence to the widgets,
        // so that the divergence can be updated to a specific number
        val currentDiv = prefs.getInt(SHARED_NEXT_DIVERGENCE, Int.MIN_VALUE)
        val previousDiv = prefs.getInt(SHARED_DIVERGENCE, Int.MIN_VALUE)
        val nextDivDigits = splitIntegerToDigits(currentDiv)

        if (currentDiv == Int.MIN_VALUE)
            throw RuntimeException(
                "Something went wrong. Please remove the widget and add it again." +
                        "If problem remains, contact the developer."
            )

        checkNotifications(context, previousDiv, currentDiv)

        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it, nextDivDigits) }

        // Secondly, save new divergence to shared prefs
        val newDiv = generateBalancedRandomDivergence(currentDiv)
        with(prefs.edit()) {
            putInt(SHARED_DIVERGENCE, currentDiv)
            putInt(SHARED_NEXT_DIVERGENCE, newDiv)
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

        val views = RemoteViews(context.packageName, R.layout.divergence_widget)
        views.setImageViewResource(R.id.tubeDot, R.drawable.nixie_dot)

        // Setting numbers in place
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

    private fun generateBalancedRandomDivergence(currentDiv: Int): Int {
        /* Coefficient needed to lower the chance of going to new worldline
         * How it works:
         *  - equalize the divergence to range [0;1_000_000)
         *  - subtract half of the maximum divergence to put the divergence in range [-500_000;+500_000)
         *  - divide by a specific number to create the coefficient */
        val coefficient = getCoefficient(currentDiv)

        var newDiv = currentDiv +
                Random.nextInt(
                    -DIVERGENCE_CHANGE_STEP + coefficient,
                    DIVERGENCE_CHANGE_STEP + coefficient
                )

        Log.d(
            "DivergenceWidget",
            """generateBalancedRandomDivergence() call.
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

    /** Notifications **/

    private fun checkNotifications(context: Context, oldDiv: Int, newDiv: Int) {
        when (newDiv) {
            in OMEGA_RANGE -> if (oldDiv !in OMEGA_RANGE)
                sendNotification(context, "Welcome to Omega worldline")
            in ALPHA_RANGE -> if (oldDiv !in ALPHA_RANGE)
                sendNotification(context, "Welcome to Alpha worldline")
            in BETA_RANGE -> if (oldDiv !in BETA_RANGE)
                sendNotification(context, "Welcome to Beta worldline")
        }
    }

    // TODO: icon looks bad
    private fun sendNotification(context: Context, text: String) {
        Log.d("DivergenceWidget", "sendNotification() call with text = \"$text\"")
        setNotificationManager(context)
        val builder = NotificationCompat.Builder(context, CHANGE_WORLDLINE_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Worldline change!")
            .setContentText(text)
            .setSound(null)
        notifyManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun setNotificationManager(context: Context) {
        if (!::notifyManager.isInitialized)
            notifyManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANGE_WORLDLINE_NOTIFICATION_CHANNEL,
                "Change worldline notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setSound(null, null)
            notifyManager.createNotificationChannel(channel)
        }
    }

}