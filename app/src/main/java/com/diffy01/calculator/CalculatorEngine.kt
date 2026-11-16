package com.diffy01.calculator

import kotlin.math.*

/** Safe recursive-descent expression evaluator. It never executes user input as code. */
object CalculatorEngine {
    fun evaluate(source: String, degrees: Boolean = true): Double {
        val parser = Parser(source, degrees)
        val result = parser.parse()
        if (!result.isFinite()) throw IllegalArgumentException("Result is outside the supported range")
        return if (abs(result) < 1e-12) 0.0 else result
    }

    private class Parser(private val input: String, private val degrees: Boolean) {
        private var pos = 0
        private fun skip() { while (pos < input.length && input[pos].isWhitespace()) pos++ }
        private fun eat(c: Char): Boolean {
            skip()
            if (pos < input.length && input[pos] == c) { pos++; return true }
            return false
        }
        private fun require(value: Boolean, message: String) {
            if (!value) throw IllegalArgumentException(message)
        }
        fun parse(): Double {
            val value = expression()
            skip()
            require(pos == input.length, "Unexpected input")
            return value
        }
        private fun expression(): Double {
            var value = term()
            while (true) {
                value = when {
                    eat('+') -> value + term()
                    eat('-') -> value - term()
                    else -> return value
                }
            }
        }
        private fun term(): Double {
            var value = unary()
            while (true) {
                value = when {
                    eat('*') || eat('×') -> value * unary()
                    eat('/') || eat('÷') -> {
                        val divisor = unary()
                        require(divisor != 0.0, "Cannot divide by zero")
                        value / divisor
                    }
                    eat('%') -> value / 100.0
                    else -> return value
                }
            }
        }
        private fun unary(): Double {
            if (eat('+')) return unary()
            if (eat('-')) return -unary()
            return power()
        }
        private fun power(): Double {
            var value = postfix()
            if (eat('^')) value = value.pow(unary())
            return value
        }
        private fun postfix(): Double {
            var value = primary()
            while (eat('!')) {
                require(value >= 0.0 && value <= 170.0 && value % 1.0 == 0.0, "Factorial requires an integer from 0 to 170")
                var result = 1.0
                for (n in 2..value.toInt()) result *= n
                value = result
            }
            return value
        }
        private fun primary(): Double {
            skip()
            require(pos < input.length, "Enter a number or function")
            if (eat('(')) {
                val value = expression()
                require(eat(')'), "Missing closing bracket")
                return value
            }
            if (input[pos].isDigit() || input[pos] == '.') return number()
            if (input[pos].isLetter() || input[pos] == 'π') {
                val name = identifier()
                if (name == "pi" || name == "π") return Math.PI
                if (name == "e") return Math.E
                require(eat('('), "Function requires parentheses")
                val x = expression()
                require(eat(')'), "Missing function bracket")
                val angle = if (degrees) Math.toRadians(x) else x
                return when (name) {
                    "sin" -> sin(angle)
                    "cos" -> cos(angle)
                    "tan" -> {
                        require(abs(cos(angle)) > 1e-12, "Tangent is undefined here")
                        tan(angle)
                    }
                    "asin" -> Math.toDegrees(asin(x)).let { if (degrees) it else asin(x) }
                    "acos" -> Math.toDegrees(acos(x)).let { if (degrees) it else acos(x) }
                    "atan" -> Math.toDegrees(atan(x)).let { if (degrees) it else atan(x) }
                    "sinh" -> sinh(x)
                    "cosh" -> cosh(x)
                    "tanh" -> tanh(x)
                    "sqrt" -> { require(x >= 0, "Square root requires a non-negative value"); sqrt(x) }
                    "cbrt" -> cbrt(x)
                    "ln" -> { require(x > 0, "Natural logarithm requires a positive value"); ln(x) }
                    "log" -> { require(x > 0, "Logarithm requires a positive value"); log10(x) }
                    "abs" -> abs(x)
                    "exp" -> exp(x)
                    "floor" -> floor(x)
                    "ceil" -> ceil(x)
                    else -> throw IllegalArgumentException("Unknown function: " + name)
                }
            }
            throw IllegalArgumentException("Invalid character")
        }
        private fun identifier(): String {
            val start = pos
            while (pos < input.length && (input[pos].isLetter() || input[pos] == 'π')) pos++
            return input.substring(start, pos).lowercase()
        }
        private fun number(): Double {
            skip()
            val start = pos
            var dots = 0
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
                if (input[pos] == '.') dots++
                pos++
            }
            require(dots <= 1 && pos > start, "Invalid number")
            return input.substring(start, pos).toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number")
        }
    }
}
