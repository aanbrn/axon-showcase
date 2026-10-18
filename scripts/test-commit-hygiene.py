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


if __name__ == "__main__":
    unittest.main()
