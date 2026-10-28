# Tell the library what the graph input looks like

Competitive programming problems disagree about where values go, whether
edges have extra fields, and how vertices are numbered. You can now **describe
the format in a block, in the exact order it appears**:

```kotlin
val g = readGraph {
    header()  // reads n m
    edges()   // reads m pairs: u v
    values()  // reads n integers AFTER the edges
}
```

`graph { ... }` is an alias for `readGraph { ... }`:

```kotlin
val g = graph {
    header()
    values()
    edges()
}
```

The result is a normal `SparseGraph`, compatible with `bfsFlex`,
`dijkstraFlex`, `aggregate`, and the rest of your library. The existing
`readGraph(n, m)` and `readGraph(n, m, weighted = true)` still work.

## Your two most common formats

### Edges first, then vertex values

Input:

```text
4 3
1 2
1 3
2 4
10 20 30 40
```

```kotlin
val g = readGraph {
    header()
    edges()
    values()
}
println(g.values[0]) // 10
```

### Vertex values first, then edges

Input:

```text
4 3
10 20 30 40
1 2
1 3
2 4
```

```kotlin
val g = readGraph {
    header()
    values()
    edges()
}
```

**No separate value array is needed.** The parsed graph stores `g.values`
in 0-based vertex order. If you construct a graph manually, use
`g.withValues(intArrayOf(...))`.

`values()` is optional. If you omit it, `g.values` deliberately throws a
clear error rather than inventing zero values.

## Each edge is u v w

Input:

```text
3 2
1 2 150
2 3 400
```

```kotlin
val g = readGraph {
    header()
    edges {
        from()
        to()
        weight()
    }
}
```

This stores the third number in `Edge.w` as a `Long`. Dijkstra uses edge
weights. Unweighted `Nearest` and `Radius` still count **edges**, not `w`.

## More unusual input: directed, 0-based, extra edge fields

Input:

```text
3 2
0 blue 1 11 1007
2 red 1 12 1008
```

Here each edge row means `from color to type capacity`.

```kotlin
val g = readGraph {
    header()
    directed()
    zeroIndexed()
    edges {
        from()
        token("color")
        to()
        int("type")
        long("capacity")
    }
}

val colors = g.edgeTokens("color")   // ["blue", "red"]
val types = g.edgeInts("type")       // [11, 12]
val caps = g.edgeLongs("capacity")  // [1007L, 1008L]
```

These arrays are indexed by the **original input edge order**, NOT by
`g.g[u]` adjacency positions. That's intentional: for undirected graphs
each edge creates two adjacency entries, but its input attributes appear
only once. Named fields are preserved for later use; the standard shortest
path algorithms only use `Edge.w` automatically.

You can use `int("color")`, `long("capacity")`, or `token("label")` for
extra fields of the appropriate type. An arbitrary third field is **not**
automatically a distance: choose `weight()` only when that is what the
problem statement says. For an ignored field use `skipInt()`,
`skipLong()`, or `skipToken()`.

## Tree with just n in the header

Input:

```text
4
1 2
2 3
2 4
```

```kotlin
val tree = readGraph {
    tree()   // reads n and infers n-1 edges
    edges()
}
```

## Nonstandard headers and named vertex fields

Use `size(n, m)` when the first two input tokens are not simply `n m`.
The reader exposes token methods inside the block to describe other layouts:

```kotlin
val g = readGraph {
    val n = int()
    val ignored = token()
    val m = int()
    size(n, m)
    vertexInts("score")
    vertexLongs("cost")
    vertexTokens("name")
    edges { from(); to() }
}
val scores = g.vertexInts("score")
val costs = g.vertexLongs("cost")
val names = g.vertexTokens("name")
```

Use `ints(count)`, `longs(count)`, `int()`, `long()`, or `token()` for
other fixed-size fields or unusual headers. Each operation consumes input
**when it is executed**, exactly like an ordinary fast scanner.

All graph DSL numeric parsing uses the existing buffered fast I/O module;
it does not allocate a token array for the entire input.

## Pretty algorithm after pretty input

Input:

```text
4 3
1 2
1 3
2 4
0 2 4 8
```

Now the complete nearest-source OR solution is:

```kotlin
fun main() {
    val g = readGraph {
        header()
        edges()
        values()
    }
    val result = g.aggregate(
        selection = Nearest(
            sources = { v -> g.values[v] != 0 },
            maxDistance = 5
        ),
        op = IntAggregations.OR
    )
    printValues(g.n) { v ->
        if (g.values[v] != 0) -1 else result.values[v]
    }
}
```

Expected output: `6 -1 -1 -1`.

The new **value-free `aggregate(op=...)` overload** uses `g.values` only
if values have been read via the DSL or supplied by `withValues()`.
You can still provide `value = { v -> ... }` to aggregate some other
information (degrees, external arrays, etc.).

## Defaults, validation, and limitations

- The default graph is undirected, 1-based in input, internally 0-based.
- `edges()` defaults to `u v`; explicit columns allow unusual orders.
- `directed()`, `undirected()`, `zeroIndexed()` and `oneIndexed()`
  must appear **before** `edges()`.
- `header()` or `size(n,m)` must appear before parsing graph arrays.
- Repeating `edges()`, duplicating edge fields, invalid vertex indices,
  and forgetting to read nonzero `m` edge records are errors.
- The DSL handles **fixed-column edge layouts** and fixed-size vertex
  arrays. Variable-length per-edge records, string vertex IDs, flow-specific
  capacities, and other special formats may require bespoke parsing or a
  more specialized graph type. Extra named fields are stored as data; they
  do not automatically reconfigure algorithms.

## Implementation and tests

- `graph/GraphReadDsl.kt`: builder, edge-column schema, input parsing.
- `graph/SparseGraph.kt`: optional typed vertex/edge attribute storage.
- `graph/GraphAggregate.kt` and `shared/UnifiedAggregate.kt`:
  automatically use attached vertex values for Int aggregation.
- `tests/GraphReadDslTests.kt`: edges-first, values-first, weights,
  directed/0-indexed, named fields, nonstandard headers, trees, invalid
  formats, and nearest/radius/neighbor aggregate integration.
- `examples/GraphInputDslNearestOrSolution.kt`: complete contest solution.

To compile the tests:

```bash
kotlinc graph/*.kt grid/*.kt shared/UnifiedAggregate.kt io/FastIO.kt \
    tests/GraphReadDslTests.kt -include-runtime -d graph-input-tests.jar
java -jar graph-input-tests.jar
```

To submit to a standalone contest judge, bundle the required library
source files together with your solution. The repository's current symbol
registry still cannot automatically resolve all overloaded functions.
