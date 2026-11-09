import kotlin.math.sqrt

/** Mutable string with O(1) character reads and lazy block-wise interval paints.
 * assign [from, until): O(blockSize + n/blockSize). Keeps a snapshot of source.
 */
class PaintedString(source: String, blockSize: Int = maxOf(1, sqrt(source.length.toDouble()).toInt())) {
    val size = source.length
    private val width = blockSize.also { require(it > 0) { "blockSize must be positive" } }
    private val chars = source.toCharArray()
    private val blocks = if (size == 0) 0 else (size - 1) / width + 1
    private val tags = CharArray(blocks)
    private val tagged = BooleanArray(blocks)

    operator fun get(index: Int): Char {
        require(index in 0 until size)
        val block = index / width
        return if (tagged[block]) tags[block] else chars[index]
    }

    private fun materialize(block: Int) {
        if (tagged[block]) {
            chars.fill(tags[block], block * width, minOf(size, (block + 1) * width))
            tagged[block] = false
        }
    }

    fun assign(from: Int, until: Int, c: Char) {
        require(from in 0..size && until in from..size) { "Invalid range [$from, $until)" }
        if (from == until) return
        val first = from / width
        val last = (until - 1) / width
        for (block in first..last) {
            val left = maxOf(from, block * width)
            val right = minOf(until, (block + 1) * width, size)
            if (left == block * width && right == minOf(size, (block + 1) * width)) {
                tags[block] = c
                tagged[block] = true
            } else {
                materialize(block)
                chars.fill(c, left, right)
            }
        }
    }
}

fun String.paintedString(blockSize: Int = maxOf(1, sqrt(length.toDouble()).toInt())): PaintedString =
    PaintedString(this, blockSize)
