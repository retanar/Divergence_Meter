package com.vlprojects.divergence.logic

import android.content.SharedPreferences
import timber.log.Timber
import java.util.Date
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextInt

@Suppress("MemberVisibilityCanBePrivate")
object DivergenceMeter {
    private const val DIVERGENCE_CHANGE_STEP = 100_000
    private const val MAX_COEFFICIENT = DIVERGENCE_CHANGE_STEP / 4

    fun generateRandomDivergence() = Random.nextInt(ALL_RANGE.range)

    fun generateBalancedDivergenceWithCooldown(
        currentDiv: Int,
        lastTimeChanged: Long,
        cooldownMs: Long = ATTRACTOR_DEFAULT_COOLDOWN_MS
    ): Int {
        val cooldownTime = Date().time - cooldownMs
        Timber.d("Cooldown: ${lastTimeChanged - cooldownTime} (positive - time you need to wait)")
        return if (lastTimeChanged < cooldownTime)
            generateBalancedDivergence(currentDiv, ALL_RANGE)
        else
            generateBalancedDivergence(currentDiv, getAttractor(currentDiv) ?: ALL_RANGE)
    }

    fun generateBalancedDivergence(currentDiv: Int, attractor: Attractor = ALL_RANGE): Int {
        if (currentDiv !in attractor) {
            val randomDiv = generateRandomDivergence()
            Timber.e("Error in generateBalancedDivergence(): currentDiv was not in Attractor's range. Generated random divergence: $randomDiv")
            return randomDiv
        }

        // It is ensured with the previous check that getCoefficient will work
        val coefficient = getCoefficient(currentDiv)
        var newDiv: Int

        do {
            newDiv = currentDiv +
                    Random.nextInt(
                        -DIVERGENCE_CHANGE_STEP + coefficient,
                        DIVERGENCE_CHANGE_STEP + coefficient
                    )
        } while (newDiv !in attractor)

        Timber.d(
            """generateBalancedDivergence() call.
              |Previous div: $currentDiv;
              |Step limits: (${-DIVERGENCE_CHANGE_STEP + coefficient} ; ${DIVERGENCE_CHANGE_STEP + coefficient});
              |Step: ${newDiv - currentDiv};
              |New div: $newDiv;""".trimMargin()
        )

        return newDiv
    }

    /* Coefficient needed to lower the chance of going to another attractor field
     * How it works:
     *  - equalize the divergence to range [0;1_000_000)
     *  - subtract half of the maximum divergence to put the divergence in range [-500_000;+500_000)
     *  - divide by a specific number to create the coefficient */
    fun getCoefficient(currentDiv: Int) =
        (-getAttractor(currentDiv)!!.range.first + currentDiv - 500_000) /
                -(MILLION / 2 / MAX_COEFFICIENT)

    fun getAttractor(div: Int): Attractor? = attractors.find { div in it }

    fun SharedPreferences.getDivergenceValuesOrGenerate(): DivergenceValues {
        var currentDiv = getInt(SHARED_CURRENT_DIVERGENCE, UNDEFINED_DIVERGENCE)
        var newDiv = getInt(SHARED_NEXT_DIVERGENCE, UNDEFINED_DIVERGENCE)

        if (currentDiv == UNDEFINED_DIVERGENCE) {
            if (newDiv == UNDEFINED_DIVERGENCE) {
                newDiv = generateRandomDivergence()
            }
            currentDiv = newDiv
            newDiv = generateBalancedDivergence(currentDiv)
            saveDivergence(currentDiv, newDiv)
        } else if (newDiv == UNDEFINED_DIVERGENCE) {
            newDiv = generateBalancedDivergence(currentDiv)
            saveDivergence(nextDiv = newDiv)
        }

        return DivergenceValues(currentDiv, newDiv)
    }

    fun SharedPreferences.saveDivergence(
        currentDiv: Int = UNDEFINED_DIVERGENCE,
        nextDiv: Int = UNDEFINED_DIVERGENCE
    ) {
        with(edit()) {
            if (currentDiv != UNDEFINED_DIVERGENCE)
                putInt(SHARED_CURRENT_DIVERGENCE, currentDiv)
            if (nextDiv != UNDEFINED_DIVERGENCE)
                putInt(SHARED_NEXT_DIVERGENCE, nextDiv)
            apply()
        }
    }

    // Returns new attractor name or null if attractor hasn't been changed
    fun checkAttractorChange(oldDiv: Int, newDiv: Int): String? {
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

class DivergenceValues(val current: Int, val next: Int)