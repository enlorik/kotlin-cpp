import kotlin.math.sqrt

/**
 * Primitive Int values with Long range sums and lazy interval assignment.
 * Unboxed sqrt decomposition avoids the allocation/dispatch cost of a generic
 * lazy segment tree. All ranges are half-open. Source array is snapshotted.
 *
 * assign/sum: O(blockSize + n/blockSize), replace: O(length + blockSize).
 */
class BlockAssignSum(values: IntArray, blockSize: Int = defaultBlockSize(values.size)) {
    val size = values.size
    private val width: Int = blockSize.also { require(it > 0) { "blockSize must be positive" } }
    private val values = values.copyOf()
    private val blockCount = if (size == 0) 0 else (size - 1) / width + 1
    private val blockSums = LongArray(blockCount)
    private val tags = IntArray(blockCount)
    private val hasTag = BooleanArray(blockCount)

    init {
        for (i in this.values.indices) blockSums[i / width] += this.values[i].toLong()
    }

    private fun start(block: Int) = block * width
    private fun end(block: Int) = minOf(size, (block + 1) * width)

    private fun materialize(block: Int) {
        if (hasTag[block]) {
            values.fill(tags[block], start(block), end(block))
            hasTag[block] = false
        }
    }

    private fun recalculate(block: Int) {
        var total = 0L
        for (i in start(block) until end(block)) total += values[i]
        blockSums[block] = total
    }

    private fun fullBlock(block: Int, value: Int) {
        hasTag[block] = true
        tags[block] = value
        blockSums[block] = (end(block) - start(block)).toLong() * value
    }

    private fun partialBlock(block: Int, from: Int, until: Int, value: Int) {
        if (from == start(block) && until == end(block)) {
            fullBlock(block, value)
            return
        }
        materialize(block)
        values.fill(value, from, until)
        recalculate(block)
    }

    fun assign(from: Int, until: Int, value: Int) {
        require(from in 0..size && until in from..size) { "Invalid range [$from, $until)" }
        if (from == until) return
        val first = from / width
        val last = (until - 1) / width
        if (first == last) {
            partialBlock(first, from, until, value)
            return
        }
        partialBlock(first, from, end(first), value)
        for (block in first + 1 until last) fullBlock(block, value)
        partialBlock(last, start(last), until, value)
    }

    /** Replace consecutive positions in one pass, even across a block boundary. */
    fun replace(from: Int, newValues: IntArray, offset: Int = 0, length: Int = newValues.size - offset) {
        require(offset >= 0 && length >= 0 && offset <= newValues.size - length)
        require(from >= 0 && from <= size && length <= size - from)
        var position = from
        var index = offset
        val until = from + length
        while (position < until) {
            val block = position / width
            val stop = minOf(until, end(block))
            materialize(block)
            while (position < stop) values[position++] = newValues[index++]
            recalculate(block)
        }
    }


    /**
     * Recompute an entire local window while materializing and rebuilding
     * each touched block only once. The callback receives (index, oldValue).
     * O(length + touchedBlocks * blockSize), no per-element tree traversal.
     */
    fun recompute(from: Int, until: Int, compute: (index: Int, previous: Int) -> Int) {
        require(from in 0..size && until in from..size) { "Invalid range [$from, $until)" }
        var position = from
        while (position < until) {
            val block = position / width
            val stop = minOf(until, end(block))
            materialize(block)
            while (position < stop) {
                values[position] = compute(position, values[position])
                position++
            }
            recalculate(block)
        }
    }

    /**
     * Apply strictly increasing point indices, rebuilding any touched block
     * once rather than once per point. Validation precedes all mutations.
     * O(count + touchedBlocks * blockSize).
     */
    fun setMany(indices: IntArray, replacements: IntArray) {
        require(indices.size == replacements.size) { "Indices and values size mismatch" }
        for (i in indices.indices) {
            require(indices[i] in 0 until size) { "Index ${indices[i]} out of bounds" }
            require(i == 0 || indices[i] > indices[i - 1]) {
                "Indices must be strictly increasing"
            }
        }
        var lastBlock = -1
        for (i in indices.indices) {
            val index = indices[i]
            val block = index / width
            if (block != lastBlock) {
                if (lastBlock >= 0) recalculate(lastBlock)
                materialize(block)
                lastBlock = block
            }
            values[index] = replacements[i]
        }
        if (lastBlock >= 0) recalculate(lastBlock)
    }

    fun sum(from: Int, until: Int): Long {
        require(from in 0..size && until in from..size) { "Invalid range [$from, $until)" }
        if (from == until) return 0L
        var result = 0L
        var position = from
        while (position < until && position % width != 0) {
            val block = position / width
            result += if (hasTag[block]) tags[block].toLong() else values[position].toLong()
            position++
        }
        while (position + width <= until) {
            result += blockSums[position / width]
            position += width
        }
        while (position < until) {
            val block = position / width
            result += if (hasTag[block]) tags[block].toLong() else values[position].toLong()
            position++
        }
        return result
    }
}

/** Primitive interval assignment + sum for contest workloads with tight limits. */
fun IntArray.blockAssignSum(blockSize: Int = defaultBlockSize(size)): BlockAssignSum =
    BlockAssignSum(this, blockSize)

private fun defaultBlockSize(n: Int): Int = maxOf(1, sqrt(n.toDouble()).toInt())
