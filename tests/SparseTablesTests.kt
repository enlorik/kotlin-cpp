import kotlin.random.Random
fun main() {
    val rnd = Random(2233)
    repeat(2200) {
        val n = rnd.nextInt(0, 100)
        val a = IntArray(n) { rnd.nextInt(-20, 21) }
        val min = a.sparseTable(IntAggregations.MIN)
        val max = a.sparseTable(IntAggregations.MAX)
        val sum = a.disjointSparseTable(IntAggregations.SUM)
        val xor = a.disjointSparseTable(IntAggregations.XOR)
        val txt = a.map { "[$it]" }
        val textOp = AggregateOp("", { x: String, y: String -> x + y })
        val concat = txt.disjointSparseTable(textOp)
        repeat(30) {
            val l = rnd.nextInt(n + 1)
            val r = rnd.nextInt(l, n + 1)
            val values = a.sliceArray(l until r)
            check(min.query(l, r) == (values.minOrNull() ?: Int.MAX_VALUE))
            check(max.query(l, r) == (values.maxOrNull() ?: Int.MIN_VALUE))
            check(sum.query(l, r) == values.sum())
            check(xor.query(l, r) == values.fold(0) { x, y -> x xor y })
            check(concat.query(l, r) == txt.subList(l, r).joinToString(""))
        }
    }
    var failed = false
    try { intArrayOf(1, 2).sparseTable(IntAggregations.SUM) } catch (_: IllegalArgumentException) { failed = true }
    check(failed)
    val a = intArrayOf(4, 1, 3)
    check(a.disjointSparseTable(IntAggregations.SUM).query(0, 3) == 8)
    check(a.sparseTable(IntAggregations.MIN).query(0, 3) == 1)
    println("PASS: 66000 random sparse/disjoint range comparisons")
}
