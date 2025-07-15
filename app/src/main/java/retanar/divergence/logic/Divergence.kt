package retanar.divergence.logic

import retanar.divergence.util.MILLION
import kotlin.math.roundToInt

@JvmInline
value class Divergence(val intValue: Int) {
    val asFloat: Float
        get() = intValue / MILLION.toFloat()

    val asString: String
        get() = "%.6f".format(asFloat)

    companion object {
        fun fromFloat(f: Float): Divergence = Divergence((f * MILLION).roundToInt())

        fun fromString(s: String) = fromFloat(s.toFloat())
    }
}
