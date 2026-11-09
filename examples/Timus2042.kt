/** Timus 2042. Nikita: all palindrome mathematics comes from the reusable index. */
fun main() {
    val index = readToken().boundedPalindromes(readInt())
    val output = StringBuilder()
    repeat(readInt()) {
        val type = readInt()
        val from = readInt() - 1
        val until = readInt()
        if (type == 1) index.assign(from, until, readToken()[0])
        else output.append(index.count(from, until)).append('\n')
    }
    print(output)
}
