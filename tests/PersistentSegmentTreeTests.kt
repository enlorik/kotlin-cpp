import kotlin.random.Random
fun main() {
    val random = Random(69125)
    repeat(1800) {
        val n = random.nextInt(0, 50)
        val a = LongArray(n) { random.nextLong(-40, 41) }
        val tree = a.persistentSegmentTree()
        val versions = ArrayList<LongArray>()
        versions.add(a.copyOf())
        repeat(80) {
            val version = random.nextInt(versions.size)
            val original = versions[version]
            val l = random.nextInt(n + 1)
            val r = random.nextInt(l, n + 1)
            check(tree.query(version, l, r) == original.sliceArray(l until r).sum())
            check(tree.all(version) == original.sum())
            if (n > 0) {
                val index = random.nextInt(n)
                val updated = original.copyOf()
                val id = if (random.nextBoolean()) {
                    val value = random.nextLong(-40, 41)
                    updated[index] = value
                    tree.set(version, index, value)
                } else {
                    val delta = random.nextLong(-40, 41)
                    updated[index] += delta
                    tree.add(version, index, delta)
                }
                versions.add(updated)
                check(id == versions.lastIndex)
                check(tree.get(id, index) == updated[index])
            }
        }
        check(tree.versionCount == versions.size)
        for (i in versions.indices) check(tree.all(i) == versions[i].sum())
    }
    val empty = persistentSegmentTree(0)
    check(empty.versionCount == 1 && empty.query(0, 0, 0) == 0L)
    val x = longArrayOf(1, 2, 3).persistentSegmentTree()
    val v1 = x.set(0, 1, 100)
    val v2 = x.add(0, 2, 5)
    check(x.query(0, 0, 3) == 6L)
    check(x.query(v1, 0, 3) == 104L)
    check(x.query(v2, 0, 3) == 11L)
    println("PASS: 144000 randomized persistent tree operations across branched histories")
}
