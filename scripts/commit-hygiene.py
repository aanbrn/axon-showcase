#!/usr/bin/env python3
"""Check commit hygiene: the staged set, `captured:` marker placement, and the tracked set.

The `--staged` mode is the pre-commit guard: it refuses a mechanically defective staged set, including a staged
review-round ledger whose checks fail or that loses a row. The complete list of its refusals is the
`showcase/quality/commit-hygiene` spec's pre-commit requirement; this docstring states only that a mode exists and
what it is for. The
`--markers` mode checks marker placement in the working-tree guidance files; the `--tracked-ignored`,
`--conflict-markers`, and
`--executable-bits` modes each verify the tracked set for the build; `--staged` also refuses a staged change directory
that is not the commit's assigned one, read from the worktree's claim or a change-named branch — except a change path
the commit's own merge accounts for, whose content is not the commit's own; `--unique-crons`
verifies the workflow
`cron` schedules do not collide; `--unique-rounds` verifies the review-round ledger carries one row per unit and
round, that each unit's rounds are contiguous from 1, that a round named with `--expect` is recorded, and that a unit
named with `--expect-clean` closes on a round carrying no finding class (both need that mode); `--session-continuity`
verifies the ledger's reviewer-session fields record each round's session and its transition; `--large-files`
verifies no tracked file exceeds the configured size limit; `--table-rows` verifies each markdown file it is given
carries table rows whose cell count matches their delimiter row's; and `--ideas-sections` verifies every dated section
of `docs/ideas.md` carries an entry, failing when the file itself is missing — a judgment the guard applies to a staged
copy through the same reporter.

No mode reads a failed read as a clean result — a git invocation or a tracked file's size: a read whose failure would
look like an empty offender list raises, and `main` reports it as the check having failed. The exits that answer a
question keep their own branch.

`--review-ready` is the unit's readiness for the owner's manual review pass, which the review gate's manual-pass step
invokes rather than a `check` task, since the state it reads exists only in an uncommitted working tree: a non-ignored
untracked path, a path staged and then edited, and a base revision (`--base`, default `origin/main`) whose history
`HEAD` does not contain. A git invocation it needs that fails is reported as readiness not established rather than as a
clean tree. It reports the revisions it compared, stages nothing, and fetches nothing. Stdlib only.
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
REVIEW_READY_BASE = "origin/main"


class CheckFailure(Exception):
    """A read a check cannot proceed without, which failed: the check reports it instead of a clean result."""


class GitFailure(CheckFailure):
    """A git invocation a check cannot proceed without, which failed."""


class FileFailure(CheckFailure):
    """A file a check cannot measure, which it reports rather than skipping."""
LEAD_RE = re.compile(r"^(- |\*\*)")
CONFLICT_MARKER_RE = r"^(<<<<<<< |>>>>>>> )"
VENDORED_SKILL_PREFIXES = (".opencode/skills/ast-grep/", ".opencode/skills/axon4to5-", ".opencode/skills/openspec-")
ARCHIVE_PREFIX = "openspec/changes/archive/"
ARCHIVED_TASKS_BASENAME = "/tasks.md"
TASK_LINE_RE = re.compile(r"^[ \t]*(?:[-*+]|\d{1,9}[.)])[ \t]+\[(?P<mark>[^\]]{0,1})\](?!\()")
CHANGES_PREFIX = "openspec/changes/"
SPECS_PREFIX = "openspec/specs/"
# The per-worktree record of the unit's assigned change, read at commit time so a staged path can be judged against it.
CLAIM_NAME = "axon-showcase-change"
IDEAS_PATH = "docs/ideas.md"
IDEA_SECTION_RE = re.compile(r"^##\s+(\d{4}-\d{2}-\d{2})\s*$")
SECTION_BOUNDARY_RE = re.compile(r"^#{1,2}\s")
CLAIM_NONE = "none"
ARCHIVED_CHANGE_RE = re.compile(r"^(?P<date>\d{4}-\d{2}-\d{2})-(?P<name>.+)$")
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
    output = git_output(repo, "diff", "--cached", "--name-only", "-z", "--diff-filter=ACMR")
    return [path for path in output.split("\0") if path]


def staged_file_text(repo: Path, path: str) -> str:
    """The staged content of `path`, read through the failure-raising helper.

    The caller reads a path it has just seen staged, so a failed read is the index having changed under the check
    rather than an absent path, and it must stop the check instead of skipping that path's marker scan.
    """
    return git_output(repo, "show", f":{path}")


def staged_then_edited_in(records: list[str]) -> list[str]:
    """The paths that are staged and then edited again among `git status --porcelain -z` records.

    `-z` NUL-separates the records and, for a rename, emits the new name in the status-prefixed field followed by the
    old name as its own field with no status columns. That old-name field must be skipped for EVERY rename, before the
    offender test: a clean rename (`R ` with an unmodified worktree) does not enter the branch below, and an old name
    whose first two characters look like status columns (e.g. `RM.txt`) would otherwise be read as a record and yield a
    bogus path such as `txt`.
    """
    offenders = []
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


def find_staged_then_edited(repo: Path) -> list[str]:
    return staged_then_edited_in(git_output(repo, "status", "--porcelain", "-z").split("\0"))


def untracked_in(records: list[str]) -> list[str]:
    """The untracked paths among `git status --porcelain -z` records, which the ignore rules already exclude."""
    return [record[3:] for record in records if record.startswith("?? ")]


def git_output(repo: Path, *args: str) -> str:
    """The stdout of a git invocation a check cannot proceed without, raising when it fails.

    A failed invocation must not read as an empty result: the check would then report a clean tree or set on evidence it
    does not have, which is the failure mode every check exists to prevent.
    """
    result = git(repo, *args)
    if result.returncode != 0:
        detail = result.stderr.strip() or "exit {0}".format(result.returncode)
        raise GitFailure("git {0} failed: {1}".format(" ".join(args), detail))
    return result.stdout


def status_entries(repo: Path) -> list[str]:
    """The records of `git status --porcelain -z`, read through the failure-raising helper.

    `--untracked-files=all` is explicit: the repository's `status.showUntrackedFiles` setting can otherwise hide
    untracked files entirely, or collapse a new directory into one record, and both read as an absent obligation.
    """
    return git_output(repo, "status", "--porcelain", "--untracked-files=all", "-z").split("\0")


def resolved_revision(repo: Path, revision: str) -> Optional[str]:
    """The commit `revision` resolves to, or None when it does not resolve; a git error raises."""
    result = git(repo, "rev-parse", "--verify", "--quiet", revision + "^{commit}")
    if result.returncode == 0:
        resolved = result.stdout.strip()
        return resolved or None
    if result.returncode == 1:
        return None
    raise GitFailure(
        "git rev-parse failed for '{0}': {1}".format(revision, result.stderr.strip() or result.returncode)
    )


def merge_base_of(repo: Path, left: str, right: str) -> Optional[str]:
    """The merge base of two revisions, or None when they share none; a git error raises.

    `git merge-base` answers "no common ancestor" with exit 1 and a failure with a higher code, which the two branches
    below keep apart rather than reading both as an absent base.
    """
    result = git(repo, "merge-base", left, right)
    if result.returncode == 1:
        return None
    if result.returncode != 0:
        raise GitFailure(
            "git merge-base failed: {0}".format(result.stderr.strip() or result.returncode)
        )
    merged = result.stdout.strip()
    return merged or None


def is_ancestor(repo: Path, base: str, head: str) -> bool:
    """Whether `head` contains `base`; git answers with exit 1 when it does not and a higher code on failure."""
    result = git(repo, "merge-base", "--is-ancestor", base, head)
    if result.returncode == 0:
        return True
    if result.returncode == 1:
        return False
    raise GitFailure(
        "git merge-base --is-ancestor failed: {0}".format(result.stderr.strip() or result.returncode)
    )


def commits_base_has_over_head(repo: Path, base: str, head: str) -> list[str]:
    """The base's commits `head` does not contain, newest first: what a stale unit is missing."""
    output = git_output(repo, "rev-list", "--oneline", "{0}..{1}".format(head, base))
    return [line for line in output.splitlines() if line.strip()]


def paths_changed_since(repo: Path, ancestor: str, revision: str) -> list[str]:
    """The paths `revision` changed after `ancestor`, which is where a moved base left the unit behind."""
    output = git_output(repo, "diff", "--name-only", "-z", ancestor, revision, "--")
    return [path for path in output.split("\0") if path]


def find_tracked_ignored(repo: Path) -> list[str]:
    output = git_output(repo, "ls-files", "--cached", "--ignored", "--exclude-standard", "-z")
    return [path for path in output.split("\0") if path]


def find_force_staged_artifacts(repo: Path) -> list[str]:
    ignored = set(find_tracked_ignored(repo))
    return [path for path in staged_paths(repo) if path in ignored]


def modified_spec_paths(repo: Path) -> str:
    """The tracked spec paths with an unstaged modification, NUL-separated."""
    return git_output(repo, "diff", "--name-only", "-z", "--", SPECS_PREFIX)


def untracked_spec_paths(repo: Path) -> str:
    """The untracked spec paths the ignore rules do not exclude, NUL-separated."""
    return git_output(repo, "ls-files", "--others", "--exclude-standard", "-z", "--", SPECS_PREFIX)


def unstaged_spec_paths(repo: Path) -> list[str]:
    return [path for path in (modified_spec_paths(repo) + untracked_spec_paths(repo)).split("\0") if path]


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


def quiet_rev_parse(repo: Path, spec: str) -> Optional[str]:
    """The object id `spec` names, or None when it names nothing.

    A miss is an ordinary answer here rather than a failed read: a merged revision that does not hold the path simply
    does not account for it, and a raising lookup would abort the hook instead of judging the path.
    """
    result = git(repo, "rev-parse", "--verify", "--quiet", spec)
    if result.returncode != 0:
        return None
    return result.stdout.strip() or None


def merge_carries_unchanged(repo: Path, path: str, revisions: list[str]) -> bool:
    """Whether the index blob of `path` equals a merged revision's blob for the same path.

    Path membership alone would exempt content the commit authored: a merge that resolves a conflict in the file, or an
    edit staged after the merge, leaves a different blob and is judged.
    """
    staged = quiet_rev_parse(repo, ":{0}".format(path))
    if staged is None:
        return False
    return any(quiet_rev_parse(repo, "{0}:{1}".format(revision, path)) == staged for revision in revisions)


def find_unticked_archived_tasks(repo: Path) -> list[tuple[str, int, str, str]]:
    """The (path, line, kind, content) of each task line a staged archived change's `tasks.md` leaves unticked.

    Selection is the staged set's own `openspec/changes/archive/**/tasks.md` paths, so no file the commit does not
    stage is judged and the archive's historical files stay untouched. The staged blob is judged rather than the
    working tree's copy, and a file a merge brings unchanged is exempt because merged content is not the commit's own.
    """
    tasks = [
        path
        for path in staged_paths(repo)
        if path.startswith(ARCHIVE_PREFIX) and path.endswith(ARCHIVED_TASKS_BASENAME)
    ]
    if not tasks:
        return []
    revisions = merge_revisions(repo)
    offenders: list[tuple[str, int, str, str]] = []
    for path in tasks:
        if revisions and merge_carries_unchanged(repo, path, revisions):
            continue
        for number, line in enumerate(staged_file_text(repo, path).splitlines(), start=1):
            match = TASK_LINE_RE.match(line)
            if match is None:
                continue
            marker = match.group("mark")
            if marker in ("x", "X"):
                continue
            kind = "unticked" if not marker.strip() else "unsupported marker '{0}'".format(marker)
            offenders.append((path, number, kind, line.strip()))
    return offenders


def find_conflict_markers(repo: Path, staged: bool = False) -> list[str]:
    """The tracked (or staged) lines carrying a conflict marker; git grep's exit 1 answers "no match"."""
    args = ["grep", "-I", "-n", "-E"]
    if staged:
        args.append("--cached")
    args.append(CONFLICT_MARKER_RE)
    result = git(repo, *args)
    if result.returncode == 0:
        return [line for line in result.stdout.splitlines() if line]
    if result.returncode == 1:
        return []
    raise GitFailure("git grep failed: {0}".format(result.stderr.strip() or result.returncode))


def _should_be_executable(path: str) -> bool:
    if path.startswith(VENDORED_SKILL_PREFIXES):
        return False
    return path == "gradlew" or path.startswith("scripts/git-hooks/") or path.endswith(".sh")


def find_non_executable_scripts(repo: Path) -> list[str]:
    offenders = []
    for line in git_output(repo, "ls-files", "-s", "-z").split("\0"):
        parts = line.split(None, 3)
        if len(parts) < 4:
            continue
        mode, path = parts[0], parts[3]
        if mode != "100755" and _should_be_executable(path):
            offenders.append(path)
    return offenders


def tracked_paths(repo: Path) -> list[str]:
    return [path for path in git_output(repo, "ls-files", "-z").split("\0") if path]


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


def tracked_file_size(repo: Path, path: str) -> int:
    """The size a tracked path contributes: measured in the worktree, or from the index when it is absent there.

    A path absent from the worktree is a normal state — deleted and not yet staged — and the index copy is what a commit
    would carry, so it is measured instead. Any other failure to measure is reported, never skipped: the mode would
    otherwise print its checked-count line without having measured the file.
    """
    try:
        return (repo / path).stat().st_size
    except FileNotFoundError:
        staged = git_output(repo, "cat-file", "-s", f":{path}")
        try:
            return int(staged.strip())
        except ValueError as error:
            raise FileFailure(f"the index size of {path} is unreadable: {staged!r}") from error
    except OSError as error:
        raise FileFailure(f"{path} could not be measured: {error}") from error


def find_oversized_files(repo: Path, limit: int) -> list[tuple[str, int]]:
    offenders = []
    for path in tracked_paths(repo):
        size = tracked_file_size(repo, path)
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


def claim_file(repo: Path) -> Path:
    """The per-worktree claim file, resolved through git so a linked worktree reads its own git directory."""
    resolved = Path(git_output(repo, "rev-parse", "--git-path", CLAIM_NAME).strip())
    return resolved if resolved.is_absolute() else repo / resolved


def change_names(repo: Path) -> set[str]:
    """Every change name the repository knows: a live directory, or an archived one with one date segment stripped."""
    names: set[str] = set()
    changes = repo / "openspec" / "changes"
    if changes.is_dir():
        names.update(entry.name for entry in changes.iterdir() if entry.is_dir() and entry.name != "archive")
    archived = changes / "archive"
    if archived.is_dir():
        for entry in archived.iterdir():
            match = ARCHIVED_CHANGE_RE.match(entry.name)
            if entry.is_dir() and match:
                names.add(match.group("name"))
    return names


def current_branch(repo: Path) -> str:
    """The branch HEAD points at, or an empty string when HEAD is detached; an unreadable HEAD raises.

    An unborn HEAD still names its branch, so it is not an error. A detached HEAD is the one answer by exit code
    (`symbolic-ref` exits 1), and neither a detached name nor a branch outside a change names a change, so the claim
    alone assigns the commit.
    """
    result = git(repo, "symbolic-ref", "--quiet", "--short", "HEAD")
    if result.returncode == 0:
        return result.stdout.strip()
    if result.returncode == 1:
        return ""
    raise GitFailure(
        "git symbolic-ref failed: {0}".format(result.stderr.strip() or result.returncode)
    )


def resolve_assignment(repo: Path) -> tuple[str | None, list[str]]:
    """The commit's assigned change and the reasons the assignment fails."""
    claim = claim_file(repo)
    branch = current_branch(repo)
    branch_change = branch if branch in change_names(repo) else None
    lines: list[str] = []
    if claim.is_file():
        lines = [line.strip() for line in claim.read_text(encoding="utf-8").splitlines() if line.strip()]
    recorded = lines[0] if len(lines) == 1 else None
    failures: list[str] = []
    claim_change = recorded if recorded and recorded != CLAIM_NONE else None
    if branch_change is not None:
        if claim_change and claim_change != branch_change:
            failures.append(
                "The claim at {0} names '{1}' while the branch names '{2}': the commit's assigned change is "
                "contradictory. Correct the claim to one of them, or remove it so the branch's assignment "
                "stands".format(claim, claim_change, branch_change)
            )
        return branch_change, failures
    if claim.is_file() and len(lines) != 1:
        failures.append(
            "The claim at {0} must hold exactly one line — a change name or '{1}' — but holds {2}. Write the unit's "
            "assigned change there".format(claim, CLAIM_NONE, len(lines))
        )
        return None, failures
    if recorded is None:
        if branch == "":
            where = "HEAD is detached"
        else:
            where = "the branch '{0}' is not a change name".format(branch)
        failures.append(
            "No commit assignment: {0} and {1} does not exist. Record the change there, or '{2}' for a unit with no "
            "change dir".format(where, claim, CLAIM_NONE)
        )
    return claim_change, failures


def change_path_name(path: str) -> str | None:
    """The change a path belongs to, live or archived, or None for a path outside a change directory."""
    if path.startswith(ARCHIVE_PREFIX):
        match = ARCHIVED_CHANGE_RE.match(path[len(ARCHIVE_PREFIX):].split("/", 1)[0])
        return match.group("name") if match else None
    if path.startswith(CHANGES_PREFIX):
        name, _, inner = path[len(CHANGES_PREFIX):].partition("/")
        return name if inner else None
    return None


def rename_sources(repo: Path) -> list[str]:
    """The paths a staged rename moved away from, which a rename report hides behind its new name."""
    output = git_output(repo, "diff", "--cached", "--name-status", "-z", "--diff-filter=R", "--", CHANGES_PREFIX)
    fields = [field for field in output.split("\0") if field]
    return [
        fields[index + 1] for index in range(0, len(fields) - 2, 3) if fields[index].startswith("R")
    ]


def deleted_change_paths(repo: Path) -> list[str]:
    """The staged deletions under a change directory; a rename reports its source separately, under `R`."""
    output = git_output(
        repo, "diff", "--cached", "--name-only", "-z", "--diff-filter=D", "--", CHANGES_PREFIX
    )
    return [path for path in output.split("\0") if path]


def staged_change_paths(repo: Path) -> list[str]:
    """Every staged path under a change directory — added, modified, deleted, or renamed away from — each once."""
    paths = staged_paths(repo) + deleted_change_paths(repo) + rename_sources(repo)
    return list(dict.fromkeys(path for path in paths if path.startswith(CHANGES_PREFIX)))


def staged_change_presence(repo: Path) -> dict[str, bool]:
    """Each staged change path, mapped to whether the index holds it: a deletion or a rename's source does not."""
    present = set(staged_paths(repo))
    return {path: path in present for path in staged_change_paths(repo)}


def merge_revisions(repo: Path) -> list[str]:
    """The revisions a merge in progress is joining, or an empty list when the commit under construction is no merge.

    The merge state is the git directory's `MERGE_HEAD`, one revision per merged parent — an octopus merge holds
    several, where `rev-parse MERGE_HEAD` answers only the first. A file that is absent means no merge is in progress;
    any other read failure stops the check rather than reading as an ordinary commit.
    """
    resolved = Path(git_output(repo, "rev-parse", "--git-path", "MERGE_HEAD").strip())
    path = resolved if resolved.is_absolute() else repo / resolved
    try:
        text = path.read_text(encoding="utf-8")
    except FileNotFoundError:
        return []
    except OSError as error:
        raise GitFailure(f"the merge state at {path} could not be read: {error}") from error
    return [line.strip() for line in text.splitlines() if line.strip()]


def tree_paths(repo: Path, revisions: list[str]) -> set[str]:
    """Every path the given revisions hold, read from their trees."""
    paths: set[str] = set()
    for revision in revisions:
        output = git_output(repo, "ls-tree", "-r", "-z", "--name-only", revision)
        paths.update(path for path in output.split("\0") if path)
    return paths


def merge_base_paths(repo: Path, revisions: list[str]) -> set[str]:
    """Every path the merged revisions' forks held, read from their merge bases with `HEAD`.

    This is the evidence that a merged revision removed a path: a path absent from the merged trees but present here
    was deleted by the merge, while a path absent from both was never the merged revision's own.
    """
    head = resolved_revision(repo, "HEAD")
    if head is None:
        return set()
    bases = [merge_base_of(repo, head, revision) for revision in revisions]
    return tree_paths(repo, [base for base in bases if base is not None])


def merge_accounts_for(path: str, in_index: bool, merged: set[str], bases: set[str]) -> bool:
    """Whether a merged revision accounts for the path's staged state.

    A path the index holds is the merged revision's own when that revision holds it too. A path the index lacks is a
    deletion, and a merge accounts for it only when a fork held it and no merged revision does: a path no fork held was
    never the merged revision's, so its deletion is the commit's own.
    """
    if in_index:
        return path in merged
    return path not in merged and path in bases


def find_foreign_change_paths(repo: Path, assignment: str | None) -> list[tuple[str, str | None]]:
    """The (path, change) pairs the commit may not carry; with no assignment, every change-directory path.

    A merge commit joins revisions whose content is not the commit's own, so a staged change path the merge accounts
    for is exempt: a path the index holds when a merged revision holds it, or a path the index lacks when a fork held it
    and the merged revision removed it. A foreign path the merge does not account for is still refused, so the
    exemption narrows the ownership check rather than disabling it for merges.
    """
    presence = staged_change_presence(repo)
    foreign: list[tuple[str, str | None]] = []
    for path in presence:
        name = change_path_name(path)
        if assignment is None or (name is not None and name != assignment):
            foreign.append((path, name))
    revisions = merge_revisions(repo)
    if not revisions:
        return foreign
    merged = tree_paths(repo, revisions)
    bases = merge_base_paths(repo, revisions)
    return [
        (path, change)
        for path, change in foreign
        if not merge_accounts_for(path, presence[path], merged, bases)
    ]


def check_staged(repo: Path, formatter: list[str]) -> int:
    failures = 0

    assignment, assignment_failures = resolve_assignment(repo)
    for message in assignment_failures:
        failures += 1
        print(message, file=sys.stderr)
    if not assignment_failures:
        for path, change in find_foreign_change_paths(repo, assignment):
            failures += 1
            if assignment is None:
                print(
                    "Path staged under a change directory, but the workspace declares none ({0} records '{1}'): "
                    "{2}".format(claim_file(repo), CLAIM_NONE, path),
                    file=sys.stderr,
                )
            else:
                print(
                    "Path belongs to change '{0}', not the commit's assigned change '{1}': {2}".format(
                        change, assignment, path
                    ),
                    file=sys.stderr,
                )

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
            for line, content in find_misplaced_markers(staged_file_text(repo, marker_file)):
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

    for path, line, kind, content in find_unticked_archived_tasks(repo):
        failures += 1
        print(f"An archived change's task is not ticked ({kind}): {path}:{line}: {content}", file=sys.stderr)

    ledger = "docs/review-rounds.tsv"
    ledger_removed = bool(
        git_output(repo, "diff", "--cached", "--name-status", "--diff-filter=DR", "--", ledger).strip()
    )
    if ledger in paths or ledger_removed:
        absent = ledger_removed and ledger not in paths
        staged_ledger = "" if absent else staged_file_text(repo, ledger)
        if not absent:
            try:
                failures += check_unique_review_rounds(repo, text=staged_ledger)
                failures += check_session_continuity(repo, text=staged_ledger)
            except ValueError as failure:
                failures += 1
                print(
                    "The review-round ledger ({0}) cannot be checked: {1}".format(ledger, failure), file=sys.stderr
                )
        failures += check_ledger_preservation(repo, staged_ledger)

    failures += check_staged_idea_sections(repo, paths)

    return failures


def check_review_ready(repo: Path, base: str) -> int:
    try:
        base_sha = resolved_revision(repo, base)
        if base_sha is None:
            print(
                f"The base revision '{base}' does not resolve; fetch it before the manual-pass request "
                "(git fetch origin main).",
                file=sys.stderr,
            )
            return 1
        head_sha = resolved_revision(repo, "HEAD")
        if head_sha is None:
            print("HEAD does not resolve to a commit, so the unit cannot be on a base.", file=sys.stderr)
            return 1
        merge_sha = merge_base_of(repo, base_sha, head_sha)
        if merge_sha is None:
            print(
                f"'{base}' and HEAD share no history, so the unit cannot be shown to sit on that base.",
                file=sys.stderr,
            )
            return 1
        contained = is_ancestor(repo, base_sha, head_sha)
        missing = commits_base_has_over_head(repo, base_sha, head_sha) if not contained else []
        changed = paths_changed_since(repo, merge_sha, base_sha) if missing else []
        records = status_entries(repo)
    except GitFailure as failure:
        print(f"Readiness is not established: {failure}", file=sys.stderr)
        return 1

    failures = 0
    if missing:
        failures += 1
        print(
            f"'{base}' carries {len(missing)} commit(s) HEAD does not, so the unit is not on that base; refresh it "
            "and re-run.",
            file=sys.stderr,
        )
        for line in missing[:3]:
            print(f"  {line}", file=sys.stderr)
        for path in changed[:5]:
            print(f"  changed on the base: {path}", file=sys.stderr)
    for path in untracked_in(records):
        failures += 1
        print(f"An untracked path is not in the index, so the manual pass would not show it: {path}", file=sys.stderr)
    for path in staged_then_edited_in(records):
        failures += 1
        print(
            f"A path is staged and then edited, so the manual pass would show the index's older copy: {path}",
            file=sys.stderr,
        )
    if failures:
        return failures
    print(
        f"Review-ready: base {base} at {base_sha} is contained in HEAD {head_sha}; the index is current and no "
        "untracked path is unaccounted for."
    )
    return 0


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


FENCE_RE = re.compile(r"^\s*(`{3,}|~{3,})")
BLOCK_START_RE = re.compile(r"^\s*(?:#{1,6}\s|>|[-*+]\s|\d+\.\s|<)")
THEMATIC_BREAK_RE = re.compile(r"^\s*([-*_])\s*(?:\1\s*){2,}$")
DELIMITER_CELL_RE = re.compile(r"^\s*:?-+:?\s*$")


def has_separator(line: str) -> bool:
    """Whether the line carries an unescaped pipe, i.e. a cell separator."""
    backslashes = 0
    for character in line:
        if character == "\\":
            backslashes += 1
            continue
        if character == "|" and backslashes % 2 == 0:
            return True
        backslashes = 0
    return False


def split_cells(line: str) -> list[str]:
    """The line's cells: one optional leading and one optional trailing pipe removed, then split on separators."""
    text = line.strip()
    if text.startswith("|"):
        text = text[1:]
    cells: list[str] = []
    current: list[str] = []
    backslashes = 0
    for character in text:
        if character == "\\":
            backslashes += 1
            current.append(character)
            continue
        if character == "|" and backslashes % 2 == 0:
            cells.append("".join(current))
            current = []
            backslashes = 0
            continue
        backslashes = 0
        current.append(character)
    cells.append("".join(current))
    if len(cells) > 1 and not cells[-1].strip():
        cells.pop()
    return cells


def is_delimiter_row(line: str) -> bool:
    """Whether the line is a table's delimiter row: every cell a hyphen run with optional alignment colons."""
    cells = split_cells(line)
    return bool(cells) and all(DELIMITER_CELL_RE.match(cell) for cell in cells)


def find_table_row_offenders(path: str, text: str) -> list[tuple[str, int, int | None, int]]:
    """The (path, line, cells, delimiter cells) of each row disagreeing with its table's delimiter row.

    A cell count of None marks a wrapped row: a non-blank line inside the table that carries no separator. A table runs
    from its header to a blank line or a line beginning another block, and a fenced block is never read as a table.
    """
    lines = text.splitlines()
    offenders: list[tuple[str, int, int | None, int]] = []
    fence: tuple[str, int] | None = None
    index = 0
    while index < len(lines):
        line = lines[index]
        marker = FENCE_RE.match(line)
        if marker:
            closing = marker.group(1)
            if fence is None:
                fence = (closing[0], len(closing))
            elif closing[0] == fence[0] and len(closing) >= fence[1]:
                fence = None
            index += 1
            continue
        if fence is not None:
            index += 1
            continue
        if (
            index + 1 < len(lines)
            and has_separator(line)
            and not BLOCK_START_RE.match(line)
            and has_separator(lines[index + 1])
            and is_delimiter_row(lines[index + 1])
        ):
            columns = len(split_cells(lines[index + 1]))
            row = index
            while row < len(lines):
                current = lines[row]
                if row > index and (
                    not current.strip()
                    or FENCE_RE.match(current)
                    or BLOCK_START_RE.match(current)
                    or THEMATIC_BREAK_RE.match(current)
                ):
                    break
                if not has_separator(current):
                    offenders.append((path, row + 1, None, columns))
                else:
                    count = len(split_cells(current))
                    if count != columns:
                        offenders.append((path, row + 1, count, columns))
                row += 1
            index = row
            continue
        index += 1
    return offenders


def check_table_rows(repo: Path, paths: Sequence[str]) -> int:
    """Verify each markdown file's tables carry their delimiter row's column count; no path is a failure."""
    if not paths:
        print("The table-row verification was given no path to read.", file=sys.stderr)
        return 1
    failures = 0
    checked = 0
    for path in paths:
        try:
            text = (repo / path).read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError) as error:
            failures += 1
            print("A markdown file could not be read: {0} ({1})".format(path, error), file=sys.stderr)
            continue
        checked += 1
        for offender, number, cells, columns in find_table_row_offenders(path, text):
            failures += 1
            if cells is None:
                print(
                    "A markdown table row is wrapped and carries no separator: {0}:{1} (the delimiter row has {2} "
                    "cells)".format(offender, number, columns),
                    file=sys.stderr,
                )
            else:
                print(
                    "A markdown table row has {0} cell{1} where its delimiter row has {2}: {3}:{4}".format(
                        cells, "" if cells == 1 else "s", columns, offender, number
                    ),
                    file=sys.stderr,
                )
    if not failures:
        print(f"Checked {checked} markdown file(s) for table row integrity.")
    return failures


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


def ledger_source(repo: Path, text: str | None = None) -> str:
    """The ledger's text: the supplied one, or the repository file's.

    `None` reads the repository file, and `""` is an empty ledger, so a caller that judged the staged content never
    falls back to the working tree.
    """
    if text is not None:
        return text
    ledger = repo / "docs" / "review-rounds.tsv"
    if not ledger.is_file():
        return ""
    return ledger.read_text(encoding="utf-8")


def ledger_row_keys(text: str) -> tuple[set[tuple[str, str]], list[str]]:
    """The ledger text's (unit, round) keys, and the data lines whose round cannot be parsed."""
    keys: set[tuple[str, str]] = set()
    malformed: list[str] = []
    for line in text.splitlines():
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        fields = line.split("\t")
        if len(fields) < 3:
            continue
        if not fields[2].isdigit():
            malformed.append(line)
            continue
        keys.add((fields[1], fields[2]))
    return keys, malformed


def find_duplicate_review_rounds(repo: Path, text: str | None = None) -> list[tuple[str, str, int, int]]:
    """The duplicated (unit, round) rows in the review-round ledger, with both line numbers.

    The ledger declares one row per review round and `scripts/experience-analysis.sh` sums a column, so a duplicate
    double-counts a round. Its leading `#` lines are comments, and a row whose pair is absent is not a pair.
    """
    first_seen: dict[tuple[str, str], int] = {}
    duplicates: list[tuple[str, str, int, int]] = []
    for number, line in enumerate(ledger_source(repo, text).splitlines(), start=1):
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


def ledger_data_rows(repo: Path, text: str | None = None) -> list[list[str]]:
    """The ledger's data rows, comment and blank lines dropped, so every reader parses and validates it once."""
    rows: list[list[str]] = []
    for line in ledger_source(repo, text).splitlines():
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
        rows.append(fields)
    return rows


def ledger_rounds(repo: Path, text: str | None = None) -> dict[str, set[int]]:
    """The recorded round numbers per unit, from the ledger's data rows."""
    rounds: dict[str, set[int]] = {}
    for fields in ledger_data_rows(repo, text):
        rounds.setdefault(fields[1], set()).add(int(fields[2]))
    return rounds


def acknowledged_gaps(repo: Path, text: str | None = None) -> set[tuple[str, int]]:
    """The (unit, round) gaps the ledger acknowledges as unrecoverable or as a misfiled deep pass."""
    gaps: set[tuple[str, int]] = set()
    for line in ledger_source(repo, text).splitlines():
        match = ACKNOWLEDGED_GAP_RE.match(line.strip())
        if match:
            gaps.add((match.group("unit"), int(match.group("round"))))
    return gaps


def find_round_gaps(repo: Path, text: str | None = None) -> list[tuple[str, int]]:
    """The (unit, missing round) pairs that break a unit's contiguity from 1, unacknowledged ones only."""
    acknowledged = acknowledged_gaps(repo, text)
    gaps: list[tuple[str, int]] = []
    for unit, recorded in sorted(ledger_rounds(repo, text).items()):
        for number in range(1, max(recorded) + 1):
            if number not in recorded and (unit, number) not in acknowledged:
                gaps.append((unit, number))
    return gaps


def find_unrecorded_expected(
    repo: Path, expected: Sequence[str], text: str | None = None
) -> list[tuple[str, int]]:
    """The (unit, round) pairs named by the caller whose row the ledger does not carry."""
    recorded = ledger_rounds(repo, text)
    missing: list[tuple[str, int]] = []
    for pair in expected:
        unit, _, number = pair.rpartition(":")
        if not unit or not number.isdigit():
            raise ValueError("expected round must be <unit>:<round>, got {0!r}".format(pair))
        if int(number) not in recorded.get(unit, set()):
            missing.append((unit, int(number)))
    return missing


def parse_expected_clean(units: Sequence[str]) -> list[str]:
    """The named units, validated, so a malformed name fails rather than matching nothing."""
    names: list[str] = []
    for unit in units:
        if not unit or any(character.isspace() for character in unit):
            raise ValueError("--expect-clean must name a non-empty unit without whitespace, got {0!r}".format(unit))
        names.append(unit)
    return names


def find_unclean_closing_rounds(
    repo: Path, units: Sequence[str], text: str | None = None
) -> list[tuple[str, str]]:
    """The (unit, reason) pairs for named units whose closing round is not recorded as clean."""
    rows: dict[str, list[list[str]]] = {}
    for fields in ledger_data_rows(repo, text):
        rows.setdefault(fields[1], []).append(fields)
    failures: list[tuple[str, str]] = []
    for unit in parse_expected_clean(units):
        unit_rows = rows.get(unit, [])
        if not unit_rows:
            failures.append((unit, "the ledger records no round for it"))
            continue
        closing = max(unit_rows, key=lambda row: int(row[2]))
        if len(closing) < 4 or not closing[3].strip():
            failures.append((unit, "round {0} carries no readable classes field".format(closing[2])))
        elif closing[3] != "-":
            failures.append((unit, "round {0} carries {1}".format(closing[2], closing[3])))
    return failures


def check_unique_review_rounds(
    repo: Path, expected: Sequence[str] = (), expect_clean: Sequence[str] = (), text: str | None = None
) -> int:
    """The ledger's duplicate pairs, contiguity gaps, expected-but-unrecorded rounds, and unclean closing rounds."""
    failures = 0
    for unit, round_number, first, second in find_duplicate_review_rounds(repo, text):
        print(
            "Two review-round rows share unit '{0}' round {1}: lines {2} and {3}".format(
                unit, round_number, first, second
            ),
            file=sys.stderr,
        )
        failures += 1
    for unit, number in find_round_gaps(repo, text):
        print(
            "Unit '{0}' is missing round {1}: its rounds must be contiguous from 1".format(unit, number),
            file=sys.stderr,
        )
        failures += 1
    for unit, number in find_unrecorded_expected(repo, expected, text):
        print(
            "Unit '{0}' has no row for round {1}, the round whose report arrived".format(unit, number),
            file=sys.stderr,
        )
        failures += 1
    for unit, reason in find_unclean_closing_rounds(repo, expect_clean, text):
        print(
            "Unit '{0}' is named with --expect-clean, but {1}".format(unit, reason),
            file=sys.stderr,
        )
        failures += 1
    return failures


def session_rows(
    repo: Path, text: str | None = None
) -> tuple[bool, list[tuple[int, list[str]]], list[tuple[int, list[str]]]]:
    """The ledger's data rows split at the session-fields boundary, plus whether that boundary is present.

    The return is `(present, above, below)`, each row a (line number, fields) pair. A row is in scope once the ledger's
    own boundary comment has been read; the rows above it predate the fields, so the continuity verification exempts
    them rather than requiring an identifier the record cannot evidence — which is only sound while those rows carry the
    pre-fields width.
    """
    above: list[tuple[int, list[str]]] = []
    below: list[tuple[int, list[str]]] = []
    seen_boundary = False
    source = ledger_source(repo, text)
    if not source:
        return False, [], []
    for number, line in enumerate(source.splitlines(), start=1):
        if line.startswith(SESSION_FIELDS_MARKER):
            seen_boundary = True
            continue
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        (below if seen_boundary else above).append((number, line.split("\t")))
    return seen_boundary, above, below


def find_session_continuity_violations(repo: Path, text: str | None = None) -> list[str]:
    """The reviewer-session continuity violations the ledger carries, each as a message naming its line.

    The rule is per unit: the first row below the boundary creates the session, a later row whose identifier equals its
    preceding row's continues it, and a row whose identifier differs replaces it and must name one of the recorded
    reasons. Distinct units may name distinct sessions. The verification validates the recorded fields; it does not
    attest that a replacement's reason occurred.
    """
    present, above, rows = session_rows(repo, text)
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


def predecessor_ledger_text(repo: Path) -> str | None:
    """`HEAD`'s review-round ledger, or None when HEAD or the ledger is absent.

    An unborn HEAD and a ledger absent at HEAD are legitimate (a first commit, a first ledger); a failed inspection is
    a read failure that raises, so an unreadable predecessor is never mistaken for an absent one.
    """
    if git(repo, "rev-parse", "--verify", "--quiet", "HEAD").returncode != 0:
        return None
    listing = git(repo, "ls-tree", "-z", "HEAD", "--", "docs/review-rounds.tsv")
    if listing.returncode != 0:
        raise CheckFailure("cannot inspect the predecessor ledger: {0}".format(listing.stderr.strip()))
    if not listing.stdout.strip("\0"):
        return None
    return git_output(repo, "show", "HEAD:docs/review-rounds.tsv")


def check_ledger_preservation(repo: Path, staged_text: str) -> int:
    """Refuse a staged ledger whose rows the commit removes without a surviving (unit, round) row.

    The comparison is against the parent commit's ledger, so a duplicated row's repair passes (its key survives) while
    a removed highest row, a unit's whole set, or a deleted or renamed-away ledger fails.
    """
    before = predecessor_ledger_text(repo)
    if before is None:
        return 0
    before_keys, malformed = ledger_row_keys(before)
    after_keys, unparseable = ledger_row_keys(staged_text)
    failures = 0
    if unparseable:
        print(
            "Review-round row preservation was not assessed until the staged ledger's parsing is repaired",
            file=sys.stderr,
        )
        return failures
    for line in malformed:
        failures += 1
        print("A removed review-round row cannot be identified: {0}".format(line), file=sys.stderr)
    for unit, round_number in sorted(before_keys - after_keys):
        failures += 1
        print(
            "A review-round row is removed and no row keeps unit '{0}' round {1}: the ledger is append-only".format(
                unit, round_number
            ),
            file=sys.stderr,
        )
    return failures


def check_session_continuity(repo: Path, text: str | None = None) -> int:
    violations = find_session_continuity_violations(repo, text)
    for violation in violations:
        print("Review-round session continuity: {0}".format(violation), file=sys.stderr)
    return len(violations)


# The mode group's flags, declared beside their registration so the mode-combination control can derive its
# forms from this list rather than repeating them.
MODE_FLAGS = (
    "--staged",
    "--review-ready",
    "--markers",
    "--tracked-ignored",
    "--conflict-markers",
    "--executable-bits",
    "--unique-crons",
    "--unique-rounds",
    "--session-continuity",
    "--large-files",
    "--table-rows",
    "--ideas-sections",
)


def check_staged_idea_sections(repo: Path, paths: Sequence[str]) -> int:
    """The guard's ideas-section judgment over the staged set: the index's emptied sections, or a staged deletion."""
    if IDEAS_PATH in paths:
        return report_empty_idea_sections(find_empty_idea_sections(staged_file_text(repo, IDEAS_PATH)))
    deleted = git_output(repo, "diff", "--cached", "--name-only", "--diff-filter=D", "--", IDEAS_PATH).strip()
    if deleted:
        print("The ideas file is missing: {0}".format(IDEAS_PATH), file=sys.stderr)
        return 1
    return 0


def report_empty_idea_sections(offenders: list[tuple[int, str]]) -> int:
    """Print each offender as a named failure and return how many there are, shared by the build check and the guard."""
    for number, heading in offenders:
        print(
            "A dated ideas section carries no entry: {0}:{1} {2}".format(IDEAS_PATH, number, heading),
            file=sys.stderr,
        )
    return len(offenders)


def check_idea_sections(repo: Path) -> int:
    """Verify every dated section of the ideas file carries an entry; no dated section passes, a missing file fails."""
    path = repo / IDEAS_PATH
    if not path.is_file():
        print("The ideas file is missing: {0}".format(IDEAS_PATH), file=sys.stderr)
        return 1
    return report_empty_idea_sections(find_empty_idea_sections(path.read_text(encoding="utf-8")))


def find_empty_idea_sections(text: str) -> list[tuple[int, str]]:
    """Each dated `## YYYY-MM-DD` section of the ideas file carrying no `- ` entry, as its line and heading."""
    lines = text.splitlines()
    offenders: list[tuple[int, str]] = []
    for index, line in enumerate(lines):
        if not IDEA_SECTION_RE.match(line):
            continue
        carried = False
        for following in lines[index + 1 :]:
            if SECTION_BOUNDARY_RE.match(following):
                break
            if following.startswith("- "):
                carried = True
                break
        if not carried:
            offenders.append((index + 1, line.strip()))
    return offenders


def build_parser() -> argparse.ArgumentParser:
    """The script's argument parser, so a test can read the mode group it declares."""
    parser = argparse.ArgumentParser(
        description=(
            "Check commit hygiene over the staged set, the unit's readiness for the manual review pass, AGENTS.md "
            "markers, the tracked set, the workflow cron schedules, the review-round ledger, a file's size, the "
            "given markdown files' table rows, or the ideas file's dated sections."
        )
    )
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--staged", action="store_true", help="check the staged set (the pre-commit guard)")
    mode.add_argument(
        "--review-ready",
        action="store_true",
        help="check the unit is ready for the manual review pass (untracked files, a stale index, a moved base)",
    )
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
    mode.add_argument(
        "--table-rows",
        action="store_true",
        help="verify each given markdown file's tables carry their delimiter row's column count",
    )
    mode.add_argument(
        "--ideas-sections",
        action="store_true",
        help="check every dated section of docs/ideas.md carries an entry",
    )
    parser.add_argument(
        "--expect",
        action="append",
        default=[],
        metavar="UNIT:ROUND",
        help="a round whose row must be recorded, as <unit>:<round> (repeatable, with --unique-rounds)",
    )
    parser.add_argument(
        "--expect-clean",
        action="append",
        default=[],
        metavar="UNIT",
        help="a unit whose highest recorded round must carry no finding class (repeatable, with --unique-rounds)",
    )
    mode.add_argument(
        "--session-continuity",
        action="store_true",
        help="check the review-round ledger records each round's reviewer session and its transition",
    )
    mode.add_argument(
        "--large-files", action="store_true", help="check no tracked file exceeds the configured size limit"
    )
    parser.add_argument("paths", nargs="*", help="the markdown files --table-rows reads")
    parser.add_argument(
        "--base",
        default=None,
        metavar="REVISION",
        help=(
            "the revision the readiness check compares the working tree against "
            f"(default: {REVIEW_READY_BASE}, with --review-ready)"
        ),
    )
    parser.add_argument("--repo", default=str(REPO_ROOT), help="repository root (default: the script's parent)")
    return parser


def dispatch(args: argparse.Namespace, repo: Path) -> int:
    """Run the mode the caller selected, returning its failure count as an exit status."""
    if args.review_ready:
        return 1 if check_review_ready(repo, args.base or REVIEW_READY_BASE) else 0
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
            return 1 if check_unique_review_rounds(repo, args.expect, args.expect_clean) else 0
        except ValueError as error:
            print(str(error), file=sys.stderr)
            return 1
    if args.session_continuity:
        return 1 if check_session_continuity(repo) else 0
    if args.large_files:
        return 1 if check_large_files(repo) else 0
    if args.table_rows:
        return 1 if check_table_rows(repo, args.paths) else 0
    if args.ideas_sections:
        return 1 if check_idea_sections(repo) else 0
    return 1 if check_staged(repo, args.formatter) else 0


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
    if args.expect_clean and not args.unique_rounds:
        print(
            "--expect-clean needs --unique-rounds: it reads the ledger the same mode parses", file=sys.stderr
        )
        return 1
    if args.base is not None and not args.review_ready:
        print(
            "--base needs --review-ready: it names the revision that mode compares the working tree against",
            file=sys.stderr,
        )
        return 1
    try:
        return dispatch(args, repo)
    except CheckFailure as failure:
        print(f"The check could not run: {failure}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
