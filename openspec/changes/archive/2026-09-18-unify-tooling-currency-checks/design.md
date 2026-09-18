## Context

See `proposal.md` for motivation. The current state that shapes the approach:

- Three update checks already share one shape: a task (or the `gradle-versions` plugin) writes
  `build/<name>/report.txt`, and a dedicated workflow runs it on a cron schedule, then opens or updates an issue from
  the report and mentions the owner only when something is actionable. The issue-posting bash block is ~40 lines and is
  duplicated verbatim across the two Gradle-task workflows, while the dependency workflow carries a longer variant of
  its own.
- `BuildpackUpdatesTask` and `HelmUpdatesTask` query with the JDK `HttpClient` and parse responses with `Regex(...)` —
  `build-logic` has no JSON library — and each carries its own private version-comparison helpers (`compareVersions`,
  `numericParts`), so those are duplicated too.
- The pins this change covers live in the workflow files rather than the version catalog — two as `with:` inputs, the
  third as an `npm install` argument. A workflow cannot read the Gradle catalog directly, so a pinned value would need a
  resolution step to reach it, and the pin stays where CI consumes it for now.
- The root `build.gradle.kts` registration supplies each existing check's pinned version — the buildpack and Helm checks
  read them from `libs.versions.*`, which works because those coordinates are catalog-owned.

## Goals / Non-Goals

**Goals:**

- One declared list of pinned tools drives one task and one workflow, so a fourth pin is a list entry rather than a
  fourth task and a fourth workflow.
- Each pin stays single-sourced where CI reads it — the workflow file.
- The report and the workflow keep the existing observable behaviour: `<name>: <pinned> -> <latest>` lines, an issue
  opened or updated, the owner mentioned only when actionable, and nothing in the merge gate.

**Non-Goals:**

- Folding `helmUpdates` and `buildpackUpdates` into the new task (see Decisions) — the one exception is the Helm CLI
  release-tag pattern, whose parse this change found broken and fixes.
- Extracting the duplicated issue-posting bash into a shared script or composite action (see Decisions).
- Moving the pins into the version catalog.

## Decisions

### One parameterized task over a declared list, not a task per tool

`ToolingUpdatesTask` takes a declared list of checks — each with a display `name`, a version source, and the workflow
file plus input key its pin lives in — plus one report file (`build/tooling-updates/report.txt`), mirroring
`BuildpackUpdatesTask`'s shape: an `@Input` list, an `@OutputFile` report, and a `@TaskAction` that queries each source
and writes `<name>: <pinned> -> <latest>` lines, or the single "no updates" line.

- _Alternative — an `openspecUpdates` task per the parked idea:_ rejected. It is the fourth near-duplicate the repo's
  own parked note warns against, and it would leave two of the three uncovered pins still uncovered.
- _Alternative — one task per tool sharing a workflow:_ rejected. Three more task classes for one report shape, and the
  duplication the note objects to is in the task _and_ workflow pair.

### The pinned version is read from the workflow that declares it

Each check declares where its pin lives (a workflow file plus the input key), and the task reads the current value from
that file. The pin therefore stays single-sourced where CI consumes it, and the check cannot drift from what CI runs.

- _Alternative — move the pins into the version catalog, as the `*-image-tag` entries already do for non-dependency
  values:_ rejected for this change. A `with:` value can only come from a prior step's output, so two of the three
  workflows would have to resolve the catalog at runtime and pass the value in — more moving parts in three working
  workflows than reading the pin where CI already consumes it. Worth revisiting if the count grows.
- _Alternative — hard-code the pinned versions in the task registration, as the existing checks do:_ rejected for these
  pins specifically, because a workflow input is the only place the value actually runs.

### Two version sources, declared per check

A check names its source kind and the exact selector to read: the npm registry's `dist-tags.latest` entry (the OpenSpec
CLI — `latest` is not the first entry in that object, and a first-match regex would read `next`), or a GitHub release's
`tag_name` (the Snyk and `pack` CLIs, whose tags are themselves `v`-prefixed). The task parses each response with regex,
as the existing checks do, so `build-logic` gains no dependency. Both the pin read from the workflow and the tag the
source returns are normalized by stripping a leading `v` before comparison: the Snyk pin carries one
(`snyk-version: v1.1307.2`) and the `pack` pin does not (`0.40.9`), while the existing helpers derive no numeric parts
from a `v`-prefixed string and would leave the Snyk check silent.

- _Alternative — add a JSON library to `build-logic`:_ rejected. The two existing checks already parse this way, and a
  dependency for three single-field reads is not worth the supply-chain surface.

The three checks, then, are:

| Check        | Pin                                  | Source                    |
| ------------ | ------------------------------------ | ------------------------- |
| OpenSpec CLI | `ci.yml`, the `npm install` argument | npm `dist-tags.latest`    |
| Snyk CLI     | `snyk.yml`, `snyk-version:`          | GitHub release `tag_name` |
| `pack` CLI   | `e2e.yml`, `pack-version:`           | GitHub release `tag_name` |

### The new check carries its own version comparison, with tests

The two existing tasks' comparisons disagree, so they are not shareable: `BuildpackUpdatesTask.compareVersions` breaks a
tie on segment count (`return aParts.size - bParts.size`), which makes `1.2.0` newer than `1.2`, while
`HelmUpdatesTask.isNewer` returns `false` for that same pair. The new task implements its own comparison (numeric
segments, longer-is-newer) with unit tests beside the existing `build-logic` tests; the existing checks keep theirs
until they are folded in, which is a Non-Goal here.

- _Alternative — extract one shared comparator now:_ rejected. With the two existing behaviours disagreeing, one
  implementation either changes a working check's result or needs a mode neither check uses — more risk than a change
  that is only adding coverage should take on.

### The check's inputs include the workflow files it reads

Each check declares the workflow file it reads its pin from as an input, and the registration mirrors the existing
tasks' `outputs.upToDateWhen { false }`, so editing a pin re-runs the check instead of leaving a stale report — which
would also make the positive control in task 1.4 silently vacuous.

### The Helm CLI parse is fixed rather than only noted

The review of this change found that `HelmUpdatesTask`'s Helm CLI lookup used the same no-space `tag_name` pattern, and
that the GitHub releases API returns pretty-printed JSON, so the lookup had always returned null and the Helm CLI was
never compared (hidden because the pin happened to equal the latest release). Correctness fixes go in the change that
finds them, even in a check the change otherwise leaves alone, and both tasks now share one pattern with a unit test
asserting the pretty and compact shapes — so a regression to the no-space form fails the build. Both GitHub lookups also
send the CI token when one is provided, because the anonymous rate limit makes a throttled response indistinguishable
from a current version — the two update workflows pass `github.token` to their report step (the token is attached only
to GitHub requests, never to the npm one).

## Risks / Trade-offs

- **Reading a pin out of a workflow file with a regex is brittle** → each check asserts it matched exactly once and
  fails the task naming the file and key, rather than silently reporting the tool as current when the read failed.
- **A failed source lookup reads as "no update"** — a property the existing checks share and the repo already records as
  a gotcha → the new check keeps the same mitigation those checks used: verify it by temporarily pinning a known-older
  version and confirming the report shows `<name>: <old> -> <latest>` before trusting a clean run.
- **The duplicated issue-posting block grows from three copies to four** → accepted for this change. Extracting it would
  touch three working workflows, and the new workflow copies the shape verbatim so the copies stay comparable; a future
  change can extract it once for all four.
