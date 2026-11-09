import kotlin.random.Random

fun main() {
    val empty = intArrayOf().blockAssignSum(3)
    empty.recompute(0, 0) { _, value -> value }
    empty.setMany(intArrayOf(), intArrayOf())
    check(empty.sum(0, 0) == 0L)

    val rnd = Random(20261109)
    var verified = 0
    repeat(180) { round ->
        val n = rnd.nextInt(0, 160)
        val initial = IntArray(n) { rnd.nextInt(-500, 501) }
        val naive = initial.copyOf()
        val blocks = initial.blockAssignSum(1 + round % 21)
        repeat(300) {
            val from = rnd.nextInt(n + 1)
            val until = rnd.nextInt(from, n + 1)
            when (rnd.nextInt(5)) {
                0 -> {
                    val value = rnd.nextInt(-300, 301)
                    blocks.assign(from, until, value)
                    naive.fill(value, from, until)
                }
                1 -> {
                    val increment = rnd.nextInt(-30, 31)
                    blocks.recompute(from, until) { i, old ->
                        check(old == naive[i])
                        old + increment
                    }
                    for (i in from until until) naive[i] += increment
                }
                2 -> {
                    val indexes = (0 until n).filter { rnd.nextInt(7) == 0 }.toIntArray()
                    val values = IntArray(indexes.size) { rnd.nextInt(-200, 201) }
                    blocks.setMany(indexes, values)
                    for (i in indexes.indices) naive[indexes[i]] = values[i]
                }
                3 -> {
                    val replacement = IntArray(until - from) { rnd.nextInt(-200, 201) }
                    blocks.replace(from, replacement)
                    for (i in replacement.indices) naive[from + i] = replacement[i]
                }
                else -> {
                    check(blocks.sum(from, until) == (from until until).sumOf { naive[it].toLong() })
                    verified++
                }
            }
            check(blocks.sum(0, n) == naive.sumOf { it.toLong() }) {
                "Mismatch batch round=$round"
            }
        }
    }

    val b = intArrayOf(1, 2, 3, 4).blockAssignSum(2)
    check(runCatching { b.setMany(intArrayOf(2, 1), intArrayOf(9, 9)) }
        .exceptionOrNull() is IllegalArgumentException)
    check(runCatching { b.setMany(intArrayOf(1, 1), intArrayOf(9, 9)) }
        .exceptionOrNull() is IllegalArgumentException)
    check(runCatching { b.setMany(intArrayOf(0, 4), intArrayOf(9, 9)) }
        .exceptionOrNull() is IllegalArgumentException)
    check(runCatching { b.setMany(intArrayOf(1), intArrayOf(9, 8)) }
        .exceptionOrNull() is IllegalArgumentException)
    check(b.sum(0, 4) == 10L) // rejected batches never partially mutate
    println("PASS: batched block recompute and setMany ($verified oracle queries)")
}
