import java.math.BigInteger

/** One layer of partition DP, with a selected optimal split for each j. */
data class PartitionDpRow(val values: LongArray, val split: IntArray)

/**
 * Divide-and-conquer optimization for
 * dp[j] = min_{0 <= i < j}(previous[i] + cost(i, j)).
 *
 * Requires the optimal split indices to be NONDECREASING with j (using
 * the leftmost optimal split on ties). This is a caller-provided mathematical
 * precondition, NOT a property inferred from arbitrary cost lambdas.
 *
 * O(n log n) cost evaluations under that property, O(n) output memory.
 * previous[0] represents an empty prefix; output[0] is infinity.
 */
fun divideConquerPartitionRow(
    previous: LongArray,
    infinity: Long = Long.MAX_VALUE / 4,
    cost: (split: Int, until: Int) -> Long
): PartitionDpRow {
    require(previous.isNotEmpty()) { "DP row needs an empty-prefix state at index 0" }
    val n = previous.size
    val answers = LongArray(n) { infinity }
    val choices = IntArray(n) { -1 }

    fun solve(left: Int, right: Int, optLeft: Int, optRight: Int) {
        if (left > right) return
        val mid = left + (right - left) / 2
        var best = infinity
        var bestIndex = -1
        val upper = minOf(mid - 1, optRight)
        for (i in optLeft..upper) {
            if (previous[i] == infinity) continue
            val candidate = previous[i] + cost(i, mid)
            if (candidate < best) {
                best = candidate
                bestIndex = i
            }
        }
        answers[mid] = best
        choices[mid] = bestIndex
        if (bestIndex >= 0) {
            solve(left, mid - 1, optLeft, bestIndex)
            solve(mid + 1, right, bestIndex, optRight)
        } else {
            solve(left, mid - 1, optLeft, optRight)
            solve(mid + 1, right, optLeft, optRight)
        }
    }

    if (n > 1) solve(1, n - 1, 0, n - 2)
    return PartitionDpRow(answers, choices)
}

data class HullLine(val slope: Long, val intercept: Long) {
    fun at(x: Long): Long = slope * x + intercept
}

/**
 * Monotone-slope lower convex hull:
 * insert lines with NONINCREASING slopes, query minimum at any x.
 * O(1) amortized insertion (excluding big-integer comparisons), O(log n)
 * binary-search queries. The intersection test itself is exact.
 *
 * Evaluating slope*x+intercept still requires it to fit signed Long.
 */
class MonotoneMinHull {
    private val lines = ArrayList<HullLine>()
    val size: Int get() = lines.size

    private fun redundant(a: HullLine, b: HullLine, c: HullLine): Boolean {
        fun big(x: Long) = BigInteger.valueOf(x)
        // (b.intercept - a.intercept)/(a.slope-b.slope)
        // >= (c.intercept-b.intercept)/(b.slope-c.slope)
        // Both denominators strictly positive because slopes decrease.
        val left = (big(b.intercept) - big(a.intercept)) *
            (big(b.slope) - big(c.slope))
        val right = (big(c.intercept) - big(b.intercept)) *
            (big(a.slope) - big(b.slope))
        return left >= right
    }

    fun add(slope: Long, intercept: Long) {
        val fresh = HullLine(slope, intercept)
        if (lines.isNotEmpty()) {
            require(slope <= lines.last().slope) {
                "Insert slopes in nonincreasing order"
            }
            if (slope == lines.last().slope) {
                if (intercept >= lines.last().intercept) return
                lines.removeAt(lines.lastIndex)
            }
        }
        while (lines.size >= 2 &&
            redundant(lines[lines.size - 2], lines.last(), fresh)) {
            lines.removeAt(lines.lastIndex)
        }
        lines.add(fresh)
    }

    /** Null if the hull contains no lines. */
    fun query(x: Long): Long? {
        if (lines.isEmpty()) return null
        var lo = 0
        var hi = lines.lastIndex
        while (lo < hi) {
            val mid = lo + (hi - lo) / 2
            if (lines[mid].at(x) <= lines[mid + 1].at(x)) hi = mid
            else lo = mid + 1
        }
        return lines[lo].at(x)
    }
}

fun monotoneMinHull(block: MonotoneMinHull.() -> Unit): MonotoneMinHull =
    MonotoneMinHull().apply(block)
