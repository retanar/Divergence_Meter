package com.vlprojects.divergence

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.absoluteValue

class ExampleUnitTest {
    @Test
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

    @Test
    fun splitIntegerToDigits() {
        val int = -1234567
        val digits = ByteArray(7)
        var integer = int

        for (i in digits.indices) {
            digits[i] = (integer % 10).toByte()
            integer /= 10
        }

        if (int <= 0)
            digits[6] = -1

        digits.forEach { println(it) }
        //return digits
    }

}
