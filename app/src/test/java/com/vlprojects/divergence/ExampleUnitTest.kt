package com.vlprojects.divergence

import org.junit.Test
import java.util.Date

class ExampleUnitTest {
    @Test
    fun test() {

    }

    @Test
    fun zeroCooldownTest() {
        val divergence = 999_999
        val attractor = DivergenceMeter.getAttractor(divergence)
        val lastTimeChanged = Date().time - 1000        // One second ago
        val cooldownMs = 0L

        var newDiv: Int
        for (i in 1..100) {
            newDiv = DivergenceMeter.generateBalancedDivergenceWithCooldown(divergence, lastTimeChanged, cooldownMs)
            if (newDiv !in attractor) {
                return
            }
        }

        println("Attractor haven't been changed")
        throw RuntimeException("Attractor haven't been changed")
    }
}
