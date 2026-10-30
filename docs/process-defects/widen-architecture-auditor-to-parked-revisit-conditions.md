# Process defects — widen-architecture-auditor-to-parked-revisit-conditions

### widen-architecture-auditor-to-parked-revisit-conditions#1 — The `MODIFIED` block was written from a truncated read of the requirement

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — proposal.

**Expected** — a `MODIFIED` block copies the requirement's whole body and every scenario (`AGENTS.md`'s "Author a spec
delta" procedure states it), so the scenario set is enumerated from the requirement's own boundary — its header to the
next `### Requirement:` — rather than from an excerpt.

**Observed** — the delta was written from a `sed -n '<header>,+45p'` slice of
`openspec/specs/showcase/quality/agent-skills/spec.md`, which ended after the seventh scenario. The requirement has
nine, so the block omitted "A deferral's condition is classified before it is reported" and "A parked idea whose work
only partly landed is reported as partial".

**Evidence** — `openspec validate --all` reported
`MODIFIED "Unexplained design intent is surfaced for clarification" omits scenario(s) the current spec still has: "A deferral's condition is classified before it is reported", "A parked idea whose work only partly landed is reported as partial". The omission was corrected by inserting both blocks in their original relative position, after which the run reported `27
passed, 0 failed`.

**Consequence** — one failed validation and one repair before any review round; no other artifact was affected, and the
two scenarios were restored verbatim.

**Knowledge boundary** — observed: the slice's line range, the two omitted scenarios, the validator's message, and the
repair. Not checked: whether any other artifact in this unit was written from a bounded read rather than from its
subject's own boundary.

- **Decision** — no-change: the spec-delta procedure already requires copying the whole requirement block;
  `openspec validate` caught the omission before review.

### widen-architecture-auditor-to-parked-revisit-conditions#2 — The positive control's premise check verified only one of its two conditions

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation (the smoke-run is that stage's verification).

**Expected** — task 4.2 requires each seed's premise verified **before** the run, and the positive control's premise has
two parts: its condition appears met **and** the work the entry describes does not exist. Both decide whether the seed
can be reported by the branch the control targets.

**Observed** — only the condition was verified. The seed was an entry whose condition was "`scripts/claim-scan.py` gains
an `--include` option" (present) and whose described work was wiring that option into the pull-request body path; both
had shipped together in #745, so the run reported the entry as condition-met **and** work-landed, and the control
isolated nothing.

**Evidence** — that run's advisory read "its recorded reopening condition is met, and the described work also landed …
added in #745". `git show --stat fd586544` names the body drafting beside the option.

**Consequence** — one extra pro-model run over a re-seeded file, and the control's evidence came from the second run;
the first run's report is recorded on task 4.2 rather than discarded.

**Knowledge boundary** — observed: the one-sided check, the run's report, and the re-seed. Not checked: whether the
other controls' premises were equally one-sided, which re-reading each seed against its control would show.

- **Decision** — no-change: the smoke-run bullet already requires the seed's premise verified against the repository
  before the run; the control's premise had two parts.
