/**
 * Check laws immediately during definition, preserving the capability type.
 * This tests representative samples, not all possible inputs.
 *
 * val sum = monoid(0) { a: Int, b: Int -> a + b }.validated(listOf(-1,0,1))
 */
fun <T> Monoid<T>.validated(
    samples: List<T>,
    iterations: Int = 500,
    seed: Int = 0
): Monoid<T> = apply { checkLaws(samples, iterations, seed) }

/** Keep IdempotentMonoid type after validation, so sparseTable accepts it. */
fun <T> IdempotentMonoid<T>.validated(
    samples: List<T>,
    iterations: Int = 500,
    seed: Int = 0
): IdempotentMonoid<T> = apply { checkLaws(samples, iterations, seed) }

/** Keep AbelianGroupSpec type after validation, so Fenwick accepts it. */
fun <T> AbelianGroupSpec<T>.validated(
    samples: List<T>,
    iterations: Int = 500,
    seed: Int = 0
): AbelianGroupSpec<T> = apply { checkLaws(samples, iterations, seed) }

/** Check a lazy action when declaring it; preserve its update/aggregate types. */
fun <T, U> MonoidAction<T, U>.validated(
    monoid: Monoid<T>,
    elements: List<T>,
    updates: List<U>,
    iterations: Int = 500,
    seed: Int = 0
): MonoidAction<T, U> = apply {
    checkActionLaws(monoid, elements, updates, iterations, seed)
}
