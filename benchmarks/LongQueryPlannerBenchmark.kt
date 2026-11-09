import kotlin.random.Random

/**
 * Reproducible online-range workloads comparing the planned primitive
 * backend against lawful generic monoid/action trees. Checksums must agree.
 * A configurable severe-slowdown threshold catches regressions without
 * pretending cloud-runner latency is a stable absolute performance budget.
 */
private class BenchmarkTrace(val n: Int = 50_000, val q: Int = 65_000) {
    val initial = LongArray(n)
    val type = IntArray(q)
    val lo = IntArray(q)
    val hi = IntArray(q)
    val value = LongArray(q)
    init {
        val random = Random(77371)
        for (i in 0 until n) initial[i] = random.nextLong(-100L, 101L)
        repeat(q) { i ->
            type[i] = if (random.nextInt(3) == 0) 1 else 0
            lo[i] = random.nextInt(n)
            hi[i] = random.nextInt(lo[i] + 1, n + 1)
            value[i] = random.nextLong(-1000L, 1001L)
        }
    }
}

private fun benchmarkPoint(trace: BenchmarkTrace, optimized: Boolean): Long {
    var fingerprint = 1L
    if (optimized) {
        val idx = trace.initial.optimize {
            aggregate(LongSumMonoid)
            support { rangeQuery(); pointSet() }
        }
        check(idx.strategy == PlannedLongStrategy.POINT_SEGMENT)
        repeat(trace.q) { i ->
            if (trace.type[i] == 1) idx.set(trace.lo[i], trace.value[i])
            else fingerprint = fingerprint * 31L + idx.query(trace.lo[i], trace.hi[i])
        }
    } else {
        val op: Monoid<Long> = LongSumMonoid
        val idx = trace.initial.asList().rangeIndex(op, mutable = true)
        repeat(trace.q) { i ->
            if (trace.type[i] == 1) idx.set(trace.lo[i], trace.value[i])
            else fingerprint = fingerprint * 31L + idx.query(trace.lo[i], trace.hi[i])
        }
    }
    return fingerprint
}

private fun benchmarkLazy(trace: BenchmarkTrace, optimized: Boolean): Long {
    var fingerprint = 1L
    if (optimized) {
        val idx = trace.initial.optimize {
            aggregate(LongSumMonoid)
            support { rangeQuery(); rangeAdd() }
        }
        check(idx.strategy == PlannedLongStrategy.LAZY_SUM)
        repeat(trace.q) { i ->
            if (trace.type[i] == 1) idx.add(trace.lo[i], trace.hi[i], trace.value[i])
            else fingerprint = fingerprint * 31L + idx.query(trace.lo[i], trace.hi[i])
        }
    } else {
        val op: Monoid<Long> = LongSumMonoid
        val act: MonoidAction<Long, Long> = LongAddAction
        val idx = trace.initial.asList().rangeIndex(op, act)
        repeat(trace.q) { i ->
            if (trace.type[i] == 1) idx.update(trace.lo[i], trace.hi[i], trace.value[i])
            else fingerprint = fingerprint * 31L + idx.query(trace.lo[i], trace.hi[i])
        }
    }
    return fingerprint
}

private fun median(a: LongArray): Long = a.sorted()[a.size / 2]

private fun compare(
    label: String,
    maxSlowdown: Double,
    general: () -> Long,
    planned: () -> Long
) {
    // Warm both implementations to reduce JVM tiered-compilation noise.
    check(general() == planned()) { "$label implementations disagree during warmup" }

    val slow = LongArray(3)
    val fast = LongArray(3)
    repeat(3) { i ->
        var start = System.nanoTime()
        val a = general()
        slow[i] = System.nanoTime() - start
        start = System.nanoTime()
        val b = planned()
        fast[i] = System.nanoTime() - start
        check(a == b) { "$label mismatch: generic=$a optimized=$b" }
    }

    val oldMs = median(slow).toDouble() / 1e6
    val newMs = median(fast).toDouble() / 1e6
    val slowdown = newMs / maxOf(oldMs, 0.0001)
    println("$label: generic %.2f ms, planned %.2f ms, ratio %.2fx".format(
        oldMs, newMs, slowdown
    ))
    check(slowdown <= maxSlowdown) {
        "Severe $label regression: planned/generic ratio %.2fx exceeds %.2fx"
            .format(slowdown, maxSlowdown)
    }
}

fun main() {
    val trace = BenchmarkTrace()
    val maxSlowdown = System.getenv("MAX_PLANNER_SLOWDOWN")?.toDoubleOrNull() ?: 4.0
    require(maxSlowdown >= 1.0 && maxSlowdown.isFinite())
    compare("point", maxSlowdown,
        { benchmarkPoint(trace, false) }, { benchmarkPoint(trace, true) })
    compare("lazy-range-add", maxSlowdown,
        { benchmarkLazy(trace, false) }, { benchmarkLazy(trace, true) })
    println("PASS: seeded planner benchmark checksums and severe-slowdown budget")
}
