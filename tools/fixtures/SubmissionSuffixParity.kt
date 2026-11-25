fun main() {
    val index = "banana".suffixArray()
    check(index.occurrences("ana").sorted() == listOf(1,3))
    check(index.commonPrefix(1,3) == 3)
    check(offlineBipartiteness(3) {
        add(0,1); add(1,2); query(); add(2,0); query(); remove(0,2); query()
    } == listOf(true,false,true))
}
