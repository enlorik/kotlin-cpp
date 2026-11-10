# All-roots tree DP and offline dynamic connectivity

## Rerooting DP

When a local tree answer depends on contributions from neighbours,
`rerootingDp()` combines the child messages, then transfers the
finished component across each edge. Prefix/suffix aggregates exclude a
specific child in O(1), so results for every root cost O(n) total.

```kotlin
val edges = listOf(0 to 1, 1 to 2)
check(treeDistanceSums(3, edges).contentEquals(longArrayOf(3, 2, 3)))
```

For a custom associative aggregation:

```kotlin
data class State(val count: Long, val distance: Long)
val op = monoid(State(0, 0)) { a: State, b: State ->
    State(a.count + b.count, a.distance + b.distance)
}
val perRoot = rerootingDp(3, edges, op,
    finish = { _, merged -> State(merged.count + 1, merged.distance) },
    lift = { value, _, _ -> State(value.count, value.distance + value.count) }
)
```

The `finish(vertex, combined)` callback turns neighbour contributions
into the rooted-component aggregate; `lift(component, from, to)` converts
that aggregate into its message along the directed edge.
`combine` must be associative with a two-sided identity.
For noncommutative monoids, neighbours are combined in their supplied
edge-adjacency order. The algorithm uses iterative traversal and accepts
a single connected undirected tree of n vertices and n-1 edges.

## Rollback DSU

```kotlin
val dsu = RollbackDsu(4)
val before = dsu.snapshot()
dsu.union(0, 1)
dsu.union(1, 2)
check(dsu.componentSize(2) == 3)
dsu.rollback(before)
check(!dsu.connected(0, 2))
```

Union by size (without path compression) gives O(log n) find/union,
with O(1) rollback for each successful union. Snapshots are stack-depth
tokens valid only while that history exists; rollback cannot restore a
future state that has been discarded.

## Offline dynamic connectivity

```kotlin
val answers = offlineConnectivity(3) {
    add(0, 1)
    connected(0, 2)
    add(1, 2)
    connected(0, 2)
    remove(0, 1)
    componentCount()
}
```

Results are returned in query order as `Connected`, `ComponentCount`,
or `Size`. Duplicate undirected edges are counted: removing one copy
does not disconnect the endpoints while another remains. An attempt
to remove an absent edge is an input error. Self-loops are accepted
and do not affect connectivity. The events must be known in advance.

The implementation creates active edge intervals, distributes them
into a segment tree over time, and runs rollback DSU while descending
the tree. Expected O((events + intervals) log(events) log(vertices))
time and O(events log events + vertices) memory. It does not claim
to solve online edge deletions.

## Contest packaging

```bash
python3 tools/build_submission.py --modules rerooting-dp,offline-connectivity \
    --solution Main.kt --output submission.kt
```

`rerooting-dp` includes the typed monoid constructor; the standalone
`offline-connectivity` module has no graph dependency.
