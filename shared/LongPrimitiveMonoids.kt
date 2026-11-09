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

