package retanar.divergence.logic

class DivergenceRange(
    override val start: Divergence,
    override val endExclusive: Divergence
) : OpenEndRange<Divergence>

operator fun Divergence.rangeUntil(end: Divergence) = DivergenceRange(this, end)
