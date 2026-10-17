#!/usr/bin/env python3
"""Tests for the claim scanner (`scripts/claim-scan.py`). Stdlib only.

Run with `python3 scripts/test-claim-scan.py`. The scanner's kebab-case filename is not an importable identifier, so it
is loaded by path.
"""

import contextlib
import importlib.util
import io
import subprocess
import tempfile
import unittest
from pathlib import Path

SCANNER_PATH = Path(__file__).resolve().parent / "claim-scan.py"
_spec = importlib.util.spec_from_file_location("claim_scan", SCANNER_PATH)
claim_scan = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(claim_scan)

def make_repo(test: unittest.TestCase) -> Path:
    directory = tempfile.TemporaryDirectory()
    test.addCleanup(directory.cleanup)
    repo = Path(directory.name)
    subprocess.run(("git", "init", "-q"), cwd=repo, check=True)
    subprocess.run(("git", "config", "user.email", "test@example.com"), cwd=repo, check=True)
    subprocess.run(("git", "config", "user.name", "Test"), cwd=repo, check=True)
    return repo


FIXTURE_DIFF = """\
diff --git a/docs/sample.md b/docs/sample.md
index 1111111..2222222 100644
--- a/docs/sample.md
+++ b/docs/sample.md
@@ -1,2 +1,2 @@
 context line
+The scan reports 12 files, only the changed ones; see `scripts/claim-scan.py` and #606.
"""


class ShapesTests(unittest.TestCase):
    def test_a_numeral_with_a_referent_is_a_count(self):
        self.assertIn(("count", "12 files"), claim_scan.shapes("The scan reports 12 files."))

    def test_an_absolute_quantifier_is_reported(self):
        self.assertIn(("absolute", "only"), claim_scan.shapes("only the changed ones"))

    def test_a_reference_to_another_artifact_is_a_cross_reference(self):
        self.assertIn(("cross-ref", "#606"), claim_scan.shapes("fixed by #606"))
        self.assertIn(("cross-ref", "AGENTS.md"), claim_scan.shapes("see AGENTS.md for the rule"))

    def test_a_backticked_path_is_reported(self):
        self.assertIn(("path", "scripts/claim-scan.py"), claim_scan.shapes("run `scripts/claim-scan.py`"))

    def test_prose_without_a_claim_shape_is_empty(self):
        self.assertEqual([], claim_scan.shapes("the change is small and self-contained"))

    def test_a_bare_none_line_is_not_a_quantifier(self):
        self.assertEqual([], claim_scan.shapes("None."))


class FindClaimsTests(unittest.TestCase):
    def test_the_fixture_carries_one_claim_of_each_kind(self):
        kinds = {kind for _, _, kind, _ in claim_scan.find_claims(FIXTURE_DIFF)}

        self.assertEqual({"count", "absolute", "cross-ref", "path"}, kinds)

    def test_the_added_line_is_reported_with_its_new_side_number(self):
        claims = claim_scan.find_claims(FIXTURE_DIFF)

        self.assertIn(("docs/sample.md", 2, "count", "12 files"), claims)

    def test_pre_existing_and_removed_lines_are_ignored(self):
        diff = (
            "diff --git a/a.txt b/a.txt\n"
            "--- a/a.txt\n"
            "+++ b/a.txt\n"
            "@@ -1,2 +1,2 @@\n"
            " only the context line\n"
            "-only the removed line\n"
            "+unchanged prose\n"
        )

        self.assertEqual([], claim_scan.find_claims(diff))

    def test_a_path_naming_a_file_the_diff_modifies_is_dropped(self):
        diff = (
            "diff --git a/scripts/tool.py b/scripts/tool.py\n"
            "--- a/scripts/tool.py\n"
            "+++ b/scripts/tool.py\n"
            "@@ -1,1 +1,1 @@\n"
            "+Run `scripts/tool.py` for the scan.\n"
        )

        self.assertEqual([], claim_scan.find_claims(diff))

    def test_a_path_naming_a_known_untracked_file_is_dropped(self):
        diff = (
            "diff --git a/notes.md b/notes.md\n"
            "--- a/notes.md\n"
            "+++ b/notes.md\n"
            "@@ -1,1 +1,1 @@\n"
            "+Run `scripts/new-tool.py` for the scan.\n"
        )

        self.assertEqual([], claim_scan.find_claims(diff, ["scripts/new-tool.py"]))


class ScanTests(unittest.TestCase):
    def test_scan_reads_the_diff_and_the_untracked_files(self):
        repo = make_repo(self)
        (repo / "tracked.md").write_text("context\n", encoding="utf-8")
        subprocess.run(("git", "add", "tracked.md"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-qm", "one"), cwd=repo, check=True)
        (repo / "tracked.md").write_text("context\nonly the tracked change\n", encoding="utf-8")
        (repo / "untracked.md").write_text("fixed by #606\n", encoding="utf-8")

        claims = claim_scan.scan(repo, "HEAD")

        self.assertIn(("tracked.md", 2, "absolute", "only"), claims)
        self.assertIn(("untracked.md", 1, "cross-ref", "#606"), claims)


class UntrackedFileTests(unittest.TestCase):
    def test_an_untracked_file_is_scanned_as_all_added(self):
        with tempfile.TemporaryDirectory() as directory:
            repo = Path(directory)
            (repo / "notes.md").write_text("first line\nfixed by #606\n", encoding="utf-8")

            self.assertEqual([("notes.md", 2, "cross-ref", "#606")], claim_scan.scan_untracked_file(repo, "notes.md"))


class CliTests(unittest.TestCase):
    def test_the_cli_prints_the_candidates_from_a_diff_file(self):
        with tempfile.TemporaryDirectory() as directory:
            diff_file = Path(directory) / "sample.diff"
            diff_file.write_text(FIXTURE_DIFF, encoding="utf-8")
            stdout = io.StringIO()

            with contextlib.redirect_stdout(stdout):
                code = claim_scan.main(["--diff-file", str(diff_file)])

        self.assertEqual(0, code)
        self.assertIn("docs/sample.md:2  [count]  12 files", stdout.getvalue())
        self.assertIn("[absolute]  only", stdout.getvalue())
        self.assertIn("[cross-ref]  #606", stdout.getvalue())
        self.assertIn("[path]  scripts/claim-scan.py", stdout.getvalue())


if __name__ == "__main__":
    unittest.main()
