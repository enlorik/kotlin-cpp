import kotlin.random.Random
fun main() {
    val random = Random(9132)
    repeat(2500) {
        val n = random.nextInt(0, 90)
        val a = IntArray(n) { random.nextInt(-10, 11) }
        val queries = List(random.nextInt(0, 90)) {
            val l = random.nextInt(n + 1)
            MoQuery(l, random.nextInt(l, n + 1))
        }
        val answer = a.moDistinctCounts(queries)
        check(answer.size == queries.size)
        for (i in queries.indices) {
            val q = queries[i]
            check(answer[i] == a.sliceArray(q.from until q.until).toSet().size)
        }
        var sum = 0L
        val sums = moQueries(n, queries, { sum += a[it] }, { sum -= a[it] }, { sum })
        for (i in queries.indices) {
            val q = queries[i]
            check(sums[i] == a.sliceArray(q.from until q.until).sum().toLong())
        }
    }
    check(intArrayOf().moDistinctCounts(listOf(MoQuery(0, 0))) == listOf(0))
    println("PASS: 2500 randomized Mo query batches")
}
