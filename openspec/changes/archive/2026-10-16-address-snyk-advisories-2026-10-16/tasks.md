# Tasks

## 1. The suppressions

- [x] 1.1 Add the `SNYK-JAVA-ORGHIBERNATEVALIDATOR-20877468` ignore to `.snyk`, pinned to
      `'* > org.hibernate.validator:hibernate-validator@8.0.3.Final'`, with a reason naming `9.2.0.Beta1` as the fix
      that retires it and a short-term expiry.
- [x] 1.2 Add the `SNYK-JAVA-JAKTAVALIDATION-20936881` ignore, pinned to
      `'* > jakarta.validation:jakarta.validation-api@3.1.1'`, with its reason and the same expiry.
- [x] 1.3 Add a header comment to `.snyk` naming the two coordinates and the deferred migration they wait on, and
      confirm each `reason` is `: `-free, so no YAML scalar is silently reinterpreted.

## 2. The spec delta

- [x] 2.1 `docs/ideas.md` — remove the entry this change implements (the two advisories the previous unit parked), so
      the park does not outlive its fix.
- [x] 2.2 Add the `MODIFIED` delta for `showcase/quality/dependency-security` carrying the "Local dependency security
      scan task" requirement's full body and all three scenarios, plus one for an unexpired suppression and one for its
      expiry passing.

## 3. Verification

- [x] 3.1 **After the owner's approval pushes the branch** (before the archive commit), dispatch
      `dependency-security.yml` from it
      (`gh workflow run dependency-security.yml --ref address-snyk-advisories-2026-10-16`) and watch the run: it must
      apply the policy and go **green**. A red run naming either suppressed coordinate means the ignore did not take
      (its pin, its id, or the policy path), not a new finding. **Run `38884444351` is green** — the policy applied both ignores and the scan
      completed, so the scheduled run goes green on its next pass.
- [x] 3.2 Run `spotlessApply`/`spotlessCheck`, `openspec validate --all`, and the manual 120-character check over the
      changed set.
- [x] 3.3 Run a `review-quick` pass over the proposal and the implementation, repeating until clean: four rounds (5, 2,
      1, clean). Two of the rounds' classes were mine — a reach claim written from recall (my grep for the coordinate
      missed the `libs.<accessor>` spelling the build uses) and a scenario count restated in two artifacts that I swept
      per site rather than by subject.

- [x] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta: **+4 lines**, one mode merged into the search-under-match bullet — a catalog alias reaches
      build scripts only as its accessor (`hibernate-validator` → `libs.hibernate.validator`), so a coordinate grep
      exits 1 while four modules reference it. It judged the second candidate (sweeping a restated claim by its subject)
      already covered by the review-gate sweeps and the repeated-class clause's "a count" — a compliance miss — and
      proposed no retirements.
- [ ] 3.5 Ask the owner for the manual review pass, after 3.3 is clean and 3.4 is done.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, and its rows are derived — the fix statements from Snyk's own pages, the version lines from Central's
      metadata, and the "BOM-managed" row from the catalog entry.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm and Paketo findings are separate units.
