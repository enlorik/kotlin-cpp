import kotlin.random.Random
fun main() {
    val random = Random(9923)
    repeat(3000) {
        val n = random.nextInt(0, 70)
        val original = LongArray(n) { random.nextLong(-30, 31) }
        val seg = original.lazySegmentTree()
        val naive = original.copyOf()
        repeat(50) {
            val l = random.nextInt(n + 1)
            val r = random.nextInt(l, n + 1)
            when (random.nextInt(3)) {
                0 -> {
                    val x = random.nextLong(-20, 21)
                    seg.add(l, r, x)
                    for (j in l until r) naive[j] += x
                }
                1 -> {
                    val x = random.nextLong(-20, 21)
                    seg.set(l, r, x)
                    for (j in l until r) naive[j] = x
                }
                else -> check(seg.query(l, r) == naive.sliceArray(l until r).sum())
            }
            check(seg.all() == naive.sum())
        }
        check(seg.query(0, n) == naive.sum())
    }
    val t = longArrayOf(1, 2, 3, 4).lazySegmentTree()
    t.add(0, 4, 2)
    t.set(1, 3, 10)
    t.add(2, 4, -1)
    check(t.query(0, 4) == 3L + 10 + 9 + 5)
    val empty = lazySegmentTree(0)
    empty.add(0, 0, 50)
    empty.set(0, 0, 10)
    check(empty.query(0, 0) == 0L)
    println("PASS LazySegmentTree: 150000 randomized operations")
}
