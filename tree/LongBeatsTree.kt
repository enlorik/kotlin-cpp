/**
 * Segment Tree Beats: range chmin/chmax/add and sum/min/max queries.
 * Uses primitive arrays and second-extreme tracking. Updates are amortized
 * O(log^2 n), queries O(log n), memory O(n). Assumes no Long overflow and
 * does not support actual Long.MIN_VALUE/Long.MAX_VALUE array elements.
 * All ranges are half-open [from, until).
 */
class LongBeatsTree(values: LongArray) {
    val size: Int = values.size
    private val capacity = maxOf(4, size * 4 + 4)
    private val total = LongArray(capacity)
    private val low = LongArray(capacity)
    private val low2 = LongArray(capacity) { Long.MAX_VALUE }
    private val high = LongArray(capacity)
    private val high2 = LongArray(capacity) { Long.MIN_VALUE }
    private val countLow = IntArray(capacity)
    private val countHigh = IntArray(capacity)
    private val pendingAdd = LongArray(capacity)

    init {
        require(size < (1 shl 28)) { "Too many elements" }
        require(values.all { it != Long.MIN_VALUE && it != Long.MAX_VALUE }) {
            "Long extremes are reserved as second-extreme sentinels"
        }
        if (size > 0) build(1, 0, size, values)
    }

    private fun build(p: Int, l: Int, r: Int, data: LongArray) {
        if (r - l == 1) {
            val x = data[l]
            total[p] = x
            low[p] = x
            high[p] = x
            countLow[p] = 1
            countHigh[p] = 1
            return
        }
        val m = l + (r - l) / 2
        build(2 * p, l, m, data)
        build(2 * p + 1, m, r, data)
        pull(p)
    }

    private fun pull(p: Int) {
        val a = 2 * p
        val b = a + 1
        total[p] = total[a] + total[b]
        when {
            low[a] < low[b] -> {
                low[p] = low[a]
                countLow[p] = countLow[a]
                low2[p] = minOf(low2[a], low[b])
            }
            low[b] < low[a] -> {
                low[p] = low[b]
                countLow[p] = countLow[b]
                low2[p] = minOf(low[a], low2[b])
            }
            else -> {
                low[p] = low[a]
                countLow[p] = countLow[a] + countLow[b]
                low2[p] = minOf(low2[a], low2[b])
            }
        }
        when {
            high[a] > high[b] -> {
                high[p] = high[a]
                countHigh[p] = countHigh[a]
                high2[p] = maxOf(high2[a], high[b])
            }
            high[b] > high[a] -> {
                high[p] = high[b]
                countHigh[p] = countHigh[b]
                high2[p] = maxOf(high[a], high2[b])
            }
            else -> {
                high[p] = high[a]
                countHigh[p] = countHigh[a] + countHigh[b]
                high2[p] = maxOf(high2[a], high2[b])
            }
        }
    }

    private fun applyAdd(p: Int, len: Int, delta: Long) {
        total[p] += delta * len.toLong()
        low[p] += delta
        high[p] += delta
        if (low2[p] != Long.MAX_VALUE) low2[p] += delta
        if (high2[p] != Long.MIN_VALUE) high2[p] += delta
        pendingAdd[p] += delta
    }

    // Precondition: x is strictly between the largest and second largest.
    private fun applyChmin(p: Int, x: Long) {
        if (x >= high[p]) return
        total[p] += (x - high[p]) * countHigh[p].toLong()
        if (low[p] == high[p]) low[p] = x
        else if (low2[p] == high[p]) low2[p] = x
        high[p] = x
    }

    // Precondition: x is strictly between the smallest and second smallest.
    private fun applyChmax(p: Int, x: Long) {
        if (x <= low[p]) return
        total[p] += (x - low[p]) * countLow[p].toLong()
        if (high[p] == low[p]) high[p] = x
        else if (high2[p] == low[p]) high2[p] = x
        low[p] = x
    }

    private fun push(p: Int, l: Int, r: Int) {
        if (r - l <= 1) return
        val m = l + (r - l) / 2
        val a = 2 * p
        val b = a + 1
        if (pendingAdd[p] != 0L) {
            val d = pendingAdd[p]
            applyAdd(a, m - l, d)
            applyAdd(b, r - m, d)
            pendingAdd[p] = 0L
        }
        if (high[a] > high[p]) applyChmin(a, high[p])
        if (high[b] > high[p]) applyChmin(b, high[p])
        if (low[a] < low[p]) applyChmax(a, low[p])
        if (low[b] < low[p]) applyChmax(b, low[p])
    }

    private fun validate(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid range [$from,$until) for size $size"
        }
    }

    fun add(from: Int, until: Int, delta: Long) {
        validate(from, until)
        if (from < until && delta != 0L) addRec(1, 0, size, from, until, delta)
    }

    private fun addRec(p: Int, l: Int, r: Int, from: Int, until: Int, x: Long) {
        if (from <= l && r <= until) {
            applyAdd(p, r - l, x)
            return
        }
        push(p, l, r)
        val m = l + (r - l) / 2
        if (from < m) addRec(2 * p, l, m, from, until, x)
        if (until > m) addRec(2 * p + 1, m, r, from, until, x)
        pull(p)
    }

    fun chmin(from: Int, until: Int, ceiling: Long) {
        validate(from, until)
        require(ceiling != Long.MIN_VALUE) { "Long.MIN_VALUE is a reserved sentinel" }
        if (from < until) chminRec(1, 0, size, from, until, ceiling)
    }

    private fun chminRec(p: Int, l: Int, r: Int, from: Int, until: Int, x: Long) {
        if (x >= high[p]) return
        if (from <= l && r <= until && x > high2[p]) {
            applyChmin(p, x)
            return
        }
        push(p, l, r)
        val m = l + (r - l) / 2
        if (from < m) chminRec(2 * p, l, m, from, until, x)
        if (until > m) chminRec(2 * p + 1, m, r, from, until, x)
        pull(p)
    }

    fun chmax(from: Int, until: Int, floor: Long) {
        validate(from, until)
        require(floor != Long.MAX_VALUE) { "Long.MAX_VALUE is a reserved sentinel" }
        if (from < until) chmaxRec(1, 0, size, from, until, floor)
    }

    private fun chmaxRec(p: Int, l: Int, r: Int, from: Int, until: Int, x: Long) {
        if (x <= low[p]) return
        if (from <= l && r <= until && x < low2[p]) {
            applyChmax(p, x)
            return
        }
        push(p, l, r)
        val m = l + (r - l) / 2
        if (from < m) chmaxRec(2 * p, l, m, from, until, x)
        if (until > m) chmaxRec(2 * p + 1, m, r, from, until, x)
        pull(p)
    }

    /** Range assignment, expressed as a ceiling followed by a floor. */
    fun assign(from: Int, until: Int, value: Long) {
        chmin(from, until, value)
        chmax(from, until, value)
    }

    fun sum(from: Int, until: Int): Long {
        validate(from, until)
        return if (from == until) 0L else sumRec(1, 0, size, from, until)
    }

    private fun sumRec(p: Int, l: Int, r: Int, from: Int, until: Int): Long {
        if (from <= l && r <= until) return total[p]
        push(p, l, r)
        val m = l + (r - l) / 2
        var result = 0L
        if (from < m) result += sumRec(2 * p, l, m, from, until)
        if (until > m) result += sumRec(2 * p + 1, m, r, from, until)
        return result
    }

    fun min(from: Int, until: Int): Long {
        validate(from, until)
        return if (from == until) Long.MAX_VALUE else minRec(1, 0, size, from, until)
    }

    private fun minRec(p: Int, l: Int, r: Int, from: Int, until: Int): Long {
        if (from <= l && r <= until) return low[p]
        push(p, l, r)
        val m = l + (r - l) / 2
        var answer = Long.MAX_VALUE
        if (from < m) answer = minOf(answer, minRec(2 * p, l, m, from, until))
        if (until > m) answer = minOf(answer, minRec(2 * p + 1, m, r, from, until))
        return answer
    }

    fun max(from: Int, until: Int): Long {
        validate(from, until)
        return if (from == until) Long.MIN_VALUE else maxRec(1, 0, size, from, until)
    }

    private fun maxRec(p: Int, l: Int, r: Int, from: Int, until: Int): Long {
        if (from <= l && r <= until) return high[p]
        push(p, l, r)
        val m = l + (r - l) / 2
        var answer = Long.MIN_VALUE
        if (from < m) answer = maxOf(answer, maxRec(2 * p, l, m, from, until))
        if (until > m) answer = maxOf(answer, maxRec(2 * p + 1, m, r, from, until))
        return answer
    }

    fun allSum(): Long = if (size == 0) 0L else total[1]
}

fun LongArray.beatsTree(): LongBeatsTree = LongBeatsTree(this)
fun IntArray.beatsTree(): LongBeatsTree =
    LongBeatsTree(LongArray(size) { this[it].toLong() })
