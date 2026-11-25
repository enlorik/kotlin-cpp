/** Binomial coefficients modulo a prime p, with factorials up to maxN < p.
 * Construction O(maxN + log p), space O(maxN), choose O(1).
 * Prime validation uses the existing deterministic Miller-Rabin predicate.
 */
class PrimeCombinatorics(val prime: Int, val maxN: Int) {
    init {
        require(prime >= 2 && isPrime64(prime.toLong())) { "Modulus must be prime" }
        require(maxN in 0 until prime) { "Factorial table must stop below the prime" }
    }
    private val fact = IntArray(maxN+1)
    private val inverseFact = IntArray(maxN+1)
    private fun multiply(a: Int,b: Int): Int = (a.toLong()*b % prime).toInt()
    init {
        fact[0] = 1
        for (i in 1..maxN) fact[i] = multiply(fact[i-1],i)
        inverseFact[maxN] = modPower64(fact[maxN].toLong(),prime.toLong()-2,prime.toLong()).toInt()
        for (i in maxN downTo 1) inverseFact[i-1] = multiply(inverseFact[i],i)
    }
    fun choose(n: Int,k: Int): Int {
        require(n in 0..maxN)
        if (k < 0 || k > n) return 0
        return multiply(multiply(fact[n],inverseFact[k]),inverseFact[n-k])
    }
    /** Lucas digit decomposition for nonnegative Long n.
     * Requires the complete table through p-1; O(log_p n) time.
     * Prefer a bounded factorial table when p is large.
     */
    fun lucas(n: Long,k: Long): Int {
        require(n >= 0)
        require(maxN == prime-1) { "Lucas requires a complete prime-sized table" }
        if (k < 0 || k > n) return 0
        var a = n; var b = k; var result = 1
        while (a > 0 || b > 0) {
            val x = (a % prime).toInt()
            val y = (b % prime).toInt()
            if (y > x) return 0
            result = multiply(result,choose(x,y))
            a /= prime; b /= prime
        }
        return result
    }
}
