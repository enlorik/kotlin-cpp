import java.math.BigInteger
import kotlin.random.Random

private fun exact(v: Long) = BigInteger.valueOf(v)
private fun trialPrime(n: Long): Boolean {
    if (n < 2L) return false
    var d = 2L
    while (d * d <= n) {
        if (n % d == 0L) return false
        d++
    }
    return true
}

fun main() {
    val max = Long.MAX_VALUE
    val min = Long.MIN_VALUE
    check(modMultiply64(max, max, max) == 0L)
    check(modAdd64(max - 2, max - 2, max) == max - 4)
    check(modSubtract64(min, max, max) == max - 1)
    check(modPower64(2, 10, 1000) == 24L)
    check(modInverse64(3, 11) == 4L)
    check(modInverse64(6, 10) == null)
    check(runCatching { modPower64(2,-1,7) }.isFailure)
    check(runCatching { modMultiply64(1,2,0) }.isFailure)

    val rng = Random(20161110)
    repeat(1500) {
        val a = rng.nextLong()
        val b = rng.nextLong()
        val modulus = rng.nextLong(1,Long.MAX_VALUE)
        val m = exact(modulus)
        val x = exact(a).mod(m)
        val y = exact(b).mod(m)
        check(modAdd64(a,b,modulus) == x.add(y).mod(m).longValueExact())
        check(modSubtract64(a,b,modulus) == x.subtract(y).mod(m).longValueExact())
        check(modMultiply64(a,b,modulus) == x.multiply(y).mod(m).longValueExact())
        val power = rng.nextLong(0,5000)
        check(modPower64(a,power,modulus) ==
            x.modPow(exact(power),m).longValueExact())
        val gcd = extendedGcdExact(a,b)
        check(gcd.gcd == exact(a).gcd(exact(b)))
        check(exact(a).multiply(gcd.x).add(exact(b).multiply(gcd.y)) == gcd.gcd)
    }
    check(extendedGcdExact(min,0).gcd == BigInteger.ONE.shiftLeft(63))

    check(chineseRemainder64(emptyList()) ==
        ChineseRemainderResult(BigInteger.ZERO,BigInteger.ONE))
    check(chineseRemainder64(listOf(2L to 4L,6L to 8L)) ==
        ChineseRemainderResult(BigInteger.valueOf(6),BigInteger.valueOf(8)))
    check(chineseRemainder64(listOf(1L to 4L,2L to 6L)) == null)
    repeat(800) {
        val equations = List(rng.nextInt(0,5)) {
            rng.nextLong(-100,101) to rng.nextLong(1,17)
        }
        val candidate = chineseRemainder64(equations)
        val limit = equations.fold(1L) { acc, (_,mod) ->
            acc / exact(acc).gcd(exact(mod)).longValueExact() * mod
        }
        val first = (0L until limit).firstOrNull { x ->
            equations.all { (residue, modulus) ->
                modNormalize64(x,modulus) == modNormalize64(residue,modulus)
            }
        }
        if (first == null) check(candidate == null)
        else {
            check(candidate != null)
            check(candidate!!.residue == exact(first))
            check(candidate.modulus == exact(limit))
        }
    }

    for (n in 0L..10_000L) check(isPrime64(n) == trialPrime(n))
    check(isPrime64(2305843009213693951L)) // 2^61-1 Mersenne prime
    check(!isPrime64(341550071728321L)) // trap for naive Miller-Rabin bases
    check(!isPrime64(3825123056546413051L))
    check(!isPrime64(Long.MAX_VALUE))
    check(factor64(1).isEmpty())
    check(factor64(60) == mapOf(2L to 2,3L to 1,5L to 1))
    check(eulerTotient64(36) == 12L)
    check(eulerTotient64(1) == 1L)
    check(runCatching { factor64(0) }.isFailure)
    for (n in 1L..2000L) {
        val factors = factor64(n)
        var product = BigInteger.ONE
        for ((prime,exponent) in factors) {
            check(isPrime64(prime))
            product = product.multiply(exact(prime).pow(exponent))
        }
        check(product == exact(n))
    }
    for (n in listOf(1_000_003L * 1_000_033L, Long.MAX_VALUE)) {
        val factors = factor64(n)
        var product = BigInteger.ONE
        for ((prime,exponent) in factors) {
            check(isPrime64(prime))
            product = product.multiply(exact(prime).pow(exponent))
        }
        check(product == exact(n))
    }
    println("PASS: modular arithmetic, generalized CRT, deterministic 64-bit primality and factorization")
}
