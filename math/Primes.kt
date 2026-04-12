import java.math.BigInteger

/** Returns BooleanArray of size n+1 where isPrime[i] is true iff i is prime. */
fun sieve(n: Int): BooleanArray {
    val isPrime = BooleanArray(n + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= n) {
        if (isPrime[i]) {
            var j = i * i
            while (j <= n) {
                isPrime[j] = false
                j += i
            }
        }
        i++
    }
    return isPrime
}

/**
 * Linear sieve up to n.
 * Returns Pair(primes, minPF) where primes is the list of primes <= n
 * and minPF[i] is the smallest prime factor of i.
 */
fun linearSieve(n: Int): Pair<IntArray, IntArray> {
    val minPF = IntArray(n + 1)
    val primes = mutableListOf<Int>()
    for (i in 2..n) {
        if (minPF[i] == 0) {
            minPF[i] = i
            primes.add(i)
        }
        for (p in primes) {
            if (p > minPF[i] || i.toLong() * p > n) break
            minPF[i * p] = p
        }
    }
    return Pair(primes.toIntArray(), minPF)
}

// --- Miller-Rabin deterministic for n < 3.2e18 ---

private fun mulmod(a: Long, b: Long, mod: Long): Long =
    BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)).mod(BigInteger.valueOf(mod)).toLong()

private fun powmod(base: Long, exp: Long, mod: Long): Long {
    var result = 1L
    var b = base % mod
    var e = exp
    while (e > 0L) {
        if (e and 1L == 1L) result = mulmod(result, b, mod)
        b = mulmod(b, b, mod)
        e = e shr 1
    }
    return result
}

private fun millerTest(n: Long, a: Long): Boolean {
    if (n % a == 0L) return n == a
    var d = n - 1L
    var r = 0
    while (d % 2L == 0L) { d /= 2L; r++ }
    var x = powmod(a, d, n)
    if (x == 1L || x == n - 1L) return true
    repeat(r - 1) {
        x = mulmod(x, x, n)
        if (x == n - 1L) return true
    }
    return false
}

/** Deterministic Miller-Rabin for n < 3,317,044,064,679,887,385,961,981. */
fun isPrime(n: Long): Boolean {
    if (n < 2L) return false
    if (n == 2L || n == 3L || n == 5L || n == 7L) return true
    if (n % 2L == 0L || n % 3L == 0L || n % 5L == 0L) return false
    // Witnesses sufficient for n < 3.3e24 (covers all Long values)
    for (a in longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
        if (!millerTest(n, a)) return false
    }
    return true
}

// --- Pollard Rho factorization ---

private fun pollardRho(n: Long): Long {
    if (n % 2L == 0L) return 2L
    var x = (2L..n - 1L).random()
    var y = x
    var c = (1L..n - 1L).random()
    var d = 1L
    while (d == 1L) {
        x = (mulmod(x, x, n) + c) % n
        y = (mulmod(y, y, n) + c) % n
        y = (mulmod(y, y, n) + c) % n
        d = gcdLong(if (x > y) x - y else y - x, n)
    }
    return d
}

private fun gcdLong(a: Long, b: Long): Long = if (b == 0L) a else gcdLong(b, a % b)

private fun factorizeHelper(n: Long, factors: MutableList<Long>) {
    if (n == 1L) return
    if (isPrime(n)) { factors.add(n); return }
    var d = n
    while (d == n) d = pollardRho(n)
    factorizeHelper(d, factors)
    factorizeHelper(n / d, factors)
}

/** Returns sorted list of prime factors of n (with repetition). */
fun factorize(n: Long): List<Long> {
    val factors = mutableListOf<Long>()
    factorizeHelper(n, factors)
    factors.sort()
    return factors
}
