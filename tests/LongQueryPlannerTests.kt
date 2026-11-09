fun main() {
    val source = longArrayOf(2, 4, 6, 8)
    val staticSum = source.optimize {
        aggregate(LongSumMonoid)
        support { rangeQuery() }
    }
    check(staticSum.strategy == PlannedLongStrategy.STATIC_PREFIX)
    check(staticSum.query(1, 3) == 10L)

    val min = source.optimize {
        aggregate(LongMinMonoid)
        support { rangeQuery() }
    }
    check(min.strategy == PlannedLongStrategy.STATIC_SPARSE)
    check(min.query(0, 4) == 2L)

    val xor = source.optimize {
        aggregate(LongXorMonoid)
        support { rangeQuery(); pointSet() }
    }
    check(xor.strategy == PlannedLongStrategy.POINT_SEGMENT)
    xor.set(0, 10)
    check(xor.query(0, 4) == (10L xor 4L xor 6L xor 8L))

    val lazy = source.optimize {
        aggregate(LongSumMonoid)
        support { rangeQuery(); rangeAdd(); rangeAssign(); pointSet() }
    }
    check(lazy.strategy == PlannedLongStrategy.LAZY_SUM)
    lazy.add(0, 4, 1L)
    lazy.assign(1, 3, 100L)
    lazy.set(0, 7L)
    check(lazy.query(0, 4) == 216L)
    check(source.contentEquals(longArrayOf(2, 4, 6, 8)))

    val beats = source.optimize {
        aggregate(LongSumMonoid)
        support { rangeQuery(); rangeAdd(); rangeChmin(); rangeChmax(); rangeAssign() }
    }
    check(beats.strategy == PlannedLongStrategy.SEGMENT_TREE_BEATS)
    beats.chmin(0, 4, 5L)
    beats.chmax(0, 2, 3L)
    beats.add(0, 4, 2L)
    check(beats.query(0, 4) == 19L)
    beats.assign(0, 2, 0L)
    check(beats.query(0, 4) == 14L)

    val minBeats = source.optimize {
        aggregate(LongMinMonoid)
        support { rangeQuery(); rangeAdd() }
    }
    check(minBeats.strategy == PlannedLongStrategy.SEGMENT_TREE_BEATS)
    minBeats.add(0, 4, 5L)
    check(minBeats.query(0, 4) == 7L)

    val history = source.optimize {
        aggregate(LongSumMonoid)
        support { rangeQuery(); pointSet(); rangeAdd(); rangeAssign(); historicalVersions() }
    }
    check(history.strategy == PlannedLongStrategy.PERSISTENT_LAZY)
    history.add(0, 4, 10L)
    check(history.currentVersion == 1)
    history.assign(1, 3, 0L)
    check(history.currentVersion == 2)
    check(history.query(0, 0, 4) == 20L)
    check(history.query(1, 0, 4) == 60L)
    check(history.query(2, 0, 4) == 30L)
    history.rollback(0)
    history.set(0, -100)
    check(history.currentVersion == 3)
    check(history.query(3, 0, 4) == -82L)
    check(history.query(2, 0, 4) == 30L)
    val branch = history.branchAdd(1, 0, 1, 5L)
    check(history.query(branch, 0, 4) == 65L)
    check(history.currentVersion == 3)

    // An arbitrary custom monoid takes the generic path, never the built-in one.
    val customSum = monoid(0L) { x: Long, y: Long -> x + y }
    val generic = source.optimize {
        aggregate(customSum)
        support { rangeQuery(); pointSet() }
    }
    check(generic.strategy == PlannedLongStrategy.GENERIC_POINT)
    generic.set(1, 50L)
    check(generic.query(0, 4) == 66L)

    check(runCatching {
        source.optimize {
            aggregate(LongGcdMonoid)
            support { rangeQuery(); rangeAdd() }
        }
    }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching {
        source.optimize {
            aggregate(LongXorMonoid)
            support { rangeQuery(); rangeChmin() }
        }
    }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching {
        source.optimize {
            aggregate(LongMinMonoid)
            support { rangeQuery(); historicalVersions() }
        }
    }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching {
        source.optimize {
            aggregate(LongSumMonoid)
            support { rangeAdd() }
        }
    }.exceptionOrNull() is IllegalArgumentException)
    check(runCatching { staticSum.add(0, 2, 3L) }
        .exceptionOrNull() is IllegalStateException)

    println("PASS: declarative query planner backend selection and operation contracts")
}
