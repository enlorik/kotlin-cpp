import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter

class FastOutput {
    private val pw = PrintWriter(BufferedWriter(OutputStreamWriter(System.out)))

    fun print(x: Any) = pw.print(x)

    fun println(x: Any) = pw.println(x)

    fun println() = pw.println()

    fun flush() = pw.flush()
}

val out by lazy { FastOutput() }
