import kotlin.random.Random

fun main() {
    val rng = Random(9561)
    repeat(200) {
        val n = rng.nextInt(1,100)
        val p = IntArray(n) { if (it == 0) -1 else rng.nextInt(it) }
        val tree = VirtualTreeIndex(p)
        fun ancestors(v: Int): List<Int> {
            val out = ArrayList<Int>(); var u = v
            while (u >= 0) { out.add(u); u = p[u] }
            return out
        }
        fun brute(a: Int,b: Int): Int = ancestors(a).first { it in ancestors(b) }
        val marked = IntArray(rng.nextInt(0,30)) { rng.nextInt(n) }
        val vt = tree.build(marked)
        val closure = marked.toMutableSet()
        for (a in marked) for (b in marked) {
            val c = brute(a,b); closure.add(c)
            check(tree.lca(a,b) == c)
        }
        check(vt.vertices.toSet() == closure)
        for (i in vt.vertices.indices) {
            val v = vt.vertices[i]
            val expected = ancestors(v).drop(1).firstOrNull { it in closure }
            check((if (vt.parent[i] < 0) null else vt.vertices[vt.parent[i]]) == expected)
            if (expected != null) check(vt.edgeLength[i] == ancestors(v).indexOf(expected))
        }
    }
    val chain = VirtualTreeIndex(IntArray(100000){it-1})
    check(chain.build(intArrayOf(0,99999)).edgeLength.contentEquals(intArrayOf(0,99999)))
    repeat(500) {
        val rows = rng.nextInt(0,50); val cols = rng.nextInt(1,60)
        val center = IntArray(rows){rng.nextInt(-20,80)}.sorted()
        val offset = LongArray(cols){rng.nextLong(-100,100)}
        fun cost(i: Int,j: Int): Long = (j-center[i]).toLong()*(j-center[i])+offset[j]
        val actual = smawkRowMinima(rows,cols,::cost)
        for (i in 0 until rows) check(actual[i] == (0 until cols).minByOrNull { cost(i,it) })
    }
    check(smawkRowMinima(5,7){_,_->0L}.contentEquals(IntArray(5)))
    var calls = 0
    val minima = smawkRowMinima(10000,10000){i,j->calls++; (i-j).toLong()*(i-j)}
    check(minima.indices.all { minima[it] == it } && calls < 400000)
    fun norm(x: Long): Int = (((x%NTT_MOD)+NTT_MOD)%NTT_MOD).toInt()
    fun pow(a: Int,e0: Int): Int {
        var x = a; var e = e0; var y = 1
        while (e > 0) { if (e and 1 != 0) y = norm(y.toLong()*x); x = norm(x.toLong()*x); e = e ushr 1 }
        return y
    }
    repeat(160) {
        val n = rng.nextInt(1,70)
        val a = IntArray(n){rng.nextInt(-100,101)}; a[0] = 1
        val inv = fpsInverse(a,n)
        for (i in 0 until n) {
            var expected = 0
            for (j in 0..i) expected = norm(expected+a[j].toLong()*inv[i-j])
            check(expected == if (i == 0) 1 else 0)
        }
        val log = fpsLog(a,n)
        check(fpsExp(log,n).contentEquals(IntArray(n){norm(a[it].toLong())}))
        a[0] = 0
        val exp = fpsExp(a,n)
        val brute = IntArray(n); brute[0] = 1
        for (i in 1 until n) {
            var sum = 0
            for (j in 1..i) sum = norm(sum+norm(j.toLong()*a[j]).toLong()*brute[i-j])
            brute[i] = norm(sum.toLong()*pow(i,NTT_MOD-2))
        }
        check(exp.contentEquals(brute))
        check(fpsLog(exp,n).contentEquals(IntArray(n){norm(a[it].toLong())}))
    }
    check(fpsInverse(intArrayOf(),0).isEmpty())
    check(runCatching { fpsInverse(intArrayOf(0),1) }.isFailure)
    val exp = fpsExp(intArrayOf(0,1),8192)
    for (i in 1 until exp.size) check(norm(exp[i].toLong()*i) == exp[i-1])
    println("PASS: virtual trees, SMAWK and formal power series oracles")
}
