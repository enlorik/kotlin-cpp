# Fast competitive-programming input and output

`io/FastIO.kt` provides compact **buffered, whitespace-tokenized**
input/output helpers. The code works with ordinary Kotlin/JVM and needs only
standard Java I/O, including on Codeforces.

## The motivating example

Your original C++ problem (OR all **equally nearest** nonzero cells within
Manhattan distance 5) can now be written as:

```kotlin
fun main() {
    val (h, w) = readInts()
    val grid = readIntGrid(h, w)

    val result = grid.aggregate(
        selection = Nearest(
            sources = { r, c -> grid[r][c] != 0 },
            maxDistance = 5
        ),
        combine = { a, b -> a or b }
    )

    printGrid(h, w) { r, c ->
        if (grid[r][c] != 0) -1 else result.values[r][c]
    }
}
```

Complete example: `examples/NearestOrSolution.kt`.

Input:

```text
3 3
0 1 0
2 0 4
0 8 0
```

Output:

```text
3 -1 5
-1 15 -1
10 -1 12
```

The aggregator comes from `grid/GridAggregate.kt`. That file needs
`grid/GridBfs.kt` for the direction constants/classes.

## Reading input

```kotlin
val n = readInt()
val bigNumber = readLong()
val word = readToken()
val (h, w) = readInts() // exactly TWO integer tokens (default count = 2)
val nums = readInts(5)  // exactly five integers
val longs = readLongs(5)
val grid = readIntGrid(h, w)   // Array<IntArray>
val maze = readCharGrid(h, w)  // Array<CharArray> using h strings of length w
```

**Important:** `readInts()` without arguments reads TWO integers, not
the remainder of a physical line. Like most contest scanners, input is
whitespace-separated; line breaks don't affect the parser.

A buffered singleton scanner powers the top-level helpers. For custom
streams or focused tests, create `val input = FastInput(myStream)` and
call `input.nextInt()`, `input.readInts(3)`, `input.readIntGrid(h,w)`,
etc. A premature EOF raises `NoSuchElementException`.

## Printing a grid

```kotlin
// Calculate output cells lazily and print one space between columns:
printGrid(h, w) { r, c -> if (grid[r][c] == 0) 0 else -1 }

// Print existing Int or Long matrices directly:
printGrid(grid)
printGrid(Array(h) { LongArray(w) { 0L } })
```

`printGrid()` writes one row per line, with one space between neighboring
values, no trailing spaces, and a buffered writer. It flushes after the
whole grid, not after each individual number.

For a special output destination, use `FastOutput(writer).printGrid(...)`
and `FastOutput.flush()`. The wrappers don't close `System.out`.

## Performance and tests

The scanner reads 64-KiB byte buffers rather than splitting a large input
into strings. The printer builds one row at a time and uses a buffered
writer. These are appropriate for large competitive programming inputs.

Local tests:

```bash
kotlinc io/FastIO.kt tests/FastIOTests.kt -include-runtime -d fast-io-tests.jar
java -jar fast-io-tests.jar
```

Tests exercise 100,000 integers across buffer boundaries, negative int/long
extremes, newline/tab whitespace, character grids, rectangular validation,
clean space-separated output, empty columns, and EOF.

For a standalone judge submission, concatenate `io/FastIO.kt` plus every
library file needed by your algorithm into the source submitted to the judge.
The repository does not currently include a receiver-aware code bundler,
so just naming the GitHub library file in a judge submission won't import it
automatically.
