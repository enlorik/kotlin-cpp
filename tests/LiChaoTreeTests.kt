import kotlin.random.Random

private data class BruteLine(
    val line: LiChaoLine,
    val from: Long,
    val until: Long
)

private fun bruteEnvelope(
    lines: List<BruteLine>,
    x: Long,
    objective: LiChaoObjective
): Long? {
    val active = lines.filter { x >= it.from && x <= it.until }
        .map { it.line.at(x) }
    return if (objective == LiChaoObjective.MIN) active.minOrNull()
        else active.maxOrNull()
}

fun main() {
    val empty = LiChaoTree(-10, 10)
    check(empty.query(0) == null && empty.lineCount == 0)
    check(runCatching { empty.query(11) }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { LiChaoTree(2, 1) }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { empty.addSegment(-11, 0, 1, 2) }
        .exceptionOrNull() is IllegalArgumentException)

    val extremes = LiChaoTree(Long.MIN_VALUE, Long.MAX_VALUE)
    extremes.addLine(0, 8)
    extremes.addSegment(Long.MAX_VALUE - 1, Long.MAX_VALUE, 0, -3)
    check(extremes.query(Long.MIN_VALUE) == 8L)
    check(extremes.query(Long.MAX_VALUE) == -3L)
    check(extremes.query(-1L) == 8L)

    val equal = (-3L..3L).liChao()
    equal.addLine(2, 7)
    equal.addLine(2, 1)
    equal.addLine(2, 9)
    check(equal.query(3) == 7L)
    check(equal.query(-3) == -5L)
    val only = LiChaoTree(5, 5, LiChaoObjective.MAX)
    only.addLine(-2, 4)
    only.addLine(1, 1)
    check(only.query(5) == 6L)

    val rng = Random(817255)
    var checks = 0
    for (objective in LiChaoObjective.entries) repeat(150) {
        val from = rng.nextInt(-60, 0).toLong()
        val until = rng.nextInt(0, 60).toLong()
        val tree = LiChaoTree(from, until, objective)
        val lines = ArrayList<BruteLine>()
        repeat(160) {
            val slope = rng.nextLong(-60, 61)
            val intercept = rng.nextLong(-100, 101)
            if (rng.nextBoolean()) {
                tree.addLine(slope, intercept)
                lines.add(BruteLine(LiChaoLine(slope, intercept), from, until))
            } else {
                val l = rng.nextLong(from, until + 1)
                val r = rng.nextLong(l, until + 1)
                tree.addSegment(l, r, slope, intercept)
                lines.add(BruteLine(LiChaoLine(slope, intercept), l, r))
            }
            repeat(5) {
                val x = rng.nextLong(from, until + 1)
                val expected = bruteEnvelope(lines, x, objective)
                val actual = tree.query(x)
                check(actual == expected) {
                    "Li Chao $objective x=$x expected=$expected actual=$actual"
                }
                checks++
            }
        }
        check(tree.lineCount == 160)
    }
    println("PASS: Li Chao min/max and interval lines ($checks randomized oracle queries)")
}
