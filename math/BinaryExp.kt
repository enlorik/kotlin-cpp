fun binpow(base: Long, exp: Long, mod: Long): Long {
    var result = 1L
    var b = base % mod
    var e = exp
    while (e > 0L) {
        if (e and 1L == 1L) result = result * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return result
}

fun binpow(base: Long, exp: Long): Long {
    var result = 1L
    var b = base
    var e = exp
    while (e > 0L) {
        if (e and 1L == 1L) result *= b
        b *= b
        e = e shr 1
    }
    return result
}
