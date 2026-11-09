/**
 * Explicitly named operation/action pairs choose primitive Long storage.
 * This overload never intercepts general Monoid/MonoidAction lambdas.
 */
class LongSumActionIndex internal constructor(
    values: LongArray,
    private val assignmentMode: Boolean
) {
    private val index = LongSumLazyTree(values)
    val size: Int get() = index.size

    fun update(from: Int, until: Int, change: Long) {
        if (assignmentMode) index.assign(from, until, change)
        else index.add(from, until, change)
    }

    fun query(from: Int, until: Int): Long = index.query(from, until)

    fun query(indices: IntRange): Long {
        val until = indices.last.toLong() + 1L
        require(until <= size.toLong()) { "Range end out of bounds" }
        return query(indices.first, until.toInt())
    }

    fun all(): Long = index.all()
}

@Suppress("UNUSED_PARAMETER")
fun LongArray.rangeIndex(op: LongSumMonoid, action: LongAddAction): LongSumActionIndex =
    LongSumActionIndex(this, assignmentMode = false)

@Suppress("UNUSED_PARAMETER")
fun LongArray.rangeIndex(op: LongSumMonoid, action: LongAssignAction): LongSumActionIndex =
    LongSumActionIndex(this, assignmentMode = true)

/** Online DSL whose backend is an unboxed LongArray-based lazy segment tree. */
@DslMarker
annotation class PrimitiveLongRangeDsl

@PrimitiveLongRangeDsl
class OnlineLongSumScope internal constructor(private val index: LongSumActionIndex) {
    val size: Int get() = index.size
    fun query(from: Int, until: Int): Long = index.query(from, until)
    fun query(indices: IntRange): Long = index.query(indices)
    fun update(from: Int, until: Int, change: Long) = index.update(from, until, change)
    fun all(): Long = index.all()
}

/**
 * Named online shortcut avoids Kotlin lambda-receiver overload ambiguity with
 * general onlineRangeProgram. The monoid capability is fixed to LongSumMonoid.
 */
fun <R> LongArray.onlineLongSum(
    action: LongAddAction,
    block: OnlineLongSumScope.() -> R
): R = OnlineLongSumScope(rangeIndex(LongSumMonoid, action)).block()

fun <R> LongArray.onlineLongSum(
    action: LongAssignAction,
    block: OnlineLongSumScope.() -> R
): R = OnlineLongSumScope(rangeIndex(LongSumMonoid, action)).block()
