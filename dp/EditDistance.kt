// Edit Distance and LCS

fun editDistance(s: String, t: String): Int {
    val m = s.length; val n = t.length
    val dp = Array(m + 1) { IntArray(n + 1) }
    for (i in 0..m) dp[i][0] = i
    for (j in 0..n) dp[0][j] = j
    for (i in 1..m) for (j in 1..n) {
        dp[i][j] = if (s[i - 1] == t[j - 1]) dp[i - 1][j - 1]
        else 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
    }
    return dp[m][n]
}

fun lcs(s: String, t: String): Int {
    val m = s.length; val n = t.length
    val dp = Array(m + 1) { IntArray(n + 1) }
    for (i in 1..m) for (j in 1..n) {
        dp[i][j] = if (s[i - 1] == t[j - 1]) dp[i - 1][j - 1] + 1
        else maxOf(dp[i - 1][j], dp[i][j - 1])
    }
    return dp[m][n]
}

fun lcsString(s: String, t: String): String {
    val m = s.length; val n = t.length
    val dp = Array(m + 1) { IntArray(n + 1) }
    for (i in 1..m) for (j in 1..n) {
        dp[i][j] = if (s[i - 1] == t[j - 1]) dp[i - 1][j - 1] + 1
        else maxOf(dp[i - 1][j], dp[i][j - 1])
    }
    val sb = StringBuilder()
    var i = m; var j = n
    while (i > 0 && j > 0) {
        when {
            s[i - 1] == t[j - 1] -> { sb.append(s[i - 1]); i--; j-- }
            dp[i - 1][j] >= dp[i][j - 1] -> i--
            else -> j--
        }
    }
    return sb.reverse().toString()
}
