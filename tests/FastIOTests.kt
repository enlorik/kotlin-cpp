import java.io.ByteArrayInputStream
import java.io.StringWriter

private fun assertIO(ok: Boolean, message: String) { if (!ok) error(message) }
private inline fun expectIOFailure(block: () -> Unit) {
    var failed = false
    try { block() } catch (_: IllegalArgumentException) { failed = true }
    assertIO(failed, "Expected IllegalArgumentException")
}

fun main() {
    val scanner = FastInput(ByteArrayInputStream("\n 3 \t 4\r\n 1 2  3 4\n5 6 7 8\n9 10 11 12\n-42\t9223372036854775807 hello\n".toByteArray()))
    val (h, w) = scanner.readInts()
    assertIO(h == 3 && w == 4, "two-dimension destructuring")
    val matrix = scanner.readIntGrid(h, w)
    assertIO(matrix[0].contentEquals(intArrayOf(1, 2, 3, 4)), "first row")
    assertIO(matrix[2].contentEquals(intArrayOf(9, 10, 11, 12)), "last row")
    assertIO(scanner.nextInt() == -42, "negative integer")
    assertIO(scanner.nextLong() == Long.MAX_VALUE, "long")
    assertIO(scanner.nextToken() == "hello", "token")
    var eof = false
    try { scanner.nextInt() } catch (_: NoSuchElementException) { eof = true }
    assertIO(eof, "EOF detection")

    val numbers = FastInput(ByteArrayInputStream("-2147483648 2147483647 -9223372036854775808".toByteArray()))
    assertIO(numbers.nextInt() == Int.MIN_VALUE, "int min")
    assertIO(numbers.nextInt() == Int.MAX_VALUE, "int max")
    assertIO(numbers.nextLong() == Long.MIN_VALUE, "long min")

    val chars = FastInput(ByteArrayInputStream(".##\n#..".toByteArray())).readCharGrid(2, 3)
    assertIO(chars[0].concatToString() == ".##" && chars[1].concatToString() == "#..", "char grid")
    expectIOFailure { FastInput(ByteArrayInputStream("..".toByteArray())).readCharGrid(1, 3) }
    expectIOFailure { FastInput().readInts(-1) }
    expectIOFailure { FastInput().readIntGrid(-1, 2) }
    expectIOFailure { FastInput().readLongs(-1) }

    val writer = StringWriter()
    val printer = FastOutput(writer)
    printer.printGrid(2, 3) { r, c -> r * 3 + c }
    assertIO(writer.toString().replace("\r\n", "\n") == "0 1 2\n3 4 5\n", "computed grid formatting")
    printer.printGrid(arrayOf(intArrayOf(1, 2), intArrayOf(3, 4)))
    printer.printGrid(arrayOf(longArrayOf(10000000000L, 0L)))
    assertIO(writer.toString().replace("\r\n", "\n").endsWith("1 2\n3 4\n10000000000 0\n"), "grid overloads")
    expectIOFailure { printer.printGrid(-1, 3) { _, _ -> 0 } }
    expectIOFailure { printer.printGrid(arrayOf(intArrayOf(1), intArrayOf(2,3))) }
    printer.printGrid(1, 0) { _, _ -> error("should not run") }
    assertIO(writer.toString().replace("\r\n", "\n").endsWith("10000000000 0\n\n"), "zero columns")

    val bufferInput = StringBuilder()
    repeat(100_000) { bufferInput.append(it).append(' ') }
    val many = FastInput(ByteArrayInputStream(bufferInput.toString().toByteArray()))
    for (i in 0 until 100_000) assertIO(many.nextInt() == i, "buffer boundary at $i")
    println("PASS: 100,000 input tokens, int/long edges, grids, formatting, bounds, EOF")
}
