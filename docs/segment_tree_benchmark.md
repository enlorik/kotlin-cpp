# Kotlin versus C++: segment tree baseline

This benchmark compares the generic and unboxed Kotlin segment trees against a
functionally equivalent iterative C++ segment tree.

Both programs:
- Read the **same deterministic generated workload** into memory before timing.
- Start from the same 64-bit integer array on each of seven passes.
- Execute the same point assignments and half-open range-sum queries.
- Measure building the tree plus all queries/updates; exclude file input.
- Discard two warmup passes and report the median of five timed passes.
- Print checksums of all query answers; the script **fails unless all three match**.

Run from the repository root with Kotlin, Java, Python 3 and g++ installed:

~~~bash
bash benchmarks/compare_segment_tree.sh
BENCH_SIZE=8192 BENCH_QUERIES=100000 bash benchmarks/compare_segment_tree.sh
~~~

The script uses kotlinc for the checked-in tree/SegmentTree.kt and tree/LongSumSegmentTree.kt, and builds
benchmarks/SegmentTreeCpp.cpp with g++ -O2. Inputs come from
benchmarks/generate_segment_tree_workload.py with seed 20261108.

**Interpretation:** These are baseline observations, not a universal language
ranking. Generic Kotlin uses boxed Long values; the primitive Kotlin implementation uses a LongArray; C++ uses
unboxed int64_t and optimized native compilation. JIT warmup, GC, CPU load,
compiler versions and running on a shared CI runner all influence the result.
No elapsed-time threshold is an automated pass/fail condition. Correctness
(checksum equality) IS required in CI.

Next steps for performance work: compare specialized IntArray and LongArray APIs,
reduce boxing/allocations, and measure memory footprint before replacing any
clean general-purpose API.
