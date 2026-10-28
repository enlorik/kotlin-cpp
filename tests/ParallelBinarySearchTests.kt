import kotlin.random.Random

fun main() {
    val targets = intArrayOf(3, 5, 8, 13, 100)
    val events = intArrayOf(2, 1, 4, 3, 5)
    var applied = 0
    var running = 0
    var previousMid = Int.MIN_VALUE
    val answers = parallelFirstTrue(targets.size, 0, events.size + 1,
        onRoundStart = { applied = 0; running = 0; previousMid = Int.MIN_VALUE },
        evaluate = { mid, ids ->
            check(mid >= previousMid)
            previousMid = mid
            while (applied < mid) running += events[applied++]
            BooleanArray(ids.size) { j -> running >= targets[ids[j]] }
        }
    )
    check(answers.contentEquals(intArrayOf(2, 3, 4, 5, 6)))
    check(parallelFirstTrue(0, 0, 10) { _, _ -> error("no queries") }.isEmpty())
    check(parallelFirstTrue(3, 0, 0) { _, _ -> error("empty range") }
        .contentEquals(intArrayOf(0, 0, 0)))
    check(parallelFirstTrue(2, Int.MIN_VALUE, Int.MAX_VALUE) { mid, ids ->
        BooleanArray(ids.size) { j -> if (ids[j] == 0) mid >= 0 else false }
    }.contentEquals(intArrayOf(0, Int.MAX_VALUE)))

    var rejected = false
    try {
        parallelFirstTrue(intArrayOf(2), intArrayOf(1)) { _, _ -> booleanArrayOf(true) }
    } catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)
    rejected = false
    try {
        parallelFirstTrue(2, 0, 4) { _, _ -> booleanArrayOf(true) }
    } catch (_: IllegalArgumentException) { rejected = true }
    check(rejected)

    val random = Random(71829)
    repeat(1500) {
        val count = random.nextInt(0, 80)
        val limits = IntArray(count) { random.nextInt(-200, 201) }
        val left = IntArray(count) { random.nextInt(-300, 301) }
        val right = IntArray(count) { random.nextInt(left[it], 302) }
        val originalLeft = left.copyOf()
        val originalRight = right.copyOf()
        val result = parallelFirstTrue(left, right) { mid, ids ->
            BooleanArray(ids.size) { j -> mid >= limits[ids[j]] }
        }
        for (q in 0 until count) {
            check(result[q] == limits[q].coerceIn(left[q], right[q]))
        }
        check(left.contentEquals(originalLeft) && right.contentEquals(originalRight))
    }
    println("PASS: parallel binary search 1500 randomized batches, full ranges and sweeps")
}
