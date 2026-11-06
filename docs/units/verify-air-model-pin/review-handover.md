# Review hand-over — verify-air-model-pin

One section per quick-review invocation, written before the caller waits on it, so a stalled session is recovered from
this record rather than from the session.

## Round 1 — proposal stage

- **Unit** — `verify-air-model-pin`; change dir `openspec/changes/verify-air-model-pin/`; record
  `docs/units/verify-air-model-pin/unit.md`.
- **Base and current revision** — `origin/main` at the branch fork; `HEAD` at the same commit (nothing committed yet).
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — `proposal.md`, `design.md` (D1–D4, the 8-row claims table, dismissals, non-goals,
  verification), `tasks.md`, `.openspec.yaml`, the unit record, and the target the change will edit:
  `.opencode/skills/setup-agent-tools/SKILL.md`'s Air section; also `AGENTS.md`'s model-pin bullet and ADR-0024 as the
  sources the wording rests on.
- **Scope** — proposal-stage screen: coherence of proposal/design/tasks; whether the check is placed where a reader acts
  on it (before the first request); whether the wording avoids enforced claims and duplicated model identifiers; whether
  each claims row reproduces (the ACP probe, the config keys, the guard/build grep, the spec grep); `skip_specs`;
  Structured Writing, code spans, and the 120-character recipe. No implementation exists yet.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `openspec validate --all` reports 27 passed (with
  `skip_specs` accepted); `claim-scan.py --base origin/main` seeds the table, and every candidate is a row or covered by
  its kind's dismissal rather than counted here, since the ledger's own writes add candidates.
- **Limitations** — the Air panel's displayed model is the owner's report, labelled as such; the installed plugin
  package is installer state at the primary's `.opencode/node_modules`, absent from this fresh tree.
- **Subject packs** — none; the change edits a skill definition and records, with no JVM code, test tier, Gradle build
  decision, or deployment surface.
- **Deadline** — 20 minutes from this record's write.

## Round 2 — proposal stage

- **Unit** — `verify-air-model-pin`; base `origin/main` at `4709f9aa`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — the planning artifacts, `README.md`'s Parallel Units section, the same target section and
  sources, and `findings/round-1.md`.
- **Scope** — re-screen of round 1's eight instances, plus an owner amendment made after that round: the guidance now
  has two halves — the skill tells the agent to compare the model its harness reports and to restore or ask the owner,
  and `README.md` tells the person starting a unit in Air that the picker overrides the pin and to check it. This is a
  material scope change; the proposal was not yet approved, so the amendment rides the same proposal.
- **Prior findings** — round 1's eight, all applied: the identifier check now reads `git diff HEAD --` with a planted
  positive control; a row sources the ACP model-setting call; `cross-ref` joined the dismissals; the commit-hygiene
  checks are three invocations; `begin-unit` became the lifecycle's Begin step; the scan counts became a relative
  outcome; the unit record names this tree's `HEAD`; the proposal and task bullets were split.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `commit-hygiene.py`, and `openspec validate --all` pass.
- **Known limitations** — the owner's report of the displayed model stays unverified; the harness-model row is
  environment-supplied; the README gain is one sentence, not a procedure.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 3 — proposal stage

- **Unit** — `verify-air-model-pin`; base `origin/main` at `4709f9aa` (the branch was fast-forwarded over #855, which
  rewrote the lifecycle, README's Parallel Units section, and AGENTS); everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — the planning artifacts, `README.md`'s rewritten Parallel Units section (with the added
  sentence), the target Air section, `AGENTS.md`, ADR-0024, the unit record, this hand-over, and rounds 1–2 findings.
- **Scope** — re-screen of round 2's four instances: the refresh (README sentence reapplied to #855's Begin/Settle
  prose), the `--expect-clean` invocation pairing, the staged planted control, and the per-text read-back split. The
  claims rows were re-run at the new base (config keys, the ACP `session/new` and `session/set_config_option` probes,
  the guard/build grep, the spec grep, the AGENTS caveat), and `claim-scan.py --base origin/main` now sees only this
  unit's files.
- **Prior findings** — round 1's eight and round 2's four, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `commit-hygiene.py`, and `openspec validate --all` pass; the
  scan's four kinds (absolute, count, cross-ref, path) are dismissed.
- **Known limitations** — the owner's displayed-model report stays unverified; the harness-model row is
  environment-supplied; the README gain is one sentence.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 4 — proposal stage

- **Unit** — `verify-air-model-pin`; base `origin/main` at `4709f9aa`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — the planning artifacts, `README.md`'s Parallel Units sentence (now "may override"), the
  target Air section, `AGENTS.md`, ADR-0024, the unit record, this hand-over, and rounds 1–3 findings.
- **Scope** — re-screen of round 3's four instances: the unit record's `Stage`/`Next step` now state the operation and
  one next action; `tasks.md` names three invocations with the one exclusivity that applies; the `cross-ref` dismissal
  covers repo paths, PR/issue references, git refs, and root documents; the inline span's five-space run is collapsed.
  The README sentence uses the owner's softer wording, decided after round 3 opened.
- **Prior findings** — rounds 1–3, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `commit-hygiene.py`, and `openspec validate --all` pass.
- **Known limitations** — the owner's displayed-model report stays unverified; the harness-model row is
  environment-supplied; the README gain is one sentence.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 5 — proposal stage

- **Unit** — `verify-air-model-pin`; base `origin/main` at `4709f9aa`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — the planning artifacts, `README.md`'s Parallel Units sentence, the target Air section,
  `AGENTS.md`, ADR-0024, the unit record, this hand-over, and rounds 1–4 findings.
- **Scope** — re-screen of round 4's two instances: `proposal.md` and `tasks.md` now say "may override", matching the
  shipped sentence and the conditional the evidence supports; the unit record's change set names `README.md`'s
  modification beside the untracked change dir and records.
- **Prior findings** — rounds 1–4, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `commit-hygiene.py`, and `openspec validate --all` pass.
- **Known limitations** — the owner's displayed-model report stays unverified; the harness-model row is
  environment-supplied; the README gain is one sentence.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 6 — proposal stage

- **Unit** — `verify-air-model-pin`; base `origin/main` at `4709f9aa`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — the planning artifacts, `README.md`'s Parallel Units sentence, the target Air section,
  `AGENTS.md`, ADR-0024, the unit record, this hand-over, and rounds 1–5 findings.
- **Scope** — re-screen of round 5's single instance: the unit record's `Stage` no longer carries a round counter, so it
  states the operation and the pending action without decaying each round.
- **Prior findings** — rounds 1–5, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `commit-hygiene.py`, and `openspec validate --all` pass.
- **Known limitations** — the owner's displayed-model report stays unverified; the harness-model row is
  environment-supplied; the README gain is one sentence.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Consultation round 1 — technical advisor, proposal

Not a quick-review round; its findings are recorded here, never in a round file.

- **Scope** — the advisor reviewed the model-pin proposal (guidance only: the skill's agent check plus the README
  sentence) before the owner's manual pass.
- **Confirmed problems** — (a) the agent cannot check "before the first request": reading the skill takes an invocation,
  and the skill loads on a setup request rather than automatically in every session; (b) the runtime-visibility,
  restoration, and impossibility claims were overstated — row 10 is environment-supplied, and the ACP probe shows what a
  client can change, not what the running agent can observe or restore.
- **Fix** — the human half is a pre-prompt check with a recheck after switching agent or model; the agent half runs when
  the guidance is loaded, with an explicit "if trustworthy runtime model information is available … otherwise ask the
  owner" fallback; "the repository cannot lock a host's picker" became "no project-level lock is established by the
  evidence"; "none of the hooks can veto" became "no supported v2 veto mechanism has been demonstrated"; the quota
  wording became "a model the repository did not choose".
- **Agreement** — the advisor agreed on the corrected text (2026-11-07); his withdrawal condition is evidence that the
  documented picker check or the runtime-information source is unreliable.

## Round 7 — proposal stage

- **Unit** — `verify-air-model-pin`; base `origin/main` at `4709f9aa`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — the planning artifacts, the README sentence, the target Air section, `AGENTS.md`, ADR-0024,
  the unit record, this hand-over, and rounds 1–6 findings, plus the consultation's corrections.
- **Scope** — the advisor's two corrections: the human half is a pre-prompt check with a recheck after switching; the
  agent half runs when the guidance is loaded, with an explicit unknown-model fallback; the two impossibility claims are
  softened to "no project-level lock is established" and "no supported v2 veto mechanism has been demonstrated"; the
  quota wording became "a model the repository did not choose".
- **Prior findings** — rounds 1–6, applied; the consultation's two problems, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `commit-hygiene.py`, and `openspec validate --all` pass.
- **Known limitations** — the owner's displayed-model report stays unverified; the harness-model row is
  environment-supplied; the README gain is one sentence.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 8 — proposal stage

- **Unit** — `verify-air-model-pin`; base `origin/main` at `4709f9aa`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — the planning artifacts, `README.md`'s Parallel Units sentence, the target Air section,
  `AGENTS.md`, ADR-0024, the unit record, this hand-over, and rounds 1–7 findings.
- **Scope** — re-screen of round 7's three instances: the Impact's impossibility claim now matches claims row 5's
  demonstrated-absence form; the skill's load path is stated as both paths (setup request and `AGENTS.md`'s Air pointer)
  and sourced by claims row 11; the unit record's `Stage` no longer carries a closed-round counter.
- **Prior findings** — rounds 1–7, applied; the consultation's two problems, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `commit-hygiene.py`, and `openspec validate --all` pass.
- **Known limitations** — the owner's displayed-model report stays unverified; the harness-model row is
  environment-supplied; the README gain is one sentence.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 9 — implementation stage

- **Unit** — `verify-air-model-pin`; base `origin/main` at `4709f9aa`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — the implementation diff (`.opencode/skills/setup-agent-tools/SKILL.md`'s new best-effort
  block, `README.md`'s Parallel Units sentence), the planning artifacts, the unit record, this hand-over, and rounds 1–8
  findings plus the consultation section.
- **Scope** — implementation-stage screen: does the shipped skill block carry the loaded-guidance trigger, the
  runtime-information fallback, the mismatch branch, the recheck, the caveat pointer, no model identifier, and no
  enforcement claim; does the README sentence carry both checks and the softened "may override"; do the planning
  artifacts still match the shipped text; are tasks ticked where the work is done.
- **Prior findings** — rounds 1–8 and the consultation's two problems, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `check -PskipITs -Pcoverage.gate.enabled=false` (412 tasks),
  `openspec validate --all` (27), the no-identifier check with a staged positive control (1 on the plant, 0 on the real
  diff), and `commit-hygiene.py` pass.
- **Known limitations** — the owner's displayed-model report stays unverified; the harness-model row is
  environment-supplied; the README gain is one sentence.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 10 — implementation stage

- **Unit** — `verify-air-model-pin`; base `origin/main` at `4709f9aa`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-verify-air-model-pin`, branch `verify-air-model-pin`.
- **Supplied artifacts** — the shipped diff (skill block, README sentence), the planning artifacts, the unit record,
  this hand-over, rounds 1–9 findings, and the consultation section.
- **Scope** — re-screen of round 9's three instances: the skill block now only points at `AGENTS.md`'s caveat; the unit
  record's `Time and revision` no longer names a stage and `Pending decision` matches the recorded approval; the change
  set names the skill file's modification.
- **Prior findings** — rounds 1–9 and the consultation's two problems, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, the root `check` (412 tasks), `openspec validate --all` (27),
  the no-identifier check with its staged positive control, and `commit-hygiene.py` pass.
- **Known limitations** — the owner's displayed-model report stays unverified; the harness-model row is
  environment-supplied; the README gain is one sentence.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.
