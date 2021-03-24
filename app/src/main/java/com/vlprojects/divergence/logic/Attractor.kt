package com.vlprojects.divergence.logic

class Attractor(val name: String, val range: IntRange) {
    operator fun contains(value: Int) = value in range
}

val OMEGA_ATTRACTOR = Attractor("Omega", -MILLION until 0)        // Inclusive -1000000 is needed for getCoefficient()
val ALPHA_ATTRACTOR = Attractor("Alpha", 0 until MILLION)
val BETA_ATTRACTOR = Attractor("Beta", MILLION until 2 * MILLION)
val GAMMA_ATTRACTOR = Attractor("Gamma", 2 * MILLION until 3 * MILLION)
val DELTA_ATTRACTOR = Attractor("Delta", 3 * MILLION until 4 * MILLION)
val EPSILON_ATTRACTOR = Attractor("Epsilon", 4 * MILLION until 5 * MILLION)

val attractors = arrayOf(
    OMEGA_ATTRACTOR,
    ALPHA_ATTRACTOR,
    BETA_ATTRACTOR,
    GAMMA_ATTRACTOR,
    DELTA_ATTRACTOR,
//    EPSILON_ATTRACTOR,
)

val ALL_RANGE = Attractor("All", attractors.first().range.first..attractors.last().range.last)
