# Suffix Automaton and Palindromic Tree

Both structures append characters online and build O(n) states over
Kotlin `Char` values (UTF-16 code units). They are not normalized Unicode
grapheme algorithms; surrogate pairs are treated as two code units.

## Suffix automaton

```kotlin
val sam = "banana".suffixAutomaton()
check(sam.countOccurrences("ana") == 2L)
check(sam.contains("nan"))
println(sam.distinctSubstringCount())
println(sam.longestCommonSubstring("anagram"))
sam.append("band")
```

- O(n) states and O(n) expected hash-map transitions on append.
- Membership and occurrence queries O(pattern length), plus O(n)
  cached occurrence propagation on first use after an append.
- Distinct substring count O(number of states).
- Longest common substring against a second text O(second text length).
- Empty pattern occurs n+1 times by convention.

## Palindromic tree / eertree

```kotlin
val pal = "ababa".palindromicTree()
check(pal.distinctPalindromeCount == 5)
check(pal.totalPalindromeCount == 9L)
check(pal.longestPalindrome() == "ababa")
println(pal.palindromes())  // Distinct palindrome strings and occurrences
pal.append('b')
```

- Append amortized O(1) expected hash-map operations and O(n) states.
- Distinct and total palindrome counts O(1) after each append.
- A cached suffix-link count propagation gives occurrence counts.
- Enumerating all palindrome **strings** may output Θ(n²) characters;
  avoid `palindromes()` on enormous repetitive strings when memory tight.

## Contest packaging

```bash
python3 tools/build_submission.py --modules suffix-automaton,palindromic-tree \
    --solution Main.kt --output submission.kt
```

Randomized tests validate incremental state against brute-force substring
and palindrome sets, including overlapping occurrences.
