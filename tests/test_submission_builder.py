"""Pure-Python checks for the deterministic single-file Kotlin bundler."""

import sys
import tempfile
from pathlib import Path
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "tools"))
import build_submission as bundler


class SubmissionBuilderTests(unittest.TestCase):
    def test_modules_are_deduplicated_in_first_use_order(self):
        files = bundler.selected_files(
            ["binary-search", "advanced-binary-search", "binary-search"]
        )
        self.assertEqual(
            files, ["search/BinarySearch.kt", "search/AdvancedBinarySearch.kt"]
        )

    def test_unknown_module_is_explicit(self):
        with self.assertRaisesRegex(ValueError, "Unknown module"):
            bundler.selected_files(["magic"])

    def test_empty_selection_is_rejected(self):
        with self.assertRaisesRegex(ValueError, "at least one"):
            bundler.selected_files([])

    def test_imports_hoisted_and_deduplicated(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / "search").mkdir()
            (root / "search/BinarySearch.kt").write_text(
                "import java.util.ArrayDeque\nfun chosen() = 1\n", encoding="utf-8"
            )
            solution = root / "Main.kt"
            solution.write_text(
                "import java.util.ArrayDeque\n"
                "import kotlin.math.abs\n"
                "fun main() { println(abs(chosen())) }\n",
                encoding="utf-8",
            )
            text, used = bundler.render(root, ["binary-search"], solution)
            self.assertEqual(text.count("import java.util.ArrayDeque"), 1)
            self.assertEqual(text.count("import kotlin.math.abs"), 1)
            self.assertLess(
                text.index("import kotlin.math.abs"), text.index("fun chosen()")
            )
            self.assertEqual(len(used), 2)
            self.assertEqual(
                text, bundler.render(root, ["binary-search"], solution)[0]
            )

    def test_disallow_package_and_file_annotations(self):
        with self.assertRaisesRegex(ValueError, "packaged"):
            bundler.split_source("package example\nfun f() = 1", "sample")
        with self.assertRaisesRegex(ValueError, "annotations"):
            bundler.split_source('@file:JvmName("Foo")\nfun f() = 1', "sample")

    def test_missing_source_errors_without_generating_incomplete_code(self):
        with tempfile.TemporaryDirectory() as temp:
            with self.assertRaisesRegex(ValueError, "Missing source"):
                bundler.render(Path(temp), ["grid-bfs"])


if __name__ == "__main__":
    unittest.main()
