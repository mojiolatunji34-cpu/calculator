package com.diffy01.calculator

import kotlin.math.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.math.BigInteger

data class Fraction(val numerator: Long, val denominator: Long) {
    init { require(denominator != 0L) { "A fraction cannot have a zero denominator" } }
    fun normalized(): Fraction = fromBig(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator))
    fun decimal(): Double = numerator.toDouble() / denominator
    override fun toString(): String {
        val f = normalized()
        return if (f.denominator == 1L) "${f.numerator}" else "${f.numerator}/${f.denominator}"
    }
    operator fun plus(other: Fraction) = fromBig(
        BigInteger.valueOf(numerator) * BigInteger.valueOf(other.denominator) + BigInteger.valueOf(other.numerator) * BigInteger.valueOf(denominator),
        BigInteger.valueOf(denominator) * BigInteger.valueOf(other.denominator)
    )
    operator fun minus(other: Fraction) = fromBig(
        BigInteger.valueOf(numerator) * BigInteger.valueOf(other.denominator) - BigInteger.valueOf(other.numerator) * BigInteger.valueOf(denominator),
        BigInteger.valueOf(denominator) * BigInteger.valueOf(other.denominator)
    )
    operator fun times(other: Fraction) = fromBig(
        BigInteger.valueOf(numerator) * BigInteger.valueOf(other.numerator),
        BigInteger.valueOf(denominator) * BigInteger.valueOf(other.denominator)
    )
    operator fun div(other: Fraction): Fraction {
        require(other.numerator != 0L) { "Cannot divide by zero" }
        return fromBig(BigInteger.valueOf(numerator) * BigInteger.valueOf(other.denominator),
            BigInteger.valueOf(denominator) * BigInteger.valueOf(other.numerator))
    }
    companion object {
        fun fromDecimal(value: Double, maxDenominator: Int = 100000): Fraction {
            require(value.isFinite() && abs(value) <= Long.MAX_VALUE.toDouble()) { "Number is outside the supported fraction range" }
            require(maxDenominator > 0) { "Maximum denominator must be positive" }
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
        private fun fromBig(numerator: BigInteger, denominator: BigInteger): Fraction {
            require(denominator != BigInteger.ZERO) { "A fraction cannot have a zero denominator" }
            var n = numerator
            var d = denominator
            if (d.signum() < 0) { n = n.negate(); d = d.negate() }
            val gcd = n.abs().gcd(d)
            return Fraction(n.divide(gcd).longValueExact(), d.divide(gcd).longValueExact())
        }
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
        requireSquare(m)
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
        requireRectangular(a); requireRectangular(b)
        require(a.first().size == b.size) { "Matrix dimensions do not match" }
        return List(a.size) { r -> List(b[0].size) { c -> a[0].indices.sumOf { k -> a[r][k] * b[k][c] } } }
    }
    fun statistics(values: List<Double>): Map<String, Double> {
        require(values.isNotEmpty() && values.all { it.isFinite() }) { "Enter at least one valid number" }
        val sorted = values.sorted(); val mean = values.average()
        val median = if (sorted.size % 2 == 1) sorted[sorted.size/2] else (sorted[sorted.size/2-1] + sorted[sorted.size/2]) / 2
        val variance = values.sumOf { (it - mean).pow(2) } / values.size
        val sampleVariance = if (values.size > 1) values.sumOf { (it - mean).pow(2) } / (values.size - 1) else 0.0
        val frequencies = values.groupingBy { it }.eachCount()
        val maxFrequency = frequencies.values.maxOrNull() ?: 0
        val modes = if (maxFrequency <= 1) emptyList() else frequencies.filterValues { it == maxFrequency }.keys.sorted()
        val q1 = percentile(sorted, 0.25)
        val q3 = percentile(sorted, 0.75)
        return linkedMapOf(
            "Count" to values.size.toDouble(), "Mean" to mean, "Median" to median,
            "Minimum" to sorted.first(), "Maximum" to sorted.last(),
            "Range" to sorted.last() - sorted.first(), "First quartile (Q1)" to q1,
            "Third quartile (Q3)" to q3, "Interquartile range" to q3 - q1,
            "Population variance" to variance, "Population standard deviation" to sqrt(variance),
            "Sample variance" to sampleVariance, "Sample standard deviation" to sqrt(sampleVariance),
            "Mode count" to modes.size.toDouble()
        ) + modes.mapIndexed { index, value -> "Mode ${index + 1}" to value }
    }
    private fun percentile(sorted: List<Double>, p: Double): Double {
        if (sorted.size == 1) return sorted.first()
        val position = p * (sorted.size - 1)
        val lower = floor(position).toInt()
        val upper = ceil(position).toInt()
        return sorted[lower] + (sorted[upper] - sorted[lower]) * (position - lower)
    }
    fun transpose(matrix: List<List<Double>>): List<List<Double>> {
        requireRectangular(matrix)
        return List(matrix.first().size) { c -> matrix.map { it[c] } }
    }
    fun trace(matrix: List<List<Double>>): Double {
        requireSquare(matrix)
        return matrix.indices.sumOf { matrix[it][it] }
    }
    fun inverse(matrix: List<List<Double>>): List<List<Double>> {
        requireSquare(matrix)
        val n = matrix.size
        val a = Array(n) { r -> DoubleArray(2 * n) { c -> when { c < n -> matrix[r][c]; c - n == r -> 1.0; else -> 0.0 } } }
        for (col in 0 until n) {
            var pivot = col
            for (row in col + 1 until n) if (abs(a[row][col]) > abs(a[pivot][col])) pivot = row
            require(abs(a[pivot][col]) > 1e-12) { "Matrix is singular and has no inverse" }
            val swap = a[col]; a[col] = a[pivot]; a[pivot] = swap
            val divisor = a[col][col]
            for (j in 0 until 2 * n) a[col][j] /= divisor
            for (row in 0 until n) if (row != col) {
                val factor = a[row][col]
                for (j in 0 until 2 * n) a[row][j] -= factor * a[col][j]
            }
        }
        return List(n) { r -> List(n) { c -> a[r][c + n] } }
    }
    fun symmetricEigenvalues(matrix: List<List<Double>>): List<Double> {
        requireSquare(matrix)
        val n = matrix.size
        require(matrix.indices.all { r -> (0 until n).all { c -> abs(matrix[r][c] - matrix[c][r]) <= 1e-10 } }) {
            "Eigenvalue solver currently requires a real symmetric matrix"
        }
        val a = Array(n) { r -> matrix[r].toDoubleArray() }
        repeat(n * n * 50) {
            var p = 0; var q = 0; var largest = 0.0
            for (r in 0 until n) for (c in r + 1 until n) {
                if (abs(a[r][c]) > largest) { largest = abs(a[r][c]); p = r; q = c }
            }
            if (largest < 1e-12) return@repeat
            val angle = 0.5 * atan2(2.0 * a[p][q], a[q][q] - a[p][p])
            val cosine = cos(angle); val sine = sin(angle)
            val app = a[p][p]; val aqq = a[q][q]; val apq = a[p][q]
            a[p][p] = cosine*cosine*app - 2*sine*cosine*apq + sine*sine*aqq
            a[q][q] = sine*sine*app + 2*sine*cosine*apq + cosine*cosine*aqq
            a[p][q] = 0.0; a[q][p] = 0.0
            for (k in 0 until n) if (k != p && k != q) {
                val akp = a[k][p]; val akq = a[k][q]
                a[k][p] = cosine*akp - sine*akq; a[p][k] = a[k][p]
                a[k][q] = sine*akp + cosine*akq; a[q][k] = a[k][q]
            }
        }
        return (0 until n).map { a[it][it] }.sortedDescending()
    }
    fun rank(matrix: List<List<Double>>): Int {
        requireRectangular(matrix)
        val a = matrix.map { it.toMutableList() }.toMutableList()
        var pivotRow = 0
        for (col in a.first().indices) {
            if (pivotRow >= a.size) break
            var pivot = pivotRow
            for (r in pivotRow + 1 until a.size) if (abs(a[r][col]) > abs(a[pivot][col])) pivot = r
            if (abs(a[pivot][col]) <= 1e-10) continue
            val swap = a[pivotRow]; a[pivotRow] = a[pivot]; a[pivot] = swap
            val divisor = a[pivotRow][col]
            for (c in col until a[pivotRow].size) a[pivotRow][c] /= divisor
            for (r in a.indices) if (r != pivotRow) {
                val factor = a[r][col]
                for (c in col until a[r].size) a[r][c] -= factor * a[pivotRow][c]
            }
            pivotRow++
        }
        return pivotRow
    }
    private fun requireRectangular(matrix: List<List<Double>>) {
        require(matrix.isNotEmpty() && matrix.first().isNotEmpty() && matrix.all { it.size == matrix.first().size && it.all(Double::isFinite) }) { "Matrix must be non-empty, rectangular and finite" }
    }
    private fun requireSquare(matrix: List<List<Double>>) {
        requireRectangular(matrix)
        require(matrix.size == matrix.first().size) { "Matrix must be square" }
    }
    fun factorial(n: Int): Double { require(n in 0..170) { "Enter an integer from 0 to 170" }; var x = 1.0; for (i in 2..n) x *= i; return x }
    fun permutation(n: Int, r: Int): Double { require(n >= 0 && r in 0..n) { "Require n ≥ r ≥ 0" }; var x=1.0; for(i in n-r+1..n) x*=i; return x }
    fun combination(n: Int, r: Int): Double { require(n >= 0 && r in 0..n) { "Require n ≥ r ≥ 0" }; val k=min(r,n-r); var x=1.0; for(i in 1..k) x=x*(n-k+i)/i; return x }
    fun convert(value: Double, from: String, to: String): Double {
        val units = mapOf("m" to 1.0, "km" to 1000.0, "cm" to .01, "mm" to .001, "mi" to 1609.344, "ft" to .3048, "in" to .0254, "kg" to 1.0, "g" to .001, "lb" to .45359237, "oz" to .028349523125, "L" to 1.0, "mL" to .001, "μL" to .000001, "m³" to 1000.0,
            "s" to 1.0, "min" to 60.0, "h" to 3600.0, "day" to 86400.0,
            "Pa" to 1.0, "kPa" to 1000.0, "bar" to 100000.0, "atm" to 101325.0,
            "J" to 1.0, "kJ" to 1000.0, "cal" to 4.184)
        if (from == "°C" && to == "°F") return value * 9/5 + 32
        if (from == "°F" && to == "°C") return (value - 32) * 5/9
        if (from == "°C" && to == "K") return value + 273.15
        if (from == "K" && to == "°C") return value - 273.15
        if (from == "°F" && to == "K") return (value - 32) * 5/9 + 273.15
        if (from == "K" && to == "°F") return (value - 273.15) * 9/5 + 32
        require(from in units && to in units) { "Choose compatible units" }
        val groups = mapOf("m" to "length", "km" to "length", "cm" to "length", "mm" to "length", "mi" to "length", "ft" to "length", "in" to "length", "kg" to "mass", "g" to "mass", "lb" to "mass", "oz" to "mass", "L" to "volume", "mL" to "volume", "μL" to "volume", "m³" to "volume",
            "s" to "time", "min" to "time", "h" to "time", "day" to "time",
            "Pa" to "pressure", "kPa" to "pressure", "bar" to "pressure", "atm" to "pressure",
            "J" to "energy", "kJ" to "energy", "cal" to "energy")
        require(groups[from] == groups[to]) { "Those units are not compatible" }
        return value * units.getValue(from) / units.getValue(to)
    }
    fun format(x: Double, maxDecimals: Int = 10): String {
        require(maxDecimals in 0..15) { "Decimal places must be between 0 and 15" }
        if (!x.isFinite()) return "Undefined"
        val value = if (abs(x) < 1e-12) 0.0 else x
        val fractionPattern = if (maxDecimals == 0) "" else "." + "#".repeat(maxDecimals)
        val pattern = if (abs(value) >= 1e12 || (value != 0.0 && abs(value) < 1e-8))
            "0$fractionPattern" + "E0" else "0$fractionPattern"
        return DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(value)
    }
}
