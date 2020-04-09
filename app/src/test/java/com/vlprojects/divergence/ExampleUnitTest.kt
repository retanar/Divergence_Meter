package com.vlprojects.divergence

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun splitIntegerToDigits() {
        val int = -1234567
        val digits = ByteArray(7)
        var integer = if (int >= 0) int else -int

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
