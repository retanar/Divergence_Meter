package retanar.divergence.logic

import retanar.divergence.util.MILLION
import kotlin.math.roundToInt

@Suppress("MemberVisibilityCanBePrivate")
@JvmInline
value class Divergence(val intValue: Int) : Comparable<Divergence> {
    val asFloat: Float
        get() = intValue / MILLION.toFloat()

    val asString: String
        get() = "%.6f".format(asFloat)

    override fun compareTo(other: Divergence) = intValue.compareTo(other.intValue)

    companion object {
        fun fromFloat(f: Float): Divergence = Divergence((f * MILLION).roundToInt())

        fun fromString(s: String) = fromFloat(s.toFloat())
    }
}
