#include <algorithm>
#include <chrono>
#include <cstdint>
#include <iostream>
#include <vector>

struct Query { int type, left, right; };

int main() {
    std::ios::sync_with_stdio(false);
    std::cin.tie(nullptr);
    int n, q;
    if (!(std::cin >> n >> q)) return 1;
    std::vector<std::int64_t> values(n);
    for (auto& value : values) std::cin >> value;
    std::vector<Query> queries(q);
    for (auto& query : queries) std::cin >> query.type >> query.left >> query.right;

    std::vector<double> durations;
    std::int64_t checksum = 0;
    for (int run = 0; run < 7; ++run) {
        const auto start = std::chrono::steady_clock::now();
        int base = 1;
        while (base < n) base <<= 1;
        std::vector<std::int64_t> tree(base * 2, 0);
        for (int i = 0; i < n; ++i) tree[base + i] = values[i];
        for (int i = base - 1; i >= 1; --i) tree[i] = tree[i * 2] + tree[i * 2 + 1];

        std::int64_t total = 0;
        for (const Query& query : queries) {
            if (query.type == 0) {
                int node = base + query.left;
                tree[node] = query.right;
                for (node /= 2; node > 0; node /= 2)
                    tree[node] = tree[node * 2] + tree[node * 2 + 1];
            } else {
                int left = base + query.left, right = base + query.right;
                std::int64_t leftSum = 0, rightSum = 0;
                while (left < right) {
                    if (left & 1) leftSum += tree[left++];
                    if (right & 1) rightSum = tree[--right] + rightSum;
                    left /= 2;
                    right /= 2;
                }
                total += leftSum + rightSum;
            }
        }

        const auto end = std::chrono::steady_clock::now();
        if (run >= 2) {
            const auto ns = std::chrono::duration_cast<std::chrono::nanoseconds>(
                end - start
            ).count();
            durations.push_back(static_cast<double>(ns) / 1e6);
        }
        checksum = total;
    }

    std::sort(durations.begin(), durations.end());
    std::cout << "checksum=" << checksum << "\n";
    std::cout << "median_ms=" << durations[durations.size() / 2] << "\n";
}
