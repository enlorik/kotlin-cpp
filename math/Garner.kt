/** Mixed-radix reconstruction for pairwise coprime moduli >= 2.
 * Preprocessing O(k^2) modular inverses; each query O(k^2) modular operations.
 * Overflow-safe signed Long arithmetic via ModularArithmetic.kt.
 * Target modulus need not be coprime to the source moduli.
 */
class GarnerIndex(moduli: LongArray) {
    private val moduli = moduli.copyOf()
    private val inverse = Array(moduli.size) { LongArray(it) }
    init {
        require(this.moduli.all { it >= 2L })
        for (i in this.moduli.indices) for (j in 0 until i)
            inverse[i][j] = requireNotNull(modInverse64(this.moduli[j],this.moduli[i])) {
                "Source moduli must be pairwise coprime"
            }
    }
    /** Unique mixed-radix digits of the canonical solution in [0, product). */
    fun digits(residues: LongArray): LongArray {
        require(residues.size == moduli.size)
        val result = LongArray(moduli.size)
        for (i in moduli.indices) {
            val m = moduli[i]
            var t = modNormalize64(residues[i],m)
            for (j in 0 until i)
                t = modMultiply64(modSubtract64(t,result[j],m),inverse[i][j],m)
            result[i] = t
        }
        return result
    }
    /** Canonical CRT solution modulo target; never constructs the product. */
    fun reconstructMod(residues: LongArray,target: Long): Long {
        require(target > 0)
        val d = digits(residues)
        var result = 0L
        for (i in d.indices.reversed())
            result = modAdd64(modMultiply64(result,moduli[i],target),d[i],target)
        return result
    }
}
fun garnerMod(residues: LongArray,moduli: LongArray,target: Long): Long =
    GarnerIndex(moduli).reconstructMod(residues,target)
