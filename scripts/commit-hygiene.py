#!/usr/bin/env python3
"""Check commit hygiene: the staged set, `captured:` marker placement, and the tracked set.

The `--staged` mode is the pre-commit guard: it refuses a mechanically defective staged set, including staged
review-round records whose checks fail or that lose a row. The complete list of its refusals is the
`showcase/quality/commit-hygiene` spec's: the pre-commit requirement's own set, and the workspace record's judgment it
also applies. This docstring states only that a mode exists and what it is for. The
`--markers` mode checks marker placement in the working-tree guidance files; the `--tracked-ignored`,
`--conflict-markers`, and
`--executable-bits` modes each verify the tracked set for the build; `--staged` also refuses a staged change directory
that is not the commit's assigned one, read from the worktree's claim or a change-named branch — except a change path
the commit's own merge accounts for, whose content is not the commit's own; `--unique-crons`
verifies the workflow
`cron` schedules do not collide; `--unique-change-names` verifies no two archived change directories carry one change
name, which is the directory's own name with one leading `YYYY-MM-DD-` segment removed, apart from the one historical
pair it acknowledges; `--unique-rounds` verifies each unit's review-round record carries at most one row per round, that
each unit's rounds are contiguous from 1, that a round named with `--expect` is recorded, and that a unit
named with `--expect-clean` closes on a round carrying no finding class (both need that mode); `--session-continuity`
verifies each record's reviewer-session fields record each round's session and its transition; `--agent-sessions`
verifies each unit's agent-session register — its fields, per-key continuity, and hand-over links; `--large-files`
verifies no tracked file exceeds the configured size limit; `--table-rows` verifies each markdown file it is given
carries table rows whose cell count matches their delimiter row's; `--ideas-sections` verifies every dated section of
`docs/ideas.md` carries an entry, failing when the file itself is missing — a judgment the guard applies to a staged
copy through the same reporter; `--workspace-records` verifies every unit record at `docs/units/<unit>/unit.md`, each
live change's reference to it, and each archive in scope — judged by its inline record or by its reference according to
its date, apart from one it acknowledges by name — the guard applies the same judgment to a staged copy and adds
the tree and branch the record names; and
`--class-vocabulary` verifies every class in a review-round row dated after the
digest script's canonical boundary is one the reviewer's definition names, reading both rather than restating either,
and exempting only a label a class-acknowledgement comment names on that row; `--finding-instances` verifies each
round recorded after the finding store's boundary carries its instances file; and `--unit-layout` verifies a unit's
directory holds only the record kinds the layout admits, at its two levels.

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
import importlib.util
import re
import subprocess
import sys
from datetime import date
from pathlib import Path
from typing import Mapping, Optional, Sequence

# The shared vocabulary module sits beside this script; load it by that path, whatever the caller's working
# directory or import route.
sys.path.insert(0, str(Path(__file__).resolve().parent))

from review_vocabulary import REVIEW_QUICK_DEFINITION, canonical_classes

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
# The line begins a bullet, a numbered step, a bold-lead paragraph, a heading, a table row, or a fence, so it starts a
# block of its own rather than carrying on the one above it.
PARAGRAPH_START_RE = re.compile(r"^(?:\s*(?:[-*+] |\d+[.)] )|\*\*|#|\||```|~~~)")
MARKDOWN_FENCE_RE = re.compile(r"^\s*(?:```|~~~)")
# The marker chain that must end a rule: one or more `captured: <name>` tokens, each name optionally wrapped onto the
# next line and optionally followed by a `(#NNN)` citation.
CHAIN_END_RE = re.compile(r"(?:captured:\s+[\w.-]+(?:\s*\(#\d+\))?\s*)+$")
# An `evidence:` tag line follows a rule's chain in the documentation rating; it is provenance, not rule text.
EVIDENCE_TAG_RE = re.compile(r"^\s*evidence:")
INLINE_CODE_RE = re.compile(r"`[^`]*`")
CONFLICT_MARKER_RE = r"^(<<<<<<< |>>>>>>> )"
VENDORED_SKILL_PREFIXES = (".opencode/skills/ast-grep/", ".opencode/skills/axon4to5-", ".opencode/skills/openspec-")
ARCHIVE_PREFIX = "openspec/changes/archive/"
ARCHIVED_TASKS_BASENAME = "/tasks.md"

# The workspace record's shape and the boundary of its archive half. An archive dated at or after the boundary is
# judged; one that landed before this verification did is exempt by name, with its reason, so a second directory
# sharing its date and carrying no record still fails.
WORKSPACE_MARKER = "Workspace:"
WORKSPACE_FIELDS = ("unit", "path", "branch", "writer")
WORKSPACE_WRITER_RE = re.compile(r"^ses_[A-Za-z0-9]+$")
WORKSPACE_MARKER_RE = re.compile(r"^`?Workspace:")
# A record ends at the next field or heading, so `State: active` on the following line is not part of a value.
RECORD_FIELD_RE = re.compile(r"^[A-Za-z][A-Za-z ]*:")
WORKSPACE_RECORD_ARCHIVE_SINCE = "2026-11-02"
ACKNOWLEDGED_RECORDLESS_ARCHIVES: dict[str, str] = {}
# A unit's own record: its workspace, state, and pause checkpoint (ADR-0029). `UNIT_RECORD_SINCE` is the boundary from
# which an archive is judged by its reference to that record instead of the fields inside its `tasks.md`; a
# boundary-dated archive that landed before the record is acknowledged by name and keeps its inline record.
UNIT_RECORD_DIR = "docs/units"
UNIT_RECORD_BASENAME = "unit.md"
# Each unit's review-round rows live beside its record, in the same directory.
ROUND_RECORD_BASENAME = "rounds.tsv"
UNIT_RECORD_SINCE = "2026-11-03"
ACKNOWLEDGED_UNIT_RECORD_ARCHIVES: dict[str, str] = {
    directory: "landed before the unit record existed; its inline record stands"
    for directory in (
        "2026-11-03-adopt-branch-refresh-role",
        "2026-11-03-adopt-remaining-guidance-roles",
        "2026-11-03-adopt-spec-delta-role",
        "2026-11-03-classify-actionlint-required",
        "2026-11-03-require-marker-ends-rule",
        "2026-11-03-verify-doctor-install-hints",
    )
}
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

# The one archived name more than one directory carries, kept as recorded rather than repaired. The exemption is the
# exact directory set, never the name: a third directory carrying it fails, and a duplicate of any other name fails.
ACKNOWLEDGED_DUPLICATE_CHANGE_NAMES = {
    "fix-openspec-config-rules": (
        ("2026-09-14-fix-openspec-config-rules", "2026-10-18-fix-openspec-config-rules"),
        "two changes chose this name before the verification existed (#218 and #624), and renaming a directory would "
        "not change either commit message, so the git log --grep confusion would remain",
    ),
}
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
# a continuity violation already committed rather than a recovery case. `parent-session-changed` records a session a
# resumed unit's new main session cannot continue.
SESSION_REPLACEMENT_REASONS = (
    "expired",
    "definition-changed",
    "stopped-responding",
    "parent-session-changed",
    "caller-restarted",
)

# A unit's agent-session register: one row per registered invocation beside its record. The fields are the date, the
# unit, the invoking main session, the agent, the scope, the opinion, the pass, the requested identifier, the returned
# identifier, the transition, the reason, the hand-over slug, and the outcome.
AGENT_SESSION_RECORD_BASENAME = "sessions.tsv"
AGENT_SESSION_ROW_FIELDS = 13
REGISTERED_AGENTS = ("review-thorough", "technical-advisor")
AGENT_SESSION_OPINIONS = ("primary", "independent")
AGENT_SESSION_OUTCOMES = ("report", "stalled", "failed", "mismatch")
HANDOVER_BASENAME = "review-handover.md"

# A gap's acknowledgement exempts the named (unit, round) pair only; a misfiled deep pass uses the same form and
# names its destination, without a data row.
# The ledger's row width once the session fields are present: the six round fields, then the identifier, the transition,
# and the reason.
SESSION_ROW_FIELDS = 9

# The digest script that owns the canonical class boundary. The canonical list itself is `review_vocabulary`'s,
# imported above.
REVIEW_ROUNDS_SCRIPT = "scripts/review-rounds.py"
# The records' two class columns, in the order the format states them.
LEDGER_CLASS_COLUMNS = (("classes raised", 3), ("classes that repeated", 4))
# A class recorded before this check landed, whose round's report was never reconstructed, is acknowledged by a comment
# naming the unit, the round, and the label, so the exemption covers one label on one row rather than a unit or a label.
ACKNOWLEDGED_CLASS_RE = re.compile(
    r"^#\s*---\s*Acknowledged class:\s*(?P<unit>\S+)\s+round\s+(?P<round>\d+)\s+(?P<label>[a-z][a-z-]*)\b"
)

# The ledger's row width before the session fields were appended. A row above the boundary whose field count is not
# this means the boundary was moved past a row the fields apply to, which would exempt that row silently.
SESSION_PRE_FIELDS = 6

# The last day a round row was recorded without the session fields. A record whose rows all predate them is one of the
# split's historical records and is read as exempt; a six-field row dated later is missing the fields and fails, so the
# exemption cannot be claimed by writing the old width today.
PRE_SESSION_FIELDS_THROUGH = date(2026, 10, 18)


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
    """The markers that are not the trailing provenance of their rule, with the line each sits on.

    A marker belongs to the nearest bold-lead rule above it and must end the bullet or paragraph it sits in: the block
    from that block's first line to its end, at the next bullet, numbered step, bold-lead paragraph, heading, table row,
    fence, or blank line. The text from the marker to the block's end therefore has to parse as a marker chain, apart
    from trailing blank lines and an `evidence:` tag line. A marker followed by more of its block reads as part of the
    sentence around it, which is the defect this verification exists for. A marker inside a fenced block is not rule
    prose and is not judged, whether it annotates a command example or records the origin of a command block's comment
    guidance.
    """
    lines = text.split("\n")
    leads = [i for i, line in enumerate(lines) if LEAD_RE.match(line)]
    offenders = []
    fenced = False
    for number, line in enumerate(lines):
        if MARKDOWN_FENCE_RE.match(line):
            fenced = not fenced
            continue
        if fenced:
            continue
        for column in _marker_matches(line):
            if _rule_lead(lines, leads, number) is None:
                offenders.append((number + 1, line.strip()))
                continue
            chain = _trailing_chain_start(lines, *_own_block_bounds(lines, number))
            if chain is None or (number, column) < chain:
                offenders.append((number + 1, line.strip()))
    return offenders


def _marker_matches(line: str) -> list[int]:
    """The columns of the line's `captured:` markers, skipping a token quoted as inline code."""
    columns = []
    for match in re.finditer(re.escape(MARKER), line):
        if match.start() > 0 and line[match.start() - 1] == "`":
            continue
        if line[match.end() :].startswith("`"):
            continue
        columns.append(match.start())
    return columns


def _rule_lead(lines: list[str], leads: list[int], number: int) -> int | None:
    """The bold-lead line of the rule the line belongs to, or None when the nearest block above it is not a rule."""
    lead = next((i for i in reversed(leads) if i <= number), None)
    if lead is None or not lines[lead].startswith(("- **", "**")):
        return None
    return lead


def _own_block_bounds(lines: list[str], number: int) -> tuple[int, int]:
    """The (first, end-exclusive) lines of the bullet or paragraph the line sits in."""
    first = number
    while first > 0 and _continues(lines[first - 1]):
        first -= 1
    last = number + 1
    while last < len(lines) and _continues(lines[last]):
        last += 1
    return first, last


def _continues(line: str) -> bool:
    """Whether the line carries on the block above it: not a bullet, step, heading, fence, table row, or blank line."""
    return bool(line.strip()) and not PARAGRAPH_START_RE.match(line)


def _trailing_chain_start(lines: list[str], first: int, last: int) -> tuple[int, int] | None:
    """The (line, column) where the block's trailing marker chain begins, or None when no chain ends the block.

    Trailing blank lines and `evidence:` tag lines are the chain's trailer, so they are dropped before the chain is
    read. Inline code is blanked rather than removed, so a backticked mention cannot match the chain and every column
    stays aligned with the line it was measured on.
    """
    while last > first:
        line = lines[last - 1]
        if not line.strip() or EVIDENCE_TAG_RE.match(line):
            last -= 1
            continue
        break
    if last == first:
        return None
    block = "\n".join(lines[first:last])
    text = INLINE_CODE_RE.sub(lambda match: " " * len(match.group(0)), block)
    chain = CHAIN_END_RE.search(text)
    if chain is None:
        return None
    line_offset = first + text[: chain.start()].count("\n")
    column_offset = chain.start() - (text.rfind("\n", 0, chain.start()) + 1)
    return line_offset, column_offset


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


def archived_change_names(repo: Path) -> dict[str, list[str]]:
    """Every archived change name with the directories carrying it, in directory order; a missing archive raises."""
    archived = repo / ARCHIVE_PREFIX
    if not archived.is_dir():
        raise CheckFailure(
            "The archived changes directory is missing: {0}".format(ARCHIVE_PREFIX.rstrip("/"))
        )
    names: dict[str, list[str]] = {}
    for entry in sorted(archived.iterdir()):
        match = ARCHIVED_CHANGE_RE.match(entry.name)
        if not (entry.is_dir() and match):
            continue
        names.setdefault(match.group("name"), []).append(entry.name)
    return names


def change_names(repo: Path) -> set[str]:
    """Every change name the repository knows: a live directory, or an archived one with one date segment stripped."""
    names: set[str] = set()
    changes = repo / "openspec" / "changes"
    if changes.is_dir():
        names.update(entry.name for entry in changes.iterdir() if entry.is_dir() and entry.name != "archive")
    if (changes / "archive").is_dir():
        names.update(archived_change_names(repo))
    return names


def find_duplicate_change_names(repo: Path) -> list[tuple[str, list[str]]]:
    """The archived change names more than one directory carries, apart from an acknowledged exact pair."""
    duplicates = []
    for name, directories in sorted(archived_change_names(repo).items()):
        if len(directories) < 2:
            continue
        acknowledged = ACKNOWLEDGED_DUPLICATE_CHANGE_NAMES.get(name)
        if acknowledged is not None and tuple(directories) == acknowledged[0]:
            continue
        duplicates.append((name, directories))
    return duplicates


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

    record_paths = {
        path.split("/")[-2]: path
        for path in paths
        if re.fullmatch(re.escape(UNIT_RECORD_DIR) + r"/[^/]+/" + re.escape(ROUND_RECORD_BASENAME), path)
    }
    removed_records: set[str] = set()
    status = git_output(
        repo, "diff", "--cached", "--name-status", "-z", "--diff-filter=DR", "--", UNIT_RECORD_DIR
    )
    tokens = [token for token in status.split("\0") if token]
    index = 0
    while index < len(tokens):
        code = tokens[index]
        source = tokens[index + 1] if index + 1 < len(tokens) else ""
        # A rename lists its source then its destination; only the source's rows leave the unit they were filed under.
        index += 3 if code.startswith(("R", "C")) else 2
        if source.endswith("/" + ROUND_RECORD_BASENAME):
            removed_records.add(source.split("/")[-2])
    if record_paths or removed_records:
        staged_records = {unit: staged_file_text(repo, path) for unit, path in sorted(record_paths.items())}
        try:
            failures += check_unique_review_rounds(repo, records=staged_records)
            failures += check_session_continuity(repo, records=staged_records)
        except ValueError as failure:
            failures += 1
            print("The staged review-round records cannot be checked: {0}".format(failure), file=sys.stderr)
        affected = sorted(set(staged_records) | removed_records)
        before = predecessor_records(repo, affected) or {}
        effective = dict(before)
        effective.update(staged_records)
        for unit in removed_records:
            effective.pop(unit, None)
        failures += check_ledger_preservation(repo, effective, before)

    changed_units = set(record_paths) | removed_records
    findings_status = git_output(
        repo, "diff", "--cached", "--name-status", "-z", "--diff-filter=ACMRD", "--", UNIT_RECORD_DIR
    )
    findings_tokens = [token for token in findings_status.split("\0") if token]
    position = 0
    while position < len(findings_tokens):
        code = findings_tokens[position]
        source = findings_tokens[position + 1] if position + 1 < len(findings_tokens) else ""
        position += 3 if code.startswith(("R", "C")) else 2
        if re.fullmatch(
            re.escape(UNIT_RECORD_DIR) + r"/([^/]+)/" + FINDING_INSTANCES_DIR + r"/round-\d+\.md", source
        ):
            changed_units.add(source.split("/")[2])
    if changed_units:
        index_records: dict[str, str] = {}
        index_instances: dict[tuple[str, str], str] = {}
        try:
            for unit in sorted(changed_units):
                record = record_label(unit)
                if git_output(repo, "ls-files", "--cached", "--", record).strip():
                    index_records[unit] = index_text(repo, record)
                listing = git_output(
                    repo,
                    "ls-files",
                    "-z",
                    "--cached",
                    "--",
                    UNIT_RECORD_DIR + "/" + unit + "/" + FINDING_INSTANCES_DIR,
                )
                for path in listing.split("\0"):
                    match = re.search(r"/round-(\d+)\.md$", path)
                    if match:
                        index_instances[(unit, match.group(1))] = index_text(repo, path)
        except ValueError as failure:
            failures += 1
            print("Review-round finding instances cannot be checked: {0}".format(failure), file=sys.stderr)
        else:
            for problem in find_instance_violations(index_records, index_instances):
                failures += 1
                print("Review-round finding instances: {0}".format(problem), file=sys.stderr)

    session_paths = {
        path.split("/")[-2]: path
        for path in paths
        if re.fullmatch(UNIT_RECORD_DIR + r"/[^/]+/" + AGENT_SESSION_RECORD_BASENAME, path)
    }
    removed_sessions: set[str] = set()
    session_status = git_output(
        repo, "diff", "--cached", "--name-status", "-z", "--diff-filter=DR", "--", UNIT_RECORD_DIR
    )
    session_tokens = [token for token in session_status.split("\0") if token]
    index = 0
    while index < len(session_tokens):
        code = session_tokens[index]
        source = session_tokens[index + 1] if index + 1 < len(session_tokens) else ""
        index += 3 if code.startswith(("R", "C")) else 2
        if source.endswith("/" + AGENT_SESSION_RECORD_BASENAME):
            removed_sessions.add(source.split("/")[-2])
    if session_paths or removed_sessions:
        staged_sessions = {unit: staged_file_text(repo, path) for unit, path in sorted(session_paths.items())}
        handovers = {unit: staged_or_tree_text(repo, handover_label(unit)) for unit in staged_sessions}
        failures += check_agent_sessions(repo, records=staged_sessions, handovers=handovers)
        affected_sessions = sorted(set(staged_sessions) | removed_sessions)
        before_sessions = (
            predecessor_records(repo, affected_sessions, basename=AGENT_SESSION_RECORD_BASENAME) or {}
        )
        effective_sessions = dict(before_sessions)
        effective_sessions.update(staged_sessions)
        for unit in removed_sessions:
            effective_sessions.pop(unit, None)
        for unit in affected_sessions:
            for problem in find_session_record_rewrites(before_sessions.get(unit), effective_sessions.get(unit)):
                failures += 1
                print(
                    "Agent-session register: {0}: {1}".format(agent_session_label(unit), problem), file=sys.stderr
                )

    failures += check_staged_idea_sections(repo, paths)

    failures += check_staged_workspace_records(repo, paths)

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


def check_unique_change_names(repo: Path) -> int:
    duplicates = find_duplicate_change_names(repo)
    for name, directories in duplicates:
        print(
            "Archived changes share a name: '{0}' ({1})".format(name, ", ".join(directories)),
            file=sys.stderr,
        )
    return len(duplicates)


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


def round_record_paths(repo: Path) -> dict[str, Path]:
    """Each unit's round record path, keyed by the unit whose directory holds it."""
    root = repo / UNIT_RECORD_DIR
    if not root.is_dir():
        return {}
    return {path.parent.name: path for path in sorted(root.glob("*/" + ROUND_RECORD_BASENAME))}


def round_records(repo: Path, records: Mapping[str, str] | None = None) -> dict[str, str]:
    """Each unit's record text, keyed by unit.

    A supplied mapping is judged as-is, so the guard judges staged content and never falls back to the working tree.
    """
    if records is not None:
        return dict(records)
    return {unit: path.read_text(encoding="utf-8") for unit, path in round_record_paths(repo).items()}


def record_label(unit: str) -> str:
    """The repository path a message names for a unit's round record."""
    return UNIT_RECORD_DIR + "/" + unit + "/" + ROUND_RECORD_BASENAME


def record_data_rows(unit: str, text: str) -> list[tuple[int, list[str]]]:
    """One record's data rows with their line numbers."""
    rows: list[tuple[int, list[str]]] = []
    for number, line in enumerate(text.splitlines(), start=1):
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        fields = line.split("\t")
        if len(fields) < 3:
            continue
        if not fields[2].isdigit():
            raise ValueError(
                "{0}:{1}: unit '{2}' has a non-numeric round '{3}': the round column must be a number".format(
                    record_label(unit), number, fields[1], fields[2]
                )
            )
        rows.append((number, fields))
    return rows


def find_misfiled_rows(records: Mapping[str, str]) -> list[tuple[str, int, str]]:
    """The (directory unit, line, recorded unit) rows filed under another unit's directory."""
    misfiled: list[tuple[str, int, str]] = []
    for unit, text in sorted(records.items()):
        for number, line in enumerate(text.splitlines(), start=1):
            if not line.strip() or line.lstrip().startswith("#"):
                continue
            fields = line.split("\t")
            if len(fields) >= 2 and fields[1] != unit:
                misfiled.append((unit, number, fields[1]))
    return misfiled


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


def find_duplicate_review_rounds(
    repo: Path, records: Mapping[str, str] | None = None
) -> list[tuple[str, str, int, int]]:
    """The duplicated (unit, round) rows in the records, each with the file's two line numbers.

    A record declares one row per round and `scripts/experience-analysis.sh` sums a column, so a duplicate
    double-counts a round. Its leading `#` lines are comments, and a row whose pair is absent is not a pair.
    """
    duplicates: list[tuple[str, str, int, int]] = []
    for unit, text in sorted(round_records(repo, records).items()):
        first_seen: dict[tuple[str, str], int] = {}
        for number, line in enumerate(text.splitlines(), start=1):
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


def ledger_data_rows(repo: Path, records: Mapping[str, str] | None = None) -> list[list[str]]:
    """Every record's data rows, comment and blank lines dropped, so every reader parses and validates them once."""
    rows: list[list[str]] = []
    for unit, text in sorted(round_records(repo, records).items()):
        rows.extend(fields for _, fields in record_data_rows(unit, text))
    return rows


def ledger_rounds(repo: Path, records: Mapping[str, str] | None = None) -> dict[str, set[int]]:
    """The recorded round numbers per unit, from the records' data rows."""
    rounds: dict[str, set[int]] = {}
    for fields in ledger_data_rows(repo, records):
        rounds.setdefault(fields[1], set()).add(int(fields[2]))
    return rounds


def acknowledged_gaps(repo: Path, records: Mapping[str, str] | None = None) -> set[tuple[str, int]]:
    """The (unit, round) gaps a record acknowledges as unrecoverable or as a misfiled deep pass.

    Only a comment in the unit's own record counts, as the format requires, so a comment filed under another unit
    exempts nothing.
    """
    gaps: set[tuple[str, int]] = set()
    for unit, text in round_records(repo, records).items():
        for line in text.splitlines():
            match = ACKNOWLEDGED_GAP_RE.match(line.strip())
            if match and match.group("unit") == unit:
                gaps.add((unit, int(match.group("round"))))
    return gaps


def find_round_gaps(repo: Path, records: Mapping[str, str] | None = None) -> list[tuple[str, int]]:
    """The (unit, missing round) pairs that break a unit's contiguity from 1, unacknowledged ones only."""
    acknowledged = acknowledged_gaps(repo, records)
    gaps: list[tuple[str, int]] = []
    for unit, recorded in sorted(ledger_rounds(repo, records).items()):
        for number in range(1, max(recorded) + 1):
            if number not in recorded and (unit, number) not in acknowledged:
                gaps.append((unit, number))
    return gaps


def find_unrecorded_expected(
    repo: Path, expected: Sequence[str], records: Mapping[str, str] | None = None
) -> list[tuple[str, int]]:
    """The (unit, round) pairs named by the caller whose row the records do not carry."""
    recorded = ledger_rounds(repo, records)
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
    repo: Path, units: Sequence[str], records: Mapping[str, str] | None = None
) -> list[tuple[str, str]]:
    """The (unit, reason) pairs for named units whose closing round is not recorded as clean."""
    rows: dict[str, list[list[str]]] = {}
    for fields in ledger_data_rows(repo, records):
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
    repo: Path, expected: Sequence[str] = (), expect_clean: Sequence[str] = (), records: Mapping[str, str] | None = None
) -> int:
    """Each record's duplicate pairs, contiguity gaps, expected-but-unrecorded rounds, and unclean closing rounds."""
    failures = 0
    for unit, line, recorded_unit in find_misfiled_rows(round_records(repo, records)):
        print(
            "A review-round row in {0} names unit '{1}' at line {2}: a record carries only its own unit's rows".format(
                record_label(unit), recorded_unit, line
            ),
            file=sys.stderr,
        )
        failures += 1
    for unit, round_number, first, second in find_duplicate_review_rounds(repo, records):
        print(
            "Two review-round rows share unit '{0}' round {1}: {2} lines {3} and {4}".format(
                unit, round_number, record_label(unit), first, second
            ),
            file=sys.stderr,
        )
        failures += 1
    for unit, number in find_round_gaps(repo, records):
        print(
            "Unit '{0}' is missing round {1}: its rounds must be contiguous from 1".format(unit, number),
            file=sys.stderr,
        )
        failures += 1
    for unit, number in find_unrecorded_expected(repo, expected, records):
        print(
            "Unit '{0}' has no row for round {1}, the round whose report arrived".format(unit, number),
            file=sys.stderr,
        )
        failures += 1
    for unit, reason in find_unclean_closing_rounds(repo, expect_clean, records):
        print(
            "Unit '{0}' is named with --expect-clean, but {1}".format(unit, reason),
            file=sys.stderr,
        )
        failures += 1
    return failures


def find_session_continuity_violations(
    repo: Path, records: Mapping[str, str] | None = None
) -> list[str]:
    """The reviewer-session continuity violations the records carry, each naming its file and line.

    The rule is per unit: the first row below the boundary creates the session, a later row whose identifier equals its
    preceding row's continues it, and a row whose identifier differs replaces it and must name one of the recorded
    reasons. A record carrying no boundary marker is read as having all its rows on the side its own rows show; one
    holding both widths without the marker is a violation rather than a guess. The verification validates the recorded
    fields; it does not attest that a replacement's reason occurred.
    """
    violations: list[str] = []
    for unit, source in sorted(round_records(repo, records).items()):
        label = record_label(unit)
        present = False
        above: list[tuple[int, list[str]]] = []
        below: list[tuple[int, list[str]]] = []
        for number, line in enumerate(source.splitlines(), start=1):
            if line.startswith(SESSION_FIELDS_MARKER):
                present = True
                continue
            if not line.strip() or line.lstrip().startswith("#"):
                continue
            (below if present else above).append((number, line.split("\t")))
        if not present:
            widths = {len(fields) for _, fields in above}
            if widths == {SESSION_ROW_FIELDS}:
                below, above = above, []
            elif not widths or widths == {SESSION_PRE_FIELDS}:
                pass
            else:
                violations.append(
                    "{0} carries rows on both sides of the session-fields boundary without its marker".format(label)
                )
                continue
        for number, fields in above:
            if len(fields) != SESSION_PRE_FIELDS:
                violations.append(
                    "{0}:{1}: a row above the session-fields boundary carries {2} field(s), not the {3} it carried "
                    "before the fields existed, so the boundary was moved past it".format(
                        label, number, len(fields), SESSION_PRE_FIELDS
                    )
                )
                continue
            row_date = ledger_date(fields[0])
            if row_date is None or row_date > PRE_SESSION_FIELDS_THROUGH:
                violations.append(
                    "{0}:{1} carries no session fields and is dated {2!r}: the fields are required for a row after "
                    "{3}".format(label, number, fields[0], PRE_SESSION_FIELDS_THROUGH.isoformat())
                )
        previous: str | None = None
        for number, fields in below:
            if len(fields) != SESSION_ROW_FIELDS:
                violations.append(
                    "{0}:{1}: a row below the session-fields boundary carries {2} field(s), not {3}".format(
                        label, number, len(fields), SESSION_ROW_FIELDS
                    )
                )
                continue
            round_number = fields[2]
            session, transition, reason = fields[6], fields[7], fields[8]
            if not session.strip() or session.strip() == "-":
                violations.append(
                    "{0}:{1}: unit '{2}' round {3} carries no session identifier".format(
                        label, number, unit, round_number
                    )
                )
                continue
            session = session.strip()
            if transition not in SESSION_TRANSITIONS:
                violations.append(
                    "{0}:{1}: unit '{2}' round {3} carries transition '{4}', not one of {5}".format(
                        label, number, unit, round_number, transition, ", ".join(SESSION_TRANSITIONS)
                    )
                )
                continue
            if reason not in ("", "-") and transition != SESSION_REPLACED:
                violations.append(
                    "{0}:{1}: unit '{2}' round {3} carries reason '{4}' with the '{5}' transition, which is not a "
                    "replacement".format(label, number, unit, round_number, reason, transition)
                )
            if previous is None:
                if transition != SESSION_CREATED:
                    violations.append(
                        "{0}:{1}: unit '{2}' round {3} is the unit's first recorded row, so its transition must be "
                        "'{4}', not '{5}'".format(label, number, unit, round_number, SESSION_CREATED, transition)
                    )
            elif session == previous:
                if transition != SESSION_CONTINUED:
                    violations.append(
                        "{0}:{1}: unit '{2}' round {3} continues session '{4}', so its transition must be '{5}', not "
                        "'{6}'".format(label, number, unit, round_number, session, SESSION_CONTINUED, transition)
                    )
            elif transition != SESSION_REPLACED:
                violations.append(
                    "{0}:{1}: unit '{2}' round {3} names session '{4}' where its preceding row named '{5}', without a "
                    "'{6}' transition".format(label, number, unit, round_number, session, previous, SESSION_REPLACED)
                )
            elif reason in ("", "-") or reason not in SESSION_REPLACEMENT_REASONS:
                violations.append(
                    "{0}:{1}: unit '{2}' round {3} replaces a session with reason '{4}', not one of {5}".format(
                        label, number, unit, round_number, reason, ", ".join(SESSION_REPLACEMENT_REASONS)
                    )
                )
            previous = session
    return violations


def predecessor_records(
    repo: Path, units: Sequence[str] | None = None, basename: str = ROUND_RECORD_BASENAME
) -> dict[str, str] | None:
    """`HEAD`'s records of `basename` for `units` (all when None), keyed by unit, or None when HEAD is absent.

    An unborn HEAD is legitimate (a first commit); a failed inspection is a read failure that raises, so an unreadable
    predecessor is never mistaken for an absent one. A caller naming units reads only those blobs, so a check of a few
    staged records costs a few reads rather than one per record in the corpus.
    """
    if git(repo, "rev-parse", "--verify", "--quiet", "HEAD").returncode != 0:
        return None
    listing = git(repo, "ls-tree", "-r", "-z", "--name-only", "HEAD", "--", UNIT_RECORD_DIR)
    if listing.returncode != 0:
        raise CheckFailure("cannot inspect the predecessor records: {0}".format(listing.stderr.strip()))
    wanted = set(units) if units is not None else None
    records: dict[str, str] = {}
    for path in listing.stdout.split("\0"):
        if not path.endswith("/" + basename):
            continue
        unit = path.split("/")[-2]
        if wanted is not None and unit not in wanted:
            continue
        records[unit] = git_output(repo, "show", "HEAD:" + path)
    return records


def check_ledger_preservation(
    repo: Path, records: Mapping[str, str], before: Mapping[str, str] | None = None
) -> int:
    """Refuse a commit whose records remove a (unit, round) row without a surviving one in that unit.

    The comparison is per unit against the parent commit's records, so a duplicated row's repair passes (its key
    survives) while a removed highest row, a deleted or renamed-away record, or a unit's whole set fails. A supplied
    `before` is the predecessor of the units under judgment, so the caller that already read them reads them once.
    """
    if before is None:
        before = predecessor_records(repo) or {}
    failures = 0
    for unit, text in sorted(before.items()):
        before_keys, malformed = ledger_row_keys(text)
        after_text = records.get(unit)
        after_keys, unparseable = ledger_row_keys(after_text) if after_text is not None else (set(), [])
        if unparseable:
            print(
                "Review-round row preservation was not assessed for {0} until its parsing is repaired".format(
                    record_label(unit)
                ),
                file=sys.stderr,
            )
            continue
        for line in malformed:
            failures += 1
            print("A removed review-round row cannot be identified: {0}".format(line), file=sys.stderr)
        for _, round_number in sorted(before_keys - after_keys, key=lambda key: int(key[1])):
            failures += 1
            print(
                "A review-round row is removed and no row keeps unit '{0}' round {1}: the records are "
                "append-only".format(unit, round_number),
                file=sys.stderr,
            )
    return failures


def check_session_continuity(repo: Path, records: Mapping[str, str] | None = None) -> int:
    violations = find_session_continuity_violations(repo, records)
    for violation in violations:
        print("Review-round session continuity: {0}".format(violation), file=sys.stderr)
    return len(violations)


def agent_session_record_paths(repo: Path) -> dict[str, Path]:
    """Each unit's agent-session register path, keyed by the unit whose directory holds it."""
    root = repo / UNIT_RECORD_DIR
    if not root.is_dir():
        return {}
    return {path.parent.name: path for path in sorted(root.glob("*/" + AGENT_SESSION_RECORD_BASENAME))}


def agent_session_label(unit: str) -> str:
    """The repository path the messages name for a unit's agent-session register."""
    return UNIT_RECORD_DIR + "/" + unit + "/" + AGENT_SESSION_RECORD_BASENAME


def handover_label(unit: str) -> str:
    """The repository path the messages name for a unit's hand-over file."""
    return UNIT_RECORD_DIR + "/" + unit + "/" + HANDOVER_BASENAME


def handover_slugs(text: str) -> set[str]:
    """The hand-over sections' slugs, read from the backticked token that begins each second-level heading."""
    slugs: set[str] = set()
    for line in text.splitlines():
        if not line.startswith("## "):
            continue
        match = re.match(r"^## `([^`]+)`", line)
        if match:
            slugs.add(match.group(1))
    return slugs


def find_agent_session_violations(
    records: Mapping[str, str], handovers: Mapping[str, str] | None = None
) -> list[str]:
    """Each agent-session register's violations, naming the unit and the row.

    The rules are the record format's and its continuity requirement's: a thirteen-field row naming its directory's
    unit and a registered agent; the outcome and transition shapes; the per-key pass; the requested/returned identity
    each transition establishes; the parentage a `parent-session-changed` replacement claims; and the hand-over slug
    resolving to a section. The verification validates the recorded fields only; it does not attest a stated cause.
    """
    violations: list[str] = []
    for unit, source in sorted(records.items()):
        label = agent_session_label(unit)
        resolved_slugs = handover_slugs((handovers or {}).get(unit, ""))
        last_resolved: dict[tuple[str, str, str, str], tuple[str, str]] = {}
        passes: dict[tuple[str, str, str, str], int] = {}
        for number, line in enumerate(source.splitlines(), start=1):
            if not line.strip() or line.lstrip().startswith("#"):
                continue
            fields = line.split("\t")
            if len(fields) != AGENT_SESSION_ROW_FIELDS:
                violations.append(
                    "{0}:{1}: a row carries {2} field(s), not {3}".format(
                        label, number, len(fields), AGENT_SESSION_ROW_FIELDS
                    )
                )
                continue
            (
                date_text,
                row_unit,
                main,
                agent,
                scope,
                opinion,
                pass_text,
                requested,
                returned,
                transition,
                reason,
                slug,
                outcome,
            ) = fields
            where = "{0}:{1}".format(label, number)
            if ledger_date(date_text) is None:
                violations.append("{0}: the date '{1}' is not YYYY-MM-DD".format(where, date_text))
            if row_unit != unit:
                violations.append("{0}: the unit column '{1}' does not name '{2}'".format(where, row_unit, unit))
            if agent not in REGISTERED_AGENTS:
                violations.append(
                    "{0}: the agent '{1}' is not one of {2}".format(where, agent, ", ".join(REGISTERED_AGENTS))
                )
                continue
            if not scope.strip() or scope.strip() == "-":
                violations.append("{0}: the scope is empty".format(where))
            elif agent == "review-thorough" and scope != unit:
                violations.append("{0}: a reviewer's scope '{1}' is not the unit".format(where, scope))
            elif agent == "technical-advisor" and not re.fullmatch(r"[a-z0-9]+(-[a-z0-9]+)*", scope):
                violations.append("{0}: a consultation's scope '{1}' is not a kebab-case slug".format(where, scope))
            expected_opinion = "-" if agent != "technical-advisor" else None
            if expected_opinion == "-" and opinion != "-":
                violations.append("{0}: a {1} row carries opinion '{2}', not '-'".format(where, agent, opinion))
            if expected_opinion is None and opinion not in AGENT_SESSION_OPINIONS:
                violations.append(
                    "{0}: a consultation row carries opinion '{1}', not one of {2}".format(
                        where, opinion, ", ".join(AGENT_SESSION_OPINIONS)
                    )
                )
            if not main.strip() or main.strip() == "-":
                violations.append("{0}: the main session is empty".format(where))
            if not pass_text.isdigit() or int(pass_text) < 1:
                violations.append("{0}: the pass '{1}' is not a positive number".format(where, pass_text))
                continue
            key = (agent, scope, opinion, unit)
            expected_pass = passes.get(key, 0) + 1
            if int(pass_text) != expected_pass:
                violations.append(
                    "{0}: pass {1} is not the key's next pass, {2}".format(where, pass_text, expected_pass)
                )
            passes[key] = expected_pass
            if slug not in resolved_slugs:
                violations.append(
                    "{0}: the hand-over slug '{1}' has no matching section in {2}".format(
                        where, slug, handover_label(unit)
                    )
                )
            if outcome not in AGENT_SESSION_OUTCOMES:
                violations.append(
                    "{0}: the outcome '{1}' is not one of {2}".format(
                        where, outcome, ", ".join(AGENT_SESSION_OUTCOMES)
                    )
                )
                continue
            previous = last_resolved.get(key)
            if outcome in ("stalled", "failed"):
                if transition != "-" or reason != "-" or returned != "-":
                    violations.append(
                        "{0}: a {1} row carries transition '{2}', reason '{3}', returned '{4}': it reports "
                        "nothing".format(where, outcome, transition, reason, returned
                        )
                    )
                continue
            if outcome == "mismatch":
                if transition != "-" or reason != "-":
                    violations.append(
                        "{0}: a mismatch row carries transition '{1}' and reason '{2}': it records no session".format(
                            where, transition, reason
                        )
                    )
                if requested in ("", "-") or returned in ("", "-"):
                    violations.append("{0}: a mismatch row needs both identifiers".format(where))
                elif requested == returned:
                    violations.append("{0}: a mismatch row's identifiers agree".format(where))
                continue
            if transition not in SESSION_TRANSITIONS:
                violations.append(
                    "{0}: the transition '{1}' is not one of {2}".format(
                        where, transition, ", ".join(SESSION_TRANSITIONS)
                    )
                )
                continue
            if returned in ("", "-"):
                violations.append("{0}: a report row carries no returned identifier".format(where))
                continue
            if transition == SESSION_CREATED:
                if previous is not None:
                    violations.append("{0}: a created row follows the key's resolved session".format(where))
                if requested != "-" or reason != "-":
                    violations.append(
                        "{0}: a created row carries requested '{1}' and reason '{2}', not '-'".format(
                            where, requested, reason
                        )
                    )
            elif transition == SESSION_CONTINUED:
                if previous is None:
                    violations.append("{0}: a continued row has no session to continue".format(where))
                else:
                    session, previous_main = previous
                    if requested != session or returned != session:
                        violations.append(
                            "{0}: a continued row must request and return '{1}'".format(where, session)
                        )
                    if main != previous_main:
                        violations.append(
                            "{0}: a continued row's main session differs from '{1}': a child cannot cross a main "
                            "session".format(where, previous_main)
                        )
                    if reason != "-":
                        violations.append("{0}: a continued row carries reason '{1}'".format(where, reason))
            else:
                if previous is None:
                    violations.append("{0}: a replaced row has no session to replace".format(where))
                elif returned == previous[0]:
                    violations.append(
                        "{0}: a replaced row returns the identifier it replaces".format(where)
                    )
                if requested != "-":
                    violations.append("{0}: a replaced row requests '{1}', not '-'".format(where, requested))
                if reason not in SESSION_REPLACEMENT_REASONS:
                    violations.append(
                        "{0}: the replacement reason '{1}' is not one of {2}".format(
                            where, reason, ", ".join(SESSION_REPLACEMENT_REASONS)
                        )
                    )
                elif reason == "parent-session-changed" and previous is not None and main == previous[1]:
                    violations.append(
                        "{0}: 'parent-session-changed' names the same main session as the replaced row".format(where)
                    )
            last_resolved[key] = (returned, main)
    return violations


def check_agent_sessions(
    repo: Path,
    records: Mapping[str, str] | None = None,
    handovers: Mapping[str, str] | None = None,
) -> int:
    """Name each agent-session register's violations; return their count."""
    if records is None:
        try:
            records = {
                unit: path.read_text(encoding="utf-8") for unit, path in agent_session_record_paths(repo).items()
            }
        except (OSError, UnicodeDecodeError) as failure:
            print("The agent-session registers cannot be read: {0}".format(failure), file=sys.stderr)
            return 1
    if handovers is None:
        handovers = {}
        for unit in records:
            path = repo / UNIT_RECORD_DIR / unit / HANDOVER_BASENAME
            try:
                handovers[unit] = path.read_text(encoding="utf-8") if path.exists() else ""
            except (OSError, UnicodeDecodeError):
                handovers[unit] = ""
    violations = find_agent_session_violations(records, handovers)
    for violation in violations:
        print("Agent-session continuity: {0}".format(violation), file=sys.stderr)
    return len(violations)


def find_session_record_rewrites(before: str | None, after: str | None) -> list[str]:
    """The committed rows a register loses or rewrites, each named by its position.

    The register is append-only, so the committed record's rows must be the staged record's prefix.
    """
    def rows(text: str | None) -> list[str]:
        return [line for line in (text or "").splitlines() if line.strip() and not line.lstrip().startswith("#")]

    if before is None:
        return []
    if after is None:
        return ["the committed register is removed"]
    old, new = rows(before), rows(after)
    for position, row in enumerate(old, start=1):
        if position > len(new):
            return ["row {0} is removed: {1}".format(position, row)]
        if new[position - 1] != row:
            return ["row {0} is rewritten: {1}".format(position, row)]
    return []


# The per-round finding-instances store: one file per recorded round, beside the record.
FINDING_INSTANCES_DIR = "findings"
# The last day a round may lack instances, because it was recorded before the store existed.
FINDING_INSTANCES_THROUGH = date(2026, 11, 3)
# One instance: its class, its location, and its claim, the claim keeping any later separator verbatim.
INSTANCE_LINE = re.compile(r"^- \[([a-z][a-z-]*)\] (\S+) — (.+)$")
INSTANCE_NONE = "- none"


def instance_path(unit: str, round_number: str) -> str:
    """The repository path of one round's instances file."""
    return UNIT_RECORD_DIR + "/" + unit + "/" + FINDING_INSTANCES_DIR + "/round-" + round_number + ".md"


def parse_instances(text: str) -> tuple[list[tuple[str, str, str]], list[str], bool]:
    """One instances file's entries, the lines outside the entry shape, and whether it states `- none`.

    An entry may be wrapped across source lines — the file sits under `docs/`, which the formatter reflows — so a line
    that does not open an entry continues the one above it, and the fields are read from the joined text.
    """
    entries: list[tuple[str, str, str]] = []
    unparseable: list[str] = []
    explicit_none = False
    pending: str | None = None

    def classify(value: str) -> None:
        match = INSTANCE_LINE.match(value)
        if match:
            entries.append((match.group(1), match.group(2), match.group(3)))
        else:
            unparseable.append(value)

    for line in text.splitlines():
        stripped = line.strip()
        if not stripped:
            continue
        if stripped == INSTANCE_NONE:
            if pending is not None:
                classify(pending)
                pending = None
            explicit_none = True
            continue
        if stripped.startswith("- "):
            if pending is not None:
                classify(pending)
            pending = stripped
            continue
        if pending is not None:
            pending = pending + " " + stripped
        else:
            unparseable.append(stripped)
    if pending is not None:
        classify(pending)
    return entries, unparseable, explicit_none


def tree_instances(repo: Path) -> dict[tuple[str, str], str]:
    """Every tracked round's instances file text, keyed by (unit, round)."""
    root = repo / UNIT_RECORD_DIR
    files: dict[tuple[str, str], str] = {}
    if not root.is_dir():
        return files
    for path in sorted(root.glob("*/" + FINDING_INSTANCES_DIR + "/round-*.md")):
        match = re.fullmatch(r"round-(\d+)\.md", path.name)
        if not match:
            continue
        unit = path.parent.parent.name
        try:
            files[(unit, match.group(1))] = path.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError):
            raise ValueError("cannot read the instances file: {0}".format(instance_path(unit, match.group(1))))
    return files


def find_instance_violations(
    records: Mapping[str, str], instances: Mapping[tuple[str, str], str]
) -> list[str]:
    """Every finding-instances violation a record set and its instances carry."""
    problems: list[str] = []
    recorded_rounds: dict[str, set[str]] = {}
    for unit, text in sorted(records.items()):
        for line in text.splitlines():
            if not line.strip() or line.lstrip().startswith("#"):
                continue
            fields = line.split("\t")
            if len(fields) < 4 or not fields[2].isdigit():
                continue
            round_number = fields[2]
            recorded_rounds.setdefault(unit, set()).add(round_number)
            row_date = ledger_date(fields[0])
            if row_date is None or row_date <= FINDING_INSTANCES_THROUGH:
                continue
            key = (unit, round_number)
            if key not in instances:
                problems.append(
                    "{0}: round {1} dated {2} has no instances file ({3})".format(
                        record_label(unit), round_number, fields[0], instance_path(unit, round_number)
                    )
                )
                continue
            entries, unparseable, explicit_none = parse_instances(instances[key])
            if unparseable:
                problems.append(
                    "{0}: round {1} carries {2} line(s) outside the instance shape, first {3!r}".format(
                        instance_path(unit, round_number), round_number, len(unparseable), unparseable[0]
                    )
                )
                continue
            if explicit_none and entries:
                problems.append(
                    "{0}: round {1} states `- none` beside {2} entry(ies)".format(
                        instance_path(unit, round_number), round_number, len(entries)
                    )
                )
                continue
            if not entries and not explicit_none:
                problems.append(
                    "{0}: round {1} carries no entry and no `- none`, so a clean round is recorded by silence".format(
                        instance_path(unit, round_number), round_number
                    )
                )
                continue
            recorded = {label.strip() for label in fields[3].split(",") if label.strip() and label.strip() != "-"}
            found = {name for name, _, _ in entries}
            if found != recorded:
                problems.append(
                    "{0}: round {1} names classes {2}, but its record carries {3}".format(
                        instance_path(unit, round_number),
                        round_number,
                        ", ".join(sorted(found)) or "none",
                        ", ".join(sorted(recorded)) or "none",
                    )
                )
    for unit, round_number in sorted(instances):
        if round_number not in recorded_rounds.get(unit, set()):
            problems.append(
                "{0} names round {1}, which {2} does not carry".format(
                    instance_path(unit, round_number), round_number, record_label(unit)
                )
            )
    return problems


def index_text(repo: Path, path: str) -> str:
    """The index's content of `path`, raised as a named failure when it is not readable text."""
    try:
        return git_output(repo, "show", ":" + path)
    except UnicodeDecodeError:
        raise ValueError("cannot read the index copy of {0}".format(path))


def staged_or_tree_text(repo: Path, path: str) -> str:
    """`path`'s staged content when it is staged, otherwise its working-tree content, or an empty string."""
    if git_output(repo, "ls-files", "--cached", "--", path).strip():
        try:
            return index_text(repo, path)
        except ValueError:
            return ""
    candidate = repo / path
    try:
        return candidate.read_text(encoding="utf-8") if candidate.exists() else ""
    except (OSError, UnicodeDecodeError):
        return ""


UNIT_LAYOUT_FILES = ("unit.md", "rounds.tsv", "review-handover.md", "process-defects.md", "sessions.tsv")


def layout_stray_paths(repo: Path) -> list[str]:
    """Every tracked path under a unit's directory that the layout's two levels do not admit."""
    prefix = UNIT_RECORD_DIR + "/"
    strays: list[str] = []
    for path in git_output(repo, "ls-files", "-z", "--", UNIT_RECORD_DIR).split("\0"):
        if not path.startswith(prefix):
            continue
        parts = path[len(prefix):].split("/")
        if len(parts) < 2:
            continue
        admitted = (len(parts) == 2 and parts[1] in UNIT_LAYOUT_FILES) or (
            len(parts) == 3 and parts[1] == FINDING_INSTANCES_DIR and re.fullmatch(r"round-\d+\.md", parts[2])
        )
        if not admitted:
            strays.append(path)
    return strays


def check_unit_layout(repo: Path) -> int:
    """Name each path the unit layout does not admit; return their count."""
    problems = layout_stray_paths(repo)
    for problem in problems:
        print("Unit layout: {0} is not a path the unit layout admits".format(problem), file=sys.stderr)
    return len(problems)


def check_finding_instances(repo: Path) -> int:
    """Name each finding-instances violation; return their count."""
    try:
        problems = find_instance_violations(round_records(repo), tree_instances(repo))
    except ValueError as failure:
        print("Review-round finding instances: {0}".format(failure), file=sys.stderr)
        return 1
    for problem in problems:
        print("Review-round finding instances: {0}".format(problem), file=sys.stderr)
    return len(problems)


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
    "--unique-change-names",
    "--session-continuity",
    "--agent-sessions",
    "--large-files",
    "--table-rows",
    "--ideas-sections",
    "--class-vocabulary",
    "--workspace-records",
    "--finding-instances",
    "--unit-layout",
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


def staged_change_name(path: str) -> Optional[str]:
    """The change name a staged `tasks.md` records: its directory's name, or the archive's stripped name."""
    if path.startswith(ARCHIVE_PREFIX):
        match = ARCHIVED_CHANGE_RE.match(path[len(ARCHIVE_PREFIX) :].split("/", 1)[0])
        return match.group("name") if match else None
    if path.startswith(CHANGES_PREFIX):
        return path[len(CHANGES_PREFIX) :].split("/", 1)[0]
    return None


def archive_record_contract(directory: str) -> str:
    """Which contract an archived change is judged by: "exempt", "inline", or "reference".

    One predicate serves both entry points, so the build check and the guard cannot disagree about which archive they
    judge. An archive dated before the gate's introduction, and one the gate acknowledges as record-less, is exempt; an
    archive dated on or after the record boundary is judged by its reference, apart from a boundary-dated one
    that landed before the change, which keeps its inline record. A name that carries no date is no archived change.
    """
    match = ARCHIVED_CHANGE_RE.match(directory)
    if match is None:
        return "exempt"
    date = match.group("date")
    if date < WORKSPACE_RECORD_ARCHIVE_SINCE or directory in ACKNOWLEDGED_RECORDLESS_ARCHIVES:
        return "exempt"
    if date >= UNIT_RECORD_SINCE and directory not in ACKNOWLEDGED_UNIT_RECORD_ARCHIVES:
        return "reference"
    return "inline"


def archived_change_out_of_scope(directory: str) -> bool:
    """Whether an archived change is outside this verification's scope; a non-archive path is not an archived change."""
    if ARCHIVED_CHANGE_RE.match(directory) is None:
        return False
    return archive_record_contract(directory) == "exempt"


def unit_record_relative(unit: str) -> str:
    """The unit's record path, relative to the repository root."""
    return "{0}/{1}/{2}".format(UNIT_RECORD_DIR, unit, UNIT_RECORD_BASENAME)


def unit_record_units(repo: Path) -> list[str]:
    """Every unit whose record exists, named by its own directory."""
    root = repo / UNIT_RECORD_DIR
    if not root.is_dir():
        return []
    return sorted(entry.name for entry in root.iterdir() if entry.is_dir() and (entry / UNIT_RECORD_BASENAME).is_file())


def find_invalid_unit_records(repo: Path) -> list[tuple[str, str]]:
    """The (path, defect) of each unit record the verification rejects, judged against its directory's name."""
    offenders: list[tuple[str, str]] = []
    for unit in unit_record_units(repo):
        path = unit_record_relative(unit)
        fields, reason = parse_workspace_record((repo / path).read_text(encoding="utf-8"))
        if reason is not None:
            offenders.append((path, reason))
        elif fields["unit"] != unit:
            offenders.append((path, "'unit' names '{0}', not the directory's '{1}'".format(fields["unit"], unit)))
    return offenders


def staged_archive_directory(path: str) -> str:
    """The archived change directory a staged path belongs to, or an empty string for a path outside the archive."""
    if not path.startswith(ARCHIVE_PREFIX):
        return ""
    return path[len(ARCHIVE_PREFIX) :].split("/", 1)[0]


def staged_unit_record(path: str) -> Optional[str]:
    """The unit a staged record belongs to, or None for a path outside `docs/units/<unit>/unit.md`."""
    if not path.startswith(UNIT_RECORD_DIR + "/") or not path.endswith("/" + UNIT_RECORD_BASENAME):
        return None
    return path[len(UNIT_RECORD_DIR) + 1 : -len("/" + UNIT_RECORD_BASENAME)]


def check_staged_workspace_records(repo: Path, paths: Sequence[str]) -> int:
    """The guard's judgment over the staged set: the build check's rule and scope, plus the tree and branch it names.

    A staged unit record is judged against the tree, branch, and unit it names. A staged `tasks.md` is judged by its
    reference when the unit record exists, and by the fields it carries otherwise, so a unit in flight keeps committing.
    An archive dated before the boundary, or one the acknowledgement names, is outside the same scope the build check
    applies, so an edit to history is not re-judged. A record a merge brings unchanged is exempt, since merged
    content is not the committing unit's own. The branch comparison is skipped on a detached head, which is git's
    answer rather than a failed read; an unreadable top level or branch raises, which `main` reports as the check
    having failed.
    """
    staged = [
        path
        for path in paths
        if (staged_unit_record(path) is not None)
        or (
            (path.startswith(CHANGES_PREFIX) or path.startswith(ARCHIVE_PREFIX))
            and path.endswith(ARCHIVED_TASKS_BASENAME)
            and not archived_change_out_of_scope(staged_archive_directory(path))
        )
    ]
    if not staged:
        return 0
    revisions = merge_revisions(repo)
    toplevel = git_output(repo, "rev-parse", "--show-toplevel").strip()
    branch = current_branch(repo)
    offenders: list[tuple[str, str]] = []
    for path in staged:
        if revisions and merge_carries_unchanged(repo, path, revisions):
            continue
        unit = staged_unit_record(path)
        if unit is not None:
            fields, reason = parse_workspace_record(staged_file_text(repo, path))
            if reason is not None:
                offenders.append((path, reason))
                continue
            if fields["unit"] != unit:
                offenders.append((path, "'unit' names '{0}', not the directory's '{1}'".format(fields["unit"], unit)))
                continue
            if fields["path"].rstrip("/") != toplevel:
                offenders.append(
                    (path, "'path' names '{0}', not the worktree's '{1}'".format(fields["path"], toplevel))
                )
                continue
            if branch and fields["branch"] != branch:
                offenders.append((path, "'branch' names '{0}', not '{1}'".format(fields["branch"], branch)))
            continue
        name = staged_change_name(path)
        if name is None:
            continue
        text = staged_file_text(repo, path)
        archived = path.startswith(ARCHIVE_PREFIX)
        contract = archive_record_contract(staged_archive_directory(path)) if archived else "live"
        record = unit_record_relative(name)
        if contract == "reference" or (contract == "live" and (repo / record).is_file()):
            if record not in text:
                offenders.append((path, "no reference to the unit record: {0}".format(record)))
            elif any(WORKSPACE_MARKER_RE.match(line) for line in text.splitlines()):
                offenders.append((path, "the record's fields are copied here as well as referenced"))
            continue
        fields, reason = parse_workspace_record(text)
        if reason is not None:
            offenders.append((path, reason))
            continue
        if fields["unit"] != name:
            offenders.append((path, "'unit' names '{0}', not the directory's '{1}'".format(fields["unit"], name)))
            continue
        if fields["path"].rstrip("/") != toplevel:
            offenders.append(
                (path, "'path' names '{0}', not the worktree's '{1}'".format(fields["path"], toplevel))
            )
            continue
        if branch and fields["branch"] != branch:
            offenders.append((path, "'branch' names '{0}', not '{1}'".format(fields["branch"], branch)))
    return report_invalid_workspace_records(offenders)


def report_empty_idea_sections(offenders: list[tuple[int, str]]) -> int:
    """Print each offender as a named failure and return how many there are, shared by the build check and the guard."""
    for number, heading in offenders:
        print(
            "A dated ideas section carries no entry: {0}:{1} {2}".format(IDEAS_PATH, number, heading),
            file=sys.stderr,
        )
    return len(offenders)


def parse_workspace_record(text: str) -> tuple[dict[str, str], Optional[str]]:
    """The record's four fields, and a reason naming what is wrong when it cannot be read.

    The record is one logical line from a `Workspace:` marker at column zero, optionally behind a backtick, to the first
    blank line, so the three forms the archive records all parse. One trailing sentence period is dropped from a value.
    """
    lines = text.splitlines()
    start = next((number for number, line in enumerate(lines) if WORKSPACE_MARKER_RE.match(line)), None)
    if start is None:
        return {}, "no {0} record".format(WORKSPACE_MARKER)
    collected: list[str] = []
    for line in lines[start:]:
        if collected and (not line.strip() or RECORD_FIELD_RE.match(line) or line.startswith("#")):
            break
        collected.append(line.strip())
    record = " ".join(collected).strip().strip("`").strip()
    if record.startswith(WORKSPACE_MARKER):
        record = record[len(WORKSPACE_MARKER) :].strip().strip("`").strip()
    fields: dict[str, str] = {}
    for part in record.split(";"):
        item = part.strip()
        if not item:
            continue
        key, _, value = item.partition("=")
        key = key.strip()
        value = value.strip()
        if value.endswith("."):
            value = value[:-1]
        if key not in WORKSPACE_FIELDS:
            return {}, "a field the format does not name: '{0}'".format(key)
        fields[key] = value.strip()
    for name in WORKSPACE_FIELDS:
        if not fields.get(name):
            return {}, "no '{0}' field".format(name)
    if not fields["path"].startswith("/"):
        return {}, "a relative 'path': '{0}'".format(fields["path"])
    if not WORKSPACE_WRITER_RE.match(fields["writer"]):
        return {}, "a 'writer' that is not a session identifier: '{0}'".format(fields["writer"])
    return fields, None


def workspace_record_paths(repo: Path) -> list[tuple[str, str]]:
    """Each `tasks.md` this verification judges, with the change name its record must carry."""
    paths: list[tuple[str, str]] = []
    changes = repo / CHANGES_PREFIX
    if changes.is_dir():
        for entry in sorted(changes.iterdir()):
            if entry.is_dir() and entry.name != "archive" and (entry / "tasks.md").is_file():
                paths.append(("{0}{1}/tasks.md".format(CHANGES_PREFIX, entry.name), entry.name))
    archived = repo / ARCHIVE_PREFIX
    if archived.is_dir():
        for entry in sorted(archived.iterdir()):
            match = ARCHIVED_CHANGE_RE.match(entry.name)
            if not (entry.is_dir() and match and (entry / "tasks.md").is_file()):
                continue
            if archived_change_out_of_scope(entry.name):
                continue
            paths.append(("{0}{1}/tasks.md".format(ARCHIVE_PREFIX, entry.name), match.group("name")))
    return paths


def find_invalid_workspace_records(repo: Path) -> list[tuple[str, str]]:
    """The (path, defect) of each `tasks.md` the workspace-record verification rejects.

    A change whose unit record exists is judged by its reference to that record; one without a record is still
    judged by the fields it carries, so a unit in flight before the move keeps committing. An archive is judged by the
    contract its date selects, and a reference contract requires the record it names to be present.
    """
    offenders: list[tuple[str, str]] = []
    for path, name in workspace_record_paths(repo):
        text = (repo / path).read_text(encoding="utf-8")
        archived = path.startswith(ARCHIVE_PREFIX)
        contract = archive_record_contract(staged_archive_directory(path)) if archived else "live"
        record = unit_record_relative(name)
        if contract == "reference" or (contract == "live" and (repo / record).is_file()):
            if record not in text:
                offenders.append((path, "no reference to the unit record: {0}".format(record)))
            elif any(WORKSPACE_MARKER_RE.match(line) for line in text.splitlines()):
                offenders.append((path, "the record's fields are copied here as well as referenced"))
            continue
        fields, reason = parse_workspace_record(text)
        if reason is not None:
            offenders.append((path, reason))
        elif fields["unit"] != name:
            offenders.append((path, "'unit' names '{0}', not the directory's '{1}'".format(fields["unit"], name)))
    return offenders


def report_invalid_workspace_records(offenders: list[tuple[str, str]]) -> int:
    """Print each offender as a named failure and return how many there are, shared by the build check and the guard."""
    for path, defect in offenders:
        print("The workspace record does not verify ({0}): {1}".format(path, defect), file=sys.stderr)
    return len(offenders)


def assigned_unit(repo: Path) -> Optional[str]:
    """The unit this tree is assigned to: its claim's change, a change-named branch, or any other named branch.

    A detached head and the primary's `main` name no unit, so neither is an assignment to assert.
    """
    claim = claim_file(repo)
    recorded = None
    if claim.is_file():
        lines = [line.strip() for line in claim.read_text(encoding="utf-8").splitlines() if line.strip()]
        recorded = lines[0] if len(lines) == 1 else None
    if recorded and recorded != CLAIM_NONE:
        return recorded
    branch = current_branch(repo)
    if not branch or branch in ("main", "master"):
        return None
    if branch in change_names(repo):
        return branch
    for unit in unit_record_units(repo):
        fields, _ = parse_workspace_record((repo / unit_record_relative(unit)).read_text(encoding="utf-8"))
        if fields.get("branch") == branch:
            return unit
    if recorded == CLAIM_NONE:
        return branch
    return None


def find_missing_assignments(repo: Path) -> list[tuple[str, str]]:
    """The unit record this tree's assignment requires, reported when neither it nor a legacy record exists."""
    unit = assigned_unit(repo)
    if unit is None:
        return []
    record = unit_record_relative(unit)
    if (repo / record).is_file():
        return []
    tasks = repo / CHANGES_PREFIX / unit / "tasks.md"
    if tasks.is_file():
        return []
    return [(record, "the unit's record is missing for the unit this tree is assigned to")]


def check_workspace_records(repo: Path) -> int:
    """Verify every unit record, every live change's record or reference, every archive in scope, and the assignment."""
    offenders = find_invalid_unit_records(repo) + find_invalid_workspace_records(repo) + find_missing_assignments(repo)
    return report_invalid_workspace_records(offenders)


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


def read_review_definition(repo: Path) -> str:
    """The reviewer's definition, whose canonical class list the ledger's classes are checked against."""
    path = repo / REVIEW_QUICK_DEFINITION
    if not path.is_file():
        raise FileFailure("the reviewer's definition is missing: {0}".format(REVIEW_QUICK_DEFINITION))
    try:
        return path.read_text(encoding="utf-8")
    except OSError as error:
        raise FileFailure("{0} could not be read: {1}".format(REVIEW_QUICK_DEFINITION, error)) from error


def ledger_date(value: str) -> Optional[date]:
    """The calendar date a ledger value states, or `None` when it is not a written-out `YYYY-MM-DD`."""
    try:
        parsed = date.fromisoformat(value)
    except ValueError:
        return None
    return parsed if parsed.isoformat() == value else None


def review_rounds_boundary(repo: Path) -> date:
    """The last day whose ledger rows may carry a pre-canonical label, from the digest script that owns it."""
    path = repo / REVIEW_ROUNDS_SCRIPT
    if not path.is_file():
        raise FileFailure("the digest script that owns the canonical boundary is missing: {0}".format(
            REVIEW_ROUNDS_SCRIPT
        ))
    spec = importlib.util.spec_from_file_location("review_rounds_boundary", path)
    if spec is None or spec.loader is None:
        raise FileFailure("{0} cannot be loaded".format(REVIEW_ROUNDS_SCRIPT))
    module = importlib.util.module_from_spec(spec)
    try:
        spec.loader.exec_module(module)
    except Exception as error:
        raise FileFailure("{0} could not be loaded: {1}".format(REVIEW_ROUNDS_SCRIPT, error)) from error
    boundary = getattr(module, "PRE_CANONICAL_THROUGH", None)
    if not isinstance(boundary, str) or not boundary:
        raise FileFailure("{0} declares no PRE_CANONICAL_THROUGH".format(REVIEW_ROUNDS_SCRIPT))
    declared = ledger_date(boundary)
    if declared is None:
        raise FileFailure(
            "{0} declares PRE_CANONICAL_THROUGH {1!r}, which is not a calendar date".format(
                REVIEW_ROUNDS_SCRIPT, boundary
            )
        )
    return declared


def acknowledged_class_triples(text: str) -> set[tuple[str, int, str]]:
    """The (unit, round, label) triples a record's acknowledgement comments exempt."""
    triples: set[tuple[str, int, str]] = set()
    for line in text.splitlines():
        match = ACKNOWLEDGED_CLASS_RE.match(line.strip())
        if match:
            triples.add((match.group("unit"), int(match.group("round")), match.group("label")))
    return triples


def find_unknown_classes(repo: Path) -> list[tuple[str, int, str, int, str, str]]:
    """Every post-boundary class the canonical list does not name and no acknowledgement exempts.

    A row whose date, round, or classes column cannot be read raises rather than being skipped, and no record at all
    raises rather than reading as zero rows.
    """
    records = round_records(repo)
    if not records:
        raise FileFailure("the review-round records are missing: {0}".format(UNIT_RECORD_DIR))
    classes, reason = canonical_classes(read_review_definition(repo))
    if reason is not None:
        raise CheckFailure(reason)
    canonical = set(classes)
    boundary = review_rounds_boundary(repo)
    offenders: list[tuple[str, int, str, int, str, str]] = []
    for unit, text in sorted(records.items()):
        path = record_label(unit)
        acknowledged = acknowledged_class_triples(text)
        for number, line in enumerate(text.splitlines(), start=1):
            if not line.strip() or line.lstrip().startswith("#"):
                continue
            fields = line.split("\t")
            if len(fields) < len(LEDGER_CLASS_COLUMNS) + 3:
                raise CheckFailure(
                    "{0}:{1} carries {2} field(s): the rows carry both class columns".format(
                        path, number, len(fields)
                    )
                )
            row_date = ledger_date(fields[0])
            if row_date is None:
                raise CheckFailure("{0}:{1} carries an unreadable date {2!r}".format(path, number, fields[0]))
            if row_date <= boundary:
                continue
            if not fields[2].isdigit():
                raise CheckFailure("{0}:{1} carries an unreadable round {2!r}".format(path, number, fields[2]))
            round_number = int(fields[2])
            for column, index in LEDGER_CLASS_COLUMNS:
                for raw in fields[index].split(","):
                    label = raw.strip()
                    if label == "-":
                        continue
                    if not label:
                        raise CheckFailure(
                            "{0}:{1} carries no readable label in its {2} column".format(path, number, column)
                        )
                    if label in canonical or (unit, round_number, label) in acknowledged:
                        continue
                    offenders.append((path, number, unit, round_number, column, label))
    return offenders


def check_class_vocabulary(repo: Path) -> int:
    """Name each post-boundary class the reviewer's canonical list does not name; return their count."""
    offenders = find_unknown_classes(repo)
    for path, number, unit, round_number, column, label in offenders:
        print(
            '{0}:{1} {2} round {3} {4} "{5}" is not in the reviewer\'s canonical class list ({6})'.format(
                path, number, unit, round_number, column, label, REVIEW_QUICK_DEFINITION
            ),
            file=sys.stderr,
        )
    return len(offenders)


def build_parser() -> argparse.ArgumentParser:
    """The script's argument parser, so a test can read the mode group it declares."""
    parser = argparse.ArgumentParser(
        description=(
            "Check commit hygiene over the staged set, the unit's readiness for the manual review pass, AGENTS.md "
            "markers, the tracked set, the workflow cron schedules, the archived change names, the review-round "
            "ledger, a file's size, the given markdown files' table rows, the ideas file's dated sections, the "
            "review-round classes, or a change's workspace record."
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
        "--agent-sessions",
        action="store_true",
        help="check each unit's agent-session register's format, continuity, and hand-over links",
    )
    mode.add_argument(
        "--unique-change-names",
        action="store_true",
        help="check no two archived changes carry the same name",
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
    mode.add_argument(
        "--class-vocabulary",
        action="store_true",
        help="check the review-round ledger's classes against the reviewer's canonical list",
    )
    mode.add_argument(
        "--workspace-records",
        action="store_true",
        help="check each live change's tasks.md records its workspace, and each archive after the gate's date",
    )
    mode.add_argument(
        "--finding-instances",
        action="store_true",
        help="check each round recorded after the boundary has its finding instances",
    )
    mode.add_argument(
        "--unit-layout",
        action="store_true",
        help="check a unit's directory holds only the record kinds the layout admits",
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
    if args.unique_change_names:
        return 1 if check_unique_change_names(repo) else 0
    if args.session_continuity:
        return 1 if check_session_continuity(repo) else 0
    if args.agent_sessions:
        return 1 if check_agent_sessions(repo) else 0
    if args.large_files:
        return 1 if check_large_files(repo) else 0
    if args.table_rows:
        return 1 if check_table_rows(repo, args.paths) else 0
    if args.ideas_sections:
        return 1 if check_idea_sections(repo) else 0
    if args.class_vocabulary:
        return 1 if check_class_vocabulary(repo) else 0
    if args.workspace_records:
        return 1 if check_workspace_records(repo) else 0
    if args.finding_instances:
        return 1 if check_finding_instances(repo) else 0
    if args.unit_layout:
        return 1 if check_unit_layout(repo) else 0
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
