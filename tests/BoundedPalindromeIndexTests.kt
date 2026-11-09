import kotlin.random.Random

private fun bruteCount(s: CharArray, from: Int, until: Int, k: Int): Long {
    var result = 0L
    for (i in from until until) for (j in i until minOf(until, i + k)) {
        var lo = i
        var hi = j
        var ok = true
        while (lo < hi) {
            if (s[lo++] != s[hi--]) { ok = false; break }
        }
        if (ok) result++
    }
    return result
}

private fun blockBasics() {
    val initial = intArrayOf(1, 2, -3, 4, 5, 6, -7, 8)
    val blocks = initial.blockAssignSum(3)
    initial[0] = 999
    check(blocks.sum(0, 1) == 1L)
    blocks.assign(1, 7, -2)
    check(blocks.sum(0, 8) == 1L - 12L + 8L)
    blocks.replace(2, intArrayOf(10, 20, 30, 40))
    check(blocks.sum(0, 8) == 1L - 2 + 10 + 20 + 30 + 40 - 2 + 8)
    val text = "abcdefgh".paintedString(3)
    text.assign(1, 7, 'z')
    check((0 until 8).map { text[it] }.joinToString("") == "azzzzzzh")
    text.assign(2, 5, 'a')
    check((0 until 8).map { text[it] }.joinToString("") == "azaaazzh")
    for (n in 0..32) {
        val rng = Random(n + 239)
        val base = IntArray(n) { rng.nextInt(-50, 51) }
        val ref = base.copyOf()
        val b = base.blockAssignSum(1 + n % 7)
        repeat(300) {
            val l = rng.nextInt(n + 1)
            val r = rng.nextInt(l, n + 1)
            when (rng.nextInt(3)) {
                0 -> {
                    val v = rng.nextInt(-100, 101)
                    b.assign(l, r, v)
                    ref.fill(v, l, r)
                }
                1 -> {
                    val data = IntArray(r - l) { rng.nextInt(-100, 101) }
                    b.replace(l, data)
                    data.copyInto(ref, l)
                }
                else -> check(b.sum(l, r) == (l until r).sumOf { ref[it].toLong() })
            }
        }
    }
}

fun main() {
    blockBasics()
    check("ab".boundedPalindromes(1_000_000).count(0, 2) == 2L)
    check("".boundedPalindromes(1_000_000).count(0, 0) == 0L)
    check("".boundedPalindromes(5).count(0, 0) == 0L)
    check("a".boundedPalindromes(1).count(0, 1) == 1L)
    check("abacaba".boundedPalindromes(4).count(0, 3) == 4L)
    val official = "abacaba".boundedPalindromes(4)
    official.assign(0, 3, 'c')
    check(official.count(0, 4) == 10L)
    for (k in listOf(1, 2, 3, 4, 5, 7, 13, 49, 50)) {
        for (seed in 0..80) {
            val rnd = Random(9136 * (seed + 1) + k)
            val n = rnd.nextInt(1, 150)
            val chars = CharArray(n) { ('a'.code + rnd.nextInt(4)).toChar() }
            val index = String(chars).boundedPalindromes(k, 1 + seed % 11)
            repeat(350) {
                val l = rnd.nextInt(n)
                val r = rnd.nextInt(l + 1, n + 1)
                if (rnd.nextInt(2) == 0) {
                    val c = ('a'.code + rnd.nextInt(4)).toChar()
                    index.assign(l, r, c)
                    chars.fill(c, l, r)
                } else {
                    val actual = index.count(l, r)
                    val expected = bruteCount(chars, l, r, k)
                    check(actual == expected) {
                        "Mismatch k=$k seed=$seed range=[$l,$r) actual=$actual expected=$expected"
                    }
                }
            }
        }
    }
    println("bounded palindrome DSL OK")
}
