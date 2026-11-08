import java.io.BufferedInputStream

private class BenchmarkInput {
    private val input = BufferedInputStream(System.`in`)
    private val buffer = ByteArray(1 shl 16)
    private var index = 0
    private var size = 0
    private fun byte(): Int {
        if (index >= size) {
            size = input.read(buffer)
            index = 0
            if (size <= 0) return -1
        }
        return buffer[index++].toInt() and 255
    }
    fun nextInt(): Int {
        var c = byte()
        while (c in 0..32) c = byte()
        require(c != -1) { "Unexpected end of workload" }
        var sign = 1
        if (c == 45) { sign = -1; c = byte() }
        var value = 0
        while (c in 48..57) {
            value = value * 10 + c - 48
            c = byte()
        }
        return value * sign
    }
}

/** Uses the actual repository SegmentTree<Long> implementation. */
fun main() {
    val input = BenchmarkInput()
    val n = input.nextInt()
    val q = input.nextInt()
    val values = LongArray(n) { input.nextInt().toLong() }
    val types = IntArray(q)
    val left = IntArray(q)
    val right = IntArray(q)
    for (i in 0 until q) {
        types[i] = input.nextInt()
        left[i] = input.nextInt()
        right[i] = input.nextInt()
    }
    val durations = LongArray(7)
    var checksum = 0L
    repeat(7) { run ->
        val start = System.nanoTime()
        val tree = values.segmentTree()
        var total = 0L
        for (i in 0 until q) {
            if (types[i] == 0) tree[left[i]] = right[i].toLong()
            else total += tree.query(left[i], right[i])
        }
        durations[run] = System.nanoTime() - start
        checksum = total
    }
    val measured = durations.drop(2).sorted()
    println("checksum=" + checksum)
    println("median_ms=" + measured[measured.size / 2] / 1_000_000.0)
}
