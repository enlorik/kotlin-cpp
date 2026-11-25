import kotlin.random.Random

fun main() {
    val rng = Random(82823)
    repeat(350) {
        val s = CharArray(rng.nextInt(0,70)) { "abc\u0000\uffff"[rng.nextInt(5)] }.concatToString()
        val t = CharArray(rng.nextInt(0,70)) { "abc\u0000\uffff"[rng.nextInt(5)] }.concatToString()
        val a = s.rollingHash(); val b = t.rollingHash()
        repeat(30) {
            val l = rng.nextInt(s.length+1); val r = rng.nextInt(l,s.length+1)
            val x = rng.nextInt(t.length+1); val y = rng.nextInt(x,t.length+1)
            val expected = s.substring(l,r) == t.substring(x,y)
            check(a.equalVerified(l,r,b,x,y) == expected)
            if (expected) check(a.probablyEqual(l,r,b,x,y))
            val part = s.substring(l,r).rollingHash()
            check(a.fingerprint(l,r) == part.fingerprint(0,r-l))
        }
    }
    repeat(220) {
        val n = rng.nextInt(1,12)
        val p = IntArray(n) { if (it == 0) -1 else rng.nextInt(it) }
        val graph = SparseGraph(n)
        for (v in 1 until n) graph.addEdge(v,p[v])
        val root = rng.nextInt(n)
        val tree = graph.rootedTree(root)
        val actualParents = tree.parents()
        val weights = LongArray(n) { rng.nextLong(-10,11) }
        val expected = arrayOfNulls<Long>(n+1); expected[0] = 0L
        for (mask in 1 until (1 shl n)) {
            if (mask and (1 shl root) == 0) continue
            var valid = true; var sum = 0L
            for (v in 0 until n) if (mask and (1 shl v) != 0) {
                if (v != root && mask and (1 shl actualParents[v]) == 0) valid = false
                sum += weights[v]
            }
            if (valid) {
                val k = Integer.bitCount(mask)
                expected[k] = expected[k]?.let { maxOf(it,sum) } ?: sum
            }
        }
        check(tree.connectedKnapsack(weights,n) == expected.toList())
        val cap = rng.nextInt(n+1)
        check(tree.connectedKnapsack(weights,cap) == expected.take(cap+1))
        val index = graph.virtualTreeIndex(root)
        var diameter = 0
        for (v in 0 until n) for (u in 0 until n) diameter = maxOf(diameter,index.distance(u,v))
        val d = tree.diameter()
        check(d.length == diameter && index.distance(d.first,d.second) == diameter)
        val cent = tree.centroidParents()
        check(cent.count { it == -1 } == 1)
        fun checkComponent(nodes: Set<Int>,parent: Int) {
            if (nodes.isEmpty()) return
            val c = nodes.single { cent[it] == parent }
            val remaining = (nodes-c).toMutableSet()
            while (remaining.isNotEmpty()) {
                val start = remaining.first()
                val group = mutableSetOf(start); remaining.remove(start)
                val queue = ArrayDeque<Int>(); queue.add(start)
                while (queue.isNotEmpty()) for (e in graph.g[queue.removeFirst()])
                    if (remaining.remove(e.to)) { group.add(e.to); queue.add(e.to) }
                check(group.size <= nodes.size/2)
                checkComponent(group,c)
            }
        }
        checkComponent((0 until n).toSet(),-1)
        val sums = graph.rerootingTree(monoid(0L){a,b->a+b},{_,x->x+1},{x,_,_->x},root)
        check(sums.all { it == n.toLong() })
    }
    val chain = RootedTreeToolkit(IntArray(100000){it-1})
    check(chain.diameter().length == 99999)
    check(chain.centroidParents().count { it == -1 } == 1)
    check(chain.connectedKnapsack(LongArray(100000){1},3) == listOf(0L,1L,2L,3L))
    val invalid = SparseGraph(3).apply { addEdge(0,1); addEdge(0,1) }
    check(runCatching { invalid.rootedTree() }.isFailure)
    check(runCatching { RootedTreeToolkit(intArrayOf(-1,2,1)) }.isFailure)
    val extreme = RootedTreeToolkit(intArrayOf(-1,0))
    check(extreme.connectedKnapsack(longArrayOf(Long.MIN_VALUE,0),2)[2] == Long.MIN_VALUE)
    check(runCatching { extreme.connectedKnapsack(longArrayOf(Long.MAX_VALUE,1),2) }.isFailure)
    println("PASS: tree toolkit, graph bridges and rolling hash oracles")
}
