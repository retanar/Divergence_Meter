package retanar.divergence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import retanar.divergence.logic.CHANGE_WORLDLINE_NOTIFICATION_CHANNEL
import retanar.divergence.logic.DivergenceMeter
import retanar.divergence.logic.DivergenceMeter.getDivergenceOrGenerate
import retanar.divergence.logic.DivergenceMeter.saveDivergence
import retanar.divergence.logic.MILLION
import retanar.divergence.logic.NOTIFICATION_ID
import retanar.divergence.logic.PREFS_LAST_ATTRACTOR_CHANGE
import retanar.divergence.logic.SETTING_ATTRACTOR_COOLDOWN_HOURS
import retanar.divergence.logic.SETTING_ATTRACTOR_NOTIFICATIONS
import retanar.divergence.logic.SETTING_WORLDLINE_NOTIFICATIONS
import retanar.divergence.logic.nixieNumberDrawables
import retanar.divergence.logic.tubeIds
import retanar.divergence.logic.worldlines
import timber.log.Timber
import kotlin.time.Duration.Companion.hours

class DivergenceWidget : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        WidgetUpdateWorker.enqueueWork()

        // FIXME may not be available without permission
        createNotificationChannel(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        WidgetUpdateWorker.stopWork()
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        super.onUpdate(context, manager, widgetIds)

        updateWidgetsNoChange(context)
    }

    companion object {
        private val prefs get() = DI.preferences
        private val settings get() = DI.settings

        /** Update widget with currently stored divergence, or with a new divergence.
         *
         * Main usage: widget initialization. */
        fun updateWidgetsNoChange(context: Context) {
            updateWidgetsWithSpecificDivergence(
                context,
                prefs.getDivergenceOrGenerate()
            )
        }

        /** Generate randomized balanced divergence to update widget with.
         *
         * Main usage: automatic update.
         *
         * @see [DivergenceMeter.generateBalancedDivergenceWithCooldown] */
        fun updateWidgetsWithRandomDivergence(context: Context) {
            val currentDiv = prefs.getDivergenceOrGenerate()
            val lastAttractorChange = prefs.getLong(PREFS_LAST_ATTRACTOR_CHANGE, 0)
            val cooldown = (settings.getString(SETTING_ATTRACTOR_COOLDOWN_HOURS, null)
                ?.toLongOrNull()
                ?: 0).hours.inWholeMilliseconds

            val newDiv = DivergenceMeter.generateBalancedDivergenceWithCooldown(
                currentDiv = currentDiv,
                lastTimeChanged = lastAttractorChange,
                cooldownMs = cooldown
            )

            updateWidgetsWithSpecificDivergence(context, newDiv, currentDiv)
        }

        /** Main update method, should always be called. Updates widgets, sends notifications, and
         * saves divergence.
         *
         * Main usage: manual update. */
        fun updateWidgetsWithSpecificDivergence(
            context: Context,
            divergence: Int,
            oldDivergence: Int = prefs.getDivergenceOrGenerate(),
        ) {
            // Would be bad to resend notifications for the same thing or during init
            if (divergence != oldDivergence) {
                onDivergenceChange(
                    context = context,
                    newDivergence = divergence,
                    oldDivergence = oldDivergence
                )
            }

            val divDigits = DivergenceMeter.splitIntegerToDigits(divergence)

            // Firstly, show divergence on the widgets
            AppWidgetManager.getInstance(context).updateAppWidget(
                ComponentName(context, DivergenceWidget::class.java),
                createRemoteViews(context.packageName, divDigits)
            )

            // Secondly, save new divergence to shared prefs
            prefs.saveDivergence(divergence)
        }

        private fun createRemoteViews(
            packageName: String,
            divergenceDigits: IntArray
        ): RemoteViews {
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

            return views
        }

        private fun onDivergenceChange(context: Context, newDivergence: Int, oldDivergence: Int) {
            DivergenceMeter.checkAttractorChange(oldDivergence, newDivergence)
                ?.let { attractorName ->
                    if (settings.getBoolean(SETTING_ATTRACTOR_NOTIFICATIONS, false))
                        sendNotification(
                            context,
                            "Attractor change",
                            "Welcome to $attractorName attractor field"
                        )

                    prefs.edit()
                        .putLong(PREFS_LAST_ATTRACTOR_CHANGE, System.currentTimeMillis())
                        .apply()
                }

            worldlines.find { worldline ->
                worldline.divergence == newDivergence
            }?.let { worldline ->
                if (settings.getBoolean(SETTING_WORLDLINE_NOTIFICATIONS, false))
                    sendNotification(
                        context,
                        "Worldline ${worldline.divergence / MILLION.toFloat()}",
                        worldline.message
                    )
            }
        }

        //<editor-fold desc="Notifications">

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

        //</editor-fold>
    }
}
