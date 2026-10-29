# Tasks

## 1. The routing tool

- [x] 1.1 In `scripts/review-routing.py`, add the record-only predicate over a changed-path set: a Markdown file under
      one of the three record directories, or the ledger path the invocation reads; non-empty; and, for a rename, both
      its source and its destination, which `git diff --name-status` supplies and `--name-only` does not.
- [x] 1.2 In `floor`, keep the repeated-class signal but do not lift the floor for a record-only diff, reporting the
      exemption as its own signal line, and state the predicate and the reported line in the module docstring, which is
      the tool's contract.
- [x] 1.3 Keep the mandatory signals, the failed-derivation `REQUIRED`, and the non-zero exit unchanged.

## 2. The controls

- [x] 2.1 A record-only diff whose ledger rows repeat a class → `NOT_NEEDED`, with the repeat and the exemption both
      reported.
- [x] 2.2 A record-only diff that also changes a mandatory path → `REQUIRED`.
- [x] 2.3 A diff of a record plus a code path whose rows repeat → `RECOMMENDED`.
- [x] 2.4 A ledger-only diff whose rows repeat → `NOT_NEEDED`.
- [x] 2.5 The predicate over a renamed record path (both sides record files), a deleted record file, an untracked record
      file, and an empty changed-path set; a JSON beside a record, a Markdown file one directory outside the three, a
      record path renamed into a code path, and a **code path renamed into a record path** are each **not** record-only.
- [x] 2.6 A record-only set that also carries a mandatory path → `REQUIRED`, and a failed derivation on a record-only
      set → `REQUIRED` with a non-zero exit.
- [x] 2.7 Mutate the exemption (remove it; make it cover every changed path; widen it to a whole record directory) and
      confirm 2.1, 2.3 and 2.5 fail respectively; restore.

## 3. The caller's procedure

- [x] 3.1 In `.opencode/skills/review-gate/SKILL.md`'s fix-and-re-run step, name the record-artifact branch: a later
      round raising only new instances of an already-raised class stops and escalates to the owner, preserving the
      finding-bearing ledger row and the finding instances, recording the owner's decision on the unit's review task or
      pull-request review record, and resuming per that decision without manufacturing a clean round; `--expect-clean`
      stays as it is.

## 4. The recording workflow

- [x] 4.1 In `AGENTS.md`'s "Workflow: Record process defects", state the drafting preference (a short account of the
      observed event; a quotation only where it reproduces; a figure attributed to a command's output taken from a run
      of that command; session-only text described), pointing at the evidence rules that own the requirements, and state
      that it never makes an observation or an event sequence inadmissible.

## 5. The spec delta

- [x] 5.1 Write the first `MODIFIED` block ("The deep review is routed and bounded"): copy its description and all its
      scenarios verbatim, add the record-only exemption to the floor clause, and add one scenario.
- [x] 5.2 Write the second `MODIFIED` block ("The quick review loop runs on every unit"): copy its description and all
      its scenarios verbatim, add a process-defect record to the record-artifact class, and add the escalation's pending
      state as a scenario.

## 6. Verification

- [x] 6.1 Run the pre-write derivation pass and record its claims table (`design.md`), with `scripts/claim-scan.py`'s
      candidates for the diff resolved into a row or a dismissal beside it, one reason per kind.
- [x] 6.2 Run `openspec validate --all`, `./gradlew spotlessApply`, `./gradlew testReviewRouting`, and the Docker-free
      `./gradlew check`, and record the results — **Done**: `openspec validate --all` 27 passed, 0 failed;
      `spotlessApply`/`spotlessCheck` green; `testReviewRouting` green (the suite's size is not stated, since round 10's
      controls grew it); the Docker-free `check` `BUILD SUCCESSFUL`.
- [x] 6.3 Write the unit's process-defect record.
- [x] 6.4 Run the review gate: the quick loop (twelve rounds on one reviewer session, closing clean) and the routing
      decision — `RECOMMENDED` (`[repeated-class]`), which the owner resolved to **skip** on the technical advisor's
      recommendation; the consultation's one gap (an empty path field accepted as a truncated record) was corrected with
      two controls, and the owner asked for a seeded probe of the edited procedure, whose three states returned the
      intended branch: new instances on a record artifact → stop and escalate without a clean round; the same unfixed
      findings → fix and re-run; a non-record diff → fix and re-run. The manual pass follows.
