fun compress(a: IntArray): IntArray {
    val sorted = a.toSortedSet().toIntArray()
    val map = HashMap<Int, Int>(sorted.size * 2)
    sorted.forEachIndexed { i, v -> map[v] = i }
    return IntArray(a.size) { map[a[it]]!! }
}

fun compress(a: LongArray): IntArray {
    val sorted = a.toSortedSet().toLongArray()
    val map = HashMap<Long, Int>(sorted.size * 2)
    sorted.forEachIndexed { i, v -> map[v] = i }
    return IntArray(a.size) { map[a[it]]!! }
}

fun compressMap(a: LongArray): Pair<IntArray, LongArray> {
    val sorted = a.toSortedSet().toLongArray()
    val map = HashMap<Long, Int>(sorted.size * 2)
    sorted.forEachIndexed { i, v -> map[v] = i }
    return Pair(IntArray(a.size) { map[a[it]]!! }, sorted)
}
