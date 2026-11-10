fun main() {
    val edges = listOf(0 to 1, 1 to 2)
    check(treeDistanceSums(3, edges).contentEquals(longArrayOf(3,2,3)))
    val results = offlineConnectivity(3) {
        add(0,1); connected(0,2)
        add(1,2); connected(0,2)
        remove(0,1); componentCount()
    }
    check(results == listOf(
        OfflineConnectivityAnswer.Connected(false),
        OfflineConnectivityAnswer.Connected(true),
        OfflineConnectivityAnswer.ComponentCount(2)
    ))
    println("standalone rerooting and offline connectivity OK")
}
