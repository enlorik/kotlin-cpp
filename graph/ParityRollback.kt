/** Rollback XOR constraints: color[u] xor color[v] = parity.
 * No path compression; O(log n) per constraint, O(1) per undo.
 * Contradictions are counted, including repeated contradictory constraints.
 */
class ParityRollbackDsu(val size: Int) {
    init { require(size >= 0) }
    private val parent = IntArray(size) { it }
    private val sizes = IntArray(size) { 1 }
    private val xorToParent = IntArray(size)
    private data class Change(val child: Int, val root: Int, val conflict: Boolean)
    private val history = ArrayList<Change>()
    private var conflicts = 0
    val isConsistent: Boolean get() = conflicts == 0
    fun snapshot(): Int = history.size
    private fun locate(v: Int): Pair<Int,Int> {
        require(v in 0 until size)
        var x = v; var p = 0
        while (parent[x] != x) { p = p xor xorToParent[x]; x = parent[x] }
        return x to p
    }
    /** Returns parity in the spanning forest, or null if disconnected.
     * Meaningful as a constraint implication only while isConsistent.
     */
    fun parityBetween(u: Int, v: Int): Int? {
        val (a,p) = locate(u); val (b,q) = locate(v)
        return if (a == b) p xor q else null
    }
    fun constrain(u: Int, v: Int, parity: Int = 1): Boolean {
        require(parity == 0 || parity == 1)
        val (a,p) = locate(u); val (b,q) = locate(v)
        val link = p xor q xor parity
        if (a == b) {
            val conflict = link != 0
            if (conflict) conflicts++
            history.add(Change(-1,-1,conflict))
        } else {
            val root = if (sizes[a] >= sizes[b]) a else b
            val child = if (root == a) b else a
            history.add(Change(child,root,false))
            parent[child] = root; xorToParent[child] = link
            sizes[root] += sizes[child]
        }
        return isConsistent
    }
    fun rollback(snapshot: Int) {
        require(snapshot in 0..history.size)
        while (history.size > snapshot) {
            val c = history.removeAt(history.lastIndex)
            if (c.conflict) conflicts--
            if (c.child >= 0) {
                sizes[c.root] -= sizes[c.child]
                parent[c.child] = c.child; xorToParent[c.child] = 0
            }
        }
    }
}

/** Offline graph bipartiteness with multiset edges, segment tree over time.
 * O((events+intervals) log(events+1) log(vertices+1)) time.
 */
class OfflineDynamicBipartiteness(val vertices: Int) {
    init { require(vertices >= 0) }
    private data class Edge(val u: Int,val v: Int)
    private data class Event(val kind: Int,val edge: Edge?)
    private val events = ArrayList<Event>()
    private fun edge(u: Int,v: Int): Edge {
        require(u in 0 until vertices && v in 0 until vertices)
        return Edge(minOf(u,v),maxOf(u,v))
    }
    fun add(u: Int,v: Int) { events.add(Event(1,edge(u,v))) }
    fun remove(u: Int,v: Int) { events.add(Event(-1,edge(u,v))) }
    fun query() { events.add(Event(0,null)) }
    fun solve(): List<Boolean> {
        val m = events.size
        if (m == 0) return emptyList()
        val buckets = Array(4*m+4) { ArrayList<Edge>() }
        fun register(node: Int,l: Int,r: Int,a: Int,b: Int,e: Edge) {
            if (a >= r || b <= l) return
            if (a <= l && r <= b) { buckets[node].add(e); return }
            val mid = l+(r-l)/2
            register(node*2,l,mid,a,b,e); register(node*2+1,mid,r,a,b,e)
        }
        val counts = HashMap<Edge,Int>()
        val starts = HashMap<Edge,Int>()
        for ((t,event) in events.withIndex()) {
            val e = event.edge ?: continue
            val count = counts[e] ?: 0
            if (event.kind == 1) {
                if (count == 0) starts[e] = t
                counts[e] = count+1
            } else {
                require(count > 0) { "Removing inactive edge at event $t" }
                if (count == 1) {
                    register(1,0,m,starts.remove(e)!!,t,e)
                    counts.remove(e)
                } else counts[e] = count-1
            }
        }
        for ((e,t) in starts) register(1,0,m,t,m,e)
        val dsu = ParityRollbackDsu(vertices)
        val result = ArrayList<Boolean>()
        fun visit(node: Int,l: Int,r: Int) {
            val snap = dsu.snapshot()
            for (e in buckets[node]) dsu.constrain(e.u,e.v)
            if (r-l == 1) {
                if (events[l].kind == 0) result.add(dsu.isConsistent)
            } else {
                val mid = l+(r-l)/2
                visit(node*2,l,mid); visit(node*2+1,mid,r)
            }
            dsu.rollback(snap)
        }
        visit(1,0,m)
        return result
    }
}
fun offlineBipartiteness(vertices: Int, block: OfflineDynamicBipartiteness.() -> Unit): List<Boolean> =
    OfflineDynamicBipartiteness(vertices).apply(block).solve()
