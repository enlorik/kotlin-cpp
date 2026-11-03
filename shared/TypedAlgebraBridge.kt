/**
 * Optional bridge between capability-typed algebra and existing AggregateOp.
 * Existing AggregateOp-based APIs stay source-compatible.
 */
fun <T> Monoid<T>.asAggregateOp(): AggregateOp<T> =
    AggregateOp(
        initial = identity,
        combine = { a, b -> combine(a, b) },
        idempotent = this is IdempotentMonoid<*>
    )

/** Existing user-defined AggregateOp can opt into the typed monoid contract. */
fun <T> AggregateOp<T>.asMonoid(): Monoid<T> =
    monoid(initial) { a, b -> combine(a, b) }

/**
 * The runtime idempotent flag alone cannot prove idempotence.
 * Explicit conversion makes the promise visible to Kotlin's type checker.
 */
fun <T> AggregateOp<T>.asIdempotentMonoid(): IdempotentMonoid<T> {
    require(idempotent) { "AggregateOp.idempotent must be true to claim IdempotentMonoid" }
    return idempotentMonoid(initial) { a, b -> combine(a, b) }
}
