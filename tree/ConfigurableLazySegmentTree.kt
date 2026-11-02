/**
 * An update action on a monoid: applying an update to an interval aggregate
 * and composing older and newer updates must preserve the aggregation.
 * `compose(old, new)` represents applying old BEFORE new.
 */
class RangeAction<T, U>(
    val apply: (aggregate: T, update: U, length: Int) -> T,
    val compose: (old: U, new: U) -> U
)

/**
 * Generic lazy segment tree. O(n) build, O(log n) range update/query,
 * O(n) memory, provided the supplied monoid action is O(1).
 */
class LazyAggregateTree<T, U>(values: List<T>, val op: AggregateOp<T>, val action: RangeAction<T, U>) {
    val size: Int = values.size
    private val tree: Array<Any?>
    private val pending: Array<Any?>
    private val hasPending: BooleanArray

    init {
        require(size <= (1 shl 28)) { "Array-backed tree size is too large" }
        val capacity = maxOf(4, size * 4)
        tree = arrayOfNulls(capacity)
        pending = arrayOfNulls(capacity)
        hasPending = BooleanArray(capacity)
        if (size > 0) build(1, 0, size, values)
    }

    @Suppress("UNCHECKED_CAST")
    private fun value(node: Int): T = tree[node] as T
    @Suppress("UNCHECKED_CAST")
    private fun delayed(node: Int): U = pending[node] as U

    private fun build(node: Int, lo: Int, hi: Int, values: List<T>) {
        if (hi - lo == 1) { tree[node] = values[lo]; return }
        val mid = lo + (hi - lo) / 2
        build(node * 2, lo, mid, values)
        build(node * 2 + 1, mid, hi, values)
        tree[node] = op.combine(value(node * 2), value(node * 2 + 1))
    }

    private fun applyNode(node: Int, length: Int, update: U) {
        tree[node] = action.apply(value(node), update, length)
        if (hasPending[node]) pending[node] = action.compose(delayed(node), update)
        else {
            hasPending[node] = true
            pending[node] = update
        }
    }

    private fun push(node: Int, lo: Int, hi: Int) {
        if (!hasPending[node] || hi - lo <= 1) return
        val mid = lo + (hi - lo) / 2
        val update = delayed(node)
        applyNode(node * 2, mid - lo, update)
        applyNode(node * 2 + 1, hi - mid, update)
        pending[node] = null
        hasPending[node] = false
    }

    private fun checkRange(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
    }

    fun update(from: Int, until: Int, change: U) {
        checkRange(from, until)
        if (from != until) updateRec(1, 0, size, from, until, change)
    }

    private fun updateRec(node: Int, lo: Int, hi: Int, from: Int, until: Int, change: U) {
        if (from <= lo && hi <= until) { applyNode(node, hi - lo, change); return }
        push(node, lo, hi)
        val mid = lo + (hi - lo) / 2
        if (from < mid) updateRec(node * 2, lo, mid, from, until, change)
        if (until > mid) updateRec(node * 2 + 1, mid, hi, from, until, change)
        tree[node] = op.combine(value(node * 2), value(node * 2 + 1))
    }

    fun query(from: Int, until: Int): T {
        checkRange(from, until)
        return if (from == until) op.initial else queryRec(1, 0, size, from, until)
    }

    private fun queryRec(node: Int, lo: Int, hi: Int, from: Int, until: Int): T {
        if (from <= lo && hi <= until) return value(node)
        push(node, lo, hi)
        val mid = lo + (hi - lo) / 2
        if (until <= mid) return queryRec(node * 2, lo, mid, from, until)
        if (from >= mid) return queryRec(node * 2 + 1, mid, hi, from, until)
        return op.combine(queryRec(node * 2, lo, mid, from, until),
            queryRec(node * 2 + 1, mid, hi, from, until))
    }

    fun all(): T = if (size == 0) op.initial else value(1)
}

/** Declare valid update semantics for a custom aggregate; no silent guessing. */
fun <T, U> List<T>.lazySegmentTree(op: AggregateOp<T>, action: RangeAction<T, U>): LazyAggregateTree<T, U> =
    LazyAggregateTree(this, op, action)

/** Built-in range update modes, checked against each aggregation. */
enum class RangeUpdates { ADD, ASSIGN, ADD_ASSIGN }

internal sealed class NumericChange<T> {
    data class Add<T>(val delta: T) : NumericChange<T>()
    data class Assign<T>(val value: T) : NumericChange<T>()
}

class NumericLazySegmentTree<T> internal constructor(
    private val backing: LazyAggregateTree<T, NumericChange<T>>,
    val updates: RangeUpdates
) {
    val size: Int get() = backing.size
    fun query(from: Int, until: Int): T = backing.query(from, until)
    fun all(): T = backing.all()
    fun add(from: Int, until: Int, delta: T) {
        require(updates != RangeUpdates.ASSIGN) { "This tree only supports ASSIGN updates" }
        backing.update(from, until, NumericChange.Add(delta))
    }
    fun set(from: Int, until: Int, value: T) {
        require(updates != RangeUpdates.ADD) { "This tree only supports ADD updates" }
        backing.update(from, until, NumericChange.Assign(value))
    }
}

class IntLazyTreeConfig {
    var aggregate: AggregateOp<Int> = IntAggregations.SUM
    var updates: RangeUpdates = RangeUpdates.ADD_ASSIGN
}

class LongLazyTreeConfig {
    var aggregate: AggregateOp<Long> = LongAggregations.SUM
    var updates: RangeUpdates = RangeUpdates.ADD_ASSIGN
}

private fun <T> buildNumericTree(
    values: List<T>, op: AggregateOp<T>, mode: RangeUpdates,
    sum: AggregateOp<T>, min: AggregateOp<T>, max: AggregateOp<T>,
    xor: AggregateOp<T>, or: AggregateOp<T>, and: AggregateOp<T>,
    plus: (T, T) -> T, times: (T, Int) -> T, zero: T
): NumericLazySegmentTree<T> {
    val sumMode = op === sum
    val minOrMax = op === min || op === max
    val bitwise = op === xor || op === or || op === and
    require(sumMode || minOrMax || bitwise) {
        "Custom aggregation requires an explicit RangeAction (List.lazySegmentTree(op, action))"
    }
    require(!bitwise || mode == RangeUpdates.ASSIGN) {
        "Bitwise OR/AND/XOR support ASSIGN only; ADD does not preserve their interval aggregates"
    }
    val action = RangeAction<T, NumericChange<T>>(
        apply = { aggregate, change, length ->
            when (change) {
                is NumericChange.Add -> {
                    require(sumMode || minOrMax) { "ADD is unsupported for this aggregate" }
                    plus(aggregate, if (sumMode) times(change.delta, length) else change.delta)
                }
                is NumericChange.Assign -> when {
                    sumMode -> times(change.value, length)
                    minOrMax || op === or || op === and -> change.value
                    else -> if ((length and 1) == 0) zero else change.value
                }
            }
        },
        compose = { old, newer ->
            when (newer) {
                is NumericChange.Assign -> newer
                is NumericChange.Add -> when (old) {
                    is NumericChange.Assign -> NumericChange.Assign(plus(old.value, newer.delta))
                    is NumericChange.Add -> NumericChange.Add(plus(old.delta, newer.delta))
                }
            }
        }
    )
    return NumericLazySegmentTree(LazyAggregateTree(values, op, action), mode)
}

/**
 * Example: a.lazySegmentTree { aggregate = IntAggregations.MIN; updates = RangeUpdates.ADD }
 * Existing a.lazySegmentTree() (Long SUM) remains unchanged.
 */
fun IntArray.lazySegmentTree(config: IntLazyTreeConfig.() -> Unit): NumericLazySegmentTree<Int> {
    val c = IntLazyTreeConfig().apply(config)
    return buildNumericTree(asList(), c.aggregate, c.updates,
        IntAggregations.SUM, IntAggregations.MIN, IntAggregations.MAX,
        IntAggregations.XOR, IntAggregations.OR, IntAggregations.AND,
        { a, b -> a + b }, { value, length -> value * length }, 0)
}

fun LongArray.lazySegmentTree(config: LongLazyTreeConfig.() -> Unit): NumericLazySegmentTree<Long> {
    val c = LongLazyTreeConfig().apply(config)
    return buildNumericTree(asList(), c.aggregate, c.updates,
        LongAggregations.SUM, LongAggregations.MIN, LongAggregations.MAX,
        LongAggregations.XOR, LongAggregations.OR, LongAggregations.AND,
        { a, b -> a + b }, { value, length -> value * length }, 0L)
}
