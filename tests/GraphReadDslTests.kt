import java.io.ByteArrayInputStream

private fun graphInput(s: String): FastInput =
    FastInput(ByteArrayInputStream(s.trimIndent().toByteArray()))

private fun checkInput(ok: Boolean, label: String) {
    check(ok) { "Graph DSL test failed: $label" }
}
private inline fun invalidInput(block: () -> Unit) {
    var rejected = false
    try { block() } catch (_: IllegalArgumentException) { rejected = true }
    checkInput(rejected, "Expected IllegalArgumentException")
}

fun main() {
    // u v edges, then a vertex-values array.
    val after = graphInput("""
        4 3
        1 2
        1 3
        2 4
        10 20 30 40
    """).readGraph {
        header()
        edges()
        values()
    }
    checkInput(after.values.contentEquals(intArrayOf(10, 20, 30, 40)), "values after edges")
    checkInput(after.g[0].map { it.to }.sorted() == listOf(1, 2), "1-indexed edges")

    // Vertex values FIRST, then weighted edge records.
    val before = graphInput("""
        4 3
        10 20 30 40
        1 2 7
        1 3 8
        2 4 9
    """).readGraph {
        header()
        values()
        edges { from(); to(); weight() }
    }
    checkInput(before.values.contentEquals(after.values), "values before edges")
    checkInput(before.g[0].map { it.w }.sorted() == listOf(7L, 8L), "weighted edges")

    // Edge record with arbitrary order and different data types.
    val custom = graphInput("""
        3 2
        0 blue 1 11 1007
        2 red 1 12 1008
    """).readGraph {
        header()
        directed()
        zeroIndexed()
        edges {
            from()
            token("color")
            to()
            int("kind")
            long("capacity")
        }
    }
    checkInput(custom.directed, "directed")
    checkInput(custom.edgeTokens("color").contentEquals(arrayOf("blue", "red")), "edge token field")
    checkInput(custom.edgeInts("kind").contentEquals(intArrayOf(11, 12)), "edge int field")
    checkInput(custom.edgeLongs("capacity").contentEquals(longArrayOf(1007, 1008)), "edge long field")
    checkInput(custom.g[0].single().to == 1 && custom.g[2].single().to == 1, "0-based directed edges")
    checkInput(custom.g[1].isEmpty(), "no reverse edges")

    // A tree with n in the header and m inferred as n-1.
    val tree = graphInput("""
        4
        0 1
        1 2
        1 3
    """).readGraph {
        tree()
        zeroIndexed()
        edges()
    }
    checkInput(tree.n == 4 && tree.g[1].size == 3, "tree shorthand")

    // Extra columns can be skipped. Values still follow after edges.
    val skipped = graphInput("""
        3 1
        1 2 ignored 7
        7 8 9
    """).readGraph {
        header()
        edges { from(); to(); skipToken(); weight() }
        values()
    }
    checkInput(skipped.g[0].single().w == 7L, "skip input column")
    checkInput(skipped.values.contentEquals(intArrayOf(7, 8, 9)), "skipped values alignment")

    // The header doesn't have to be n m.
    val unusual = graphInput("case 3 1 0 1").readGraph {
        checkInput(token() == "case", "custom header token")
        val n = int()
        val m = int()
        size(n, m)
        zeroIndexed()
        edges()
    }
    checkInput(unusual.g[0].single().to == 1, "custom header")

    // Multiple named arrays of different types, with zero edges.
    val named = graphInput("""
        3 0
        5 7 9
        12345678901 1 2
        green blue pink
    """).readGraph {
        header()
        vertexInts("score")
        vertexLongs("cost")
        vertexTokens("color")
    }
    checkInput(named.vertexInts("score").contentEquals(intArrayOf(5, 7, 9)), "named vertex ints")
    checkInput(named.vertexLongs("cost")[0] == 12345678901L, "named vertex longs")
    checkInput(named.vertexTokens("color")[2] == "pink", "named vertex tokens")

    // Missing values() is explicit, never silently treated as zeros.
    var missing = false
    try { named.values } catch (_: IllegalStateException) { missing = true }
    checkInput(missing, "missing values error")

    val manual = SparseGraph(2).withValues(intArrayOf(5, 6))
    checkInput(manual.values.contentEquals(intArrayOf(5, 6)), "manually attach values")
    invalidInput { manual.withValues(intArrayOf(1)) }

    checkInput(graphInput("0 0").readGraph { header() }.n == 0, "empty graph")
    checkInput(graphInput("3 0").readGraph { header() }.n == 3, "no edges")

    invalidInput { graphInput("3 1 1 2").readGraph { header() } }
    invalidInput { graphInput("3 1 1 2").readGraph { header(); edges { from() } } }
    invalidInput { graphInput("3 1 1 2").readGraph { header(); edges { from(); to(); to() } } }
    invalidInput { graphInput("3 1 1 2").readGraph { header(); edges { from(); to(); weight(); weight() } } }
    invalidInput { graphInput("3 1 1 2").readGraph { header(); edges { from(); to(); int("x"); long("x") } } }
    invalidInput { graphInput("3 1 1 4").readGraph { header(); edges() } }
    invalidInput { graphInput("3 1 1 0").readGraph { header(); edges() } }
    invalidInput { graphInput("3 1 1 2").readGraph { header(); edges(); directed() } }
    invalidInput { graphInput("3 1 1 2").readGraph { header(); edges(); zeroIndexed() } }
    invalidInput { graphInput("3 0 1 2 3").readGraph { header(); values(); values() } }
    invalidInput { graphInput("3 1 1 2").readGraph { header(); edges(); edges() } }
    invalidInput { graphInput("-1 1").readGraph { header() } }
    invalidInput { graphInput("0").readGraph { tree() } }

    println("PASS: graph DSL layouts, metadata, indexing, trees, and invalid formats")
}
