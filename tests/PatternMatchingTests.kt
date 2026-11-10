import kotlin.random.Random

private fun naivePi(s: String): IntArray = IntArray(s.length) { i ->
    (1..i).lastOrNull { length ->
        s.substring(0, length) == s.substring(i-length+1,i+1)
    } ?: 0
}

private fun naiveZ(s: String): IntArray = IntArray(s.length) { i ->
    if (i == 0) s.length else {
        var k = 0
        while (i+k < s.length && s[k] == s[i+k]) k++
        k
    }
}

private fun naiveKmp(text: String, pattern: String): IntArray {
    if (pattern.isEmpty()) return IntArray(text.length+1) { it }
    return (0..text.length-pattern.length).filter { start ->
        text.regionMatches(start,pattern,0,pattern.length)
    }.toIntArray()
}

fun main() {
    check(prefixFunction("").isEmpty())
    check(zFunction("").isEmpty())
    check(kmpOccurrences("aaa","aa").contentEquals(intArrayOf(0,1)))
    check(kmpOccurrences("a","").contentEquals(intArrayOf(0,1)))
    check(kmpOccurrences("a","ab").isEmpty())

    val aho = listOf("he","she","his","hers","he").ahoCorasick()
    val expected = listOf(
        PatternMatch(0,2,4), PatternMatch(4,2,4),
        PatternMatch(1,1,4), PatternMatch(3,2,6)
    ).sortedWith(compareBy({ it.from }, { it.patternIndex }))
    check(aho.findAll("ushers").sortedWith(compareBy({ it.from }, { it.patternIndex })) ==
        expected)
    check(aho.countPerPattern("ushers").contentEquals(longArrayOf(1,1,0,1,1)))
    check(emptyList<String>().ahoCorasick().findAll("hello").isEmpty())
    check(runCatching { listOf("").ahoCorasick() }.isFailure)

    val rng = Random(20261110)
    var checked = 0
    repeat(750) {
        val n = rng.nextInt(0,75)
        val text = CharArray(n) { "aabž"[rng.nextInt(4)] }.concatToString()
        check(prefixFunction(text).contentEquals(naivePi(text)))
        check(zFunction(text).contentEquals(naiveZ(text)))
        val patternCount = rng.nextInt(0,13)
        val patterns = List(patternCount) {
            CharArray(rng.nextInt(1,10)) {
                "aabž"[rng.nextInt(4)]
            }.concatToString()
        }
        val index = patterns.ahoCorasick()
        val allExpected = ArrayList<PatternMatch>()
        val counts = LongArray(patternCount)
        for ((id,pattern) in patterns.withIndex()) {
            val starts = naiveKmp(text,pattern)
            check(kmpOccurrences(text,pattern).contentEquals(starts))
            for (start in starts) {
                allExpected.add(PatternMatch(id,start,start+pattern.length))
                counts[id]++
            }
        }
        val order = compareBy<PatternMatch>({it.until},{it.patternIndex},{it.from})
        check(index.findAll(text).sortedWith(order) ==
            allExpected.sortedWith(order))
        check(index.countPerPattern(text).contentEquals(counts))
        checked++
    }
    // Adversarial suffix chain: memory should not copy all terminal ids
    // into every failure node, yet count queries must see all suffixes.
    val patterns = (1..150).map { "a".repeat(it) }
    val longIndex = patterns.ahoCorasick()
    val counts = longIndex.countPerPattern("a".repeat(1000))
    for (i in patterns.indices) check(counts[i] == (1000-i).toLong())
    check(longIndex.stateCount == 151)
    println("PASS: KMP/Z and Aho-Corasick ($checked randomized oracle streams)")
}
