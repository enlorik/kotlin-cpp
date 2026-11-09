fun main() {
    val static = longArrayOf(4, 3, 5).optimize {
        aggregate(LongMinMonoid)
        support { rangeQuery() }
    }
    check(static.query(0, 3) == 3L)
    val planned = longArrayOf(1, 2, 3).optimize {
        aggregate(LongSumMonoid)
        support { rangeQuery(); rangeAdd(); rangeAssign() }
    }
    planned.add(0, 3, 3L)
    planned.assign(1, 2, 0L)
    check(planned.query(0, 3) == 10L)

    val historical = longArrayOf(1, 2, 3).optimize {
        aggregate(LongSumMonoid)
        support { rangeQuery(); rangeAdd(); historicalVersions() }
    }
    historical.add(0, 3, 5L)
    check(historical.query(0, 0, 3) == 6L)
    check(historical.query(1, 0, 3) == 21L)

    val beats = longArrayOf(2, 7, 3).optimize {
        aggregate(LongSumMonoid)
        support { rangeQuery(); rangeChmin(); rangeChmax() }
    }
    beats.chmin(0, 3, 4)
    beats.chmax(0, 2, 3)
    check(beats.query(0, 3) == 10L)
    println("standalone planner/persistence/beats OK")
}
