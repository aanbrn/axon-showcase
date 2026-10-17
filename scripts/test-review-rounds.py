#!/usr/bin/env python3
"""Tests for the review-round digest (`scripts/review-rounds.py`). Stdlib only.

Run with `python3 scripts/test-review-rounds.py`. The tool's kebab-case filename is not an importable identifier, so it
is loaded by path, the way `scripts/test-claim-scan.py` loads the claim scanner.

The window split is asserted over fixture ledgers rather than the real one: the record grows, so a figure quoted from a
past run (the 2026-10-15 retrospective's 130/203 and 87/145) no longer reproduces from today's ledger.
"""

import contextlib
import importlib.util
import io
import re
import tempfile
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
TOOL_PATH = Path(__file__).resolve().parent / "review-rounds.py"
_spec = importlib.util.spec_from_file_location("review_rounds", TOOL_PATH)
review_rounds = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(review_rounds)

LEDGER = REPO / "docs" / "review-rounds.tsv"
ARCHIVED_MAPPING = (
    REPO
    / "openspec"
    / "changes"
    / "archive"
    / "2026-10-15-give-the-review-a-canonical-class-list"
    / "design.md"
)
MAPPING_ROW = re.compile(r"^\|\s*`([a-z-]+)`\s*\|([^|]*)\|\s*$", re.M)
BOUNDARY = review_rounds.PRE_CANONICAL_THROUGH

# The label the record carries that is not a class: the rows' own flag, which appears once in the classes column of a
# pre-canonical row, so the mapping deliberately leaves it uncovered and the tool reports it as unmapped.
NOT_A_CLASS = "compliance-miss"


def fixture(test, lines):
    """A temporary ledger of the given tab-separated data lines, as a `Path`."""
    directory = tempfile.TemporaryDirectory()
    test.addCleanup(directory.cleanup)
    path = Path(directory.name) / "review-rounds.tsv"
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return path


def block(test, lines, since):
    """The rendered block for a fixture ledger."""
    return review_rounds.render("fixture.tsv", since, review_rounds.read_rows(fixture(test, lines)))


def line(block_lines, prefix):
    """The single block line starting with `prefix`."""
    matches = [text for text in block_lines if text.startswith(prefix)]
    assert len(matches) == 1, "expected one {0!r} line, got {1}".format(prefix, matches)
    return matches[0]


STRADDLING = [
    "2026-01-01\tunit-a\t1\twording\t-\t2",
    "2026-01-02\tunit-a\t2\twording\twording\t1",
    "2026-01-03\tunit-b\t1\tformatting\t-\t3",
]


class WindowSplitTests(unittest.TestCase):
    def test_the_inclusive_figures_include_the_since_date(self):
        lines = block(self, STRADDLING, "2026-01-02")
        self.assertIn(": 2 round(s), 4 compliance miss(es)", line(lines, "  window (since 2026-01-02"))

    def test_the_window_proper_figures_exclude_the_since_date(self):
        lines = block(self, STRADDLING, "2026-01-02")
        self.assertIn(": 1 round(s), 3 compliance miss(es)", line(lines, "  window-proper (rows dated after"))

    def test_every_window_row_is_printed_verbatim(self):
        lines = block(self, STRADDLING, "2026-01-02")
        self.assertEqual(
            sorted(text.strip() for text in lines if text.startswith("  20")),
            ["2026-01-02\tunit-a\t2\twording\twording\t1", "2026-01-03\tunit-b\t1\tformatting\t-\t3"],
        )

    def test_a_window_holding_nothing_reports_zero(self):
        lines = block(self, STRADDLING, "2026-02-01")
        self.assertIn(": 0 round(s), 0 compliance miss(es)", line(lines, "  window-proper (rows dated after"))
        self.assertIn(": none", line(lines, "  classes raised"))


class CandidateClassTests(unittest.TestCase):
    def test_a_pre_canonical_label_is_normalized_to_its_class(self):
        lines = block(self, ["2026-10-01\tunit-a\t1\tclaims-table-anchor,ambiguity\t-\t1"], "2026-09-30")
        raised = line(lines, "  classes raised")
        self.assertIn("claim-missing 1", raised)
        self.assertIn("wording 1", raised)
        self.assertNotIn("claims-table-anchor", raised)

    def test_a_label_the_mapping_lacks_is_reported_unmapped(self):
        lines = block(self, ["2026-10-01\tunit-a\t1\t" + NOT_A_CLASS + "\t-\t1"], "2026-09-30")
        self.assertIn(": " + NOT_A_CLASS + " 1", line(lines, "  unmapped"))
        self.assertNotIn(NOT_A_CLASS, line(lines, "  classes raised"))

    def test_a_post_boundary_label_is_taken_as_recorded(self):
        lines = block(self, ["2026-12-01\tunit-a\t1\tstructured-writing\t-\t1"], "2026-11-30")
        self.assertIn("structured-writing 1", line(lines, "  classes raised"))
        self.assertIn(": none", line(lines, "  unmapped"))

    def test_a_repeated_label_on_one_row_counts_once(self):
        lines = block(self, ["2026-10-01\tunit-a\t1\twording,ambiguity\t-\t1"], "2026-09-30")
        self.assertIn("wording 2", line(lines, "  classes raised"))


class DeclaredMappingTests(unittest.TestCase):
    def archived_pairs(self):
        """The mapping #577's archived design records, re-derived from its table."""
        pairs = {}
        for cls, labels in MAPPING_ROW.findall(ARCHIVED_MAPPING.read_text(encoding="utf-8")):
            for label in labels.split(","):
                label = label.strip()
                if label:
                    pairs[label] = cls
        return pairs

    def test_the_declared_pairs_equal_the_archived_table(self):
        archived = self.archived_pairs()
        self.assertEqual(
            len(archived), 54, "the archived table's pair count changed: {0}".format(len(archived))
        )
        declared = {label: cls for label, cls in review_rounds.PRE_CANONICAL_MAP.items() if label in archived}
        self.assertEqual(declared, archived)

    def test_the_identity_rows_cover_the_canonical_labels_the_table_omits(self):
        archived = self.archived_pairs()
        extras = {label: cls for label, cls in review_rounds.PRE_CANONICAL_MAP.items() if label not in archived}
        self.assertEqual(extras, {"claim-missing": "claim-missing", "claim-unreproducible": "claim-unreproducible"})

    def test_the_mapping_covers_every_pre_boundary_label_the_ledger_carries(self):
        covered = set(review_rounds.PRE_CANONICAL_MAP)
        observed = set()
        for row in review_rounds.read_rows(LEDGER):
            if row[0] <= BOUNDARY:
                observed.update(review_rounds.raised_classes(row))
        self.assertEqual(observed - covered, {NOT_A_CLASS})
        self.assertTrue(observed <= covered | {NOT_A_CLASS})


class CommandTests(unittest.TestCase):
    def test_a_missing_ledger_reports_none_on_disk(self):
        missing = str(Path(tempfile.gettempdir()) / "no-such-review-rounds.tsv")
        with contextlib.redirect_stdout(io.StringIO()) as printed:
            self.assertEqual(review_rounds.main(["--record", missing, "--since", "2026-01-01"]), 0)
        self.assertIn("(none on disk)", printed.getvalue())

    def test_a_malformed_since_date_is_rejected(self):
        with contextlib.redirect_stderr(io.StringIO()):
            with self.assertRaises(SystemExit):
                review_rounds.main(["--since", "20260101"])


if __name__ == "__main__":
    unittest.main()
