package retanar.divergence.logic

import retanar.divergence.util.logd
import retanar.divergence.util.loge
import kotlin.math.absoluteValue
import kotlin.random.Random

@Suppress("MemberVisibilityCanBePrivate")
object DivergenceMeter {
    private const val DIVERGENCE_CHANGE_STEP = 100_000
    private const val MAX_COEFFICIENT = DIVERGENCE_CHANGE_STEP / 4

    fun generateRandomDivergence(): Divergence {
        return Divergence(
            Random.nextInt(
                ALL_RANGE.start.intValue,
                ALL_RANGE.endExclusive.intValue
            )
        )
    }

    fun generateBalancedDivergenceWithCooldown(
        currentDiv: Divergence,
        lastTimeChanged: Long,
        cooldownMs: Long,
    ): Divergence {
        val cooldownTime = System.currentTimeMillis() - cooldownMs
        logd { "Cooldown: ${lastTimeChanged - cooldownTime} (positive - time you need to wait)" }

        return if (lastTimeChanged < cooldownTime)
            generateBalancedDivergence(currentDiv, ALL_RANGE)
        else
            generateBalancedDivergence(
                currentDiv,
                Attractor.findFor(currentDiv)?.range ?: ALL_RANGE
            )
    }

    fun generateBalancedDivergence(
        currentDiv: Divergence,
        divRange: DivergenceRange = ALL_RANGE
    ): Divergence {
        if (currentDiv !in divRange) {
            val randomDiv = generateRandomDivergence()
            loge {
                "Error in generateBalancedDivergence(): currentDiv was not in Attractor's range. " +
                    "Generated random divergence: $randomDiv"
            }
            return randomDiv
        }

        // It is ensured with the previous check that getCoefficient will work
        val coefficient = getCoefficient(currentDiv)
        var newDiv: Divergence
        var step: Int

        do {
            step = Random.nextInt(
                -DIVERGENCE_CHANGE_STEP + coefficient,
                DIVERGENCE_CHANGE_STEP + coefficient
            )
            newDiv = Divergence(currentDiv.intValue + step)
        } while (newDiv !in divRange)

        logd {
            """generateBalancedDivergence() call.
              |Previous div: $currentDiv;
              |Step limits: (${-DIVERGENCE_CHANGE_STEP + coefficient} ; ${DIVERGENCE_CHANGE_STEP + coefficient});
              |Step: $step;
              |New div: $newDiv;""".trimMargin()
        }
        return newDiv
    }

    /* Coefficient needed to lower the chance of going to another attractor field
     * How it works:
     *  - equalize the divergence to range [0;1_000_000)
     *  - subtract half of the maximum divergence to put the divergence in range [-500_000;+500_000)
     *  - scale it to fit under MAX_COEFFICIENT
     *  - negate the result, as if pointing to the middle value */
    fun getCoefficient(div: Divergence): Int {
        val attractor = Attractor.findFor(div) ?: return 0

        val equalizedDiv = div.intValue - attractor.range.start.intValue - 500_000
        val scaleConst = 500_000 / MAX_COEFFICIENT
        val coefficient = -(equalizedDiv / scaleConst)

        return coefficient
    }

    /** Returns new attractor name or null if attractor hasn't been changed */
    fun checkAttractorChange(oldDiv: Divergence, newDiv: Divergence): String? {
        val attractor = Attractor.findFor(oldDiv)
        return if (attractor != null && newDiv !in attractor)
            Attractor.findFor(newDiv)?.title
        else null
    }

    // idk if I should somehow simplify this
    fun splitIntegerToDigits(number: Int): IntArray {
        val digits = IntArray(7)
        var integer = number.absoluteValue

        for (i in digits.indices) {
            digits[i] = (integer % 10)
            integer /= 10
        }
        if (number < 0)
            digits[6] = -1

        return digits
    }
}
