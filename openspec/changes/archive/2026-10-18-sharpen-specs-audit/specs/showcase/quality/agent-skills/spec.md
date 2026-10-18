## MODIFIED Requirements

### Requirement: The spec corpus is audited for structure and consistency

The repository SHALL provide a `specs-auditor` agent subagent that audits the `openspec/specs/` corpus for **structure
and consistency**, and reports its findings without editing files. The audit SHALL cover, at least: each spec's `#`
title matching its capability path; each Purpose describing the requirements the spec holds; requirement-header and
requirement-body conventions (a declarative header, a SHALL body, WHEN/THEN scenarios); requirements duplicated or
near-duplicated across specs; and dead cross-references to renamed classes, files, or configuration the specs cite. The
audit SHALL NOT verify behavior against the implementation — that remains with the change workflow's review loop and the
archive-time spec sync. Each finding SHALL be verified against the repository, and SHALL be reported grouped by severity
with its location and a concrete suggested rewrite. A requirement header reused across two genuinely different
capabilities SHALL be reported for judgment rather than treated as a defect. The subagent SHALL propose its findings
without modifying files; the main agent verifies and applies those the user approves.

A reference SHALL be reported as dead only where the referenced artifact is verified absent or obsolete — its exact name
and variants searched and its rename, relocation, replacement, and current context checked — and SHALL NOT be reported
where it merely cannot be located, where a filename differs in case or extension from an informal mention, where the
class is generated or produced by a build, where the command belongs to an external tool, where the configuration key is
defined indirectly, where the name appears only in documentation, or where the reference is conceptual rather than an
exact symbol. A stale reference SHALL be corrected to its verified successor, or removed where the referenced concept no
longer applies, without rewriting the requirement's behavior.

Two similar requirements SHALL be compared semantically — the obligation, the actors and components, the conditions, the
observable outcomes, and the scope, ownership, and exceptions — and SHALL be reported only where the obligations are
substantially identical or their overlap leaves ownership or precedence materially ambiguous; a requirement that is
related but distinct (the same concept with a different actor, boundary, condition, outcome, or responsibility) and an
intentional repetition (a shared convention repeated so each capability stays self-contained) SHALL NOT be reported as a
defect. Before recommending a consolidation the audit SHALL read both requirement bodies and their scenarios and the
surrounding Purposes, establish the owners and scopes, and establish what a merge would lose.

The audit SHALL NOT audit the corpus's capability decomposition (whether the role groups still match the module and
service structure), which the `architecture-auditor` owns; SHALL NOT re-report an issue an existing gate already
enforces (the `openspec validate` well-formedness, the review loop's behavior check); and SHALL NOT audit the internal
quality of a proposed change or change delta, which the review loop owns.

A requirement heading, a Purpose, or a requirement body SHALL be reported only for a violation of a stated convention: a
heading containing a verb, a technical term, or an established expression is not malformed, a valid normative body is
not reported for carrying explanatory prose, an incidental occurrence of `SHALL` that expresses no obligation is not
sufficient, and a scenario is checked by its Markdown structure rather than by a pattern match alone.

Confidence SHALL be an internal filter, not a report category: the audit SHALL NOT attach confidence or severity scores,
and SHALL omit a candidate whose evidence shows an intentional, coherent choice.

Symptoms sharing a root cause and a correction SHALL be reported as one finding, and independent defects SHALL NOT be
merged, repeated across severity groups, or padded with a placeholder.

The audit SHALL proceed in stages — inventory, local structure, cross-spec consistency, reference verification, report
validation — reusing the evidence an earlier stage gathered.

#### Scenario: A spec-corpus audit is produced

- **WHEN** the main agent invokes the `specs-auditor` subagent (e.g. via the `/audit-specs` command)
- **THEN** it returns findings grouped by severity, covering title/path match, Purpose fit, requirement conventions,
  cross-spec duplication, and dead cross-references, each with a location and a suggested rewrite

#### Scenario: Findings are verified against the repository

- **WHEN** the `specs-auditor` subagent flags a title, cross-reference, or apparent duplicate
- **THEN** it checks the claim against the repository (the capability path, the referenced file or symbol, the bodies of
  the requirements involved) before reporting it, so a still-true title or a legitimately shared header is not reported
  as a defect

#### Scenario: Behavior drift is out of scope

- **WHEN** the `specs-auditor` subagent audits the corpus
- **THEN** it does not verify requirement scenarios against the implementation, leaving behavior-vs-code drift to the
  change workflow's review loop and the archive-time spec sync

#### Scenario: The auditor does not edit files itself

- **WHEN** the `specs-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves

#### Scenario: A dead reference is verified before it is reported

- **WHEN** the `specs-auditor` subagent finds a class, file, configuration key, or command a spec names that it cannot
  locate
- **THEN** it searches the exact name and its variants and checks renames, relocations, generated artifacts,
  replacements, and the reference's current context before reporting it, and it does not report a case/extension
  difference, a generated class, an external tool's command, an indirectly-defined configuration key, a
  documentation-only name, or a conceptual reference as dead

#### Scenario: A candidate duplication is classified before it is reported

- **WHEN** two requirements across specifications appear similar
- **THEN** the audit compares the obligation, actors, conditions, outcomes, scope, and exceptions, and reports the
  candidate only where the obligations are substantially identical or their overlap is materially ambiguous — not a
  related-but-distinct requirement or an intentional repetition

#### Scenario: The out-of-scope surfaces are not audited

- **WHEN** the `specs-auditor` subagent audits the corpus
- **THEN** it does not audit whether the role groups still match the module or service structure (the
  `architecture-auditor` owns that), does not re-report an issue an existing gate already enforces, and does not audit
  the internal quality of a proposed change or change delta (the review loop owns that)

#### Scenario: A correction preserves the intended behavior

- **WHEN** the `specs-auditor` subagent proposes a rewrite, merge, split, or reference update
- **THEN** the recommendation names the exact text to change and the smallest correction that repairs the problem,
  preserving the requirements' conditions, outcomes, exceptions, and the distinctions between capabilities, and stating
  for a merge why the requirements are semantically equivalent

#### Scenario: Confidence is a filter, not a report category

- **WHEN** the `specs-auditor` subagent weighs a candidate whose evidence shows an intentional, coherent choice, or
  several symptoms of one root cause
- **THEN** it omits the coherent choice, reports the shared symptoms as one finding, and attaches no confidence or
  severity score — repeating no item across groups and adding no placeholder for a category with no findings

#### Scenario: The audit proceeds in bounded stages

- **WHEN** the `specs-auditor` subagent audits the corpus
- **THEN** it inventories the corpus, checks local structure, compares related requirements across specs, verifies the
  questionable references, and validates the report against the scope — reusing the evidence an earlier stage gathered
