# Tasks

Workspace: unit=document-java-version-selector; path=/Users/afanasyev/Development/axon-showcase;
branch=document-java-version-selector; writer=ses_e8ede802cffeqJu3lPJfhdQyis

## 1. Planning and records

- [x] 1.1 Record the workspace assignment above before the first edit.
- [x] 1.2 Derive the claims table in `design.md` — each fact the block states, with the command that reproduces it — and
      re-run every row at this state.
- [x] 1.3 Record `scripts/claim-scan.py`'s candidates resolved by kind, beside the table.

## 2. Implementation

- [x] 2.1 `README.md`: insert the approved block in **Getting Started → Build the Project**, immediately before the
      `./gradlew build` fence — the optional version-manager convention, support depending on the manager, the three
      mechanisms apart (`JAVA_HOME` launcher, catalog toolchain, CI), and the closing note that `JAVA_HOME` and `PATH`
      can disagree.
- [x] 2.2 Confirm the block names no specific manager, manager list, or host detail, so it neither reads as exclusive
      nor ages with a manager release.

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply` after the last edit, then `./gradlew spotlessCheck`.
- [x] 3.2 `openspec validate --all` passes, and `openspec validate --changes` accepts this change.
- [x] 3.3 Re-read the inserted block against the claims table and confirm every sentence is one a row supports.

## 4. Review gate

- [x] 4.1 Record the proposal round and each implementation round in `docs/review-rounds.tsv`, running
      `python3 scripts/commit-hygiene.py --unique-rounds --expect document-java-version-selector:<round>` and
      `python3 scripts/commit-hygiene.py --session-continuity` after every append. Done: rounds 1–3 appended; both
      ledger checks pass after each append.
- [x] 4.2 Derive the routing floor with `python3 scripts/review-routing.py --change document-java-version-selector`,
      record its outcome here, and run the deep review when it is `REQUIRED`. Done: `NOT_NEEDED` — the derivation names
      no signal, and the diff is a README block plus guidance edits, with no build, runtime, or deployment surface, so
      no deep review runs.
- [x] 4.3 Ask the owner for the manual review pass and stop there. Done: asked with the six-round record and the
      advisor's review; nothing is committed until the owner approves.

## 5. Capture

- [x] 5.1 Run `lesson-capture` over the diff and the rounds, or record the skip and its reason here. Done: the skip
      condition failed (rounds carried `claim-missing`/`claim-unreproducible`), so it ran. Two durable proposals
      applied: the claims row for a pre-change claim anchors to the base revision **SHA** in `review-gate` (with the
      sibling `git show HEAD:<path>` in `jvm-development` retargeted), and the tool-behavior-claim bullet in `AGENTS.md`
      now covers a claim stated to the owner as the basis for a decision. Its two instances: the owner-facing claim that
      `.java-version` was read by jenv **only**, which verification against jenv's own source and mise's `core:java`
      backend disproved (design rows 3 and 5) — a claim stated as the basis for the owner's decision; and the more
      absolute `.python-version`/IDE statement, which exists in the session record only and serves as corroboration, not
      as the rule's evidence. The `+1` net line is justified as a new trigger scope, not a restatement: no existing
      bullet covers an owner-facing claim, and folding it into the artifact clause would lose the advice-versus-artifact
      distinction. Measured net deltas, re-derived after the final `spotlessApply`: `AGENTS.md` `+9/−8`, `review-gate`
      `+18/−15`, `jvm-development` `+5/−5` (one semantic line), `docs/ideas.md` `+3/−1`, `openspec/config.yaml` `+6/−4`
      (the retargeted design rule, found by review round 4). No retirement proposed.

## 6. After approval

- [ ] 6.1 Commit, push, open the pull request with the template's sections, and wait for a green `build`.
- [ ] 6.2 Archive the change in the same pull request and merge.
