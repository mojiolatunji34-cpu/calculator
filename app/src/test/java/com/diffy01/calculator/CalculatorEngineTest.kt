package com.diffy01.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculatorEngineTest {
    private fun close(expected: Double, actual: Double) = assertEquals(expected, actual, 1e-8)

    @Test fun arithmeticAndPrecedence() {
        close(14.0, CalculatorEngine.evaluate("2+3*4"))
        close(20.0, CalculatorEngine.evaluate("(2+3)*4"))
        close(8.0, CalculatorEngine.evaluate("2^3"))
        close(0.5, CalculatorEngine.evaluate("5/10"))
    }

    @Test fun scientificFunctionsInDegrees() {
        close(1.0, CalculatorEngine.evaluate("sin(90)"))
        close(1.0, CalculatorEngine.evaluate("cos(0)"))
        close(1.0, CalculatorEngine.evaluate("sqrt(1)"))
        close(2.0, CalculatorEngine.evaluate("log(100)"))
        close(Math.PI, CalculatorEngine.evaluate("pi"))
    }

    @Test fun scientificFunctionsInRadians() {
        close(1.0, CalculatorEngine.evaluate("sin(1.5707963267948966)", false))
    }

    @Test fun stepByStepEvaluationRecordsOperationOrder() {
        val result=CalculatorEngine.evaluateWithSteps("2+3*4")
        close(14.0,result.value)
        assertEquals(true,result.steps.any { it.contains("3 × 4 = 12") })
        assertEquals(true,result.steps.any { it.contains("2 + 12 = 14") })
    }

    @Test fun scientificNotationAndComplexArithmetic() {
        close(1500.0, CalculatorEngine.evaluate("1.5e3"))
        close(0.002, CalculatorEngine.evaluate("2E-3"))
        val z = ComplexNumber(3.0,2.0)
        val w = ComplexNumber(1.0,-4.0)
        assertEquals(ComplexNumber(11.0,-10.0), z*w)
        assertEquals(ComplexNumber(3.0,2.0), z.conjugate().conjugate())
    }

    @Test fun factorialAndPercent() {
        close(120.0, CalculatorEngine.evaluate("5!"))
        close(0.5, CalculatorEngine.evaluate("50%"))
    }

    @Test fun missingClosingParenthesesAreCompletedAutomatically() {
        close(0.5, CalculatorEngine.evaluate("cos(60"))
        close(0.5, CalculatorEngine.evaluate("cos(60+0"))
        close(5.0, CalculatorEngine.evaluate("(2+3"))
        close(14.0, CalculatorEngine.evaluate("2*(3+4"))
        assertEquals("cos(60)", CalculatorEngine.completeMissingClosingParentheses("cos(60"))
        assertEquals("cos(60)", CalculatorEngine.completeMissingClosingParentheses("cos(60)"))
    }

    @Test fun allScientificFunctionsAcceptMissingClosingBrackets() {
        close(1.0, CalculatorEngine.evaluate("sin(90"))
        close(0.5, CalculatorEngine.evaluate("cos(60"))
        close(1.0, CalculatorEngine.evaluate("tan(45"))
        close(30.0, CalculatorEngine.evaluate("asin(0.5"))
        close(60.0, CalculatorEngine.evaluate("acos(0.5"))
        close(45.0, CalculatorEngine.evaluate("atan(1"))
        close(kotlin.math.sinh(1.0), CalculatorEngine.evaluate("sinh(1"))
        close(kotlin.math.cosh(1.0), CalculatorEngine.evaluate("cosh(1"))
        close(kotlin.math.tanh(1.0), CalculatorEngine.evaluate("tanh(1"))
        close(3.0, CalculatorEngine.evaluate("sqrt(9"))
        close(3.0, CalculatorEngine.evaluate("cbrt(27"))
        close(1.0, CalculatorEngine.evaluate("ln(2.718281828459045"))
        close(2.0, CalculatorEngine.evaluate("log(100"))
    }

    @Test fun invalidOperationsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { CalculatorEngine.evaluate("1/0") }
        assertThrows(IllegalArgumentException::class.java) { CalculatorEngine.evaluate("sqrt(-1)") }
        assertThrows(IllegalArgumentException::class.java) { CalculatorEngine.evaluate("1e") }
    }
    @Test fun matrixOperationsHandleSquareAndRectangularInputs() {
        val a = listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))
        val b = listOf(listOf(5.0, 6.0), listOf(7.0, 8.0))
        assertEquals(listOf(listOf(6.0, 8.0), listOf(10.0, 12.0)), AdvancedMath.addMatrices(a, b))
        assertEquals(listOf(listOf(-4.0, -4.0), listOf(-4.0, -4.0)), AdvancedMath.subtractMatrices(a, b))
        assertEquals(listOf(listOf(19.0, 22.0), listOf(43.0, 50.0)), AdvancedMath.multiply(a, b))
        assertEquals(listOf(listOf(2.0, 4.0), listOf(6.0, 8.0)), AdvancedMath.scaleMatrix(a, 2.0))
        assertEquals(listOf(listOf(7.0, 10.0), listOf(15.0, 22.0)), AdvancedMath.matrixPower(a, 2))
        assertEquals(listOf(listOf(1.0, 3.0), listOf(2.0, 4.0)), AdvancedMath.transpose(a))
        close(-2.0, AdvancedMath.determinant(a))
        assertEquals(2, AdvancedMath.rank(a))
        assertEquals(5.0, AdvancedMath.trace(a), 1e-8)
    }

    @Test fun matrixOperationsRejectInvalidDimensionsAndSingularInverse() {
        val rectangular = listOf(listOf(1.0, 2.0, 3.0), listOf(4.0, 5.0, 6.0))
        assertThrows(IllegalArgumentException::class.java) {
            AdvancedMath.addMatrices(rectangular, listOf(listOf(1.0), listOf(2.0)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            AdvancedMath.matrixPower(rectangular, 2)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AdvancedMath.inverse(listOf(listOf(1.0, 2.0), listOf(2.0, 4.0)))
        }
    }

    @Test fun graphExpressionsEvaluateAfterReplacingX() {
        close(0.5, CalculatorEngine.evaluate("sin((30.0))"))
        close(4.0, CalculatorEngine.evaluate("((2.0))^2"))
        close(1.0, CalculatorEngine.evaluate("sqrt((1.0))"))
    }

}
