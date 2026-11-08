# Deep review — select-the-installed-idea-config

## Routing decision

- **Decided** — 2026-11-08, from the completed implementation and the review evidence.
- **Outcome** — `RECOMMENDED`, awaiting the owner's explicit run-or-skip resolution.
- **Floor and signals** —
  `python3 scripts/review-routing.py --unit select-the-installed-idea-config --change select-the-installed-idea-config`
  reports `RECOMMENDED` on `[repeated-class] 11 rows repeat a prior class` (the proposal loop ran 10 rounds, the
  implementation loop 8, and `coherence`/`test-control`/`formatting` recurred while the design's pivots propagated).
- **Why not lower** — the change alters an existing tool's behaviour: which IntelliJ configuration it reads, and the
  message it stops with. A wrong selection writes a module whose SDK name the IDE cannot resolve, which the tests cover
  but which an independent pass should re-derive from the delta.
- **Why not `REQUIRED`** — no gate, schema, deployment, or public contract changes; the tool is a local setup helper and
  the delta modifies one requirement.
- **My recommendation** — run it: the loop churned (18 rounds), so an independent implementation-vs-delta check is worth
  one pass, and it is the first change to this script since the archived decision it refines. The advisor's
  `advisor-routing-1` agrees, and asked the pass to focus on the selection decision model crossed with SDK cardinality,
  the failure states, and the wrapper's launcher selection — not another formatting sweep. Its confirmed problem (the
  identified path bypassing the duplicate-entry refusal) is fixed and pinned by a case.

## Owner's resolution

- **Resolved** — 2026-11-08: the owner chose to **run** the deep review ("Отревьювь с советником до согласия"), on the
  advisor's recommendation, and to iterate with the advisor until agreement.
- **Focus** — the selection decision model crossed with SDK cardinality, the failure states, and the wrapper's launcher
  selection.

## Passes

### Pass 1 — full pass

- **Kind** — full pass, the initial review, at the advisor's requested focus.
- **Register row and hand-over** — `sessions.tsv` pass 1 (`created`), hand-over section `deep-1`.
- **Reviewed revision** — the staged tree at base `0727fb8b`; nothing committed.
- **Outcome** — one should-fix finding and one nitpick; the reviewer verified the delta's preservation, the archived
  decision's engagement, the no-fallback stop, the SDK-match predicate, the wrapper's order, and the prior findings.
- **Limits** — the fixtures verify the paths and the live run covers one host state.

#### D1 — The same-version tie refused on bundle presence, not on "both register"

- **Status** — `FIXED_AND_VERIFIED`.
- **Evidence** — the implementation refused whenever two editions shared the newest version, while the delta and the tie
  scenario say "both register the interpreter", and the design's order 3 selects a pair that registers. A contributor
  with both editions but one registration was refused.
- **Fixed** — the tie now counts the same-version configurations that register the interpreter: exactly one selects it,
  more than one refuses naming both, none takes the missing-SDK stop; a case covers "only one registers".

#### D2 — The duplicate-entry refusal had no delta scenario or design row

- **Status** — `FIXED_AND_VERIFIED`.
- **Evidence** — the round-17 fix added the identified-path duplicate-refusal, and the proposal claimed a gained
  scenario, while the delta had none and D2's table no row.
- **Fixed** — the delta gains "The identified configuration registers the interpreter twice", D2's table gains the row,
  and the tie paragraph states the one-registration case.

### Pass 2 — full pass, verification

- **Kind** — full pass, the independent verification of pass 1's fixes.
- **Register row and hand-over** — `sessions.tsv` pass 2 (`continued`), hand-over section `deep-2`.
- **Reviewed revision** — the staged tree at base `0727fb8b` after D1 and D2 were fixed.
- **Outcome** — both prior findings `FIXED_AND_VERIFIED`, and one nitpick raised; no other finding.
- **Limits** — the fixtures verify the paths; the live run covers one host state.

#### D1 — verified

- **Status** — `FIXED_AND_VERIFIED`.
- **Evidence** — the tie counts the same-version configurations that register the interpreter (one selects, more than
  one refuses, none takes the missing-SDK stop), matching the delta's "both register" wording in every state; the new
  case fails on the pass-1 code.

#### D2 — verified

- **Status** — `FIXED_AND_VERIFIED`.
- **Evidence** — the delta carries one duplicate-entry scenario, D2's table its row, and the proposal's capability list
  maps to it; the implementation's message matches the scenario's THEN.

#### D3 — The tie's "neither registers" state had no case

- **Status** — `FIXED_AND_VERIFIED` by the caller.
- **Evidence** — a mutation of that fall-through would have passed the suite;
  `test_a_two_product_tie_with_no_registration_takes_the_missing_sdk_stop` now asserts the missing-SDK stop (not the tie
  wording) and no module.
- **Provenance** — the verification pass's own nitpick, so no third full pass is spent on it.

#### The budget

Two full passes are spent: the initial review and the independent verification. No third pass is owed: the only change
after the verification is the case the verification pass itself directed, and its evidence is the suite.
