import kotlin.random.Random
import java.util.ArrayDeque

private fun verifyGraph(ok:Boolean, msg:String) { if(!ok) error(msg) }

private fun selectedVertices(
    g:SparseGraph, center:Int, sel:GraphSelection, eligible:(Int)->Boolean
):List<Int> {
    val dist=IntArray(g.n){-1}
    val q=ArrayDeque<Int>();q.add(center);dist[center]=0
    while(q.isNotEmpty()){
        val u=q.removeFirst()
        for(e in g.g[u])if(dist[e.to]==-1){
            dist[e.to]=dist[u]+1;q.add(e.to)
        }
    }
    return when(sel){
        GraphNeighbors -> (0 until g.n).filter { it!=center && dist[it]==1 && eligible(it) }
        is GraphRadius -> (0 until g.n).filter {
            dist[it] in sel.minDistance..sel.radius && eligible(it)
        }
        is GraphNearest -> {
            val candidates=(0 until g.n).filter {
                dist[it] in 0..sel.maxDistance && eligible(it) && sel.sources(it)
            }
            val d=candidates.minOfOrNull{dist[it]}
            candidates.filter{dist[it]==d}
        }
    }
}

fun main(){
    val g=SparseGraph(4)
    g.addEdge(0,1);g.addEdge(0,2);g.addEdge(1,3);g.addEdge(2,3)
    val data=intArrayOf(0,2,4,8)
    val nearest=GraphNearest({data[it]!=0},5)
    verifyGraph(g.aggregate(nearest,value={data[it]},op=IntAggregations.OR)[0]==6,"tied OR")
    verifyGraph(g.aggregate(nearest,value={data[it]},op=IntAggregations.SUM)[0]==6,"tied SUM")
    verifyGraph(g.aggregate(center=0,selection=GraphRadius(2),value={data[it]},op=IntAggregations.SUM)==14,"radius")
    verifyGraph(g.aggregate(center=0,selection=GraphNeighbors,value={data[it]},op=IntAggregations.SUM)==6,"neighbors")
    val directed=SparseGraph(3,true)
    directed.addEdge(0,1);directed.addEdge(1,2)
    val source=GraphNearest({it==2})
    val d=directed.aggregate(source,value={if(it==2)8 else 0},op=IntAggregations.OR)
    verifyGraph(d.values==listOf(8,8,8) && d.distance(0)==2,"directed center->source")
    val p=SparseGraph(2)
    repeat(3){p.addEdge(0,1)}
    verifyGraph(p.aggregate(center=0,selection=GraphNeighbors,value={if(it==1)7 else 0},
        op=IntAggregations.SUM)==7,"parallel edges don't double count")
    val rng=Random(4163)
    var cases=0
    for(dir in listOf(false,true))for(n in 1..12)repeat(20){seed->
        val graph=SparseGraph(n,dir)
        repeat(n*n/2){
            val u=rng.nextInt(n);val v=rng.nextInt(n)
            if(rng.nextBoolean())graph.addEdge(u,v)
        }
        val nums=IntArray(n){rng.nextInt(0,16)}
        val options=listOf<GraphSelection>(GraphNeighbors,GraphRadius(0),
            GraphRadius(2),GraphRadius(3,2),
            GraphNearest({nums[it]!=0},3),GraphNearest({nums[it]%3==0},10))
        for(sel in options){
            val sum=graph.aggregate(sel,value={nums[it]},op=IntAggregations.SUM)
            val or=graph.aggregate(sel,value={nums[it]},op=IntAggregations.OR)
            for(c in 0 until n){
                val selected=selectedVertices(graph,c,sel){true}
                verifyGraph(sum[c]==selected.sumOf{nums[it]},"sum oracle n=$n seed=$seed center=$c")
                verifyGraph(or[c]==selected.fold(0){a,v->a or nums[v]},"OR oracle")
                verifyGraph(graph.aggregate(center=c,selection=sel,value={nums[it]},
                    op=IntAggregations.SUM)==sum[c],"single center")
                cases++
            }
        }
    }
    val chain=SparseGraph(100_000)
    for(i in 0 until 99_999)chain.addEdge(i,i+1)
    val fast=chain.aggregate(GraphNearest({it==0},100_000),
        value={1},op=IntAggregations.OR)
    verifyGraph(fast.values[99_999]==1 && fast.distance(99_999)==99_999,"100k chain")
    println("PASS: $cases randomized reference comparisons, directed and parallel edges, 100k graph")
}
