fun main() {
    val dp = divideConquerPartitionRow(longArrayOf(0L,2L,6L)) { i, j ->
        (j-i).toLong()
    }
    check(dp.values[2] == 2L)
    val hull = monotoneMinHull {
        add(3,0)
        add(1,4)
    }
    check(hull.query(10) == 14L)
    check(subtractionGrundy(4,intArrayOf(1))[4] == 0)
    check(retrogradeGame(2,listOf(0 to 1)).contentEquals(
        arrayOf(GameOutcome.WIN,GameOutcome.LOSE)))
    println("standalone DP/game theory OK")
}
