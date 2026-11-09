import kotlin.random.Random

private fun expectedSum(a: LongArray, lo: Int, hi: Int): Long =
    (lo until hi).sumOf { a[it] }

fun main() {
    val initial = longArrayOf(1, 2, 3, 4)
    val tree = initial.versionedLongSum()
    initial[0] = 100
    check(tree.all() == 10L)
    val a = tree.add(0, 4, 10)
    check(a == 1 && tree.currentVersion == 1 && tree.query(0, 4) == 50L)
    val b = tree.assign(0, 2, -5)
    check(b == 2 && tree.query(0, 4) == 17L)
    tree.checkout(0)
    check(tree.all() == 10L && tree.versionCount == 3)
    val c = tree.add(0, 1, 100)
    check(c == 3 && tree.all() == 110L)
    check(tree.query(a, 0, 4) == 50L)
    check(tree.query(b, 0, 4) == 17L)
    val branch = tree.assign(1, 1, 3, 2)
    check(branch == 4 && tree.currentVersion == 3)
    check(tree.query(branch, 0, 4) == 29L)
    tree.rollback(2)
    check(tree.query(0, 4) == 17L)

    val empty = longArrayOf().versionedLongSum()
    check(empty.versionCount == 1 && empty.query(0, 0) == 0L)
    val emptyVersion = empty.add(0, 0, 0, 99L)
    check(emptyVersion == 1 && empty.versionCount == 2)
    check(runCatching { empty.query(2, 0, 0) }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { empty.set(0, 5L) }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { empty.checkout(-1) }.exceptionOrNull() is IllegalArgumentException)

    val rng = Random(6291383)
    var comparisons = 0
    repeat(170) { round ->
        val n = rng.nextInt(0, 57)
        val src = LongArray(n) { rng.nextLong(-100, 101) }
        val ds = src.versionedLongSum()
        val versions = arrayListOf(src.copyOf())
        var active = 0
        repeat(180) {
            val from = rng.nextInt(n + 1)
            val until = rng.nextInt(from, n + 1)
            when (rng.nextInt(7)) {
                0, 1 -> {
                    val base = rng.nextInt(versions.size)
                    val v = rng.nextLong(-99, 100)
                    val expected = versions[base].copyOf()
                    for (i in from until until) expected[i] += v
                    val id = ds.add(base, from, until, v)
                    check(id == versions.size)
                    versions.add(expected)
                    check(ds.currentVersion == active)
                }
                2, 3 -> {
                    val base = rng.nextInt(versions.size)
                    val v = rng.nextLong(-99, 100)
                    val expected = versions[base].copyOf()
                    expected.fill(v, from, until)
                    val id = ds.assign(base, from, until, v)
                    check(id == versions.size)
                    versions.add(expected)
                    check(ds.currentVersion == active)
                }
                4 -> {
                    active = rng.nextInt(versions.size)
                    ds.checkout(active)
                }
                5 -> {
                    val v = rng.nextLong(-99, 100)
                    val expected = versions[active].copyOf()
                    for (i in from until until) expected[i] += v
                    active = ds.add(from, until, v)
                    check(active == versions.size)
                    versions.add(expected)
                }
                else -> {
                    val version = rng.nextInt(versions.size)
                    check(ds.query(version, from, until) ==
                        expectedSum(versions[version], from, until)) {
                        "History mismatch round=$round ver=$version [$from,$until)"
                    }
                    comparisons++
                }
            }
            val version = rng.nextInt(versions.size)
            check(ds.all(version) == versions[version].sum())
            check(ds.all() == versions[active].sum())
            check(ds.versionCount == versions.size)
        }
    }
    println("PASS: persistent branch/rollback and lazy Long range updates ($comparisons oracle queries)")
}
