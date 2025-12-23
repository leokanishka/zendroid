package com.zendroid.launcher.ui.intervention.friction

import org.junit.Assert.*
import org.junit.Test

class FrictionEngineTest {

    @Test
    fun `selectFriction returns LIQUID_HOLD for LOW level`() {
        val result = FrictionEngine.selectFriction(FrictionEngine.FrictionLevel.LOW)
        assertEquals(FrictionEngine.FrictionType.LIQUID_HOLD, result)
    }

    @Test
    fun `getDurationMs returns correct values`() {
        assertEquals(3000L, FrictionEngine.getDurationMs(FrictionEngine.FrictionType.LIQUID_HOLD, FrictionEngine.FrictionLevel.LOW))
        assertEquals(7000L, FrictionEngine.getDurationMs(FrictionEngine.FrictionType.LIQUID_HOLD, FrictionEngine.FrictionLevel.HIGH))
        assertEquals(15000L, FrictionEngine.getDurationMs(FrictionEngine.FrictionType.BREATHING, FrictionEngine.FrictionLevel.HIGH))
    }

    @Test
    fun `generateMathProblem returns accurate result`() {
        val (display, result) = FrictionEngine.generateMathProblem()
        assertTrue(display.contains("+"))
        assertTrue(display.contains("="))
        
        val parts = display.split("+", "=").map { it.trim() }
        val sum = parts[0].toInt() + parts[1].toInt()
        assertEquals(sum, result)
    }
}
