class AhoCorasick {
    private val ALPHA = 26
    private val go = ArrayList<IntArray>()
    private val fail = ArrayList<Int>()
    private val out = ArrayList<MutableList<Int>>()
    private var built = false

    init { newNode() }

    private fun newNode(): Int {
        go.add(IntArray(ALPHA) { -1 })
        fail.add(0)
        out.add(mutableListOf())
        return go.size - 1
    }

    fun addPattern(s: String, id: Int = 0) {
        var cur = 0
        for (c in s) {
            val ci = c - 'a'
            if (go[cur][ci] == -1) go[cur][ci] = newNode()
            cur = go[cur][ci]
        }
        out[cur].add(id)
    }

    fun build() {
        val q = ArrayDeque<Int>()
        for (c in 0 until ALPHA) {
            if (go[0][c] == -1) go[0][c] = 0
            else { fail[go[0][c]] = 0; q.add(go[0][c]) }
        }
        while (q.isNotEmpty()) {
            val u = q.removeFirst()
            out[u].addAll(out[fail[u]])
            for (c in 0 until ALPHA) {
                if (go[u][c] == -1) {
                    go[u][c] = go[fail[u]][c]
                } else {
                    fail[go[u][c]] = go[fail[u]][c]
                    q.add(go[u][c])
                }
            }
        }
        built = true
    }

    fun search(text: String): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        var cur = 0
        for ((i, c) in text.withIndex()) {
            cur = go[cur][c - 'a']
            for (id in out[cur]) result.add(i to id)
        }
        return result
    }

    fun countAll(text: String): Long {
        var cnt = 0L
        var cur = 0
        for (c in text) {
            cur = go[cur][c - 'a']
            cnt += out[cur].size
        }
        return cnt
    }
}
