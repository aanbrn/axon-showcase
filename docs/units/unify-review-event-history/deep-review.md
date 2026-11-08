# Deep review — unify-review-event-history

## Routing decision

- **Decided** — 2026-11-08, before any deep-review session was created.
- **Outcome** — `REQUIRED`.
- **Floor and signals** —
  `python3 scripts/review-routing.py --unit unify-review-event-history --change unify-review-event-history` reports
  `REQUIRED` on
  `[requirement-block-added] openspec/changes/unify-review-event-history/specs/showcase/quality/commit-hygiene/spec.md`.
  The decision is at the floor: the change adds a requirement the build gate must enforce, and it changes the validation
  every future unit's records pass through.
- **Owner resolution** — not applicable; a `REQUIRED` outcome runs.

## Passes

### Pass 1 — full pass

- **Kind** — full pass, the initial review.
- **Register row and hand-over** — `sessions.tsv` pass 1 (`created`), hand-over section `deep-1`.
- **Reviewed revision** — the staged tree at base `457dca95`; nothing committed.
- **Outcome** — one should-fix finding and one nitpick; the reviewer's own report named the implementation otherwise
  correct, well-tested, and coherent with its specs.
- **Limits** — this unit's quick rounds predate the retirement date, so the six-field path is exercised by the controls
  and a disposable fixture rather than by a live unit.

#### D1 — The quick row's `Round <N>` link reimplemented the `--handover` predicate, and already diverged

- **Status** — `FIXED_AND_VERIFIED`.
- **Evidence** — `handover_rounds` matched `^## Round ([1-9][0-9]*)\b`, while `--handover` uses `HANDOVER_HEADING_RE`,
  whose legal ATX forms include ` ## Round 1` and `##\tRound 1`; the finding reproduced that a heading the mandatory
  pre-delegation check accepts failed the register's link check, and that a round token such as `01` was accepted by the
  register's `isdigit()` while the argument grammar rejects it.
- **Fixed** — one `round_heading_tokens` predicate now backs both `carries_round_section` and `handover_rounds`, and a
  quick row's round is validated by `HANDOVER_ROUND_RE`;
  `test_a_quick_row_resolves_the_heading_the_hand_over_check_accepts` and
  `test_a_quick_row_with_a_non_canonical_round_fails` pin both halves.

#### D2 — The digest's single-file `--record` join had no test

- **Status** — `FIXED_AND_VERIFIED`.
- **Evidence** — the register join was exercised only through a directory fixture, so the `[path]` branch and its
  sibling-register lookup were unread by the suite.
- **Fixed** — `test_a_single_file_record_joins_the_register_beside_it` asserts the echo for a single `rounds.tsv` file
  beside its register.

### Pass 2 — full pass, verification

- **Kind** — full pass, the independent verification of pass 1's fixes.
- **Register row and hand-over** — `sessions.tsv` pass 2 (`continued`), hand-over section `deep-2`.
- **Reviewed revision** — the staged tree at base `457dca95` after D1 and D2 were fixed.
- **Outcome** — both prior findings verified fixed, and one new nitpick raised; no other finding.
- **Limits** — unchanged from pass 1.

#### D1 — verified

- **Status** — `FIXED_AND_VERIFIED`.
- **Evidence** — `round_heading_tokens` is the single predicate both `carries_round_section` and `handover_rounds` read,
  the register's quick-row round is validated by the same `HANDOVER_ROUND_RE` grammar `parse_handover_argument` uses,
  and both new tests fail on the unfixed behavior and pass now.

#### D2 — verified

- **Status** — `FIXED_AND_VERIFIED`.
- **Evidence** — `test_a_single_file_record_joins_the_register_beside_it` exercises the single-file branch and its
  sibling-register lookup, and fails if the join yields no session.

#### D3 — The run predicate's fix left its per-line helper orphaned

- **Status** — `FIXED_AND_VERIFIED` by the caller.
- **Evidence** — `heading_names_round` had no caller after D1;
  `grep -rn heading_names_round scripts/ .opencode/ docs/ openspec/ README.md AGENTS.md` now returns only one historical
  mention, in another unit's hand-over record (`docs/units/gate-review-handover-section/review-handover.md`), which is
  left as recorded because it states what that unit did at the time. The suites pass unchanged (472 and 27).
- **Provenance** — the deletion is the verification pass's own directed nitpick, so no third full pass is spent on it.

#### D4 — A post-retirement unit with no register escaped the completeness rule

- **Status** — `FIXED_AND_VERIFIED`.
- **Provenance** — raised by the owner-authorized technical-advisor consultation (`advisor-impl-1`), not by a deep pass;
  the record is here because it is the same unit's review history.
- **Evidence** — `find_agent_session_violations` iterated only the registers that exist, so a post-retirement ledger
  with no register received no completeness check; and the staged guard ran the agent-session validation only when a
  register changed, so a ledger-only staged append bypassed it.
- **Fixed** — the validation iterates the union of register units and post-retirement ledger units, and `check_staged`
  judges a unit whose ledger is staged as well as one whose register is; controls cover a missing register at the mode
  and function levels and a staged ledger-only append beside an unchanged register.

#### D5 — Staged completeness could take its evidence from an untracked register

- **Status** — `FIXED_AND_VERIFIED`.
- **Provenance** — the same consultation's follow-up (`advisor-impl-2`).
- **Evidence** — the staged read used `staged_or_tree_text`, whose working-tree fallback let a staged ledger take report
  evidence from an untracked `sessions.tsv` the commit would omit; the reproducing control returned a false pass under
  that read.
- **Fixed** — `check_staged` reads registers, ledgers, and the hand-over index-only through `index_or_empty`, and the
  control now refuses the incomplete staged state.

#### The budget

Two full passes are spent: the initial review and the independent verification. After the verification, the owner's
consultation raised D4 and D5 — coverage defects of the checker's staged path — and each was fixed with a control
reproduced in both directions and re-verified by the advisor across two follow-ups; a third full pass is therefore not
spent, the recorded decision resting on the reproduced controls rather than on an impression of low risk.
