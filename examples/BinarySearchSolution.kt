/**
 * Input: n q, followed by n ascending integers and q query integers.
 * Output: for each query, the 0-based index of its first occurrence, or -1.
 * Demonstrates lowerBound and the firstTrue predicate form.
 */
fun main() {
    val (n, q) = readInts()
    val a = readInts(n)
    repeat(q) {
        val x = readInt()
        val pos = a.lowerBound(x)
        println(if (pos < n && a[pos] == x) pos else -1)
    }
}
