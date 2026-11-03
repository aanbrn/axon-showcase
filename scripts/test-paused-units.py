#!/usr/bin/env python3
"""Tests for the paused-unit resolver (`scripts/paused-units.py`). Stdlib only.

Run with `python3 scripts/test-paused-units.py`. Each case builds an isolated repository, so the developer's own
worktrees never enter a fixture.
"""

import importlib.util
import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

RESOLVER_PATH = Path(__file__).resolve().parent / "paused-units.py"
_spec = importlib.util.spec_from_file_location("paused_units", RESOLVER_PATH)
paused_units = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(paused_units)


def run(repo: Path, *arguments: str) -> str:
    result = subprocess.run(("git",) + arguments, cwd=repo, capture_output=True, text=True, check=True)
    return result.stdout


def record_text(unit: str, tree: Path, branch: str, state: str | None) -> str:
    text = "# Unit: {0}\n\nWorkspace:\n`unit={0}; path={1}; branch={2}; writer=ses_abc123`\n".format(
        unit, tree, branch
    )
    if state is not None:
        text += "\nState: {0}\n".format(state)
    return text


def write_record(tree: Path, unit: str, state: str | None, branch: str | None = None) -> Path:
    directory = tree / "docs" / "units" / unit
    directory.mkdir(parents=True, exist_ok=True)
    path = directory / "unit.md"
    path.write_text(record_text(unit, tree, branch or current_branch(tree), state), encoding="utf-8")
    return path


def current_branch(tree: Path) -> str:
    return run(tree, "symbolic-ref", "--short", "HEAD").strip()


def write_claim(tree: Path, value: str) -> None:
    path = Path(run(tree, "rev-parse", "--git-path", "axon-showcase-change").strip())
    (path if path.is_absolute() else tree / path).write_text(value + "\n", encoding="utf-8")


def make_repo(test: unittest.TestCase) -> Path:
    directory = tempfile.TemporaryDirectory()
    test.addCleanup(directory.cleanup)
    repo = Path(directory.name).resolve()
    run(repo, "init", "-q", "-b", "main")
    run(repo, "config", "user.email", "test@example.com")
    run(repo, "config", "user.name", "Test")
    (repo / "README.md").write_text("fixture\n", encoding="utf-8")
    run(repo, "add", "README.md")
    run(repo, "commit", "-q", "-m", "fixture")
    return repo


def add_worktree(test: unittest.TestCase, repo: Path, branch: str) -> Path:
    directory = tempfile.TemporaryDirectory()
    test.addCleanup(directory.cleanup)
    tree = Path(directory.name).resolve() / ("tree-" + branch.replace("/", "-"))
    run(repo, "worktree", "add", "-q", "-b", branch, str(tree), "main")
    return tree


class ResolverTests(unittest.TestCase):
    def test_one_paused_unit_is_a_candidate(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        write_record(tree, "alpha", "paused")
        document = paused_units.resolve(repo)
        self.assertTrue(document["complete"])
        self.assertEqual(["alpha"], [candidate["unit"] for candidate in document["candidates"]])

    def test_several_paused_units_are_all_reported(self) -> None:
        repo = make_repo(self)
        for name in ("alpha", "beta"):
            write_record(add_worktree(self, repo, name), name, "paused")
        document = paused_units.resolve(repo)
        self.assertTrue(document["complete"])
        self.assertEqual(["alpha", "beta"], [candidate["unit"] for candidate in document["candidates"]])

    def test_no_paused_unit_is_a_complete_empty_result(self) -> None:
        repo = make_repo(self)
        write_record(add_worktree(self, repo, "alpha"), "alpha", "active")
        document = paused_units.resolve(repo)
        self.assertTrue(document["complete"])
        self.assertEqual([], document["candidates"])

    def test_a_unit_without_a_change_directory_is_a_candidate(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "docs-only")
        write_record(tree, "docs-only", "paused")
        self.assertFalse((tree / "openspec" / "changes" / "docs-only").exists())
        document = paused_units.resolve(repo)
        self.assertEqual(["docs-only"], [candidate["unit"] for candidate in document["candidates"]])

    def test_a_record_without_a_state_is_unknown(self) -> None:
        repo = make_repo(self)
        write_record(add_worktree(self, repo, "alpha"), "alpha", None)
        document = paused_units.resolve(repo)
        self.assertTrue(document["complete"])
        self.assertEqual([], document["candidates"])
        self.assertEqual(["alpha"], [entry["unit"] for entry in document["unknown"]])

    def test_a_legacy_tasks_record_is_unknown(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        change = tree / "openspec" / "changes" / "alpha"
        change.mkdir(parents=True)
        (change / "tasks.md").write_text(
            "# Tasks\n\nWorkspace:\n`unit=alpha; path={0}; branch=alpha; writer=ses_abc123`\n".format(tree),
            encoding="utf-8",
        )
        document = paused_units.resolve(repo)
        self.assertTrue(document["complete"])
        self.assertEqual([], document["candidates"])
        self.assertEqual(["alpha"], [entry["unit"] for entry in document["unknown"]])

    def test_a_record_naming_another_unit_is_an_error(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        directory = tree / "docs" / "units" / "alpha"
        directory.mkdir(parents=True)
        (directory / "unit.md").write_text(record_text("beta", tree, "alpha", "paused"), encoding="utf-8")
        document = paused_units.resolve(repo)
        self.assertFalse(document["complete"])
        self.assertEqual([], document["candidates"])

    def test_a_contradicting_claim_is_an_error(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        write_record(tree, "alpha", "paused")
        claim = Path(run(tree, "rev-parse", "--git-path", "axon-showcase-change").strip())
        (claim if claim.is_absolute() else tree / claim).write_text("beta\n", encoding="utf-8")
        document = paused_units.resolve(repo)
        self.assertFalse(document["complete"])
        self.assertEqual([], document["candidates"])

    def test_two_records_for_one_tree_are_an_error(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        write_record(tree, "alpha", "paused")
        write_record(tree, "beta", "paused")
        document = paused_units.resolve(repo)
        self.assertFalse(document["complete"])

    def test_an_archived_but_unfinished_unit_is_still_a_candidate(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        archived = tree / "openspec" / "changes" / "archive" / "2026-11-03-alpha"
        archived.mkdir(parents=True)
        (archived / "tasks.md").write_text("# Tasks\n", encoding="utf-8")
        write_record(tree, "alpha", "paused")
        document = paused_units.resolve(repo)
        self.assertEqual(["alpha"], [candidate["unit"] for candidate in document["candidates"]])

    def test_a_record_naming_another_branch_is_not_assigned(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        write_record(tree, "alpha", "paused", branch="other-branch")
        document = paused_units.resolve(repo)
        self.assertTrue(document["complete"])
        self.assertEqual([], document["candidates"])
        self.assertEqual([], document["assignments"])

    def test_a_record_contradicting_the_claim_is_an_error(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        write_claim(tree, "alpha")
        write_record(tree, "alpha", "paused", branch="other-branch")
        document = paused_units.resolve(repo)
        self.assertFalse(document["complete"])

    def test_a_continued_branch_resolves_through_its_record(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "opencode/123")
        write_claim(tree, "none")
        write_record(tree, "alpha", "paused", branch="opencode/123")
        document = paused_units.resolve(repo)
        self.assertTrue(document["complete"])
        self.assertEqual(["alpha"], [candidate["unit"] for candidate in document["candidates"]])

    def test_a_registered_tree_whose_directory_is_gone_is_an_error(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        write_record(tree, "alpha", "paused")
        shutil.rmtree(tree)
        document = paused_units.resolve(repo)
        self.assertFalse(document["complete"])
        self.assertEqual([], document["candidates"])

    def test_one_paused_unit_beside_an_unresolved_assignment_is_not_selectable(self) -> None:
        repo = make_repo(self)
        write_record(add_worktree(self, repo, "alpha"), "alpha", "paused")
        write_record(add_worktree(self, repo, "beta"), "beta", None)
        document = paused_units.resolve(repo)
        self.assertTrue(document["complete"])
        self.assertFalse(document["selectable"])
        self.assertEqual(["alpha"], [candidate["unit"] for candidate in document["candidates"]])

    def test_a_named_unit_resolves_through_its_assignment(self) -> None:
        repo = make_repo(self)
        write_record(add_worktree(self, repo, "alpha"), "alpha", "active")
        result = subprocess.run(
            ("python3", str(RESOLVER_PATH), "--repo", str(repo), "--unit", "alpha"), capture_output=True, text=True
        )
        document = json.loads(result.stdout)
        self.assertEqual(0, result.returncode)
        self.assertEqual(["alpha"], [entry["unit"] for entry in document["assignments"]])
        self.assertTrue(document["selectable"])
        self.assertEqual([], document["candidates"])

    def test_an_unknown_name_fails(self) -> None:
        repo = make_repo(self)
        result = subprocess.run(
            ("python3", str(RESOLVER_PATH), "--repo", str(repo), "--unit", "missing"), capture_output=True, text=True
        )
        self.assertEqual(1, result.returncode)
        self.assertIn("no registered tree assigns", json.loads(result.stdout)["errors"][0])

    def test_a_name_assigned_by_two_trees_fails(self) -> None:
        repo = make_repo(self)
        first = add_worktree(self, repo, "alpha-one")
        second = add_worktree(self, repo, "alpha-two")
        for tree in (first, second):
            write_claim(tree, "alpha")
            write_record(tree, "alpha", "paused", branch=current_branch(tree))
        result = subprocess.run(
            ("python3", str(RESOLVER_PATH), "--repo", str(repo), "--unit", "alpha"), capture_output=True, text=True
        )
        self.assertEqual(1, result.returncode)
        errors = json.loads(result.stdout)["errors"]
        self.assertEqual(1, len(errors))
        self.assertIn("is assigned by 2 trees", errors[0])

    def test_a_closed_record_with_no_live_assignment_fails(self) -> None:
        repo = make_repo(self)
        write_record(repo, "alpha", "closed", branch="alpha")
        result = subprocess.run(
            ("python3", str(RESOLVER_PATH), "--repo", str(repo), "--unit", "alpha"), capture_output=True, text=True
        )
        self.assertEqual(1, result.returncode)
        self.assertIn("no registered tree assigns", json.loads(result.stdout)["errors"][0])

    def test_an_unreadable_tree_beside_a_good_candidate_keeps_the_candidate_but_is_incomplete(self) -> None:
        repo = make_repo(self)
        good = add_worktree(self, repo, "alpha")
        gone = add_worktree(self, repo, "beta")
        write_record(good, "alpha", "paused")
        write_record(gone, "beta", "paused")
        shutil.rmtree(gone)
        document = paused_units.resolve(repo)
        self.assertFalse(document["complete"])
        self.assertFalse(document["selectable"])
        self.assertEqual(["alpha"], [candidate["unit"] for candidate in document["candidates"]])

    def test_a_missing_record_for_an_assigned_unit_is_an_error(self) -> None:
        repo = make_repo(self)
        write_claim(add_worktree(self, repo, "alpha"), "alpha")
        document = paused_units.resolve(repo)
        self.assertFalse(document["complete"])
        self.assertIn("is missing", document["errors"][0])

    def test_a_legacy_unit_resolves_by_name_for_its_migration(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        change = tree / "openspec" / "changes" / "alpha"
        change.mkdir(parents=True)
        (change / "tasks.md").write_text(
            "# Tasks\n\nWorkspace:\n`unit=alpha; path={0}; branch=alpha; writer=ses_abc123`\n".format(tree),
            encoding="utf-8",
        )
        result = subprocess.run(
            ("python3", str(RESOLVER_PATH), "--repo", str(repo), "--unit", "alpha"), capture_output=True, text=True
        )
        document = json.loads(result.stdout)
        self.assertEqual(0, result.returncode)
        self.assertEqual(["alpha"], [entry["unit"] for entry in document["assignments"]])
        self.assertTrue(document["assignments"][0]["legacy"])
        self.assertTrue(document["selectable"])
        self.assertEqual([], document["candidates"])

    def test_a_blank_claim_file_is_no_claim(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        write_record(tree, "alpha", "paused")
        write_claim(tree, "")
        document = paused_units.resolve(repo)
        self.assertTrue(document["complete"])
        self.assertEqual(["alpha"], [candidate["unit"] for candidate in document["candidates"]])

    def test_two_legacy_assignments_of_one_name_are_a_conflict(self) -> None:
        repo = make_repo(self)
        for name in ("alpha-one", "alpha-two"):
            tree = add_worktree(self, repo, name)
            write_claim(tree, "alpha")
            change = tree / "openspec" / "changes" / "alpha"
            change.mkdir(parents=True)
            (change / "tasks.md").write_text(
                "# Tasks\n\nWorkspace:\n`unit=alpha; path={0}; branch={1}; writer=ses_abc123`\n".format(tree, name),
                encoding="utf-8",
            )
        result = subprocess.run(
            ("python3", str(RESOLVER_PATH), "--repo", str(repo), "--unit", "alpha"), capture_output=True, text=True
        )
        self.assertEqual(1, result.returncode)
        errors = json.loads(result.stdout)["errors"]
        self.assertEqual(1, len(errors))
        self.assertIn("is assigned by 2 trees", errors[0])

    def test_a_named_unit_beside_an_unreadable_tree_is_not_selectable(self) -> None:
        repo = make_repo(self)
        write_record(add_worktree(self, repo, "alpha"), "alpha", "active")
        gone = add_worktree(self, repo, "beta")
        write_record(gone, "beta", "paused")
        shutil.rmtree(gone)
        result = subprocess.run(
            ("python3", str(RESOLVER_PATH), "--repo", str(repo), "--unit", "alpha"), capture_output=True, text=True
        )
        document = json.loads(result.stdout)
        self.assertEqual(1, result.returncode)
        self.assertFalse(document["complete"])
        self.assertFalse(document["selectable"])
        self.assertEqual(["alpha"], [entry["unit"] for entry in document["assignments"]])

    def test_the_cli_exits_zero_on_a_complete_enumeration(self) -> None:
        repo = make_repo(self)
        write_record(add_worktree(self, repo, "alpha"), "alpha", "paused")
        result = subprocess.run(
            ("python3", str(RESOLVER_PATH), "--repo", str(repo)), capture_output=True, text=True
        )
        self.assertEqual(0, result.returncode)
        self.assertEqual(["alpha"], [c["unit"] for c in json.loads(result.stdout)["candidates"]])

    def test_the_cli_exits_one_on_an_incomplete_enumeration(self) -> None:
        repo = make_repo(self)
        tree = add_worktree(self, repo, "alpha")
        directory = tree / "docs" / "units" / "alpha"
        directory.mkdir(parents=True)
        (directory / "unit.md").write_text(record_text("beta", tree, "alpha", "paused"), encoding="utf-8")
        result = subprocess.run(
            ("python3", str(RESOLVER_PATH), "--repo", str(repo)), capture_output=True, text=True
        )
        self.assertEqual(1, result.returncode)
        self.assertFalse(json.loads(result.stdout)["complete"])


if __name__ == "__main__":
    unittest.main()
