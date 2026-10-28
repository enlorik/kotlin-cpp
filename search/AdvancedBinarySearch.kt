import java.util.function.LongPredicate

/**
 * Finds the first true Int >= start without requiring an upper bound.
 * Returns null if the condition is false through Int.MAX_VALUE.
 * Predicate must be monotone (false then true) on [start, Int.MAX_VALUE].
 */
inline fun firstTrueFrom(start: Int, condition: (Int) -> Boolean): Int? {
    if (condition(start)) return start
    var previousFalse = start.toLong()
    var step = 1L
    while (true) {
        val next = minOf(Int.MAX_VALUE.toLong(), previousFalse + step)
        if (condition(next.toInt())) {
            var left = previousFalse + 1
            var right = next
            while (left < right) {
                val mid = left + (right - left) / 2
                if (condition(mid.toInt())) right = mid else left = mid + 1
            }
            return left.toInt()
        }
        if (next == Int.MAX_VALUE.toLong()) return null
        previousFalse = next
        step = minOf(step * 2, 1L shl 33)
    }
}

/**
 * Finds the first true Long >= start without knowing an upper bound.
 * Returns null if no value through Long.MAX_VALUE qualifies.
 * Exponential probing followed by binary search: O(log(distance + 1)).
 */
fun firstTrueFrom(start: Long, condition: LongPredicate): Long? {
    if (condition.test(start)) return start
    var previousFalse = start
    var step = 1L
    while (true) {
        val next = if (previousFalse > Long.MAX_VALUE - step) Long.MAX_VALUE
                   else previousFalse + step
        if (condition.test(next)) {
            var left = previousFalse + 1
            var right = next
            while (left < right) {
                val mid = left + ((right - left) ushr 1)
                if (condition.test(mid)) right = mid else left = mid + 1
            }
            return left
        }
        if (next == Long.MAX_VALUE) return null
        previousFalse = next
        step = if (step > Long.MAX_VALUE / 2) Long.MAX_VALUE else step * 2
    }
}

/**
 * Approximates the first true real x inside [left, right].
 * The condition must be monotone false->true and hold at right.
 * Returns left immediately if condition(left) already holds.
 */
inline fun bisect(
    left: Double,
    right: Double,
    iterations: Int = 100,
    condition: (Double) -> Boolean
): Double {
    require(left.isFinite() && right.isFinite() && left <= right) {
        "bisect requires finite bounds left <= right"
    }
    require(iterations >= 0) { "iterations must be nonnegative" }
    if (condition(left)) return left
    require(condition(right)) { "condition must be true at right bound" }
    var lo = left
    var hi = right
    repeat(iterations) {
        val difference = hi - lo
        val mid = if (difference.isFinite()) lo + difference / 2.0 else lo / 2.0 + hi / 2.0
        if (mid <= lo || mid >= hi) return hi
        if (condition(mid)) hi = mid else lo = mid
    }
    return hi
}

/**
 * Bisection for a continuous function with opposite signs at the endpoints.
 * Returns an exact endpoint when f(endpoint)==0, otherwise a root estimate.
 * The function must be continuous between endpoints for root existence.
 */
inline fun bisectRoot(
    left: Double,
    right: Double,
    iterations: Int = 100,
    function: (Double) -> Double
): Double {
    require(left.isFinite() && right.isFinite() && left <= right) {
        "bisectRoot requires finite bounds left <= right"
    }
    require(iterations >= 0) { "iterations must be nonnegative" }
    var lo = left
    var hi = right
    var fLo = function(lo)
    val fHi = function(hi)
    require(fLo.isFinite() && fHi.isFinite()) { "function must be finite at endpoints" }
    if (fLo == 0.0) return lo
    if (fHi == 0.0) return hi
    require((fLo < 0) != (fHi < 0)) { "function must change sign across the interval" }
    repeat(iterations) {
        val difference = hi - lo
        val mid = if (difference.isFinite()) lo + difference / 2.0 else lo / 2.0 + hi / 2.0
        if (mid <= lo || mid >= hi) return mid
        val fMid = function(mid)
        require(fMid.isFinite()) { "function must be finite within the interval" }
        if (fMid == 0.0) return mid
        if ((fLo < 0) == (fMid < 0)) {
            lo = mid
            fLo = fMid
        } else hi = mid
    }
    val difference = hi - lo
    return if (difference.isFinite()) lo + difference / 2.0 else lo / 2.0 + hi / 2.0
}

/** First element satisfying a false->true predicate in [fromIndex, toIndex). */
inline fun IntArray.partitionPoint(
    fromIndex: Int = 0,
    toIndex: Int = size,
    predicate: (Int) -> Boolean
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { predicate(this[it]) }
}

/** First element satisfying a false->true predicate in [fromIndex, toIndex). */
inline fun LongArray.partitionPoint(
    fromIndex: Int = 0,
    toIndex: Int = size,
    predicate: (Long) -> Boolean
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { predicate(this[it]) }
}

/** First element satisfying a false->true predicate in [fromIndex, toIndex). */
inline fun <T> List<T>.partitionPoint(
    fromIndex: Int = 0,
    toIndex: Int = size,
    predicate: (T) -> Boolean
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { predicate(this[it]) }
}

/** First element satisfying a false->true predicate in [fromIndex, toIndex). */
inline fun <T> Array<out T>.partitionPoint(
    fromIndex: Int = 0,
    toIndex: Int = size,
    predicate: (T) -> Boolean
): Int {
    checkBinaryRange(fromIndex, toIndex, size)
    return firstTrue(fromIndex, toIndex) { predicate(this[it]) }
}
