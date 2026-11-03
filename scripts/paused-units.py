#!/usr/bin/env python3
"""List the units whose record says they are paused, across the repository's registered worktrees.

Read-only and stdlib only. The resolver enumerates each registered tree's assignment — the unit record whose `path` and
`branch` name that tree — and reports the records whose state is `paused`, the records whose state is unknown (a legacy
`tasks.md` record, or one without a state), and the errors that make the enumeration incomplete.

Exit status: 0 when the enumeration is complete, whatever the number of candidates; 1 when it is incomplete. An
incomplete enumeration never establishes the set of paused units, so a caller must not select one from it.

The output is one JSON document:

    {"complete": true,
     "selectable": true,
     "candidates": [{"unit": "...", "tree": "...", "branch": "...", "record": "...", "state": "paused"}],
     "unknown": [{"unit": "...", "tree": "...", "record": "...", "reason": "..."}],
     "assignments": [{"unit": "...", "tree": "...", "branch": "...", "record": "...", "state": "active"}],
     "errors": ["..."]}

For the bare form, `selectable` is true only when the enumeration is complete, nothing is unknown, and exactly one unit
is paused; a caller must not choose a unit from a document where it is false. With `--unit`, `selectable` means exactly
one tree assigns the named unit, and `candidates` still lists only a paused unit.
"""

import argparse
import importlib.util
import json
import re
import subprocess
import sys
from pathlib import Path

UNIT_RECORD_DIR = "docs/units"
UNIT_RECORD_BASENAME = "unit.md"
CLAIM_NAME = "axon-showcase-change"
CLAIM_NONE = "none"
STATE_LINE_RE = re.compile(r"^State:\s*(\S+)\s*$")
LEGACY_MARKER_RE = re.compile(r"^`?Workspace:")
PAUSED = "paused"
STATES = ("active", "paused", "closed")

_CHECKER_PATH = Path(__file__).resolve().parent / "commit-hygiene.py"
_spec = importlib.util.spec_from_file_location("commit_hygiene", _CHECKER_PATH)
commit_hygiene = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(commit_hygiene)


def git(repo: Path, *arguments: str) -> str:
    """Run git in `repo` and return its output, raising on failure."""
    result = subprocess.run(("git",) + arguments, cwd=repo, capture_output=True, text=True)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "git {0} failed".format(" ".join(arguments)))
    return result.stdout


def registered_trees(repo: Path) -> list[tuple[Path, str]]:
    """Each registered worktree with its checked-out branch, or an empty string for a detached head."""
    output = git(repo, "worktree", "list", "--porcelain")
    trees: list[tuple[Path, str]] = []
    path = None
    branch = ""
    for line in output.splitlines() + [""]:
        if line.startswith("worktree "):
            path = Path(line[len("worktree ") :])
            branch = ""
        elif line.startswith("branch "):
            ref = line[len("branch ") :]
            branch = ref[len("refs/heads/") :] if ref.startswith("refs/heads/") else ref
        elif not line and path is not None:
            trees.append((path, branch))
            path = None
    return trees


def record_state(text: str) -> str | None:
    """The record's state, or None when it carries none or one this resolver does not know."""
    for line in text.splitlines():
        match = STATE_LINE_RE.match(line)
        if match:
            state = match.group(1).strip("`").strip()
            return state if state in STATES else None
    return None


def claim_of(tree: Path) -> str | None:
    """The tree's claim value, or None when no claim file exists."""
    resolved = Path(git(tree, "rev-parse", "--git-path", CLAIM_NAME).strip())
    path = resolved if resolved.is_absolute() else tree / resolved
    if not path.is_file():
        return None
    lines = [line.strip() for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]
    if not lines:
        return None
    return lines[0] if len(lines) == 1 else ""


def records_of(tree: Path) -> list[tuple[str, str, str | None]]:
    """Each record in the tree as (unit, text, parse reason), judged by its own directory's name."""
    root = tree / UNIT_RECORD_DIR
    if not root.is_dir():
        return []
    found: list[tuple[str, str, str | None]] = []
    for entry in sorted(root.iterdir()):
        path = entry / UNIT_RECORD_BASENAME
        if not (entry.is_dir() and path.is_file()):
            continue
        text = path.read_text(encoding="utf-8")
        fields, reason = commit_hygiene.parse_workspace_record(text)
        if reason is not None:
            found.append((entry.name, text, reason))
        elif fields["unit"] != entry.name:
            found.append((entry.name, text, "'unit' names '{0}', not the directory's '{1}'".format(fields["unit"], entry.name)))
        else:
            found.append((entry.name, text, None))
    return found


def claim_and_branch(tree: Path, branch: str) -> tuple[str | None, str]:
    """The tree's claim and branch, with a multi-line claim an error and a blank one read as none."""
    claim = claim_of(tree)
    if claim == "":
        raise RuntimeError("the claim of {0} holds more than one line".format(tree))
    return claim, branch


def resolve(repo: Path) -> dict:
    """The enumeration: candidates, unknown records, assignments, errors, and whether it is complete."""
    candidates: list[dict] = []
    unknown: list[dict] = []
    assignments: list[dict] = []
    errors: list[str] = []
    for tree, branch in registered_trees(repo):
        tree = tree.resolve()
        if not tree.is_dir():
            errors.append("the registered tree {0} does not exist; prune it if it is gone".format(tree))
            continue
        try:
            claim, branch = claim_and_branch(tree, branch)
        except RuntimeError as error:
            errors.append(str(error))
            continue
        unit: str | None = None
        if claim is not None and claim != CLAIM_NONE:
            unit = claim
        elif branch and branch in commit_hygiene.change_names(tree):
            unit = branch
        try:
            records = records_of(tree)
        except OSError as error:
            errors.append("cannot read the records of {0}: {1}".format(tree, error))
            continue
        parsed: dict[str, tuple[str, str | None]] = {name: (text, reason) for name, text, reason in records}
        matching = []
        for name, (text, reason) in parsed.items():
            if reason is not None:
                errors.append("the record of '{0}' in {1} does not parse: {2}".format(name, tree, reason))
                continue
            fields, _ = commit_hygiene.parse_workspace_record(text)
            if Path(fields["path"]).resolve() == tree and fields["branch"] == branch:
                matching.append(name)
        if len(matching) > 1:
            errors.append(
                "several records assign {0} (branch '{1}'): {2}".format(tree, branch, ", ".join(sorted(matching)))
            )
            continue
        if matching and unit is None:
            unit = matching[0]
        elif matching and unit is not None and matching[0] != unit:
            errors.append(
                "the record of {0} names '{1}' while the tree is assigned to '{2}'".format(tree, matching[0], unit)
            )
            continue
        if unit is None and claim == CLAIM_NONE and branch not in ("main", "master"):
            unit = branch
        if unit is None:
            continue
        record = tree / UNIT_RECORD_DIR / unit / UNIT_RECORD_BASENAME
        if not record.is_file():
            tasks = tree / "openspec" / "changes" / unit / "tasks.md"
            if tasks.is_file() and any(
                LEGACY_MARKER_RE.match(line) for line in tasks.read_text(encoding="utf-8").splitlines()
            ):
                unknown.append(
                    {
                        "unit": unit,
                        "tree": str(tree),
                        "branch": branch,
                        "record": str(tasks),
                        "reason": "a legacy record in tasks.md",
                    }
                )
                continue
            errors.append("the record of the unit this tree is assigned to is missing: {0}".format(record))
            continue
        text, reason = parsed.get(unit, ("", "its record does not parse"))
        if reason is not None:
            errors.append("the record of '{0}' in {1} does not parse: {2}".format(unit, tree, reason))
            continue
        fields, _ = commit_hygiene.parse_workspace_record(text)
        if Path(fields["path"]).resolve() != tree or fields["branch"] != branch:
            errors.append(
                "the record of '{0}' names {1} and branch '{2}', not this tree and branch '{3}'".format(
                    unit, fields["path"], fields["branch"], branch
                )
            )
            continue
        state = record_state(text)
        assignments.append({"unit": unit, "tree": str(tree), "branch": branch, "record": str(record), "state": state})
        if state is None:
            unknown.append(
                {
                    "unit": unit,
                    "tree": str(tree),
                    "record": str(record),
                    "reason": "no state, or one this resolver does not know",
                }
            )
        elif state == PAUSED:
            candidates.append(
                {"unit": unit, "tree": str(tree), "branch": branch, "record": str(record), "state": state}
            )
    return {
        "complete": not errors,
        "selectable": not errors and not unknown and len(candidates) == 1,
        "candidates": sorted(candidates, key=lambda entry: entry["unit"]),
        "unknown": sorted(unknown, key=lambda entry: entry["unit"]),
        "assignments": sorted(assignments, key=lambda entry: entry["unit"]),
        "errors": errors,
    }


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=__doc__.splitlines()[0],
        epilog="Exit status: 0 when the enumeration is complete, whatever the number of candidates; 1 when it is not."
        + __doc__.split("The output is one JSON document:")[1],
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument(
        "--repo",
        default=str(Path(__file__).resolve().parent.parent),
        help="the repository whose registered trees are enumerated (default: this script's repository)",
    )
    parser.add_argument("--unit", help="resolve one named unit instead of listing the paused set")
    arguments = parser.parse_args(argv)
    try:
        document = resolve(Path(arguments.repo).resolve())
    except RuntimeError as error:
        print("paused-units: {0}".format(error), file=sys.stderr)
        return 1
    if arguments.unit:
        assigned = [entry for entry in document["assignments"] if entry["unit"] == arguments.unit]
        legacy = [entry for entry in document["unknown"] if entry["unit"] == arguments.unit]
        document["candidates"] = [entry for entry in document["candidates"] if entry["unit"] == arguments.unit]
        document["unknown"] = legacy
        if not assigned and len(legacy) == 1:
            assigned = [dict(legacy[0], state=None, legacy=True)]
            document["unknown"] = []
        document["assignments"] = assigned
        if len(assigned) > 1:
            document["errors"] = document["errors"] + [
                "'{0}' is assigned by {1} trees".format(arguments.unit, len(assigned))
            ]
        elif not assigned and len(legacy) > 1:
            document["errors"] = document["errors"] + [
                "'{0}' is assigned by {1} trees".format(arguments.unit, len(legacy))
            ]
        elif not assigned and not legacy:
            document["errors"] = document["errors"] + ["no registered tree assigns '{0}'".format(arguments.unit)]
        document["complete"] = not document["errors"]
        document["selectable"] = len(assigned) == 1 and document["complete"]
    print(json.dumps(document, indent=2, sort_keys=True))
    return 0 if document["complete"] else 1


if __name__ == "__main__":
    sys.exit(main())
