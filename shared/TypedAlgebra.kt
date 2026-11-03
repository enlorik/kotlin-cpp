import kotlin.random.Random

/**
 * A binary operation with a two-sided identity. Implementations PROMISE that
 * combine is associative; Kotlin cannot prove that property for arbitrary code.
 */
interface Monoid<T> {
    val identity: T
    fun combine(left: T, right: T): T
}

/** Monoid with the additional law combine(x, x) == x. */
interface IdempotentMonoid<T> : Monoid<T>

/**
 * Commutative group: monoid, commutativity and inverse for every element.
 * A general noncommutative group is NOT enough for Fenwick range queries.
 */
interface AbelianGroupSpec<T> : Monoid<T> {
    fun inverse(value: T): T
}

/**
 * Lazy-update action on a monoid.
 * compose(old, newer) means apply old, then newer. Both the composition
 * law and distribution over concatenated segments are caller promises.
 */
interface MonoidAction<T, U> {
    fun apply(aggregate: T, update: U, length: Int): T
    fun compose(old: U, newer: U): U
}

/** IDE-friendly constructor: combines and an identity are compulsory. */
fun <T> monoid(identity: T, combine: (T, T) -> T): Monoid<T> =
    object : Monoid<T> {
        override val identity = identity
        override fun combine(left: T, right: T): T = combine(left, right)
    }

/** IDE-friendly constructor for an associative, idempotent operation. */
fun <T> idempotentMonoid(identity: T, combine: (T, T) -> T): IdempotentMonoid<T> =
    object : IdempotentMonoid<T> {
        override val identity = identity
        override fun combine(left: T, right: T): T = combine(left, right)
    }

/** Missing inverse is a Kotlin compile error, not a runtime surprise. */
fun <T> group(
    identity: T,
    combine: (T, T) -> T,
    inverse: (T) -> T
): AbelianGroupSpec<T> =
    object : AbelianGroupSpec<T> {
        override val identity = identity
        override fun combine(left: T, right: T): T = combine(left, right)
        override fun inverse(value: T): T = inverse(value)
    }

/** Provide both interval-update and old-before-new composition semantics. */
fun <T, U> action(
    apply: (aggregate: T, update: U, length: Int) -> T,
    compose: (old: U, newer: U) -> U
): MonoidAction<T, U> =
    object : MonoidAction<T, U> {
        override fun apply(aggregate: T, update: U, length: Int): T = apply(aggregate, update, length)
        override fun compose(old: U, newer: U): U = compose(old, newer)
    }

/** A concrete failing law and sampled counterexample. */
class AlgebraLawViolation(
    val law: String,
    val counterexample: String
) : IllegalStateException("Algebra law failed: $law. Counterexample: $counterexample")

/**
 * Deterministically sample the laws this static type promises.
 * Tests are NOT a formal proof; choose representative samples, especially
 * zeros, negative values, boundaries, and overflow cases where applicable.
 *
 * The equality callback supports approximate or domain-specific comparisons.
 */
fun <T> Monoid<T>.checkLaws(
    samples: List<T>,
    iterations: Int = 500,
    seed: Int = 0,
    equivalent: (T, T) -> Boolean = { a, b -> a == b }
) {
    require(samples.isNotEmpty()) { "Provide at least one sample" }
    require(iterations >= 0) { "iterations must be nonnegative" }

    for (x in samples) {
        val left = combine(identity, x)
        val right = combine(x, identity)
        if (!equivalent(left, x) || !equivalent(right, x)) {
            throw AlgebraLawViolation("identity", "x=$x, identity=$identity, identity*x=$left, x*identity=$right")
        }
        if (this is IdempotentMonoid<*>) {
            val repeated = combine(x, x)
            if (!equivalent(repeated, x)) {
                throw AlgebraLawViolation("idempotence", "x=$x, x*x=$repeated")
            }
        }
        if (this is AbelianGroupSpec<*>) {
            @Suppress("UNCHECKED_CAST")
            val inverseValue = (this as AbelianGroupSpec<T>).inverse(x)
            val leftInverse = combine(x, inverseValue)
            val rightInverse = combine(inverseValue, x)
            if (!equivalent(leftInverse, identity) || !equivalent(rightInverse, identity)) {
                throw AlgebraLawViolation(
                    "inverse",
                    "x=$x, inverse(x)=$inverseValue, x*inverse=$leftInverse, inverse*x=$rightInverse, identity=$identity"
                )
            }
        }
    }

    val random = Random(seed)
    repeat(iterations) {
        val a = samples[random.nextInt(samples.size)]
        val b = samples[random.nextInt(samples.size)]
        val c = samples[random.nextInt(samples.size)]
        val first = combine(combine(a, b), c)
        val second = combine(a, combine(b, c))
        if (!equivalent(first, second)) {
            throw AlgebraLawViolation("associativity", "a=$a, b=$b, c=$c, (a*b)*c=$first, a*(b*c)=$second")
        }
        if (this is AbelianGroupSpec<*>) {
            val ab = combine(a, b)
            val ba = combine(b, a)
            if (!equivalent(ab, ba)) {
                throw AlgebraLawViolation("commutativity", "a=$a, b=$b, a*b=$ab, b*a=$ba")
            }
        }
    }
}

/**
 * Sample both lazy-action laws:
 * - apply(combine(a,b), u, lenA+lenB) equals combine(apply(a,u,lenA), apply(b,u,lenB)).
 * - apply(apply(a,old,len),new,len) equals apply(a,compose(old,new),len).
 *
 * Each constructed segment consists of sampled *elements*; updates should
 * themselves be representative of the real update domain.
 */
fun <T, U> MonoidAction<T, U>.checkActionLaws(
    monoid: Monoid<T>,
    elements: List<T>,
    updates: List<U>,
    iterations: Int = 500,
    seed: Int = 0,
    equivalent: (T, T) -> Boolean = { a, b -> a == b }
) {
    require(elements.isNotEmpty() && updates.isNotEmpty()) { "Supply sample elements and updates" }
    require(iterations >= 0) { "iterations must be nonnegative" }
    val random = Random(seed)

    fun sampleAggregate(length: Int): T {
        var result = monoid.identity
        repeat(length) { result = monoid.combine(result, elements[random.nextInt(elements.size)]) }
        return result
    }

    repeat(iterations) {
        val leftLength = random.nextInt(0, 5)
        val rightLength = random.nextInt(0, 5)
        val left = sampleAggregate(leftLength)
        val right = sampleAggregate(rightLength)
        val before = updates[random.nextInt(updates.size)]
        val after = updates[random.nextInt(updates.size)]
        val whole = apply(monoid.combine(left, right), before, leftLength + rightLength)
        val separate = monoid.combine(
            apply(left, before, leftLength), apply(right, before, rightLength)
        )
        if (!equivalent(whole, separate)) {
            throw AlgebraLawViolation(
                "action distribution",
                "left=$left ($leftLength), right=$right ($rightLength), update=$before, whole=$whole, split=$separate"
            )
        }
        val twice = apply(apply(left, before, leftLength), after, leftLength)
        val composed = apply(left, compose(before, after), leftLength)
        if (!equivalent(twice, composed)) {
            throw AlgebraLawViolation(
                "action composition",
                "aggregate=$left, length=$leftLength, old=$before, newer=$after, sequential=$twice, composed=$composed"
            )
        }
        val empty = apply(monoid.identity, before, 0)
        if (!equivalent(empty, monoid.identity)) {
            throw AlgebraLawViolation(
                "empty interval action", "update=$before, apply(identity,0)=$empty, identity=${monoid.identity}"
            )
        }
    }
}
