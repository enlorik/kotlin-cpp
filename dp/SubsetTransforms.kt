/**
 * Sum-over-subsets transforms over all masks 0..2^bits-1.
 * Long arithmetic uses normal two's-complement overflow semantics.
 * Inputs are snapshotted. O(bits * 2^bits) time, O(2^bits) memory.
 */
private fun requireMaskArray(size: Int) {
    require(size > 0 && (size and (size - 1)) == 0) {
        "Subset transform requires a nonempty power-of-two array"
    }
}

fun LongArray.subsetZeta(): LongArray {
    requireMaskArray(size)
    val result = copyOf()
    var bit = 1
    while (bit < size) {
        for (mask in indices) if ((mask and bit) != 0)
            result[mask] += result[mask xor bit]
        bit = bit shl 1
    }
    return result
}

/** Inverse of subsetZeta using Möbius inversion. */
fun LongArray.subsetMobius(): LongArray {
    requireMaskArray(size)
    val result = copyOf()
    var bit = 1
    while (bit < size) {
        for (mask in indices) if ((mask and bit) != 0)
            result[mask] -= result[mask xor bit]
        bit = bit shl 1
    }
    return result
}

fun LongArray.supersetZeta(): LongArray {
    requireMaskArray(size)
    val result = copyOf()
    var bit = 1
    while (bit < size) {
        for (mask in indices) if ((mask and bit) == 0)
            result[mask] += result[mask or bit]
        bit = bit shl 1
    }
    return result
}

/** Inverse of supersetZeta. */
fun LongArray.supersetMobius(): LongArray {
    requireMaskArray(size)
    val result = copyOf()
    var bit = 1
    while (bit < size) {
        for (mask in indices) if ((mask and bit) == 0)
            result[mask] -= result[mask or bit]
        bit = bit shl 1
    }
    return result
}

/** c[mask] = sum of a[x]*b[y] over all x | y == mask. */
fun orConvolution(a: LongArray, b: LongArray): LongArray {
    require(a.size == b.size) { "Input mask arrays must have equal length" }
    val left = a.subsetZeta()
    val right = b.subsetZeta()
    for (mask in left.indices) left[mask] *= right[mask]
    return left.subsetMobius()
}

/** c[mask] = sum of a[x]*b[y] over all x & y == mask. */
fun andConvolution(a: LongArray, b: LongArray): LongArray {
    require(a.size == b.size) { "Input mask arrays must have equal length" }
    val left = a.supersetZeta()
    val right = b.supersetZeta()
    for (mask in left.indices) left[mask] *= right[mask]
    return left.supersetMobius()
}

fun IntArray.subsetZetaLong(): LongArray =
    LongArray(size) { this[it].toLong() }.subsetZeta()

fun IntArray.supersetZetaLong(): LongArray =
    LongArray(size) { this[it].toLong() }.supersetZeta()
