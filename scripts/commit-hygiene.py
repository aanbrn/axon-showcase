#!/usr/bin/env python3
"""Check commit hygiene: the staged set, `captured:` marker placement, and the tracked set.

The `--staged` mode is the pre-commit guard. It refuses a commit whose staged set the project's formatter would
rewrite, that force-stages a generated artifact, that stages a path and then edits it again (leaving the index stale),
that carries a merge conflict marker, or that misplaces a `captured:` marker in `AGENTS.md`. The `--markers` mode checks
marker placement in the working-tree `AGENTS.md`; the `--tracked-ignored`, `--conflict-markers`, and `--executable-bits`
modes each verify the tracked set, for the build. Stdlib only.
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
LEAD_RE = re.compile(r"^(- |\*\*)")
CONFLICT_MARKER_RE = r"^(<<<<<<< |>>>>>>> )"
VENDORED_SKILL_PREFIXES = (".opencode/skills/axon4to5-", ".opencode/skills/openspec-")


def git(repo: Path, *args: str) -> subprocess.CompletedProcess:
    return subprocess.run(("git", *args), cwd=repo, capture_output=True, text=True)


def staged_paths(repo: Path) -> list[str]:
    result = git(repo, "diff", "--cached", "--name-only", "--diff-filter=ACMR")
    return [line for line in result.stdout.splitlines() if line]


def staged_file_text(repo: Path, path: str) -> Optional[str]:
    result = git(repo, "show", f":{path}")
    return result.stdout if result.returncode == 0 else None


def find_staged_then_edited(repo: Path) -> list[str]:
    offenders = []
    for line in git(repo, "status", "--porcelain").stdout.splitlines():
        if len(line) < 3:
            continue
        index_status, worktree_status = line[0], line[1]
        if index_status in "MARC" and worktree_status in "MADRC":
            path = line[3:]
            if " -> " in path:
                path = path.split(" -> ", 1)[1]
            offenders.append(path.strip('"'))
    return offenders


def find_tracked_ignored(repo: Path) -> list[str]:
    result = git(repo, "ls-files", "--cached", "--ignored", "--exclude-standard")
    return [line for line in result.stdout.splitlines() if line]


def find_force_staged_artifacts(repo: Path) -> list[str]:
    ignored = set(find_tracked_ignored(repo))
    return [path for path in staged_paths(repo) if path in ignored]


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


def main(argv: Sequence[str] = ()) -> int:
    parser = argparse.ArgumentParser(
        description="Check commit hygiene over the staged set, AGENTS.md markers, or the tracked set."
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
    return 1 if check_staged(repo, args.formatter) else 0


if __name__ == "__main__":
    sys.exit(main())
