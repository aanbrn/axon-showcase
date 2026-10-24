#!/usr/bin/env python3
"""Check commit hygiene: the staged set, `captured:` marker placement, and the tracked set.

The `--staged` mode is the pre-commit guard. It refuses a commit whose staged set the project's formatter would
rewrite, that force-stages a generated artifact, that stages a path and then edits it again (leaving the index stale),
that carries a merge conflict marker, that misplaces a `captured:` marker in the project's guidance files, or that
stages an `openspec/changes/archive/**` change dir while leaving its `openspec/specs/**` sync unstaged. The
`--markers` mode checks marker placement in the working-tree guidance files; the `--tracked-ignored`,
`--conflict-markers`, and
`--executable-bits` modes each verify the tracked set for the build; `--unique-crons` verifies the workflow
`cron` schedules do not collide; `--unique-rounds` verifies the review-round ledger carries one row per unit and
round, that each unit's rounds are contiguous from 1, and that a round named with `--expect` is recorded (which
needs that mode); `--session-continuity` verifies the ledger's reviewer-session fields record each round's session
and its transition; and `--large-files` verifies no tracked file exceeds the configured size limit. Stdlib only.
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path
from typing import Optional, Sequence

REPO_ROOT = Path(__file__).resolve().parent.parent
AGENTS = "AGENTS.md"
MARKER_GLOBS = (".opencode/agent/*.md", ".opencode/skills/*/SKILL.md")
FORMATTER_OWNED = (".java", ".kt", ".kts", ".md", ".json")
DEFAULT_FORMATTER = ("./gradlew", "spotlessCheck")
MARKER = "captured:"
LARGE_FILES_CONFIG = "config/commit-hygiene/large-files.properties"
LEAD_RE = re.compile(r"^(- |\*\*)")
CONFLICT_MARKER_RE = r"^(<<<<<<< |>>>>>>> )"
VENDORED_SKILL_PREFIXES = (".opencode/skills/ast-grep/", ".opencode/skills/axon4to5-", ".opencode/skills/openspec-")
ARCHIVE_PREFIX = "openspec/changes/archive/"
SPECS_PREFIX = "openspec/specs/"
CRON_LINE_RE = re.compile(r"^\s*-\s*cron:\s*(?P<value>.+?)\s*$")
WORKFLOW_SUFFIXES = (".yml", ".yaml")

# The ledger's own comment marking the first row that carries the reviewer-session fields; the rows above it predate
# them, so the continuity verification exempts those. Its absence is itself a violation, so the boundary cannot be
# dropped silently to exempt a row.
SESSION_FIELDS_MARKER = "# --- Session continuity"
# A gap whose record is unrecoverable is acknowledged by a comment naming the unit and the round, so the
# contiguity check stays strict for every other row rather than exempting a unit. The same form acknowledges a number
# assigned incorrectly to a deep-review pass whose authoritative record moved to the change's review task.
ACKNOWLEDGED_GAP_RE = re.compile(r"^#\s*---\s*Acknowledged gap:\s*(?P<unit>\S+)\s+round\s+(?P<round>\d+)\b")

# A row's session transition, one per shape the review loop's rule allows; a replacement's reason is a recorded
# field, never evidence that the stated cause occurred.
SESSION_CREATED = "created"
SESSION_CONTINUED = "continued"
SESSION_REPLACED = "replaced"
SESSION_TRANSITIONS = (SESSION_CREATED, SESSION_CONTINUED, SESSION_REPLACED)

# The reasons a replacement may carry: the review loop's recovery cases, plus `caller-restarted`, which records
# a continuity violation already committed rather than a recovery case.
SESSION_REPLACEMENT_REASONS = (
    "expired",
    "definition-changed",
    "stopped-responding",
    "caller-restarted",
)

# A gap's acknowledgement exempts the named (unit, round) pair only; a misfiled deep pass uses the same form and
# names its destination, without a data row.
# The ledger's row width once the session fields are present: the six round fields, then the identifier, the transition,
# and the reason.
SESSION_ROW_FIELDS = 9

# The ledger's row width before the session fields were appended. A row above the boundary whose field count is not
# this means the boundary was moved past a row the fields apply to, which would exempt that row silently.
SESSION_PRE_FIELDS = 6


def git(repo: Path, *args: str) -> subprocess.CompletedProcess:
    return subprocess.run(("git", *args), cwd=repo, capture_output=True, text=True)


def staged_paths(repo: Path) -> list[str]:
    result = git(repo, "diff", "--cached", "--name-only", "-z", "--diff-filter=ACMR")
    return [path for path in result.stdout.split("\0") if path]


def staged_file_text(repo: Path, path: str) -> Optional[str]:
    result = git(repo, "show", f":{path}")
    return result.stdout if result.returncode == 0 else None


def find_staged_then_edited(repo: Path) -> list[str]:
    offenders = []
    # `-z` NUL-separates the records and, for a rename, emits the new name in the status-prefixed field followed by the
    # old name as its own field with no status columns. That old-name field must be skipped for EVERY rename, before the
    # offender test: a clean rename (`R ` with an unmodified worktree) does not enter the branch below, and an old name
    # whose first two characters look like status columns (e.g. `RM.txt`) would otherwise be read as a record and yield
    # a bogus path such as `txt`.
    records = git(repo, "status", "--porcelain", "-z").stdout.split("\0")
    index = 0
    while index < len(records):
        line = records[index]
        index += 1
        if len(line) < 3:
            continue
        index_status, worktree_status = line[0], line[1]
        if index_status == "R":
            index += 1
        if index_status in "MARC" and worktree_status in "MADRC":
            offenders.append(line[3:])
    return offenders


def find_tracked_ignored(repo: Path) -> list[str]:
    result = git(repo, "ls-files", "--cached", "--ignored", "--exclude-standard", "-z")
    return [path for path in result.stdout.split("\0") if path]


def find_force_staged_artifacts(repo: Path) -> list[str]:
    ignored = set(find_tracked_ignored(repo))
    return [path for path in staged_paths(repo) if path in ignored]


def unstaged_spec_paths(repo: Path) -> list[str]:
    modified = git(repo, "diff", "--name-only", "-z", "--", SPECS_PREFIX).stdout
    untracked = git(repo, "ls-files", "--others", "--exclude-standard", "-z", "--", SPECS_PREFIX).stdout
    return [path for path in (modified + untracked).split("\0") if path]


def _staged_archived_changes(staged: list[str]) -> list[str]:
    changes = set()
    for path in staged:
        if path.startswith(ARCHIVE_PREFIX):
            rest = path[len(ARCHIVE_PREFIX):]
            if "/" in rest:
                changes.add(ARCHIVE_PREFIX + rest.split("/", 1)[0])
    return sorted(changes)


def _carries_delta_specs(repo: Path, change_dir: str) -> bool:
    return any((repo / change_dir).glob("specs/**/spec.md"))


def find_unpaired_archive_sync(repo: Path) -> list[tuple[str, list[str]]]:
    staged = staged_paths(repo)
    with_deltas = [change for change in _staged_archived_changes(staged) if _carries_delta_specs(repo, change)]
    if not with_deltas:
        return []
    spec_paths = unstaged_spec_paths(repo)
    return [(change, spec_paths) for change in with_deltas] if spec_paths else []


def find_conflict_markers(repo: Path, staged: bool = False) -> list[str]:
    args = ["grep", "-I", "-n", "-E"]
    if staged:
        args.append("--cached")
    args.append(CONFLICT_MARKER_RE)
    return [line for line in git(repo, *args).stdout.splitlines() if line]


def _should_be_executable(path: str) -> bool:
    if path.startswith(VENDORED_SKILL_PREFIXES):
        return False
    return path == "gradlew" or path.startswith("scripts/git-hooks/") or path.endswith(".sh")


def find_non_executable_scripts(repo: Path) -> list[str]:
    offenders = []
    for line in git(repo, "ls-files", "-s", "-z").stdout.split("\0"):
        parts = line.split(None, 3)
        if len(parts) < 4:
            continue
        mode, path = parts[0], parts[3]
        if mode != "100755" and _should_be_executable(path):
            offenders.append(path)
    return offenders


def tracked_paths(repo: Path) -> list[str]:
    result = git(repo, "ls-files", "-z")
    return [path for path in result.stdout.split("\0") if path]


def read_max_bytes(repo: Path) -> int:
    """The configured maximum file size, from the properties file."""
    properties = repo / LARGE_FILES_CONFIG
    try:
        text = properties.read_text(encoding="utf-8")
    except OSError as error:
        raise ValueError(f"{LARGE_FILES_CONFIG} could not be read: {error}") from error
    for line in text.splitlines():
        stripped = line.strip()
        if stripped.startswith("#") or "=" not in stripped:
            continue
        key, _, value = stripped.partition("=")
        if key.strip() == "maxBytes":
            try:
                return int(value.strip())
            except ValueError as error:
                raise ValueError(f"maxBytes in {LARGE_FILES_CONFIG} is not an integer: {value.strip()!r}") from error
    raise ValueError(f"maxBytes is not set in {LARGE_FILES_CONFIG}")


def find_oversized_files(repo: Path, limit: int) -> list[tuple[str, int]]:
    offenders = []
    for path in tracked_paths(repo):
        try:
            size = (repo / path).stat().st_size
        except OSError:
            continue
        if size > limit:
            offenders.append((path, size))
    return offenders


def workflow_files(repo: Path) -> list[Path]:
    directory = repo / ".github" / "workflows"
    if not directory.is_dir():
        return []
    return sorted(path for path in directory.iterdir() if path.suffix in WORKFLOW_SUFFIXES)


def _cron_expression(raw: str) -> Optional[str]:
    value = raw.strip()
    if value[:1] in ("'", '"'):
        end = value.find(value[0], 1)
        if end < 0:
            return None
        return value[1:end].strip() or None
    if " #" in value:
        value = value.split(" #", 1)[0]
    return value.strip() or None


def find_workflow_crons(repo: Path) -> dict[str, list[str]]:
    crons = {}
    for path in workflow_files(repo):
        expressions = []
        for line in path.read_text(encoding="utf-8").splitlines():
            if line.lstrip().startswith("#"):
                continue
            match = CRON_LINE_RE.match(line)
            if match is None:
                continue
            expression = _cron_expression(match.group("value"))
            if expression is not None:
                expressions.append(expression)
        if expressions:
            crons[str(path.relative_to(repo))] = expressions
    return crons


def _cron_fields(expression: str) -> Optional[tuple[str, str, str, str, str]]:
    fields = expression.split()
    if len(fields) != 5:
        return None
    weekday = "0" if fields[4] == "7" else fields[4]
    return fields[0], fields[1], fields[2], fields[3], weekday


def _crons_collide(left: str, right: str) -> bool:
    if left == right:
        return True
    left_fields = _cron_fields(left)
    right_fields = _cron_fields(right)
    if left_fields is None or right_fields is None:
        return False
    left_minute, left_hour, left_day, left_month, left_weekday = left_fields
    right_minute, right_hour, right_day, right_month, right_weekday = right_fields
    if left_day != "*" or left_month != "*" or right_day != "*" or right_month != "*":
        return False
    return (
        left_minute == right_minute
        and left_hour == right_hour
        and (left_weekday == right_weekday or left_weekday == "*" or right_weekday == "*")
    )


def find_cron_collisions(repo: Path) -> list[tuple[str, str, str, str]]:
    entries = [
        (path, expression)
        for path, expressions in find_workflow_crons(repo).items()
        for expression in expressions
    ]
    collisions = []
    for index, (left_path, left_cron) in enumerate(entries):
        for right_path, right_cron in entries[index + 1 :]:
            if _crons_collide(left_cron, right_cron):
                collisions.append((left_path, left_cron, right_path, right_cron))
    return collisions


def marker_paths(repo: Path) -> list[str]:
    """The guidance files whose `captured:` markers must lie inside a rule block, relative to the repository."""
    paths = [AGENTS]
    for pattern in MARKER_GLOBS:
        paths.extend(str(path.relative_to(repo)) for path in sorted(repo.glob(pattern)))
    return [
        path
        for path in paths
        if (repo / path).is_file() and not path.startswith(VENDORED_SKILL_PREFIXES)
    ]


def find_misplaced_markers(text: str) -> list[tuple[int, str]]:
    lines = text.split("\n")
    leads = [i for i, line in enumerate(lines) if LEAD_RE.match(line)]
    offenders = []
    for number, line in enumerate(lines):
        for match in re.finditer(re.escape(MARKER), line):
            if match.start() > 0 and line[match.start() - 1] == "`":
                continue
            if line[match.end() :].startswith("`"):
                continue
            if not _marker_is_in_rule_block(lines, leads, number):
                offenders.append((number + 1, line.strip()))
    return offenders


def _marker_is_in_rule_block(lines: list[str], leads: list[int], number: int) -> bool:
    lead = next((i for i in reversed(leads) if i <= number), None)
    return lead is not None and lines[lead].startswith(("- **", "**"))


def formatter_owned(paths: list[str]) -> list[str]:
    return [path for path in paths if path.endswith(FORMATTER_OWNED)]


def run_formatter(repo: Path, command: list[str]) -> subprocess.CompletedProcess:
    try:
        return subprocess.run(command, cwd=repo, capture_output=True, text=True)
    except OSError as error:
        return subprocess.CompletedProcess(command, 1, "", f"error: formatter command cannot be run: {error}")


def check_staged(repo: Path, formatter: list[str]) -> int:
    failures = 0

    paths = staged_paths(repo)
    owned = formatter_owned(paths)
    if owned:
        result = run_formatter(repo, formatter)
        if result.returncode != 0:
            failures += 1
            print("The project's formatter check failed; run the formatter before committing:", file=sys.stderr)
            print(result.stdout.strip(), file=sys.stderr)
            print(result.stderr.strip(), file=sys.stderr)

    for path in find_force_staged_artifacts(repo):
        failures += 1
        print(f"A generated artifact is staged and must not be committed: {path}", file=sys.stderr)

    for path in find_staged_then_edited(repo):
        failures += 1
        print(f"Staged and then edited again, so the index is stale: {path}", file=sys.stderr)

    for entry in find_conflict_markers(repo, staged=True):
        failures += 1
        print(f"A merge conflict marker is staged: {entry}", file=sys.stderr)

    for marker_file in marker_paths(repo):
        if marker_file in paths:
            text = staged_file_text(repo, marker_file)
            if text is not None:
                for line, content in find_misplaced_markers(text):
                    failures += 1
                    print(f"Misplaced captured: marker in {marker_file}:{line}: {content}", file=sys.stderr)

    for change_dir, spec_paths in find_unpaired_archive_sync(repo):
        failures += 1
        print(
            f"An archive move is staged without its spec sync ({change_dir}); "
            "stage the un-staged openspec/specs sync:",
            file=sys.stderr,
        )
        for path in spec_paths:
            print(f"  {path}", file=sys.stderr)

    return failures


def check_markers(repo: Path) -> int:
    paths = marker_paths(repo)
    if AGENTS not in paths:
        print(f"error: {repo / AGENTS} not found", file=sys.stderr)
        return 1
    failures = 0
    for path in paths:
        for line, content in find_misplaced_markers((repo / path).read_text(encoding="utf-8")):
            failures += 1
            print(f"Misplaced captured: marker in {path}:{line}: {content}", file=sys.stderr)
    return failures


def check_tracked_ignored(repo: Path) -> int:
    offenders = find_tracked_ignored(repo)
    for path in offenders:
        print(f"A tracked file is excluded by the repository's ignore rules: {path}", file=sys.stderr)
    return len(offenders)


def check_conflict_markers(repo: Path) -> int:
    offenders = find_conflict_markers(repo)
    for entry in offenders:
        print(f"A merge conflict marker is tracked: {entry}", file=sys.stderr)
    return len(offenders)


def check_executable_bits(repo: Path) -> int:
    offenders = find_non_executable_scripts(repo)
    for path in offenders:
        print(f"A tracked file git runs directly is not executable: {path}", file=sys.stderr)
    return len(offenders)


def check_large_files(repo: Path) -> int:
    try:
        limit = read_max_bytes(repo)
    except ValueError as error:
        print(f"The large-file size limit is misconfigured: {error}", file=sys.stderr)
        return 1
    checked = len(tracked_paths(repo))
    offenders = find_oversized_files(repo, limit)
    for path, size in offenders:
        print(
            "A tracked file exceeds the size limit: {0} ({1} bytes, limit {2} bytes / {3:.0f} KiB)".format(
                path, size, limit, limit / 1024
            ),
            file=sys.stderr,
        )
    if not offenders:
        print(f"Checked {checked} tracked file(s) against the {limit}-byte size limit.")
    return len(offenders)


def check_unique_crons(repo: Path) -> int:
    collisions = find_cron_collisions(repo)
    for left_path, left_cron, right_path, right_cron in collisions:
        print(
            "Two workflow schedules collide: {0} ('{1}') and {2} ('{3}')".format(
                left_path, left_cron, right_path, right_cron
            ),
            file=sys.stderr,
        )
    return len(collisions)


def find_duplicate_review_rounds(repo: Path) -> list[tuple[str, str, int, int]]:
    """The duplicated (unit, round) rows in the review-round ledger, with both line numbers.

    The ledger declares one row per review round and `scripts/experience-analysis.sh` sums a column, so a duplicate
    double-counts a round. Its leading `#` lines are comments, and a row whose pair is absent is not a pair.
    """
    ledger = repo / "docs" / "review-rounds.tsv"
    if not ledger.is_file():
        return []
    first_seen: dict[tuple[str, str], int] = {}
    duplicates: list[tuple[str, str, int, int]] = []
    for number, line in enumerate(ledger.read_text(encoding="utf-8").splitlines(), start=1):
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        fields = line.split("\t")
        if len(fields) < 3:
            continue
        key = (fields[1], fields[2])
        if key in first_seen:
            duplicates.append((key[0], key[1], first_seen[key], number))
        else:
            first_seen[key] = number
    return duplicates


def check_unique_review_rounds(repo: Path) -> int:
    duplicates = find_duplicate_review_rounds(repo)
    for unit, round_number, first, second in duplicates:
        print(
            "Two review-round rows share unit '{0}' round {1}: lines {2} and {3}".format(
                unit, round_number, first, second
            ),
            file=sys.stderr,
        )
    return len(duplicates)


def ledger_rounds(repo: Path) -> dict[str, set[int]]:
    """The recorded round numbers per unit, from the ledger's data rows."""
    ledger = repo / "docs" / "review-rounds.tsv"
    if not ledger.is_file():
        return {}
    rounds: dict[str, set[int]] = {}
    for line in ledger.read_text(encoding="utf-8").splitlines():
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        fields = line.split("\t")
        if len(fields) < 3:
            continue
        if not fields[2].isdigit():
            raise ValueError(
                "unit '{0}' has a non-numeric round '{1}': the ledger's round column must be a number".format(
                    fields[1], fields[2]
                )
            )
        rounds.setdefault(fields[1], set()).add(int(fields[2]))
    return rounds


def acknowledged_gaps(repo: Path) -> set[tuple[str, int]]:
    """The (unit, round) gaps the ledger acknowledges as unrecoverable or as a misfiled deep pass."""
    ledger = repo / "docs" / "review-rounds.tsv"
    if not ledger.is_file():
        return set()
    gaps: set[tuple[str, int]] = set()
    for line in ledger.read_text(encoding="utf-8").splitlines():
        match = ACKNOWLEDGED_GAP_RE.match(line.strip())
        if match:
            gaps.add((match.group("unit"), int(match.group("round"))))
    return gaps


def find_round_gaps(repo: Path) -> list[tuple[str, int]]:
    """The (unit, missing round) pairs that break a unit's contiguity from 1, unacknowledged ones only."""
    acknowledged = acknowledged_gaps(repo)
    gaps: list[tuple[str, int]] = []
    for unit, recorded in sorted(ledger_rounds(repo).items()):
        for number in range(1, max(recorded) + 1):
            if number not in recorded and (unit, number) not in acknowledged:
                gaps.append((unit, number))
    return gaps


def find_unrecorded_expected(repo: Path, expected: Sequence[str]) -> list[tuple[str, int]]:
    """The (unit, round) pairs named by the caller whose row the ledger does not carry."""
    recorded = ledger_rounds(repo)
    missing: list[tuple[str, int]] = []
    for pair in expected:
        unit, _, number = pair.rpartition(":")
        if not unit or not number.isdigit():
            raise ValueError("expected round must be <unit>:<round>, got {0!r}".format(pair))
        if int(number) not in recorded.get(unit, set()):
            missing.append((unit, int(number)))
    return missing


def check_unique_review_rounds(repo: Path, expected: Sequence[str] = ()) -> int:
    """The ledger's duplicate pairs, contiguity gaps, and expected-but-unrecorded rounds, reported to stderr."""
    failures = 0
    for unit, round_number, first, second in find_duplicate_review_rounds(repo):
        print(
            "Two review-round rows share unit '{0}' round {1}: lines {2} and {3}".format(
                unit, round_number, first, second
            ),
            file=sys.stderr,
        )
        failures += 1
    for unit, number in find_round_gaps(repo):
        print(
            "Unit '{0}' is missing round {1}: its rounds must be contiguous from 1".format(unit, number),
            file=sys.stderr,
        )
        failures += 1
    for unit, number in find_unrecorded_expected(repo, expected):
        print(
            "Unit '{0}' has no row for round {1}, the round whose report arrived".format(unit, number),
            file=sys.stderr,
        )
        failures += 1
    return failures


def session_rows(
    repo: Path,
) -> tuple[bool, list[tuple[int, list[str]]], list[tuple[int, list[str]]]]:
    """The ledger's data rows split at the session-fields boundary, plus whether that boundary is present.

    The return is `(present, above, below)`, each row a (line number, fields) pair. A row is in scope once the ledger's
    own boundary comment has been read; the rows above it predate the fields, so the continuity verification exempts
    them rather than requiring an identifier the record cannot evidence — which is only sound while those rows carry the
    pre-fields width.
    """
    ledger = repo / "docs" / "review-rounds.tsv"
    if not ledger.is_file():
        return False, [], []
    above: list[tuple[int, list[str]]] = []
    below: list[tuple[int, list[str]]] = []
    seen_boundary = False
    for number, line in enumerate(ledger.read_text(encoding="utf-8").splitlines(), start=1):
        if line.startswith(SESSION_FIELDS_MARKER):
            seen_boundary = True
            continue
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        (below if seen_boundary else above).append((number, line.split("\t")))
    return seen_boundary, above, below


def find_session_continuity_violations(repo: Path) -> list[str]:
    """The reviewer-session continuity violations the ledger carries, each as a message naming its line.

    The rule is per unit: the first row below the boundary creates the session, a later row whose identifier equals its
    preceding row's continues it, and a row whose identifier differs replaces it and must name one of the recorded
    reasons. Distinct units may name distinct sessions. The verification validates the recorded fields; it does not
    attest that a replacement's reason occurred.
    """
    present, above, rows = session_rows(repo)
    if not present:
        return ["the review-round ledger carries no '{0}' boundary comment".format(SESSION_FIELDS_MARKER)]
    violations: list[str] = []
    for number, fields in above:
        if len(fields) != SESSION_PRE_FIELDS:
            violations.append(
                "line {0}: a row above the session-fields boundary carries {1} field(s), not the {2} it carried before "
                "the fields existed, so the boundary was moved past it".format(
                    number, len(fields), SESSION_PRE_FIELDS
                )
            )
    previous: dict[str, str] = {}
    for number, fields in rows:
        if len(fields) != SESSION_ROW_FIELDS:
            violations.append(
                "line {0}: a row below the session-fields boundary carries {1} field(s), not {2}".format(
                    number, len(fields), SESSION_ROW_FIELDS
                )
            )
            continue
        unit, round_number = fields[1], fields[2]
        session, transition, reason = fields[6], fields[7], fields[8]
        if not session.strip() or session.strip() == "-":
            violations.append(
                "line {0}: unit '{1}' round {2} carries no session identifier".format(
                    number, unit, round_number
                )
            )
            continue
        session = session.strip()
        if transition not in SESSION_TRANSITIONS:
            violations.append(
                "line {0}: unit '{1}' round {2} carries transition '{3}', not one of {4}".format(
                    number, unit, round_number, transition, ", ".join(SESSION_TRANSITIONS)
                )
            )
            continue
        if reason not in ("", "-") and transition != SESSION_REPLACED:
            violations.append(
                "line {0}: unit '{1}' round {2} carries reason '{3}' with the '{4}' transition, which is not a "
                "replacement".format(number, unit, round_number, reason, transition)
            )
        prior = previous.get(unit)
        if prior is None:
            if transition != SESSION_CREATED:
                violations.append(
                    "line {0}: unit '{1}' round {2} is the unit's first recorded row, so its transition must be "
                    "'{3}', not '{4}'".format(number, unit, round_number, SESSION_CREATED, transition)
                )
        elif session == prior:
            if transition != SESSION_CONTINUED:
                violations.append(
                    "line {0}: unit '{1}' round {2} continues session '{3}', so its transition must be '{4}', not "
                    "'{5}'".format(number, unit, round_number, session, SESSION_CONTINUED, transition)
                )
        elif transition != SESSION_REPLACED:
            violations.append(
                "line {0}: unit '{1}' round {2} names session '{3}' where its preceding row named '{4}', without a "
                "'{5}' transition".format(number, unit, round_number, session, prior, SESSION_REPLACED)
            )
        elif reason in ("", "-") or reason not in SESSION_REPLACEMENT_REASONS:
            violations.append(
                "line {0}: unit '{1}' round {2} replaces a session with reason '{3}', not one of {4}".format(
                    number, unit, round_number, reason, ", ".join(SESSION_REPLACEMENT_REASONS)
                )
            )
        previous[unit] = session
    return violations


def check_session_continuity(repo: Path) -> int:
    violations = find_session_continuity_violations(repo)
    for violation in violations:
        print("Review-round session continuity: {0}".format(violation), file=sys.stderr)
    return len(violations)


# The mode group's flags, declared beside their registration so the mode-combination control can derive its
# forms from this list rather than repeating them.
MODE_FLAGS = (
    "--staged",
    "--markers",
    "--tracked-ignored",
    "--conflict-markers",
    "--executable-bits",
    "--unique-crons",
    "--unique-rounds",
    "--session-continuity",
    "--large-files",
)


def build_parser() -> argparse.ArgumentParser:
    """The script's argument parser, so a test can read the mode group it declares."""
    parser = argparse.ArgumentParser(
        description=(
            "Check commit hygiene over the staged set, AGENTS.md markers, the tracked set, the workflow cron "
            "schedules, the review-round ledger, or a file's size."
        )
    )
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--staged", action="store_true", help="check the staged set (the pre-commit guard)")
    mode.add_argument("--markers", action="store_true", help="check captured: marker placement in the guidance files")
    mode.add_argument(
        "--tracked-ignored", action="store_true", help="check the tracked set excludes every ignored path"
    )
    mode.add_argument(
        "--conflict-markers", action="store_true", help="check the tracked set carries no merge conflict marker"
    )
    mode.add_argument(
        "--executable-bits", action="store_true", help="check tracked scripts carry the executable bit"
    )
    mode.add_argument(
        "--unique-crons", action="store_true", help="check no two workflow cron schedules collide"
    )
    mode.add_argument(
        "--unique-rounds", action="store_true", help="check the review-round ledger carries one row per unit and round"
    )
    parser.add_argument(
        "--expect",
        action="append",
        default=[],
        metavar="UNIT:ROUND",
        help="a round whose row must be recorded, as <unit>:<round> (repeatable, with --unique-rounds)",
    )
    mode.add_argument(
        "--session-continuity",
        action="store_true",
        help="check the review-round ledger records each round's reviewer session and its transition",
    )
    mode.add_argument(
        "--large-files", action="store_true", help="check no tracked file exceeds the configured size limit"
    )
    parser.add_argument("--repo", default=str(REPO_ROOT), help="repository root (default: the script's parent)")
    return parser


def main(argv: Sequence[str] = ()) -> int:
    parser = build_parser()
    parser.add_argument(
        "--formatter",
        nargs="+",
        default=list(DEFAULT_FORMATTER),
        help="the formatter check command (default: ./gradlew spotlessCheck)",
    )
    args = parser.parse_args(list(argv) or None)

    repo = Path(args.repo)
    if args.expect and not args.unique_rounds:
        print("--expect needs --unique-rounds: it names a round the ledger must carry", file=sys.stderr)
        return 1
    if args.markers:
        return 1 if check_markers(repo) else 0
    if args.tracked_ignored:
        return 1 if check_tracked_ignored(repo) else 0
    if args.conflict_markers:
        return 1 if check_conflict_markers(repo) else 0
    if args.executable_bits:
        return 1 if check_executable_bits(repo) else 0
    if args.unique_crons:
        return 1 if check_unique_crons(repo) else 0
    if args.unique_rounds:
        try:
            return 1 if check_unique_review_rounds(repo, args.expect) else 0
        except ValueError as error:
            print(str(error), file=sys.stderr)
            return 1
    if args.session_continuity:
        return 1 if check_session_continuity(repo) else 0
    if args.large_files:
        return 1 if check_large_files(repo) else 0
    return 1 if check_staged(repo, args.formatter) else 0


if __name__ == "__main__":
    sys.exit(main())
