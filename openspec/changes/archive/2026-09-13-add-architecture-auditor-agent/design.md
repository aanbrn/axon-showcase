## Context

`docs/adr/` holds the project's architecture decisions (seven ADRs plus an index) under the convention that a
cross-cutting decision is recorded at the time it is made. The architecture — the service boundaries, the module
dependency graph, and the `showcase/*` capability decomposition — is expressed across `AGENTS.md`, the spec corpus, and
the build files, while the ADRs record a subset of its cross-cutting decisions. No existing agent reads any of it:
`review-quick`/`review-thorough` are scoped to a single change, `specs-auditor` owns the spec corpus's internal
structure and explicitly not behavior-vs-code, `agents-auditor` owns the agent tooling, and `experience-analyzer` reads
a window of history rather than the current system. The repo's auditor-justification convention requires naming the
artifact's drift and which existing auditor cannot see it; here the artifact is the architecture and none can.

## Goals / Non-Goals

**Goals:**

- An on-demand, repeatable audit of the architecture artifact for drift from its recorded decisions.
- Two clearly separated output sections: verified findings (actionable) and advisory observations (design judgment).
- Findings verified against the repository, each with a location and a concrete suggested correction.
- No edits by the auditor: the main agent applies what the user approves, under the normal review gate.

**Non-Goals:**

- Scheduling or automation — deferred, not designed out: the scheduled (unattended) variant is parked in `docs/ideas.md`
  alongside the spec-corpus and AGENTS.md audits, which share the same mechanism, so a zero-touch run can be added later
  without changing this auditor.
- Behavior-vs-code drift — the change workflow's review loop and the archive-time spec sync own it.
- The spec corpus's internal structure — `specs-auditor` owns it.
- Re-checking any property an existing gate already enforces (Spotless formatting, Checkstyle, `forbidWildcardImports`,
  ESLint).
- Auditing the other docs (the README is its own parked idea; proposals and retrospectives are out).

## Decisions

- **Two-section report (findings vs advisory).** An auditor is defined by having a reference to audit against; the
  ADR/boundary/spec-fit checks have one, so a finding is true or false and the main agent can act on it. Design judgment
  has no such reference, so it is fenced into an advisory section — no severity, not a defect. _Alternative considered:_
  a separate "system-design reviewer" agent. Rejected on two grounds: with no reference it produces only opinion (the
  essay-generator failure mode), and it would share the architecture artifact with this auditor, so by the repo's own
  "widen an existing auditor when its artifacts are coupled" rule the judgment belongs inside this one, not in a second
  agent. _Also considered and rejected:_ widening `specs-auditor`, whose corpus touches the spec-decomposition axis —
  that auditor's remit is deliberately intra-corpus and explicitly not corpus-vs-code, so widening it would break its
  stated boundary.
- **Scope is the ADRs plus the architectural surface.** The ADRs are the smallest artifact that encodes design intent
  and are currently ungated; the boundary and spec-decomposition checks are what make this a system-design audit rather
  than ADR lint. _Alternative considered:_ ADR-only — rejected as too thin (seven files, low yield) and because it would
  leave the unrecorded-decision axis uncovered.
- **Spec-decomposition fit is in scope; spec-internal structure is not.** The `showcase/*` role groups are an
  architectural decomposition, so comparing them to the module/service structure is corpus-vs-code — a property neither
  existing auditor sees. `specs-auditor`'s intra-corpus checks (headers, Purpose fit, duplication) stay out.
- **Exclude any property an existing gate enforces.** A property the build already fails on is not a design finding:
  re-reporting it adds noise without adding information, and the auditor's remit is the design question behind the rule,
  not the rule itself. Where a rule is enforced (`spotlessCheck` formatting, Checkstyle, `forbidWildcardImports`,
  ESLint), the auditor stays out of it.
- **Unrecorded decisions are a finding.** Anchored to the existing `AGENTS.md` convention that a decision is recorded
  when it is made: a cross-cutting structural choice visible in the code or configuration with no ADR is reported as a
  finding with a suggested ADR. This is the axis no other agent can see, and it is why the audit earns its place.
- **Pro model, temperature 0** (`opencode-go/deepseek-v4-pro`), matching the other auditors: a careful cross-artifact
  pass, on-demand rather than per-change.
- **Requirement placement and the four-artifact coupling.** A new requirement in `showcase/quality/agent-skills`
  (mirroring the existing auditor requirements). A subagent is described across four artifacts — its own definition, an
  `AGENTS.md` convention bullet, the README agent table, and the `agent-skills` spec — so all four ship in the same
  change (plus the README slash-command-table row for the trigger, and the README architecture paragraph that surfaces
  the audit to a human reader).
- **A command trigger.** Per the repo rule that a subagent is only invocable through a trigger, not its own
  documentation.

## Risks / Trade-offs

- **The advisory bucket is mistaken for defects** → the agent definition fences it explicitly (no severity, not
  actionable without the user's decision) and the main agent must not "fix" advisory items.
- **Unbounded scope / essay generation** → the audit is bounded to the named drift classes plus the advisory section,
  and each verified finding must cite a location and evidence.
- **False positives** (a deliberate asymmetry, a structure that is intentional) → findings are verified against the
  repository before reporting and the user is the arbiter; the definition states the deliberate-asymmetry caveat
  explicitly, as the other auditors do.
- **Low yield** (seven ADRs, and the repo's recorded incidents have not been ADR drift) → accepted: the
  unrecorded-decision and boundary axes are the genuinely uncovered ones, and a clean audit is a valid result.
- **Cost** → on-demand only; the periodic variant is a deferred idea parked in `docs/ideas.md`, not part of this change.
