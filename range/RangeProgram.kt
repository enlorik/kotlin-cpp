/**
 * A tiny deferred language for ordered range queries and point assignments.
 *
 * `query` writes one answer in query order; `set` changes later queries.
 * Commands are recorded first so the existing RangeIndex can choose a static
 * sparse-table strategy when there are no writes, otherwise a segment tree.
 *
 * The block is a description, not immediate execution: query() returns Unit.
 * Use rangeProgram(...).answers to inspect the computed answers afterward.
 */
@DslMarker
annotation class RangeProgramDsl

internal sealed interface RangeProgramStep<T> {
    class Query<T>(val from: Int, val until: Int) : RangeProgramStep<T>
    class Set<T>(val index: Int, val value: T) : RangeProgramStep<T>
}

/** All query outputs in their original order, plus the chosen data structure. */
data class RangeProgramResult<T>(
    val answers: List<T>,
    val strategy: RangeIndexStrategy
) {
    operator fun get(queryIndex: Int): T = answers[queryIndex]
}

/**
 * Declarative command recorder for a single `rangeProgram` invocation.
 *
 * Bounds are validated when commands are recorded. Only point assignments
 * are supported; arbitrary lazy range updates need a compatible action.
 */
@RangeProgramDsl
class RangeProgramScope<T> internal constructor(private val size: Int) {
    private val steps = ArrayList<RangeProgramStep<T>>()
    private var hasAssignments = false

    /** Add a query over the half-open range [from, until). */
    fun query(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid half-open range [$from,$until) for size $size"
        }
        steps.add(RangeProgramStep.Query(from, until))
    }

    /** Add a query over an inclusive Kotlin range (e.g. 1..3). */
    fun query(indices: IntRange) {
        val until = indices.last.toLong() + 1L
        require(until <= size.toLong()) { "Range end out of bounds for size $size" }
        query(indices.first, until.toInt())
    }

    /** Assign one index for all subsequent queries. */
    fun set(index: Int, value: T) {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        hasAssignments = true
        steps.add(RangeProgramStep.Set(index, value))
    }

    internal fun execute(snapshot: List<T>, operation: AggregateOp<T>): RangeProgramResult<T> {
        val index = snapshot.rangeIndex(operation, mutable = hasAssignments)
        val answers = ArrayList<T>()
        for (step in steps) {
            when (step) {
                is RangeProgramStep.Query -> answers.add(index.query(step.from, step.until))
                is RangeProgramStep.Set -> index.set(step.index, step.value)
            }
        }
        return RangeProgramResult(answers, index.strategy)
    }
}

/** Snapshot the input before recording, then execute the commands in order. */
fun <T> List<T>.rangeProgram(
    operation: AggregateOp<T>,
    block: RangeProgramScope<T>.() -> Unit
): RangeProgramResult<T> {
    val snapshot = toList()
    val scope = RangeProgramScope<T>(snapshot.size)
    scope.block()
    return scope.execute(snapshot, operation)
}

/** Typed monoids preserve their idempotency when selecting a static backend. */
fun <T> List<T>.rangeProgram(
    operation: Monoid<T>,
    block: RangeProgramScope<T>.() -> Unit
): RangeProgramResult<T> = rangeProgram(operation.asAggregateOp(), block)

fun <T> Array<out T>.rangeProgram(
    operation: AggregateOp<T>,
    block: RangeProgramScope<T>.() -> Unit
): RangeProgramResult<T> = asList().rangeProgram(operation, block)

fun <T> Array<out T>.rangeProgram(
    operation: Monoid<T>,
    block: RangeProgramScope<T>.() -> Unit
): RangeProgramResult<T> = asList().rangeProgram(operation, block)

fun IntArray.rangeProgram(
    operation: AggregateOp<Int>,
    block: RangeProgramScope<Int>.() -> Unit
): RangeProgramResult<Int> = asList().rangeProgram(operation, block)

fun IntArray.rangeProgram(
    operation: Monoid<Int>,
    block: RangeProgramScope<Int>.() -> Unit
): RangeProgramResult<Int> = asList().rangeProgram(operation, block)

fun LongArray.rangeProgram(
    operation: AggregateOp<Long>,
    block: RangeProgramScope<Long>.() -> Unit
): RangeProgramResult<Long> = asList().rangeProgram(operation, block)

fun LongArray.rangeProgram(
    operation: Monoid<Long>,
    block: RangeProgramScope<Long>.() -> Unit
): RangeProgramResult<Long> = asList().rangeProgram(operation, block)
