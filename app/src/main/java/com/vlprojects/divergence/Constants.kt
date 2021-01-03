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
