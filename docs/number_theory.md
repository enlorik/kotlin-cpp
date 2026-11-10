# Modular arithmetic, 64-bit factorization and NTT

## Exact modular arithmetic

`modNormalize64`, `modAdd64`, `modSubtract64`,
`modMultiply64`, and `modPower64` accept any signed Long
operands with a **positive Long modulus**, and never overflow in
intermediate arithmetic. Multiplication and modular powers use
`java.math.BigInteger` internally for correctness.

`extendedGcdExact(a,b)` returns BigInteger Bézout coefficients,
safe even for `Long.MIN_VALUE`.
`modInverse64(a,m)` returns null when the inverse does not exist.

`chineseRemainder64(equations)` implements generalized CRT, where
each equation is `residue to modulus`. Inconsistent equations
return null; a valid answer contains the smallest nonnegative
residue and a BigInteger combined modulus.

## Deterministic 64-bit primality and factoring

```kotlin
check(isPrime64(1_000_000_007L))
check(factor64(360L) == mapOf(2L to 3, 3L to 2, 5L to 1))
check(eulerTotient64(36L) == 12L)
```

`isPrime64` is deterministic on all signed Long inputs by a
known sufficient 7-base Miller–Rabin witness set.
`factor64` accepts positive signed Long integers and uses
Pollard–Rho with Brent batching; returned keys are prime and values
are exponents. Its runtime is probabilistic (the default seed
is fixed for reproducibility) even though the primality test
it relies on is deterministic.

Factorization and `modMultiply64` use BigInteger internally,
so speed-sensitive contests may benefit from specialized
unsigned modular-multiplication techniques if allowed by constraints.

## Number Theoretic Transform (NTT)

```kotlin
val product = convolution998244353(
    intArrayOf(1,2,3), intArrayOf(4,5))
check(product.contentEquals(intArrayOf(4,13,22,15)))
```

This performs exact polynomial multiplication **modulo 998244353**,
a prime with primitive 2^23-th roots of unity.
The maximum required convolution length is 2^23.
The implementation is radix-2, normalizes arbitrary signed Int
coefficients, and costs O((n+m)log(n+m)) time and O(n+m) memory.
`numberTheoreticTransform998244353(a, inverse)` mutates a
power-of-two array in place; inverse divides by its length modulo
the prime. NTT is not the same as real/complex FFT and does not
compute arbitrary-precision integer products directly.

## Submission

```bash
python3 tools/build_submission.py --modules number-theory,ntt \
    --solution Main.kt --output submission.kt
```

Random tests compare modular operations with BigInteger ground truth,
CRT with exhaustive small congruence searches, factorization with
product and primality checks, and NTT with quadratic convolution.
