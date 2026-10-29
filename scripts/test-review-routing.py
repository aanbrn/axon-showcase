#!/usr/bin/env python3
"""Tests for the deep-review routing floor (`scripts/review-routing.py`). Stdlib only.

Run with `python3 scripts/test-review-routing.py`. The tool's kebab-case filename is not an importable identifier, so it
is loaded by path, the way `scripts/test-claim-scan.py` loads the claim scanner.

Every arm of every signal class carries its own case, so a rule that stops detecting its signal fails here rather than
reading as a floor that found nothing.
"""

import contextlib
import importlib.util
import unittest.mock as mock
import io
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
TOOL_PATH = Path(__file__).resolve().parent / "review-routing.py"
_spec = importlib.util.spec_from_file_location("review_routing", TOOL_PATH)
review_routing = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(review_routing)

CHANGE = "a-change"
REQUIRED = review_routing.REQUIRED
RECOMMENDED = review_routing.RECOMMENDED
NOT_NEEDED = review_routing.NOT_NEEDED


def kinds(*names):
    """Block-kind fixtures, as the gatherer yields them."""
    return [(name, "openspec/changes/{0}/specs/capability/spec.md".format(CHANGE)) for name in names]


def rows(*units_and_repeats):
    """Ledger-row fixtures, as tab-separated field lists."""
    return [
        ["2026-01-01", unit, str(index), "wording", repeated, "1"]
        for index, (unit, repeated) in enumerate(units_and_repeats, 1)
    ]


class PathSignalTests(unittest.TestCase):
    def test_a_public_contract_path_is_required(self):
        for path in ("showcase-command-api/src/main/java/x.java", "showcase-query-proto/proto/x.proto"):
            self.assertTrue(review_routing.path_signals([path]), path)

    def test_a_domain_path_is_required(self):
        for path in (
            "showcase-command-service/src/main/java/showcase/command/ShowcaseAggregate.java",
            "showcase-projection-service/src/main/java/showcase/projection/ShowcaseProjector.java",
            "showcase-query-service/src/main/java/showcase/query/ShowcaseQueryHandler.java",
        ):
            self.assertEqual(review_routing.path_signals([path])[0][0], "domain-surface")

    def test_a_persistence_path_is_required(self):
        for path in ("db/migration/V1__init.sql", "showcase-command-service/src/main/resources/schema.sql"):
            self.assertEqual(review_routing.path_signals([path])[0][0], "persistence")

    def test_an_infrastructure_or_deployment_path_is_required(self):
        for path in (
            ".github/workflows/ci.yml",
            "helm/chart/src/main/helm/values.yaml",
            "config/checkstyle/checkstyle.xml",
            "docker-compose.yml",
            "showcase-web-ui/Dockerfile",
        ):
            self.assertEqual(review_routing.path_signals([path])[0][0], "infrastructure-or-deployment")

    def test_an_unrelated_path_carries_no_signal(self):
        self.assertEqual(review_routing.path_signals(["README.md", "docs/ideas.md"]), [])


class DeclarationSignalTests(unittest.TestCase):
    def test_a_security_chain_declaration_in_source_is_required(self):
        for token in ("SecurityWebFilterChain securityFilterChain(", "SecurityFilterChain filterChain("):
            files = [("showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiApplication.java", token)]
            self.assertEqual(review_routing.declaration_signals(files)[0][0], "security-declaration")

    def test_prose_quoting_the_token_is_not_a_declaration(self):
        for path in ("openspec/changes/{0}/design.md".format(CHANGE), "README.md"):
            self.assertEqual(review_routing.declaration_signals([(path, "SecurityWebFilterChain")]), [], path)

    def test_a_plain_source_change_carries_no_declaration_signal(self):
        files = [("showcase/command/ShowcaseAggregate.java", "val status = ShowcaseStatus.SCHEDULED")]
        self.assertEqual(review_routing.declaration_signals(files), [])


class BlockKindTests(unittest.TestCase):
    def test_each_requirement_block_kind_is_required(self):
        for kind in ("ADDED", "REMOVED", "RENAMED"):
            signals = review_routing.block_signals(kinds(kind))
            self.assertEqual(signals, [("requirement-block-" + kind.lower(), kinds(kind)[0][1])])

    def test_a_modified_only_delta_carries_no_block_signal(self):
        self.assertEqual(review_routing.block_signals([]), [])


class LedgerSignalTests(unittest.TestCase):
    def test_two_repeated_rows_recommend_a_deep_review(self):
        signal = review_routing.ledger_signal(rows((CHANGE, "wording"), (CHANGE, "coherence")), CHANGE)
        self.assertEqual(signal[0][0], "repeated-class")

    def test_one_repeated_row_is_not_enough(self):
        self.assertEqual(review_routing.ledger_signal(rows((CHANGE, "wording")), CHANGE), [])

    def test_rows_without_a_repeat_carry_no_signal(self):
        self.assertEqual(review_routing.ledger_signal(rows((CHANGE, "-"), (CHANGE, "-")), CHANGE), [])

    def test_another_units_rows_are_ignored(self):
        self.assertEqual(review_routing.ledger_signal(rows(("other", "wording"), ("other", "coherence")), CHANGE), [])

    def test_no_unit_identity_is_a_failure_not_a_silent_floor(self):
        with self.assertRaises(ValueError):
            review_routing.ledger_signal(rows((CHANGE, "wording")), None)

    def test_a_malformed_row_is_a_failure_not_a_silent_floor(self):
        with self.assertRaises(ValueError):
            review_routing.ledger_signal([["2026-01-01", CHANGE]], CHANGE)


LEDGER = review_routing.LEDGER_DEFAULT


class RecordOnlyTests(unittest.TestCase):
    """The record-only predicate: the Markdown records plus the ledger the invocation reads, and its boundaries."""

    def test_a_record_set_with_the_ledger_is_record_only(self):
        self.assertTrue(
            review_routing.record_only(["docs/process-defects/a.md", LEDGER], LEDGER)
        )

    def test_an_empty_set_is_not_record_only(self):
        self.assertFalse(review_routing.record_only([], LEDGER))

    def test_a_supporting_json_beside_a_record_keeps_normal_routing(self):
        self.assertFalse(
            review_routing.record_only(["docs/process-defects/a.md", "docs/process-defects/data.json"], LEDGER)
        )

    def test_a_markdown_file_outside_the_record_directories_keeps_normal_routing(self):
        self.assertFalse(review_routing.record_only(["docs/ideas.md"], LEDGER))

    def test_a_code_path_renamed_from_a_record_directory_keeps_normal_routing(self):
        both = ["docs/process-defects/a.md", "src/Record.md"]
        self.assertFalse(review_routing.record_only(both, LEDGER))

    def test_a_record_renamed_between_record_directories_is_record_only(self):
        self.assertTrue(
            review_routing.record_only(["docs/process-defects/a.md", "docs/retrospectives/a.md"], LEDGER)
        )

    def test_a_load_test_record_is_record_only(self):
        self.assertTrue(review_routing.record_only(["docs/load-tests/2026-10-01.md"], LEDGER))

    def test_the_ledger_recognised_is_the_one_read(self):
        self.assertTrue(review_routing.record_only(["ledgers/rounds.tsv"], "ledgers/rounds.tsv"))
        self.assertFalse(review_routing.record_only(["ledgers/rounds.tsv"], LEDGER))


class ChangedPathTests(unittest.TestCase):
    """The changed-path parser over `git diff --name-status -z` records, independent of any repository."""

    def test_a_rename_contributes_both_sides(self):
        with mock.patch.object(review_routing, "_git", return_value="R100\0old.md\0new.md"):
            self.assertEqual(review_routing._changed_paths(Path("."), "HEAD"), ["old.md", "new.md"])

    def test_a_copy_record_contributes_both_sides(self):
        with mock.patch.object(
            review_routing,
            "_git",
            return_value="C100\0src/note.md\0docs/process-defects/note.md",
        ):
            self.assertEqual(
                review_routing._changed_paths(Path("."), "HEAD"),
                ["src/note.md", "docs/process-defects/note.md"],
            )

    def test_a_truncated_status_is_a_failure(self):
        with mock.patch.object(review_routing, "_git", return_value="M"):
            with self.assertRaises(ValueError):
                review_routing._changed_paths(Path("."), "HEAD")

    def test_an_empty_path_field_is_a_failure(self):
        with mock.patch.object(review_routing, "_git", return_value="M\0"):
            with self.assertRaises(ValueError):
                review_routing._changed_paths(Path("."), "HEAD")

    def test_a_rename_without_a_destination_is_a_failure(self):
        with mock.patch.object(review_routing, "_git", return_value="R100\0old.md\0"):
            with self.assertRaises(ValueError):
                review_routing._changed_paths(Path("."), "HEAD")

    def test_a_truncated_rename_is_a_failure(self):
        with mock.patch.object(review_routing, "_git", return_value="R100\0old.md"):
            with self.assertRaises(ValueError):
                review_routing._changed_paths(Path("."), "HEAD")


class FloorTests(unittest.TestCase):
    def test_a_docs_only_change_is_not_needed(self):
        self.assertEqual(
            review_routing.floor(["README.md", "docs/ideas.md"], [("README.md", "prose")], [], [], CHANGE),
            (NOT_NEEDED, []),
        )

    def test_a_required_signal_outranks_a_repeated_class(self):
        outcome, signals = review_routing.floor(
            ["helm/values/x.yaml"], [], [], rows((CHANGE, "wording"), (CHANGE, "wording")), CHANGE
        )
        self.assertEqual(outcome, REQUIRED)
        self.assertEqual(signals[0][0], "infrastructure-or-deployment")

    def test_a_repeated_class_without_a_required_signal_is_recommended(self):
        outcome, _ = review_routing.floor(
            ["README.md"], [("README.md", "prose")], [], rows((CHANGE, "wording"), (CHANGE, "wording")), CHANGE
        )
        self.assertEqual(outcome, RECOMMENDED)

    def test_a_record_only_diff_with_repeats_is_not_needed_and_reports_the_exemption(self):
        outcome, signals = review_routing.floor(
            ["docs/process-defects/a.md", LEDGER],
            [],
            [],
            rows((CHANGE, "wording"), (CHANGE, "wording")),
            CHANGE,
        )
        self.assertEqual(outcome, NOT_NEEDED)
        self.assertEqual([kind for kind, _ in signals], ["repeated-class", "record-only"])

    def test_a_record_beside_a_code_path_stays_recommended(self):
        outcome, _ = review_routing.floor(
            ["docs/process-defects/a.md", "scripts/x.py"],
            [],
            [],
            rows((CHANGE, "wording"), (CHANGE, "wording")),
            CHANGE,
        )
        self.assertEqual(outcome, RECOMMENDED)

    def test_a_record_beside_a_mandatory_path_is_required(self):
        outcome, signals = review_routing.floor(
            ["docs/process-defects/a.md", "helm/values/x.yaml"],
            [],
            [],
            rows((CHANGE, "wording"), (CHANGE, "wording")),
            CHANGE,
        )
        self.assertEqual(outcome, REQUIRED)
        self.assertEqual(signals[0][0], "infrastructure-or-deployment")

    def test_a_new_requirement_block_is_required(self):
        outcome, signals = review_routing.floor(["docs/x.md"], [], kinds("ADDED"), [], CHANGE)
        self.assertEqual(outcome, REQUIRED)
        self.assertEqual(signals[0][0], "requirement-block-added")


class FailureTests(unittest.TestCase):
    def test_a_failed_derivation_reports_required_and_exits_non_zero(self):
        with contextlib.redirect_stdout(io.StringIO()) as printed:
            status = review_routing.main(["--repo", "/no/such/repository", "--change", CHANGE])
        self.assertEqual(status, 1)
        self.assertTrue(printed.getvalue().startswith(REQUIRED + ": the routing derivation failed"))

    def test_an_unreadable_ledger_is_a_failure(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        ledger = Path(directory.name) / "review-rounds.tsv"
        ledger.write_bytes(b"\xff\xfe not utf-8 \xff")
        with contextlib.redirect_stdout(io.StringIO()) as printed:
            status = review_routing.main(
                ["--repo", str(REPO), "--change", CHANGE, "--ledger", str(ledger)]
            )
        self.assertEqual(status, 1)
        self.assertIn(REQUIRED, printed.getvalue())

    def test_a_missing_ledger_is_a_failure(self):
        with contextlib.redirect_stdout(io.StringIO()) as printed:
            status = review_routing.main(
                ["--repo", str(REPO), "--change", CHANGE, "--ledger", "docs/no-such-ledger.tsv"]
            )
        self.assertEqual(status, 1)
        self.assertIn(REQUIRED, printed.getvalue())

    def test_an_unknown_change_is_a_failure_not_a_silent_not_needed(self):
        with contextlib.redirect_stdout(io.StringIO()) as printed:
            status = review_routing.main(["--repo", str(REPO), "--change", "no-such-change"])
        self.assertEqual(status, 1)
        self.assertIn(REQUIRED, printed.getvalue())


class CliTests(unittest.TestCase):
    """Argument-plumbing controls over a scratch repository: each run names its unit, its change, or neither."""

    def scratch(self, ledger_rows, change=None, delta=None, untracked=None):
        """A temporary git repository holding the ledger rows, and optionally a change dir and an untracked path."""
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        repo = Path(directory.name)
        (repo / "README.md").write_text("base\n", encoding="utf-8")
        ledger = repo / "docs" / "review-rounds.tsv"
        ledger.parent.mkdir(parents=True)
        ledger.write_text(
            "# header\n" + "".join("\t".join(row) + "\n" for row in ledger_rows), encoding="utf-8"
        )
        if change:
            (repo / "openspec" / "changes" / change / "specs" / "cap").mkdir(parents=True)
            if delta is not None:
                (repo / "openspec" / "changes" / change / "specs" / "cap" / "spec.md").write_text(
                    delta, encoding="utf-8"
                )
        self.git(repo, "init", "-q")
        self.git(repo, "add", "README.md", "docs/review-rounds.tsv")
        for path in ("openspec",):
            if (repo / path).exists():
                self.git(repo, "add", path)
        self.git(repo, "commit", "-q", "-m", "base")
        if untracked:
            (repo / untracked).parent.mkdir(parents=True, exist_ok=True)
            (repo / untracked).write_text("x\n", encoding="utf-8")
        return repo

    def git(self, repo, *args):
        subprocess.run(
            ["git", "-c", "user.email=t@example.com", "-c", "user.name=t", *args],
            cwd=str(repo),
            check=True,
            capture_output=True,
        )

    def run_tool(self, repo, *args):
        with contextlib.redirect_stdout(io.StringIO()) as printed:
            status = review_routing.main(["--repo", str(repo), "--base", "HEAD", *args])
        return status, printed.getvalue()

    @staticmethod
    def repeated(unit):
        return [
            ["2026-01-01", unit, "1", "wording", "wording", "1"],
            ["2026-01-01", unit, "2", "coherence", "coherence", "1"],
        ]

    def record(self, repo, path, text="x\n"):
        target = repo / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(text, encoding="utf-8")
        return target

    def test_a_record_only_run_is_not_needed_and_reports_the_exemption(self):
        repo = self.scratch(self.repeated("u"))
        self.record(repo, "docs/process-defects/new.md")

        status, out = self.run_tool(repo, "--unit", "u")

        self.assertEqual(status, 0)
        self.assertIn("ROUTING: NOT_NEEDED", out)
        self.assertIn("[repeated-class]", out)
        self.assertIn("[record-only]", out)

    def test_a_code_file_renamed_into_a_record_directory_stays_recommended(self):
        repo = self.scratch(self.repeated("u"))
        self.record(repo, "src/note.md")
        self.git(repo, "add", "src/note.md")
        self.git(repo, "commit", "-q", "-m", "src")
        (repo / "docs" / "process-defects").mkdir(parents=True, exist_ok=True)
        self.git(repo, "mv", "src/note.md", "docs/process-defects/note.md")

        status, out = self.run_tool(repo, "--unit", "u")

        self.assertEqual(status, 0)
        self.assertIn("ROUTING: RECOMMENDED", out)
        self.assertNotIn("[record-only]", out)

    def test_a_record_renamed_between_record_directories_is_not_needed(self):
        repo = self.scratch(self.repeated("u"))
        self.record(repo, "docs/process-defects/note.md")
        self.git(repo, "add", "docs/process-defects/note.md")
        self.git(repo, "commit", "-q", "-m", "record")
        (repo / "docs" / "retrospectives").mkdir(parents=True, exist_ok=True)
        self.git(repo, "mv", "docs/process-defects/note.md", "docs/retrospectives/note.md")

        status, out = self.run_tool(repo, "--unit", "u")

        self.assertEqual(status, 0)
        self.assertIn("ROUTING: NOT_NEEDED", out)
        self.assertIn("[record-only]", out)

    def test_a_deleted_record_is_still_record_only(self):
        repo = self.scratch(self.repeated("u"))
        self.record(repo, "docs/process-defects/old.md")
        self.git(repo, "add", "docs/process-defects/old.md")
        self.git(repo, "commit", "-q", "-m", "record")
        self.git(repo, "rm", "-q", "docs/process-defects/old.md")

        status, out = self.run_tool(repo, "--unit", "u")

        self.assertEqual(status, 0)
        self.assertIn("ROUTING: NOT_NEEDED", out)
        self.assertIn("[record-only]", out)

    def test_a_non_ascii_record_path_is_classified(self):
        repo = self.scratch(self.repeated("u"))
        self.record(repo, "docs/process-defects/запись.md")

        status, out = self.run_tool(repo, "--unit", "u")

        self.assertEqual(status, 0)
        self.assertIn("ROUTING: NOT_NEEDED", out)
        self.assertIn("[record-only]", out)

    def test_a_dot_prefixed_ledger_is_recognised(self):
        repo = self.scratch([])
        ledger = repo / "docs" / "review-rounds.tsv"
        ledger.write_text(
            ledger.read_text(encoding="utf-8")
            + "".join("\t".join(row) + "\n" for row in self.repeated("u")),
            encoding="utf-8",
        )
        self.record(repo, "docs/process-defects/a.md")

        status, out = self.run_tool(repo, "--unit", "u", "--ledger", "./docs/review-rounds.tsv")

        self.assertEqual(status, 0)
        self.assertIn("ROUTING: NOT_NEEDED", out)
        self.assertIn("[record-only]", out)

    def test_an_absolute_ledger_is_recognised(self):
        repo = self.scratch([])
        ledger = repo / "docs" / "review-rounds.tsv"
        ledger.write_text(
            ledger.read_text(encoding="utf-8")
            + "".join("\t".join(row) + "\n" for row in self.repeated("u")),
            encoding="utf-8",
        )
        self.record(repo, "docs/process-defects/a.md")

        status, out = self.run_tool(
            repo, "--unit", "u", "--ledger", str(repo / "docs" / "review-rounds.tsv")
        )

        self.assertEqual(status, 0)
        self.assertIn("ROUTING: NOT_NEEDED", out)
        self.assertIn("[record-only]", out)

    def test_a_custom_ledger_is_the_path_the_run_reads(self):
        repo = self.scratch([])
        self.record(repo, "ledgers/rounds.tsv", "\n".join("\t".join(r) for r in self.repeated("u")) + "\n")
        self.record(repo, "docs/process-defects/a.md")

        status, out = self.run_tool(repo, "--unit", "u", "--ledger", "ledgers/rounds.tsv")

        self.assertEqual(status, 0)
        self.assertIn("ROUTING: NOT_NEEDED", out)
        self.assertIn("[record-only]", out)

    def test_a_malformed_ledger_fails_on_a_record_only_set(self):
        repo = self.scratch([["2026-01-01", "u"]])
        self.record(repo, "docs/process-defects/a.md")

        status, out = self.run_tool(repo, "--unit", "u")

        self.assertNotEqual(status, 0)
        self.assertIn("REQUIRED: the routing derivation failed", out)
        self.assertIn("malformed ledger row", out)

    def test_a_malformed_ledger_fails_beside_a_mandatory_path(self):
        repo = self.scratch([["2026-01-01", "u"]])
        self.record(repo, "helm/values/x.yaml")

        status, out = self.run_tool(repo, "--unit", "u")

        self.assertNotEqual(status, 0)
        self.assertIn("REQUIRED: the routing derivation failed", out)
        self.assertIn("malformed ledger row", out)

    def test_a_unit_only_run_without_a_change_directory_recommends(self):
        status, out = self.run_tool(self.scratch(self.repeated("u")), "--unit", "u")
        self.assertEqual(status, 0)
        self.assertIn("ROUTING: RECOMMENDED", out)
        self.assertIn("[repeated-class]", out)

    def test_a_unit_only_run_without_its_own_repeats_is_not_needed(self):
        status, out = self.run_tool(self.scratch(self.repeated("other")), "--unit", "u")
        self.assertEqual(status, 0)
        self.assertIn("ROUTING: NOT_NEEDED", out)
        self.assertNotIn("repeated-class", out)

    def test_a_change_only_run_keeps_the_change_units_signal(self):
        status, out = self.run_tool(self.scratch(self.repeated("c"), change="c"), "--change", "c")
        self.assertEqual(status, 0)
        self.assertIn("ROUTING: RECOMMENDED", out)
        self.assertIn("[repeated-class]", out)

    def test_a_different_unit_decides_when_the_change_delta_carries_no_block(self):
        status, out = self.run_tool(
            self.scratch(self.repeated("u"), change="c", delta="## Purpose\nprose\n"), "--change", "c", "--unit", "u"
        )
        self.assertEqual(status, 0)
        self.assertIn("ROUTING: RECOMMENDED", out)
        self.assertIn("[repeated-class]", out)

    def test_a_change_block_decides_and_names_its_spec(self):
        status, out = self.run_tool(
            self.scratch(self.repeated("u"), change="c", delta="## ADDED Requirements\n"),
            "--change",
            "c",
            "--unit",
            "u",
        )
        self.assertEqual(status, 0)
        self.assertIn("ROUTING: REQUIRED", out)
        self.assertIn("openspec/changes/c/specs/cap/spec.md", out)

    def test_no_identity_is_a_visible_failure(self):
        status, out = self.run_tool(self.scratch(self.repeated("u")))
        self.assertEqual(status, 1)
        self.assertIn("no unit identity", out)
        self.assertTrue(out.startswith(REQUIRED + ": the routing derivation failed"))

    def test_a_required_path_signal_does_not_hide_a_missing_identity(self):
        repo = self.scratch(self.repeated("u"), untracked="showcase-command-api/x.java")
        status, out = self.run_tool(repo)
        self.assertEqual(status, 1)
        self.assertIn("no unit identity", out)

    def test_an_explicit_unit_does_not_bypass_change_directory_validation(self):
        status, out = self.run_tool(self.scratch(self.repeated("u")), "--unit", "u", "--change", "no-such-change")
        self.assertEqual(status, 1)
        self.assertIn("no such change", out)


if __name__ == "__main__":
    unittest.main()
