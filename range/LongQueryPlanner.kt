/**
 * Capability-directed planner: select a lawful index from the operations a
 * program declares before running its queries. No lambda-body inspection.
 */
enum class PlannedLongStrategy {
    STATIC_PREFIX, STATIC_SPARSE, POINT_SEGMENT,
    GENERIC_STATIC, GENERIC_POINT,
    LAZY_SUM, SEGMENT_TREE_BEATS, PERSISTENT_LAZY
}

@DslMarker
annotation class LongQueryDsl

@LongQueryDsl
class LongQueryRequirements {
    internal var wantsQuery = false
    internal var points = false
    internal var adds = false
    internal var assigns = false
    internal var ceilings = false
    internal var floors = false
    internal var history = false

    fun rangeQuery() { wantsQuery = true }
    fun pointSet() { points = true }
    fun rangeAdd() { adds = true }
    fun rangeAssign() { assigns = true }
    fun rangeChmin() { ceilings = true }
    fun rangeChmax() { floors = true }
    fun historicalVersions() { history = true }
}

@LongQueryDsl
class LongQueryPlanner internal constructor(private val values: LongArray) {
    private var aggregate: Monoid<Long>? = null
    private val needs = LongQueryRequirements()
    private var supportsDeclared = false

    fun aggregate(monoid: Monoid<Long>) {
        check(aggregate == null) { "Declare aggregate once" }
        aggregate = monoid
    }

    fun support(block: LongQueryRequirements.() -> Unit) {
        supportsDeclared = true
        needs.block()
    }

    internal fun create(): PlannedLongIndex {
        val op = requireNotNull(aggregate) { "Declare aggregate(monoid)" }
        require(supportsDeclared && needs.wantsQuery) {
            "Declare support { rangeQuery(); ... }"
        }
        val builtIn = op as? LongBuiltInMonoid
        val hasRangeOps = needs.adds || needs.assigns || needs.ceilings || needs.floors

        if (needs.history) {
            require(op === LongSumMonoid) {
                "Historical range updates currently require LongSumMonoid"
            }
            require(!needs.ceilings && !needs.floors) {
                "Persistent chmin/chmax is not supported"
            }
            return PlannedLongIndex(
                PlannedLongStrategy.PERSISTENT_LAZY, needs, op,
                historical = values.versionedLongSum()
            )
        }

        if (needs.ceilings || needs.floors) {
            require(builtIn === LongSumMonoid ||
                    builtIn === LongMinMonoid || builtIn === LongMaxMonoid) {
                "Segment Tree Beats supports SUM/MIN/MAX, not GCD/XOR or arbitrary monoids"
            }
            return PlannedLongIndex(
                PlannedLongStrategy.SEGMENT_TREE_BEATS, needs, op,
                beats = values.beatsTree()
            )
        }

        if (hasRangeOps) {
            require(builtIn === LongSumMonoid ||
                    builtIn === LongMinMonoid || builtIn === LongMaxMonoid) {
                "Range additions/assignments require a SUM/MIN/MAX built-in; " +
                    "use rangeIndex(op, action) for custom lazy algebra"
            }
            if (builtIn === LongSumMonoid) {
                return PlannedLongIndex(PlannedLongStrategy.LAZY_SUM, needs, op,
                    lazy = values.longSumLazyTree())
            }
            return PlannedLongIndex(PlannedLongStrategy.SEGMENT_TREE_BEATS, needs, op,
                beats = values.beatsTree())
        }

        if (builtIn != null) {
            val index = values.rangeIndex(builtIn, mutable = needs.points)
            val strategy = when (index.strategy) {
                LongPrimitiveStrategy.STATIC_PREFIX -> PlannedLongStrategy.STATIC_PREFIX
                LongPrimitiveStrategy.STATIC_SPARSE -> PlannedLongStrategy.STATIC_SPARSE
                LongPrimitiveStrategy.POINT_SEGMENT -> PlannedLongStrategy.POINT_SEGMENT
            }
            return PlannedLongIndex(strategy, needs, op, primitive = index)
        }
        val index = values.asList().rangeIndex(op, mutable = needs.points)
        return PlannedLongIndex(
            if (needs.points) PlannedLongStrategy.GENERIC_POINT
            else PlannedLongStrategy.GENERIC_STATIC,
            needs, op, generic = index
        )
    }
}

/** One stable interface, chosen from declared algebraic/query capabilities. */
class PlannedLongIndex internal constructor(
    val strategy: PlannedLongStrategy,
    private val requirements: LongQueryRequirements,
    private val operation: Monoid<Long>,
    private val primitive: LongPrimitiveRangeIndex? = null,
    private val generic: RangeIndex<Long>? = null,
    private val lazy: LongSumLazyTree? = null,
    private val beats: LongBeatsTree? = null,
    private val historical: VersionedLongSum? = null
) {
    val size: Int get() = when {
        primitive != null -> primitive.size
        generic != null -> generic.size
        lazy != null -> lazy.size
        beats != null -> beats.size
        else -> historical!!.size
    }

    private fun requireCapability(enabled: Boolean, label: String) {
        check(enabled) { "Declare support { $label() } before using this operation" }
    }

    fun query(from: Int, until: Int): Long = when {
        primitive != null -> primitive.query(from, until)
        generic != null -> generic.query(from, until)
        lazy != null -> lazy.query(from, until)
        beats != null -> when (operation) {
            LongMinMonoid -> beats.min(from, until)
            LongMaxMonoid -> beats.max(from, until)
            else -> beats.sum(from, until)
        }
        else -> historical!!.query(from, until)
    }

    fun query(version: Int, from: Int, until: Int): Long {
        requireCapability(requirements.history, "historicalVersions")
        return historical!!.query(version, from, until)
    }

    fun all(): Long = query(0, size)

    fun set(index: Int, value: Long) {
        requireCapability(requirements.points, "pointSet")
        require(index in 0 until size) { "Index $index out of bounds" }
        when {
            primitive != null -> primitive.set(index, value)
            generic != null -> generic.set(index, value)
            lazy != null -> lazy.assign(index, index + 1, value)
            beats != null -> beats.assign(index, index + 1, value)
            else -> historical!!.set(index, value)
        }
    }

    fun add(from: Int, until: Int, delta: Long) {
        requireCapability(requirements.adds, "rangeAdd")
        when {
            lazy != null -> lazy.add(from, until, delta)
            beats != null -> beats.add(from, until, delta)
            else -> historical!!.add(from, until, delta)
        }
    }

    fun assign(from: Int, until: Int, value: Long) {
        requireCapability(requirements.assigns, "rangeAssign")
        when {
            lazy != null -> lazy.assign(from, until, value)
            beats != null -> beats.assign(from, until, value)
            else -> historical!!.assign(from, until, value)
        }
    }

    fun chmin(from: Int, until: Int, ceiling: Long) {
        requireCapability(requirements.ceilings, "rangeChmin")
        beats!!.chmin(from, until, ceiling)
    }

    fun chmax(from: Int, until: Int, floor: Long) {
        requireCapability(requirements.floors, "rangeChmax")
        beats!!.chmax(from, until, floor)
    }

    val currentVersion: Int get() {
        requireCapability(requirements.history, "historicalVersions")
        return historical!!.currentVersion
    }

    val versionCount: Int get() {
        requireCapability(requirements.history, "historicalVersions")
        return historical!!.versionCount
    }

    fun checkout(version: Int) {
        requireCapability(requirements.history, "historicalVersions")
        historical!!.checkout(version)
    }

    fun rollback(version: Int) = checkout(version)

    /** Append a branch from any older version without switching currentVersion. */
    fun branchAdd(version: Int, from: Int, until: Int, delta: Long): Int {
        requireCapability(requirements.history && requirements.adds, "historicalVersions + rangeAdd")
        return historical!!.add(version, from, until, delta)
    }

    fun branchAssign(version: Int, from: Int, until: Int, value: Long): Int {
        requireCapability(requirements.history && requirements.assigns, "historicalVersions + rangeAssign")
        return historical!!.assign(version, from, until, value)
    }
}

/**
 * Example:
 * val index = data.optimize {
 *     aggregate(LongSumMonoid)
 *     support { rangeQuery(); rangeAdd(); rangeAssign() }
 * }
 */
fun LongArray.optimize(block: LongQueryPlanner.() -> Unit): PlannedLongIndex =
    LongQueryPlanner(this).apply(block).create()

fun IntArray.optimize(block: LongQueryPlanner.() -> Unit): PlannedLongIndex =
    LongArray(size) { this[it].toLong() }.optimize(block)
