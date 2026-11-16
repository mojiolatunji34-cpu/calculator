package com.diffy01.calculator

import kotlin.math.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class Fraction(val numerator: Long, val denominator: Long) {
    init { require(denominator != 0L) { "A fraction cannot have a zero denominator" } }
    fun normalized(): Fraction {
        val sign = if (denominator < 0) -1L else 1L
        val gcd = gcd(absSafe(numerator), absSafe(denominator))
        return Fraction(sign * numerator / gcd, sign * denominator / gcd)
    }
    fun decimal(): Double = numerator.toDouble() / denominator
    override fun toString(): String {
        val f = normalized()
        return if (f.denominator == 1L) "${f.numerator}" else "${f.numerator}/${f.denominator}"
    }
    operator fun plus(other: Fraction) = Fraction(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator).normalized()
    operator fun minus(other: Fraction) = Fraction(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator).normalized()
    operator fun times(other: Fraction) = Fraction(numerator * other.numerator, denominator * other.denominator).normalized()
    operator fun div(other: Fraction): Fraction { require(other.numerator != 0L) { "Cannot divide by zero" }; return Fraction(numerator * other.denominator, denominator * other.numerator).normalized() }
    companion object {
        fun fromDecimal(value: Double, maxDenominator: Int = 100000): Fraction {
            require(value.isFinite()) { "Enter a finite number" }
            var x = abs(value); var h1 = 1L; var h2 = 0L; var k1 = 0L; var k2 = 1L
            while (true) {
                val a = floor(x).toLong()
                val h = a * h1 + h2; val k = a * k1 + k2
                if (k > maxDenominator || abs(value - h.toDouble() / k) < 1e-10) return Fraction(if (value < 0) -h else h, k).normalized()
                h2 = h1; h1 = h; k2 = k1; k1 = k
                val remainder = x - a
                if (remainder < 1e-12) return Fraction(if (value < 0) -h else h, k).normalized()
                x = 1.0 / remainder
            }
        }
        private fun gcd(a0: Long, b0: Long): Long {
            var a = a0; var b = b0
            while (b != 0L) { val t = b; b = a % b; a = t }
            return if (a == 0L) 1L else a
        }
        private fun absSafe(v: Long) = if (v == Long.MIN_VALUE) Long.MAX_VALUE else abs(v)
    }
}

object AdvancedMath {
    fun solveLinear(a: Double, b: Double): String {
        require(a.isFinite() && b.isFinite()) { "Use finite coefficients" }
        require(a != 0.0) { "No unique solution: coefficient of x is zero" }
        return "x = ${format(-b / a)}"
    }
    fun solveQuadratic(a: Double, b: Double, c: Double): String {
        require(a != 0.0) { "The coefficient a must not be zero" }
        val d = b*b - 4*a*c
        if (d < 0) return "No real roots (discriminant = ${format(d)})"
        val root = sqrt(d)
        return "x₁ = ${format((-b + root)/(2*a))},  x₂ = ${format((-b - root)/(2*a))}"
    }
    fun determinant(m: List<List<Double>>): Double {
        require(m.isNotEmpty() && m.size == m[0].size && m.all { it.size == m.size }) { "Matrix must be square" }
        val a = m.map { it.toMutableList() }.toMutableList()
        var det = 1.0
        for (i in a.indices) {
            var pivot = i
            for (r in i+1 until a.size) if (abs(a[r][i]) > abs(a[pivot][i])) pivot = r
            if (abs(a[pivot][i]) < 1e-12) return 0.0
            if (pivot != i) { val tmp = a[i]; a[i] = a[pivot]; a[pivot] = tmp; det = -det }
            val p = a[i][i]; det *= p
            for (r in i+1 until a.size) {
                val factor = a[r][i] / p
                for (c in i+1 until a.size) a[r][c] -= factor * a[i][c]
            }
        }
        return det
    }
    fun multiply(a: List<List<Double>>, b: List<List<Double>>): List<List<Double>> {
        require(a.isNotEmpty() && b.isNotEmpty() && a.all { it.size == b.size } && b.all { it.size == b[0].size }) { "Matrix dimensions do not match" }
        return List(a.size) { r -> List(b[0].size) { c -> a[0].indices.sumOf { k -> a[r][k] * b[k][c] } } }
    }
    fun statistics(values: List<Double>): Map<String, Double> {
        require(values.isNotEmpty() && values.all { it.isFinite() }) { "Enter at least one valid number" }
        val sorted = values.sorted(); val mean = values.average()
        val median = if (sorted.size % 2 == 1) sorted[sorted.size/2] else (sorted[sorted.size/2-1] + sorted[sorted.size/2]) / 2
        val variance = values.sumOf { (it - mean).pow(2) } / values.size
        return linkedMapOf("Count" to values.size.toDouble(), "Mean" to mean, "Median" to median, "Minimum" to sorted.first(), "Maximum" to sorted.last(), "Population variance" to variance, "Population standard deviation" to sqrt(variance))
    }
    fun factorial(n: Int): Double { require(n in 0..170) { "Enter an integer from 0 to 170" }; var x = 1.0; for (i in 2..n) x *= i; return x }
    fun permutation(n: Int, r: Int): Double { require(n >= 0 && r in 0..n) { "Require n ≥ r ≥ 0" }; var x=1.0; for(i in n-r+1..n) x*=i; return x }
    fun combination(n: Int, r: Int): Double { require(n >= 0 && r in 0..n) { "Require n ≥ r ≥ 0" }; val k=min(r,n-r); var x=1.0; for(i in 1..k) x=x*(n-k+i)/i; return x }
    fun convert(value: Double, from: String, to: String): Double {
        val units = mapOf("m" to 1.0, "km" to 1000.0, "cm" to .01, "mm" to .001, "mi" to 1609.344, "ft" to .3048, "in" to .0254, "kg" to 1.0, "g" to .001, "lb" to .45359237, "oz" to .028349523125, "L" to 1.0, "mL" to .001)
        if (from == "°C" && to == "°F") return value * 9/5 + 32
        if (from == "°F" && to == "°C") return (value - 32) * 5/9
        if (from == "°C" && to == "K") return value + 273.15
        if (from == "K" && to == "°C") return value - 273.15
        require(from in units && to in units) { "Choose compatible units" }
        val groups = mapOf("m" to "length", "km" to "length", "cm" to "length", "mm" to "length", "mi" to "length", "ft" to "length", "in" to "length", "kg" to "mass", "g" to "mass", "lb" to "mass", "oz" to "mass", "L" to "volume", "mL" to "volume")
        require(groups[from] == groups[to]) { "Those units are not compatible" }
        return value * units.getValue(from) / units.getValue(to)
    }
    fun format(x: Double): String {
        if (!x.isFinite()) return "Undefined"
        val value = if (abs(x) < 1e-12) 0.0 else x
        val pattern = if (abs(value) >= 1e12 || (value != 0.0 && abs(value) < 1e-8)) "0.##########E0" else "0.##########"
        return DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(value)
    }
}
