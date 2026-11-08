import kotlin.random.Random

private fun illegal(body:()->Unit) {
    var throws = false
    try { body() } catch (_: IllegalArgumentException) { throws = true }
    check(throws)
}
private fun immutable(body:()->Unit) {
    var throws = false
    try { body() } catch (_: IllegalStateException) { throws = true }
    check(throws)
}
fun main() {
    val rand = Random(2206)
    var checks = 0
    val min = idempotentMonoid(Int.MAX_VALUE) { a: Int,b: Int -> minOf(a,b) }
    val max = aggregateOp(Int.MIN_VALUE,idempotent=true) { a: Int,b: Int -> maxOf(a,b) }
    val sum = monoid(0) { a: Int,b: Int -> a+b }
    val concat = monoid("") { a: String,b: String -> a+b }

    repeat(450) {
        val n = rand.nextInt(0,90)
        val values = IntArray(n) { rand.nextInt(-100,101) }
        val original = values.copyOf()
        val dynamicExpected = values.copyOf()
        val minStatic = values.rangeIndex(min)
        val maxStatic = values.rangeIndex(max)
        val sumStatic = values.rangeIndex(sum)
        val dynamic = values.rangeIndex(sum, mutable=true)
        check(minStatic.strategy == RangeIndexStrategy.STATIC_IDEMPOTENT)
        check(maxStatic.strategy == RangeIndexStrategy.STATIC_IDEMPOTENT)
        check(sumStatic.strategy == RangeIndexStrategy.STATIC_GENERAL)
        check(dynamic.strategy == RangeIndexStrategy.POINT_UPDATES)
        immutable { minStatic.set(0,2) }
        repeat(70) {
            val l = rand.nextInt(n+1)
            val r = rand.nextInt(l,n+1)
            check(minStatic.query(l,r) == (original.sliceArray(l until r).minOrNull() ?: Int.MAX_VALUE))
            check(maxStatic.query(l,r) == (original.sliceArray(l until r).maxOrNull() ?: Int.MIN_VALUE))
            check(sumStatic.query(l,r) == original.sliceArray(l until r).sum())
            check(dynamic.query(l,r) == dynamicExpected.sliceArray(l until r).sum())
            checks+=4
            if(n>0&&rand.nextBoolean()){
                val i=rand.nextInt(n)
                val v=rand.nextInt(-100,101)
                dynamic.set(i,v)
                dynamicExpected[i]=v
            }
        }
        check(dynamic.all() == dynamicExpected.sum())
        illegal { dynamic.query(0,n+1) }
    }

    repeat(200) {
        val n = rand.nextInt(0,35)
        val words = List(n) { ('a' + rand.nextInt(0,6)).toString() }
        val immutable = words.rangeIndex(concat)
        val expected = words.toMutableList()
        val live = words.rangeIndex(concat,mutable=true)
        repeat(30) {
            val l=rand.nextInt(n+1)
            val r=rand.nextInt(l,n+1)
            check(immutable.query(l,r) == words.subList(l,r).joinToString(""))
            check(live.query(l,r) == expected.subList(l,r).joinToString(""))
            checks+=2
            if(n>0 && rand.nextBoolean()) {
                val i=rand.nextInt(n)
                expected[i] = "!"
                live.set(i,"!")
            }
        }
    }

    // User-defined lazy actions (typed and legacy).
    val add = action<Int,Int>(
        apply={ total,delta,length -> total+delta*length },
        compose={ old,newer -> old+newer }
    )
    val lazy = intArrayOf(2,4,6).rangeIndex(sum,add)
    lazy.update(0,2,3)
    check(lazy.query(0,3)==18)
    val legacySum = aggregateOp(0) { a:Int,b:Int -> a+b }
    val legacyAction=RangeAction<Int,Int>(
        apply={ value,delta,len -> value+delta*len },
        compose={ old,new -> old+new }
    )
    val lazyLegacy = listOf(2,4,6).rangeIndex(legacySum,legacyAction)
    lazyLegacy.update(0,3,1)
    check(lazyLegacy.query(0,3)==15)
    check(intArrayOf().rangeIndex(sum).all()==0)
    check(intArrayOf().rangeIndex(min).all()==Int.MAX_VALUE)
    println("PASS: $checks randomized static/dynamic facade queries, custom noncommutative ops and typed lazy actions")
}
