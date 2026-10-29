## 1. Widen the auditor definition

- [x] 1.1 Add the language axis to `.opencode/agent/readme-auditor.md`: a provable error (agreement, tense, article,
      preposition, a duplicated or dropped word, an em-dash left unclosed where a paired insertion was opened, a
      fragment in a passage that needs a full sentence) is a finding carrying the passage, the rule the text violates,
      and a minimal meaning-preserving rewrite; a debatable wording, an unclear intent, and a grammatical sentence whose
      first parse misleads stay advisory
- [x] 1.2 State the routing rule in the same definition — a language correction that would change a technical claim is
      verified against a source, with the three outcomes the delta names (refuted → accuracy finding, holds → the
      correction is rejected, unestablishable → advisory) — and narrow the "subjective quality is advisory" rule so it
      no longer swallows a provable error
- [x] 1.3 Update the definition's frontmatter `description` and its report contract to name four axes, keeping the
      verdict line shape (`<n> findings, <n> advisory`) and the shared per-item budget reference
- [x] 1.4 Split the definition's method rule — "treat every claim as a hypothesis and verify it against the repository"
      — so it states the two evidence sources: an assertion is verified against the repository, a language finding by
      the passage and the rule it violates
- [x] 1.5 Verify the definition states the same provable-fault set the delta's clauses state — no narrower, no wider —
      naming `showcase/quality/agent-skills` as the owning spec; the definition implements the set (the model reads it
      at run time) and must not cite the change directory, which the archive removes

## 2. Align the sites that describe the audit

- [x] 2.1 `.opencode/commands/audit-readme.md`: step 1 names four axes, the frontmatter `description` names the language
      axis, and the advisory decision point distinguishes a preference from a provable error — pointing at the
      definition's boundary rather than restating it
- [x] 2.2 `AGENTS.md`'s readme-auditor bullet names the language axis (it currently says "on three axes") and keeps its
      pointer to the `agent-skills` spec for the boundary
- [x] 2.3 `README.md`: the `readme-auditor` agent-table row, the `/audit-readme` slash-command row, and the
      Self-Learning Loop bullet that describes the audit each name the language axis, preserving the section order and
      the one-line row style

## 3. Spec corpus

- [x] 3.1 Verify the delta at
      `openspec/changes/widen-readme-auditor-with-language-axis/specs/showcase/quality/agent-skills/spec.md` — REMOVED
      names the old header with its reason and migration, ADDED carries every retained obligation and every retained
      scenario under its original name and in the original relative order, plus the language scenarios and the
      design-intent scenario — and that `openspec validate --all` passes
- [x] 3.2 Apply the capability `## Purpose` refresh in the archive commit: the Purpose names the README audit's scope,
      which the language axis extends — recorded as an explicit task because a delta cannot carry a Purpose

## 4. Verification

- [x] 4.1 Smoke-run the widened auditor with controls. Write the expected outcome and its ground for each seeded passage
      into the task record **before** the run, and do not hand the auditor those answers. Save a copy of `README.md`,
      seed five passages into it, and run the `readme-auditor` subagent once: - two provable language errors — expected
      as language findings, each with the rule it violates and a rewrite that preserves the meaning; - one "grammar"
      correction whose minimal rewrite would alter a technical claim — expected as an accuracy finding, a rejected
      correction, or advisory, depending on what the source establishes; - one style preference — expected as advisory
      **or absent from the report**, never a finding; - one correct control that the axis must not flag, such as a
      single em-dash standing alone as an aside. Then restore the saved copy and confirm the file matches it byte for
      byte — on a failed run too, before any fix is attempted. This is a bounded smoke test of five passages, not a
      measure of classification accuracy
- [x] 4.2 `./gradlew spotlessApply` after the final edit, then `./gradlew spotlessCheck`, the PR gate
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, and `openspec validate --all` all clean
- [x] 4.3 Re-read the `AGENTS.md` auditor-justification bullet and adjust it only if this widening makes its text
      inaccurate

## 5. Review gate and records

- [x] 5.1 The pre-write derivation pass is recorded as this change's claims table in `design.md`, and it is re-derived
      after the implementation's last edit
- [x] 5.2 Run `review-quick` over the implementation, fix its findings, and re-run until it reports nothing new; record
      each round in `docs/review-rounds.tsv` under this unit
- [x] 5.3 Derive the deep-review routing decision with
      `python3 scripts/review-routing.py --unit widen-readme-auditor-with-language-axis --change     widen-readme-auditor-with-language-axis`
      and record its outcome and signals on this task; a `REQUIRED` outcome runs before completion, a `RECOMMENDED` one
      waits for the owner's run-or-skip decision. **Outcome: `REQUIRED`; signals `requirement-block-added`,
      `requirement-block-removed`. Deep review pass 1 ran: 0 should-fix, 1 nitpick (these two tasks' state), nothing
      blocking.**
- [x] 5.4 Append the unit's process-defect record at `docs/process-defects/widen-readme-auditor-with-language-axis.md`,
      or state that none was observed

  **Smoke-run expectations (recorded before the run, with the seeds withheld from the auditor):**

  | Seed (seeded at)                                                                       | Expected outcome                                                                     | Ground                                                                                                                                      |
  | -------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------- |
  | `README.md:23` "The query service expose a single generic gRPC RPC."                   | language finding                                                                     | wrong agreement: a singular subject takes `exposes`                                                                                         |
  | `README.md:135` "Although the projection service consumes Kafka events."               | language finding                                                                     | a subordinate clause standing as a sentence where the README needs a full one                                                               |
  | `README.md:218` "The published images support `linux/arm64` as well as `linux/amd64`." | accuracy finding, a rejected correction, or advisory — never a bare language finding | grammatical, but false against `docs/adr/0018` and the `releases` spec; a language pass that keeps the claim is the failure this seed tests |
  | `README.md:831` "It should be noted that … in a manner that lets you observe …"        | advisory **or absent**, never a finding                                              | grammatical, merely wordy                                                                                                                   |
  | `README.md:942` "… — a deliberate choice."                                             | not reported by the language axis                                                    | a single aside dash with no unclosed pair; flagging it is the false-positive this control tests                                             |

  **Smoke-run result:** 5 seeds, one run. Line 23 came back as a language finding with the rule and a rewrite; line 135
  as a language finding (recommended deletion); line 218 as an **accuracy** finding citing `docs/adr/0018` and the
  `releases` spec, not a language finding; line 831 as advisory (wordiness); line 942 was not flagged by the language
  axis — it appeared in the advisory on redundancy grounds, which is a different basis and not the unpaired-dash
  false-positive the control tests. `README.md` was restored from the saved copy and `cmp` reports it byte-identical.
  The run also surfaced one pre-existing convention violation outside the seeds — the README's growing-corpus count
  ("120+ and counting"; the figure is still literally true, and the convention forbids such a count in a durable
  artifact regardless) — which this change parks rather than fixes.
