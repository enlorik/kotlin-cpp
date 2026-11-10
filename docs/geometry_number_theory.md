# Exact geometry, number theory and polynomial convolution

These modules prioritize **correct mathematical contracts** over pretending
every Kotlin Long operation is automatically safe or arbitrarily fast.

## Exact integer geometry

```kotlin
val triangle = listOf(LongPoint(0, 0), LongPoint(3, 0), LongPoint(0, 4))
check(orientation(triangle[0], triangle[1], triangle[2]) == 1)
check(diameterSquared(triangle).toLong() == 25L)
check(closestPairSquared(triangle)!!.toLong() == 9L)
val hull = convexHull(triangle)
check(pointInPolygon(LongPoint(1,1), hull))
```

- Coordinates are signed Long.
- `cross`, `orientation`, and squared distances use BigInteger internally:
  even extreme Long coordinates do not overflow.
- `segmentsIntersect` includes collinear overlap and endpoints.
- `convexHull` returns distinct strict CCW hull vertices with collinear
  interior boundary vertices removed, O(n log n).
- `diameterSquared` uses convex hull and rotating calipers, O(n log n).
- `closestPairSquared` uses y-sorted divide-and-conquer, O(n log n),
  returning null for fewer than two points.
- `polygonAreaTwice` is signed and exact.
- `pointInPolygon` includes boundary and uses the even-odd rule for
  a simple polygon.

BigInteger exactness has higher constants than raw Long arithmetic.
Use an overflow-proven narrower coordinate type for strict 64 MB judges.

## Number theory and factorization

```kotlin
check(isPrime64(97))
check(factorize64(360L) == mapOf(2L to 3, 3L to 2, 5L to 1))
check(powerMod(2, 10, 1_000) == 24L)
check(inverseMod(3, 11) == 4L)
check(eulerTotient(36) == 12L)
```

- `isPrime64`: deterministic Miller–Rabin for signed 64-bit input,
  including pseudoprime-resistant bases for all n below 2^64.
- `factorize64`: Pollard–Brent rho + Miller–Rabin, for positive signed
  Long values (n=1 returns empty factorization); complexity depends
  probabilistically on factor structure and can be costly for large
  semiprimes.
- `normalizeMod`, `multiplyMod`, `powerMod`, `inverseMod` explicitly
  enforce modular domains, using BigInteger where needed.
- `eulerTotient` uses the distinct prime factors.
- Factorization is intentionally exact rather than guaranteed to meet
  narrow contest timing limits on near-63-bit semiprimes.

## Polynomial multiplication

For **exact modular** coefficients:

```kotlin
val product = convolutionNtt(intArrayOf(1,2),intArrayOf(3,4))
check(product.contentEquals(intArrayOf(3,10,8)))
```

NTT uses modulus **998244353**, primitive root **3**, supports up to
2^23 transform elements, and handles negative input coefficients by
normalization. Time O(n log n), memory O(n). Output is modulo the prime,
not exact unbounded integer coefficients.

For **approximate floating-point** convolution:

```kotlin
val c = convolutionFft(doubleArrayOf(1.0,2.0),
                       doubleArrayOf(3.0,4.0))
```

FFT uses Double coefficients and does not promise exact integer results:
floating-point rounding, cancellation and very large values can lose
precision. Use NTT/CRT for exact modular/integer problems.

## Contest bundling

```bash
python3 tools/build_submission.py --modules exact-geometry \
    --solution Main.kt --output submission.kt
python3 tools/build_submission.py --modules number-theory,polynomial-ntt \
    --solution Main.kt --output submission.kt
python3 tools/build_submission.py --modules polynomial-fft \
    --solution Main.kt --output submission.kt
```

Randomized correctness tests compare hull/diameter/closest-pair to
quadratic enumeration, small factorizations to trial division, and
FFT/NTT to naive O(n²) polynomial multiplication.
