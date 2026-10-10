#!/usr/bin/env python3
"""Check commit hygiene: the staged set, `captured:` marker placement, and the tracked set.

The `--staged` mode is the pre-commit guard. It refuses a commit whose staged set the project's formatter would
rewrite, that force-stages a generated artifact, that stages a path and then edits it again (leaving the index stale),
that carries a merge conflict marker, that misplaces a `captured:` marker in `AGENTS.md`, or that stages an
`openspec/changes/archive/**` change dir while leaving its `openspec/specs/**` sync unstaged. The `--markers` mode
checks marker placement in the working-tree `AGENTS.md`; the `--tracked-ignored`, `--conflict-markers`, and
`--executable-bits` modes each verify the tracked set for the build, `--unique-crons` verifies the workflow `cron`
schedules do not collide, and `--large-files` verifies no tracked file exceeds the configured size limit. Stdlib only.
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path
from typing import Optional, Sequence

REPO_ROOT = Path(__file__).resolve().parent.parent
AGENTS = "AGENTS.md"
FORMATTER_OWNED = (".java", ".kt", ".kts", ".md", ".json")
DEFAULT_FORMATTER = ("./gradlew", "spotlessCheck")
MARKER = "captured:"
LARGE_FILES_CONFIG = "config/commit-hygiene/large-files.properties"
LEAD_RE = re.compile(r"^(- |\*\*)")
CONFLICT_MARKER_RE = r"^(<<<<<<< |>>>>>>> )"
VENDORED_SKILL_PREFIXES = (".opencode/skills/axon4to5-", ".opencode/skills/openspec-")
ARCHIVE_PREFIX = "openspec/changes/archive/"
SPECS_PREFIX = "openspec/specs/"
CRON_LINE_RE = re.compile(r"^\s*-\s*cron:\s*(?P<value>.+?)\s*$")
WORKFLOW_SUFFIXES = (".yml", ".yaml")


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

    if AGENTS in paths:
        text = staged_file_text(repo, AGENTS)
        if text is not None:
            for line, content in find_misplaced_markers(text):
                failures += 1
                print(f"Misplaced captured: marker in {AGENTS}:{line}: {content}", file=sys.stderr)

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
    agents = repo / AGENTS
    if not agents.exists():
        print(f"error: {agents} not found", file=sys.stderr)
        return 1
    offenders = find_misplaced_markers(agents.read_text(encoding="utf-8"))
    for line, content in offenders:
        print(f"Misplaced captured: marker in {AGENTS}:{line}: {content}", file=sys.stderr)
    return len(offenders)


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


def main(argv: Sequence[str] = ()) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Check commit hygiene over the staged set, AGENTS.md markers, the tracked set, or the workflow cron "
            "schedules (--unique-crons)."
        )
    )
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--staged", action="store_true", help="check the staged set (the pre-commit guard)")
    mode.add_argument("--markers", action="store_true", help="check captured: marker placement in AGENTS.md")
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
        "--large-files", action="store_true", help="check no tracked file exceeds the configured size limit"
    )
    parser.add_argument("--repo", default=str(REPO_ROOT), help="repository root (default: the script's parent)")
    parser.add_argument(
        "--formatter",
        nargs="+",
        default=list(DEFAULT_FORMATTER),
        help="the formatter check command (default: ./gradlew spotlessCheck)",
    )
    args = parser.parse_args(list(argv) or None)

    repo = Path(args.repo)
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
    if args.large_files:
        return 1 if check_large_files(repo) else 0
    return 1 if check_staged(repo, args.formatter) else 0


if __name__ == "__main__":
    sys.exit(main())
