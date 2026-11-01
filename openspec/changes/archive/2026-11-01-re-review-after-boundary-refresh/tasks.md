# Tasks

Workspace: unit=re-review-after-boundary-refresh; path=/Users/afanasyev/Development/axon-showcase;
branch=re-review-after-boundary-refresh; writer=ses_e6a51d9beffeto8qnIbWdlzo8j.

## 1. The correction

- [x] 1.1 Add the return branch to `.opencode/skills/review-gate/SKILL.md` §5's readiness bullet: a refresh that changed
      a file the closing round reviewed re-opens the gate over the result before the ask — §1's claims table re-derived
      when a planning artifact changed, then the quick loop, then the routing decision reassessed on the changed tree
      and any deep review §3 and §4 own — and the readiness step is re-run after it; point at `/sync-branch`'s lifecycle
      line.
- [x] 1.2 Remove the `docs/ideas.md` 2026-10-28 entry, since this change implements it. **Done**: the entry is gone, and
      #795's newer `## 2026-11-01` entry (arriving with the base) is present beside it.
- [x] 1.3 Author the `MODIFIED` delta for `showcase/quality/agent-skills`: the loop requirement gains the
      boundary-refresh run point, and one scenario is added beside the four existing ones. **Done**: the delta validates
      and carries the four scenarios in order plus one added.

## 2. Verification

- [x] 2.1 Read §5 end to end against the recorded incident's sequence; run `spotlessApply`/`spotlessCheck`,
      `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`, `openspec validate --all`, `verifyCapturedMarkers`,
      `verifyIdeasSections`; run the manual 120-character check over the changed set. **Done**: all green; the change's
      own branch then fired on this unit's own refresh (the base advanced to `57f416a0`), which re-opened the quick
      loop.
- [x] 2.2 Resolve the installer's `.opencode/package.json` pin rewrite (the session start rewrote `@opencode-ai/plugin`
      `1.19.4` → `1.19.5`): restore it from `HEAD` and record the resolution rather than staging it. **Done**: restored
      with `git restore --source=HEAD`; the installer rewrites the pin at a session start, so the restore is repeated
      before staging rather than being a one-time state. After the refresh to `57f416a0` the base itself carries
      `1.19.5`, so the tree matches `HEAD` and the path contributes no diff.

## 3. Review, records and the manual pass

- [x] 3.1 Record the pre-write derivation pass and its claims table in `design.md`, seeded by `scripts/claim-scan.py`.
      **Done**: `claim-scan.py --base de9c0273` seeded it; every row reproduces at its named source and the dismissals
      are stated by kind.
- [x] 3.2 Run the `review-quick` loop, appending each round's row to `docs/review-rounds.tsv`, until a round reports
      nothing new. **Done**: ten rounds on one session, each row appended as its report arrived with both ledger checks
      after; the base advanced mid-loop, the unit's own branch re-opened the loop over the refreshed tree, and the
      closing round is clean.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py` and run the routed pass. **Done**:
      `review-routing.py --unit re-review-after-boundary-refresh --change re-review-after-boundary-refresh` derived
      `RECOMMENDED` on `[repeated-class]`, the signal the unit's repeated rounds raise; the owner resolved to run it, on
      the advisor's recommendation, and pass 1 ran (3.3.1).
- [x] 3.3.1 Record each deep-review pass and every finding's status. **Pass 1** (initial, session
      `ses_e6947fe71ffeBXylTadXnAJ2x3`): - F1 `contradiction` — the branch re-ran routing without reconciling it with
      "one decision per unit" and the material-new-evidence clause — **FIXED_AND_VERIFIED** (the branch and the scenario
      say "reassessed on the changed tree"). - F2 `claim-unreproducible` — the installer's `.opencode/package.json`
      rewrite reappeared and task 2.2's Done note was stale — **FIXED_AND_VERIFIED** (restored; the note states the
      rewrite recurs at a session start). - F3 `wording` — "the full gate" enumerated §2–§4, omitting §1 —
      **FIXED_AND_VERIFIED** (§1's claims table is named, conditional on a planning artifact having changed). **Pass 2**
      (verification, same session): the three fixes verified against the tree; two nitpicks fixed — the claims row's
      anchor moved with the base, and the re-run order now reads §1 → quick loop → routing → deep review; no finding
      remains.
- [x] 3.4 Write `docs/process-defects/re-review-after-boundary-refresh.md` — or state that no defect was observed.
      **Done**: the record states one defect — the refresh reconciliation took one side wholesale and dropped the base's
      `docs/ideas.md` entry, repaired in the same step.
- [x] 3.5 Ask the owner for the manual review pass; commit and push only after approval. **Done**: the owner approved on
      2026-11-01, after the advisor agreed the unit was ready.
