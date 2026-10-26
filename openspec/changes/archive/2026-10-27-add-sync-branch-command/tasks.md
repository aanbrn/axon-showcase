# Tasks

Workspace: unit=add-sync-branch-command; path=/Users/afanasyev/Development/axon-showcase-add-sync-branch-command;
branch=add-sync-branch-command; writer=ses_e881dae91ffee088PlBcDGVUph

## Resume checkpoint

The `pause-resume-unit` skill owns this field set; this is the unit's one record of it.

| Field                       | State                                                                                                                                                                                                                                                                             |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Time and revision           | 2026-10-27; revision `2cfde45d` after the fifth refresh                                                                                                                                                                                                                           |
| Workspace                   | `unit=add-sync-branch-command; path=/Users/afanasyev/Development/axon-showcase-add-sync-branch-command; branch=add-sync-branch-command; writer=ses_e881dae91ffee088PlBcDGVUph` — this session is that writer                                                                      |
| Stage                       | scope reduced to a prose command, under the review gate; the review loop's state is `docs/review-rounds.tsv`'s                                                                                                                                                                    |
| Next step                   | review the reduced diff, then the owner's manual pass                                                                                                                                                                                                                             |
| Change set                  | the command file, one `README.md` row, the `AGENTS.md` pointer and derivation correction, three parked entries in `docs/ideas.md`, and the ledger rows                                                                                                                            |
| Blockers and open decisions | none blocking. The earlier script draft (400 lines, a 41-case suite, a `check` task, a three-requirement delta) was dropped after an independent consultation: its automation ended at the semantic conflict the caller resolves anyway, and the rule already covers the sequence |
| Pending decision            | the owner's manual review pass                                                                                                                                                                                                                                                    |
| Findings no record holds    | none outstanding; the script draft's findings are closed with the artifact they described                                                                                                                                                                                         |
| Operations in flight        | none                                                                                                                                                                                                                                                                              |

## 1. The command

- [x] 1.1 Create `.opencode/commands/sync-branch.md`: the trigger, the comparison, the route per branch state, the
      both-sides conflict resolution, the read-only call from a diagnosis, the report, the failure handling, and the
      lifecycle preflight. Verify: the frontmatter parses; the body cites `AGENTS.md`'s rule and carries no copy of its
      route table (`grep -c "^|" .opencode/commands/sync-branch.md` → `0`), while naming the state's route in prose.
      **Done:** the frontmatter parses, the body cites the rule, and no table row appears in it; the load is confirmed
      by the PR's `build` probe.

## 2. Documentation

- [x] 2.1 Add the `/sync-branch` row to `README.md`'s slash-command table. Verify: `grep -n "/sync-branch" README.md`
      shows the row, and the table keeps its column count. **Done:** `grep -n "/sync-branch" README.md` shows the row;
      the table keeps four columns.
- [x] 2.2 Add the command's pointer to `AGENTS.md`'s "Refreshing a branch" bullet, keeping every clause `#704` merged
      (the mechanism enumeration, the stash recovery, the diff deletion, the `git diff --stat` confirmation, the
      both-sides rule) and both `captured:` markers. Verify: the bullet still contains
      `stash first and re-apply after it`, `Resolve that rebase for both sides` (or its `#704` wording),
      `deletes whatever merged since`, and both markers. **Done:** the bullet keeps `stash first and re-apply after it`,
      both-sides reconciliation, `deletes whatever merged since`, `git diff --stat`, and both markers, and gains the
      pointer.
- [x] 2.3 Correct the tooling-placement bullet's derivation command so it reaches the non-executable tools the bullet
      enumerates. Verify: `git ls-files scripts showcase-web-ui/scripts '*.sh' ':!:.opencode/*'` lists `db.sh`,
      `showcase-web-ui/start.sh`, and `scripts/claim-scan.py`. **Done:**
      `git ls-files scripts showcase-web-ui/scripts '*.sh' ':!:.opencode/*'` lists `db.sh`, `showcase-web-ui/start.sh`,
      and `scripts/claim-scan.py`.
- [x] 2.4 Park the three lessons this unit produced — an interception's partial reach, an assertion the sibling outcomes
      share, and the `pause-resume-unit` Stage field that invites the ledger's state — in `docs/ideas.md`, each with its
      evidence and its status tag. Verify: each of the three appears once under the new dated section, and each entry
      ends in a status tag. **Done:** all three are there, with the parked tag.

## 3. Verification

- [x] 3.1 Run `openspec validate --all`; the change carries `skip_specs: true` and no delta. Verify: exit `0`. **Run:**
      `openspec validate --all` → 27/27, the change carrying `skip_specs: true` and no delta.
- [x] 3.2 After the final edit, run `./gradlew spotlessApply` then `./gradlew spotlessCheck`, and the 120-character
      recipe over the changed files the formatter does not own. Verify: both exit `0` and the check prints nothing
      outside table rows. **Run:** `spotlessApply`/`spotlessCheck` → exit 0; the 120-character recipe over the changed
      Python-free set prints nothing outside table rows.
- [x] 3.3 Run the review gate's quick loop over the reduced diff to a clean round. Verify: the closing round reports
      `nothing remains`, and its row is appended to `docs/review-rounds.tsv`.
      **Run:** rounds 45–52 covered the reduced diff; round 52 reports `nothing remains`, and its row is in
      `docs/review-rounds.tsv`.
- [x] 3.4 Record the capture decision for the reduced scope: the durable lessons that ship (the spec-delta ordered-guard
      rule and the tooling derivation correction), the three parked in 2.4, and the net `AGENTS.md` delta. Verify: the
      record is on this task and `git diff --numstat AGENTS.md` reproduces the figure. **Record:** the durable lessons
      that ship are the spec-delta ordered-guard rule and the tooling-derivation correction, both in `AGENTS.md`; the
      two test-authoring modes and the `pause-resume-unit` `Stage` lesson are parked in `docs/ideas.md` under
      `## 2026-10-27`. Deltas, measured apart: the capture's own is **+6/−2 (`+4` net)** — the ordered-guard rule's
      three lines and the derivation correction's three-for-two — and the command's pointer **+9/−7 (`+2` net)**, for
      the **+15/−9** total (`git diff --numstat origin/main -- AGENTS.md`). The four-line capture growth is recorded
      rather than offset: the ordered-guard rule is a new obligation no existing rule carries, and the derivation
      correction fixes a command that could not reach the tools its own sentence enumerates.

## Workflow follow-up

- Ask the owner for the manual review pass before any commit, push, pull request, archive, or merge.
- Archive the change with `openspec archive add-sync-branch-command --yes` (no delta to sync), stage both sides, and
  verify the archive.
