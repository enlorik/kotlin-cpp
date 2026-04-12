class SqrtDecomp(private val n: Int) {
    private val block = Math.sqrt(n.toDouble()).toInt().coerceAtLeast(1)
    private val data = LongArray(n)
    private val blocks = LongArray((n + block - 1) / block)

    fun update(i: Int, v: Long) {
        data[i] += v
        blocks[i / block] += v
    }

    fun query(l: Int, r: Int): Long {
        var sum = 0L
        val bl = l / block
        val br = r / block
        if (bl == br) {
            for (i in l..r) sum += data[i]
        } else {
            for (i in l until (bl + 1) * block) sum += data[i]
            for (b in bl + 1 until br) sum += blocks[b]
            for (i in br * block..r) sum += data[i]
        }
        return sum
    }
}
