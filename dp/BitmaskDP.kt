// Bitmask DP helpers

// TSP exact solution using bitmask DP (O(n^2 * 2^n))
// dist[i][j] = cost from i to j; returns minimum Hamiltonian cycle starting at 0
fun tsp(dist: Array<LongArray>): Long {
    val n = dist.size
    val INF = Long.MAX_VALUE / 2
    val full = (1 shl n) - 1
    val dp = Array(1 shl n) { LongArray(n) { INF } }
    dp[1][0] = 0L
    for (mask in 1..full) {
        for (u in 0 until n) {
            if (dp[mask][u] == INF) continue
            if (mask and (1 shl u) == 0) continue
            for (v in 0 until n) {
                if (mask and (1 shl v) != 0) continue
                val newMask = mask or (1 shl v)
                val newCost = dp[mask][u] + dist[u][v]
                if (newCost < dp[newMask][v]) dp[newMask][v] = newCost
            }
        }
    }
    var ans = INF
    for (u in 1 until n) {
        if (dp[full][u] != INF && dist[u][0] != INF) ans = minOf(ans, dp[full][u] + dist[u][0])
    }
    return ans
}

// SOS (Sum over Subsets) DP
// Returns f[mask] = sum of a[sub] for all sub that are submasks of mask
fun sos(a: LongArray): LongArray {
    val n = Integer.numberOfTrailingZeros(Integer.highestOneBit(a.size)) + 1
    return sosDp(a, n)
}

fun sosDp(a: LongArray, n: Int): LongArray {
    val f = a.copyOf()
    for (i in 0 until n) {
        for (mask in f.indices) {
            if (mask and (1 shl i) != 0) f[mask] += f[mask xor (1 shl i)]
        }
    }
    return f
}
