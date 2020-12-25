package com.vlprojects.divergence

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.absoluteValue

class ExampleUnitTest {

    @Test
    fun constantsCheck() {
        println("MILLION: $MILLION")
        println("alpha range: $ALPHA_RANGE")
        println("beta range:  $BETA_RANGE")
        println("omega range: $OMEGA_RANGE")
        println("all range: $ALL_RANGE")
    }

    // @Test
    fun testRandomizer() {
        val dw = DivergenceWidget()
        var newDiv = 500_000
        var iteration = 0
        while (newDiv in 0..999999) {
            newDiv = dw.generateRandomDivergence(newDiv)
            iteration++
            //print("$newDiv, ")
        }
        println("$newDiv on iteration $iteration")
    }
}
