class SparseTable<T>(arr: Array<T>, val id: T, val merge: (T, T) -> T) {
    private val n = arr.size
    private val log2 = IntArray(n + 1).also {
        it[1] = 0
        for (i in 2..n) it[i] = it[i / 2] + 1
    }
    private val k = if (n > 0) log2[n] + 1 else 1
    private val table: Array<Array<Any?>> = Array(k) { arrayOfNulls(n) }

    init {
        for (i in 0 until n) table[0][i] = arr[i]
        for (j in 1 until k) {
            for (i in 0..n - (1 shl j)) {
                @Suppress("UNCHECKED_CAST")
                table[j][i] = merge(table[j - 1][i] as T, table[j - 1][i + (1 shl (j - 1))] as T)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun query(l: Int, r: Int): T {
        if (l > r) return id
        val j = log2[r - l + 1]
        return merge(table[j][l] as T, table[j][r - (1 shl j) + 1] as T)
    }
}

class LongSparseTable(arr: LongArray, val id: Long, val merge: (Long, Long) -> Long) {
    private val n = arr.size
    private val log2 = IntArray(n + 1).also {
        it[1] = 0
        for (i in 2..n) it[i] = it[i / 2] + 1
    }
    private val k = if (n > 0) log2[n] + 1 else 1
    private val table: Array<LongArray> = Array(k) { LongArray(n) }

    init {
        for (i in 0 until n) table[0][i] = arr[i]
        for (j in 1 until k) {
            for (i in 0..n - (1 shl j)) {
                table[j][i] = merge(table[j - 1][i], table[j - 1][i + (1 shl (j - 1))])
            }
        }
    }

    fun query(l: Int, r: Int): Long {
        if (l > r) return id
        val j = log2[r - l + 1]
        return merge(table[j][l], table[j][r - (1 shl j) + 1])
    }
}

fun <T> spt(arr: Array<T>, id: T, merge: (T, T) -> T): SparseTable<T> =
    SparseTable(arr, id, merge)

fun spt(arr: LongArray, id: Long, merge: (Long, Long) -> Long): LongSparseTable =
    LongSparseTable(arr, id, merge)
