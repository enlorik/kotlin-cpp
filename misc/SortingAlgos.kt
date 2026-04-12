fun mergeSort(a: IntArray): Long {
    if (a.size <= 1) return 0L
    val mid = a.size / 2
    val left = a.copyOfRange(0, mid)
    val right = a.copyOfRange(mid, a.size)
    var inv = mergeSort(left) + mergeSort(right)
    var i = 0; var j = 0; var k = 0
    while (i < left.size && j < right.size) {
        if (left[i] <= right[j]) a[k++] = left[i++]
        else { inv += left.size - i; a[k++] = right[j++] }
    }
    while (i < left.size) a[k++] = left[i++]
    while (j < right.size) a[k++] = right[j++]
    return inv
}

fun mergeSortLong(a: LongArray): Long {
    if (a.size <= 1) return 0L
    val mid = a.size / 2
    val left = a.copyOfRange(0, mid)
    val right = a.copyOfRange(mid, a.size)
    var inv = mergeSortLong(left) + mergeSortLong(right)
    var i = 0; var j = 0; var k = 0
    while (i < left.size && j < right.size) {
        if (left[i] <= right[j]) a[k++] = left[i++]
        else { inv += left.size - i; a[k++] = right[j++] }
    }
    while (i < left.size) a[k++] = left[i++]
    while (j < right.size) a[k++] = right[j++]
    return inv
}

fun countSort(a: IntArray, maxVal: Int = a.max()!!): IntArray {
    val cnt = IntArray(maxVal + 1)
    for (v in a) cnt[v]++
    for (i in 1..maxVal) cnt[i] += cnt[i - 1]
    val out = IntArray(a.size)
    for (i in a.indices.reversed()) out[--cnt[a[i]]] = a[i]
    return out
}
