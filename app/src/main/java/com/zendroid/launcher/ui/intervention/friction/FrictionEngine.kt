package com.zendroid.launcher.ui.intervention.friction

import kotlin.random.Random

/**
 * Engine for selecting and managing friction types.
 * 
 * Implements randomized friction to prevent habituation.
 * Respects user's friction level preference from Settings.
 */
object FrictionEngine {

    enum class FrictionType {
        LIQUID_HOLD,    // 7s press-and-hold
        BREATHING,      // 15s breathing animation
        MATH            // Random math problem
    }

    enum class FrictionLevel {
        LOW,    // Quick hold (3s)
        MEDIUM, // Moderate hold (5s) or Breathing (10s)
        HIGH,   // Full randomization (7s hold / 15s breathing)
        EXTREME // Math or Long Breathing (20s)
    }

    /**
     * Selects a random friction type based on the user's friction level.
     */
    fun selectFriction(level: FrictionLevel): FrictionType {
        return when (level) {
            FrictionLevel.LOW -> FrictionType.LIQUID_HOLD
            FrictionLevel.MEDIUM -> if (Random.nextBoolean()) FrictionType.LIQUID_HOLD else FrictionType.BREATHING
            FrictionLevel.HIGH -> {
                val types = FrictionType.entries
                types[Random.nextInt(types.size)]
            }
            FrictionLevel.EXTREME -> if (Random.nextBoolean()) FrictionType.MATH else FrictionType.BREATHING
        }
    }

    /**
     * Gets the duration in milliseconds for a friction type.
     */
    fun getDurationMs(type: FrictionType, level: FrictionLevel): Long {
        return when (type) {
            FrictionType.LIQUID_HOLD -> when(level) {
                FrictionLevel.LOW -> 3000L
                FrictionLevel.MEDIUM -> 5000L
                else -> 7000L
            }
            FrictionType.BREATHING -> when(level) {
                FrictionLevel.MEDIUM -> 10000L
                FrictionLevel.EXTREME -> 20000L
                else -> 15000L
            }
            FrictionType.MATH -> 0L // No fixed duration; depends on user
        }
    }

    /**
     * Generates a random math problem.
     * Returns Pair(displayString, correctAnswer)
     */
    fun generateMathProblem(): Pair<String, Int> {
        val a = Random.nextInt(10, 50)
        val b = Random.nextInt(10, 50)
        return Pair("$a + $b = ?", a + b)
    }
}
