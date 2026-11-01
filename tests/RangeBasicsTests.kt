import kotlin.random.Random

fun main() {
    val rnd = Random(99)
    repeat(3000) {
        val n = rnd.nextInt(0, 80)
        val orig = LongArray(n) { rnd.nextLong(-50, 51) }
        val fixed = orig.copyOf()
        val pref = orig.prefixSums()
        val diff = orig.differenceArray()
        val fenwick = orig.fenwickTree()
        val naive = orig.copyOf()
        repeat(80) {
            val l = rnd.nextInt(n + 1)
            val r = rnd.nextInt(l, n + 1)
            check(pref.query(l, r) == fixed.sliceArray(l until r).sum())
            if (n > 0 && rnd.nextBoolean()) {
                val i = rnd.nextInt(n)
                val delta = rnd.nextLong(-50, 51)
                fenwick.add(i, delta)
                naive[i] += delta
            }
            check(fenwick.query(l, r) == naive.sliceArray(l until r).sum())
            val delta = rnd.nextLong(-10, 11)
            diff.add(l, r, delta)
            for (i in l until r) orig[i] += delta
            check(diff.values().contentEquals(orig))
        }
    }
    val ft = longArrayOf(2, 0, 3, 1, 4).fenwickTree()
    check(ft.findPrefix(6) == 3)
    check(ft.findPrefix(1) == 0)
    check(ft.findPrefix(11) == null)
    check(ft.findPrefix(0) == 0)
    ft.set(1, 3)
    check(ft.findPrefix(6) == 2)
    check(longArrayOf().fenwickTree().findPrefix(0) == null)
    println("PASS RangeBasics: 240000 randomized iterations, Fenwick prefix search")
}
