// Generated from enlorik/kotlin-cpp library for Timus 1028
// Modules: fast-io, range-program
// Source files: io/FastIO.kt, shared/AggregationOp.kt, shared/CustomAlgebra.kt, shared/TypedAlgebra.kt, shared/TypedAlgebraBridge.kt, tree/SegmentTree.kt, shared/SegmentTreeAggregations.kt, range/SparseTables.kt, tree/ConfigurableLazySegmentTree.kt, tree/PersistentAggregateTree.kt, tree/TypedAlgebraTrees.kt, tree/AssignmentLazyTree.kt, range/RangeIndex.kt, range/RangeProgram.kt

import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStreamWriter
import java.io.Writer
import kotlin.random.Random

// ===== io/FastIO.kt =====
/** Fast, whitespace-tokenized input for competitive programming. */
class FastInput(private val input: InputStream = System.`in`) {
    private val buffer = ByteArray(1 shl 16)
    private var pos = 0
    private var size = 0

    private fun nextByte(): Int {
        if (pos >= size) {
            size = input.read(buffer)
            pos = 0
            if (size <= 0) return -1
        }
        return buffer[pos++].toInt() and 0xff
    }

    private fun firstTokenByte(): Int {
        var c = nextByte()
        while (c in 0..32) c = nextByte()
        if (c == -1) throw NoSuchElementException("Unexpected end of input")
        return c
    }

    fun nextInt(): Int {
        var c = firstTokenByte()
        var sign = 1
        if (c == '-'.code) {
            sign = -1
            c = nextByte()
        }
        if (c !in '0'.code..'9'.code) throw NumberFormatException("Expected an integer")
        var value = 0
        while (c in '0'.code..'9'.code) {
            value = value * 10 + c - '0'.code
            c = nextByte()
        }
        if (c > 32) throw NumberFormatException("Invalid integer token")
        return value * sign
    }

    fun nextLong(): Long {
        var c = firstTokenByte()
        var sign = 1L
        if (c == '-'.code) {
            sign = -1L
            c = nextByte()
        }
        if (c !in '0'.code..'9'.code) throw NumberFormatException("Expected a long integer")
        var value = 0L
        while (c in '0'.code..'9'.code) {
            value = value * 10 + (c - '0'.code)
            c = nextByte()
        }
        if (c > 32) throw NumberFormatException("Invalid long integer token")
        return value * sign
    }

    fun nextToken(): String {
        var c = firstTokenByte()
        val text = StringBuilder()
        while (c > 32) {
            text.append(c.toChar())
            c = nextByte()
        }
        return text.toString()
    }

    /** Reads exactly [count] integers, not necessarily from one line. Defaults to 2 for (h, w). */
    fun readInts(count: Int = 2): IntArray {
        require(count >= 0) { "count must be nonnegative" }
        return IntArray(count) { nextInt() }
    }

    fun readLongs(count: Int): LongArray {
        require(count >= 0) { "count must be nonnegative" }
        return LongArray(count) { nextLong() }
    }

    /** Reads [rows] * [cols] whitespace-separated integers. */
    fun readIntGrid(rows: Int, cols: Int): Array<IntArray> {
        require(rows >= 0 && cols >= 0) { "Grid dimensions must be nonnegative" }
        return Array(rows) { IntArray(cols) { nextInt() } }
    }

    /** Reads one whitespace-separated string per row, preserving its characters. */
    fun readCharGrid(rows: Int, cols: Int): Array<CharArray> {
        require(rows >= 0 && cols >= 0) { "Grid dimensions must be nonnegative" }
        return Array(rows) {
            val token = nextToken()
            require(token.length == cols) { "Expected a row of $cols characters, got ${token.length}" }
            token.toCharArray()
        }
    }
}

/** Fast buffered output. It never closes System.out. */
class FastOutput(output: Writer = OutputStreamWriter(System.out)) {
    private val writer = BufferedWriter(output, 1 shl 16)

    /** Prints [rows] lines of [cols] space-separated computed values. */
    fun printGrid(rows: Int, cols: Int, value: (r: Int, c: Int) -> Any?) {
        require(rows >= 0 && cols >= 0) { "Grid dimensions must be nonnegative" }
        val line = StringBuilder()
        for (r in 0 until rows) {
            line.setLength(0)
            for (c in 0 until cols) {
                if (c > 0) line.append(' ')
                line.append(value(r, c))
            }
            writer.write(line.toString())
            writer.newLine()
        }
        writer.flush()
    }

    fun printGrid(values: Array<IntArray>) {
        require(values.isEmpty() || values.all { it.size == values[0].size }) { "Grid must be rectangular" }
        printGrid(values.size, if (values.isEmpty()) 0 else values[0].size) { r, c -> values[r][c] }
    }

    fun printGrid(values: Array<LongArray>) {
        require(values.isEmpty() || values.all { it.size == values[0].size }) { "Grid must be rectangular" }
        printGrid(values.size, if (values.isEmpty()) 0 else values[0].size) { r, c -> values[r][c] }
    }

    /** Print a one-line space-separated sequence without joinToString(). */
    fun printValues(count: Int, value: (index: Int) -> Any?) {
        require(count >= 0) { "count must be nonnegative" }
        for (i in 0 until count) {
            if (i > 0) writer.write(' '.code)
            writer.write(value(i).toString())
        }
        writer.newLine()
        writer.flush()
    }

    fun printValues(values: IntArray) = printValues(values.size) { values[it] }
    fun printValues(values: LongArray) = printValues(values.size) { values[it] }
    fun <T> printValues(values: List<T>) = printValues(values.size) { values[it] }

    fun flush() = writer.flush()
}

/** Use readInt()/readInts()/readIntGrid() directly in main(). */
private val standardInput by lazy { FastInput() }
private val standardOutput by lazy { FastOutput() }

fun readInt(): Int = standardInput.nextInt()
fun readLong(): Long = standardInput.nextLong()
fun readToken(): String = standardInput.nextToken()

/** Reads exactly [count] whitespace-separated integers; default: two dimensions. */
fun readInts(count: Int = 2): IntArray = standardInput.readInts(count)
fun readLongs(count: Int): LongArray = standardInput.readLongs(count)
fun readIntGrid(rows: Int, cols: Int): Array<IntArray> = standardInput.readIntGrid(rows, cols)
fun readCharGrid(rows: Int, cols: Int): Array<CharArray> = standardInput.readCharGrid(rows, cols)

/** Print a computed grid without manual spaces/newlines or joinToString(). */
fun printGrid(rows: Int, cols: Int, value: (r: Int, c: Int) -> Any?) =
    standardOutput.printGrid(rows, cols, value)

/** Print existing Int and Long grids. */
fun printGrid(values: Array<IntArray>) = standardOutput.printGrid(values)
fun printGrid(values: Array<LongArray>) = standardOutput.printGrid(values)

/** Print a sequence on one line with spaces, no manual joins. */
fun printValues(count: Int, value: (index: Int) -> Any?) =
    standardOutput.printValues(count, value)
fun printValues(values: IntArray) = standardOutput.printValues(values)
fun printValues(values: LongArray) = standardOutput.printValues(values)
fun <T> printValues(values: List<T>) = standardOutput.printValues(values)

// ===== shared/AggregationOp.kt =====
/**
 * Associative aggregation with a two-sided identity.
 *
 * The operation is shared by grids, graphs, and segment trees.
 * idempotent affects the graph nearest-source fast path only:
 * it means combine(x, x) == x (e.g. MIN, MAX, OR, AND).
 */
class AggregateOp<T>(
    val initial: T,
    val combine: (T, T) -> T,
    val idempotent: Boolean = false
)

/** Backwards-compatible name for existing graph and grid APIs. */
typealias GraphAggregateOp<T> = AggregateOp<T>

/** Aggregations for Int-valued grids, graphs, and segment trees. */
object IntAggregations {
    val OR = AggregateOp(0, { a: Int, b: Int -> a or b }, idempotent = true)
    val AND = AggregateOp(-1, { a: Int, b: Int -> a and b }, idempotent = true)
    val MIN = AggregateOp(Int.MAX_VALUE, { a: Int, b: Int -> minOf(a, b) }, idempotent = true)
    val MAX = AggregateOp(Int.MIN_VALUE, { a: Int, b: Int -> maxOf(a, b) }, idempotent = true)
    val SUM = AggregateOp(0, { a: Int, b: Int -> a + b })
    val XOR = AggregateOp(0, { a: Int, b: Int -> a xor b })
}

/** Corresponding operations for Long-valued ranges and graphs. */
object LongAggregations {
    val OR = AggregateOp(0L, { a: Long, b: Long -> a or b }, idempotent = true)
    val AND = AggregateOp(-1L, { a: Long, b: Long -> a and b }, idempotent = true)
    val MIN = AggregateOp(Long.MAX_VALUE, { a: Long, b: Long -> minOf(a, b) }, idempotent = true)
    val MAX = AggregateOp(Long.MIN_VALUE, { a: Long, b: Long -> maxOf(a, b) }, idempotent = true)
    val SUM = AggregateOp(0L, { a: Long, b: Long -> a + b })
    val XOR = AggregateOp(0L, { a: Long, b: Long -> a xor b })
}

// ===== shared/CustomAlgebra.kt =====
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

/**
 * Alias with the mathematical term `identity` for IDE-friendly discovery.
 * Use `monoid(identity) { ... }` for compile-time capability typing.
 */
fun <T> aggregateOp(
    identity: T,
    idempotent: Boolean = false,
    combine: (T, T) -> T
): AggregateOp<T> = aggregation(identity, idempotent, combine)

// ===== shared/TypedAlgebra.kt =====
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

// ===== shared/TypedAlgebraBridge.kt =====
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

// ===== tree/SegmentTree.kt =====
/**
 * Point-update, range-aggregate segment tree. The operation must be associative
 * and [initial] a two-sided identity. Supports empty input.
 * All ranges are half-open [from, until); query/set/findPrefix are O(log n).
 * findPrefix requires a monotone false->true predicate over prefixes.
 */
class SegmentTree<T>(values: List<T>, val initial: T, val combine: (T, T) -> T) {
    val size: Int = values.size
    private val base: Int
    private val tree: Array<Any?>

    init {
        require(size <= (1 shl 29)) { "Too many elements for an array-backed segment tree" }
        var power = 1
        while (power < size) power = power shl 1
        base = power
        tree = arrayOfNulls(base * 2)
        for (i in tree.indices) tree[i] = initial
        for (i in values.indices) tree[base + i] = values[i]
        for (i in base - 1 downTo 1) tree[i] = combine(item(i * 2), item(i * 2 + 1))
    }

    @Suppress("UNCHECKED_CAST")
    private fun item(index: Int): T = tree[index] as T

    /** Current element at index (0-based). */
    operator fun get(index: Int): T {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        return item(base + index)
    }

    /** Replace one value and recompute ancestors in O(log n). */
    operator fun set(index: Int, value: T) {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        var node = base + index
        tree[node] = value
        node /= 2
        while (node > 0) {
            tree[node] = combine(item(node * 2), item(node * 2 + 1))
            node /= 2
        }
    }

    /** Aggregate [from, until) in O(log n), preserving left-to-right order. */
    fun query(from: Int, until: Int): T {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid half-open range [$from,$until) for size $size"
        }
        var left = base + from
        var right = base + until
        var leftAcc = initial
        var rightAcc = initial
        while (left < right) {
            if ((left and 1) != 0) leftAcc = combine(leftAcc, item(left++))
            if ((right and 1) != 0) rightAcc = combine(item(--right), rightAcc)
            left /= 2
            right /= 2
        }
        return combine(leftAcc, rightAcc)
    }

    /** Inclusive Kotlin ranges: query(1..3) is equivalent to query(1, 4). */
    fun query(indices: IntRange): T {
        val until = indices.last.toLong() + 1L
        require(until <= size.toLong()) { "Range end out of bounds" }
        return query(indices.first, until.toInt())
    }

    /** Shorthand for an inclusive range query: tree[1..3]. */
    operator fun get(indices: IntRange): T = query(indices)

    /** Aggregate the whole array, or return identity when empty. */
    fun all(): T = item(1)

    /**
     * First index i such that predicate(combine(a[0],...,a[i])) is true.
     * Returns null when no prefix qualifies. O(log n), not O(log^2 n).
     * Predicate must be monotonic as prefixes grow (e.g. nonnegative SUM).
     */
    fun findPrefix(predicate: (T) -> Boolean): Int? {
        if (size == 0) return null
        if (predicate(initial)) return 0
        if (!predicate(item(1))) return null
        var node = 1
        var before = initial
        while (node < base) {
            val next = combine(before, item(node * 2))
            if (predicate(next)) node *= 2
            else {
                before = next
                node = node * 2 + 1
            }
        }
        val index = node - base
        return if (index < size) index else null
    }
}

/** Build a segment tree over arbitrary associative values (generic monoid). */
fun <T> List<T>.segmentTree(initial: T, combine: (T, T) -> T): SegmentTree<T> =
    SegmentTree(this, initial, combine)

/** Sum segment tree by default; pass a different identity/reducer if needed. */
fun IntArray.segmentTree(
    initial: Int = 0,
    combine: (Int, Int) -> Int = { a, b -> a + b }
): SegmentTree<Int> = SegmentTree(asList(), initial, combine)

fun LongArray.segmentTree(
    initial: Long = 0L,
    combine: (Long, Long) -> Long = { a, b -> a + b }
): SegmentTree<Long> = SegmentTree(asList(), initial, combine)

// ===== shared/SegmentTreeAggregations.kt =====
/**
 * Opt-in convenience adapters for the reusable aggregation operations.
 * Requires tree/SegmentTree.kt + shared/AggregationOp.kt, NOT graph code.
 */
fun IntArray.segmentTree(op: AggregateOp<Int>): SegmentTree<Int> =
    SegmentTree(asList(), op.initial, op.combine)

fun LongArray.segmentTree(op: AggregateOp<Long>): SegmentTree<Long> =
    SegmentTree(asList(), op.initial, op.combine)

fun <T> List<T>.segmentTree(op: AggregateOp<T>): SegmentTree<T> =
    SegmentTree(this, op.initial, op.combine)

fun <T> Array<out T>.segmentTree(op: AggregateOp<T>): SegmentTree<T> =
    SegmentTree(asList(), op.initial, op.combine)

// ===== range/SparseTables.kt =====
/**
 * Static O(1) range queries for an idempotent associative operation (MIN/MAX/GCD).
 * Build O(n log n). Overlapping power-of-two intervals are only safe if
 * combine(x,x) == x; e.g. SUM is NOT supported.
 */
class SparseTable<T>(values: List<T>, private val op: AggregateOp<T>) {
    val size: Int = values.size
    private val levels = ArrayList<List<T>>()

    init {
        require(op.idempotent) { "SparseTable requires an idempotent operation; use DisjointSparseTable for SUM" }
        if (size > 0) {
            levels.add(values.toList())
            var half = 1
            while (half <= size / 2) {
                val previous = levels.last()
                val block = half * 2
                val current = ArrayList<T>(size - block + 1)
                for (i in 0..size - block) current.add(op.combine(previous[i], previous[i + half]))
                levels.add(current)
                half = block
            }
        }
    }

    fun query(from: Int, until: Int): T {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        if (from == until) return op.initial
        val len = until - from
        val k = 31 - Integer.numberOfLeadingZeros(len)
        val step = 1 shl k
        return op.combine(levels[k][from], levels[k][until - step])
    }
}

/**
 * Static O(1) queries for ANY associative reducer, including SUM and
 * noncommutative operations such as string concatenation. O(n log n) build.
 */
class DisjointSparseTable<T>(values: List<T>, private val op: AggregateOp<T>) {
    val size: Int = values.size
    private val originals = values.toList()
    private val levels = ArrayList<List<T>>()

    init {
        var half = 1L
        while (half < size) {
            val data = originals.toMutableList()
            val block = half * 2
            var start = 0L
            while (start < size) {
                val center = minOf(start + half, size.toLong()).toInt()
                val end = minOf(start + block, size.toLong()).toInt()
                val begin = start.toInt()
                if (center > begin) {
                    data[center - 1] = originals[center - 1]
                    for (i in center - 2 downTo begin) data[i] = op.combine(originals[i], data[i + 1])
                }
                if (center < end) {
                    data[center] = originals[center]
                    for (i in center + 1 until end) data[i] = op.combine(data[i - 1], originals[i])
                }
                start += block
            }
            levels.add(data)
            half = block
        }
    }

    fun query(from: Int, until: Int): T {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
        if (from == until) return op.initial
        if (until == from + 1) return originals[from]
        val level = 31 - Integer.numberOfLeadingZeros(from xor (until - 1))
        return op.combine(levels[level][from], levels[level][until - 1])
    }
}

fun <T> List<T>.sparseTable(op: AggregateOp<T>): SparseTable<T> = SparseTable(this, op)
fun IntArray.sparseTable(op: AggregateOp<Int>): SparseTable<Int> = SparseTable(asList(), op)
fun LongArray.sparseTable(op: AggregateOp<Long>): SparseTable<Long> = SparseTable(asList(), op)

fun <T> List<T>.disjointSparseTable(op: AggregateOp<T>): DisjointSparseTable<T> = DisjointSparseTable(this, op)
fun IntArray.disjointSparseTable(op: AggregateOp<Int>): DisjointSparseTable<Int> = DisjointSparseTable(asList(), op)
fun LongArray.disjointSparseTable(op: AggregateOp<Long>): DisjointSparseTable<Long> = DisjointSparseTable(asList(), op)

// ===== tree/ConfigurableLazySegmentTree.kt =====
/**
 * An update action on a monoid: applying an update to an interval aggregate
 * and composing older and newer updates must preserve the aggregation.
 * `compose(old, new)` represents applying old BEFORE new.
 */
class RangeAction<T, U>(
    val apply: (aggregate: T, update: U, length: Int) -> T,
    val compose: (old: U, new: U) -> U
)

/**
 * Generic lazy segment tree. O(n) build, O(log n) range update/query,
 * O(n) memory, provided the supplied monoid action is O(1).
 */
class LazyAggregateTree<T, U>(values: List<T>, val op: AggregateOp<T>, val action: RangeAction<T, U>) {
    val size: Int = values.size
    private val tree: Array<Any?>
    private val pending: Array<Any?>
    private val hasPending: BooleanArray

    init {
        require(size <= (1 shl 28)) { "Array-backed tree size is too large" }
        val capacity = maxOf(4, size * 4)
        tree = arrayOfNulls(capacity)
        pending = arrayOfNulls(capacity)
        hasPending = BooleanArray(capacity)
        if (size > 0) build(1, 0, size, values)
    }

    @Suppress("UNCHECKED_CAST")
    private fun value(node: Int): T = tree[node] as T
    @Suppress("UNCHECKED_CAST")
    private fun delayed(node: Int): U = pending[node] as U

    private fun build(node: Int, lo: Int, hi: Int, values: List<T>) {
        if (hi - lo == 1) { tree[node] = values[lo]; return }
        val mid = lo + (hi - lo) / 2
        build(node * 2, lo, mid, values)
        build(node * 2 + 1, mid, hi, values)
        tree[node] = op.combine(value(node * 2), value(node * 2 + 1))
    }

    private fun applyNode(node: Int, length: Int, update: U) {
        tree[node] = action.apply(value(node), update, length)
        if (hasPending[node]) pending[node] = action.compose(delayed(node), update)
        else {
            hasPending[node] = true
            pending[node] = update
        }
    }

    private fun push(node: Int, lo: Int, hi: Int) {
        if (!hasPending[node] || hi - lo <= 1) return
        val mid = lo + (hi - lo) / 2
        val update = delayed(node)
        applyNode(node * 2, mid - lo, update)
        applyNode(node * 2 + 1, hi - mid, update)
        pending[node] = null
        hasPending[node] = false
    }

    private fun checkRange(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from, $until)" }
    }

    fun update(from: Int, until: Int, change: U) {
        checkRange(from, until)
        if (from != until) updateRec(1, 0, size, from, until, change)
    }

    private fun updateRec(node: Int, lo: Int, hi: Int, from: Int, until: Int, change: U) {
        if (from <= lo && hi <= until) { applyNode(node, hi - lo, change); return }
        push(node, lo, hi)
        val mid = lo + (hi - lo) / 2
        if (from < mid) updateRec(node * 2, lo, mid, from, until, change)
        if (until > mid) updateRec(node * 2 + 1, mid, hi, from, until, change)
        tree[node] = op.combine(value(node * 2), value(node * 2 + 1))
    }

    fun query(from: Int, until: Int): T {
        checkRange(from, until)
        return if (from == until) op.initial else queryRec(1, 0, size, from, until)
    }

    private fun queryRec(node: Int, lo: Int, hi: Int, from: Int, until: Int): T {
        if (from <= lo && hi <= until) return value(node)
        push(node, lo, hi)
        val mid = lo + (hi - lo) / 2
        if (until <= mid) return queryRec(node * 2, lo, mid, from, until)
        if (from >= mid) return queryRec(node * 2 + 1, mid, hi, from, until)
        return op.combine(queryRec(node * 2, lo, mid, from, until),
            queryRec(node * 2 + 1, mid, hi, from, until))
    }

    fun all(): T = if (size == 0) op.initial else value(1)
}

/** Declare valid update semantics for a custom aggregate; no silent guessing. */
fun <T, U> List<T>.lazySegmentTree(op: AggregateOp<T>, action: RangeAction<T, U>): LazyAggregateTree<T, U> =
    LazyAggregateTree(this, op, action)

/** Built-in range update modes, checked against each aggregation. */
enum class RangeUpdates { ADD, ASSIGN, ADD_ASSIGN }

internal sealed class NumericChange<T> {
    data class Add<T>(val delta: T) : NumericChange<T>()
    data class Assign<T>(val value: T) : NumericChange<T>()
}

class NumericLazySegmentTree<T> internal constructor(
    private val backing: LazyAggregateTree<T, NumericChange<T>>,
    val updates: RangeUpdates
) {
    val size: Int get() = backing.size
    fun query(from: Int, until: Int): T = backing.query(from, until)
    fun all(): T = backing.all()
    fun add(from: Int, until: Int, delta: T) {
        require(updates != RangeUpdates.ASSIGN) { "This tree only supports ASSIGN updates" }
        backing.update(from, until, NumericChange.Add(delta))
    }
    fun set(from: Int, until: Int, value: T) {
        require(updates != RangeUpdates.ADD) { "This tree only supports ADD updates" }
        backing.update(from, until, NumericChange.Assign(value))
    }
}

class IntLazyTreeConfig {
    var aggregate: AggregateOp<Int> = IntAggregations.SUM
    var updates: RangeUpdates = RangeUpdates.ADD_ASSIGN
}

class LongLazyTreeConfig {
    var aggregate: AggregateOp<Long> = LongAggregations.SUM
    var updates: RangeUpdates = RangeUpdates.ADD_ASSIGN
}

private fun <T> buildNumericTree(
    values: List<T>, op: AggregateOp<T>, mode: RangeUpdates,
    sum: AggregateOp<T>, min: AggregateOp<T>, max: AggregateOp<T>,
    xor: AggregateOp<T>, or: AggregateOp<T>, and: AggregateOp<T>,
    plus: (T, T) -> T, times: (T, Int) -> T, zero: T
): NumericLazySegmentTree<T> {
    val sumMode = op === sum
    val minOrMax = op === min || op === max
    val bitwise = op === xor || op === or || op === and
    require(sumMode || minOrMax || bitwise) {
        "Custom aggregation requires an explicit RangeAction (List.lazySegmentTree(op, action))"
    }
    require(!bitwise || mode == RangeUpdates.ASSIGN) {
        "Bitwise OR/AND/XOR support ASSIGN only; ADD does not preserve their interval aggregates"
    }
    val action = RangeAction<T, NumericChange<T>>(
        apply = { aggregate, change, length ->
            when (change) {
                is NumericChange.Add -> {
                    require(sumMode || minOrMax) { "ADD is unsupported for this aggregate" }
                    plus(aggregate, if (sumMode) times(change.delta, length) else change.delta)
                }
                is NumericChange.Assign -> when {
                    sumMode -> times(change.value, length)
                    minOrMax || op === or || op === and -> change.value
                    else -> if ((length and 1) == 0) zero else change.value
                }
            }
        },
        compose = { old, newer ->
            when (newer) {
                is NumericChange.Assign -> newer
                is NumericChange.Add -> when (old) {
                    is NumericChange.Assign -> NumericChange.Assign(plus(old.value, newer.delta))
                    is NumericChange.Add -> NumericChange.Add(plus(old.delta, newer.delta))
                }
            }
        }
    )
    return NumericLazySegmentTree(LazyAggregateTree(values, op, action), mode)
}

/**
 * Example: a.lazySegmentTree { aggregate = IntAggregations.MIN; updates = RangeUpdates.ADD }
 * Existing a.lazySegmentTree() (Long SUM) remains unchanged.
 */
fun IntArray.lazySegmentTree(config: IntLazyTreeConfig.() -> Unit): NumericLazySegmentTree<Int> {
    val c = IntLazyTreeConfig().apply(config)
    return buildNumericTree(asList(), c.aggregate, c.updates,
        IntAggregations.SUM, IntAggregations.MIN, IntAggregations.MAX,
        IntAggregations.XOR, IntAggregations.OR, IntAggregations.AND,
        { a, b -> a + b }, { value, length -> value * length }, 0)
}

fun LongArray.lazySegmentTree(config: LongLazyTreeConfig.() -> Unit): NumericLazySegmentTree<Long> {
    val c = LongLazyTreeConfig().apply(config)
    return buildNumericTree(asList(), c.aggregate, c.updates,
        LongAggregations.SUM, LongAggregations.MIN, LongAggregations.MAX,
        LongAggregations.XOR, LongAggregations.OR, LongAggregations.AND,
        { a, b -> a + b }, { value, length -> value * length }, 0L)
}

// ===== tree/PersistentAggregateTree.kt =====
/**
 * Generic persistent segment tree for any associative operation with identity.
 * Each update returns a new version ID, without mutating older versions.
 * O(n) initial build, O(log n) queries and point updates, O(log n) new nodes
 * per version. Order is retained for noncommutative operations.
 */
class PersistentAggregateTree<T>(values: List<T>, val op: AggregateOp<T>) {
    val size: Int = values.size
    private data class Node<T>(val value: T, val left: Node<T>?, val right: Node<T>?)
    private val roots = ArrayList<Node<T>?>()
    val versionCount: Int get() = roots.size

    init { roots.add(if (size == 0) null else build(values, 0, size)) }

    private fun build(values: List<T>, lo: Int, hi: Int): Node<T> {
        if (hi - lo == 1) return Node(values[lo], null, null)
        val mid = lo + (hi - lo) / 2
        val left = build(values, lo, mid)
        val right = build(values, mid, hi)
        return Node(op.combine(left.value, right.value), left, right)
    }

    private fun root(version: Int): Node<T>? {
        require(version in roots.indices) { "Version $version out of bounds" }
        return roots[version]
    }

    private fun checkIndex(index: Int) { require(index in 0 until size) { "Index $index out of bounds" } }
    private fun checkRange(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) { "Invalid range [$from,$until)" }
    }

    /** Replace a point and return a new, independent version. */
    fun set(version: Int, index: Int, value: T): Int {
        val previous = root(version)
        checkIndex(index)
        roots.add(replace(previous!!, 0, size, index, value))
        return roots.lastIndex
    }

    /** Combine a delta at a point. For SUM this adds; for XOR this toggles. */
    fun apply(version: Int, index: Int, delta: T): Int =
        set(version, index, op.combine(get(version, index), delta))

    private fun replace(node: Node<T>, lo: Int, hi: Int, index: Int, value: T): Node<T> {
        if (hi - lo == 1) return Node(value, null, null)
        val mid = lo + (hi - lo) / 2
        return if (index < mid) {
            val left = replace(node.left!!, lo, mid, index, value)
            Node(op.combine(left.value, node.right!!.value), left, node.right)
        } else {
            val right = replace(node.right!!, mid, hi, index, value)
            Node(op.combine(node.left!!.value, right.value), node.left, right)
        }
    }

    fun query(version: Int, from: Int, until: Int): T {
        val node = root(version)
        checkRange(from, until)
        return if (from == until) op.initial else range(node!!, 0, size, from, until)
    }

    private fun range(node: Node<T>, lo: Int, hi: Int, from: Int, until: Int): T {
        if (from <= lo && hi <= until) return node.value
        val mid = lo + (hi - lo) / 2
        if (until <= mid) return range(node.left!!, lo, mid, from, until)
        if (from >= mid) return range(node.right!!, mid, hi, from, until)
        return op.combine(range(node.left!!, lo, mid, from, until),
            range(node.right!!, mid, hi, from, until))
    }

    fun get(version: Int, index: Int): T {
        checkIndex(index)
        return query(version, index, index + 1)
    }

    fun all(version: Int): T = root(version)?.value ?: op.initial
}

fun IntArray.persistentSegmentTree(op: AggregateOp<Int>): PersistentAggregateTree<Int> =
    PersistentAggregateTree(asList(), op)
fun LongArray.persistentSegmentTree(op: AggregateOp<Long>): PersistentAggregateTree<Long> =
    PersistentAggregateTree(asList(), op)
fun <T> List<T>.persistentSegmentTree(op: AggregateOp<T>): PersistentAggregateTree<T> =
    PersistentAggregateTree(this, op)
fun <T> Array<out T>.persistentSegmentTree(op: AggregateOp<T>): PersistentAggregateTree<T> =
    PersistentAggregateTree(asList(), op)

// ===== tree/TypedAlgebraTrees.kt =====
/**
 * Trees need only an associative operation with a two-sided identity.
 * These overloads delegate to existing battle-tested implementations.
 */

// Ordinary segment trees accept any Monoid, including noncommutative ones.
fun IntArray.segmentTree(op: Monoid<Int>): SegmentTree<Int> = segmentTree(op.asAggregateOp())
fun LongArray.segmentTree(op: Monoid<Long>): SegmentTree<Long> = segmentTree(op.asAggregateOp())
fun <T> List<T>.segmentTree(op: Monoid<T>): SegmentTree<T> = segmentTree(op.asAggregateOp())
fun <T> Array<out T>.segmentTree(op: Monoid<T>): SegmentTree<T> = segmentTree(op.asAggregateOp())

// Historical versions also only need a Monoid.
fun IntArray.persistentSegmentTree(op: Monoid<Int>): PersistentAggregateTree<Int> =
    persistentSegmentTree(op.asAggregateOp())
fun LongArray.persistentSegmentTree(op: Monoid<Long>): PersistentAggregateTree<Long> =
    persistentSegmentTree(op.asAggregateOp())
fun <T> List<T>.persistentSegmentTree(op: Monoid<T>): PersistentAggregateTree<T> =
    persistentSegmentTree(op.asAggregateOp())
fun <T> Array<out T>.persistentSegmentTree(op: Monoid<T>): PersistentAggregateTree<T> =
    persistentSegmentTree(op.asAggregateOp())

/**
 * Lazy updates require both a Monoid and a compatible MonoidAction.
 * An arbitrary lambda that merely changes individual values is not enough:
 * apply must transform the aggregate of the entire interval.
 */
fun <T, U> List<T>.lazySegmentTree(
    op: Monoid<T>,
    action: MonoidAction<T, U>
): LazyAggregateTree<T, U> =
    lazySegmentTree(
        op.asAggregateOp(),
        RangeAction(
            apply = { aggregate, update, length -> action.apply(aggregate, update, length) },
            compose = { old, newer -> action.compose(old, newer) }
        )
    )

fun <U> IntArray.lazySegmentTree(
    op: Monoid<Int>,
    action: MonoidAction<Int, U>
): LazyAggregateTree<Int, U> = asList().lazySegmentTree(op, action)

fun <U> LongArray.lazySegmentTree(
    op: Monoid<Long>,
    action: MonoidAction<Long, U>
): LazyAggregateTree<Long, U> = asList().lazySegmentTree(op, action)

fun <T, U> Array<out T>.lazySegmentTree(
    op: Monoid<T>,
    action: MonoidAction<T, U>
): LazyAggregateTree<T, U> = asList().lazySegmentTree(op, action)

/**
 * All typed monoids support range assignments, because x repeated n times
 * has a well-defined aggregate. The default repeat uses O(log n) combines.
 */
fun <T> List<T>.lazySegmentTree(
    op: Monoid<T>,
    repeatValue: ((value: T, count: Int) -> T)? = null
): AssignmentLazyTree<T> = lazySegmentTree(op.asAggregateOp(), repeatValue)

fun IntArray.lazySegmentTree(
    op: Monoid<Int>,
    repeatValue: ((value: Int, count: Int) -> Int)? = null
): AssignmentLazyTree<Int> = asList().lazySegmentTree(op, repeatValue)

fun LongArray.lazySegmentTree(
    op: Monoid<Long>,
    repeatValue: ((value: Long, count: Int) -> Long)? = null
): AssignmentLazyTree<Long> = asList().lazySegmentTree(op, repeatValue)

fun <T> Array<out T>.lazySegmentTree(
    op: Monoid<T>,
    repeatValue: ((value: T, count: Int) -> T)? = null
): AssignmentLazyTree<T> = asList().lazySegmentTree(op, repeatValue)

// ===== tree/AssignmentLazyTree.kt =====
/**
 * Compute value combined with itself `count` times in O(log count)
 * operations (binary exponentiation in an arbitrary associative monoid).
 * Works for noncommutative operations because every factor is `value`.
 */
private fun <T> monoidPower(value: T, count: Int, op: AggregateOp<T>): T {
    require(count >= 0) { "Repeat count must be nonnegative" }
    var remaining = count
    var power = value
    var result = op.initial
    while (remaining > 0) {
        if ((remaining and 1) != 0) result = op.combine(result, power)
        remaining = remaining ushr 1
        if (remaining > 0) power = op.combine(power, power)
    }
    return result
}

/**
 * Lazy segment tree supporting range ASSIGN for any lawful monoid.
 *
 * Assigning `x` to an entire segment replaces its aggregate with
 * x combined with itself `segmentLength` times.
 *
 * With the default logarithmic monoid power: O(log² n) assignment,
 * O(log n) range queries, O(n) construction and memory.
 * If `repeatValue(x, length)` is an O(1) lawful implementation
 * (e.g. SUM -> x * length), assignment improves to O(log n).
 *
 * All ranges are half-open; works with noncommutative aggregates.
 */
class AssignmentLazyTree<T> internal constructor(
    values: List<T>,
    val op: AggregateOp<T>,
    repeatValue: (value: T, count: Int) -> T
) {
    private val backing = LazyAggregateTree(
        values, op,
        RangeAction<T, T>(
            apply = { _, value, length -> repeatValue(value, length) },
            compose = { _, newer -> newer } // Last assignment wins.
        )
    )

    val size: Int get() = backing.size

    /** Replace every element of [from, until) with the given value. */
    fun set(from: Int, until: Int, value: T) = backing.update(from, until, value)

    /** Alias to match the generic LazyAggregateTree vocabulary. */
    fun update(from: Int, until: Int, value: T) = set(from, until, value)

    fun query(from: Int, until: Int): T = backing.query(from, until)

    fun all(): T = backing.all()
}

/**
 * Assign whole intervals under a custom associative operation.
 * A more general update requires an explicit RangeAction.
 */
fun <T> List<T>.lazySegmentTree(
    op: AggregateOp<T>,
    repeatValue: ((value: T, count: Int) -> T)? = null
): AssignmentLazyTree<T> = AssignmentLazyTree(
    this, op, repeatValue ?: { value, count -> monoidPower(value, count, op) }
)

fun IntArray.lazySegmentTree(
    op: AggregateOp<Int>,
    repeatValue: ((value: Int, count: Int) -> Int)? = null
): AssignmentLazyTree<Int> = asList().lazySegmentTree(op, repeatValue)

fun LongArray.lazySegmentTree(
    op: AggregateOp<Long>,
    repeatValue: ((value: Long, count: Int) -> Long)? = null
): AssignmentLazyTree<Long> = asList().lazySegmentTree(op, repeatValue)

fun <T> Array<out T>.lazySegmentTree(
    op: AggregateOp<T>,
    repeatValue: ((value: T, count: Int) -> T)? = null
): AssignmentLazyTree<T> = asList().lazySegmentTree(op, repeatValue)

// ===== range/RangeIndex.kt =====
/**
 * The chosen implementation for rangeIndex(op, mutable=...).
 * STATIC_IDEMPOTENT: sparse table, STATIC_GENERAL: disjoint sparse table,
 * POINT_UPDATES: segment tree.
 */
enum class RangeIndexStrategy { STATIC_IDEMPOTENT, STATIC_GENERAL, POINT_UPDATES }

/**
 * A convenient facade that selects a lawful data structure for the requested
 * usage. Static query O(1) after O(n log n) build; mutable point update and
 * query O(log n) after O(n) build. Selection never guesses associativity:
 * callers must provide an actual monoid and correctly declare idempotency.
 */
class RangeIndex<T>(values: List<T>, op: AggregateOp<T>, mutable: Boolean) {
    val size: Int = values.size
    val strategy: RangeIndexStrategy = when {
        mutable -> RangeIndexStrategy.POINT_UPDATES
        op.idempotent -> RangeIndexStrategy.STATIC_IDEMPOTENT
        else -> RangeIndexStrategy.STATIC_GENERAL
    }
    private val segment: SegmentTree<T>? = if (mutable) SegmentTree(values, op.initial, op.combine) else null
    private val sparse: SparseTable<T>? = if (strategy == RangeIndexStrategy.STATIC_IDEMPOTENT) SparseTable(values, op) else null
    private val disjoint: DisjointSparseTable<T>? = if (strategy == RangeIndexStrategy.STATIC_GENERAL) DisjointSparseTable(values, op) else null

    fun query(from: Int, until: Int): T = when (strategy) {
        RangeIndexStrategy.POINT_UPDATES -> segment!!.query(from, until)
        RangeIndexStrategy.STATIC_IDEMPOTENT -> sparse!!.query(from, until)
        RangeIndexStrategy.STATIC_GENERAL -> disjoint!!.query(from, until)
    }

    /** Only valid if mutable=true. */
    fun set(index: Int, value: T) {
        check(strategy == RangeIndexStrategy.POINT_UPDATES) {
            "rangeIndex was built as static; construct with mutable=true for point updates"
        }
        segment!![index] = value
    }

    fun all(): T = query(0, size)
}

fun <T> List<T>.rangeIndex(op: AggregateOp<T>, mutable: Boolean = false): RangeIndex<T> =
    RangeIndex(this, op, mutable)
fun <T> Array<out T>.rangeIndex(op: AggregateOp<T>, mutable: Boolean = false): RangeIndex<T> =
    asList().rangeIndex(op, mutable)
fun IntArray.rangeIndex(op: AggregateOp<Int>, mutable: Boolean = false): RangeIndex<Int> =
    asList().rangeIndex(op, mutable)
fun LongArray.rangeIndex(op: AggregateOp<Long>, mutable: Boolean = false): RangeIndex<Long> =
    asList().rangeIndex(op, mutable)

/** With range updates, choose a generic lazy action instead of a static index. */
fun <T, U> List<T>.rangeIndex(op: AggregateOp<T>, action: RangeAction<T, U>): LazyAggregateTree<T, U> =
    LazyAggregateTree(this, op, action)
fun <T, U> Array<out T>.rangeIndex(op: AggregateOp<T>, action: RangeAction<T, U>): LazyAggregateTree<T, U> =
    asList().rangeIndex(op, action)
fun <U> IntArray.rangeIndex(op: AggregateOp<Int>, action: RangeAction<Int, U>): LazyAggregateTree<Int, U> =
    asList().rangeIndex(op, action)
fun <U> LongArray.rangeIndex(op: AggregateOp<Long>, action: RangeAction<Long, U>): LazyAggregateTree<Long, U> =
    asList().rangeIndex(op, action)

/** Typed monoids preserve their declared idempotency when selecting an index. */
fun <T> List<T>.rangeIndex(op: Monoid<T>, mutable: Boolean = false): RangeIndex<T> =
    rangeIndex(op.asAggregateOp(), mutable)
fun <T> Array<out T>.rangeIndex(op: Monoid<T>, mutable: Boolean = false): RangeIndex<T> =
    asList().rangeIndex(op, mutable)
fun IntArray.rangeIndex(op: Monoid<Int>, mutable: Boolean = false): RangeIndex<Int> =
    asList().rangeIndex(op, mutable)
fun LongArray.rangeIndex(op: Monoid<Long>, mutable: Boolean = false): RangeIndex<Long> =
    asList().rangeIndex(op, mutable)

/** Typed range-update actions select a LazyAggregateTree with the full action law. */
fun <T, U> List<T>.rangeIndex(op: Monoid<T>, action: MonoidAction<T, U>): LazyAggregateTree<T, U> =
    lazySegmentTree(op, action)
fun <T, U> Array<out T>.rangeIndex(op: Monoid<T>, action: MonoidAction<T, U>): LazyAggregateTree<T, U> =
    asList().rangeIndex(op, action)
fun <U> IntArray.rangeIndex(op: Monoid<Int>, action: MonoidAction<Int, U>): LazyAggregateTree<Int, U> =
    asList().rangeIndex(op, action)
fun <U> LongArray.rangeIndex(op: Monoid<Long>, action: MonoidAction<Long, U>): LazyAggregateTree<Long, U> =
    asList().rangeIndex(op, action)

// ===== range/RangeProgram.kt =====
/**
 * A tiny deferred language for ordered range queries and point assignments.
 *
 * `query` writes one answer in query order; `set` changes later queries.
 * Commands are recorded first so the existing RangeIndex can choose a static
 * sparse-table strategy when there are no writes, otherwise a segment tree.
 *
 * The block is a description, not immediate execution: query() returns Unit.
 * Use rangeProgram(...).answers to inspect the computed answers afterward.
 */
@DslMarker
annotation class RangeProgramDsl

internal sealed interface RangeProgramStep<T> {
    class Query<T>(val from: Int, val until: Int) : RangeProgramStep<T>
    class Set<T>(val index: Int, val value: T) : RangeProgramStep<T>
}

/** All query outputs in their original order, plus the chosen data structure. */
data class RangeProgramResult<T>(
    val answers: List<T>,
    val strategy: RangeIndexStrategy
) {
    operator fun get(queryIndex: Int): T = answers[queryIndex]
}

/**
 * Declarative command recorder for a single `rangeProgram` invocation.
 *
 * Bounds are validated when commands are recorded. Only point assignments
 * are supported; arbitrary lazy range updates need a compatible action.
 */
@RangeProgramDsl
class RangeProgramScope<T> internal constructor(private val size: Int) {
    private val steps = ArrayList<RangeProgramStep<T>>()
    private var hasAssignments = false

    /** Add a query over the half-open range [from, until). */
    fun query(from: Int, until: Int) {
        require(from >= 0 && from <= until && until <= size) {
            "Invalid half-open range [$from,$until) for size $size"
        }
        steps.add(RangeProgramStep.Query(from, until))
    }

    /** Add a query over an inclusive Kotlin range (e.g. 1..3). */
    fun query(indices: IntRange) {
        val until = indices.last.toLong() + 1L
        require(until <= size.toLong()) { "Range end out of bounds for size $size" }
        query(indices.first, until.toInt())
    }

    /** Assign one index for all subsequent queries. */
    fun set(index: Int, value: T) {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        hasAssignments = true
        steps.add(RangeProgramStep.Set(index, value))
    }

    internal fun execute(snapshot: List<T>, operation: AggregateOp<T>): RangeProgramResult<T> {
        val index = snapshot.rangeIndex(operation, mutable = hasAssignments)
        val answers = ArrayList<T>()
        for (step in steps) {
            when (step) {
                is RangeProgramStep.Query -> answers.add(index.query(step.from, step.until))
                is RangeProgramStep.Set -> index.set(step.index, step.value)
            }
        }
        return RangeProgramResult(answers, index.strategy)
    }
}

/** Snapshot the input before recording, then execute the commands in order. */
fun <T> List<T>.rangeProgram(
    operation: AggregateOp<T>,
    block: RangeProgramScope<T>.() -> Unit
): RangeProgramResult<T> {
    val snapshot = toList()
    val scope = RangeProgramScope<T>(snapshot.size)
    scope.block()
    return scope.execute(snapshot, operation)
}

/** Typed monoids preserve their idempotency when selecting a static backend. */
fun <T> List<T>.rangeProgram(
    operation: Monoid<T>,
    block: RangeProgramScope<T>.() -> Unit
): RangeProgramResult<T> = rangeProgram(operation.asAggregateOp(), block)

fun <T> Array<out T>.rangeProgram(
    operation: AggregateOp<T>,
    block: RangeProgramScope<T>.() -> Unit
): RangeProgramResult<T> = asList().rangeProgram(operation, block)

fun <T> Array<out T>.rangeProgram(
    operation: Monoid<T>,
    block: RangeProgramScope<T>.() -> Unit
): RangeProgramResult<T> = asList().rangeProgram(operation, block)

fun IntArray.rangeProgram(
    operation: AggregateOp<Int>,
    block: RangeProgramScope<Int>.() -> Unit
): RangeProgramResult<Int> = asList().rangeProgram(operation, block)

fun IntArray.rangeProgram(
    operation: Monoid<Int>,
    block: RangeProgramScope<Int>.() -> Unit
): RangeProgramResult<Int> = asList().rangeProgram(operation, block)

fun LongArray.rangeProgram(
    operation: AggregateOp<Long>,
    block: RangeProgramScope<Long>.() -> Unit
): RangeProgramResult<Long> = asList().rangeProgram(operation, block)

fun LongArray.rangeProgram(
    operation: Monoid<Long>,
    block: RangeProgramScope<Long>.() -> Unit
): RangeProgramResult<Long> = asList().rangeProgram(operation, block)

// ===== solution =====
fun main() {
    val n = readInt()
    val xs = IntArray(n)
    repeat(n) { i ->
        xs[i] = readInt()
        readInt()
    }
    val sum = monoid(0) { a: Int, b: Int -> a + b }
    val seen = IntArray(32001)
    val levels = IntArray(32001).rangeProgram(sum) {
        for (x in xs) {
            query(0, x + 1)
            seen[x]++
            set(x, seen[x])
        }
    }.answers
    val counts = IntArray(n)
    for (level in levels) counts[level]++
    print(counts.joinToString("\n"))
}
