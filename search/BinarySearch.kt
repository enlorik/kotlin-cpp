import java.util.function.LongPredicate

/**
 * Search the first true index in [0, n). Return n if no index qualifies.
 * The predicate must be monotone: false ... false, true ... true.
 * O(log n) calls, O(1) extra space.
 */
inline fun firstTrue(n: Int, condition: (Int) -> Boolean): Int =
    firstTrue(0, n, condition)

/**
 * Search [from, until), returning until if no index qualifies.
 * Uses Long arithmetic for the midpoint, so even Int.MIN_VALUE to
 * Int.MAX_VALUE does not overflow. The upper bound is excluded.
 */
inline fun firstTrue(from: Int, until: Int, condition: (Int) -> Boolean): Int {
    require(from <= until) { "Expected from <= until" }
    var left = from
    var right = until
    while (left < right) {
        val mid = (left.toLong() + (right.toLong() - left.toLong()) / 2).toInt()
        if (condition(mid)) right = mid else left = mid + 1
    }
    return left
}

/** Long counterpart of firstTrue(n): search [0, n), return n if none. */
fun firstTrue(n: Long, condition: LongPredicate): Long =
    firstTrue(0L, n, condition)

/**
 * Search [from, until) for the first true Long, returning until if none.
 * Unsigned shifting of the modular distance avoids overflow across the full
 * Long domain, including Long.MIN_VALUE .. Long.MAX_VALUE (exclusive).
 */
fun firstTrue(from: Long, until: Long, condition: LongPredicate): Long {
    require(from <= until) { "Expected from <= until" }
    var left = from
    var right = until
    while (left < right) {
        val mid = left + ((right - left) ushr 1)
        if (condition.test(mid)) right = mid else left = mid + 1
    }
    return left
}

/**
 * Search last true in [0, n), or return -1 if no index qualifies.
 * The predicate must be monotone: true ... true, false ... false.
 */
inline fun lastTrue(n: Int, condition: (Int) -> Boolean): Int =
    firstTrue(0, n) { !condition(it) } - 1

/**
 * Search last true in [from, until), or null if none qualifies.
 * The nullable result avoids overflow when from == Int.MIN_VALUE.
 */
inline fun lastTrue(from: Int, until: Int, condition: (Int) -> Boolean): Int? {
    val boundary = firstTrue(from, until) { !condition(it) }
    return if (boundary == from) null else boundary - 1
}

/** Long counterpart of lastTrue(n), returning -1 if none. */
fun lastTrue(n: Long, condition: LongPredicate): Long =
    firstTrue(0L, n) { !condition.test(it) } - 1L

/** Last true in [from, until), or null; safe for Long.MIN_VALUE. */
fun lastTrue(from: Long, until: Long, condition: LongPredicate): Long? {
    val boundary = firstTrue(from, until) { !condition.test(it) }
    return if (boundary == from) null else boundary - 1L
}

/** Search boundaries are half-open [fromIndex, toIndex); both may equal size. */
@PublishedApi internal fun checkBinaryRange(fromIndex: Int, toIndex: Int, size: Int) {
    require(fromIndex >= 0 && fromIndex <= toIndex && toIndex <= size) {
        "Expected 0 <= fromIndex <= toIndex <= size ($size), got [$fromIndex, $toIndex)"
    }
}

/** First element >= target in an ascending IntArray. Returns toIndex if none. */
fun IntArray.lowerBound(target: Int, fromIndex: Int = 0, toIndex: Int = size): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { this[it] >= target }
}

/** First element > target in an ascending IntArray. Returns toIndex if none. */
fun IntArray.upperBound(target: Int, fromIndex: Int = 0, toIndex: Int = size): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { this[it] > target }
}

/** First element >= target in an ascending LongArray. Returns toIndex if none. */
fun LongArray.lowerBound(target: Long, fromIndex: Int = 0, toIndex: Int = size): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { this[it] >= target }
}

/** First element > target in an ascending LongArray. Returns toIndex if none. */
fun LongArray.upperBound(target: Long, fromIndex: Int = 0, toIndex: Int = size): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { this[it] > target }
}

/**
 * First >= target in an ascending comparable List. Prefer random-access
 * lists (e.g. ArrayList): linked lists may take O(n log n) time.
 */
fun <T : Comparable<T>> List<T>.lowerBound(
    target: T, fromIndex: Int = 0, toIndex: Int = size
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { this[it] >= target }
}

/** First > target in an ascending comparable List. */
fun <T : Comparable<T>> List<T>.upperBound(
    target: T, fromIndex: Int = 0, toIndex: Int = size
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { this[it] > target }
}

/** First element >= target under a supplied ascending-order Comparator. */
fun <T> List<T>.lowerBound(
    target: T, comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = size
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { comparator.compare(this[it], target) >= 0 }
}

/** First element > target under a supplied ascending-order Comparator. */
fun <T> List<T>.upperBound(
    target: T, comparator: Comparator<in T>, fromIndex: Int = 0, toIndex: Int = size
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { comparator.compare(this[it], target) > 0 }
}

/** First element >= target in an ascending comparable object Array. */
fun <T : Comparable<T>> Array<out T>.lowerBound(
    target: T, fromIndex: Int = 0, toIndex: Int = size
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { this[it] >= target }
}

/** First element > target in an ascending comparable object Array. */
fun <T : Comparable<T>> Array<out T>.upperBound(
    target: T, fromIndex: Int = 0, toIndex: Int = size
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { this[it] > target }
}
