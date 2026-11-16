package com.diffy01.calculator

import kotlin.math.hypot

data class ComplexNumber(val real: Double, val imaginary: Double) {
    init {
        require(real.isFinite() && imaginary.isFinite()) { "Complex components must be finite" }
    }
    operator fun plus(other: ComplexNumber) = ComplexNumber(real + other.real, imaginary + other.imaginary)
    operator fun minus(other: ComplexNumber) = ComplexNumber(real - other.real, imaginary - other.imaginary)
    operator fun times(other: ComplexNumber) = ComplexNumber(
        real * other.real - imaginary * other.imaginary,
        real * other.imaginary + imaginary * other.real
    )
    operator fun div(other: ComplexNumber): ComplexNumber {
        val scale = other.real * other.real + other.imaginary * other.imaginary
        require(scale != 0.0) { "Cannot divide by zero" }
        return ComplexNumber(
            (real * other.real + imaginary * other.imaginary) / scale,
            (imaginary * other.real - real * other.imaginary) / scale
        )
    }
    fun conjugate() = ComplexNumber(real, -imaginary)
    fun magnitude() = hypot(real, imaginary)
    fun format(): String {
        val sign = if (imaginary < 0) " - " else " + "
        return AdvancedMath.format(real) + sign + AdvancedMath.format(kotlin.math.abs(imaginary)) + "i"
    }
}
