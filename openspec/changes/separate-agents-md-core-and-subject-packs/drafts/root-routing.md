# Root replacement draft

This file drafts every `AGENTS.md` edit the subject-pack unit needs, plus the supporting edits whose behavior a reviewer
must be able to check: the new routing section, the minimal retained `## Build & Test`, the pointer at each vacated
section, the capture-routing enumerations, the reviewer definitions and command, the auditor, the upstream-reference
scan, the OpenSpec config, the README, and the parked idea. Text is written for `spotlessApply` to wrap.

## New section, inserted after `## Project Overview`

```markdown
## Subject guidance packs

Most of this file is guidance every task needs. Three packs hold technical guidance whose decision belongs to one
subject, and they load in full before the first decision in that subject.

| Pack              | Load it before you                                                                                                                                                                                                                                    | Path                                        |
| ----------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------- |
| `jvm-development` | choosing, writing, or reviewing JVM production code, or forming the first hypothesis or running the first diagnostic experiment in a JVM runtime diagnosis                                                                                            | `.opencode/skills/jvm-development/SKILL.md` |
| `jvm-testing`     | choosing, writing, running, or interpreting a JVM test or its coverage, or forming the first hypothesis or running the first diagnostic experiment for a test failure                                                                                 | `.opencode/skills/jvm-testing/SKILL.md`     |
| `gradle-build`    | changing a Gradle build decision; running or interpreting a Gradle task or an update or security report **when its result decides a build question**; or forming the first hypothesis or running the first diagnostic experiment for a Gradle failure | `.opencode/skills/gradle-build/SKILL.md`    |

A **governed decision** is a choice, an edit, an execution, a review judgment, a report interpretation, or a diagnosis —
not only an edit. A request that runs an existing test or reads a dependency report is a governed decision: load the
pack before selecting the command or the experiment, not after concluding its result. For a change proposal, classify by
what the change will decide, not by whether it touches a file yet.

- A diagnosis loads its pack **before the first hypothesis or the first diagnostic experiment**, not after the
  conclusion: the moved rules choose the experiment (which rerun, which probe, which cast), so a probe they govern must
  not run first.
- The root's `spotlessApply` and minimal `check` are verification commands: running them does not fire a pack. A failure
  whose cause is a build question loads `gradle-build` before the cause is concluded. `jvm-testing` owns a coverage run
  and its rule; a coverage-gate failure that turns on the build's own configuration loads `gradle-build` at that point.
- Load every pack whose row matches the task; a JVM change that writes production code and tests loads both
  `jvm-development` and `jvm-testing`. A test run is `jvm-testing`'s; a build question it raises — a missing image, a
  suite misconfiguration — loads `gradle-build` when that decision is reached.
- IF no row matches, THEN load none.
- Before your first edit, state one `Packs:` line naming every pack you loaded in full, or `Packs: none` with the
  reason. The line reports the classification; it does not prove the content was loaded.
- IF a pack is missing or unreadable, THEN stop and report it; do not proceed from memory or from the root pointers.
- IF investigation reveals a second subject, THEN load its pack before the next decision in that subject.
- A delegated agent does not inherit a pack its caller loaded: name every applicable pack in its prompt.
- A reviewer classifies the decisions and claims in the artifacts it is given — the proposal, design, delta specs, and
  diff, with changed paths as evidence rather than the only predicate — and loads every pack they turn on. The caller
  names the applicable packs in the reviewer's hand-over; a subject evident only in those artifacts is reported as a
  missing input, never searched for.
- After context compaction or a resumed session, re-classify and reload before the next governed decision.

An obligation lives in exactly one pack; where a pack needs another's rule it names it. Each pack carries a `Source:`
line naming the `AGENTS.md` headings it replaced.
```

## `## Conventions` — pointer bullet, appended to the section

```markdown
- **JVM and Gradle conventions live in the subject packs.** `jvm-development` carries the value-type, nullness,
  MapStruct, gateway-routing, and runtime-library rules; `jvm-testing` the test naming, tier, slice, logging, container,
  coverage, and execution rules; `gradle-build` the catalog, convention-plugin, gate, command, and report rules. Load
  the pack the classification table names before the decision it governs. captured:
  separate-agents-md-core-and-subject-packs
```

## `## Build & Test` — replacement, keeping the verification entry point and the non-JVM subjects

````markdown
## Build & Test

Every implementation change runs the touched module's gates before it is reported done. The Docker-free form keeps the
coverage gate green without integration data:

```bash
./gradlew :showcase-command-service:check -PskipITs -Pcoverage.gate.enabled=false
```

`check` runs the gates the `check` block in `build.gradle.kts` enumerates. The full Gradle command surface, the
dependency-update reports, and the build-failure gotchas are in the `gradle-build` pack; the test commands, tiers, and
execution prerequisites are in `jvm-testing`. `e2eTest` is never part of `check` and is a separate opt-in task.

```bash
# Run web UI end-to-end tests (separate opt-in task; builds service images + boots infra via docker compose,
# serves the built UI via vite preview, drives it with Playwright, then tears the stack down)
./gradlew :showcase-web-ui:e2eTest

# Load tests (Gatling; opt-in, targets the local Helm cluster via its gateway ingress hostname)
./gradlew :load-tests:gatlingRun -Pprofile=smoke
# `gatlingRun` runs the simulation and `:load-tests:test` starts nothing; the property set (each passed as `-P<name>`),
# the profile set and their assertions, the threshold derivation and its fallback values, and the baseline mechanics are
# `showcase/quality/load-tests`'s. The measurement run is `./scripts/load-test-baseline.sh`, whose docstring states the
# run's tolerance and refresh variables and the floors they are compared against. The calibration deliberately ramps
# past the knee and the resource sampling follows the current kube context (`kubectl top pods -A`), so point a run at an
# environment you own.

# Trend the dated records as a chronological series
./gradlew :load-tests:baselineTrend
# reads docs/load-tests/*.md by default (-Precords=<dir> overrides it); the record's fields are the load-tests spec's,
# and a per-request series across recorded runs comes from git
# (git show <commit>:load-tests/src/gatling/resources/baseline.properties).

# Web UI npm vulnerability audit (fails on high-severity findings; not part of check)
./gradlew :showcase-web-ui:npmAudit

# Web UI npm update report (not part of check)
./gradlew :showcase-web-ui:npmOutdated

# Frontend (showcase-web-ui): install, lint, format-check, type-check, tests (verification; the production bundle
# is built by `build`/`assemble`, like the JVM modules' bootJar)
./gradlew :showcase-web-ui:check
```

The `/opsx-tool-update` command regenerates the OpenSpec command/skill instruction files after a new `openspec` CLI
release.

The Helm update check has its own suppression file, `config/helm-updates/major-disabled.txt`: major bumps of the bitnami
infra charts (postgres, kafka, opensearch) are suppressed there because a major chart ships a new preconfigured
`image.tag` that diverges from the test-surface `*-image-tag` pins (docker-compose/Testcontainers) — a coordinated
migration, not an automatic update. The observability charts (kps, tempo) carry no `*-image-tag`, so their major bumps
surface as actionable; verify them with a live install + smoke test (pods ready, Prometheus targets up, Grafana
datasources wired), since `verifyInfraImageVersions` covers only the bitnami infra charts.

The web UI's npm report has a third suppression list, `config/web-ui-updates/major-disabled.txt`: its listed packages'
major-only rows are filtered out of `npmOutdated` (`NpmOutdatedRules`), while their same-major updates stay reported —
`npm outdated` has no `rejectVersionIf` equivalent, so the filter runs on the report's text.

**DB scripts** — before running the command-service standalone (outside Docker), ensure the PostgreSQL event store is
initialized:

```bash
./db.sh init   # creates user `showcase` and database `showcase-events` if absent
./db.sh reset  # drops the database and recreates it
```
````

## `## Gotchas` — pointers at the three vacated locations

```markdown
### Build, Gradle & dependency management

**Moved:** the build and dependency-management gotchas are in the `gradle-build` pack
(`.opencode/skills/gradle-build/SKILL.md`). Load it before changing a build decision, running or interpreting a Gradle
task or report, or concluding the cause of a Gradle failure.

### Runtime code, libraries & Spring

**Moved:** the runtime-library and Spring semantics gotchas are in the `jvm-development` pack
(`.opencode/skills/jvm-development/SKILL.md`). Load it before concluding a runtime diagnosis.

### Testing, measurement & load verification

**Moved:** the JVM test authoring, execution, and coverage rules are in the `jvm-testing` pack
(`.opencode/skills/jvm-testing/SKILL.md`). The measurement and load-test rules stay below; load `jvm-testing` before
choosing a test's tier, writing or running a JVM test, or interpreting its result.
```

## `## Conventions` — README design-intent amendment

Current clause: "`AGENTS.md` carries the agent's full command surface and its environment/port reference".

```markdown
- **README design intent**: … Keep the two files' division of labour as it stands rather than extracting one into the
  other or into a task runner: the root `AGENTS.md` carries the verification command every task needs and the
  environment and port reference, each subject pack carries its subject's full command surface, and the README carries
  the human onboarding path. …
```

## Capture routing — `## OpenSpec Workflow Agreement → Workflow: Capture lessons`

Four edits, each a destination enumeration or the capture record's figure.

Step 2:

```markdown
additions to the artifact that owns each lesson — an `AGENTS.md` gotcha or convention, a check/test authoring mode in
the `check-and-test-authoring` skill, a shipping-procedure correction in the `ship-change` skill, or a section of the
subject pack (`jvm-development`, `jvm-testing`, or `gradle-build`) whose subject owns the lesson's decision — the rules
the unit makes obsolete or redundant as retirement or replacement candidates, and any authoring-system recommendation,
```

Step 3:

```markdown
3. Apply the proposals the main agent judges durable, and record the outcome on the capture record — the applied net
   `AGENTS.md` delta (or the pack's delta when the lesson landed there), or the skip and its reason. An implementation
   capture is caused by the unit, so it rides the unit's branch and ships in its PR.
```

Decision point "Every proposal names the existing rule it extends":

```markdown
- **Every proposal names the existing rule it extends** — an `AGENTS.md` bullet, a check/test authoring mode already in
  the `check-and-test-authoring` skill, a `ship-change` procedure, or a subject-pack section — or states that none
  covers the lesson, so a new rule merges into or replaces one rather than accreting.
```

Merge-time capture paragraph:

```markdown
4. A merge runs no capture: at the merge, read what the merge alone affected — its non-diff effects, such as an agent PR
   closing its own tracker — and report any candidate lesson it leaves with the rule it would extend (an `AGENTS.md`
   bullet, a mode in `check-and-test-authoring`, a `ship-change` procedure, or a subject-pack section), then ask the
   user for explicit confirmation before running one. Apply that merge-time capture as its own docs PR (it has no
   in-flight branch to ride).
```

## `lesson-capture.md` — destination edits

Its three destination lists and its two figures must agree with the root and the capture requirement. The edits:

- **Frontmatter `description`**: "into the repo's guidance — AGENTS.md, the check-and-test-authoring skill, or the
  ship-change skill" → "into the repo's guidance — AGENTS.md, a subject pack (jvm-development, jvm-testing, or
  gradle-build), the check-and-test-authoring skill, or the ship-change skill".
- **Opening paragraph**: "`AGENTS.md`, a check or test authoring mode in `check-and-test-authoring`, or a
  shipping-procedure correction in `ship-change`" → "`AGENTS.md`, a section of the owning subject pack, a check or test
  authoring mode in `check-and-test-authoring`, or a shipping-procedure correction in `ship-change`".
- **"You are given" bullet**: "the `AGENTS.md` bullet, a mode already in `check-and-test-authoring`, or a `ship-change`
  procedure" → add "or a subject-pack section".
- **Destination paragraph**: "an `AGENTS.md` bullet, a section of the `check-and-test-authoring` skill for a check/test
  authoring mode, a shipping-procedure correction in the `ship-change` skill, or a pair" → "an `AGENTS.md` bullet, a
  section of the owning subject pack, a section of the `check-and-test-authoring` skill for a check/test authoring mode,
  a shipping-procedure correction in the `ship-change` skill, or a pair".
- **"Name the existing rule" clause**: add "or a subject-pack section".
- **"Do NOT edit `AGENTS.md` or either skill yourself"** → "Do NOT edit `AGENTS.md`, either skill, or a subject pack
  yourself".
- **Retirement report**: "report the `AGENTS.md` rules this unit makes obsolete" → "report the `AGENTS.md` and
  subject-pack rules this unit makes obsolete".
- **Report contract**: "net `<n>` AGENTS.md lines" → "net `<n>` guidance lines (AGENTS.md and the packs)".

## Reviewer definitions

`review-quick.md` — three edits. Its bounded input contract is preserved: the packs are supplied inputs, and loading one
is reading a named file, not a search.

At the historical position — the **last instruction before the bounded-screen section, after the hand-over paragraph**.
The final suite's G/G2 (no caller clause) failed; G3 (clause + this position) and G4 (plus the hard stop) passed. The
pass is attributed to the caller clause (isolated by G2 vs G3) and a single pre-check above the opening (the final-suite
passing placement); the first preflight's per-run states are not recoverable, so no shape or placement conclusion rests
on them; the hard stop is a hardening (capture A2):

```markdown
**Before any other read, classify the supplied artifacts against `AGENTS.md`'s subject table.** IF a matching row's pack
is not named in the hand-over, THEN stop and report the missing pack as a missing input before judging the artifact; do
not search the repository for it. Your only output in that case is the missing-input report: do not read another file,
run a command, or produce a finding. A proposal review does not verify the proposal's premise against the code: a claim
you cannot check from the supplied artifacts is a finding or a stated limitation, not a reason to search. Load every
named pack in full; a pack the caller or the implementer loaded is not in your context.
```

The caller's hand-over carries the same contract: it names the packs **and** instructs the reviewer to follow its
definition's input contract. The final suite's G fixture failed without that clause (it judged and searched) and passed
with it, so the clause is part of the routing contract, not a caller preference.

Capture clause: "a diff that adds or changes `AGENTS.md` rules" → "a diff that adds or changes guidance in `AGENTS.md`
or a subject pack".

Compliance corpus in the record line: "(`AGENTS.md`, the specs, or this definition)" → "(`AGENTS.md`, the subject packs,
the specs, or this definition)".

`review-thorough.md` — two edits.

Its input list gains:

```markdown
- the applicable subject packs, classified from the change's decisions and claims (the proposal, design, delta specs,
  and diff), with changed paths as evidence rather than the only predicate.
```

Its conventions bullet becomes: "the applicable repository conventions the named subject packs and `AGENTS.md` state
(for example no comments; the 120-column limit applies only to the files the formatter does not cover, e.g. YAML)". Add:
"A pack the caller did not name but the supplied artifacts turn on is a reported missing input, not a reason to search
the repository."

## `/review-thorough` command

Step 3's context list gains "the applicable subject packs, classified from the change's decisions and claims rather than
the changed paths alone". No other step changes; the session and pass procedure are untouched.

## `agents-auditor.md`

- Its class table gains: "Technical subject guidance | Project-owned subject pack under `.opencode/skills/`, with the
  root classification row and a `Source:` line".
- Its extraction conditions gain the subject-scope permission: the destination is a pack only where the root table names
  the trigger and the path, the missing-pack stop is stated, and the content is not a cross-cutting safety, workflow,
  git, evidence, or documentation rule.
- Its "Destination type" field gains "subject pack".

## `upstreamReferences` — scan scope

The intended scope is **every project-authored skill**, not a three-file list: a future pack must not silently drop a
reference. The provenance partition the auditor already uses defines the exclusion.

```kotlin
sourceFiles.from(
    fileTree(rootDir) {
        include("AGENTS.md")
        include("README.md")
        include("docs/ideas.md")
        include("docs/adr/**/*.md")
        include(".opencode/skills/**/SKILL.md")
        exclude(".opencode/skills/axon4to5-*/**")   // vendored
        exclude(".opencode/skills/opsx-*/**")       // generated
        exclude(".opencode/skills/openspec-*/**")   // generated
    },
)
```

Its file-set test gains: a reference in a project-authored skill is reported; one in a vendored skill and one in a
generated skill are not. The delta in `merge-governance` states the same scope in behavior terms.

## `openspec/config.yaml` — the whole context's restatements

The context block restates the moved rules on two lists. Both are pointed at the packs; the DB-script prerequisite stays
(local development, not a pack).

```yaml
  Conventions:
    - No comments in source code
    - Wrap code and text at 120 characters
    - Load the owning subject pack before a decision in its subject:
      `.opencode/skills/jvm-development/SKILL.md`, `.opencode/skills/jvm-testing/SKILL.md`, or
      `.opencode/skills/gradle-build/SKILL.md` — the root `AGENTS.md` table states the triggers

  Build & test (./gradlew):
    - The root's verification entry point is
      `./gradlew :<module>:check -PskipITs -Pcoverage.gate.enabled=false`
    - The command surface, test tiers and execution prerequisites, build gates, and failure diagnosis are in
      `.opencode/skills/jvm-testing/SKILL.md` and `.opencode/skills/gradle-build/SKILL.md`
    - ./db.sh init creates user `showcase` and database `showcase-events` before running
      the command-service standalone
```

This removes the restated Lombok, MapStruct, ErrorProne, SpotBugs, `-parameters`, test-suite order, tier descriptions,
gateway e2e image dependency, and configuration-cache lines. The README's Testing section is a different surface: it
stays a human-facing summary and is named in the design as such, not rewritten here.

## `README.md` — three capture statements

- Agent table, `lesson-capture` row: "Captures gotchas/conventions into AGENTS.md" → "Captures gotchas/conventions into
  AGENTS.md or the owning subject pack".
- "Lesson capture" bullet: "proposes AGENTS.md gotchas and conventions" → "proposes gotchas and conventions for
  `AGENTS.md` or the owning subject pack"; "rather than to AGENTS.md" → "rather than to the guidance file".
- "Every unit closes the loop" bullet: "the ones you accept land in `AGENTS.md`" → "the ones you accept land in
  `AGENTS.md` or the owning subject pack".

## `docs/ideas.md` — the parked extraction-condition entry

Its premise — "the next destination class inherits the gap" — is met by this change for the subject class. The entry is
refreshed, not removed: the subject-scope class now carries a per-pack `Source:` line, and the remaining gap applies to
other destination classes.

## Partial implementation

No partial variant is pre-approved. The implementation order (packs, then probes in a clone, then root deletion) is what
keeps a failed probe safe: a pack that misses its positive control stops the unit with the root untouched, and the owner
decides the reduced boundary. The design's "Partial implementation" section owns the full rule.
