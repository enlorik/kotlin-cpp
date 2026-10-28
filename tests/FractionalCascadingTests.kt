import kotlin.random.Random

fun main() {
    val first = intArrayOf(1, 2, 2, 6)
    val empty = intArrayOf()
    val last = intArrayOf(2, 2, 3)
    val cascade = fractionalCascade(first, empty, last)
    check(cascade.lowerBounds(2).contentEquals(intArrayOf(1, 0, 0)))
    check(cascade.upperBounds(2).contentEquals(intArrayOf(3, 0, 2)))
    check(cascade.lowerBounds(Int.MIN_VALUE).contentEquals(intArrayOf(0, 0, 0)))
    check(cascade.upperBounds(Int.MAX_VALUE).contentEquals(intArrayOf(4, 0, 3)))
    first[0] = 999 // Caller mutations must not affect the cascade.
    check(cascade.lowerBounds(2)[0] == 1)
    check(fractionalCascade().lowerBounds(1).isEmpty())
    var rejected = false
    try { fractionalCascade(intArrayOf(2, 1)) }
    catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)

    val random = Random(20913)
    var comparisons = 0
    repeat(10000) {
        val count = random.nextInt(0, 20)
        val catalogs = List(count) {
            IntArray(random.nextInt(0, 45)) {
                when (random.nextInt(30)) {
                    0 -> Int.MIN_VALUE
                    1 -> Int.MAX_VALUE
                    else -> random.nextInt(-30, 31)
                }
            }.sortedArray()
        }
        val fc = FractionalCascade(catalogs)
        repeat(8) {
            val value = when (random.nextInt(20)) {
                0 -> Int.MIN_VALUE
                1 -> Int.MAX_VALUE
                else -> random.nextInt(-32, 33)
            }
            val lowers = fc.lowerBounds(value)
            val uppers = fc.upperBounds(value)
            check(lowers.size == count && uppers.size == count)
            for (i in 0 until count) {
                val a = catalogs[i]
                val expectedLower = a.indexOfFirst { it >= value }.let { if (it < 0) a.size else it }
                val expectedUpper = a.indexOfFirst { it > value }.let { if (it < 0) a.size else it }
                check(lowers[i] == expectedLower)
                check(uppers[i] == expectedUpper)
                comparisons++
            }
        }
    }

    // Many independent catalogs: compare all bounds against a direct reference.
    val catalogs = List(80) { i -> IntArray(500) { j -> i * 1000 + j * 2 } }
    val large = FractionalCascade(catalogs)
    repeat(1000) {
        val value = random.nextInt(-100, 82000)
        val lowers = large.lowerBounds(value)
        val uppers = large.upperBounds(value)
        for (i in catalogs.indices) {
            val a = catalogs[i]
            val expectedLower = a.indexOfFirst { it >= value }.let { if (it < 0) a.size else it }
            val expectedUpper = a.indexOfFirst { it > value }.let { if (it < 0) a.size else it }
            check(lowers[i] == expectedLower && uppers[i] == expectedUpper)
        }
    }
    println("PASS: $comparisons randomized catalog bounds plus 80000 large catalog comparisons")
}
