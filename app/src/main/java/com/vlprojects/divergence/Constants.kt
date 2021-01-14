package com.vlprojects.divergence

const val SHARED_FILENAME = "divergence_widget"
const val SHARED_DIVERGENCE = "divergence_value"
const val SHARED_NEXT_DIVERGENCE = "divergence_next_value"

const val CHANGE_WORLDLINE_NOTIFICATION_CHANNEL = "change_worldline_channel"
const val NOTIFICATION_ID = 101

const val MILLION = 1_000_000
val OMEGA_RANGE = -MILLION until 0
val ALPHA_RANGE = 0 until MILLION
val BETA_RANGE = MILLION until 2 * MILLION
val ALL_RANGE = OMEGA_RANGE.first..BETA_RANGE.last

val nixieNumberDrawables by lazy {
    arrayOf(
        R.drawable.nixie0,
        R.drawable.nixie1,
        R.drawable.nixie2,
        R.drawable.nixie3,
        R.drawable.nixie4,
        R.drawable.nixie5,
        R.drawable.nixie6,
        R.drawable.nixie7,
        R.drawable.nixie8,
        R.drawable.nixie9,
    )
}
val tubeIds by lazy {
    arrayOf(
        R.id.tube0,
        R.id.tube1,
        R.id.tube2,
        R.id.tube3,
        R.id.tube4,
        R.id.tube5,
        R.id.tube6
    )
}