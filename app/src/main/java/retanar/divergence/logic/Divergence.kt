package retanar.divergence.logic

import kotlin.math.roundToInt

@Suppress("MemberVisibilityCanBePrivate")
@JvmInline
value class Divergence(val intValue: Int) : Comparable<Divergence> {
    val asFloat: Float
        get() = intValue / CONVERSION

    val asString: String
        get() = "%.6f".format(asFloat)

    override fun compareTo(other: Divergence) = intValue.compareTo(other.intValue)

    companion object {
        const val UNDEFINED = Int.MIN_VALUE

        /** Used to convert between int and float representations */
        private const val CONVERSION = 1_000_000f

        fun fromFloat(f: Float): Divergence = Divergence((f * CONVERSION).roundToInt())

        fun fromString(s: String) = fromFloat(s.toFloat())
    }
}
