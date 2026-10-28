/**
 * Describe graph input in the SAME order it appears in the problem statement.
 * No line assumptions: all fields are whitespace-separated tokens.
 *
 * val g = readGraph {
 *     header()                 // read n m
 *     directed()              // optional, before edges()
 *     values()                // optional: read n values right here
 *     edges { from(); to(); weight() } // read m u v w records
 * }
 *
 * Swap values() and edges() when the input puts vertex values last.
 */
@DslMarker annotation class GraphInputDsl

internal enum class EdgeColumnKind {
    FROM, TO, WEIGHT, INT, LONG, TOKEN, SKIP_INT, SKIP_LONG, SKIP_TOKEN
}
internal data class EdgeColumn(val kind: EdgeColumnKind, val name: String = "")

/** Exact columns of one input edge record, evaluated from left to right. */
@GraphInputDsl
class EdgeInputFormat internal constructor() {
    internal val columns = ArrayList<EdgeColumn>()
    private var hasFrom = false
    private var hasTo = false
    private var hasWeight = false
    private val named = HashSet<String>()

    fun from() {
        require(!hasFrom) { "from() must appear once" }
        hasFrom = true
        columns += EdgeColumn(EdgeColumnKind.FROM)
    }
    fun to() {
        require(!hasTo) { "to() must appear once" }
        hasTo = true
        columns += EdgeColumn(EdgeColumnKind.TO)
    }
    fun weight() {
        require(!hasWeight) { "weight() may appear at most once" }
        hasWeight = true
        columns += EdgeColumn(EdgeColumnKind.WEIGHT)
    }
    private fun add(kind: EdgeColumnKind, name: String) {
        require(name.isNotBlank()) { "Field name cannot be blank" }
        require(named.add(name)) { "Duplicate edge field $name" }
        columns += EdgeColumn(kind, name)
    }
    fun int(name: String) = add(EdgeColumnKind.INT, name)
    fun long(name: String) = add(EdgeColumnKind.LONG, name)
    fun token(name: String) = add(EdgeColumnKind.TOKEN, name)
    fun skipInt() { columns += EdgeColumn(EdgeColumnKind.SKIP_INT) }
    fun skipLong() { columns += EdgeColumn(EdgeColumnKind.SKIP_LONG) }
    fun skipToken() { columns += EdgeColumn(EdgeColumnKind.SKIP_TOKEN) }
    internal fun verify() {
        require(hasFrom && hasTo) { "Each edge needs exactly one from() and one to()" }
    }
}

@GraphInputDsl
class GraphInputBuilder internal constructor(
    private val nextIntValue: () -> Int,
    private val nextLongValue: () -> Long,
    private val nextTokenValue: () -> String
) {
    private var n = -1
    private var m = -1
    private var isDirected = false
    private var base = 1
    private var didEdges = false
    private var graph: SparseGraph? = null
    private val vertexIntFields = LinkedHashMap<String, IntArray>()
    private val vertexLongFields = LinkedHashMap<String, LongArray>()
    private val vertexTokenFields = LinkedHashMap<String, Array<String>>()

    /** Standard header: first two integers are n m. */
    fun header() { size(int(), int()) }
    /** Already know n and m, or parsed an unusual header using int()/token(). */
    fun size(vertices: Int, edges: Int) {
        require(n == -1) { "Graph size already configured" }
        require(vertices >= 0 && edges >= 0) { "Graph sizes must be nonnegative" }
        n = vertices; m = edges
    }
    /** Convenience for a tree input: read n and infer m=n-1. */
    fun tree() {
        val vertices = int()
        require(vertices > 0) { "Tree must have at least one vertex" }
        size(vertices, vertices - 1)
    }
    fun directed() { require(!didEdges) { "Set directed() before edges()" }; isDirected = true }
    fun undirected() { require(!didEdges) { "Set undirected() before edges()" }; isDirected = false }
    fun oneIndexed() { require(!didEdges) { "Set indexing before edges()" }; base = 1 }
    fun zeroIndexed() { require(!didEdges) { "Set indexing before edges()" }; base = 0 }

    /** Token readers for unusual headers, optional fields, or custom segments. */
    fun int(): Int = nextIntValue()
    fun long(): Long = nextLongValue()
    fun token(): String = nextTokenValue()
    fun ints(count: Int): IntArray {
        require(count >= 0) { "count must be nonnegative" }
        return IntArray(count) { int() }
    }
    fun longs(count: Int): LongArray {
        require(count >= 0) { "count must be nonnegative" }
        return LongArray(count) { long() }
    }

    /** n integer vertex values (may appear before or after edges). */
    fun values(): IntArray = vertexInts("values")
    fun vertexInts(name: String): IntArray {
        needSize(); checkName(name)
        return ints(n).also { vertexIntFields[name] = it }
    }
    fun vertexLongs(name: String): LongArray {
        needSize(); checkName(name)
        return longs(n).also { vertexLongFields[name] = it }
    }
    fun vertexTokens(name: String): Array<String> {
        needSize(); checkName(name)
        return Array(n) { token() }.also { vertexTokenFields[name] = it }
    }
    private fun checkName(name: String) {
        require(name.isNotBlank() && name !in vertexIntFields &&
            name !in vertexLongFields && name !in vertexTokenFields) {
            "Duplicate or blank vertex field $name"
        }
    }
    private fun needSize() {
        require(n >= 0) { "Declare header() or size(n,m) before reading data" }
    }
    private fun index(raw: Int): Int {
        val v = raw.toLong() - base
        require(v >= 0L && v < n.toLong()) { "Vertex label $raw outside graph" }
        return v.toInt()
    }

    /**
     * Read m edges with the given exact column layout; defaults to u v.
     * Named attributes are stored in input-edge order (not adjacency order).
     * Undirected reverse adjacency entries therefore do not duplicate them.
     */
    fun edges(format: EdgeInputFormat.() -> Unit = { from(); to() }) {
        needSize()
        require(!didEdges) { "edges() can only be called once" }
        val layout = EdgeInputFormat().apply(format)
        layout.verify()
        val result = SparseGraph(n, isDirected)
        val ints = LinkedHashMap<String, IntArray>()
        val longs = LinkedHashMap<String, LongArray>()
        val tokens = LinkedHashMap<String, Array<String>>()
        for (column in layout.columns) when (column.kind) {
            EdgeColumnKind.INT -> ints[column.name] = IntArray(m)
            EdgeColumnKind.LONG -> longs[column.name] = LongArray(m)
            EdgeColumnKind.TOKEN -> tokens[column.name] = Array(m) { "" }
            else -> Unit
        }
        repeat(m) { i ->
            var from: Int? = null
            var to: Int? = null
            var weight = 1L
            for (column in layout.columns) when (column.kind) {
                EdgeColumnKind.FROM -> from = index(int())
                EdgeColumnKind.TO -> to = index(int())
                EdgeColumnKind.WEIGHT -> weight = long()
                EdgeColumnKind.INT -> ints.getValue(column.name)[i] = int()
                EdgeColumnKind.LONG -> longs.getValue(column.name)[i] = long()
                EdgeColumnKind.TOKEN -> tokens.getValue(column.name)[i] = token()
                EdgeColumnKind.SKIP_INT -> { int() }
                EdgeColumnKind.SKIP_LONG -> { long() }
                EdgeColumnKind.SKIP_TOKEN -> { token() }
            }
            result.addEdge(from!!, to!!, weight)
        }
        for ((name, data) in ints) result.setEdgeInts(name, data)
        for ((name, data) in longs) result.setEdgeLongs(name, data)
        for ((name, data) in tokens) result.setEdgeTokens(name, data)
        graph = result
        didEdges = true
    }

    internal fun build(): SparseGraph {
        needSize()
        require(didEdges || m == 0) { "Call edges() to consume the $m edge records" }
        val result = graph ?: SparseGraph(n, isDirected)
        for ((name, data) in vertexIntFields) result.setVertexInts(name, data)
        for ((name, data) in vertexLongFields) result.setVertexLongs(name, data)
        for ((name, data) in vertexTokenFields) result.setVertexTokens(name, data)
        return result
    }
}

/** Describe the input format directly in a Kotlin block, returning SparseGraph. */
fun readGraph(block: GraphInputBuilder.() -> Unit): SparseGraph =
    GraphInputBuilder(::readInt, ::readLong, ::readToken).apply(block).build()

/** Parse the same DSL from a custom FastInput, for tests or multiple streams. */
fun FastInput.readGraph(block: GraphInputBuilder.() -> Unit): SparseGraph =
    GraphInputBuilder({ nextInt() }, { nextLong() }, { nextToken() }).apply(block).build()

/** Short expressive alias for readGraph { ... }; it consumes input immediately. */
fun graph(block: GraphInputBuilder.() -> Unit): SparseGraph = readGraph(block)
