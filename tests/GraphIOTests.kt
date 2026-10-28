import java.io.ByteArrayInputStream
import java.io.StringWriter

private fun ioCheck(ok: Boolean, message: String) { if (!ok) error(message) }
private inline fun ioInvalid(block: () -> Unit) {
    var failed = false
    try { block() } catch (_: IllegalArgumentException) { failed = true }
    ioCheck(failed, "Expected IllegalArgumentException")
}

fun main() {
    val reader = FastInput(ByteArrayInputStream("1 2 2 3 3 1".toByteArray()))
    val graph = reader.readGraph(3, 3)
    ioCheck(!graph.directed, "default undirected")
    ioCheck(graph.g[0].map { it.to }.sorted() == listOf(1, 2), "1-indexed edges")
    ioCheck(graph.g[2].map { it.to }.sorted() == listOf(0, 1), "undirected reverse edges")

    val weighted = FastInput(ByteArrayInputStream("0 1 9 1 2 17".toByteArray()))
        .readGraph(3, 2, directed = true, oneIndexed = false, weighted = true)
    ioCheck(weighted.directed && weighted.g[0].single().w == 9L, "directed weighted")
    ioCheck(weighted.g[1].single().w == 17L && weighted.g[2].isEmpty(), "no reverse edges")

    ioInvalid {
        FastInput(ByteArrayInputStream("1 4".toByteArray())).readGraph(3, 1)
    }
    ioInvalid {
        FastInput(ByteArrayInputStream(byteArrayOf())).readGraph(-1, 0)
    }

    val sink = StringWriter()
    val out = FastOutput(sink)
    out.printValues(intArrayOf(4, -1, 6))
    out.printValues(longArrayOf(7L, 8L))
    out.printValues(listOf("A", "B"))
    out.printValues(3) { i -> i * i }
    out.printValues(0) { error("No values expected") }
    ioCheck(
        sink.toString().replace("\r\n", "\n") == "4 -1 6\n7 8\nA B\n0 1 4\n\n",
        "one-line output with spaces but no trailing space"
    )
    ioInvalid { out.printValues(-1) { it } }
    println("PASS: graph input, directed/weighted/indexed options, printValues formatting")
}
