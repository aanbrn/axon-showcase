# Tasks

## 1. The tool

- [x] 1.1 `scripts/claim-scan.py` — `find_claims(diff_text)` over unified-diff text, returning each added line's claim
      candidates as `(path, line, kind, text)` for the four kinds: `count`, `absolute`, `cross-ref`, `path`; plus a CLI
      taking `--base <ref>` (default `origin/main`), `--repo <dir>` (default the current dir), and `--diff-file <path>`
      (read a diff instead of running git, which the tests use). It scans `git diff <base>` and the untracked,
      non-ignored files (`git ls-files --others --exclude-standard`), and prints `path:line  [kind]  text`. Module
      docstring and `--help` enumerate the kinds; stdlib only; `/usr/bin/python3` compatible (no PEP 604 unions).
      **Done.**
- [x] 1.2 `scripts/test-claim-scan.py` — a case per kind over a unified-diff fixture, a case for pre-existing (context
      and removed) lines being ignored, a case for the untracked-file inclusion, and a case driving `main([...])` for
      the CLI. The known-positive control is a committed fixture carrying one claim of each kind, so a pattern that
      stops matching fails its test rather than going quiet. **Done** (14 tests green under `/usr/bin/python3`,
      including `scan()` over a temp git repo, the modified-path drop, the known-untracked drop, and the `None.` guard).
- [x] 1.3 Tune the patterns: run the tool over a few recent merged changes' diffs (`git diff <merge>^ <merge>`), and
      narrow any kind whose output buries the others. Record what was tuned and what the run emitted. **Done** — three
      narrowings: a `path` candidate naming a file the authored set itself modifies or creates is dropped (it is not a
      claim about prior evidence), a bare `None.` line is not a quantifier, and a leading-slash command name is not a
      path. Over `git diff <sha>^ <sha>` for `9f2cb8cd`, `c7e6402f`, and `5ed8037d` the shipped tool emits 88/113/98
      candidates (`absolute` 26/57/39, `count` 40/29/22, `path` 14/15/18, `cross-ref` 8/12/19); the pre-narrowing run
      measured 134/158/164 at the time. Over this change's own diff the run moves as the artifacts grow, so it is not
      quoted as a count. The remainder is the prose's own density — `absolute` is the largest of the four but does not
      bury the others — which the kind split lets an author dismiss by group.

## 2. The wiring

- [x] 2.1 `build.gradle.kts` — a `testClaimScan` `Exec` task running `scripts/test-claim-scan.py`, mirroring
      `testCommitHygiene`, and its `check` dependency. **Done.**
- [x] 2.2 `.opencode/commands/seed-claims.md` — the trigger command, since a `scripts/` tool an agent drives gets one:
      it runs the scan over the current unit's authored set and hands the candidates to the author to resolve into
      claims-table rows or record as dismissals (with the reason), grouped by kind where uniform. **Done.**
- [x] 2.3 `.opencode/agent/review-quick.md` — the claims-table bullet gains the scan candidates: a candidate that is
      neither a row nor a listed dismissal is a finding. Lifecycle: it lands in the apply pass (an early edit was
      reverted, since a definition edit is implementation and the planning stage is read-only); the definition change
      then invalidates the running reviewer session's contract, so the implementation rounds start fresh (the delta's
      fresh-session case), and their scan-candidate check is the seeded control — round 10 reported an unaccounted
      candidate (the positive control) and the round that closes the loop reports none (the negative control).

## 3. The spec and docs

- [x] 3.1 The `MODIFIED` delta at `specs/showcase/quality/agent-skills/spec.md` is in place and passes the change
      validation. **Done at propose.** No `## Purpose` refresh is owed — the Purpose describes the capability's scope,
      not the pass's mechanism (read at proposal time).
- [x] 3.2 `AGENTS.md` — the pre-write bullet gains the scan's actionable form (run `scripts/claim-scan.py` via
      `/seed-claims`; resolve each candidate into a row or record its dismissal, with the reason), pointing at the spec
      for the behaviour; the `check` members list gains `testClaimScan`; the agent-driven-script enumeration gains
      `claim-scan.py` ↔ `/seed-claims`; and the Python prerequisite names the tool. **Done.**
- [x] 3.3 `README.md` — the Slash Commands table gains a `/seed-claims` row, and the Python prerequisite row names the
      tool. **Done.**
- [x] 3.4 `docs/ideas.md` — remove the "Mechanically enumerate an authored diff's claim shapes" entry this change
      implements, keeping the `## 2026-10-15` section's other entries. **Done.**

## 4. Verification

- [x] 4.1 `openspec validate --all`, `./gradlew spotlessApply`/`spotlessCheck`, the guard-style suite runs
      (`scripts/test-claim-scan.py`), and the manual 120-character check over the changed set. Then the Docker-free
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. **Done** — validate 27/27, `spotlessCheck` and the
      Docker-free `check` green, the suite 14/14, and no changed non-markdown line over 120.
- [x] 4.2 Run the `review-quick` loop over the proposal and the implementation, repeating until clean, reusing one
      reviewer session across the rounds (passing its `sessionID`; fresh only if it expires or the reviewer's definition
      changed). Record each round in `docs/review-rounds.tsv`. **Round 1 (proposal) — 7 classes, all fixed**: the
      claimed figures named no selection (175 of 542, the ten claim classes grouped, claims-table family 47 — now
      derived by the stated `uniq -c` command); two stated facts had no claims row (the scanned shapes' provenance in
      the idea, the Purpose assessment); the symbol-narrowing rationale over-claimed that the loop's misses are all
      counts/scopes/cross-refs (the largest classes are `contradiction` and `wording`); the pre-write record claimed the
      scanner had seeded the table before the scanner exists; the idea's "explicitly dismissed" was dropped without a
      decision; the new command's README row and the tooling/Python enumerations were missing; and two enumerations were
      under-listed (the pre-write sources, the `AGENTS.md` edits). **Round 2 — 4 classes, three repeating, all fixed**:
      the deriving command dropped its `grep -v '^#'`, counting the header fragments (542 measured against 559); the
      symbol-narrowing rationale's "Identifiers are verified by the compiler and the gates" is false for the artifact
      names a doc cites; the dismissal record's terms diverged (the delta omitted the reason, task 2.2 dropped the
      step); and `.opencode/agent/review-quick.md` was missing from the plan though the record's rationale is the
      reviewer's control. The three repeats named the root cause — instance-patching — so each fix swept the whole
      artifact set. **Round 3 — 2 classes, one repeating, all fixed**: the symbol-narrowing sentence over-claimed for a
      third round (a doc's _class_ citation is reached by no kind, only a path or a capability id is) and was re-derived
      from the four kinds; and `.opencode/agent/review-quick.md` was edited while the change is still at the proposal
      stage — reverted, with task 2.3 to apply it in the apply pass. **Round 4 — 1 class (missing-task), repeating, all
      fixed**: the plan edited a definition the loop itself reads and reused the reviewer session, so the new check
      would never run; task 2.3 now states the lifecycle (apply → restart the reviewer session → prove the check with a
      seeded control → record). **Round 5 — 2 classes, both repeating, all fixed**: task 2.3 attributed a reload
      exception to the delta's session clause, which states none (the restart is now the smoke-run, outside the round
      policy); and two AGENTS.md facts in that note had no claims row (added). **Round 6 — 1 class
      (incomplete-enumeration), repeating, all fixed**: the pre-write record's source list went stale when two rows were
      added; replaced with a pointer to the claims table. **Round 7 — 1 class (incomplete-enumeration), repeating, all
      fixed**: task 3.1's delta description under-listed its two edits; the clause-level description was deleted,
      leaving the pointer. The class has recurred on artifacts described inside other artifacts, so the fixes drop those
      descriptions rather than update them. **Round 8 — 1 class (formatting), all fixed**: my edit split an inline code
      span across a source line, so the reflow left a five-space run inside it (Prettier never rewrites a span's
      interior); kept on one line. **Round 9 — nothing remains (same session): the proposal-stage loop closed.** Rounds
      1-9 reused one session, so the reuse path ran here too. The implementation stage restarted the session (the
      definition changed — task 2.3's smoke-run), and round 10 opened the new loop. **Round 10 — 5 classes, four
      repeating, all fixed**: the tuning figures were measured before the last narrowing and did not reproduce
      (88/113/98 with the shipped tool); the dismissal bullets claimed a coverage the run did not support
      (`origin/main`, `/usr/bin/python3`, `#606`), and the `path` narrowing did not reach the authored set's untracked
      files; the `/seed-claims` command omitted the no-change-dir destination; task 4.2 said "Done." while unticked; and
      no test drove `scan()` or the narrowings. The reviewer's loaded definition carried the new scan-candidate check,
      and it reported an unaccounted candidate — the smoke-run's positive control. **Round 11 — 4 classes, three
      repeating, all fixed**: the dismissal bullets were re-derived again, with a catch-all covering the regexes' prose
      false positives and the artifacts' numerals; task 1.2's test count said 10 against the suite's 14; the smoke-run
      model was reconciled (the definition change makes the implementation rounds the fresh session, carrying both
      controls); and "the two rows above" named no pair. **Round 12 — 2 classes, one repeating, all fixed**: AGENTS.md's
      session sentence still stated the old rule the delta widened (the definition-change restart); and the dismissal
      catch-all's gloss described the groups as false positives and numerals, which the `absolute` and reference groups
      are not. The negative control held — the run's candidates all fall under a listed dismissal. **Round 13 — nothing
      remains (same session): the loop closed.** The implementation rounds (10-13) ran in the restarted session, so both
      the definition-change restart and the reuse path ran here. **Round 14 — 2 classes, both repeating, all fixed**:
      the capture's `--diff-file` help line ran to 140 characters (wrapped), and the `/seed-claims` command still
      promised the untracked scan in that mode (scoped to the default `--base` mode, where the tool does scan them).
      **Round 15 — 1 class (docs-residue), repeating, all fixed**: this task's own instruction still said "fresh only if
      it expires" where the rule now admits the definition change; aligned. **Round 16 — nothing remains (same session):
      the loop closed.** The implementation rounds ran in the restarted session (the definition change), so both the
      restart and the reuse path ran here, and the scan-candidate control held at both poles. **Done.**
- [x] 4.3 Ask the owner for the manual review pass, after 4.2 is clean. **Asked** — the request is in the session; the
      outcome is the owner's.

## 5. Capture

- [x] 5.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Done** — two additions merged into existing sentences, no retirement: (A) the
      pre-write bullet's fifth class gains "re-deriving it after that mechanism's last edit, since a figure measured
      before the final tuning does not reproduce"; (B) the loop's root-cause shape #2 gains a description of a sibling
      artifact's contents as the same shape with a moving subject. The capture also flagged the tool's `--diff-file`
      help as over-claiming (it scans no untracked files); the help now says so. Net `AGENTS.md` delta: **+8 lines
      measured** (`git diff --numstat origin/main -- AGENTS.md` = 58/50) — the two sentences are 4 and the reflow the
      insertions force the rest — growth stated rather than offset by a retirement.

## 6. The change's own records

- [x] 6.1 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, each row carrying its own source or deriving command — see that table rather than a list here, which
      a row addition would make stale. **Done** — the table is author-seeded; the scanner this change adds does not
      exist yet, so running it over this change's own diff belongs to task 1.3's implementation.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
