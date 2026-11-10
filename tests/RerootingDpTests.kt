import kotlin.random.Random
import java.util.ArrayDeque

fun main() {
    val rng = Random(102908)
    check(treeDistanceSums(1, emptyList()).contentEquals(longArrayOf(0)))
    check(treeDistanceSums(4, listOf(0 to 1, 1 to 2, 2 to 3))
        .contentEquals(longArrayOf(6, 4, 4, 6)))
    check(runCatching { treeDistanceSums(0, emptyList()) }.isFailure)
    check(runCatching { treeDistanceSums(3, listOf(0 to 1, 0 to 1)) }.isFailure)

    repeat(140) {
        val n = rng.nextInt(1, 38)
        val edges = (1 until n).map { i -> i to rng.nextInt(i) }
        val adj = Array(n) { ArrayList<Int>() }
        for ((a,b) in edges) { adj[a].add(b); adj[b].add(a) }

        val result = treeDistanceSums(n, edges)
        for (root in 0 until n) {
            val dist = IntArray(n) { -1 }
            dist[root] = 0
            val q: ArrayDeque<Int> = ArrayDeque()
            q.add(root)
            while (!q.isEmpty()) {
                val v = q.removeFirst()
                for (u in adj[v]) if (dist[u] == -1) {
                    dist[u] = dist[v] + 1
                    q.add(u)
                }
            }
            check(result[root] == dist.sum().toLong())
        }

        // A noncommutative monoid: neighbour order must be preserved.
        val str = monoid("") { a: String, b: String -> a + b }
        val output = rerootingDp(n, edges, str,
            finish = { v, aggregate -> "[$v$aggregate]" },
            lift = { value, _, _ -> value })
        fun brute(v: Int, parent: Int): String {
            var children = ""
            for (u in adj[v]) if (u != parent) children += brute(u, v)
            return "[$v$children]"
        }
        for (v in 0 until n) check(output[v] == brute(v, -1))
    }

    val n = 100_000
    val chain = (1 until n).map { (it - 1) to it }
    val ans = treeDistanceSums(n, chain)
    val extremity = n.toLong() * (n - 1) / 2
    check(ans[0] == extremity && ans[n - 1] == extremity)
    check(ans[1] == extremity - (n - 2).toLong())
    println("PASS: generic ordered rerooting and 100000-node chain")
}
