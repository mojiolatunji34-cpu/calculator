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

    @Test fun factorialAndPercent() {
        close(120.0, CalculatorEngine.evaluate("5!"))
        close(0.5, CalculatorEngine.evaluate("50%"))
    }

    @Test fun invalidOperationsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { CalculatorEngine.evaluate("1/0") }
        assertThrows(IllegalArgumentException::class.java) { CalculatorEngine.evaluate("sqrt(-1)") }
        assertThrows(IllegalArgumentException::class.java) { CalculatorEngine.evaluate("(2+3") }
    }
}
