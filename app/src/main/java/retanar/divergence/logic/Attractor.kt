package retanar.divergence.logic

import retanar.divergence.util.MILLION

enum class Attractor(val title: String, val range: DivergenceRange) {
    // Inclusive -1000000 is needed for getCoefficient()
    OMEGA("Omega", Divergence(-MILLION)..<Divergence(0)),

    ALPHA("Alpha", Divergence(0)..<Divergence(MILLION)),
    BETA("Beta", Divergence(MILLION)..<Divergence(2 * MILLION)),
    GAMMA("Gamma", Divergence(2 * MILLION)..<Divergence(3 * MILLION)),
    DELTA("Delta", Divergence(3 * MILLION)..<Divergence(4 * MILLION)),
    EPSILON("Epsilon", Divergence(4 * MILLION)..<Divergence(5 * MILLION));

    operator fun contains(value: Divergence) = value in range

    companion object {
        fun findFor(div: Divergence): Attractor? = Attractor.entries.find { div in it }
    }
}

val ALL_RANGE = Attractor.entries.first().range.start..<Attractor.entries.last().range.endExclusive
