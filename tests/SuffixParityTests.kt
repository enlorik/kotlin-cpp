import kotlin.random.Random

fun main() {
    val rng = Random(71623)
    repeat(300) {
        val s = (0 until rng.nextInt(0,65)).map { "abca\u0000\uffff"[rng.nextInt(6)] }.joinToString("")
        val index = s.suffixArray()
        check(index.suffixes.contentEquals(s.indices.sortedBy { s.substring(it) }.toIntArray()))
        for (i in s.indices) for (j in s.indices) {
            var k = 0
            while (i+k < s.length && j+k < s.length && s[i+k] == s[j+k]) k++
            check(index.commonPrefix(i,j) == k)
        }
        repeat(8) {
            val p = (0 until rng.nextInt(0,6)).map { "abc"[rng.nextInt(3)] }.joinToString("")
            check(index.occurrences(p).sorted() == s.indices.filter { s.startsWith(p,it) })
        }
        check(index.distinctSubstringCount() == s.indices.flatMap { i ->
            (i+1..s.length).map { j -> s.substring(i,j) }
        }.toSet().size.toLong())
    }
    val big = "a".repeat(100000).suffixArray()
    check(big.commonPrefix(0,1) == 99999)
    check(big.suffixes.first() == 99999)
    val dsu = ParityRollbackDsu(3)
    val start = dsu.snapshot()
    check(dsu.constrain(0,1)); check(dsu.constrain(1,2))
    check(dsu.parityBetween(0,2) == 0)
    val before = dsu.snapshot()
    check(!dsu.constrain(0,2)); check(!dsu.constrain(0,0))
    dsu.rollback(before); check(dsu.isConsistent)
    dsu.rollback(start); check(dsu.parityBetween(0,2) == null)
    repeat(250) {
        val n = rng.nextInt(1,12)
        val solver = OfflineDynamicBipartiteness(n)
        val edges = ArrayList<Pair<Int,Int>>()
        val expected = ArrayList<Boolean>()
        fun brute(): Boolean {
            val colors = IntArray(n) { -1 }
            for (root in 0 until n) if (colors[root] < 0) {
                val queue = ArrayDeque<Int>(); queue.add(root); colors[root] = 0
                while (queue.isNotEmpty()) {
                    val u = queue.removeFirst()
                    for ((a,b) in edges) {
                        val v = if (a == u) b else if (b == u) a else continue
                        if (colors[v] == colors[u]) return false
                        if (colors[v] < 0) { colors[v] = colors[u] xor 1; queue.add(v) }
                    }
                }
            }
            return true
        }
        repeat(100) {
            when (rng.nextInt(3)) {
                0 -> { val u = rng.nextInt(n); val v = rng.nextInt(n); solver.add(u,v); edges.add(u to v) }
                1 -> if (edges.isNotEmpty()) { val (u,v) = edges.removeAt(rng.nextInt(edges.size)); solver.remove(v,u) }
                else -> { solver.query(); expected.add(brute()) }
            }
        }
        check(solver.solve() == expected)
    }
    println("PASS: suffix array and parity rollback oracles")
}
