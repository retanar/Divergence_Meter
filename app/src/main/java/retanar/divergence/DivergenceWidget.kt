package retanar.divergence

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import retanar.divergence.logic.Divergence
import retanar.divergence.logic.DivergenceMeter
import retanar.divergence.logic.worldlines
import retanar.divergence.util.DI
import retanar.divergence.util.NotificationUtils.sendNotification
import retanar.divergence.util.nixieNumberDrawables
import retanar.divergence.util.tubeIds

class DivergenceWidget : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        WidgetUpdateWorker.enqueueWork()
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
        private val preferences get() = DI.preferences

        /** Update widget with currently stored divergence, or with a new divergence.
         *
         * Main usage: widget initialization. */
        fun updateWidgetsNoChange(context: Context) {
            updateWidgetsWithSpecificDivergence(
                context,
                preferences.getDivergenceOrCreate()
            )
        }

        /** Generate randomized balanced divergence to update widget with.
         *
         * Main usage: automatic update.
         *
         * @see [DivergenceMeter.generateBalancedDivergenceWithCooldown] */
        fun updateWidgetsWithRandomDivergence(context: Context) {
            val currentDiv = preferences.getDivergenceOrCreate()
            val lastAttractorChange = preferences.getLastAttractorChangeTime()
            val cooldown = preferences.getAttractorCooldown().inWholeMilliseconds

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
            divergence: Divergence,
            oldDivergence: Divergence = preferences.getDivergenceOrCreate(),
        ) {
            // Would be bad to resend notifications for the same thing or during init
            if (divergence != oldDivergence) {
                onDivergenceChangeNotification(
                    context = context,
                    newDivergence = divergence,
                    oldDivergence = oldDivergence
                )
            }

            val divDigits = DivergenceMeter.splitIntegerToDigits(divergence.intValue)

            // Firstly, show divergence on the widgets
            AppWidgetManager.getInstance(context).updateAppWidget(
                ComponentName(context, DivergenceWidget::class.java),
                createRemoteViews(context.packageName, divDigits)
            )

            // Secondly, save new divergence to shared prefs
            preferences.setDivergence(divergence)
        }

        private fun createRemoteViews(
            packageName: String,
            divergenceDigits: IntArray,
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

        private fun onDivergenceChangeNotification(
            context: Context,
            newDivergence: Divergence,
            oldDivergence: Divergence,
        ) {
            DivergenceMeter.checkAttractorChange(oldDivergence, newDivergence)
                ?.let { attractorName ->
                    if (preferences.getAttractorNotificationsEnabled())
                        sendNotification(
                            context,
                            "Attractor change",
                            "Welcome to $attractorName attractor field"
                        )

                    preferences.setLastAttractorChangeTime(System.currentTimeMillis())
                }

            worldlines.find { worldline ->
                worldline.divergence == newDivergence
            }?.let { worldline ->
                if (preferences.getWorldlineNotificationsEnabled())
                    sendNotification(
                        context,
                        "Worldline ${worldline.divergence.asString}",
                        worldline.message
                    )
            }
        }
    }
}
