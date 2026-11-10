import kotlin.random.Random
import java.util.ArrayDeque

private fun pathVertices(adj: Array<ArrayList<Int>>, src: Int, dst: Int): List<Int> {
    val prev = IntArray(adj.size) { -1 }
    prev[src] = src
    val q: ArrayDeque<Int> = ArrayDeque()
    q.addLast(src)
    while (q.isNotEmpty() && prev[dst] == -1) {
        val v = q.removeFirst()
        for (w in adj[v]) if (prev[w] == -1) {
            prev[w] = v
            q.addLast(w)
        }
    }
    val route = ArrayList<Int>()
    var cur = dst
    while (cur != src) {
        route.add(cur)
        cur = prev[cur]
    }
    route.add(src)
    route.reverse()
    return route
}

private fun rootedParents(adj: Array<ArrayList<Int>>, root: Int): IntArray {
    val parent = IntArray(adj.size) { -1 }
    parent[root] = root
    val queue = ArrayDeque<Int>()
    queue.addLast(root)
    while (queue.isNotEmpty()) {
        val v = queue.removeFirst()
        for (w in adj[v]) if (parent[w] == -1) {
            parent[w] = v
            queue.addLast(w)
        }
    }
    parent[root] = -1
    return parent
}

private fun isDescendant(v: Int, ancestor: Int, parent: IntArray): Boolean {
    var current = v
    while (current != -1) {
        if (current == ancestor) return true
        current = parent[current]
    }
    return false
}

fun main() {
    val letters = listOf("A", "bc", "D")
    val concat = monoid("") { a: String, b: String -> a + b }
    val chain = listOf(0 to 1, 1 to 2)
    val ordered = letters.heavyLightTree(chain, concat)
    check(ordered.pathQuery(0, 2) == "AbcD")
    check(ordered.pathQuery(2, 0) == "DbcA")
    ordered.set(1, "xyz")
    check(ordered.pathQuery(2, 0) == "DxyzA")
    check(ordered.subtreeQuery(1) == "xyzD")
    check(ordered.lca(0, 2) == 0)
    check(ordered.distance(0, 2) == 2)

    val singleton = longArrayOf(7).heavyLightSums(emptyList())
    check(singleton.pathSum(0, 0) == 7L)
    check(singleton.subtreeSum(0) == 7L)
    check(runCatching { longArrayOf().heavyLightSums(emptyList()) }
        .exceptionOrNull() is IllegalArgumentException)
    check(runCatching {
        longArrayOf(1, 2, 3).heavyLightSums(listOf(0 to 1, 0 to 1))
    }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching {
        longArrayOf(1, 2).heavyLightSums(listOf(0 to 0))
    }.exceptionOrNull() is IllegalArgumentException)

    val rng = Random(20261110)
    var checks = 0
    repeat(130) { round ->
        val n = rng.nextInt(1, 90)
        val root = rng.nextInt(n)
        val edges = (1 until n).map { vertex -> vertex to rng.nextInt(vertex) }
        val adj = Array(n) { ArrayList<Int>() }
        for ((a, b) in edges) {
            adj[a].add(b)
            adj[b].add(a)
        }
        val parent = rootedParents(adj, root)
        val values = LongArray(n) { rng.nextLong(-100, 101) }
        val words = MutableList(n) { ('a'.code + rng.nextInt(26)).toChar().toString() }
        val generic = words.heavyLightTree(edges, concat, root)
        val fast = values.heavyLightSums(edges, root)
        val topo = fast.topology
        check(topo.root == root)
        check(topo.position.toSet().size == n)
        repeat(230) {
            val u = rng.nextInt(n)
            val v = rng.nextInt(n)
            if (rng.nextInt(5) == 0) {
                val newValue = rng.nextLong(-100, 101)
                val newWord = ('A'.code + rng.nextInt(26)).toChar().toString()
                values[u] = newValue
                words[u] = newWord
                fast.set(u, newValue)
                generic.set(u, newWord)
            } else {
                val route = pathVertices(adj, u, v)
                val expectedSum = route.sumOf { values[it] }
                val expectedWord = route.joinToString("") { words[it] }
                check(fast.pathSum(u, v) == expectedSum) {
                    "HLD sum mismatch round=$round from=$u to=$v"
                }
                check(generic.pathQuery(u, v) == expectedWord) {
                    "HLD order mismatch round=$round from=$u to=$v"
                }
                check(topo.distance(u, v) == route.size - 1)
                val lca = route.minBy { topo.depth[it] }
                check(topo.lca(u, v) == lca)

                val members = (0 until n)
                    .filter { isDescendant(it, u, parent) }
                    .sortedBy { topo.position[it] }
                val expectedSubtreeSum = members.sumOf { values[it] }
                val expectedSubtreeWord = members.joinToString("") { words[it] }
                check(topo.subtreeSize[u] == members.size)
                check(fast.subtreeSum(u) == expectedSubtreeSum)
                check(generic.subtreeQuery(u) == expectedSubtreeWord)
                check(members.map { topo.position[it] } ==
                    (topo.position[u] until topo.position[u] + members.size).toList())
                check(topo.isAncestor(u, v) == isDescendant(v, u, parent))
                checks++
            }
        }
    }

    // Explicitly stress nonrecursive topology on a chain that would overflow
    // a recursive Kotlin DFS.
    val n = 100_000
    val bigEdges = (1 until n).map { it - 1 to it }
    val big = LongArray(n) { 1L }.heavyLightSums(bigEdges)
    check(big.pathSum(0, n - 1) == n.toLong())
    check(big.subtreeSum(n / 2) == (n - n / 2).toLong())
    big.set(n - 1, 19L)
    check(big.pathSum(0, n - 1) == n + 18L)

    println("PASS: generic noncommutative and primitive Heavy-Light ($checks oracle paths)")
}
