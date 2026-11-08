"""Print the retrospective digest's review-rounds block from the per-unit records (`docs/units/*/rounds.tsv`).

The digest states a retrospective window's review rounds for the `experience-analyzer` subagent: the round count and the
compliance-miss count both as the `since`-inclusive filter yields them and for the window proper (the rows dated after
the `since` date), and the classes raised tallied under the reviewer's canonical vocabulary, each class counted once per
round that raised it.

The classes compare across the canonical boundary only once normalized. Rows recorded before the reviewer's canonical
class list landed (with the archived `2026-10-15-give-the-review-a-canonical-class-list` change, merged 2026-10-15) keep
the labels they were recorded with, so a label on a row dated on or before `PRE_CANONICAL_THROUGH` is mapped through
`PRE_CANONICAL_MAP`, and a label that mapping does not cover is reported as unmapped rather than passed through
silently. A label on a later row is taken as recorded when the canonical list names it and reported as unmapped when it
does not. When that list cannot be read, the block is still printed, its unmapped line states that normalization is
unestablished and why, and the later labels are counted as classes, so the retrospective gather keeps running.

Usage: python3 scripts/review-rounds.py [--since YYYY-MM-DD] [--record PATH]
Stdlib only.
"""

import argparse
import re
import sys
from datetime import date, timedelta
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Sequence, Set, Tuple

# The shared vocabulary module sits beside this script; load it by that path, whatever the caller's working
# directory or import route.
sys.path.insert(0, str(Path(__file__).resolve().parent))

from review_vocabulary import REVIEW_QUICK_DEFINITION, canonical_classes

RECORD_DEFAULT = "docs/units"

# The last day whose rows may carry a pre-canonical label: the canonical class list landed with #577, merged 2026-10-15.
PRE_CANONICAL_THROUGH = "2026-10-15"

# Label -> canonical class, for the labels the record carries from before the canonical list landed. The first 54 pairs
# are transcribed from the mapping table in that change's archived `design.md`; the last two are the canonical labels
# whose rows already used the class's own name, so that table lists them under no other label. The tests re-derive the
# 54 from the archived table and assert them equal, and assert the two identities present.
PRE_CANONICAL_MAP: Dict[str, str] = dict(
    [
        ("claims-table-source", "claim-unreproducible"),
        ("unsourced-claim", "claim-unreproducible"),
        ("derived-claim", "claim-unreproducible"),
        ("unverifiable-claim", "claim-unreproducible"),
        ("stale-claim", "claim-unreproducible"),
        ("documented-numbers", "claim-unreproducible"),
        ("derived-number", "claim-unreproducible"),
        ("unreproducible-reproduction", "claim-unreproducible"),
        ("claims-table-omission", "claim-missing"),
        ("claims-table", "claim-missing"),
        ("claims-table-anchor", "claim-missing"),
        ("missing-row", "claim-missing"),
        ("contradiction", "contradiction"),
        ("incomplete-enumeration", "incomplete-enumeration"),
        ("missing-artifact", "incomplete-enumeration"),
        ("over-claim", "over-claim"),
        ("boundary", "over-claim"),
        ("scope", "over-claim"),
        ("robustness", "over-claim"),
        ("wording", "wording"),
        ("imprecise-claim", "wording"),
        ("misleading-name", "wording"),
        ("ambiguity", "wording"),
        ("typo", "wording"),
        ("formatting", "formatting"),
        ("single-sourcing", "single-sourcing"),
        ("missing-task", "missing-task"),
        ("missing-step", "missing-task"),
        ("missing-record", "missing-task"),
        ("missing-artifact-decision", "missing-task"),
        ("missing-marker", "missing-task"),
        ("missing-section", "missing-task"),
        ("missing-delta", "missing-task"),
        ("missing-content", "missing-task"),
        ("missing-verification", "missing-task"),
        ("unticked-task", "missing-task"),
        ("task-state", "missing-task"),
        ("task-check", "missing-task"),
        ("missing-scenario", "missing-scenario"),
        ("coverage-gap", "missing-scenario"),
        ("impact-omission", "impact-omission"),
        ("test-control", "test-control"),
        ("command-defect", "test-control"),
        ("dropped-content", "dropped-content"),
        ("incorrect-mapping", "dropped-content"),
        ("premise", "premise"),
        ("mechanism", "premise"),
        ("coherence", "coherence"),
        ("dangling-reference", "coherence"),
        ("stale-cross-reference", "coherence"),
        ("contingent-reference", "coherence"),
        ("redundant-code", "redundant-code"),
        ("docs-residue", "docs-residue"),
        ("proposal-schema", "proposal-schema"),
        ("claim-missing", "claim-missing"),
        ("claim-unreproducible", "claim-unreproducible"),
    ]
)

DATE = re.compile(r"\d{4}-\d{2}-\d{2}")

# The width of the six-field round row the retirement introduced, which carries the loop's outcome and no session. The
# same number of fields is the pre-fields form of a row dated before 2026-10-18, which likewise carries no session. Only a
# row this wide takes the register join.
RETIRED_ROW_FIELDS = 6

# The register's field positions, for its last widest form.
REGISTER_AGENT = 3
REGISTER_RETURNED = 8
REGISTER_OUTCOME = 12
REGISTER_ROUND = 14
REGISTER_EVENT_ROW_FIELDS = 15


def register_sessions(path: Path) -> Dict[Tuple[str, str], str]:
    """The session each `(unit, round)` resolves to, from the registers' quick reviewer `report` rows.

    The register is read permissively: a row of either width is skipped when it carries no round, and a round with
    several attempts resolves through its `report` row rather than a stalled one. A unit with no register yields no
    entries, so the echo never requires one.
    """
    files = sorted(path.glob("*/sessions.tsv")) if path.is_dir() else [path.parent / "sessions.tsv"]
    sessions: Dict[Tuple[str, str], str] = {}
    for register in files:
        if not register.is_file():
            continue
        for line in register.read_text(encoding="utf-8").splitlines():
            if not line.strip() or line.startswith("#"):
                continue
            fields = line.split("\t")
            if len(fields) < REGISTER_EVENT_ROW_FIELDS or fields[REGISTER_AGENT] != "review-quick":
                continue
            round_text = fields[REGISTER_ROUND]
            returned = fields[REGISTER_RETURNED]
            if fields[REGISTER_OUTCOME] != "report" or not round_text.isdigit() or returned in ("", "-"):
                continue
            sessions[(fields[1], round_text)] = returned
    return sessions


def read_rows(path: Path) -> List[List[str]]:
    """The records' data rows as tab-separated field lists, comment and blank lines dropped.

    `path` is the directory holding the per-unit records, read in unit order so a unit's rows keep their recorded order;
    a single file is still read when one is named. A row of the retired six-field form gains the session the unit's
    register reports for its round, and keeps its own fields when the register has none.
    """
    files = sorted(path.glob("*/rounds.tsv")) if path.is_dir() else [path]
    sessions = register_sessions(path)
    rows: List[List[str]] = []
    for record in files:
        for line in record.read_text(encoding="utf-8").splitlines():
            if not line.strip() or line.startswith("#"):
                continue
            fields = line.split("\t")
            if len(fields) == RETIRED_ROW_FIELDS:
                session = sessions.get((fields[1], fields[2]))
                if session is not None:
                    fields = fields + [session]
            rows.append(fields)
    return rows


def raised_classes(row: Sequence[str]) -> List[str]:
    """The distinct labels a row's `classes raised` field carries."""
    return [label.strip() for label in row[3].split(",") if label.strip() and label.strip() != "-"]


def canonical_class(label: str, row_date: str, canonical: Optional[Set[str]]) -> Optional[str]:
    """The label's canonical class, or `None` when no normalization yields one.

    A row at or before the boundary is mapped through `PRE_CANONICAL_MAP`; a later row is taken as recorded when the
    canonical list names it. An unreadable list (`canonical` is `None`) takes every later label as recorded, which the
    block states as unestablished rather than reporting no unmapped label.
    """
    if row_date <= PRE_CANONICAL_THROUGH:
        return PRE_CANONICAL_MAP.get(label)
    if canonical is None:
        return label
    return label if label in canonical else None


def tally(
    rows: Iterable[Sequence[str]], canonical: Optional[Set[str]]
) -> Tuple[List[Tuple[str, int]], List[Tuple[str, int]]]:
    """The (classes, unmapped labels) a row set raises, each as (name, count) pairs, most-raised first.

    A class contributes at most once per round: the ledger records the classes a round raised as a set, so two labels
    that normalize to one class on one row are one occurrence, not two, and the count is never a share of the
    compliance-miss total.
    """
    counts: Dict[str, int] = {}
    unmapped: Dict[str, int] = {}
    for row in rows:
        seen: Set[Tuple[str, str]] = set()
        for label in raised_classes(row):
            cls = canonical_class(label, row[0], canonical)
            kind = "class" if cls is not None else "unmapped"
            bucket = counts if cls is not None else unmapped
            name = cls if cls is not None else label
            key = (kind, name)
            if key in seen:
                continue
            seen.add(key)
            bucket[name] = bucket.get(name, 0) + 1
    by_count = lambda pair: (-pair[1], pair[0])
    return sorted(counts.items(), key=by_count), sorted(unmapped.items(), key=by_count)


def figures(rows: Sequence[Sequence[str]]) -> Tuple[int, int]:
    """The (round count, compliance-miss count) of a row set."""
    return len(rows), sum(int(row[5]) for row in rows)


def render(
    record: str,
    since: str,
    rows: Sequence[Sequence[str]],
    canonical: Optional[Set[str]],
    reason: Optional[str],
) -> List[str]:
    """The digest's review-rounds block for `rows`, and what it could not normalize when `reason` states it."""
    window = [row for row in rows if row[0] >= since]
    proper = [row for row in window if row[0] > since]
    inclusive_rounds, inclusive_misses = figures(window)
    proper_rounds, proper_misses = figures(proper)
    classes, unmapped = tally(proper, canonical)
    unmapped_line = "  unmapped (window-proper, kept as recorded): {0}".format(_pairs(unmapped))
    if reason is not None:
        unmapped_line += " — unestablished: {0}; post-boundary labels counted as classes".format(reason)
    return (
        [heading(record)]
        + ["  " + "\t".join(row) for row in window]
        + [
            "  window (since {0}, inclusive of the since date): {1} round(s), {2} compliance miss(es)".format(
                since, inclusive_rounds, inclusive_misses
            ),
            "  window-proper (rows dated after {0}): {1} round(s), {2} compliance miss(es)".format(
                since, proper_rounds, proper_misses
            ),
            "  classes raised (window-proper; one contribution per canonical class per round, not a count of findings; "
            "labels on rows dated on or before {0} normalized to the reviewer's canonical list): {1}".format(
                PRE_CANONICAL_THROUGH, _pairs(classes)
            ),
            unmapped_line,
        ]
    )


def definition_classes() -> Tuple[Optional[Set[str]], Optional[str]]:
    """The canonical class set, and the reason it could not be read."""
    try:
        text = Path(REVIEW_QUICK_DEFINITION).read_text(encoding="utf-8")
    except OSError as error:
        return None, "{0} could not be read: {1}".format(REVIEW_QUICK_DEFINITION, error)
    classes, reason = canonical_classes(text)
    if reason is not None:
        return None, reason
    return set(classes), None


def heading(record: str) -> str:
    """The block's first line, naming the ledger and the miss column."""
    return "Review rounds ({0}; compliance_misses = findings that were a miss of an existing rule):".format(
        record
    )


def _pairs(items: Sequence[Tuple[str, int]]) -> str:
    """`name count` pairs, or `none` when empty."""
    return ", ".join("{0} {1}".format(name, count) for name, count in items) or "none"


def _since(value: str) -> str:
    if not DATE.fullmatch(value):
        raise argparse.ArgumentTypeError("expected a YYYY-MM-DD date, got {0!r}".format(value))
    return value


def main(argv: Sequence[str] = ()) -> int:
    parser = argparse.ArgumentParser(
        description="Print the digest's review-rounds block for a retrospective window."
    )
    parser.add_argument(
        "--since",
        type=_since,
        default=(date.today() - timedelta(days=7)).isoformat(),
        help="the window's first date, YYYY-MM-DD (default: 7 days ago)",
    )
    parser.add_argument(
        "--record",
        default=RECORD_DEFAULT,
        help="the directory holding the per-unit review-round records (default: {0})".format(RECORD_DEFAULT),
    )
    args = parser.parse_args(argv)
    path = Path(args.record)
    if not path.is_file() and not path.is_dir():
        print(heading(args.record))
        print("  (none on disk)")
        return 0
    canonical, reason = definition_classes()
    for line in render(args.record, args.since, read_rows(path), canonical, reason):
        print(line)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
