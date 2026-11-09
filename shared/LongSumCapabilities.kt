/**
 * Named mathematical capabilities permit safe overload resolution to an
 * unboxed Long backend. Arbitrary monoid lambdas are never pattern-matched.
 */
object LongSumMonoid : LongBuiltInMonoid {
    override val reduction: LongReduction = LongReduction.SUM
    override val identity: Long = 0L
    override fun combine(left: Long, right: Long): Long = left + right
}

/** Apply a Long increment to every element of a segment. */
object LongAddAction : MonoidAction<Long, Long> {
    override fun apply(aggregate: Long, update: Long, length: Int): Long =
        aggregate + update * length.toLong()

    override fun compose(old: Long, newer: Long): Long = old + newer
}

/** Replace every element of a segment with one Long value. */
object LongAssignAction : MonoidAction<Long, Long> {
    override fun apply(aggregate: Long, update: Long, length: Int): Long =
        update * length.toLong()

    override fun compose(old: Long, newer: Long): Long = newer
}
