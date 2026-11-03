/**
 * Mo's algorithm convenience for any abelian-group range aggregate.
 * A general associative reduction cannot necessarily remove an element;
 * the inverse makes O(1) add/remove callbacks possible here.
 */
fun <T> List<T>.moAggregate(queries: List<MoQuery>, group: AbelianGroup<T>): List<T> {
    var running = group.op.initial
    return moQueries(
        n = size,
        queries = queries,
        add = { index -> running = group.op.combine(running, this[index]) },
        remove = { index -> running = group.op.combine(running, group.inverse(this[index])) },
        snapshot = { running }
    )
}

fun IntArray.moAggregate(queries: List<MoQuery>, group: AbelianGroup<Int>): List<Int> =
    asList().moAggregate(queries, group)
fun LongArray.moAggregate(queries: List<MoQuery>, group: AbelianGroup<Long>): List<Long> =
    asList().moAggregate(queries, group)
fun <T> Array<out T>.moAggregate(queries: List<MoQuery>, group: AbelianGroup<T>): List<T> =
    asList().moAggregate(queries, group)

