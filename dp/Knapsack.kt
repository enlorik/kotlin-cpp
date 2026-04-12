// Knapsack DP variants

fun knapsack01(weights: IntArray, values: LongArray, capacity: Int): Long {
    val dp = LongArray(capacity + 1)
    for (i in weights.indices) {
        for (w in capacity downTo weights[i]) {
            dp[w] = maxOf(dp[w], dp[w - weights[i]] + values[i])
        }
    }
    return dp[capacity]
}

fun knapsack01Items(weights: IntArray, values: LongArray, capacity: Int): List<Int> {
    val n = weights.size
    val dp = Array(n + 1) { LongArray(capacity + 1) }
    for (i in 1..n) {
        for (w in 0..capacity) {
            dp[i][w] = dp[i - 1][w]
            if (weights[i - 1] <= w) dp[i][w] = maxOf(dp[i][w], dp[i - 1][w - weights[i - 1]] + values[i - 1])
        }
    }
    val items = mutableListOf<Int>()
    var w = capacity
    for (i in n downTo 1) {
        if (dp[i][w] != dp[i - 1][w]) { items.add(i - 1); w -= weights[i - 1] }
    }
    return items.reversed()
}

fun knapsackUnbounded(weights: IntArray, values: LongArray, capacity: Int): Long {
    val dp = LongArray(capacity + 1)
    for (w in 1..capacity) {
        for (i in weights.indices) {
            if (weights[i] <= w) dp[w] = maxOf(dp[w], dp[w - weights[i]] + values[i])
        }
    }
    return dp[capacity]
}

fun knapsackBounded(weights: IntArray, values: LongArray, counts: IntArray, capacity: Int): Long {
    val dp = LongArray(capacity + 1)
    for (i in weights.indices) {
        var cnt = counts[i]; var k = 1
        while (cnt > 0) {
            val take = minOf(k, cnt); cnt -= take
            val bw = weights[i] * take; val bv = values[i] * take
            for (w in capacity downTo bw) dp[w] = maxOf(dp[w], dp[w - bw] + bv)
            k *= 2
        }
    }
    return dp[capacity]
}
