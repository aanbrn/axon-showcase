"""Scan an authored diff for the claim shapes the pre-write pass's claims table must seed.

The pre-write derivation pass emits a claims table -- each fact the change states about its evidence, with its source or
deriving command -- and its rows are author-seeded, so a fact the author never noticed stating cannot become a row.
This tool reads the added lines of an authored set (the diff against a base ref, plus the untracked, non-ignored files),
and optionally one supplementary text file (`--include`, e.g. a unit's saved draft pull-request body, whose claims table
has no other home), and prints one candidate per line for the four shapes the review loop keeps catching:

  count      a numeral with a referent (e.g. "12 files", "0.80 baseline")
  absolute   an absolute quantifier or absence claim (only, every, all, never, always, none, no, nothing) and the
             definite singular (the one, the sole, the single)
  cross-ref  a reference to another artifact (a repo path, #NNN, owner/repo#NNN, a root document)
  path       a backticked path

Each candidate is a lead, not a finding: the author resolves it into a claims-table row or records its dismissal with
the reason. The tool never fails on a candidate -- it is an authoring aid, not a gate -- but an explicitly requested
input it cannot read is a failure, because an empty candidate list and a failed scan are different outcomes. Empty
output means no recognized candidate in the scanned inputs, never that the claims table is complete. Stdlib only.
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path
from typing import List, Optional, Sequence, Tuple

Claim = Tuple[str, int, str, str]

HUNK_HEADER = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,\d+)? @@")
NEW_FILE = re.compile(r"^\+\+\+ b/(.+)$")
CODE_SPAN = re.compile(r"`([^`]*)`")

PATH_EXTENSION = re.compile(
    r"\.(md|kts|py|sh|ya?ml|json|toml|xml|txt|java|kt|ts|tsx|js|jsx|properties|conf|gradle)$",
    re.IGNORECASE,
)
CROSS_REFERENCE = re.compile(
    r"(?:[\w.-]+/[\w./-]+|[\w.-]+#\d+|#\d+|(?<![\w/])AGENTS\.md|(?<![\w/])README\.md|(?<![\w/])SECURITY\.md)"
)
ABSOLUTE = re.compile(
    r"\b(?:only|every|all|never|always|none|no|nothing)\b|\bthe\s+(?:one|sole|single)\b", re.IGNORECASE
)
COUNT = re.compile(r"(?<![\w.#-])(\d[\d,]*(?:\.\d+)?)`?\s*([A-Za-z][\w-]*)")


def shapes(text: str) -> List[Tuple[str, str]]:
    """The claim candidates in one line of prose, as (kind, matched text) pairs."""
    if text.strip().rstrip(".").lower() == "none":
        return []
    candidates: List[Tuple[str, str]] = []
    for span in CODE_SPAN.finditer(text):
        token = span.group(1)
        if PATH_EXTENSION.search(token) or "/" in token.strip("/"):
            candidates.append(("path", token))
    prose = CODE_SPAN.sub(" ", text)
    for match in CROSS_REFERENCE.finditer(prose):
        candidates.append(("cross-ref", match.group(0)))
    for match in ABSOLUTE.finditer(prose):
        candidates.append(("absolute", match.group(0)))
    for match in COUNT.finditer(prose):
        candidates.append(("count", match.group(1) + " " + match.group(2)))
    return candidates


def _names_a_modified_path(text: str, modified: List[str]) -> bool:
    """Whether a candidate merely names a file the diff itself modifies, which is not a claim about prior evidence."""
    return any(text == path or path.endswith("/" + text) for path in modified)


def _modified_paths(diff_text: str) -> List[str]:
    """The paths the diff itself modifies (the new side of each file header)."""
    modified: List[str] = []
    for raw in diff_text.splitlines():
        if raw.startswith("+++"):
            match = NEW_FILE.match(raw)
            if match:
                modified.append(match.group(1))
    return modified


def find_claims(diff_text: str, known: Sequence[str] = ()) -> List[Claim]:
    """The claim candidates in a unified diff's added lines, with their new-side line numbers.

    ``known`` adds paths the authored set creates outside the diff (its untracked files), so a candidate naming one is
    dropped exactly as a candidate naming a modified path is.
    """
    modified = _modified_paths(diff_text) + list(known)
    claims: List[Claim] = []
    path: Optional[str] = None
    new_line = 0
    for raw in diff_text.splitlines():
        if raw.startswith("+++"):
            match = NEW_FILE.match(raw)
            path = match.group(1) if match else None
            continue
        if raw.startswith("@@"):
            match = HUNK_HEADER.match(raw)
            new_line = int(match.group(1)) if match else 0
            continue
        if raw.startswith("+"):
            if path:
                for kind, text in shapes(raw[1:]):
                    if kind == "path" and _names_a_modified_path(text, modified):
                        continue
                    claims.append((path, new_line, kind, text))
            new_line += 1
        elif raw.startswith("-"):
            continue
        elif raw.startswith(" "):
            new_line += 1
    return claims


def scan_file(repo: Path, path: str, known: Sequence[str] = (), required: bool = False) -> List[Claim]:
    """Every line of a file outside the diff, as all-added candidates.

    ``known`` names the paths the authored set itself adds or modifies, so a candidate that merely names one is dropped
    as it is in a diff (naming a file the unit writes is not a claim about prior evidence). ``required`` marks an
    explicitly requested input: its read failure is raised instead of read as "no candidates", while an untracked file's
    failure stays swamped because the caller never named it.
    """
    claims: List[Claim] = []
    try:
        text = (repo / path).read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError) as failure:
        if required:
            raise ValueError("{0}: {1}".format(path, failure))
        return claims
    for number, line in enumerate(text.splitlines(), start=1):
        for kind, matched in shapes(line):
            if kind == "path" and not required and _names_a_modified_path(matched, known):
                continue
            claims.append((path, number, kind, matched))
    return claims


def _git(repo: Path, *args: str) -> str:
    result = subprocess.run(
        ["git", *args], cwd=str(repo), check=True, capture_output=True, text=True
    )
    return result.stdout


def scan(repo: Path, base: str) -> List[Claim]:
    """The candidates in the diff against ``base`` plus the untracked, non-ignored files."""
    diff_text = _git(repo, "diff", "--no-color", base, "--")
    untracked = _git(repo, "ls-files", "--others", "--exclude-standard").splitlines()
    known = _modified_paths(diff_text) + untracked
    claims = find_claims(diff_text, untracked)
    for path in untracked:
        claims.extend(scan_file(repo, path, known))
    return claims


def main(argv: Sequence[str] = ()) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Scan an authored diff's added lines, and optionally one included text file, for claim shapes (count, "
            "absolute, cross-ref, path), one candidate per line, for the pre-write pass's claims table."
        )
    )
    parser.add_argument("--base", default="origin/main", help="the ref to diff against (default: origin/main)")
    parser.add_argument("--repo", default=".", help="the repository to scan (default: the current directory)")
    parser.add_argument(
        "--include",
        help=(
            "scan this file's lines as candidates beside the ordinary scan, e.g. a unit's saved draft pull-request "
            "body; a path-shaped candidate here is kept even when it names a modified path, and an unreadable one is "
            "a failure"
        ),
    )
    parser.add_argument(
        "--diff-file",
        help=(
            "read a unified diff from this file instead of running git "
            "(used by the tests; it scans no untracked files)"
        ),
    )
    args = parser.parse_args(argv)
    repo = Path(args.repo)
    claims: List[Claim] = []
    try:
        if args.diff_file:
            claims = find_claims(Path(args.diff_file).read_text(encoding="utf-8"))
        else:
            claims = scan(repo, args.base)
        if args.include:
            claims = claims + scan_file(repo, args.include, required=True)
    except (OSError, ValueError) as failure:
        for path, line, kind, text in claims:
            print("{0}:{1}  [{2}]  {3}".format(path, line, kind, text))
        print("the scan failed: {0}".format(failure), file=sys.stderr)
        return 1
    for path, line, kind, text in claims:
        print("{0}:{1}  [{2}]  {3}".format(path, line, kind, text))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
