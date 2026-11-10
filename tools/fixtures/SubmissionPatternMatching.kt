fun main() {
    check(prefixFunction("ababaca").contentEquals(intArrayOf(0,0,1,2,3,0,1)))
    check(zFunction("aaaa").contentEquals(intArrayOf(4,3,2,1)))
    check(kmpOccurrences("banana","ana").contentEquals(intArrayOf(1,3)))
    val aho = listOf("ana","na").ahoCorasick()
    check(aho.countPerPattern("banana").contentEquals(longArrayOf(2,2)))
    println("standalone KMP/Z/Aho OK")
}
