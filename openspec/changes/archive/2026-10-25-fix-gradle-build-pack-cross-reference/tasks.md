# Tasks

Workspace: unit=fix-gradle-build-pack-cross-reference; path=/Users/afanasyev/Development/axon-showcase;
branch=fix-gradle-build-pack-cross-reference; writer=ses_e8f026f7dffeGSlwOe26i6eZUF

## 1. Repoint the cross-reference

- [x] 1.1 In `.opencode/skills/gradle-build/SKILL.md`'s coverage-gate bullet, replace the parenthetical's target
      `AGENTS.md`'s `## Continuous Integration` with `AGENTS.md`'s `## Build & Test`, leaving the quoted command, its
      rationale, and every other clause unchanged. **Done** — the diff is that parenthetical plus the formatter's reflow
      of the same bullet (`13 insertions, 13 deletions`), nothing else.
- [x] 1.2 Confirm the new target actually carries what the parenthetical promises (the Docker-free form) and that the
      old target does not, so the repoint is a correction rather than a lateral move. **Done** — rows 2 and 4: the CI
      section holds no `coverage.gate.enabled`, and `## Build & Test` states the Docker-free rationale and command.

## 2. Pre-write derivation pass

- [x] 2.1 The pre-write derivation pass ran over this change's artifacts: each fact below was re-derived from its named
      command before the artifacts were finished, and every candidate `scripts/claim-scan.py` surfaced was resolved into
      a row or a dismissal beside the table. **Done** — the table and its four per-kind dismissals are below, seeded
      from `python3 scripts/claim-scan.py` over the intent-to-add diff.

### Claims table

| #   | Claim the change states                                                                     | Source command and its output                                                                                                                                                         |
| --- | ------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Before the change, the pack's bullet points at `## Continuous Integration`                  | `git show HEAD:.opencode/skills/gradle-build/SKILL.md \| grep -n "Continuous Integration"` → one hit, line `223`, the bullet's parenthetical                                          |
| 2   | `## Continuous Integration` states no gate command and no Docker-free rationale             | `sed -n '/^## Continuous Integration/,/^## /p' AGENTS.md \| grep -c coverage.gate.enabled` → `0`                                                                                      |
| 3   | The CI workflow runs exactly that command for a pull request                                | `sed -n 69p .github/workflows/ci.yml` → `            ./gradlew check -PskipITs -Pcoverage.gate.enabled=false`                                                                         |
| 4   | `## Build & Test` states the Docker-free form and why it keeps the coverage gate green      | `sed -n '/^## Build & Test/,+6p' AGENTS.md` → the heading, "The Docker-free form keeps the coverage gate green without integration data:", and the `showcase-command-service` command |
| 5   | `extract-ci-narrative` (#644) is the change that emptied that section of the gate command   | `git show cc59a892^:AGENTS.md \| sed -n '/^## Continuous Integration/,/^## /p' \| grep -c coverage.gate.enabled` → `1`; the same over `cc59a892:AGENTS.md` → `0`                      |
| 6   | No capability spec governs a pack's internal cross-references, so no delta is owed          | `grep -rln "gradle-build" openspec/specs/` → `openspec/specs/showcase/quality/agent-skills/spec.md`, whose only hit names the pack as a possible capture destination                  |
| 7   | The command-surface pointer to the pack is in `## Build & Test`, not the CI section         | `grep -n "^## Build & Test" AGENTS.md` → `772:`; `grep -n "The build command surface is in the" AGENTS.md` → `781:`                                                                   |
| 8   | The report contract budgets each finding item one line of evidence, not one per proposition | `grep -n "one line of evidence" openspec/specs/showcase/quality/agent-skills/spec.md` → the "Each item is budgeted" scenario's THEN line                                              |

Dismissals, one reason per kind `scripts/claim-scan.py` reported for this diff. The edit re-wraps the whole bullet, so
most candidates are pre-existing text this change does not modify:

- **`count`** — every candidate is a numbering or a record field: a task item's `N.M`, a ledger row's round, class, and
  compliance-miss fields, a figure this change records in a task's Done note, or a figure inside pre-existing bullet
  text.
- **`path`** — every candidate is a path, a command string, or a workflow path with its line anchor; each resolves.
- **`cross-ref`** — every candidate is a cross-reference the change itself carries in a claims row (`#644`), a
  pre-existing one inside the unchanged bullet (`#514`), the workspace path in the `Workspace:` record, a reference
  inside the entry this change parks in `docs/ideas.md`, or a slash-joined phrase the scanner reads as an owner-and-repo
  path.
- **`absolute`** — every candidate is the proposal's `None` capability placeholder, a pre-existing quantifier inside the
  unchanged bullet, or a scope word confined to its own sentence.

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; confirm both pass.
      **Done** — `spotlessCheck` exit 0, `openspec validate --all` → 27 passed, 0 failed.
- [x] 3.2 Run the 120-character check over the change's whole changed set, using the `perl -CSD -lne` recipe. **Done** —
      `.opencode/skills/gradle-build/SKILL.md` 0, `.openspec.yaml` 0, `proposal.md` 0, `docs/ideas.md` 0; `tasks.md` 10
      (the claims table's header, delimiter, and eight rows, unwrappable); `docs/review-rounds.tsv` rows are one line by
      contract.

## 4. Review gate

- [x] 4.1 The `review-quick` loop ran over the proposal and then the implementation, repeating until it reports no new
      observations; the ledger owns the round state, and both ledger checks run after each append. **Done** — the
      ledger's last row for this unit records no classes, its closing condition; one session
      (`ses_e8eb854b1ffe1o01r9SIoeNyjZ`, `created` then `continued`), and both ledger checks pass after every append.
- [x] 4.2 The deep-review routing decision is recorded on this task, derived with
      `python3 scripts/review-routing.py --change fix-gradle-build-pack-cross-reference`. **Derived: `RECOMMENDED` on
      `[repeated-class]`** — ledger rows for this unit repeat prior classes; no path, declaration, or requirement-block
      signal matched. **Owner resolution: no deep review** — the repeated findings concerned supporting claims and
      records, the corrected cross-reference is verified against its destination and CI, and the capture introduced no
      behavioral change. No deep-review session is created.

## 5. Capture

- [x] 5.1 The `lesson-capture` subagent ran over the diff and the review findings, its durable proposals were applied or
      its nothing-durable verdict recorded, and the applied net `AGENTS.md` delta is stated on this task. **Done** — it
      ran (session `ses_e8ea8ed8dffeVqJaHlkD33iryj`): 2 proposals to `.opencode/skills/review-gate/SKILL.md` stage 1 (a
      dismissal holds only when each candidate's own text falls inside its kind's reason and the reason adds no
      candidate of its own; an applied finding's supporting narrative is a claim class derived from history), 0
      retirements, and 1 authoring-system recommendation (a finding's report item states evidence per proposition).
      **All three are routed, not applied:** both proposals rest on one dependent lineage inside this unit (rounds 1-4),
      below the promotion gate's two-independent-occurrences threshold, and a sixth claim class would change behavior
      the `agent-skills` spec describes, which a `skip_specs` change cannot carry; the recommendation is parked in
      `docs/ideas.md` under `## 2026-10-25` as its own change with a `MODIFIED` delta. Applied net `AGENTS.md`/packs
      delta: **0 lines**; its two facts are kept here — a kind label is not a dismissal check (the scan prints it on
      every line), and a finding's causal clause is a claim to derive.

## 6. Manual review and archive

- [x] 6.1 The owner's manual review pass was requested after the loop was clean and the capture ran. **Done** —
      requested with this unit's report after the ledger's closing round, with the routing resolution recorded on 4.2;
      the working tree is left uncommitted for the pass.
- [x] 6.2 After the owner's approval: push the branch, open the pull request, confirm `build` is green, archive the
      change in the same pull request, and merge once approved. **Done** — pushed, PR #681 opened, `build` green, the
      change archived as `2026-10-25-fix-gradle-build-pack-cross-reference` in this commit, and the PR merged (squash)
      with the branch deleted.

## Workflow follow-up

- Nothing committed, pushed, or archived before the owner approves the implementation.
