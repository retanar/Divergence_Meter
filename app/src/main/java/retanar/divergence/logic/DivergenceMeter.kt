package retanar.divergence.logic

import retanar.divergence.util.logd
import retanar.divergence.util.loge
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextInt

@Suppress("MemberVisibilityCanBePrivate")
object DivergenceMeter {
    private const val DIVERGENCE_CHANGE_STEP = 100_000
    private const val MAX_COEFFICIENT = DIVERGENCE_CHANGE_STEP / 4

    fun generateRandomDivergence() = Divergence(Random.nextInt(ALL_RANGE.range))

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
            generateBalancedDivergence(currentDiv, getAttractor(currentDiv) ?: ALL_RANGE)
    }

    fun generateBalancedDivergence(
        currentDiv: Divergence,
        attractor: Attractor = ALL_RANGE
    ): Divergence {
        if (currentDiv !in attractor) {
            val randomDiv = generateRandomDivergence()
            loge {
                "Error in generateBalancedDivergence(): currentDiv was not in Attractor's range. " +
                    "Generated random divergence: $randomDiv"
            }
            return randomDiv
        }

        // It is ensured with the previous check that getCoefficient will work
        val coefficient = getCoefficient(currentDiv)
        var newDivInt: Int
        var step: Int

        do {
            step = Random.nextInt(
                -DIVERGENCE_CHANGE_STEP + coefficient,
                DIVERGENCE_CHANGE_STEP + coefficient
            )
            newDivInt = currentDiv.intValue + step
        } while (newDivInt !in attractor)

        val newDiv = Divergence(newDivInt)

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
        val attractor = getAttractor(div) ?: return 0

        val equalizedDiv = div.intValue - attractor.range.first - 500_000
        val scaleConst = 500_000 / MAX_COEFFICIENT
        val coefficient = -(equalizedDiv / scaleConst)

        return coefficient
    }

    fun getAttractor(div: Divergence): Attractor? = attractors.find { div in it }

    /** Returns new attractor name or null if attractor hasn't been changed */
    fun checkAttractorChange(oldDiv: Divergence, newDiv: Divergence): String? {
        val attractor = getAttractor(oldDiv)
        return if (attractor != null && newDiv !in attractor)
            getAttractor(newDiv)?.name
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
