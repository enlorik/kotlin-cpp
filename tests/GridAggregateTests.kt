// Run: kotlinc grid/GridBfs.kt grid/GridAggregate.kt tests/GridAggregateTests.kt -include-runtime -d aggregate-tests.jar
//      java -jar aggregate-tests.jar
private fun checkA(cond:Boolean, message:String) { if (!cond) throw AssertionError(message) }
private inline fun throwsA(block:()->Unit) {
    var did = false
    try { block() } catch (_: IllegalArgumentException) { did = true }
    checkA(did, "Expected IllegalArgumentException")
}
private fun bruteOriginal(a:Array<IntArray>,r:Int,c:Int):Int {
    val h=a.size; val w=a[0].size
    if (a[r][c]!=0) return -1
    var near=6; var ans=0
    for(dr in -5..5) for(dc in -5..5) {
        val x=r+dr;val y=c+dc;val d=kotlin.math.abs(dr)+kotlin.math.abs(dc)
        if(d !in 1..5 || x !in 0 until h || y !in 0 until w || a[x][y]==0)continue
        if(d<near){near=d;ans=a[x][y]}else if(d==near){ans=ans or a[x][y]}
    }
    return ans
}
fun main() {
    val a = arrayOf(intArrayOf(1,2,3), intArrayOf(4,5,6), intArrayOf(7,8,9))
    val plus:(Int,Int)->Int={x,y->x+y}
    val radius = a.aggregate(selection=Radius(1),combine=plus)
    checkA(radius.values[1][1]==25, "radius incl center")
    checkA(radius[0,0]==7 && radius[1 to 1]==25, "result access")
    checkA(radius.values[0][2]==3+2+6, "bounds")
    checkA(a.aggregate(center=1 to 1, selection=Radius(1),combine=plus)==25,"single center radius")
    checkA(a.aggregate(center=1 to 1, selection=Radius(2),combine=plus)==45,"radius 2")
    checkA(a.aggregate(center=1 to 1, selection=Radius(1,minDistance=1),combine=plus)==20,"ring")
    checkA(a.aggregate(center=1 to 1, selection=Radius(1,metric=GridMetric.CHEBYSHEV),combine=plus)==45,"square")
    checkA(a.aggregate(center=1 to 1, selection=Radius(100),combine=plus)==45,"large radius clamp")
    val directions = a.aggregate(selection=Offsets(FOUR),combine=plus)
    checkA(directions[1,1]==20 && directions[0,0]==6, "four neighbors excluding center")
    checkA(a.aggregate(center=1 to 1,selection=Offsets(FOUR),combine=plus)==20,"center four neighbors")
    checkA(a.aggregate(center=1 to 1,selection=Offsets(FOUR)) { x, y -> x + y }==20,"trailing lambda convenience")
    checkA(a.aggregate(center=1 to 1,selection=Offsets(0 to 1,0 to 1,1 to 0),combine=plus)==14,"duplicate offsets only once")
    checkA(a.aggregate(center=1 to 1,selection=Offsets(0 to 0),combine=plus)==5,"explicit center offset")
    checkA(a.aggregate(center=1 to 1,selection=Offsets(99 to 99,-999 to 0),combine=plus)==0,"out-of-bounds offsets")
    checkA(a.aggregate(center=1 to 1,selection=Offsets(Int.MAX_VALUE to 0, Int.MIN_VALUE to 0),combine=plus)==0,"overflow offsets")
    checkA(a.aggregate(center=1 to 1,selection=Radius(1),include={r,c->a[r][c]%2==0},combine=plus)==20,"filter even")
    checkA(a.aggregate(center=1 to 1,selection=Offsets(FOUR),initial=0L,
        value={r,c->a[r][c].toLong()},combine={x,y->x+y})==20L,"generic long center")
    checkA(a.aggregate(selection=Offsets(FOUR),initial=0L,
        value={r,c->a[r][c].toLong()},combine={x,y->x+y}).values[1][1]==20L,"generic long all")
    val grid = arrayOf(intArrayOf(0,0,0),intArrayOf(2,0,4),intArrayOf(0,0,0))
    val nearest=Nearest(sources={r,c->grid[r][c]!=0},maxDistance=5)
    val orOperation:(Int,Int)->Int={x,y->x or y}
    checkA(grid.aggregate(center=1 to 1,selection=nearest,combine=orOperation)==6,"tied nearest OR")
    checkA(grid.aggregate(center=1 to 1,selection=nearest,combine=plus)==6,"tied nearest SUM")
    checkA(grid.aggregate(center=1 to 1,selection=nearest,initial=Int.MAX_VALUE,combine={x,y->minOf(x,y)})==2,"tied nearest MIN")
    val allNearest = grid.aggregate(selection=nearest,combine=orOperation)
    checkA(allNearest.values[1][1]==6 && allNearest.distance(1,1)==1, "all nearest and min distance")
    checkA(allNearest.distance(1,0)==0 && allNearest.distance(1,2)==0, "source distance zero")
    checkA(grid.aggregate(selection=Offsets(FOUR),combine=plus).distance(1,1)==null, "only nearest has distance")
    checkA(grid.aggregate(selection=Nearest(sources={r,c->grid[r][c]!=0},maxDistance=0),combine=orOperation).values[1][1]==0,"nearest max radius 0")
    checkA(grid.aggregate(center=1 to 1,selection=Nearest(sources={r,c->grid[r][c]!=0},maxDistance=0),combine=orOperation)==0,"nearest at 0 no source")
    checkA(grid.aggregate(center=1 to 0,selection=nearest,combine=orOperation)==2,"source at zero distance")
    checkA(grid.aggregate(selection=nearest,initial=1,combine={x,y->x*y}).values[1][1]==8,"tied nearest product")
    checkA(grid.aggregate(selection=nearest,initial=0,include={_,c->c==0},combine=plus).values[1][1]==2,"include filters eligible sources")
    val noSources=Array(2){IntArray(3)}
    checkA(noSources.aggregate(selection=Nearest(sources={r,c->noSources[r][c]!=0},maxDistance=5),combine=plus).values[1][1]==0,"no sources")
    // A far source with MANY shortest paths must count only once for non-idempotent sum.
    val far=Array(5){IntArray(5)}
    far[0][0]=7;far[0][4]=11
    val distinct=Nearest(sources={r,c->far[r][c]!=0},maxDistance=6)
    checkA(far.aggregate(center=2 to 2,selection=distinct,combine=plus)==18,"no duplicate source across shortest paths")
    checkA(far.aggregate(selection=distinct,combine=plus).values[2][2]==18,"full grid no duplicate source")
    checkA(far.aggregate(center=2 to 2,selection=Nearest(sources={r,c->far[r][c]!=0},maxDistance=3),combine=plus)==0,"nearest radius cutoff")
    val chars=listOf("a#b",".X.")
    checkA(chars.aggregate(selection=Offsets(FOUR),initial="",value={r,c->chars[r][c].toString()},combine={x,y->x+y}).values[0][0]==".#","list string")
    checkA(chars.aggregate(center=0 to 1,selection=Radius(0),initial="",value={r,c->chars[r][c].toString()},combine={x,y->x+y})=="#","list center")
    val arrayStrings=arrayOf("12","34")
    checkA(arrayStrings.aggregate(selection=Radius(0),initial=0,value={r,c->arrayStrings[r][c].digitToInt()},combine=plus).values[1][1]==4,"array string")
    val arrayChars=arrayOf(charArrayOf('5','6'))
    checkA(arrayChars.aggregate(center=0 to 1,selection=Radius(0),initial=0,value={r,c->arrayChars[r][c].digitToInt()},combine=plus)==6,"array char")
    checkA(gridAggregate(1,2,Radius(1),initial=0,value={r,c->r+c+1},combine=plus).values[0][0]==3,"shape-only all")
    checkA(gridAggregateAt(1,2,0 to 0,Radius(1),initial=0,value={r,c->r+c+1},combine=plus)==3,"shape-only center")
    throwsA { Radius(-1) }
    throwsA { Radius(1,minDistance=2) }
    throwsA { Nearest(sources={_,_->false}, maxDistance=-1) }
    throwsA { emptyArray<IntArray>().aggregate(selection=Radius(0),combine=plus) }
    throwsA { arrayOf(intArrayOf(1),intArrayOf(2,3)).aggregate(selection=Radius(0),combine=plus) }
    throwsA { a.aggregate(center=3 to 0,selection=Radius(0),combine=plus) }
    throwsA { gridAggregate(0,2,Radius(0),initial=0,value={_,_->1},combine=plus) }
    // Compare with exact original C++ behavior on 1216 deterministic grids.
    for(h in 1..8)for(w in 1..8)for(seed in 0..18){
        val b=Array(h){r->IntArray(w){c->
            val t=(r*73+c*127+seed*31+r*c*17)%17
            if(t%3==0) 0 else ((t*13+seed*7)%32)-8
        }}
        val res=b.aggregate(selection=Nearest(sources={r,c->b[r][c]!=0},maxDistance=5),combine=orOperation)
        for(r in 0 until h)for(c in 0 until w){
            val expected=bruteOriginal(b,r,c)
            val actual=if(b[r][c]!=0)-1 else res.values[r][c]
            checkA(actual==expected,"C++ reference mismatch at $r,$c for $h x $w seed=$seed: $actual != $expected")
        }
    }
    // Compare Radius, Offsets and Nearest (sum) against a brute-force oracle.
    for (h in 1..5) for(w in 1..5) for(seed in 0..9){
        val b=Array(h){r->IntArray(w){c->(r*31+c*11+seed*17)%7}}
        val offsets=Offsets(0 to 1,0 to -1,1 to 0,-1 to 0,0 to 1)
        val rr=b.aggregate(selection=Radius(2,minDistance=1),combine=plus)
        val oo=b.aggregate(selection=offsets,combine=plus)
        val nearestSelection=Nearest(sources={r,c->b[r][c]%2==0},maxDistance=3)
        val nn=b.aggregate(selection=nearestSelection,combine=plus)
        for(r in 0 until h)for(c in 0 until w){
            var radiusExpected=0; var offsetsExpected=0; var minDist=4;var nearestExpected=0
            for(x in 0 until h)for(y in 0 until w){
                val d=kotlin.math.abs(x-r)+kotlin.math.abs(y-c)
                if(d in 1..2)radiusExpected+=b[x][y]
                if(d==1)offsetsExpected+=b[x][y]
                if(b[x][y]%2==0 && d<=3){
                    if(d<minDist){minDist=d;nearestExpected=b[x][y]}
                    else if(d==minDist)nearestExpected+=b[x][y]
                }
            }
            checkA(rr.values[r][c]==radiusExpected,"radius oracle")
            checkA(oo.values[r][c]==offsetsExpected,"offsets oracle")
            checkA(nn.values[r][c]==nearestExpected,"nearest sum oracle")
            checkA(b.aggregate(center=r to c,selection=nearestSelection,combine=plus)==nearestExpected,"single center nearest oracle")
        }
    }
    println("PASS: radius, offsets, nearest OR/sum/min, single center, original C++ checks, randomized references")
}
