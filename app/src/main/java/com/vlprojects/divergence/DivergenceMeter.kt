package com.vlprojects.divergence

import android.content.SharedPreferences
import android.util.Log
import java.util.Date
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextInt

@Suppress("MemberVisibilityCanBePrivate")
object DivergenceMeter {
    private const val DIVERGENCE_CHANGE_STEP = 100_000
    private const val MAX_COEFFICIENT = DIVERGENCE_CHANGE_STEP / 4

    fun generateRandomDivergence() = Random.nextInt(ALL_RANGE.range)

    fun generateBalancedDivergenceWithCooldown(currentDiv: Int, lastTimeChanged: Long): Int {
        val cooldownTime = Date().time - ATTRACTOR_CHANGE_COOLDOWN_MS
        return if (lastTimeChanged < cooldownTime)
            generateBalancedDivergence(currentDiv, ALL_RANGE)
        else
            generateBalancedDivergence(currentDiv, getAttractor(currentDiv))
    }

    fun generateBalancedDivergence(currentDiv: Int, attractor: Attractor = ALL_RANGE): Int {
        if (currentDiv !in attractor)
            throw IllegalArgumentException("Current divergence ($currentDiv) is not in attractor's range (${attractor.range})")

        val coefficient = getCoefficient(currentDiv)
        var newDiv: Int

        do {
            newDiv = currentDiv +
                    Random.nextInt(
                        -DIVERGENCE_CHANGE_STEP + coefficient,
                        DIVERGENCE_CHANGE_STEP + coefficient
                    )
        } while (newDiv !in attractor)

        Log.d(
            "DivergenceMeter",
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
    private fun getCoefficient(currentDiv: Int) =
        (-getAttractor(currentDiv).range.first + currentDiv - 500_000) /
                -(MILLION / 2 / MAX_COEFFICIENT)

    fun getAttractor(div: Int) = attractors.find { div in it }
        ?: throw IllegalArgumentException("Divergence is out of range of existing attractors!")

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

    /** Returns new attractor name or null if attractor hasn't been changed */
    fun checkAttractorChange(oldDiv: Int, newDiv: Int): String? =
        with(getAttractor(newDiv)) {
            if (oldDiv !in this) this.name
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

data class DivergenceValues(val currentDiv: Int, val nextDiv: Int)