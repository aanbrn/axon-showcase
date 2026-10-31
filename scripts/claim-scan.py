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

The opt-in `--verify <table>` mode is different: it executes a supported row's command and compares the Result's
explicit literals, so it can fail. It runs only when invoked; the default scan and `check` execute nothing.
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


VERIFY_GIT_READ = {"show", "grep", "diff", "log", "rev-parse", "ls-files", "merge-base", "cat-file"}
VERIFY_GIT_WRITE = {"push", "commit", "reset", "clean", "checkout", "tag", "config"}
VERIFY_DEFERRED = {"awk", "gh", "opencode", "openspec", "perl", "python3"}
VERIFY_SED_WRITE = re.compile(r"(^|\s)(-i|w|W|e)(\s|$)|s/.*/[^/]*/[we]")


def verify_cells(line: str) -> List[str]:
    """The cells of one markdown table row, with the table's pipe escapes left raw."""
    parts = re.split(r"(?<!\\)\|", line.strip())
    return [part.strip() for part in parts[1:-1]]


def verify_literals(cell: str) -> List[str]:
    """The backticked literals a cell states."""
    return re.findall(r"`([^`]+)`", cell)


def has_redirection(command: str) -> bool:
    """True when the command carries a `>` outside a quoted span."""
    quote = ""
    escaped = False
    for char in command:
        if escaped:
            escaped = False
            continue
        if char == "\\" and quote != "'":
            escaped = True
            continue
        if quote:
            if char == quote:
                quote = ""
        elif char in {"'", '"'}:
            quote = char
        elif char == ">":
            return True
    return False


def unquoted_tokens(command: str) -> list:
    """The command's shell words, each paired with whether it was quoted."""
    tokens = []
    current = ""
    quoted = False
    quote = ""
    escaped = False
    for char in command:
        if escaped:
            current += char
            escaped = False
            continue
        if char == "\\" and quote != "'":
            current += char
            escaped = True
            continue
        if quote:
            if char == quote:
                quote = ""
            else:
                current += char
        elif char in {"'", '"'}:
            quote = char
            quoted = True
        elif char.isspace():
            if current or quoted:
                tokens.append((current, quoted))
            current = ""
            quoted = False
        else:
            current += char
    if current or quoted:
        tokens.append((current, quoted))
    return tokens


def verify_eligible(source: str, result: str) -> Tuple[str, object]:
    """Classify one row: a (status, command-and-literals-or-reason) pair."""
    if "\\|" in source:
        return "advisory", "ambiguous pipe escape"
    spans = re.findall(r"`([^`]+)`", source)
    if len(spans) == 1:
        command = spans[0]
    elif len(spans) == 2 and "piped through" in source:
        command = " | ".join(spans)
    else:
        return "advisory", "the source must be one command"
    if command.split()[0] in {"for", "while"}:
        return "advisory", "shell loop"
    if has_redirection(command):
        return "unsafe", "redirection"
    for part in command.split(" | "):
        part_tokens = part.split()
        if not part_tokens:
            continue
        first_part = part_tokens[0]
        if first_part == "git":
            if len(part_tokens) < 2 or part_tokens[1] not in VERIFY_GIT_READ or "--output" in part:
                return "unsafe", "git {0}".format(part_tokens[1] if len(part_tokens) > 1 else "")
        elif first_part == "sed":
            if VERIFY_SED_WRITE.search(part):
                return "unsafe", "sed write form"
            return "advisory", "working-tree sed"
        elif first_part in {"grep", "wc"}:
            return "advisory", "working-tree {0}".format(first_part)
        elif first_part in VERIFY_DEFERRED:
            return "external", first_part
        else:
            return "unsafe", first_part
    tokens = command.split()
    first = tokens[0]
    if first == "git":
        sub = tokens[1] if len(tokens) > 1 else ""
        if sub in VERIFY_GIT_WRITE or "--output" in command:
            return "unsafe", "git write form"
        if sub not in VERIFY_GIT_READ:
            return "unsafe", "git {0}".format(sub)
        if sub == "diff":
            return "advisory", "working-tree diff"
        if sub == "grep":
            tokens = unquoted_tokens(command.split(" -- ")[0])
            quoted_positions = [index for index, (_, quoted) in enumerate(tokens) if quoted]
            if not quoted_positions:
                return "advisory", "working-tree grep"
            last = quoted_positions[-1]
            if (
                last + 2 != len(tokens)
                or tokens[last + 1][1]
                or not re.fullmatch(r"[0-9a-f]{7,40}", tokens[last + 1][0])
            ):
                return "advisory", "working-tree grep"
    elif first in {"grep", "sed", "wc"}:
        if first == "sed" and VERIFY_SED_WRITE.search(command):
            return "unsafe", "sed write form"
        return "advisory", "working-tree {0}".format(first)
    elif first in VERIFY_DEFERRED:
        return "external", first
    else:
        return "unsafe", first
    literals = verify_literals(result)
    if not literals:
        return "advisory", "no explicit literal"
    remainder = result
    for literal in literals:
        remainder = remainder.replace("`" + literal + "`", "")
    if remainder.strip(" ,\t"):
        return "advisory", "the result is not literal-only"
    return "candidate", (command, literals)


def verify_run(command: str, repo: Path) -> Tuple[str, Optional[str]]:
    """Run one eligible command; return its stdout, or None and an error reason."""
    try:
        completed = subprocess.run(
            command, shell=True, cwd=str(repo), capture_output=True, text=True, timeout=10
        )
    except subprocess.TimeoutExpired:
        return "", "timeout"
    except OSError as failure:
        return "", str(failure)
    if completed.returncode != 0:
        return completed.stdout, "exit {0}".format(completed.returncode)
    return completed.stdout, None


def literal_matches(literal: str, output: str) -> bool:
    """True when the literal occurs in the output; a numeric or locator literal matches at a boundary."""
    if re.fullmatch(r":\d+", literal):
        return re.search(re.escape(literal) + r"(?!\d)", output) is not None
    if re.fullmatch(r"\d+", literal):
        return re.search(r"(?<![\w.])" + re.escape(literal) + r"(?![\w.])", output) is not None
    return literal in output


def verify_table(repo: Path, table: Path) -> int:
    """Verify a claims table's explicit literal rows; return 1 on a mismatch or a read failure."""
    text = table.read_text(encoding="utf-8")
    start = text.find("## Claims table")
    section = text[start:] if start >= 0 else text
    checked = skipped = failed = 0
    for line in section.splitlines():
        if not line.startswith("| "):
            continue
        cells = verify_cells(line)
        if len(cells) < 2 or cells[0] == "Claim" or set(cells[0]) <= {"-", " "}:
            continue
        if len(cells) < 3:
            skipped += 1
            print("advisory: {0} (two-column table)".format(cells[0][:80]))
            continue
        kind, info = verify_eligible(cells[1], cells[2])
        if kind != "candidate":
            skipped += 1
            print("{0}: {1} ({2})".format(kind, cells[0][:80], info))
            continue
        command, literals = info
        output, error = verify_run(command, repo)
        checked += 1
        if error:
            failed += 1
            print("mismatch: {0} ({1})".format(cells[0][:80], error))
            continue
        missing = [literal for literal in literals if not literal_matches(literal, output)]
        if missing:
            failed += 1
            print("mismatch: {0} (missing {1})".format(cells[0][:80], ", ".join(missing)))
            if output:
                print("  output: {0}".format(output.strip()[:500]))
        else:
            print("verified: {0}".format(cells[0][:80]))
    print("checked {0}, skipped {1}, mismatches {2}".format(checked, skipped, failed))
    return 1 if failed else 0


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
    parser.add_argument(
        "--verify",
        help=(
            "verify a claims table's explicit literal rows (opt-in; the default scan and check run no execution)"
        ),
    )
    args = parser.parse_args(argv)
    repo = Path(args.repo)
    if args.verify:
        try:
            return verify_table(repo, Path(args.verify))
        except (OSError, UnicodeDecodeError) as failure:
            print("the verification failed: {0}".format(failure), file=sys.stderr)
            return 1
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
