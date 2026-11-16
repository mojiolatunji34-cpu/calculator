package com.diffy01.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class AdvancedMathTest {
    @Test fun fractionConversionAndArithmetic() {
        assertEquals("3/4", Fraction.fromDecimal(0.75).toString())
        assertEquals("5/6", (Fraction(1,2) + Fraction(1,3)).toString())
        assertEquals(0.5, Fraction(1,2).decimal(), 1e-10)
    }
    @Test fun fractionArithmeticRejectsOverflowInsteadOfWrapping() {
        assertThrows(ArithmeticException::class.java) { Fraction(Long.MAX_VALUE,1) + Fraction(1,1) }
        assertEquals("-1/2", Fraction(1,-2).normalized().toString())
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
    @Test fun matrixAdvancedOperations() {
        val matrix = listOf(listOf(1.0,2.0),listOf(3.0,4.0))
        val inverse = AdvancedMath.inverse(matrix)
        assertEquals(-2.0, inverse[0][0], 1e-10)
        assertEquals(5.0, AdvancedMath.trace(matrix), 1e-10)
        assertEquals(2, AdvancedMath.rank(matrix))
        assertEquals(listOf(listOf(1.0,3.0),listOf(2.0,4.0)), AdvancedMath.transpose(matrix))
        val eigen = AdvancedMath.symmetricEigenvalues(listOf(listOf(2.0,1.0),listOf(1.0,2.0)))
        assertEquals(3.0, eigen[0], 1e-8)
        assertEquals(1.0, eigen[1], 1e-8)
    }
    @Test fun richerStatisticsAndUnitGroups() {
        val stats=AdvancedMath.statistics(listOf(1.0,2.0,2.0,3.0))
        assertEquals(2.0, stats.getValue("Mode 1"), 1e-10)
        assertEquals(2.0/3.0, stats.getValue("Sample variance"), 1e-10)
        assertEquals(3600.0, AdvancedMath.convert(1.0,"h","s"),1e-10)
        assertEquals(100.0, AdvancedMath.convert(1.0,"bar","kPa"),1e-10)
        assertEquals(273.15, AdvancedMath.convert(32.0,"°F","K"),1e-10)
    }
    @Test fun unitConversion() {
        assertEquals(1000.0, AdvancedMath.convert(1.0,"km","m"),1e-10)
        assertEquals(32.0, AdvancedMath.convert(0.0,"°C","°F"),1e-10)
    }
}
