#!/usr/bin/env python3
"""Tests for the commit-hygiene checker (`scripts/commit-hygiene.py`). Stdlib only.

Run with `python3 scripts/test-commit-hygiene.py`. The checker's kebab-case filename is not an importable identifier, so
it is loaded by path.
"""

import contextlib
import importlib.util
import io
import subprocess
import tempfile
import unittest
from pathlib import Path

CHECKER_PATH = Path(__file__).resolve().parent / "commit-hygiene.py"
_spec = importlib.util.spec_from_file_location("commit_hygiene", CHECKER_PATH)
commit_hygiene = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(commit_hygiene)


def make_repo(test: unittest.TestCase) -> Path:
    directory = tempfile.TemporaryDirectory()
    test.addCleanup(directory.cleanup)
    repo = Path(directory.name)
    subprocess.run(("git", "init", "-q"), cwd=repo, check=True)
    subprocess.run(("git", "config", "user.email", "test@example.com"), cwd=repo, check=True)
    subprocess.run(("git", "config", "user.name", "Test"), cwd=repo, check=True)
    return repo


class StagedThenEditedTests(unittest.TestCase):
    def test_a_path_staged_and_then_edited_is_reported(self):
        repo = make_repo(self)
        (repo / "a.txt").write_text("one", encoding="utf-8")
        subprocess.run(("git", "add", "a.txt"), cwd=repo, check=True)
        (repo / "a.txt").write_text("two", encoding="utf-8")

        self.assertEqual(["a.txt"], commit_hygiene.find_staged_then_edited(repo))

    def test_a_cleanly_staged_path_is_not_reported(self):
        repo = make_repo(self)
        (repo / "a.txt").write_text("one", encoding="utf-8")
        subprocess.run(("git", "add", "a.txt"), cwd=repo, check=True)

        self.assertEqual([], commit_hygiene.find_staged_then_edited(repo))


class TrackedIgnoredTests(unittest.TestCase):
    def _repo_with_committed_artifact(self) -> Path:
        repo = make_repo(self)
        (repo / ".gitignore").write_text("*.pyc\n", encoding="utf-8")
        (repo / "a.pyc").write_text("x", encoding="utf-8")
        subprocess.run(("git", "add", ".gitignore"), cwd=repo, check=True)
        subprocess.run(("git", "add", "-f", "a.pyc"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)
        return repo

    def test_a_force_committed_ignored_file_is_reported(self):
        repo = self._repo_with_committed_artifact()

        self.assertEqual(["a.pyc"], commit_hygiene.find_tracked_ignored(repo))

    def test_a_clean_repository_is_not_reported(self):
        repo = make_repo(self)
        (repo / "a.txt").write_text("x", encoding="utf-8")
        subprocess.run(("git", "add", "a.txt"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)

        self.assertEqual([], commit_hygiene.find_tracked_ignored(repo))

    def test_tracked_ignored_mode_reports_the_offender(self):
        repo = self._repo_with_committed_artifact()

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--tracked-ignored", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn("a.pyc", stderr.getvalue())

    def test_tracked_ignored_mode_passes_a_clean_repository(self):
        repo = make_repo(self)
        (repo / "a.txt").write_text("x", encoding="utf-8")
        subprocess.run(("git", "add", "a.txt"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--tracked-ignored", "--repo", str(repo)])

        self.assertEqual(0, code)
        self.assertEqual("", stderr.getvalue())


class ForceStagedArtifactTests(unittest.TestCase):
    def test_a_force_staged_ignored_artifact_is_reported(self):
        repo = make_repo(self)
        (repo / ".gitignore").write_text("*.pyc\n", encoding="utf-8")
        (repo / "a.pyc").write_text("x", encoding="utf-8")
        subprocess.run(("git", "add", ".gitignore"), cwd=repo, check=True)
        subprocess.run(("git", "add", "-f", "a.pyc"), cwd=repo, check=True)

        self.assertEqual(["a.pyc"], commit_hygiene.find_force_staged_artifacts(repo))

    def test_an_ordinary_staged_file_is_not_reported(self):
        repo = make_repo(self)
        (repo / "a.txt").write_text("x", encoding="utf-8")
        subprocess.run(("git", "add", "a.txt"), cwd=repo, check=True)

        self.assertEqual([], commit_hygiene.find_force_staged_artifacts(repo))


class MisplacedMarkerTests(unittest.TestCase):
    def test_a_marker_on_a_bold_lead_bullet_passes(self):
        text = "- **Rule.** Some text. captured: some-change\n"

        self.assertEqual([], commit_hygiene.find_misplaced_markers(text))

    def test_a_marker_on_a_bold_lead_paragraph_passes(self):
        text = "**Rule.** Some text. captured: some-change\n"

        self.assertEqual([], commit_hygiene.find_misplaced_markers(text))

    def test_a_marker_on_a_plain_bullet_fails(self):
        text = "- Plain text. captured: some-change\n"

        offenders = commit_hygiene.find_misplaced_markers(text)

        self.assertEqual(1, len(offenders))
        self.assertEqual(1, offenders[0][0])

    def test_a_marker_in_a_rule_continuation_paragraph_passes(self):
        text = "**Rule.** Some text.\n\nA continuation paragraph of the rule. captured: some-change\n"

        self.assertEqual([], commit_hygiene.find_misplaced_markers(text))

    def test_a_marker_wrapped_onto_the_next_line_passes(self):
        text = "- **Rule.** Some text. captured:\n  some-change\n"

        self.assertEqual([], commit_hygiene.find_misplaced_markers(text))

    def test_a_backticked_prose_mention_is_ignored(self):
        text = "- **Rule.** The `captured:` token and `captured: <change>` are not markers.\n"

        self.assertEqual([], commit_hygiene.find_misplaced_markers(text))

    def test_a_merged_into_bullet_with_a_mid_item_marker_passes(self):
        text = "- **Rule.** First. captured: change-a\n  Later text appended. captured: change-b\n"

        self.assertEqual([], commit_hygiene.find_misplaced_markers(text))

    def test_the_current_agents_md_has_no_misplaced_markers(self):
        text = (commit_hygiene.REPO_ROOT / "AGENTS.md").read_text(encoding="utf-8")

        self.assertEqual([], commit_hygiene.find_misplaced_markers(text))


class FormatterTests(unittest.TestCase):
    @staticmethod
    def _check(repo, formatter):
        with contextlib.redirect_stderr(io.StringIO()):
            code = commit_hygiene.check_staged(repo, formatter)
        return code

    def test_a_failing_formatter_refuses_the_commit(self):
        repo = make_repo(self)
        (repo / "a.md").write_text("# A\n", encoding="utf-8")
        subprocess.run(("git", "add", "a.md"), cwd=repo, check=True)

        self.assertEqual(1, self._check(repo, ["false"]))

    def test_a_passing_formatter_allows_the_commit(self):
        repo = make_repo(self)
        (repo / "a.md").write_text("# A\n", encoding="utf-8")
        subprocess.run(("git", "add", "a.md"), cwd=repo, check=True)

        self.assertEqual(0, self._check(repo, ["true"]))

    def test_the_formatter_is_skipped_when_no_owned_file_is_staged(self):
        repo = make_repo(self)
        (repo / "a.txt").write_text("x", encoding="utf-8")
        subprocess.run(("git", "add", "a.txt"), cwd=repo, check=True)

        self.assertEqual(0, self._check(repo, ["false"]))

    def test_a_missing_formatter_command_refuses_with_a_clear_message(self):
        repo = make_repo(self)
        (repo / "a.md").write_text("# A\n", encoding="utf-8")
        subprocess.run(("git", "add", "a.md"), cwd=repo, check=True)

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.check_staged(repo, ["./does-not-exist"])

        self.assertEqual(1, code)
        self.assertIn("formatter command cannot be run", stderr.getvalue())

    def test_a_non_executable_formatter_command_refuses_with_a_clear_message(self):
        repo = make_repo(self)
        (repo / "a.md").write_text("# A\n", encoding="utf-8")
        subprocess.run(("git", "add", "a.md"), cwd=repo, check=True)
        not_executable = repo / "formatter.sh"
        not_executable.write_text("#!/bin/sh\nexit 0\n", encoding="utf-8")

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.check_staged(repo, ["./formatter.sh"])

        self.assertEqual(1, code)
        self.assertIn("formatter command cannot be run", stderr.getvalue())


class StagedMarkerTests(unittest.TestCase):
    def test_staged_mode_reports_a_marker_on_a_plain_bullet(self):
        repo = make_repo(self)
        (repo / "AGENTS.md").write_text("- Plain. captured: some-change\n", encoding="utf-8")
        subprocess.run(("git", "add", "AGENTS.md"), cwd=repo, check=True)

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.check_staged(repo, ["true"])

        self.assertEqual(1, code)
        self.assertIn("AGENTS.md:1", stderr.getvalue())

    def test_staged_mode_skips_the_marker_check_when_agents_is_not_staged(self):
        repo = make_repo(self)
        (repo / "a.txt").write_text("x", encoding="utf-8")
        subprocess.run(("git", "add", "a.txt"), cwd=repo, check=True)

        self.assertEqual(0, commit_hygiene.check_staged(repo, ["true"]))


class CliTests(unittest.TestCase):
    @staticmethod
    def _run(argv):
        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(argv)
        return code, stderr.getvalue()

    def test_markers_mode_reports_a_misplaced_marker(self):
        repo = make_repo(self)
        (repo / "AGENTS.md").write_text("- Plain. captured: some-change\n", encoding="utf-8")

        code, stderr = self._run(["--markers", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn("AGENTS.md:1", stderr)

    def test_markers_mode_passes_a_correctly_placed_marker(self):
        repo = make_repo(self)
        (repo / "AGENTS.md").write_text("- **Rule.** Some text. captured: some-change\n", encoding="utf-8")

        code, stderr = self._run(["--markers", "--repo", str(repo)])

        self.assertEqual(0, code)
        self.assertEqual("", stderr)

    def test_staged_mode_reports_a_stale_index(self):
        repo = make_repo(self)
        (repo / "a.txt").write_text("one", encoding="utf-8")
        subprocess.run(("git", "add", "a.txt"), cwd=repo, check=True)
        (repo / "a.txt").write_text("two", encoding="utf-8")

        code, stderr = self._run(["--staged", "--repo", str(repo), "--formatter", "true"])

        self.assertEqual(1, code)
        self.assertIn("a.txt", stderr)


if __name__ == "__main__":
    unittest.main()
