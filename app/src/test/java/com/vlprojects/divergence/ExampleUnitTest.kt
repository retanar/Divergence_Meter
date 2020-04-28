package com.vlprojects.divergence

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun randomTest() {
        val dw = DivergenceWidget()
        var newDiv = 800_000
        while (newDiv in 0..999999) {
            newDiv = dw.generateRandomDivergence(newDiv)
            //print("$newDiv, ")
        }
        println(newDiv)
    }

    @Test
    fun splitIntegerToDigits() {
        val int = -1234567
        val digits = ByteArray(7)
        var integer = int

        for (i in 0..6) {
            digits[i] = (integer % 10).toByte()
            integer /= 10
        }

        if (int <= 0)
            digits[6] = -1

        digits.forEach { println(it) }
        //return digits
    }
}
