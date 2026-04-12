// tests/Tests.kt — Basic correctness tests for the CP library
// Run with: kotlinc -cp . tests/Tests.kt && kotlin TestsKt

// ---- Utilities ----
private var passed = 0
private var failed = 0

private fun check(name: String, got: Any?, expected: Any?) {
    if (got == expected) {
        println("PASS  $name")
        passed++
    } else {
        println("FAIL  $name  got=$got  expected=$expected")
        failed++
    }
}

private fun checkTrue(name: String, cond: Boolean) = check(name, cond, true)

// ---- The library source is assumed to be compiled together with this file ----

fun main() {
    testGcd()
    testBinpow()
    testSieve()
    testLIS()
    testBFS()
    testDSU()
    testSegTree()
    testFenwick()
    testSparseTable()
    testBellmanFord()
    testKruskal()
    testKMP()
    testZFunction()
    testEditDistance()
    testConvexHull()
    testBinarySearch()
    println("\n$passed passed, $failed failed")
    if (failed > 0) throw AssertionError("$failed test(s) failed")
}

// ---- gcd / lcm ----
fun testGcd() {
    check("gcd(12,8)", gcd(12L, 8L), 4L)
    check("gcd(0,5)", gcd(0L, 5L), 5L)
    check("lcm(4,6)", lcm(4L, 6L), 12L)
}

// ---- binpow ----
fun testBinpow() {
    check("binpow(2,10,1e9+7)", binpow(2L, 10L, 1_000_000_007L), 1024L)
    check("binpow(3,0,MOD)", binpow(3L, 0L, 1_000_000_007L), 1L)
}

// ---- prime sieve ----
fun testSieve() {
    val s = sieve(20)
    check("2 is prime", s[2], true)
    check("4 not prime", s[4], false)
    check("19 is prime", s[19], true)
}

// ---- LIS ----
fun testLIS() {
    check("lis([3,1,2,1,8,5,6])", lis(intArrayOf(3, 1, 2, 1, 8, 5, 6)), 4)
    check("lis empty", lis(intArrayOf()), 0)
    check("lis single", lis(intArrayOf(5)), 1)
}

// ---- BFS ----
fun testBFS() {
    // Simple path: 0-1-2-3
    val g = Array(4) { intArrayOf() }
    g[0] = intArrayOf(1); g[1] = intArrayOf(0, 2); g[2] = intArrayOf(1, 3); g[3] = intArrayOf(2)
    val dist = bfs(g, 0)
    check("bfs dist[3]", dist[3], 3)
    check("bfs dist[0]", dist[0], 0)
}

// ---- DSU ----
fun testDSU() {
    val d = DSU(5)
    d.union(0, 1); d.union(1, 2)
    checkTrue("dsu same(0,2)", d.same(0, 2))
    checkTrue("dsu diff(0,3)", !d.same(0, 3))
    check("dsu components", d.components, 3)
}

// ---- Segment Tree ----
fun testSegTree() {
    val t = st(5, 0L) { a, b -> a + b }
    for (i in 0 until 5) t.update(i, (i + 1).toLong())
    check("st sum[0,5)", t.query(0, 5), 15L)
    check("st sum[1,3)", t.query(1, 3), 5L)
    t.update(2, 10L)
    check("st after update", t.query(0, 5), 22L)
}

// ---- Fenwick Tree ----
fun testFenwick() {
    val fw = fw(5)
    for (i in 1..5) fw.update(i, i.toLong())
    check("fw prefix(5)", fw.prefix(5), 15L)
    check("fw query(2,4)", fw.query(2, 4), 9L)
}

// ---- Sparse Table ----
fun testSparseTable() {
    val arr = longArrayOf(3, 1, 4, 1, 5, 9, 2, 6)
    val spt = spt(arr, Long.MAX_VALUE) { a, b -> minOf(a, b) }
    check("spt min[0,7]", spt.query(0, 7), 1L)
    check("spt min[4,7]", spt.query(4, 7), 2L)
}

// ---- Bellman-Ford ----
fun testBellmanFord() {
    val edges = listOf(
        Triple(0, 1, 1L), Triple(1, 2, 2L), Triple(0, 2, 10L)
    )
    val dist = bellmanFord(3, edges, 0)
    check("bf dist[2]", dist?.get(2), 3L)
}

// ---- Kruskal ----
fun testKruskal() {
    val edges = listOf(
        Triple(0, 1, 1L), Triple(1, 2, 2L), Triple(0, 2, 10L)
    )
    val mst = kruskal(3, edges)
    val totalW = mst?.sumOf { it.third } ?: -1L
    check("kruskal weight", totalW, 3L)
}

// ---- KMP ----
fun testKMP() {
    val occ = kmpMatch("ababab", "ab")
    check("kmp count", occ.size, 3)
    check("kmp first", occ[0], 0)
}

// ---- Z-function ----
fun testZFunction() {
    val occ = zMatch("ababab", "ab")
    check("z count", occ.size, 3)
}

// ---- Edit distance ----
fun testEditDistance() {
    check("ed(kitten,sitting)", editDistance("kitten", "sitting"), 3)
    check("ed(,abc)", editDistance("", "abc"), 3)
    check("lcs(abcde,ace)", lcs("abcde", "ace"), 3)
}

// ---- Convex Hull ----
fun testConvexHull() {
    val pts = listOf(
        Point(0, 0), Point(1, 1), Point(2, 0), Point(1, -1),
        Point(0, 0) // duplicate
    )
    val hull = convexHull(pts)
    check("hull size", hull.size, 4)
}

// ---- Binary search helpers ----
fun testBinarySearch() {
    val a = longArrayOf(1, 3, 5, 7, 9)
    check("lb(5)", lowerBound(a, 5L), 2)
    check("ub(5)", upperBound(a, 5L), 3)
    check("bs answer", bsLong(0L, 100L) { it * it <= 49L }, 7L)
}
