/**
 * Union-find with O(log n) finds and O(1) rollback per successful union.
 * No path compression: that would destroy rollback history.
 */
class RollbackDsu(val size: Int) {
    private val parent = IntArray(size) { it }
    private val sizes = IntArray(size) { 1 }
    private val changedChild = ArrayList<Int>()
    private val changedParent = ArrayList<Int>()
    var components: Int = size
        private set

    init { require(size >= 0) }
    private fun checkVertex(v: Int) {
        require(v in 0 until size) { "Vertex $v outside 0 until $size" }
    }

    fun find(v: Int): Int {
        checkVertex(v)
        var x = v
        while (parent[x] != x) x = parent[x]
        return x
    }

    fun connected(u: Int, v: Int): Boolean = find(u) == find(v)
    fun componentSize(v: Int): Int = sizes[find(v)]
    fun snapshot(): Int = changedChild.size

    fun union(u: Int, v: Int): Boolean {
        var a = find(u)
        var b = find(v)
        if (a == b) return false
        if (sizes[a] < sizes[b]) {
            val tmp = a
            a = b
            b = tmp
        }
        changedChild.add(b)
        changedParent.add(a)
        parent[b] = a
        sizes[a] += sizes[b]
        components--
        return true
    }

    /** Restore the exact connectivity state from a previous snapshot. */
    fun rollback(snapshot: Int) {
        require(snapshot in 0..changedChild.size) {
            "Invalid snapshot $snapshot"
        }
        while (changedChild.size > snapshot) {
            val b = changedChild.removeAt(changedChild.lastIndex)
            val a = changedParent.removeAt(changedParent.lastIndex)
            parent[b] = b
            sizes[a] -= sizes[b]
            components++
        }
    }
}

sealed class OfflineConnectivityAnswer {
    data class Connected(val value: Boolean) : OfflineConnectivityAnswer()
    data class ComponentCount(val value: Int) : OfflineConnectivityAnswer()
    data class Size(val value: Int) : OfflineConnectivityAnswer()
}

/**
 * Offline dynamic connectivity over a chronological event sequence.
 * Supports multiple copies of the same undirected edge; an edge is active
 * while its multiplicity is >0. Removing an absent edge is rejected.
 *
 * O((events + active intervals) log events log vertices) time,
 * O(events log events + vertices) memory.
 */
class OfflineDynamicConnectivity(val n: Int) {
    init { require(n >= 0) }

    private data class Edge(val u: Int, val v: Int)
    private enum class Kind { ADD, REMOVE, CONNECTED, COMPONENTS, SIZE }
    private data class Event(val kind: Kind, val u: Int = -1, val v: Int = -1)
    private val events = ArrayList<Event>()

    private fun vertex(v: Int) {
        require(v in 0 until n) { "Vertex $v outside 0 until $n" }
    }
    private fun edge(u: Int, v: Int): Edge {
        vertex(u)
        vertex(v)
        return if (u <= v) Edge(u, v) else Edge(v, u)
    }
    fun add(u: Int, v: Int) {
        val e = edge(u, v)
        events.add(Event(Kind.ADD, e.u, e.v))
    }
    fun remove(u: Int, v: Int) {
        val e = edge(u, v)
        events.add(Event(Kind.REMOVE, e.u, e.v))
    }
    fun connected(u: Int, v: Int) {
        vertex(u); vertex(v)
        events.add(Event(Kind.CONNECTED, u, v))
    }
    fun componentCount() {
        events.add(Event(Kind.COMPONENTS))
    }
    fun componentSize(v: Int) {
        vertex(v)
        events.add(Event(Kind.SIZE, v))
    }

    fun solve(): List<OfflineConnectivityAnswer> {
        val m = events.size
        if (m == 0) return emptyList()
        val buckets = Array(m * 4 + 4) { ArrayList<Edge>() }
        fun register(node: Int, left: Int, right: Int, start: Int, end: Int, e: Edge) {
            if (start >= right || end <= left) return
            if (start <= left && right <= end) {
                buckets[node].add(e)
                return
            }
            val mid = left + (right - left) / 2
            register(node * 2, left, mid, start, end, e)
            register(node * 2 + 1, mid, right, start, end, e)
        }

        val multiplicity = HashMap<Edge, Int>()
        val started = HashMap<Edge, Int>()
        events.forEachIndexed { time, event ->
            if (event.kind != Kind.ADD && event.kind != Kind.REMOVE) return@forEachIndexed
            val e = Edge(event.u, event.v)
            val before = multiplicity[e] ?: 0
            if (event.kind == Kind.ADD) {
                if (before == 0) started[e] = time
                multiplicity[e] = before + 1
            } else {
                require(before > 0) { "Remove of absent edge at event $time: $e" }
                if (before == 1) {
                    register(1, 0, m, started.remove(e)!!, time, e)
                    multiplicity.remove(e)
                } else multiplicity[e] = before - 1
            }
        }
        for ((e, time) in started) register(1, 0, m, time, m, e)

        val dsu = RollbackDsu(n)
        val answers = ArrayList<OfflineConnectivityAnswer>()
        fun traverse(node: Int, left: Int, right: Int) {
            val checkpoint = dsu.snapshot()
            for (e in buckets[node]) dsu.union(e.u, e.v)
            if (right - left == 1) {
                val event = events[left]
                when (event.kind) {
                    Kind.CONNECTED -> answers.add(
                        OfflineConnectivityAnswer.Connected(dsu.connected(event.u, event.v))
                    )
                    Kind.COMPONENTS -> answers.add(
                        OfflineConnectivityAnswer.ComponentCount(dsu.components)
                    )
                    Kind.SIZE -> answers.add(
                        OfflineConnectivityAnswer.Size(dsu.componentSize(event.u))
                    )
                    else -> Unit
                }
            } else {
                val mid = left + (right - left) / 2
                traverse(node * 2, left, mid)
                traverse(node * 2 + 1, mid, right)
            }
            dsu.rollback(checkpoint)
        }
        traverse(1, 0, m)
        return answers
    }
}

fun offlineConnectivity(
    vertices: Int,
    block: OfflineDynamicConnectivity.() -> Unit
): List<OfflineConnectivityAnswer> =
    OfflineDynamicConnectivity(vertices).apply(block).solve()
