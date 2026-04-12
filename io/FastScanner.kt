import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StreamTokenizer

class FastScanner {
    private val br = BufferedReader(InputStreamReader(System.`in`))
    private val st = StreamTokenizer(br)

    fun nextInt(): Int {
        st.nextToken()
        return st.nval.toInt()
    }

    fun nextLong(): Long {
        st.nextToken()
        return st.nval.toLong()
    }

    /** Reads the next whitespace-delimited token as a String (including tokens with digits). */
    fun next(): String {
        st.ordinaryChars('0'.code, '9'.code)
        st.wordChars('0'.code, '9'.code)
        st.nextToken()
        val s = st.sval
        st.parseNumbers()
        return s
    }

    fun nextLine(): String = br.readLine() ?: ""

    fun intArray(n: Int): IntArray = IntArray(n) { nextInt() }

    fun longArray(n: Int): LongArray = LongArray(n) { nextLong() }

    fun intMatrix(n: Int, m: Int): Array<IntArray> = Array(n) { intArray(m) }
}
