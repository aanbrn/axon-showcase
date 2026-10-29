"""Derive the deep-review routing floor for a change from its own facts.

The review gate makes one routing decision per unit -- `REQUIRED`, `RECOMMENDED`, or `NOT_NEEDED` -- and this tool
supplies its deterministic floor, so the decision is explainable and reproducible rather than an unaided judgment. It
reads the change's own facts, not the repository's size:

  * the changed paths and the declarations in its diff;
  * the requirement-block kinds its delta spec carries (`ADDED`, `REMOVED`, `RENAMED`);
  * the unit's recorded rounds in the ledger, where a class repeated on at least two rows is evidence the quick loop is
    not converging. The unit's identity is `--unit` (its recorded name), defaulting to `--change`; a unit with no change
    directory passes `--unit` alone, and an invocation that names neither is a failed derivation;
  * a record-only changed-path set -- the Markdown records the repository keeps for its own process, plus the ledger
    read here -- where that repeat signal is reported but does not raise the floor, since such a unit ships no behavior
    to converge on. A rename is judged by both its source and its destination, which `git diff --name-status` supplies
    and `--name-only` does not.

The floor is a floor: the agent's decision must be at or above it, and a fact the tool cannot derive -- a major
specification deviation, a material defect an earlier review missed -- is the agent's judgment above it.

A derivation that fails -- an unreadable input, a malformed ledger row, a git error -- reports `REQUIRED` and exits
non-zero, so a missing or failed decision can only make review run, never skip it. Stdlib only, compatible with the
build's declared Python 3.12 minimum.
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path
from typing import List, Optional, Sequence, Tuple

REQUIRED = "REQUIRED"
RECOMMENDED = "RECOMMENDED"
NOT_NEEDED = "NOT_NEEDED"

LEDGER_DEFAULT = "docs/review-rounds.tsv"
# The record-artifact class the routing floor exempts: the Markdown records the repository keeps for its own process,
# and the ledger the invocation reads. The classification lives here alone, so the tool, its controls and the spec
# cannot drift apart.
RECORD_DIRS = ("docs/process-defects/", "docs/retrospectives/", "docs/load-tests/")
RECORD_SUFFIX = ".md"
CHANGE_ROOT = "openspec/changes"

# The signal classes, derived from the repository's own structure (its modules, its service domain packages, and its
# deployment surfaces), never from a hand-written guess about what is risky.
PUBLIC_CONTRACT = (
    "showcase-command-api/",
    "showcase-query-api/",
    "showcase-query-proto/",
    "showcase-projection-model/",
)
DOMAIN = (
    "showcase-command-service/src/main/java/showcase/command/",
    "showcase-projection-service/src/main/java/showcase/projection/",
    "showcase-query-service/src/main/java/showcase/query/",
)
PERSISTENCE = (re.compile(r"\.sql$"), re.compile(r"(^|/)db/"))
INFRASTRUCTURE = (".github/", "helm/", "config/", "docker-compose.yml")
INFRASTRUCTURE_NAME = re.compile(r"(^|/)Dockerfile$")
SECURITY_DECLARATION = re.compile(r"\bSecurityWebFilterChain\b|\bSecurityFilterChain\b")
# A declaration signal reads source, never prose: a document that merely quotes the token is not a changed declaration.
CODE_EXTENSION = re.compile(r"\.(java|kt|kts)$")
BLOCK_KIND = re.compile(r"^##\s+(ADDED|REMOVED|RENAMED)\s+Requirements\s*$", re.MULTILINE)

Signal = Tuple[str, str]
BlockKind = Tuple[str, str]
Row = Sequence[str]
FileText = Tuple[str, str]


def path_signals(paths: Sequence[str]) -> List[Signal]:
    """The `REQUIRED` signals a changed-path set carries."""
    signals: List[Signal] = []
    for path in paths:
        if any(path.startswith(prefix) for prefix in PUBLIC_CONTRACT):
            signals.append(("public-contract", path))
        elif any(path.startswith(prefix) for prefix in DOMAIN):
            signals.append(("domain-surface", path))
        elif any(pattern.search(path) for pattern in PERSISTENCE):
            signals.append(("persistence", path))
        elif any(path.startswith(prefix) for prefix in INFRASTRUCTURE) or INFRASTRUCTURE_NAME.search(path):
            signals.append(("infrastructure-or-deployment", path))
    return signals


def declaration_signals(files: Sequence[FileText]) -> List[Signal]:
    """The `REQUIRED` signal a changed source file's declarations carry, read from source rather than prose."""
    for path, text in files:
        if CODE_EXTENSION.search(path) and SECURITY_DECLARATION.search(text):
            return [("security-declaration", path + " declares a security filter chain")]
    return []


def block_signals(kinds: Sequence[BlockKind]) -> List[Signal]:
    """The `REQUIRED` signals a delta spec's requirement-block kinds carry."""
    return [("requirement-block-" + kind.lower(), source) for kind, source in kinds]


def ledger_rows(text: str) -> List[List[str]]:
    """The ledger's data rows, validated as they are read.

    A malformed row is a failed derivation wherever it appears, including beside a mandatory signal, so the validation
    runs during gathering rather than only where the signal is derived.
    """
    rows = [line.split("\t") for line in text.splitlines() if line.strip() and not line.startswith("#")]
    for row in rows:
        if len(row) < 5:
            raise ValueError("malformed ledger row: {0}".format("\t".join(row)))
    return rows


def ledger_signal(rows: Sequence[Row], unit: Optional[str]) -> List[Signal]:
    """The `RECOMMENDED` signal a unit's recorded rounds carry when a class repeats on two or more rows."""
    if not unit:
        raise ValueError("no unit identity: pass --unit or --change")
    repeats = 0
    for row in rows:
        if len(row) < 5:
            raise ValueError("malformed ledger row: {0}".format("\t".join(row)))
        if row[1] == unit and row[4].strip() not in ("", "-"):
            repeats += 1
    if repeats >= 2:
        return [("repeated-class", "{0} rows repeat a prior class".format(repeats))]
    return []


def ledger_identity(repo: Path, ledger: str) -> str:
    """The ledger's repository-relative path, so the predicate recognises it however the invocation spells it.

    `--ledger` may arrive relative to the repository, relative with a `./` prefix, or absolute; a form that resolves
    outside the repository keeps its own text and is not recognised as the ledger.
    """
    candidate = Path(ledger)
    absolute = (candidate if candidate.is_absolute() else repo / candidate).resolve()
    try:
        return absolute.relative_to(repo.resolve()).as_posix()
    except ValueError:
        return ledger


def is_record_path(path: str, ledger: str) -> bool:
    """Whether one changed path belongs to the record-artifact class: a Markdown record, or the ledger read here."""
    if path == ledger:
        return True
    return path.startswith(RECORD_DIRS) and path.endswith(RECORD_SUFFIX)


def record_only(paths: Sequence[str], ledger: str) -> bool:
    """Whether the changed-path set is non-empty and holds only record artifacts and the ledger the invocation reads.

    A rename arrives as both its source and its destination, so a code path renamed into a record directory keeps normal
    routing; a deleted record has no surviving copy and is still classified by its own path.
    """
    return bool(paths) and all(is_record_path(path, ledger) for path in paths)


def floor(
    paths: Sequence[str],
    files: Sequence[FileText],
    kinds: Sequence[BlockKind],
    rows: Sequence[Row],
    unit: Optional[str],
    ledger: str = LEDGER_DEFAULT,
) -> Tuple[str, List[Signal]]:
    """The routing floor and the signals it matched, from the change's own facts."""
    required = path_signals(paths) + declaration_signals(files) + block_signals(kinds)
    if required:
        return REQUIRED, required
    recommended = ledger_signal(rows, unit)
    if not recommended:
        return NOT_NEEDED, []
    if record_only(paths, ledger):
        return NOT_NEEDED, recommended + [
            ("record-only", "the repeat signal does not raise a record artifact's floor")
        ]
    return RECOMMENDED, recommended


def _git(repo: Path, *args: str) -> str:
    """Run git in ``repo`` and return its stdout, raising on failure."""
    result = subprocess.run(
        ["git", *args], cwd=str(repo), check=True, capture_output=True, text=True
    )
    return result.stdout


def _changed_paths(repo: Path, base: str) -> List[str]:
    """The changed paths, NUL-delimited, with both sides of a rename or a copy.

    `--name-only` prints only a rename's destination, so a code path renamed into a record directory would read as a
    record; the status form carries the source beside it. NUL delimiting keeps a non-ASCII path intact.
    """
    tokens = _git(repo, "diff", "--name-status", "-z", base, "--").split("\0")
    paths: List[str] = []
    index = 0
    while index < len(tokens):
        status = tokens[index]
        index += 1
        if not status:
            continue
        if index >= len(tokens) or not tokens[index]:
            raise ValueError("malformed diff status: {0}".format(status))
        paths.append(tokens[index])
        index += 1
        if status[:1] in ("R", "C"):
            if index >= len(tokens) or not tokens[index]:
                raise ValueError("malformed rename record: {0}".format(status))
            paths.append(tokens[index])
            index += 1
    return [path for path in paths if path]


def _added_by_file(diff_text: str) -> List[FileText]:
    """The diff's added lines, grouped by the file they belong to."""
    files: List[FileText] = []
    path = None
    buffer: List[str] = []
    for raw in diff_text.splitlines():
        if raw.startswith("+++"):
            if path is not None:
                files.append((path, "\n".join(buffer)))
            match = re.match(r"^\+\+\+ b/(.+)$", raw)
            path = match.group(1) if match else None
            buffer = []
        elif raw.startswith("+") and not raw.startswith("+++") and path is not None:
            buffer.append(raw[1:])
    if path is not None:
        files.append((path, "\n".join(buffer)))
    return files


def gather(repo: Path, base: str, change: Optional[str], ledger: str) -> Tuple[
    List[str], List[FileText], List[BlockKind], List[List[str]]
]:
    """The change's own facts: its changed paths, its changed files' added text, its delta's block kinds, its rows."""
    paths = _changed_paths(repo, base)
    untracked = [
        path for path in _git(repo, "ls-files", "--others", "--exclude-standard", "-z").split("\0") if path
    ]
    paths.extend(untracked)
    files = _added_by_file(_git(repo, "diff", "--no-color", base, "--"))
    for path in untracked:
        try:
            files.append((path, (repo / path).read_text(encoding="utf-8")))
        except (OSError, UnicodeDecodeError):
            raise ValueError("cannot read untracked file: {0}".format(path))
    kinds: List[BlockKind] = []
    if change:
        change_root = repo / CHANGE_ROOT / change
        if not change_root.is_dir():
            raise ValueError("no such change: {0}".format(change))
        delta_root = change_root / "specs"
        for spec in sorted(delta_root.rglob("spec.md")):
            try:
                text = spec.read_text(encoding="utf-8")
            except (OSError, UnicodeDecodeError):
                raise ValueError("cannot read delta spec: {0}".format(spec))
            kinds.extend(
                (kind, str(spec.relative_to(repo))) for kind in sorted(set(BLOCK_KIND.findall(text)))
            )
    ledger_path = repo / ledger
    if not ledger_path.is_file():
        raise ValueError("no such ledger: {0}".format(ledger))
    try:
        rows = ledger_rows(ledger_path.read_text(encoding="utf-8"))
    except (OSError, UnicodeDecodeError):
        raise ValueError("cannot read the ledger: {0}".format(ledger))
    return paths, files, kinds, rows


def main(argv: Sequence[str] = ()) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Derive the deep-review routing floor (REQUIRED, RECOMMENDED, or NOT_NEEDED) for a change from its own "
            "facts, and print the signals it matched."
        )
    )
    parser.add_argument(
        "--unit",
        help=(
            "the unit's recorded ledger identity (default: the --change name); pass it for a unit with no change "
            "directory"
        ),
    )
    parser.add_argument("--change", help="the change directory whose delta spec is read")
    parser.add_argument("--base", default="origin/main", help="the ref to diff against (default: origin/main)")
    parser.add_argument("--repo", default=".", help="the repository to read (default: the current directory)")
    parser.add_argument(
        "--ledger",
        default=LEDGER_DEFAULT,
        help="the review-round ledger (default: {0})".format(LEDGER_DEFAULT),
    )
    args = parser.parse_args(argv)
    try:
        unit = args.unit or args.change
        if not unit:
            raise ValueError("no unit identity: pass --unit or --change")
        paths, files, kinds, rows = gather(Path(args.repo), args.base, args.change, args.ledger)
        ledger_path = ledger_identity(Path(args.repo), args.ledger)
        outcome, signals = floor(paths, files, kinds, rows, unit, ledger_path)
    except (subprocess.CalledProcessError, ValueError, OSError) as failure:
        print("{0}: the routing derivation failed ({1})".format(REQUIRED, failure))
        return 1
    print("ROUTING: {0}".format(outcome))
    for kind, text in signals:
        print("  [{0}] {1}".format(kind, text))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
