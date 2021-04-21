package com.vlprojects.divergence.logic

import com.vlprojects.divergence.R

const val SHARED_FILENAME = "divergence_widget"
const val SHARED_CURRENT_DIVERGENCE = "divergence_value"
const val SHARED_NEXT_DIVERGENCE = "divergence_next_value"
const val SHARED_LAST_ATTRACTOR_CHANGE = "last_attractor_change_time"

const val SETTING_ATTRACTOR_NOTIFICATIONS = "attractor_notifications"
const val SETTING_WORLDLINE_NOTIFICATIONS = "known_worldline_notifications"
const val SETTING_ATTRACTOR_COOLDOWN_HOURS = "attractor_change_cooldown"

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