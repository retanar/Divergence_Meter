package retanar.divergence.util

import kotlin.math.roundToInt

fun Int.floatDivergence(): Float = this / MILLION.toFloat()

fun Int.stringDivergence(): String = "%.6f".format(this.floatDivergence())

fun Float.intDivergence(): Int = (this * MILLION).roundToInt()

fun String.intDivergence(): Int = this.toFloat().intDivergence()
