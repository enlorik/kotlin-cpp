/**
 * Returns the 0-indexed position of the survivor in the Josephus problem
 * with n people (0..n-1) and every k-th person eliminated.
 *
 * Uses the recurrence J(1,k)=0, J(n,k)=(J(n-1,k)+k) % n in O(n) time.
 * For k=2 an O(log n) solution exists but this handles the general case.
 */
fun josephus(n: Int, k: Int): Int {
    var pos = 0
    for (i in 2..n) pos = (pos + k) % i
    return pos
}
