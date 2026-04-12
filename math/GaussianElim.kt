import kotlin.math.abs

fun gaussianElim(a: Array<DoubleArray>): DoubleArray? {
    val n = a.size
    val m = a[0].size - 1
    val row = IntArray(n) { it }
    for (col in 0 until m) {
        var pivot = -1
        for (i in col until n) if (pivot == -1 || abs(a[row[i]][col]) > abs(a[row[pivot]][col])) pivot = i
        if (abs(a[row[pivot]][col]) < 1e-9) continue
        val tmp = row[col]; row[col] = row[pivot]; row[pivot] = tmp
        val scale = a[row[col]][col]
        for (j in col..m) a[row[col]][j] /= scale
        for (i in 0 until n) if (i != col && abs(a[row[i]][col]) > 1e-9) {
            val f = a[row[i]][col]
            for (j in col..m) a[row[i]][j] -= f * a[row[col]][j]
        }
    }
    val sol = DoubleArray(m)
    for (i in 0 until n) {
        var nonzero = -1
        for (j in 0 until m) if (abs(a[row[i]][j]) > 1e-9) { nonzero = j; break }
        if (nonzero == -1 && abs(a[row[i]][m]) > 1e-9) return null
        if (nonzero != -1) sol[nonzero] = a[row[i]][m]
    }
    return sol
}

class XorBasis {
    private val basis = LongArray(64)
    var rank = 0; private set

    fun add(x: Long): Boolean {
        var v = x
        for (b in basis) v = minOf(v, v xor b)
        if (v == 0L) return false
        basis[rank++] = v
        return true
    }

    fun maxXor(): Long {
        var res = 0L
        for (i in rank - 1 downTo 0) res = maxOf(res, res xor basis[i])
        return res
    }
}

fun gaussianElimGF2(rows: LongArray): Int {
    val basis = mutableListOf<Long>()
    for (r in rows) {
        var v = r
        for (b in basis) v = minOf(v, v xor b)
        if (v != 0L) basis.add(v)
    }
    return basis.size
}
