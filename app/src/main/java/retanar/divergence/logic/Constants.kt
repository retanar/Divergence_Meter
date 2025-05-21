package retanar.divergence.logic

import retanar.divergence.R

const val PREFS_FILENAME = "divergence_widget"
const val PREFS_CURRENT_DIVERGENCE = "divergence_value"
const val PREFS_LAST_ATTRACTOR_CHANGE = "last_attractor_change_time"

const val SETTING_ATTRACTOR_NOTIFICATIONS = "attractor_notifications"
const val SETTING_WORLDLINE_NOTIFICATIONS = "known_worldline_notifications"
const val SETTING_ATTRACTOR_COOLDOWN_HOURS = "attractor_change_cooldown"
const val SETTING_WIDGET_UPDATE_MINUTES = "widget_autoupdate_delay"

const val CHANGE_WORLDLINE_NOTIFICATION_CHANNEL = "change_worldline_channel"
const val NOTIFICATION_ID = 101

const val MILLION = 1_000_000
const val UNDEFINED_DIVERGENCE = Int.MIN_VALUE
const val ATTRACTOR_DEFAULT_COOLDOWN_MS = 86_400_000L        // 1 day

val nixieNumberDrawables = arrayOf(
    R.drawable.nixie0,
    R.drawable.nixie1,
    R.drawable.nixie2,
    R.drawable.nixie3,
    R.drawable.nixie4,
    R.drawable.nixie5,
    R.drawable.nixie6,
    R.drawable.nixie7,
    R.drawable.nixie8,
    R.drawable.nixie9
)
val tubeIds = arrayOf(
    R.id.tube0,
    R.id.tube1,
    R.id.tube2,
    R.id.tube3,
    R.id.tube4,
    R.id.tube5,
    R.id.tube6
)
