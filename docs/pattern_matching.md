# KMP, Z-function and Aho–Corasick matching

The string toolbox complements the online suffix automaton and eertree.

```kotlin
val starts = kmpOccurrences("banana", "ana") // [1,3]
val pi = prefixFunction("ababaca")
val z = zFunction("aaaa") // [4,3,2,1]
```

- `prefixFunction(text)` returns KMP prefix borders in O(n).
- `zFunction(text)` returns longest prefix matches at each suffix,
  with Z[0] defined as n; O(n).
- `kmpOccurrences(text, pattern)` finds all overlapping starts in
  O(n+m+matches); empty pattern matches all n+1 text boundaries.

For many patterns at once:

```kotlin
val dictionary = listOf("he", "she", "his", "hers").ahoCorasick()
val hits = dictionary.findAll("ushers")
val counts = dictionary.countPerPattern("ushers")
```

`AhoCorasick` builds a trie and failure/dictionary suffix links, with
expected O(total pattern length) construction and O(text length+matches)
search. It does **not** duplicate terminal output sets into suffix nodes;
that avoids quadratic storage for patterns "a", "aa", "aaa", ... .
Duplicate patterns remain distinct by input index. Empty patterns are
rejected. Every `PatternMatch` includes the original zero-based pattern
index and half-open text interval `[from,until)`.

All algorithms use Kotlin UTF-16 `Char` code units rather than Unicode
graphemes. Match reporting may require output-sized memory; use
`countPerPattern` if you only need counts.

```bash
python3 tools/build_submission.py --modules kmp-z,aho-corasick \
  --solution Main.kt --output submission.kt
```

Randomized tests compare every KMP/Z result and Aho occurrence against
naive brute-force matching, including overlapping and duplicate matches.
