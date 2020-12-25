package com.vlprojects.divergence

const val SHARED_FILENAME = "divergence_widget"
const val SHARED_DIVERGENCE = "divergence_value"

const val MILLION = 1_000_000
val OMEGA_RANGE = -MILLION until 0
val ALPHA_RANGE = 0 until MILLION
val BETA_RANGE = MILLION until 2 * MILLION
val ALL_RANGE = OMEGA_RANGE.first..BETA_RANGE.last

//const val ALPHA_WORLDLINE = 0
//const val BETA_WORLDLINE = MILLION
//const val OMEGA_WORLDLINE = -MILLION
