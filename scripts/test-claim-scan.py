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
from unittest import mock

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

    def test_an_absence_form_is_reported(self):
        for line in ("nothing is parked for it", "no finding reduced the guide", "no other row is pinned"):
            matches = [kind for kind, _ in claim_scan.shapes(line)]
            self.assertIn("absolute", matches, line)

    def test_a_definite_singular_is_reported(self):
        for line in ("it is the one loss vector", "the sole owner of that fact", "the single reader"):
            matches = [kind for kind, _ in claim_scan.shapes(line)]
            self.assertIn("absolute", matches, line)

    def test_a_bare_determiner_and_a_compound_surface(self):
        for line in ("no change yet", "the no-op skip"):
            matches = [kind for kind, _ in claim_scan.shapes(line)]
            self.assertIn("absolute", matches, line)

    def test_a_code_span_is_removed_before_the_class(self):
        self.assertNotIn("absolute", [kind for kind, _ in claim_scan.shapes("a `no` in a span")])

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


class FileScanTests(unittest.TestCase):
    def test_a_file_outside_the_diff_is_scanned_as_all_added(self):
        with tempfile.TemporaryDirectory() as directory:
            repo = Path(directory)
            (repo / "notes.md").write_text("first line\nfixed by #606\n", encoding="utf-8")

            self.assertEqual([("notes.md", 2, "cross-ref", "#606")], claim_scan.scan_file(repo, "notes.md"))

    def test_an_included_file_keeps_a_path_it_shares_with_the_diff(self):
        with tempfile.TemporaryDirectory() as directory:
            repo = Path(directory)
            (repo / "body.md").write_text("the row names `AGENTS.md`\n", encoding="utf-8")

            self.assertEqual(
                [("body.md", 1, "path", "AGENTS.md")],
                claim_scan.scan_file(repo, "body.md", known=["AGENTS.md"], required=True),
            )

    def test_an_unreadable_untracked_file_is_swamped_while_an_included_one_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            repo = Path(directory)

            self.assertEqual([], claim_scan.scan_file(repo, "missing.md"))
            with self.assertRaises(ValueError):
                claim_scan.scan_file(repo, "missing.md", required=True)


class ScanFailureTests(unittest.TestCase):
    def test_an_undecodable_untracked_file_does_not_stop_the_scan(self):
        with tempfile.TemporaryDirectory() as directory:
            repo = Path(directory)
            subprocess.run(["git", "init", "-q"], cwd=str(repo), check=True)
            (repo / "base.md").write_text("base\n", encoding="utf-8")
            subprocess.run(["git", "add", "base.md"], cwd=str(repo), check=True)
            subprocess.run(
                ["git", "-c", "user.email=t@example.com", "-c", "user.name=t", "commit", "-q", "-m", "base"],
                cwd=str(repo),
                check=True,
            )
            (repo / "binary.md").write_bytes(b"\xff\xfe fixed by #606\n")
            (repo / "notes.md").write_text("fixed by #606\n", encoding="utf-8")

            claims = claim_scan.scan(repo, "HEAD")

        self.assertIn(("notes.md", 1, "cross-ref", "#606"), claims)


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

    def test_a_deletion_only_diff_seeds_from_the_included_body(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            diff_file = root / "deletion.diff"
            diff_file.write_text(
                "--- a/docs/sample.md\n+++ /dev/null\n@@ -1,2 +0,0 @@\n-only 12 files\n-fixed by #606\n",
                encoding="utf-8",
            )
            body = root / "body.md"
            body.write_text("The pass keeps 3 rows and only the saved body names #741.\n", encoding="utf-8")
            stdout = io.StringIO()

            with contextlib.redirect_stdout(stdout):
                code = claim_scan.main(["--diff-file", str(diff_file), "--include", str(body), "--repo", str(root)])

        self.assertEqual(0, code)
        self.assertIn("body.md:1  [count]  3 rows", stdout.getvalue())
        self.assertIn("[absolute]  only", stdout.getvalue())
        self.assertIn("[cross-ref]  #741", stdout.getvalue())

    def test_the_include_and_the_diff_both_contribute(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            diff_file = root / "sample.diff"
            diff_file.write_text(FIXTURE_DIFF, encoding="utf-8")
            body = root / "body.md"
            body.write_text("The body names #741.\n", encoding="utf-8")
            stdout = io.StringIO()

            with contextlib.redirect_stdout(stdout):
                code = claim_scan.main(["--diff-file", str(diff_file), "--include", str(body), "--repo", str(root)])

        self.assertEqual(0, code)
        self.assertIn("docs/sample.md:2  [count]  12 files", stdout.getvalue())
        self.assertIn("body.md:1  [cross-ref]  #741", stdout.getvalue())

    def test_an_empty_included_file_keeps_the_diff_candidates(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            diff_file = root / "sample.diff"
            diff_file.write_text(FIXTURE_DIFF, encoding="utf-8")
            body = root / "body.md"
            body.write_text("", encoding="utf-8")
            stdout = io.StringIO()

            with contextlib.redirect_stdout(stdout):
                code = claim_scan.main(["--diff-file", str(diff_file), "--include", str(body), "--repo", str(root)])

        self.assertEqual(0, code)
        self.assertIn("[count]  12 files", stdout.getvalue())
        self.assertNotIn("body.md", stdout.getvalue())

    def test_a_missing_included_file_fails_and_keeps_the_diff_candidates(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            diff_file = root / "sample.diff"
            diff_file.write_text(FIXTURE_DIFF, encoding="utf-8")
            stdout = io.StringIO()
            stderr = io.StringIO()

            with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
                code = claim_scan.main(
                    ["--diff-file", str(diff_file), "--include", str(root / "missing.md"), "--repo", str(root)]
                )

        self.assertEqual(1, code)
        self.assertIn("missing.md", stderr.getvalue())
        self.assertIn("[count]  12 files", stdout.getvalue())


class VerifyModeTests(unittest.TestCase):
    """The opt-in `--verify` mode's contract: eligibility, comparison, statuses, exit code."""

    def setUp(self):
        self.repo = make_repo(self)
        (self.repo / "sample.md").write_text("alpha 8\nbeta\nzero 0\n", encoding="utf-8")
        (self.repo / "pi.md").write_text("pi 8.5\n", encoding="utf-8")
        (self.repo / "loc.md").write_text("x:20:y\n", encoding="utf-8")
        subprocess.run(("git", "add", "sample.md", "pi.md", "loc.md"), cwd=self.repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "fixture"), cwd=self.repo, check=True)
        self.sha = subprocess.run(
            ("git", "rev-parse", "HEAD"), cwd=self.repo, capture_output=True, text=True, check=True
        ).stdout.strip()

    def run_rows(self, rows):
        lines = ["## Claims table", "", "| Claim | Command | Result |", "| --- | --- | --- |"]
        for row in rows:
            lines.append("| " + " | ".join(row) + " |")
        table = self.repo / "claims.md"
        table.write_text("\n".join(lines) + "\n", encoding="utf-8")
        stdout = io.StringIO()
        with contextlib.redirect_stdout(stdout):
            code = claim_scan.main(["--verify", str(table), "--repo", str(self.repo)])
        return code, stdout.getvalue()

    def test_a_correct_explicit_row_is_verified(self):
        code, out = self.run_rows([("correct", "`git show {0}:sample.md`".format(self.sha), "`8`")])
        self.assertEqual(0, code)
        self.assertIn("verified: correct", out)
        self.assertIn("checked 1, skipped 0, mismatches 0", out)

    def test_a_wrong_literal_is_a_mismatch(self):
        code, out = self.run_rows([("wrong", "`git show {0}:sample.md`".format(self.sha), "`9`")])
        self.assertEqual(1, code)
        self.assertIn("mismatch: wrong (missing 9)", out)

    def test_a_zero_literal_is_verified(self):
        code, out = self.run_rows([("zero", "`git show {0}:sample.md`".format(self.sha), "`0`")])
        self.assertEqual(0, code)
        self.assertIn("verified: zero", out)

    def test_a_prose_result_is_advisory(self):
        code, out = self.run_rows([("prose", "`git show {0}:sample.md`".format(self.sha), "the count")])
        self.assertEqual(0, code)
        self.assertIn("advisory: prose", out)

    def test_a_two_column_row_is_advisory(self):
        code, out = self.run_rows([("two", "`git show {0}:sample.md`".format(self.sha))])
        self.assertEqual(0, code)
        self.assertIn("advisory: two (two-column table)", out)

    def test_an_ambiguous_pipe_escape_is_advisory(self):
        code, out = self.run_rows([("ambiguous", "`git show {0}:sample.md \\| grep 8`".format(self.sha), "`8`")])
        self.assertEqual(0, code)
        self.assertIn("advisory: ambiguous", out)

    def test_an_unsafe_form_is_reported(self):
        code, out = self.run_rows([("unsafe", "`git push`", "`x`")])
        self.assertEqual(0, code)
        self.assertIn("unsafe: unsafe", out)

    def test_a_working_tree_diff_is_advisory(self):
        code, out = self.run_rows([("working", "`git diff HEAD`", "`x`")])
        self.assertEqual(0, code)
        self.assertIn("advisory: working", out)

    def test_a_wrong_emitted_line_is_a_mismatch(self):
        code, out = self.run_rows([("line", "`git grep -n 'alpha' {0}`".format(self.sha), "`:2`")])
        self.assertEqual(1, code)
        self.assertIn("mismatch: line (missing :2)", out)

    def test_a_sed_write_form_is_unsafe(self):
        code, out = self.run_rows([("sed-write", "`sed -i 's/a/b/' sample.md`", "`b`")])
        self.assertEqual(0, code)
        self.assertIn("unsafe: sed-write", out)

    def test_a_git_output_form_is_unsafe(self):
        code, out = self.run_rows([("git-output", "`git log --output=x`", "`x`")])
        self.assertEqual(0, code)
        self.assertIn("unsafe: git-output", out)

    def test_a_redirection_is_unsafe(self):
        code, out = self.run_rows([("redirect", "`git show {0}:sample.md > /tmp/x`".format(self.sha), "`8`")])
        self.assertEqual(0, code)
        self.assertIn("unsafe: redirect", out)

    def test_a_pipeline_write_form_is_unsafe(self):
        code, out = self.run_rows([
            ("pipeline", "`git show {0}:sample.md` piped through `sed -i 's/a/b/'`".format(self.sha), "`8`")
        ])
        self.assertEqual(0, code)
        self.assertIn("unsafe: pipeline", out)

    def test_a_read_only_greater_than_pattern_is_not_unsafe(self):
        code, out = self.run_rows([("gt", "`git grep -n '>' {0}`".format(self.sha), "`x`")])
        self.assertEqual(1, code)
        self.assertIn("mismatch: gt", out)
        self.assertNotIn("unsafe: gt", out)

    def test_a_pipeline_with_a_deferred_command_is_external(self):
        code, out = self.run_rows([
            ("pipe-gh", "`git show {0}:sample.md` piped through `gh api repos/x`".format(self.sha), "`8`")
        ])
        self.assertEqual(0, code)
        self.assertIn("external: pipe-gh", out)

    def test_an_escaped_quote_pattern_is_not_unsafe(self):
        code, out = self.run_rows([
            ("escaped", "`git grep -n \"a\\\"b>c\" {0}`".format(self.sha), "`x`")
        ])
        self.assertEqual(1, code)
        self.assertIn("mismatch: escaped", out)
        self.assertNotIn("unsafe: escaped", out)

    def test_a_nonzero_exit_with_the_literal_is_a_mismatch(self):
        completed = subprocess.CompletedProcess(["x"], 1, stdout="8", stderr="")
        with mock.patch.object(claim_scan.subprocess, "run", return_value=completed):
            code, out = self.run_rows([("exit", "`git show {0}:sample.md`".format(self.sha), "`8`")])
        self.assertEqual(1, code)
        self.assertIn("mismatch: exit (exit 1)", out)

    def test_a_multi_literal_result_verifies_all(self):
        code, out = self.run_rows([("multi", "`git show {0}:sample.md`".format(self.sha), "`8`, `0`")])
        self.assertEqual(0, code)
        self.assertIn("verified: multi", out)

    def test_a_multi_literal_result_fails_on_a_missing_literal(self):
        code, out = self.run_rows([("multi-bad", "`git show {0}:sample.md`".format(self.sha), "`8`, `9`")])
        self.assertEqual(1, code)
        self.assertIn("mismatch: multi-bad (missing 9)", out)

    def test_a_single_revision_diff_is_advisory(self):
        code, out = self.run_rows([("diff", "`git diff {0}`".format(self.sha), "`8`")])
        self.assertEqual(0, code)
        self.assertIn("advisory: diff", out)

    def test_a_hex_pattern_without_a_revision_is_advisory(self):
        code, out = self.run_rows([("hex", "`git grep -n deadbeef`", "`8`")])
        self.assertEqual(0, code)
        self.assertIn("advisory: hex", out)

    def test_a_numeric_literal_does_not_match_a_decimal(self):
        code, out = self.run_rows([("decimal", "`git show {0}:pi.md`".format(self.sha), "`8`")])
        self.assertEqual(1, code)
        self.assertIn("mismatch: decimal (missing 8)", out)

    def test_a_bare_line_number_matches_a_path_line(self):
        code, out = self.run_rows([("line-ok", "`git grep -n 'alpha' {0}`".format(self.sha), "`:1`")])
        self.assertEqual(0, code)
        self.assertIn("verified: line-ok", out)

    def test_a_locator_does_not_match_a_longer_line_number(self):
        code, out = self.run_rows([("loc", "`git show {0}:loc.md`".format(self.sha), "`:2`")])
        self.assertEqual(1, code)
        self.assertIn("mismatch: loc (missing :2)", out)

    def test_a_locator_matches_its_own_line(self):
        code, out = self.run_rows([("loc-ok", "`git show {0}:loc.md`".format(self.sha), "`:20`")])
        self.assertEqual(0, code)
        self.assertIn("verified: loc-ok", out)

    def test_a_hex_filename_without_a_revision_is_advisory(self):
        code, out = self.run_rows([("hexfile", "`git grep -n 'alpha' -- deadbeef.md`", "`8`")])
        self.assertEqual(0, code)
        self.assertIn("advisory: hexfile", out)

    def test_two_patterns_with_a_hex_pattern_are_advisory(self):
        code, out = self.run_rows([("two-patterns", "`git grep -n -e 'alpha' -e 'deadbeef'`", "`8`")])
        self.assertEqual(0, code)
        self.assertIn("advisory: two-patterns", out)

    def test_a_mixed_quotation_pattern_operand_is_advisory(self):
        code, out = self.run_rows([("mixed", "`git grep -n -e 'alpha' -e deadbeef`", "`8`")])
        self.assertEqual(0, code)
        self.assertIn("advisory: mixed", out)

    def test_a_timeout_is_a_mismatch(self):
        with mock.patch.object(claim_scan.subprocess, "run", side_effect=subprocess.TimeoutExpired("x", 10)):
            code, out = self.run_rows([("slow", "`git show {0}:sample.md`".format(self.sha), "`8`")])
        self.assertEqual(1, code)
        self.assertIn("mismatch: slow (timeout)", out)

    def test_an_execution_failure_is_a_mismatch(self):
        with mock.patch.object(claim_scan.subprocess, "run", side_effect=OSError("boom")):
            code, out = self.run_rows([("broken", "`git show {0}:sample.md`".format(self.sha), "`8`")])
        self.assertEqual(1, code)
        self.assertIn("mismatch: broken (boom)", out)


if __name__ == "__main__":
    unittest.main()
