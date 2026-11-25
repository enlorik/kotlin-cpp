fun main() {
    val tree = VirtualTreeIndex(intArrayOf(-1,0,0,1,1))
    check(tree.build(intArrayOf(3,4)).vertices.toSet() == setOf(1,3,4))
    check(smawkRowMinima(3,4){i,j->(i-j).toLong()*(i-j)}.contentEquals(intArrayOf(0,1,2)))
    check(fpsInverse(intArrayOf(1,-1),5).contentEquals(IntArray(5){1}))
    check(fpsLog(fpsExp(intArrayOf(0,1),5),5).contentEquals(intArrayOf(0,1,0,0,0)))
}
