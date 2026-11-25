import java.math.BigInteger
import kotlin.random.Random

fun main() {
    val rng = Random(91273)
    val primes = intArrayOf(2,3,5,7,11,31,97)
    fun binomial(n: Int,k: Int): BigInteger {
        var v = BigInteger.ONE
        for (i in 1..minOf(k,n-k)) v = v.multiply(BigInteger.valueOf((n-i+1).toLong())).divide(BigInteger.valueOf(i.toLong()))
        return v
    }
    for (p in primes) {
        val c = PrimeCombinatorics(p,p-1)
        val m = BigInteger.valueOf(p.toLong())
        repeat(100) {
            val n = rng.nextInt(0,350); val k = rng.nextInt(0,n+1)
            check(c.lucas(n.toLong(),k.toLong()) == binomial(n,k).mod(m).toInt())
            val a = rng.nextInt(p); val b = rng.nextInt(a+1)
            check(c.choose(a,b) == binomial(a,b).mod(m).toInt())
        }
        check(c.lucas(Long.MAX_VALUE,0) == 1)
        check(c.lucas(Long.MAX_VALUE,1) == (Long.MAX_VALUE%p).toInt())
    }
    val large = PrimeCombinatorics(1000000007,100000)
    check(large.choose(100000,2) == (100000L*99999/2%1000000007).toInt())
    check(runCatching { PrimeCombinatorics(9,4) }.isFailure)
    check(runCatching { large.lucas(3,2) }.isFailure)

    repeat(300) {
        val mods = listOf(2L,3L,5L,7L,11L,13L,17L,19L).shuffled(rng).take(rng.nextInt(0,9)).toLongArray()
        val residues = LongArray(mods.size) { rng.nextLong() }
        val crt = chineseRemainder64(mods.indices.map { residues[it] to mods[it] })!!
        val target = rng.nextLong(1,Long.MAX_VALUE)
        val index = GarnerIndex(mods)
        check(index.reconstructMod(residues,target) == crt.residue.mod(BigInteger.valueOf(target)).toLong())
        if (mods.isNotEmpty()) check(index.reconstructMod(residues,mods[0]) == modNormalize64(residues[0],mods[0]))
    }
    val huge = longArrayOf(1000000007L,1000000009L,998244353L,2147483647L)
    val rs = longArrayOf(Long.MIN_VALUE,Long.MAX_VALUE,-1,0)
    val expected = chineseRemainder64(huge.indices.map { rs[it] to huge[it] })!!
    check(garnerMod(rs,huge,Long.MAX_VALUE) == expected.residue.mod(BigInteger.valueOf(Long.MAX_VALUE)).toLong())
    check(runCatching { GarnerIndex(longArrayOf(6,9)) }.isFailure)
    check(garnerMod(longArrayOf(),longArrayOf(),1) == 0L)

    repeat(500) {
        val cap = rng.nextInt(0,300)
        val bits = SubsetSumBitset(cap)
        val brute = BooleanArray(cap+1); brute[0] = true
        repeat(rng.nextInt(0,50)) {
            val w = rng.nextInt(0,350)
            bits.add(w)
            for (s in cap downTo w) brute[s] = brute[s] || brute[s-w]
            for (s in 0..cap) check((s in bits) == brute[s])
            val limit = rng.nextInt(cap+1)
            check(bits.bestAtMost(limit) == (limit downTo 0).first { brute[it] })
        }
    }
    val boundary = intArrayOf(63,64,65).subsetSumBitset(192)
    check(192 in boundary && 128 in boundary && 1 !in boundary)
    val stress = IntArray(1000){1000}.subsetSumBitset(1000000)
    check(stress.bestAtMost(999999) == 999000 && 1000000 in stress)
    println("PASS: prime combinatorics, Garner, and bitset subset sum")
}
