package retanar.divergence

import org.junit.Assert
import org.junit.Test
import retanar.divergence.logic.ALL_RANGE
import retanar.divergence.logic.ALPHA_ATTRACTOR
import retanar.divergence.logic.DivergenceMeter
import retanar.divergence.logic.attractors
import retanar.divergence.util.MILLION
import retanar.divergence.util.intDivergence
import retanar.divergence.util.stringDivergence
import java.util.Date

class ExampleUnitTest {

    // android Log is not working
    @Test
    fun zeroCooldownTest() {
        val divergence = 999_999
        val attractor = DivergenceMeter.getAttractor(divergence)!!
        val lastTimeChanged = Date().time - 1000        // One second ago
        val cooldownMs = 0L

        var newDiv: Int
        for (i in 1..100) {
            newDiv = DivergenceMeter.generateBalancedDivergenceWithCooldown(
                divergence,
                lastTimeChanged,
                cooldownMs
            )
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
        Assert.assertTrue(DivergenceMeter.getCoefficient(div1) in coefRange)

        // 0 should give max coefficient = 25000
        // 999999 should give almost lowest coefficient = -24999.95, rounded down = -24999
        // exact middle should give no coefficient
        attractors.forEach {
            val firstDiv = it.range.first
            val lastDiv = it.range.last
            val middleDiv = firstDiv + 500_000
            Assert.assertEquals(25_000, DivergenceMeter.getCoefficient(firstDiv))
            Assert.assertEquals(-24_999, DivergenceMeter.getCoefficient(lastDiv))
            Assert.assertEquals(0, DivergenceMeter.getCoefficient(middleDiv))
        }
    }

    @Test
    fun divergenceTransformations() {
        val range = ALPHA_ATTRACTOR

        for (i in range.range) {
            val manual = "0.%06d".format(i)
            val actual = i.stringDivergence()
            Assert.assertEquals(manual, actual)
            Assert.assertEquals(i, actual.intDivergence())
        }
    }
}
