/**
 * Explicit, built-in Long algebra. The marker is sealed: arbitrary user lambdas
 * cannot accidentally claim an optimized backend. Custom monoids still use
 * the generic rangeIndex/onlineRangeProgram APIs.
 */
sealed interface LongBuiltInMonoid : Monoid<Long> {
    val reduction: LongReduction
}

/** SUM/XOR have O(n) prefix indexes; MIN/MAX/GCD have O(n log n) sparse tables. */
enum class LongReduction { SUM, MIN, MAX, GCD, XOR }

object LongMinMonoid : LongBuiltInMonoid, IdempotentMonoid<Long> {
    override val reduction = LongReduction.MIN
    override val identity = Long.MAX_VALUE
    override fun combine(left: Long, right: Long): Long = minOf(left, right)
}

object LongMaxMonoid : LongBuiltInMonoid, IdempotentMonoid<Long> {
    override val reduction = LongReduction.MAX
    override val identity = Long.MIN_VALUE
    override fun combine(left: Long, right: Long): Long = maxOf(left, right)
}

/**
 * GCD is a monoid and idempotent on NONNEGATIVE Long values only.
 * This contract is checked by the primitive index on build and point update.
 */
object LongGcdMonoid : LongBuiltInMonoid, IdempotentMonoid<Long> {
    override val reduction = LongReduction.GCD
    override val identity = 0L
    override fun combine(left: Long, right: Long): Long {
        require(left >= 0L && right >= 0L) { "LongGcdMonoid requires nonnegative values" }
        var a = left
        var b = right
        while (b != 0L) {
            val r = a % b
            a = b
            b = r
        }
        return a
    }
}

object LongXorMonoid : LongBuiltInMonoid, AbelianGroupSpec<Long> {
    override val reduction = LongReduction.XOR
    override val identity = 0L
    override fun combine(left: Long, right: Long): Long = left xor right
    override fun inverse(value: Long): Long = value
}

/**
 * Noncommutative monoids are NOT automatically routed through this class:
 * the generic rangeIndex preserves left-to-right combination order.
 */
enum class LongPrimitiveStrategy { STATIC_PREFIX, STATIC_SPARSE, POINT_SEGMENT }

/**
 * Unboxed Long values and explicit mathematical selection, O(1) static query
 * or O(log n) mutable point updates/queries. All ranges are [from, until).
 */
class LongPrimitiveRangeIndex internal constructor(
    values: LongArray,
    val operation: LongBuiltInMonoid,
    mutable: Boolean
) {
    val size = values.size
    val strategy = when {
        mutable -> LongPrimitiveStrategy.POINT_SEGMENT
        operation.reduction == LongReduction.SUM || operation.reduction == LongReduction.XOR ->
            LongPrimitiveStrategy.STATIC_PREFIX
        else -> LongPrimitiveStrategy.STATIC_SPARSE
    }

    private val prefix: LongArray?
    private val logs: IntArray?
    private val sparse: Array<LongArray>?
    private val tree: LongArray?
    private val base: Int

    init {
        require(size < (1 shl 29)) { "Array-backed index too large" }
        if (operation.reduction == LongReduction.GCD) {
            require(values.all { it >= 0L }) { "GCD requires nonnegative values" }
        }
        when (strategy) {
            LongPrimitiveStrategy.STATIC_PREFIX -> {
                val p = LongArray(size + 1)
                for (i in 0 until size) {
                    p[i + 1] = combine(p[i], values[i])
                }
                prefix = p
                logs = null
                sparse = null
                tree = null
                base = 0
            }
            LongPrimitiveStrategy.STATIC_SPARSE -> {
                val l = IntArray(size + 1)
                for (i in 2..size) l[i] = l[i / 2] + 1
                val levels = if (size == 0) 0 else l[size] + 1
                val tables = Array(levels) { level ->
                    LongArray(size - (1 shl level) + 1)
                }
                if (size > 0) values.copyInto(tables[0])
                for (level in 1 until levels) {
                    val half = 1 shl (level - 1)
                    for (i in tables[level].indices) {
                        tables[level][i] =
                            combine(tables[level - 1][i], tables[level - 1][i + half])
                    }
                }
                prefix = null
                logs = l
                sparse = tables
                tree = null
                base = 0
            }
            LongPrimitiveStrategy.POINT_SEGMENT -> {
                var power = 1
                while (power < size) power = power shl 1
                val t = LongArray(power * 2) { operation.identity }
                values.copyInto(t, destinationOffset = power)
                for (i in power - 1 downTo 1) t[i] = combine(t[2 * i], t[2 * i + 1])
                prefix = null
                logs = null
                sparse = null
                tree = t
                base = power
            }
        }
    }

    private fun combine(a: Long, b: Long): Long = when (operation.reduction) {
        LongReduction.SUM -> a + b
        LongReduction.MIN -> minOf(a, b)
        LongReduction.MAX -> maxOf(a, b)
        LongReduction.GCD -> {
            var x = a
            var y = b
            while (y != 0L) {
                val rem = x % y
                x = y
                y = rem
            }
            x
        }
        LongReduction.XOR -> a xor b
    }

    fun query(from: Int, until: Int): Long {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid range [$from,$until) for size $size"
        }
        if (from == until) return operation.identity
        return when (strategy) {
            LongPrimitiveStrategy.STATIC_PREFIX -> {
                if (operation.reduction == LongReduction.XOR)
                    prefix!![until] xor prefix[from]
                else prefix!![until] - prefix[from]
            }
            LongPrimitiveStrategy.STATIC_SPARSE -> {
                val level = logs!![until - from]
                combine(sparse!![level][from], sparse[level][until - (1 shl level)])
            }
            LongPrimitiveStrategy.POINT_SEGMENT -> {
                var left = from + base
                var right = until + base
                var accLeft = operation.identity
                var accRight = operation.identity
                val t = tree!!
                while (left < right) {
                    if ((left and 1) != 0) accLeft = combine(accLeft, t[left++])
                    if ((right and 1) != 0) accRight = combine(t[--right], accRight)
                    left /= 2
                    right /= 2
                }
                combine(accLeft, accRight)
            }
        }
    }

    fun set(index: Int, value: Long) {
        check(strategy == LongPrimitiveStrategy.POINT_SEGMENT) {
            "Static index does not support updates; select mutable=true"
        }
        require(index in 0 until size) { "Index $index out of bounds" }
        if (operation.reduction == LongReduction.GCD)
            require(value >= 0L) { "GCD requires nonnegative values" }
        val t = tree!!
        var node = base + index
        t[node] = value
        node /= 2
        while (node > 0) {
            t[node] = combine(t[node * 2], t[node * 2 + 1])
            node /= 2
        }
    }

    fun all(): Long = query(0, size)
}

/**
 * Exact compile-time capability overload. A user-written monoid's static type
 * remains Monoid<Long> and calls the generic version.
 */
fun LongArray.rangeIndex(
    operation: LongBuiltInMonoid,
    mutable: Boolean = false
): LongPrimitiveRangeIndex = LongPrimitiveRangeIndex(this, operation, mutable)

fun IntArray.longRangeIndex(
    operation: LongBuiltInMonoid,
    mutable: Boolean = false
): LongPrimitiveRangeIndex =
    LongPrimitiveRangeIndex(LongArray(size) { this[it].toLong() }, operation, mutable)

/** An online path with an unboxed backend; cannot select static structures. */
@DslMarker
annotation class PrimitiveLongPointDsl

@PrimitiveLongPointDsl
class OnlineLongPointScope internal constructor(private val index: LongPrimitiveRangeIndex) {
    val size: Int get() = index.size
    fun query(from: Int, until: Int): Long = index.query(from, until)
    fun set(index: Int, value: Long) = index.set(index, value)
    fun all(): Long = index.all()
}

fun <R> LongArray.onlineLongPoints(
    operation: LongBuiltInMonoid,
    block: OnlineLongPointScope.() -> R
): R = OnlineLongPointScope(rangeIndex(operation, mutable = true)).block()
