class SAM(s: String) {
    inner class State(var len: Int = 0, var link: Int = -1) {
        val next = HashMap<Char, Int>()
        var cnt = 0L
        var visited = false
    }

    private val st = ArrayList<State>()
    private var last = 0

    init {
        st.add(State())
        for (c in s) extend(c)
    }

    private fun extend(c: Char) {
        val cur = st.size
        st.add(State(st[last].len + 1))
        st[cur].cnt = 1
        var p = last
        while (p != -1 && !st[p].next.containsKey(c)) {
            st[p].next[c] = cur; p = st[p].link
        }
        if (p == -1) {
            st[cur].link = 0
        } else {
            val q = st[p].next[c]!!
            if (st[p].len + 1 == st[q].len) {
                st[cur].link = q
            } else {
                val clone = st.size
                st.add(State(st[p].len + 1, st[q].link))
                st[clone].next.putAll(st[q].next)
                while (p != -1 && st[p].next[c] == q) {
                    st[p].next[c] = clone; p = st[p].link
                }
                st[q].link = clone; st[cur].link = clone
            }
        }
        last = cur
    }

    val size get() = st.size

    private fun topoOrder(): List<Int> {
        val order = (0 until st.size).sortedByDescending { st[it].len }
        return order
    }

    private fun computeCnt(): LongArray {
        val cnt = LongArray(st.size) { st[it].cnt }
        for (v in topoOrder()) {
            val lnk = st[v].link
            if (lnk >= 0) cnt[lnk] += cnt[v]
        }
        return cnt
    }

    fun distinctSubstrings(): Long {
        var ans = 0L
        for (i in 1 until st.size) ans += (st[i].len - st[st[i].link].len).toLong()
        return ans
    }

    fun occurrences(): LongArray = computeCnt()

    fun contains(t: String): Boolean {
        var cur = 0
        for (c in t) {
            cur = st[cur].next[c] ?: return false
        }
        return true
    }
}
