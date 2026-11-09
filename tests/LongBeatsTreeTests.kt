import kotlin.random.Random

fun main() {
    val empty = longArrayOf().beatsTree()
    check(empty.sum(0, 0) == 0L && empty.min(0, 0) == Long.MAX_VALUE)
    check(empty.max(0, 0) == Long.MIN_VALUE)
    empty.add(0, 0, 10)
    empty.chmin(0, 0, 0)
    empty.chmax(0, 0, 0)
    empty.assign(0, 0, 123)
    check(runCatching { empty.sum(0, 1) }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { longArrayOf(Long.MAX_VALUE).beatsTree() }
        .exceptionOrNull() is IllegalArgumentException)

    val single = longArrayOf(3).beatsTree()
    single.chmin(0, 1, 2)
    single.chmax(0, 1, 6)
    single.add(0, 1, -4)
    check(single.sum(0, 1) == 2L)

    val rnd = Random(20261109)
    var checks = 0
    repeat(220) { round ->
        val n = rnd.nextInt(0, 70)
        val src = LongArray(n) { rnd.nextLong(-50, 51) }
        val tree = src.beatsTree()
        val naive = src.copyOf()
        repeat(410) {
            val l = rnd.nextInt(n + 1)
            val r = rnd.nextInt(l, n + 1)
            val value = rnd.nextLong(-100, 101)
            when (rnd.nextInt(9)) {
                0, 1 -> {
                    tree.chmin(l, r, value)
                    for (j in l until r) naive[j] = minOf(naive[j], value)
                }
                2, 3 -> {
                    tree.chmax(l, r, value)
                    for (j in l until r) naive[j] = maxOf(naive[j], value)
                }
                4 -> {
                    tree.add(l, r, value)
                    for (j in l until r) naive[j] += value
                }
                5 -> {
                    tree.assign(l, r, value)
                    naive.fill(value, l, r)
                }
                else -> {
                    check(tree.sum(l, r) == (l until r).sumOf { naive[it] }) {
                        "Sum mismatch round=$round [$l,$r)"
                    }
                    check(tree.min(l, r) ==
                        ((l until r).minOfOrNull { naive[it] } ?: Long.MAX_VALUE))
                    check(tree.max(l, r) ==
                        ((l until r).maxOfOrNull { naive[it] } ?: Long.MIN_VALUE))
                    checks++
                }
            }
            check(tree.allSum() == naive.sum()) { "Root mismatch round=$round" }
        }
    }
    println("PASS: Segment Tree Beats randomized chmin/chmax/add/assign ($checks queries)")
}
