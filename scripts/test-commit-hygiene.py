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
    write_claim(repo, "demo")
    return repo


def claim_path(repo: Path) -> Path:
    resolved = subprocess.run(
        ("git", "rev-parse", "--git-path", commit_hygiene.CLAIM_NAME),
        cwd=repo,
        capture_output=True,
        text=True,
        check=True,
    ).stdout.strip()
    path = Path(resolved)
    return path if path.is_absolute() else repo / path


def write_claim(repo: Path, value: str) -> None:
    claim_path(repo).write_text(value + "\n", encoding="utf-8")


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

    def test_a_non_ascii_path_staged_and_then_edited_is_reported_by_real_name(self):
        repo = make_repo(self)
        (repo / "café.txt").write_text("one", encoding="utf-8")
        subprocess.run(("git", "add", "café.txt"), cwd=repo, check=True)
        (repo / "café.txt").write_text("two", encoding="utf-8")

        self.assertEqual(["café.txt"], commit_hygiene.find_staged_then_edited(repo))

    def test_a_renamed_staged_path_does_not_yield_a_bogus_offender(self):
        repo = make_repo(self)
        (repo / "RM.txt").write_text("one", encoding="utf-8")
        subprocess.run(("git", "add", "RM.txt"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)
        subprocess.run(("git", "mv", "RM.txt", "New.txt"), cwd=repo, check=True)
        (repo / "New.txt").write_text("two", encoding="utf-8")

        self.assertEqual(["New.txt"], commit_hygiene.find_staged_then_edited(repo))

    def test_a_clean_rename_is_not_reported(self):
        repo = make_repo(self)
        (repo / "RM.txt").write_text("one", encoding="utf-8")
        subprocess.run(("git", "add", "RM.txt"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)
        subprocess.run(("git", "mv", "RM.txt", "New.txt"), cwd=repo, check=True)

        self.assertEqual([], commit_hygiene.find_staged_then_edited(repo))


class ArchiveSyncTests(unittest.TestCase):
    @staticmethod
    def _stage(repo: Path, *paths: str) -> None:
        subprocess.run(("git", "add", *paths), cwd=repo, check=True)

    def _stage_archive_move(self, repo: Path, name: str = "2026-01-01-demo", with_deltas: bool = True) -> None:
        change = repo / "openspec" / "changes" / "archive" / name
        change.mkdir(parents=True, exist_ok=True)
        (change / "proposal.md").write_text("# Proposal\n", encoding="utf-8")
        if with_deltas:
            spec = change / "specs" / "showcase" / "demo" / "spec.md"
            spec.parent.mkdir(parents=True, exist_ok=True)
            spec.write_text("# Spec Delta\n", encoding="utf-8")

    @staticmethod
    def _write_main_spec(repo: Path) -> None:
        main_spec = repo / "openspec" / "specs" / "showcase" / "demo" / "spec.md"
        main_spec.parent.mkdir(parents=True, exist_ok=True)
        main_spec.write_text("# Spec\n", encoding="utf-8")

    def test_an_unstaged_sync_is_reported(self):
        repo = make_repo(self)
        self._stage_archive_move(repo)
        self._write_main_spec(repo)
        self._stage(repo, "openspec/changes/archive")

        offenders = commit_hygiene.find_unpaired_archive_sync(repo)

        self.assertEqual(
            [("openspec/changes/archive/2026-01-01-demo", ["openspec/specs/showcase/demo/spec.md"])],
            offenders,
        )

    def test_a_modified_tracked_spec_is_reported(self):
        repo = make_repo(self)
        self._write_main_spec(repo)
        self._stage(repo, "openspec/specs")
        subprocess.run(("git", "commit", "-q", "-m", "add spec"), cwd=repo, check=True)
        self._stage_archive_move(repo)
        self._stage(repo, "openspec/changes/archive")
        (repo / "openspec" / "specs" / "showcase" / "demo" / "spec.md").write_text("# Spec v2\n", encoding="utf-8")

        self.assertEqual(
            [("openspec/changes/archive/2026-01-01-demo", ["openspec/specs/showcase/demo/spec.md"])],
            commit_hygiene.find_unpaired_archive_sync(repo),
        )

    def test_a_staged_sync_is_not_reported(self):
        repo = make_repo(self)
        self._stage_archive_move(repo)
        self._write_main_spec(repo)
        self._stage(repo, "openspec/changes/archive", "openspec/specs")

        self.assertEqual([], commit_hygiene.find_unpaired_archive_sync(repo))

    def test_a_no_delta_archive_move_is_not_reported(self):
        repo = make_repo(self)
        self._stage_archive_move(repo, with_deltas=False)
        self._write_main_spec(repo)
        self._stage(repo, "openspec/changes/archive")

        self.assertEqual([], commit_hygiene.find_unpaired_archive_sync(repo))

    def test_a_partially_staged_archive_change_still_reports(self):
        repo = make_repo(self)
        self._stage_archive_move(repo)
        self._write_main_spec(repo)
        self._stage(repo, "openspec/changes/archive/2026-01-01-demo/proposal.md")

        self.assertEqual(
            [("openspec/changes/archive/2026-01-01-demo", ["openspec/specs/showcase/demo/spec.md"])],
            commit_hygiene.find_unpaired_archive_sync(repo),
        )

    def test_check_staged_refuses_an_unpaired_archive_move(self):
        repo = make_repo(self)
        self._stage_archive_move(repo)
        self._write_main_spec(repo)
        self._stage(repo, "openspec/changes/archive")

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.check_staged(repo, ["true"])

        self.assertEqual(1, code)
        self.assertIn("openspec/changes/archive/2026-01-01-demo", stderr.getvalue())
        self.assertIn("openspec/specs/showcase/demo/spec.md", stderr.getvalue())

    def test_check_staged_allows_a_paired_archive_move(self):
        repo = make_repo(self)
        self._stage_archive_move(repo)
        self._write_main_spec(repo)
        self._stage(repo, "openspec/changes/archive", "openspec/specs")

        with contextlib.redirect_stderr(io.StringIO()):
            code = commit_hygiene.check_staged(repo, ["true"])

        self.assertEqual(0, code)


class TrackedIgnoredTests(unittest.TestCase):
    def _repo_with_committed_artifact(self, name: str = "a.pyc") -> Path:
        repo = make_repo(self)
        (repo / ".gitignore").write_text("*.pyc\n", encoding="utf-8")
        (repo / name).write_text("x", encoding="utf-8")
        subprocess.run(("git", "add", ".gitignore"), cwd=repo, check=True)
        subprocess.run(("git", "add", "-f", name), cwd=repo, check=True)
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

    def test_a_non_ascii_force_committed_ignored_file_is_reported_by_real_name(self):
        repo = self._repo_with_committed_artifact(name="café.pyc")

        self.assertEqual(["café.pyc"], commit_hygiene.find_tracked_ignored(repo))

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

    def test_a_non_ascii_force_staged_artifact_is_reported_across_both_sites(self):
        repo = make_repo(self)
        (repo / ".gitignore").write_text("*.pyc\n", encoding="utf-8")
        (repo / "café.pyc").write_text("x", encoding="utf-8")
        subprocess.run(("git", "add", ".gitignore"), cwd=repo, check=True)
        subprocess.run(("git", "add", "-f", "café.pyc"), cwd=repo, check=True)

        self.assertEqual(["café.pyc"], commit_hygiene.find_force_staged_artifacts(repo))


class MarkerFileScopeTests(unittest.TestCase):
    def test_the_project_guidance_files_are_scanned_with_agents_md(self):
        repo = make_repo(self)
        skill = repo / ".opencode/skills/check-and-test-authoring/SKILL.md"
        skill.parent.mkdir(parents=True)
        skill.write_text("- **Rule.** Some text. captured: unit-a\n", encoding="utf-8")
        (repo / "AGENTS.md").write_text("- **Rule.** Some text. captured: unit-a\n", encoding="utf-8")

        self.assertEqual(
            ["AGENTS.md", ".opencode/skills/check-and-test-authoring/SKILL.md"],
            commit_hygiene.marker_paths(repo),
        )

    def test_a_misplaced_marker_in_a_skill_file_fails_and_names_the_file(self):
        repo = make_repo(self)
        skill = repo / ".opencode/skills/check-and-test-authoring/SKILL.md"
        skill.parent.mkdir(parents=True)
        skill.write_text("- **Rule.** Some text.\n- Plain text. captured: unit-a\n", encoding="utf-8")
        (repo / "AGENTS.md").write_text("- **Rule.** Some text. captured: unit-a\n", encoding="utf-8")

        with contextlib.redirect_stderr(io.StringIO()) as stderr:
            failures = commit_hygiene.check_markers(repo)

        self.assertEqual(1, failures)
        self.assertIn(".opencode/skills/check-and-test-authoring/SKILL.md:2", stderr.getvalue())

    def test_a_vendored_skill_file_is_out_of_the_marker_scope(self):
        repo = make_repo(self)
        (repo / "AGENTS.md").write_text("- **Rule.** Some text. captured: unit-a\n", encoding="utf-8")
        for name in ("ast-grep", "check-and-test-authoring"):
            skill = repo / f".opencode/skills/{name}/SKILL.md"
            skill.parent.mkdir(parents=True, exist_ok=True)
            skill.write_text("- **Rule.** Some text. captured: unit-a\n", encoding="utf-8")

        self.assertEqual(
            ["AGENTS.md", ".opencode/skills/check-and-test-authoring/SKILL.md"],
            commit_hygiene.marker_paths(repo),
        )


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

    def test_a_non_ascii_formatter_owned_file_is_staged_under_its_real_name(self):
        repo = make_repo(self)
        (repo / "café.md").write_text("# A\n", encoding="utf-8")
        subprocess.run(("git", "add", "café.md"), cwd=repo, check=True)

        staged = commit_hygiene.staged_paths(repo)

        self.assertIn("café.md", staged)
        self.assertEqual(["café.md"], commit_hygiene.formatter_owned(staged))

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


class ConflictMarkerTests(unittest.TestCase):
    def _repo_with_marker(self) -> Path:
        repo = make_repo(self)
        (repo / "a.md").write_text("text\n<<<<<<< HEAD\nours\n=======\ntheirs\n>>>>>>> branch\n", encoding="utf-8")
        subprocess.run(("git", "add", "a.md"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)
        return repo

    def test_a_tracked_conflict_marker_is_reported(self):
        repo = self._repo_with_marker()

        offenders = commit_hygiene.find_conflict_markers(repo)

        self.assertTrue(any(entry.startswith("a.md:2:") for entry in offenders))
        self.assertTrue(any(entry.startswith("a.md:6:") for entry in offenders))

    def test_a_clean_repository_is_not_reported(self):
        repo = make_repo(self)
        (repo / "a.md").write_text("text\n", encoding="utf-8")
        subprocess.run(("git", "add", "a.md"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)

        self.assertEqual([], commit_hygiene.find_conflict_markers(repo))

    def test_staged_mode_reports_a_conflict_marker(self):
        repo = make_repo(self)
        (repo / "a.md").write_text("text\n<<<<<<< HEAD\nours\n>>>>>>> branch\n", encoding="utf-8")
        subprocess.run(("git", "add", "a.md"), cwd=repo, check=True)

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.check_staged(repo, ["true"])

        self.assertGreaterEqual(code, 1)
        self.assertIn("a.md:2:", stderr.getvalue())
        self.assertIn("a.md:4:", stderr.getvalue())

    def test_conflict_markers_mode_reports_the_offender(self):
        repo = self._repo_with_marker()

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--conflict-markers", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn("a.md:2:", stderr.getvalue())


class ExecutableBitTests(unittest.TestCase):
    def _repo_with_script(self, mode: int) -> Path:
        repo = make_repo(self)
        script = repo / "scripts" / "foo.sh"
        script.parent.mkdir(parents=True, exist_ok=True)
        script.write_text("#!/bin/sh\n", encoding="utf-8")
        script.chmod(mode)
        subprocess.run(("git", "add", "scripts/foo.sh"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)
        return repo

    def test_a_non_executable_shell_script_is_reported(self):
        repo = self._repo_with_script(0o644)

        self.assertEqual(["scripts/foo.sh"], commit_hygiene.find_non_executable_scripts(repo))

    def test_an_executable_shell_script_is_not_reported(self):
        repo = self._repo_with_script(0o755)

        self.assertEqual([], commit_hygiene.find_non_executable_scripts(repo))

    def test_a_non_ascii_shell_script_is_reported(self):
        repo = make_repo(self)
        script = repo / "café.sh"
        script.write_text("#!/bin/sh\n", encoding="utf-8")
        script.chmod(0o644)
        subprocess.run(("git", "add", "café.sh"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)

        self.assertEqual(["café.sh"], commit_hygiene.find_non_executable_scripts(repo))

    def test_a_vendored_skill_script_is_not_reported(self):
        repo = make_repo(self)
        script = repo / ".opencode" / "skills" / "axon4to5-x" / "scripts" / "foo.sh"
        script.parent.mkdir(parents=True, exist_ok=True)
        script.write_text("#!/bin/sh\n", encoding="utf-8")
        script.chmod(0o644)
        subprocess.run(("git", "add", ".opencode/skills/axon4to5-x/scripts/foo.sh"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)

        self.assertEqual([], commit_hygiene.find_non_executable_scripts(repo))

    def test_a_non_executable_hook_is_reported(self):
        repo = make_repo(self)
        hook = repo / "scripts" / "git-hooks" / "pre-commit"
        hook.parent.mkdir(parents=True, exist_ok=True)
        hook.write_text("#!/bin/sh\n", encoding="utf-8")
        hook.chmod(0o644)
        subprocess.run(("git", "add", "scripts/git-hooks/pre-commit"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)

        self.assertEqual(["scripts/git-hooks/pre-commit"], commit_hygiene.find_non_executable_scripts(repo))

    def test_a_non_executable_gradle_wrapper_is_reported(self):
        repo = make_repo(self)
        wrapper = repo / "gradlew"
        wrapper.write_text("#!/bin/sh\n", encoding="utf-8")
        wrapper.chmod(0o644)
        subprocess.run(("git", "add", "gradlew"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)

        self.assertEqual(["gradlew"], commit_hygiene.find_non_executable_scripts(repo))

    def test_a_generated_skill_script_is_not_reported(self):
        repo = make_repo(self)
        script = repo / ".opencode" / "skills" / "openspec-x" / "scripts" / "foo.sh"
        script.parent.mkdir(parents=True, exist_ok=True)
        script.write_text("#!/bin/sh\n", encoding="utf-8")
        script.chmod(0o644)
        subprocess.run(("git", "add", ".opencode/skills/openspec-x/scripts/foo.sh"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", "add"), cwd=repo, check=True)

        self.assertEqual([], commit_hygiene.find_non_executable_scripts(repo))

    def test_executable_bits_mode_reports_the_offender(self):
        repo = self._repo_with_script(0o644)

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--executable-bits", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn("scripts/foo.sh", stderr.getvalue())


class UniqueCronTests(unittest.TestCase):
    def _repo_with_workflows(self, workflows: dict[str, str]) -> Path:
        repo = make_repo(self)
        directory = repo / ".github" / "workflows"
        directory.mkdir(parents=True)
        for name, cron_line in workflows.items():
            (directory / name).write_text(
                "name: {0}\non:\n  schedule:\n    {1}\n".format(name, cron_line), encoding="utf-8"
            )
        return repo

    def _repo_with_workflow_text(self, name: str, text: str) -> Path:
        repo = make_repo(self)
        directory = repo / ".github" / "workflows"
        directory.mkdir(parents=True)
        (directory / name).write_text(text, encoding="utf-8")
        return repo

    def test_an_exact_duplicate_cron_collides(self):
        repo = self._repo_with_workflows(
            {"a.yml": "- cron: '0 2 * * 1'", "b.yml": "- cron: '0 2 * * 1'"}
        )

        collisions = commit_hygiene.find_cron_collisions(repo)

        self.assertEqual(1, len(collisions))
        self.assertEqual(".github/workflows/a.yml", collisions[0][0])
        self.assertEqual("0 2 * * 1", collisions[0][1])
        self.assertEqual(".github/workflows/b.yml", collisions[0][2])
        self.assertEqual("0 2 * * 1", collisions[0][3])

    def test_two_schedules_in_one_workflow_file_collide(self):
        repo = self._repo_with_workflow_text(
            "a.yml", "name: a\non:\n  schedule:\n    - cron: '0 2 * * 1'\n    - cron: '0 2 * * 1'\n"
        )

        collisions = commit_hygiene.find_cron_collisions(repo)

        self.assertEqual(1, len(collisions))
        self.assertEqual(".github/workflows/a.yml", collisions[0][0])
        self.assertEqual(".github/workflows/a.yml", collisions[0][2])

    def test_a_daily_and_a_weekly_schedule_sharing_a_minute_collide(self):
        repo = self._repo_with_workflows(
            {"daily.yml": "- cron: '15 20 * * *'", "weekly.yml": "- cron: '15 20 * * 0'"}
        )

        self.assertEqual(1, len(commit_hygiene.find_cron_collisions(repo)))

    def test_two_weekly_schedules_on_different_days_do_not_collide(self):
        repo = self._repo_with_workflows(
            {"mon.yml": "- cron: '0 2 * * 1'", "wed.yml": "- cron: '0 2 * * 3'"}
        )

        self.assertEqual([], commit_hygiene.find_cron_collisions(repo))

    def test_a_sunday_written_seven_collides_with_zero(self):
        repo = self._repo_with_workflows(
            {"a.yml": "- cron: '0 2 * * 7'", "b.yml": "- cron: '0 2 * * 0'"}
        )

        self.assertEqual(1, len(commit_hygiene.find_cron_collisions(repo)))

    def test_two_different_restricted_day_schedules_do_not_collide(self):
        repo = self._repo_with_workflows(
            {"first.yml": "- cron: '0 2 1 * *'", "fifteenth.yml": "- cron: '0 2 15 * *'"}
        )

        self.assertEqual([], commit_hygiene.find_cron_collisions(repo))

    def test_identical_restricted_day_schedules_collide(self):
        repo = self._repo_with_workflows(
            {"a.yml": "- cron: '0 2 1 * *'", "b.yml": "- cron: '0 2 1 * *'"}
        )

        self.assertEqual(1, len(commit_hygiene.find_cron_collisions(repo)))

    def test_a_malformed_cron_is_not_a_collision(self):
        repo = self._repo_with_workflows(
            {"a.yml": "- cron: '0 2 * *'", "b.yml": "- cron: '0 2 * * 1'"}
        )

        self.assertEqual([], commit_hygiene.find_cron_collisions(repo))

    def test_a_commented_out_cron_is_not_a_schedule(self):
        repo = self._repo_with_workflows(
            {"a.yml": "# - cron: '0 2 * * 1'", "b.yml": "- cron: '0 2 * * 1'"}
        )

        self.assertEqual([], commit_hygiene.find_cron_collisions(repo))

    def test_an_inline_comment_is_stripped_from_an_unquoted_cron(self):
        repo = self._repo_with_workflows(
            {"a.yml": "- cron: 0 2 * * 1 # primary", "b.yml": "- cron: '0 2 * * 1'"}
        )

        self.assertEqual(1, len(commit_hygiene.find_cron_collisions(repo)))

    def test_a_clean_set_of_schedules_passes(self):
        repo = self._repo_with_workflows(
            {"a.yml": "- cron: '0 2 * * 1'", "b.yml": "- cron: '0 3 * * 1'"}
        )

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--unique-crons", "--repo", str(repo)])

        self.assertEqual(0, code)
        self.assertEqual("", stderr.getvalue())

    def test_a_yaml_workflow_is_scanned(self):
        repo = self._repo_with_workflows(
            {"a.yml": "- cron: '0 2 * * 1'", "b.yaml": "- cron: '0 2 * * 1'"}
        )

        collisions = commit_hygiene.find_cron_collisions(repo)

        self.assertEqual(1, len(collisions))
        self.assertEqual(".github/workflows/b.yaml", collisions[0][2])

    def test_unique_crons_mode_names_both_files_and_the_shared_cron(self):
        repo = self._repo_with_workflows(
            {"a.yml": "- cron: '0 2 * * 1'", "b.yml": "- cron: '0 2 * * 1'"}
        )

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--unique-crons", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn(".github/workflows/a.yml", stderr.getvalue())
        self.assertIn(".github/workflows/b.yml", stderr.getvalue())
        self.assertIn("0 2 * * 1", stderr.getvalue())


class LargeFileTests(unittest.TestCase):
    def _repo_with_limit(self, limit: int) -> Path:
        repo = make_repo(self)
        config = repo / "config" / "commit-hygiene" / "large-files.properties"
        config.parent.mkdir(parents=True)
        config.write_text("# max size\nmaxBytes = {0}\n".format(limit), encoding="utf-8")
        return repo

    def _commit(self, repo: Path, name: str, size: int) -> None:
        path = repo / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(b"x" * size)
        subprocess.run(("git", "add", "-f", name), cwd=repo, check=True)

    def test_the_limit_is_read_from_the_config_file(self):
        repo = self._repo_with_limit(4096)

        self.assertEqual(4096, commit_hygiene.read_max_bytes(repo))

    def test_a_tracked_file_over_the_limit_is_reported(self):
        repo = self._repo_with_limit(4096)
        self._commit(repo, "big.bin", 4097)

        offenders = commit_hygiene.find_oversized_files(repo, 4096)

        self.assertEqual([("big.bin", 4097)], offenders)

    def test_a_tracked_file_at_the_limit_is_not_reported(self):
        repo = self._repo_with_limit(4096)
        self._commit(repo, "exact.bin", 4096)

        self.assertEqual([], commit_hygiene.find_oversized_files(repo, 4096))

    def test_a_tracked_file_under_the_limit_is_not_reported(self):
        repo = self._repo_with_limit(4096)
        self._commit(repo, "small.txt", 10)

        self.assertEqual([], commit_hygiene.find_oversized_files(repo, 4096))

    def test_large_files_mode_reports_the_offender(self):
        repo = self._repo_with_limit(4096)
        self._commit(repo, "big.bin", 5000)

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--large-files", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn("big.bin", stderr.getvalue())
        self.assertIn("5000", stderr.getvalue())

    def test_large_files_mode_passes_a_repository_under_the_limit(self):
        repo = self._repo_with_limit(4096)
        self._commit(repo, "small.txt", 10)

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--large-files", "--repo", str(repo)])

        self.assertEqual(0, code)
        self.assertEqual("", stderr.getvalue())

    def test_a_missing_config_is_reported_not_a_traceback(self):
        repo = make_repo(self)
        self._commit(repo, "small.txt", 10)

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--large-files", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn("misconfigured", stderr.getvalue())

    def test_a_non_integer_limit_is_reported_not_a_traceback(self):
        repo = self._repo_with_limit(0)
        (repo / "config" / "commit-hygiene" / "large-files.properties").write_text(
            "maxBytes = big\n", encoding="utf-8"
        )
        self._commit(repo, "small.txt", 10)

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--large-files", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn("misconfigured", stderr.getvalue())


class UniqueReviewRoundTests(unittest.TestCase):
    def _write_ledger(self, repo: Path, body: str) -> Path:
        ledger = repo / "docs" / "review-rounds.tsv"
        ledger.parent.mkdir(parents=True, exist_ok=True)
        ledger.write_text("# One row per review round.\n" + body, encoding="utf-8")
        return ledger

    def test_a_duplicated_round_is_reported_with_both_lines(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\twording\t-\t1\n"
            "2026-10-16\tunit-a\t2\t-\t-\t0\n"
            "2026-10-16\tunit-a\t2\t-\t-\t0\n",
        )

        self.assertEqual(commit_hygiene.find_duplicate_review_rounds(repo), [("unit-a", "2", 3, 4)])
        with contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(commit_hygiene.check_unique_review_rounds(repo), 1)

    def test_the_same_round_for_another_unit_is_not_a_duplicate(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\twording\t-\t1\n"
            "2026-10-16\tunit-b\t1\t-\t-\t0\n",
        )

        self.assertEqual(commit_hygiene.find_duplicate_review_rounds(repo), [])
        with contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(commit_hygiene.check_unique_review_rounds(repo), 0)

    def test_a_missing_ledger_is_not_reported(self):
        repo = make_repo(self)

        self.assertEqual(commit_hygiene.find_duplicate_review_rounds(repo), [])

    def test_the_mode_is_reachable_through_main(self):
        repo = make_repo(self)
        self._write_ledger(repo, "2026-10-16\tunit-a\t1\twording\t-\t1\n")

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(commit_hygiene.main(["--unique-rounds", "--repo", str(repo)]), 0)

        self._write_ledger(repo, "2026-10-16\tunit-a\t1\twording\t-\t1\n2026-10-16\tunit-a\t1\twording\t-\t1\n")
        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(commit_hygiene.main(["--unique-rounds", "--repo", str(repo)]), 1)
        self.assertIn("share unit 'unit-a' round 1", stderr.getvalue())

    def test_a_gap_in_a_units_rounds_is_reported(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\twording\t-\t1\n"
            "2026-10-16\tunit-a\t2\t-\t-\t0\n"
            "2026-10-16\tunit-a\t4\t-\t-\t0\n",
        )

        self.assertEqual(commit_hygiene.find_round_gaps(repo), [("unit-a", 3)])
        with contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(commit_hygiene.check_unique_review_rounds(repo), 1)

    def test_an_acknowledged_gap_passes_and_an_unacknowledged_one_does_not(self):
        repo = make_repo(self)
        body = (
            "2026-10-16\tunit-a\t1\twording\t-\t1\n"
            "2026-10-16\tunit-a\t3\t-\t-\t0\n"
        )
        self._write_ledger(repo, body)
        with contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(commit_hygiene.check_unique_review_rounds(repo), 1)

        self._write_ledger(
            repo,
            "# --- Acknowledged gap: unit-a round 2 - the report was never reconstructed\n" + body,
        )

        self.assertEqual(commit_hygiene.acknowledged_gaps(repo), {("unit-a", 2)})
        with contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(commit_hygiene.check_unique_review_rounds(repo), 0)

    def test_rounds_starting_at_two_are_reported(self):
        repo = make_repo(self)
        self._write_ledger(repo, "2026-10-16\tunit-a\t2\t-\t-\t0\n")

        self.assertEqual(commit_hygiene.find_round_gaps(repo), [("unit-a", 1)])

    def test_two_units_are_checked_independently(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\twording\t-\t1\n"
            "2026-10-16\tunit-a\t2\t-\t-\t0\n"
            "2026-10-16\tunit-b\t2\t-\t-\t0\n",
        )

        # unit-b starts at 2, so pooling both units' rounds into one set would hide its gap.
        self.assertEqual(commit_hygiene.find_round_gaps(repo), [("unit-b", 1)])
        with contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(commit_hygiene.check_unique_review_rounds(repo), 1)

    def test_an_expected_round_that_is_not_recorded_is_reported(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\twording\t-\t1\n"
            "2026-10-16\tunit-a\t2\t-\t-\t0\n",
        )

        self.assertEqual(commit_hygiene.find_unrecorded_expected(repo, ["unit-a:2"]), [])
        self.assertEqual(commit_hygiene.find_unrecorded_expected(repo, ["unit-a:3"]), [("unit-a", 3)])
        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(commit_hygiene.check_unique_review_rounds(repo, ["unit-a:3"]), 1)
        self.assertIn("has no row for round 3", stderr.getvalue())

    def test_a_malformed_expectation_fails_the_cli_cleanly(self):
        repo = make_repo(self)
        self._write_ledger(repo, "2026-10-16\tunit-a\t1\twording\t-\t1\n")

        with self.assertRaises(ValueError):
            commit_hygiene.find_unrecorded_expected(repo, ["unit-a"])

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect", "unit-a", "--repo", str(repo)]), 1
            )
        self.assertIn("expected round must be <unit>:<round>", stderr.getvalue())

    def test_a_non_numeric_round_fails_rather_than_being_skipped(self):
        repo = make_repo(self)
        self._write_ledger(repo, "2026-10-16\tunit-a\tnine\twording\t-\t1\n")

        with self.assertRaises(ValueError):
            commit_hygiene.ledger_rounds(repo)
        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(commit_hygiene.main(["--unique-rounds", "--repo", str(repo)]), 1)
        self.assertIn("non-numeric round 'nine'", stderr.getvalue())

    def test_the_expected_round_is_reachable_through_main(self):
        repo = make_repo(self)
        self._write_ledger(repo, "2026-10-16\tunit-a\t1\twording\t-\t1\n")

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect", "unit-a:1", "--repo", str(repo)]), 0
            )
        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect", "unit-a:2", "--repo", str(repo)]), 1
            )
        self.assertIn("has no row for round 2", stderr.getvalue())

    def test_an_acknowledgement_for_another_pair_does_not_exempt_the_gap(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "# --- Acknowledged gap: unit-b round 2 - unrelated\n"
            "# --- Acknowledged gap: unit-a round 5 - unrelated\n"
            "2026-10-16\tunit-a\t1\twording\t-\t1\n"
            "2026-10-16\tunit-a\t3\t-\t-\t0\n",
        )

        self.assertEqual(commit_hygiene.find_round_gaps(repo), [("unit-a", 2)])
        with contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(commit_hygiene.check_unique_review_rounds(repo), 1)

    def test_expect_without_the_mode_is_an_error_for_every_mode(self):
        repo = make_repo(self)
        self._write_ledger(repo, "2026-10-16\tunit-a\t1\twording\t-\t1\n")

        # The forms are derived from the script's own mode list, so a new mode joins the control rather than being
        # forgotten, and the list is checked against the parser's group so it cannot drift.
        parser = commit_hygiene.build_parser()
        declared = sorted(action.option_strings[0] for action in parser._mutually_exclusive_groups[0]._group_actions)
        self.assertEqual(sorted(commit_hygiene.MODE_FLAGS), declared)

        for flag in commit_hygiene.MODE_FLAGS:
            if flag == "--unique-rounds":
                continue
            stderr = io.StringIO()
            with contextlib.redirect_stderr(stderr):
                self.assertEqual(commit_hygiene.main([flag, "--expect", "unit-a:1", "--repo", str(repo)]), 1)
            self.assertIn("--expect needs --unique-rounds", stderr.getvalue())

        # The bare form, with no mode flag at all.
        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(commit_hygiene.main(["--expect", "unit-a:1", "--repo", str(repo)]), 1)
        self.assertIn("--expect needs --unique-rounds", stderr.getvalue())

    def test_a_clean_closing_round_passes_over_earlier_unclean_rounds(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\twording\t-\t1\n"
            "2026-10-16\tunit-a\t2\tclaim-unreproducible\t-\t1\n"
            "2026-10-16\tunit-a\t3\t-\t-\t0\n",
        )

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect-clean", "unit-a", "--repo", str(repo)]), 0
            )
        self.assertEqual(stderr.getvalue(), "")

    def test_an_unclean_closing_round_is_reported_with_its_classes(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\t-\t-\t0\n"
            "2026-10-16\tunit-a\t2\twording,test-control\t-\t2\n",
        )

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect-clean", "unit-a", "--repo", str(repo)]), 1
            )
        self.assertIn("Unit 'unit-a' is named with --expect-clean", stderr.getvalue())
        self.assertIn("round 2 carries wording,test-control", stderr.getvalue())

    def test_an_unclean_unit_the_caller_does_not_name_is_not_evaluated(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\t-\t-\t0\n"
            "2026-10-16\tunit-b\t1\tclaim-unreproducible\t-\t1\n",
        )

        with contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect-clean", "unit-a", "--repo", str(repo)]), 0
            )

    def test_a_repeated_expectation_fails_when_any_named_unit_is_unclean(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\t-\t-\t0\n"
            "2026-10-16\tunit-b\t1\twording\t-\t1\n",
        )

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(
                    ["--unique-rounds", "--expect-clean", "unit-a", "--expect-clean", "unit-b", "--repo", str(repo)]
                ),
                1,
            )
        self.assertNotIn("Unit 'unit-a'", stderr.getvalue())
        self.assertIn("Unit 'unit-b'", stderr.getvalue())

    def test_a_named_unit_with_no_rows_fails_rather_than_passing_vacuously(self):
        repo = make_repo(self)
        self._write_ledger(repo, "2026-10-16\tunit-a\t1\t-\t-\t0\n")

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect-clean", "unit-missing", "--repo", str(repo)]), 1
            )
        self.assertIn("the ledger records no round for it", stderr.getvalue())

    def test_an_unreadable_or_short_classes_field_fails_rather_than_reading_as_clean(self):
        repo = make_repo(self)
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t1\t-\t-\t0\n"
            "2026-10-16\tunit-a\t2\t\t-\t0\n"
            "2026-10-16\tunit-b\t1\n",
        )

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect-clean", "unit-a", "--repo", str(repo)]), 1
            )
        self.assertIn("round 2 carries no readable classes field", stderr.getvalue())

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect-clean", "unit-b", "--repo", str(repo)]), 1
            )
        self.assertIn("unit-b", stderr.getvalue())

    def test_the_numerically_highest_round_is_the_one_checked(self):
        repo = make_repo(self)
        # Round 10 sorts below round 9 as a string, and its row is not the file's last one.
        self._write_ledger(
            repo,
            "2026-10-16\tunit-a\t10\twording\t-\t1\n"
            "2026-10-16\tunit-a\t9\t-\t-\t0\n",
        )

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(
                commit_hygiene.main(["--unique-rounds", "--expect-clean", "unit-a", "--repo", str(repo)]), 1
            )
        self.assertIn("round 10 carries wording", stderr.getvalue())

    def test_expect_clean_without_the_mode_fails(self):
        repo = make_repo(self)
        self._write_ledger(repo, "2026-10-16\tunit-a\t1\t-\t-\t0\n")

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            self.assertEqual(commit_hygiene.main(["--expect-clean", "unit-a", "--repo", str(repo)]), 1)
        self.assertIn("--expect-clean needs --unique-rounds", stderr.getvalue())

    def test_a_malformed_unit_name_fails(self):
        repo = make_repo(self)
        self._write_ledger(repo, "2026-10-16\tunit-a\t1\t-\t-\t0\n")

        for name in ("", "unit a", " unit-a"):
            stderr = io.StringIO()
            with contextlib.redirect_stderr(stderr):
                self.assertEqual(
                    commit_hygiene.main(["--unique-rounds", "--expect-clean", name, "--repo", str(repo)]), 1
                )
            self.assertIn("must name a non-empty unit without whitespace", stderr.getvalue())

class ReviewReadyTests(unittest.TestCase):
    @staticmethod
    def _commit_all(repo: Path, message: str = "commit") -> None:
        subprocess.run(("git", "add", "-A"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "-m", message), cwd=repo, check=True)

    def _repo(self, ignore: str = None) -> Path:
        repo = make_repo(self)
        (repo / "notes.txt").write_text("one\ntwo\nthree\n", encoding="utf-8")
        if ignore is not None:
            (repo / ".gitignore").write_text(ignore, encoding="utf-8")
        self._commit_all(repo, "base")
        subprocess.run(("git", "checkout", "-q", "-B", "main"), cwd=repo, check=True)
        subprocess.run(("git", "checkout", "-q", "-b", "unit"), cwd=repo, check=True)
        return repo

    def _advance_main(self, repo: Path, content: str = "one\ntwo\nthree\nfour\n") -> None:
        subprocess.run(("git", "checkout", "-q", "main"), cwd=repo, check=True)
        (repo / "notes.txt").write_text(content, encoding="utf-8")
        self._commit_all(repo, "advance")
        subprocess.run(("git", "checkout", "-q", "unit"), cwd=repo, check=True)

    @staticmethod
    def _run(repo: Path, base: str = "main"):
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            status = commit_hygiene.check_review_ready(repo, base)
        return status, out.getvalue(), err.getvalue()

    def test_a_ready_tree_passes_and_reports_the_revisions_it_checked(self):
        repo = self._repo()
        (repo / "notes.txt").write_text("ONE\ntwo\nthree\n", encoding="utf-8")
        subprocess.run(("git", "add", "notes.txt"), cwd=repo, check=True)

        status, out, err = self._run(repo)

        self.assertEqual(0, status, err)
        self.assertIn("Review-ready", out)
        self.assertIn("base main at", out)
        self.assertIn("is contained in HEAD", out)

    def test_a_unit_with_its_own_commits_on_a_current_base_passes(self):
        repo = self._repo()
        (repo / "unit.md").write_text("draft\n", encoding="utf-8")
        self._commit_all(repo, "unit work")

        status, out, err = self._run(repo)

        self.assertEqual(0, status, err)
        self.assertIn("is contained in HEAD", out)

    def test_an_untracked_file_fails_and_is_not_staged(self):
        repo = self._repo()
        (repo / "unit.md").write_text("draft\n", encoding="utf-8")

        status, _, err = self._run(repo)

        self.assertEqual(1, status)
        self.assertIn("unit.md", err)
        untracked = subprocess.run(
            ("git", "ls-files", "--others", "--exclude-standard"), cwd=repo, capture_output=True, text=True
        ).stdout
        self.assertIn("unit.md", untracked, "the checker must report an untracked path, never stage it")

    def test_an_ignored_untracked_path_is_not_reported(self):
        repo = self._repo(ignore="build/\n")
        (repo / "build").mkdir()
        (repo / "build" / "out.txt").write_text("artifact\n", encoding="utf-8")

        status, _, err = self._run(repo)

        self.assertEqual(0, status, err)

    def test_an_untracked_directory_is_reported_by_file(self):
        repo = self._repo()
        (repo / "newdir" / "nested").mkdir(parents=True)
        (repo / "newdir" / "b.txt").write_text("b\n", encoding="utf-8")
        (repo / "newdir" / "nested" / "a.txt").write_text("a\n", encoding="utf-8")

        status, _, err = self._run(repo)

        self.assertIn("newdir/b.txt", err)
        self.assertIn("newdir/nested/a.txt", err)
        self.assertEqual(2, err.count("An untracked path is not in the index"), err)

    def test_a_repository_hiding_untracked_files_still_reports_them(self):
        repo = self._repo()
        subprocess.run(("git", "config", "status.showUntrackedFiles", "no"), cwd=repo, check=True)
        (repo / "unit.md").write_text("draft\n", encoding="utf-8")

        status, _, err = self._run(repo)

        self.assertEqual(1, status)
        self.assertIn("unit.md", err)

    def test_a_path_staged_and_then_edited_fails(self):
        repo = self._repo()
        (repo / "unit.md").write_text("one\n", encoding="utf-8")
        subprocess.run(("git", "add", "unit.md"), cwd=repo, check=True)
        (repo / "unit.md").write_text("two\n", encoding="utf-8")

        status, _, err = self._run(repo)

        self.assertEqual(1, status)
        self.assertIn("unit.md", err)
        self.assertIn("staged and then edited", err)

    def test_a_base_that_advanced_fails(self):
        repo = self._repo()
        self._advance_main(repo)
        (repo / "unit.md").write_text("draft\n", encoding="utf-8")
        subprocess.run(("git", "add", "unit.md"), cwd=repo, check=True)

        status, _, err = self._run(repo)

        self.assertEqual(1, status)
        self.assertIn("carries 1 commit(s) HEAD does not", err)
        self.assertIn("changed on the base: notes.txt", err)

    def test_a_base_that_dropped_a_line_fails(self):
        repo = self._repo()
        self._advance_main(repo, content="one\nthree\n")

        status, _, err = self._run(repo)

        self.assertEqual(1, status)
        self.assertIn("carries 1 commit(s) HEAD does not", err)

    def test_a_refreshed_tree_passes(self):
        repo = self._repo()
        (repo / "unit.md").write_text("draft\n", encoding="utf-8")
        subprocess.run(("git", "add", "unit.md"), cwd=repo, check=True)
        self._advance_main(repo)
        subprocess.run(("git", "merge", "--ff-only", "-q", "main"), cwd=repo, check=True)

        status, _, err = self._run(repo)

        self.assertEqual(0, status, err)

    def test_two_failures_are_reported_together(self):
        repo = self._repo()
        (repo / "unit.md").write_text("one\n", encoding="utf-8")
        subprocess.run(("git", "add", "unit.md"), cwd=repo, check=True)
        (repo / "unit.md").write_text("two\n", encoding="utf-8")
        (repo / "later.md").write_text("draft\n", encoding="utf-8")

        status, _, err = self._run(repo)

        self.assertEqual(2, status)
        self.assertIn("unit.md", err)
        self.assertIn("later.md", err)

    def test_an_unresolvable_base_fails_cleanly(self):
        repo = self._repo()

        status, _, err = self._run(repo, base="no-such-ref")

        self.assertEqual(1, status)
        self.assertIn("no-such-ref", err)
        self.assertIn("does not resolve", err)

    def test_a_base_without_shared_history_fails(self):
        repo = make_repo(self)
        (repo / "notes.txt").write_text("one\n", encoding="utf-8")
        self._commit_all(repo, "base")
        subprocess.run(("git", "checkout", "-q", "-B", "main"), cwd=repo, check=True)
        subprocess.run(("git", "checkout", "-q", "--orphan", "unrelated"), cwd=repo, check=True)
        subprocess.run(("git", "rm", "-q", "-rf", "."), cwd=repo, check=True)
        (repo / "other.txt").write_text("two\n", encoding="utf-8")
        self._commit_all(repo, "unrelated root")

        status, _, err = self._run(repo, base="main")

        self.assertEqual(1, status)
        self.assertIn("share no history", err)

    def test_a_failing_git_invocation_fails_as_not_established(self):
        repo = self._repo()
        relative = subprocess.run(
            ("git", "rev-parse", "--git-path", "index"), cwd=repo, capture_output=True, text=True, check=True
        ).stdout.strip()
        index = Path(relative)
        (index if index.is_absolute() else repo / index).write_text("garbage\n", encoding="utf-8")

        status, _, err = self._run(repo)

        self.assertEqual(1, status)
        self.assertIn("Readiness is not established", err)

    def test_the_mode_is_reachable_through_main(self):
        repo = self._repo()
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = commit_hygiene.main(["--review-ready", "--base", "main", "--repo", str(repo)])
        self.assertEqual(0, code, err.getvalue())
        self.assertIn("Review-ready", out.getvalue())

        (repo / "unit.md").write_text("draft\n", encoding="utf-8")
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = commit_hygiene.main(["--review-ready", "--base", "main", "--repo", str(repo)])
        self.assertEqual(1, code)
        self.assertIn("unit.md", err.getvalue())

    def test_a_fatal_git_error_fails_as_not_established(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)

        status, _, err = self._run(Path(directory.name))

        self.assertEqual(1, status)
        self.assertIn("Readiness is not established", err)

    def test_the_default_base_is_origin_main_and_its_absence_fails(self):
        repo = self._repo()

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--review-ready", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn("origin/main", stderr.getvalue())
        self.assertIn("does not resolve", stderr.getvalue())

    def test_base_without_the_mode_fails_rather_than_being_ignored(self):
        repo = self._repo()

        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.main(["--base", "main", "--repo", str(repo)])

        self.assertEqual(1, code)
        self.assertIn("--base needs --review-ready", stderr.getvalue())


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


MARKER = commit_hygiene.SESSION_FIELDS_MARKER
CREATED = commit_hygiene.SESSION_CREATED
CONTINUED = commit_hygiene.SESSION_CONTINUED
REPLACED = commit_hygiene.SESSION_REPLACED


def ledger_repo(test: unittest.TestCase, lines: list[str]) -> Path:
    """A temporary repository whose review-round ledger carries the given data lines, the boundary included."""
    directory = tempfile.TemporaryDirectory()
    test.addCleanup(directory.cleanup)
    repo = Path(directory.name)
    ledger = repo / "docs" / "review-rounds.tsv"
    ledger.parent.mkdir(parents=True, exist_ok=True)
    ledger.write_text("\n".join([MARKER] + lines) + "\n", encoding="utf-8")
    return repo


def row(
    unit: str,
    round_number: int,
    session: str,
    transition: str,
    reason: str = "-",
) -> str:
    """One ledger row, its round fields filled with placeholders the continuity check does not read."""
    return "\t".join(
        ["2026-10-18", unit, str(round_number), "-", "-", "0", session, transition, reason]
    )


class SessionContinuityTests(unittest.TestCase):
    def violations(self, lines: list[str]) -> list[str]:
        return commit_hygiene.find_session_continuity_violations(ledger_repo(self, lines))

    def test_consecutive_rounds_sharing_one_session_pass(self):
        self.assertEqual(
            [],
            self.violations([row("unit-a", 1, "ses_a", CREATED), row("unit-a", 2, "ses_a", CONTINUED)]),
        )

    def test_an_allowed_replacement_passes_with_its_reason(self):
        self.assertEqual(
            [],
            self.violations(
                [
                    row("unit-a", 1, "ses_a", CREATED),
                    row("unit-a", 2, "ses_b", REPLACED, "expired"),
                ]
            ),
        )

    def test_every_allowed_reason_passes(self):
        for reason in commit_hygiene.SESSION_REPLACEMENT_REASONS:
            with self.subTest(reason=reason):
                self.assertEqual(
                    [],
                    self.violations(
                        [
                            row("unit-a", 1, "ses_a", CREATED),
                            row("unit-a", 2, "ses_b", REPLACED, reason),
                        ]
                    ),
                )

    def test_the_caller_restarted_violation_token_is_accepted_on_a_replacement(self):
        self.assertEqual(
            [],
            self.violations(
                [
                    row("unit-a", 1, "ses_a", CREATED),
                    row("unit-a", 2, "ses_b", REPLACED, "caller-restarted"),
                ]
            ),
        )

    def test_the_caller_restarted_violation_token_is_rejected_on_a_continued_row(self):
        violations = self.violations(
            [
                row("unit-a", 1, "ses_a", CREATED),
                row("unit-a", 2, "ses_a", CONTINUED, "caller-restarted"),
            ]
        )
        self.assertIn("not a replacement", violations[0])

    def test_an_unexplained_session_change_fails(self):
        violations = self.violations(
            [row("unit-a", 1, "ses_a", CREATED), row("unit-a", 2, "ses_b", CONTINUED)]
        )
        self.assertEqual(1, len(violations))
        self.assertIn("unit-a", violations[0])
        self.assertIn("ses_a", violations[0])
        self.assertIn("ses_b", violations[0])

    def test_a_missing_session_identifier_fails(self):
        violations = self.violations([row("unit-a", 1, "-", CREATED)])
        self.assertEqual(1, len(violations))
        self.assertIn("no session identifier", violations[0])

    def test_a_row_carrying_a_tenth_field_fails(self):
        violations = self.violations([row("unit-a", 1, "ses_a", CREATED) + "\textra"])
        self.assertEqual(1, len(violations))
        self.assertIn("10 field(s), not 9", violations[0])

    def test_a_padded_identifier_continues_the_trimmed_one(self):
        self.assertEqual(
            [],
            self.violations(
                [
                    row("unit-a", 1, " ses_a ", CREATED),
                    row("unit-a", 2, "ses_a", CONTINUED),
                ]
            ),
        )

    def test_a_whitespace_only_identifier_fails(self):
        violations = self.violations([row("unit-a", 1, "   ", CREATED)])
        self.assertEqual(1, len(violations))
        self.assertIn("no session identifier", violations[0])

    def test_a_row_below_the_boundary_with_too_few_fields_fails(self):
        violations = self.violations(["\t".join(["2026-10-18", "unit-a", "1", "-", "-", "0"])])
        self.assertEqual(1, len(violations))
        self.assertIn("6 field(s), not 9", violations[0])

    def test_a_replacement_with_an_unlisted_reason_fails(self):
        violations = self.violations(
            [row("unit-a", 1, "ses_a", CREATED), row("unit-a", 2, "ses_b", REPLACED, "because")]
        )
        self.assertEqual(1, len(violations))
        self.assertIn("because", violations[0])

    def test_a_replacement_without_a_reason_fails(self):
        violations = self.violations(
            [row("unit-a", 1, "ses_a", CREATED), row("unit-a", 2, "ses_b", REPLACED)]
        )
        self.assertEqual(1, len(violations))
        self.assertIn("replaces a session", violations[0])

    def test_a_reason_on_a_continued_row_fails(self):
        violations = self.violations(
            [row("unit-a", 1, "ses_a", CREATED), row("unit-a", 2, "ses_a", CONTINUED, "expired")]
        )
        self.assertEqual(1, len(violations))
        self.assertIn("not a replacement", violations[0])

    def test_a_first_row_that_is_not_a_creation_fails(self):
        violations = self.violations([row("unit-a", 1, "ses_a", CONTINUED)])
        self.assertEqual(1, len(violations))
        self.assertIn(CREATED, violations[0])

    def test_a_repeated_identifier_without_a_continued_transition_fails(self):
        violations = self.violations(
            [row("unit-a", 1, "ses_a", CREATED), row("unit-a", 2, "ses_a", REPLACED, "expired")]
        )
        self.assertEqual(1, len(violations))
        self.assertIn("continues session 'ses_a'", violations[0])

    def test_an_unknown_transition_fails(self):
        violations = self.violations(
            [row("unit-a", 1, "ses_a", CREATED), row("unit-a", 2, "ses_a", "restarted")]
        )
        self.assertEqual(1, len(violations))
        self.assertIn("not one of created, continued, replaced", violations[0])

    def test_rows_above_the_boundary_are_exempt(self):
        repo = ledger_repo(self, [row("unit-a", 1, "ses_a", CREATED)])
        ledger = repo / "docs" / "review-rounds.tsv"
        ledger.write_text(
            "2026-01-01\tunit-old\t1\t-\t-\t0\n" + ledger.read_text(encoding="utf-8"), encoding="utf-8"
        )
        self.assertEqual([], commit_hygiene.find_session_continuity_violations(repo))

    def test_a_marker_moved_past_a_wider_row_fails(self):
        repo = ledger_repo(self, [])
        (repo / "docs" / "review-rounds.tsv").write_text(
            row("unit-a", 1, "ses_a", CREATED) + "\n" + MARKER + "\n", encoding="utf-8"
        )
        violations = commit_hygiene.find_session_continuity_violations(repo)
        self.assertEqual(1, len(violations))
        self.assertIn("the boundary was moved past it", violations[0])

    def test_a_marker_moved_past_a_narrower_row_fails(self):
        repo = ledger_repo(self, [])
        (repo / "docs" / "review-rounds.tsv").write_text(
            "\t".join(["2026-01-01", "unit-old", "1", "-", "-"]) + "\n" + MARKER + "\n", encoding="utf-8"
        )
        violations = commit_hygiene.find_session_continuity_violations(repo)
        self.assertEqual(1, len(violations))
        self.assertIn("not the 6 it carried before", violations[0])

    def test_a_missing_boundary_comment_fails(self):
        repo = ledger_repo(self, [])
        (repo / "docs" / "review-rounds.tsv").write_text(
            row("unit-a", 1, "ses_a", CREATED) + "\n", encoding="utf-8"
        )
        violations = commit_hygiene.find_session_continuity_violations(repo)
        self.assertEqual(1, len(violations))
        self.assertIn("boundary comment", violations[0])

    def test_distinct_units_may_start_with_distinct_sessions(self):
        self.assertEqual(
            [],
            self.violations([row("unit-a", 1, "ses_a", CREATED), row("unit-b", 1, "ses_b", CREATED)]),
        )

    def test_the_mode_reports_and_exits_non_zero_on_a_violation(self):
        repo = ledger_repo(self, [row("unit-a", 1, "ses_a", CONTINUED)])
        with contextlib.redirect_stderr(io.StringIO()) as stderr:
            code = commit_hygiene.main(["--session-continuity", "--repo", str(repo)])
        self.assertEqual(1, code)
        self.assertIn("unit-a", stderr.getvalue())

    def test_the_mode_passes_a_continuing_ledger(self):
        repo = ledger_repo(self, [row("unit-a", 1, "ses_a", CREATED), row("unit-a", 2, "ses_a", CONTINUED)])
        with contextlib.redirect_stderr(io.StringIO()) as stderr:
            code = commit_hygiene.main(["--session-continuity", "--repo", str(repo)])
        self.assertEqual(0, code)
        self.assertEqual("", stderr.getvalue())


class CommitAssignmentTests(unittest.TestCase):
    @staticmethod
    def _write(repo: Path, path: str) -> None:
        target = repo / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text("# Proposal\n", encoding="utf-8")

    @staticmethod
    def _stage(repo: Path, *paths: str) -> None:
        subprocess.run(("git", "add", *paths), cwd=repo, check=True)

    @staticmethod
    def _commit_all(repo: Path) -> None:
        subprocess.run(("git", "add", "--all"), cwd=repo, check=True)
        subprocess.run(("git", "commit", "-q", "--allow-empty", "-m", "base"), cwd=repo, check=True)

    @staticmethod
    def _switch(repo: Path, branch: str) -> None:
        subprocess.run(("git", "switch", "-q", "-c", branch), cwd=repo, check=True)

    @staticmethod
    def _run(repo: Path) -> tuple[int, str]:
        stderr = io.StringIO()
        with contextlib.redirect_stderr(stderr):
            code = commit_hygiene.check_staged(repo, ["true"])
        return code, stderr.getvalue()

    def test_the_assigned_changes_own_paths_are_accepted(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/proposal.md")
        self._stage(repo, "openspec/changes/demo")

        self.assertEqual(0, self._run(repo)[0])

    def test_another_changes_directory_is_refused_even_as_the_only_change_dir(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/intruder/proposal.md")
        self._stage(repo, "openspec/changes/intruder")

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("belongs to change 'intruder'", stderr)
        self.assertIn("assigned change 'demo'", stderr)

    def test_the_assigned_changes_archive_move_is_accepted(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/proposal.md")
        self._commit_all(repo)
        subprocess.run(("git", "rm", "-q", "-r", "openspec/changes/demo"), cwd=repo, check=True)
        self._write(repo, "openspec/changes/archive/2026-01-01-demo/proposal.md")
        self._stage(repo, "openspec/changes")

        self.assertEqual(0, self._run(repo)[0])

    def test_another_changes_archive_move_is_refused(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/archive/2026-01-01-other/proposal.md")
        self._stage(repo, "openspec/changes/archive")

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("belongs to change 'other'", stderr)

    def test_the_assigned_changes_own_deletion_is_accepted(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/design.md")
        self._commit_all(repo)
        subprocess.run(("git", "rm", "-q", "openspec/changes/demo/design.md"), cwd=repo, check=True)

        self.assertEqual(0, self._run(repo)[0])

    def test_another_changes_deletion_is_refused(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/other/proposal.md")
        self._commit_all(repo)
        subprocess.run(("git", "rm", "-q", "-r", "openspec/changes/other"), cwd=repo, check=True)

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("belongs to change 'other'", stderr)

    def test_a_missing_assignment_refuses_and_recording_it_resolves_the_same_set(self):
        repo = make_repo(self)
        self._commit_all(repo)
        claim_path(repo).unlink()
        self._write(repo, "docs/note.md")
        self._stage(repo, "docs")

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("No commit assignment", stderr)

        write_claim(repo, "demo")
        self.assertEqual(0, self._run(repo)[0])

    def test_a_contradictory_claim_and_branch_are_refused(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/proposal.md")
        self._commit_all(repo)
        self._switch(repo, "demo")
        write_claim(repo, "other")

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("contradictory", stderr)

    def test_a_none_claim_refuses_any_change_path(self):
        repo = make_repo(self)
        self._commit_all(repo)
        write_claim(repo, "none")
        self._write(repo, "openspec/changes/demo/proposal.md")
        self._stage(repo, "openspec/changes/demo")

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("declares none", stderr)

    def test_a_none_claim_on_a_change_named_branch_leaves_the_branch_assignment(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/proposal.md")
        self._commit_all(repo)
        self._switch(repo, "demo")
        write_claim(repo, "none")
        self._write(repo, "openspec/changes/demo/tasks.md")
        self._stage(repo, "openspec/changes/demo")

        self.assertEqual(0, self._run(repo)[0])

    def test_a_staged_set_outside_change_dirs_passes_under_none(self):
        repo = make_repo(self)
        self._commit_all(repo)
        write_claim(repo, "none")
        self._write(repo, "docs/note.md")
        self._stage(repo, "docs")

        self.assertEqual(0, self._run(repo)[0])

    def test_a_claim_that_is_empty_or_holds_two_lines_is_refused(self):
        repo = make_repo(self)
        self._commit_all(repo)
        self._write(repo, "docs/note.md")
        self._stage(repo, "docs")

        for content in ("", "demo\nother"):
            claim_path(repo).write_text(content, encoding="utf-8")
            code, stderr = self._run(repo)
            self.assertEqual(1, code)
            self.assertIn("must hold exactly one line", stderr)

    def test_the_contradiction_message_names_the_remedy(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/proposal.md")
        self._commit_all(repo)
        self._switch(repo, "demo")
        write_claim(repo, "other")

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("Correct the claim", stderr)

    def test_a_file_directly_under_the_changes_directory_names_no_change(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/README.md")
        self._stage(repo, "openspec/changes/README.md")

        self.assertEqual(0, self._run(repo)[0])

    def test_a_second_foreign_rename_is_refused_after_a_legitimate_one(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/proposal.md")
        self._write(repo, "openspec/changes/demo/keep.md")
        self._write(repo, "openspec/changes/intruder/theirs.md")
        self._commit_all(repo)
        subprocess.run(
            ("git", "mv", "openspec/changes/demo/proposal.md", "openspec/changes/demo/renamed.md"),
            cwd=repo,
            check=True,
        )
        subprocess.run(
            ("git", "mv", "openspec/changes/intruder/theirs.md", "openspec/changes/demo/moved.md"),
            cwd=repo,
            check=True,
        )

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("belongs to change 'intruder'", stderr)

    def test_a_foreign_path_renamed_out_of_the_changes_directory_is_refused(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/intruder/theirs.md")
        self._commit_all(repo)
        (repo / "docs").mkdir(exist_ok=True)
        subprocess.run(
            ("git", "mv", "openspec/changes/intruder/theirs.md", "docs/moved.md"), cwd=repo, check=True
        )

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("belongs to change 'intruder'", stderr)

    def test_a_foreign_path_renamed_into_the_assigned_change_is_refused(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/keep.md")
        self._write(repo, "openspec/changes/intruder/theirs.md")
        self._commit_all(repo)
        subprocess.run(
            ("git", "mv", "openspec/changes/intruder/theirs.md", "openspec/changes/demo/moved.md"),
            cwd=repo,
            check=True,
        )

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("belongs to change 'intruder'", stderr)

    def test_a_none_claim_refuses_a_file_directly_under_the_changes_directory(self):
        repo = make_repo(self)
        self._commit_all(repo)
        write_claim(repo, "none")
        self._write(repo, "openspec/changes/README.md")
        self._stage(repo, "openspec/changes/README.md")

        code, stderr = self._run(repo)
        self.assertEqual(1, code)
        self.assertIn("declares none", stderr)

    def test_a_change_named_branch_is_assigned_despite_a_malformed_claim(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/proposal.md")
        self._commit_all(repo)
        self._switch(repo, "demo")
        claim_path(repo).write_text("demo\nother", encoding="utf-8")
        self._write(repo, "openspec/changes/demo/tasks.md")
        self._stage(repo, "openspec/changes/demo")

        self.assertEqual(0, self._run(repo)[0])

    def test_the_assigned_changes_archive_move_is_accepted_when_git_reports_an_addition(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/demo/design.md")
        self._commit_all(repo)
        subprocess.run(("git", "rm", "-q", "openspec/changes/demo/design.md"), cwd=repo, check=True)
        archived = repo / "openspec/changes/archive/2026-01-01-demo/design.md"
        archived.parent.mkdir(parents=True, exist_ok=True)
        archived.write_text("# Design\n\nRewritten for the archive.\n", encoding="utf-8")
        self._stage(repo, "openspec/changes")

        status = subprocess.run(
            ("git", "diff", "--cached", "--name-status", "-z"), cwd=repo, capture_output=True, text=True, check=True
        ).stdout
        self.assertIn("A\0openspec/changes/archive/2026-01-01-demo/design.md", status)
        self.assertEqual(0, self._run(repo)[0])

    def test_a_branch_named_after_an_archived_change_needs_no_claim(self):
        repo = make_repo(self)
        self._write(repo, "openspec/changes/archive/2026-01-01-demo/proposal.md")
        self._commit_all(repo)
        self._switch(repo, "demo")
        claim_path(repo).unlink()
        self._write(repo, "openspec/changes/archive/2026-01-01-demo/tasks.md")
        self._stage(repo, "openspec/changes/archive")

        self.assertEqual(0, self._run(repo)[0])


if __name__ == "__main__":
    unittest.main()
