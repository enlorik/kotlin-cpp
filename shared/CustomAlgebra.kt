/**
 * Declare a reusable associative reduction with an identity element.
 * Example: val product = aggregation(1) { a: Int, b: Int -> a * b }
 * The caller is responsible for associativity and correct identity.
 * Set idempotent=true only if combine(x,x) == x for every x.
 */
fun <T> aggregation(
    initial: T,
    idempotent: Boolean = false,
    combine: (T, T) -> T
): AggregateOp<T> = AggregateOp(initial, combine, idempotent)

