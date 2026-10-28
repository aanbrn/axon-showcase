#!/usr/bin/env python3
"""Tests for the deep-review routing floor (`scripts/review-routing.py`). Stdlib only.

Run with `python3 scripts/test-review-routing.py`. The tool's kebab-case filename is not an importable identifier, so it
is loaded by path, the way `scripts/test-claim-scan.py` loads the claim scanner.

Every arm of every signal class carries its own case, so a rule that stops detecting its signal fails here rather than
reading as a floor that found nothing.
"""

import contextlib
import importlib.util
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
