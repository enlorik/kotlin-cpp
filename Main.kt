// Main.kt — Sample competitive programming solution
// Bundler directives:
//   //%AUTO         — auto-include files based on symbol references
//   //%include path — manually include a file
//   //%exclude path — manually exclude a file
//
//%AUTO

fun main() {
    val sc = FastScanner()
    val n = sc.nextInt()
    val a = sc.longArray(n)

    // Example: range sum with Fenwick tree
    val fw = fw(n, 0L, { x, y -> x + y }, { x, y -> x - y })
    for (i in 0 until n) fw.update(i + 1, a[i])

    val q = sc.nextInt()
    val sb = StringBuilder()
    repeat(q) {
        val l = sc.nextInt()
        val r = sc.nextInt()
        sb.appendLine(fw.query(l, r))
    }
    print(sb)
}
