import kotlin.random.Random
import kotlin.math.abs

fun main() {
    val rng = Random(739921)
    repeat(500) {
        val a = DoubleArray(rng.nextInt(0,100)) { rng.nextDouble(-10.0,10.0) }
        val b = DoubleArray(rng.nextInt(0,100)) { rng.nextDouble(-10.0,10.0) }
        val expected = DoubleArray(if (a.isEmpty() || b.isEmpty()) 0 else a.size+b.size-1)
        for (i in a.indices) for (j in b.indices) expected[i+j] += a[i]*b[j]
        val actual = convolutionFft(a,b)
        check(actual.size == expected.size)
        for (i in actual.indices) check(abs(actual[i]-expected[i]) < 1e-7*maxOf(1.0,abs(expected[i])))
    }
    check(convolutionFft(doubleArrayOf(2.0),doubleArrayOf(-3.0)).contentEquals(doubleArrayOf(-6.0)))
    val stress = convolutionFft(DoubleArray(32768){1.0},DoubleArray(32768){1.0})
    for (i in stress.indices) check(abs(stress[i]-minOf(i+1,stress.size-i)) < 1e-4)
    println("PASS: FFT oracle and stress tests")
}
