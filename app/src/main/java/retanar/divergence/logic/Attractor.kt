package retanar.divergence.logic

enum class Attractor(val title: String, val range: DivergenceRange) {
    // Inclusive -1_000_000 is needed for getCoefficient()
    OMEGA("Omega", Divergence(-1_000_000)..<Divergence(0)),

    ALPHA("Alpha", Divergence(0)..<Divergence(1_000_000)),
    BETA("Beta", Divergence(1_000_000)..<Divergence(2_000_000)),
    GAMMA("Gamma", Divergence(2_000_000)..<Divergence(3_000_000)),
    DELTA("Delta", Divergence(3_000_000)..<Divergence(4_000_000)),
    EPSILON("Epsilon", Divergence(4_000_000)..<Divergence(5_000_000));

    operator fun contains(value: Divergence) = value in range

    companion object {
        fun findFor(div: Divergence): Attractor? = Attractor.entries.find { div in it }
    }
}

val ALL_RANGE = Attractor.entries.first().range.start..<Attractor.entries.last().range.endExclusive
