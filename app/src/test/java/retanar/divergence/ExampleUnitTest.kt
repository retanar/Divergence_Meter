package retanar.divergence

import retanar.divergence.logic.*
import org.junit.Assert
import org.junit.Test
import java.util.Date

class ExampleUnitTest {

    @Test
    fun zeroCooldownTest() {
        val divergence = 999_999
        val attractor = DivergenceMeter.getAttractor(divergence)!!
        val lastTimeChanged = Date().time - 1000        // One second ago
        val cooldownMs = 0L

        var newDiv: Int
        for (i in 1..100) {
            newDiv = DivergenceMeter.generateBalancedDivergenceWithCooldown(divergence, lastTimeChanged, cooldownMs)
            if (newDiv !in attractor) {
                return
            }
        }

        throw AssertionError("Attractor haven't been changed")
    }

    @Test
    fun oldDivergenceOutOfAllRange() {
        val oldDiv = ALL_RANGE.range.last + MILLION / 2
        val newDiv1 = ALL_RANGE.range.last - MILLION / 2
        val newDiv2 = ALL_RANGE.range.last + MILLION / 2

        Assert.assertNull(
            "Expected ALL_RANGE when the divergence is out of range",
            DivergenceMeter.getAttractor(oldDiv)
        )
        Assert.assertNull(
            "Expected null when old divergence is out of range",
            DivergenceMeter.checkAttractorChange(oldDiv, newDiv1)
        )
        Assert.assertNull(
            "Expected null when new divergence is out of range",
            DivergenceMeter.checkAttractorChange(oldDiv, newDiv2)
        )
    }

    @Test
    fun getCoefficientTest() {
        val coefRange = -25000..25000
        val div1 = 0
        val div2 = attractors.random().range.last
//        val divOutOfRange = ALL_RANGE.range.last + 2_123_456
        Assert.assertTrue(DivergenceMeter.getCoefficient(div1) in coefRange)
        Assert.assertTrue(DivergenceMeter.getCoefficient(div2) in coefRange)
    }
}
