data class Job(val duration: Int, val weight: Int)

fun scheduleWCT(jobs: List<Job>): List<Int> {
    // minimize sum of weighted completion times: sort by duration/weight ratio
    return jobs.indices.sortedWith(Comparator { i, j ->
        jobs[i].duration.toLong() * jobs[j].weight - jobs[j].duration.toLong() * jobs[i].weight
    }.thenBy { it })
}

data class Interval(val start: Int, val end: Int, val id: Int = 0)

fun intervalSchedule(intervals: List<Interval>): List<Int> {
    val sorted = intervals.sortedBy { it.end }
    val result = mutableListOf<Int>()
    var lastEnd = Int.MIN_VALUE
    for (iv in sorted) {
        if (iv.start >= lastEnd) {
            result.add(iv.id)
            lastEnd = iv.end
        }
    }
    return result
}
