package com.vlprojects.divergence

import android.content.SharedPreferences
import android.util.Log
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextInt

object DivergenceGenerator {
    private const val DIVERGENCE_CHANGE_STEP = 100_000
    private const val MAX_COEFFICIENT = DIVERGENCE_CHANGE_STEP / 4

    fun setRandomDivergence(preferences: SharedPreferences) {
        val randomDivergence = Random.nextInt(ALL_RANGE)
        Log.d("DivergenceWidget", "setRandomDivergence() call. Random divergence = $randomDivergence")

        preferences.edit()
            .putInt(SHARED_DIVERGENCE, randomDivergence)
            .putInt(SHARED_NEXT_DIVERGENCE, randomDivergence)
            .apply()
    }

    // TODO: 0.4.0 cooldown
    fun generateBalanced(currentDiv: Int): Int {
        val coefficient = getCoefficient(currentDiv)

        var newDiv = currentDiv +
                Random.nextInt(
                    -DIVERGENCE_CHANGE_STEP + coefficient,
                    DIVERGENCE_CHANGE_STEP + coefficient
                )

        Log.d(
            "DivergenceWidget",
            """generateBalancedRandomDivergence() call.
              |Previous div: $currentDiv;
              |Step limits: (${-DIVERGENCE_CHANGE_STEP + coefficient} ; ${DIVERGENCE_CHANGE_STEP + coefficient});
              |Step: ${newDiv - currentDiv};
              |New div: $newDiv;""".trimMargin()
        )

        while (newDiv !in ALL_RANGE) {
            if (newDiv < ALL_RANGE.first)
                newDiv += Random.nextInt(DIVERGENCE_CHANGE_STEP)
            else if (newDiv > ALL_RANGE.last)
                newDiv -= Random.nextInt(DIVERGENCE_CHANGE_STEP)
        }

        return newDiv
    }

    /* Coefficient needed to lower the chance of going to another attractor field
     * How it works:
     *  - equalize the divergence to range [0;1_000_000)
     *  - subtract half of the maximum divergence to put the divergence in range [-500_000;+500_000)
     *  - divide by a specific number to create the coefficient */
    private fun getCoefficient(currentDiv: Int): Int {
        return (when (currentDiv) {
            in BETA_RANGE -> -MILLION
            in OMEGA_RANGE -> +MILLION
            else -> 0
        } + currentDiv - 500_000) /
                -(MILLION / 2 / MAX_COEFFICIENT)
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