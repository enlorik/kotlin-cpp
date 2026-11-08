#!/usr/bin/env python3
"""Deterministic shared workload for Kotlin/C++ point-set, range-sum benchmarks."""
import argparse
from random import Random

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--size", type=int, default=4096)
    parser.add_argument("--queries", type=int, default=40000)
    parser.add_argument("--seed", type=int, default=20261108)
    args = parser.parse_args()
    if args.size < 1 or args.queries < 1:
        parser.error("size and queries must be positive")
    rng = Random(args.seed)
    print(args.size, args.queries)
    print(" ".join(str(rng.randint(-1000, 1000)) for _ in range(args.size)))
    for i in range(args.queries):
        if i % 3 == 0:
            print(0, rng.randrange(args.size), rng.randint(-1000, 1000))
        else:
            left = rng.randrange(args.size)
            right = rng.randrange(left, args.size + 1)
            print(1, left, right)

if __name__ == "__main__":
    main()
