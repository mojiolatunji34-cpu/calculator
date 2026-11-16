package com.diffy01.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedMathTest {
    @Test fun fractionConversionAndArithmetic() {
        assertEquals("3/4", Fraction.fromDecimal(0.75).toString())
        assertEquals("5/6", (Fraction(1,2) + Fraction(1,3)).toString())
        assertEquals(0.5, Fraction(1,2).decimal(), 1e-10)
    }
    @Test fun linearAndQuadraticEquations() {
        assertEquals("x = -2", AdvancedMath.solveLinear(2.0,4.0))
        assertTrue(AdvancedMath.solveQuadratic(1.0,-3.0,2.0).contains("x₁ = 2"))
    }
    @Test fun matrixDeterminant() {
        assertEquals(-2.0, AdvancedMath.determinant(listOf(listOf(1.0,2.0),listOf(3.0,4.0))),1e-10)
    }
    @Test fun statisticsAndCounting() {
        assertEquals(5.0, AdvancedMath.statistics(listOf(2.0,4.0,6.0,8.0)).getValue("Mean"),1e-10)
        assertEquals(20.0, AdvancedMath.permutation(5,2),1e-10)
        assertEquals(10.0, AdvancedMath.combination(5,2),1e-10)
    }
    @Test fun unitConversion() {
        assertEquals(1000.0, AdvancedMath.convert(1.0,"km","m"),1e-10)
        assertEquals(32.0, AdvancedMath.convert(0.0,"°C","°F"),1e-10)
    }
}
