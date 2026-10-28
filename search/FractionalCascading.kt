/**
 * Fractional cascading across immutable sorted IntArray catalogs.
 * After one binary search, at most two comparisons per additional catalog:
 * O(log(total size + 1) + catalogCount) per lowerBounds/upperBounds query.
 * Construction is O(total elements) time and space via every-second sampling.
 * Duplicates and empty catalogs are supported; input arrays are copied.
 */
class FractionalCascade(catalogs: List<IntArray>) {
    val catalogCount: Int = catalogs.size

    private class Layer(
        val values: IntArray,
        val originalBefore: IntArray,
        val downHint: IntArray
    )
    private val layers: Array<Layer>

    init {
        for ((i, cat) in catalogs.withIndex()) for (j in 1 until cat.size) {
            require(cat[j - 1] <= cat[j]) { "Catalog $i must be sorted ascending" }
        }
        val result = arrayOfNulls<Layer>(catalogCount)
        var lowerLayer: Layer? = null
        for (i in catalogCount - 1 downTo 0) {
            val original = catalogs[i]
            val below = lowerLayer?.values ?: intArrayOf()
            val sampleCount = (below.size / 2) + (below.size % 2)
            val count = original.size.toLong() + sampleCount.toLong()
            require(count <= Int.MAX_VALUE - 1L) { "Augmented catalog too large" }
            val size = count.toInt()
            val values = IntArray(size)
            val originalBefore = IntArray(size + 1)
            val downHint = IntArray(size + 1)
            var origPos = 0
            var samplePos = 0
            var previousSample = -1
            for (j in 0 until size) {
                originalBefore[j] = origPos
                downHint[j] = previousSample
                val sampleIndex = samplePos * 2
                val takeSample = samplePos < sampleCount &&
                    (origPos == original.size || below[sampleIndex] < original[origPos])
                if (takeSample) {
                    values[j] = below[sampleIndex]
                    previousSample = sampleIndex
                    samplePos++
                } else values[j] = original[origPos++]
            }
            originalBefore[size] = origPos
            downHint[size] = previousSample
            val layer = Layer(values, originalBefore, downHint)
            result[i] = layer
            lowerLayer = layer
        }
        @Suppress("UNCHECKED_CAST")
        layers = result as Array<Layer>
    }

    private fun boundaries(target: Int, strict: Boolean): IntArray {
        if (layers.isEmpty()) return intArrayOf()
        val result = IntArray(catalogCount)
        val top = layers[0].values
        var left = 0
        var right = top.size
        while (left < right) {
            val mid = left + (right - left) / 2
            val below = if (strict) top[mid] <= target else top[mid] < target
            if (below) left = mid + 1 else right = mid
        }
        var pos = left
        for (i in layers.indices) {
            val level = layers[i]
            result[i] = level.originalBefore[pos]
            if (i + 1 < layers.size) {
                val next = layers[i + 1].values
                val hint = level.downHint[pos]
                var nextPos = if (hint >= 0) hint else 0
                // Every second entry from next is sampled:
                // each descent examines at most two further elements.
                while (nextPos < next.size &&
                    (if (strict) next[nextPos] <= target else next[nextPos] < target)) {
                    nextPos++
                }
                pos = nextPos
            }
        }
        return result
    }

    /** Lower bound of target in each ORIGINAL catalog, not augmented layers. */
    fun lowerBounds(target: Int): IntArray = boundaries(target, strict = false)

    /** Upper bound of target in each ORIGINAL catalog, not augmented layers. */
    fun upperBounds(target: Int): IntArray = boundaries(target, strict = true)
}

/** Construct from several ascending Int arrays; duplicates are allowed. */
fun fractionalCascade(vararg catalogs: IntArray): FractionalCascade =
    FractionalCascade(catalogs.asList())
