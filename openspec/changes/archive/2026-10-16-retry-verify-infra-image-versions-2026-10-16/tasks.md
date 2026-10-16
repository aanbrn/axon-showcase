# Tasks

## 1. The retry

- [x] 1.1 Add `BoundedRetry` to `build-logic` as a pure object: a bounded attempt loop with a pause, an
      `onTransientFailure` callback, and the last attempt's failure propagating (the `require(attempts >= 1)` guard).
- [x] 1.2 Add `BoundedRetryTests`: a transient failure succeeds on a later attempt (asserting the attempt count and the
      reported failures), a persistent failure reports its last failure after exactly the bounded attempts, a single
      attempt never retries, and fewer than one attempt is rejected.
- [x] 1.3 Run the repository add/update pair in `AbstractHelmRepositoriesTask` through the retry, leaving the
      `tolerateFailures` path and the thrown failure unchanged, so both callers are covered at the one point the network
      call happens.

## 2. The spec delta

- [x] 2.1 Add the `MODIFIED` delta for `showcase/quality/infra-image-versions`, carrying the "Drift between surfaces is
      caught by the build" requirement's full body and all five scenarios in order, plus the resilience clause and one
      scenario covering both directions (a transient failure retried, a persistent one still failing).

## 3. The docs

- [x] 3.1 `docs/ideas.md` — remove this change's entry (the retry), since the change implements it.
- [x] 3.2 `docs/ideas.md` — remove the stale actionlint-retry entry, which #463 implemented; this unit's own sweep
      surfaced it while reading that entry as this change's precedent. Its incident is preserved in that commit's
      message, so nothing durable is lost.
- [x] 3.3 Confirm `AGENTS.md`, `README.md`, `openspec/config.yaml`, and `docs/adr/` need no edit: the
      `verifyInfraImageVersions` bullet states the derivation, the network dependence and the caching but never the
      flake or the `check -x` workaround (which lived only in the removed idea entry), and the spec now owns the
      resilience.

## 4. Verification

- [x] 4.1 Run the `build-logic` tests, and prove they are evidence with the known-bad control: with `BoundedRetry`
      neutered (`repeat(attempts - 1)` → `repeat(0)`) **2 of the 4 cases fail**; restored, all 4 pass.
- [x] 4.2 **The live control**: point one declared repository at an unreachable host and run `verifyInfraImageVersions`
      — it prints **two** `not reachable yet; retrying` warnings (three attempts) and then `FAILED` with exit 1, so a
      persistent failure still fails the check; restore the URL and confirm the task passes again.
- [x] 4.3 Run `spotlessApply`/`spotlessCheck`, `openspec validate --all`, and the manual 120-character check over the
      changed set.
- [x] 4.4 Run a `review-quick` pass over the proposal and the implementation, repeating until clean: five rounds
      (wording, clean, incomplete-enumeration, incomplete-enumeration, clean). Round 3's class repeated in round 4
      because my fix answered it by hand-listing the artifact set beside the bullet that owns it — the root cause, which
      the second round closed by deleting the gloss rather than re-wording it. Round 1's `wording` (a misnamed referent)
      was fixed, and its observation about `AGENTS.md`'s "bitnami chart repository" sentence is surfaced for the owner:
      it is a pre-existing fact this change did not cause.

## 5. Capture

- [x] 5.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta: **+5 lines**, one complement clause merged into the pull-request-gate bullet — a gate-worthy
      step whose own fetch can fail transiently stays in the gate and is made resilient with a bounded retry, never
      toleration or the `check -x` interim. It judged the double control covered by the check-evidence bullet, and
      routed the missing `docs/ideas.md` stale-entry sweep to widening `architecture-auditor` rather than to an
      `AGENTS.md` rule — parked as its own idea under 2026-10-16, riding this change. No retirements.
- [x] 5.2 Ask the owner for the manual review pass, after 4.4 is clean and 5.1 is done.

## 6. The change's own records

- [x] 6.1 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, and its rows are derived — the pre-change helper from the file itself, the two call sites from the
      sources, the cacheability from the task's annotations and the spec's scenario, the CI twin from `69d26b02`, and
      the two controls from their own runs.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
