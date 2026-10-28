import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStreamWriter
import java.io.Writer

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
