import kotlin.random.Random
import java.util.ArrayDeque

private fun componentsAndSizes(n: Int, edges: Map<Pair<Int,Int>,Int>): Pair<Int,IntArray> {
    val adj = Array(n) { ArrayList<Int>() }
    for ((e, count) in edges) if (count > 0) {
        adj[e.first].add(e.second)
        adj[e.second].add(e.first)
    }
    val sizes = IntArray(n)
    var groups = 0
    val visited = BooleanArray(n)
    for (start in 0 until n) {
        if (visited[start]) continue
        groups++
        val nodes = ArrayList<Int>()
        val q: ArrayDeque<Int> = ArrayDeque()
        q.addLast(start)
        visited[start] = true
        while (!q.isEmpty()) {
            val v = q.removeFirst()
            nodes.add(v)
            for (u in adj[v]) if (!visited[u]) {
                visited[u] = true
                q.addLast(u)
            }
        }
        for (v in nodes) sizes[v] = nodes.size
    }
    return groups to sizes
}

fun main() {
    val dsu = RollbackDsu(5)
    val original = dsu.snapshot()
    check(dsu.union(0, 1) && dsu.componentSize(0) == 2)
    val one = dsu.snapshot()
    check(dsu.union(1, 2))
    check(!dsu.union(0, 2))
    check(dsu.components == 3)
    dsu.rollback(one)
    check(dsu.components == 4 && !dsu.connected(0, 2))
    dsu.rollback(original)
    check(dsu.components == 5)
    check(runCatching { dsu.rollback(2) }.isFailure)
    check(offlineConnectivity(0) { componentCount() } ==
        listOf(OfflineConnectivityAnswer.ComponentCount(0)))
    val sample = offlineConnectivity(3) {
        add(0,1); connected(0,2)
        add(1,2); connected(0,2)
        add(1,2); remove(1,2); componentCount()
        remove(1,2); connected(0,2); componentSize(0)
    }
    check(sample == listOf(
        OfflineConnectivityAnswer.Connected(false),
        OfflineConnectivityAnswer.Connected(true),
        OfflineConnectivityAnswer.ComponentCount(1),
        OfflineConnectivityAnswer.Connected(false),
        OfflineConnectivityAnswer.Size(2)
    ))
    check(runCatching { offlineConnectivity(2) { remove(0,1) } }.isFailure)

    val rng = Random(272719)
    var queries = 0
    repeat(220) {
        val n = rng.nextInt(1, 18)
        val builder = OfflineDynamicConnectivity(n)
        val active = HashMap<Pair<Int,Int>,Int>()
        val expected = ArrayList<OfflineConnectivityAnswer>()
        repeat(210) {
            val u = rng.nextInt(n)
            val v = rng.nextInt(n)
            val key = minOf(u,v) to maxOf(u,v)
            when (rng.nextInt(7)) {
                0, 1 -> {
                    builder.add(u,v)
                    active[key] = (active[key] ?: 0) + 1
                }
                2 -> {
                    if (active.isNotEmpty()) {
                        val k = active.keys.random(rng)
                        builder.remove(k.first,k.second)
                        val remaining = active[k]!! - 1
                        if (remaining == 0) active.remove(k)
                        else active[k] = remaining
                    }
                }
                3, 4 -> {
                    builder.connected(u,v)
                    val (_, sizes) = componentsAndSizes(n, active)
                    // Compare connectivity by flood fill from u on currently active edges.
                    val visited = BooleanArray(n)
                    val q = ArrayDeque<Int>()
                    visited[u] = true
                    q.addLast(u)
                    while (!q.isEmpty()) {
                        val x = q.removeFirst()
                        for ((edge, count) in active) if (count > 0) {
                            val next = when(x) {
                                edge.first -> edge.second
                                edge.second -> edge.first
                                else -> -1
                            }
                            if (next >= 0 && !visited[next]) {
                                visited[next] = true
                                q.addLast(next)
                            }
                        }
                    }
                    check(sizes[u] >= 1)
                    expected.add(OfflineConnectivityAnswer.Connected(visited[v]))
                    queries++
                }
                5 -> {
                    builder.componentCount()
                    expected.add(OfflineConnectivityAnswer.ComponentCount(
                        componentsAndSizes(n,active).first))
                    queries++
                }
                else -> {
                    builder.componentSize(u)
                    expected.add(OfflineConnectivityAnswer.Size(
                        componentsAndSizes(n,active).second[u]))
                    queries++
                }
            }
        }
        check(builder.solve() == expected)
    }
    println("PASS: rollback DSU and dynamic multigraph connectivity ($queries queries)")
}
