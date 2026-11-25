/** A length-aware, double polynomial fingerprint. Equality is NOT a proof of string equality. */
data class StringFingerprint(val length: Int,val first: Int,val second: Int)

/** Immutable UTF-16 substring index: O(n) preprocessing/space, O(1) fingerprints.
 * Fixed public base and primes make results comparable across instances, but
 * adversarial collisions remain possible. Use equalVerified for exact answers.
 */
class RollingHash(private val text: String) {
    private val p = 1000000007L
    private val q = 1000000009L
    private val base = 911382323L
    private val h1 = LongArray(text.length+1)
    private val h2 = LongArray(text.length+1)
    private val pow1 = LongArray(text.length+1)
    private val pow2 = LongArray(text.length+1)
    val length: Int get() = text.length
    init {
        pow1[0] = 1; pow2[0] = 1
        for (i in text.indices) {
            val c = text[i].code+1L
            h1[i+1] = (h1[i]*base+c)%p
            h2[i+1] = (h2[i]*base+c)%q
            pow1[i+1] = pow1[i]*base%p
            pow2[i+1] = pow2[i]*base%q
        }
    }
    fun fingerprint(from: Int,until: Int): StringFingerprint {
        require(from in 0..until && until <= length)
        val len = until-from
        val a = (h1[until]-h1[from]*pow1[len]%p+p)%p
        val b = (h2[until]-h2[from]*pow2[len]%q+q)%q
        return StringFingerprint(len,a.toInt(),b.toInt())
    }
    /** May return false positives due to hash collisions. */
    fun probablyEqual(from: Int,until: Int,other: RollingHash,otherFrom: Int,otherUntil: Int): Boolean =
        fingerprint(from,until) == other.fingerprint(otherFrom,otherUntil)
    /** Exact comparison: O(1) rejection, O(length) verification on a hash match. */
    fun equalVerified(from: Int,until: Int,other: RollingHash,otherFrom: Int,otherUntil: Int): Boolean =
        probablyEqual(from,until,other,otherFrom,otherUntil) &&
            text.regionMatches(from,other.text,otherFrom,until-from)
}
fun String.rollingHash(): RollingHash = RollingHash(this)
