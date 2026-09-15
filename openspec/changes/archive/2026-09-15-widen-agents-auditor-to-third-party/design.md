## Context

The `agents-auditor` audits the project-owned agent tooling along two axes — consistency and conciseness — over a scope
drawn by provenance: `AGENTS.md` and the project-authored `.opencode/` files. It deliberately skips the generated
OpenSpec instruction files (`openspec update` writes them) and the vendored `axon4to5-*` skills, because "a local 'fix'
there is overwritten or breaks provenance". The rationale is about **editing**, and it holds.

The gap is that the same partition also suppresses **observing**. A skipped file can disagree with this repository in a
way that matters — a vendored migration skill prescribing an Axon 4 pattern the codebase has moved past, a generated
command naming a file or flag the workflow no longer has — and today nothing reads it: the audit skips it, and no build
gate covers prose. The contradiction is discovered only by accident, if at all.

The existing `architecture-auditor` faces the same shape and solved it: it reports some items as **advisory** — "design
judgment … No severity, and not defects: the calling agent must not 'fix' an advisory item without the user's decision."
That is the pattern to reuse: an observation whose remedy is a decision, not an edit.

## Goals / Non-Goals

**Goals**

- Make third-party-tooling inconsistency visible, bounded by a harm test that keeps the class high-signal.
- Keep the provenance partition's fix rule intact: the auditor still edits nothing, and never proposes an edit to a
  skipped file.
- Route each item to a decision the owner can act on (report upstream, re-vendor, or change our usage).
- State the class in the `agent-skills` spec so it is a durable behavior.

**Non-Goals**

- Bringing generated or vendored files into the fix scope (the partition stands).
- Auditing third-party files against _each other_ (internal consistency of the upstream set) — the class is
  inconsistency **with our usage**; comparing upstream files to one another is a different property with a different
  remedy, and is not included.
- Re-deriving redundancy in `AGENTS.md` — the existing "Conciseness" axis already mandates merging near-duplicate
  entries, with `redundant` as a severity class.

## Decisions

**A third output class, not a new auditor.** The auditor-justification convention says widen an existing auditor when
the drift lives on artifacts it already holds; `.opencode/` is already its corpus, so a new auditor would duplicate the
whole definition to add one class. The class is added to `agents-auditor`.

**Advisory, not a severity finding.** The existing findings (`contradiction`, `stale`, `dead reference`, `redundant`,
`structural`) are all things the calling agent _fixes_. A third-party inconsistency cannot be fixed locally by
construction, so it must not sit among them — it joins a separate advisory section, like `architecture-auditor`'s, so
nothing treats it as a defect to repair.

**A harm test, stated.** Without one the class degenerates: every generated file differs from our prose by design, and
an audit that reports textual difference buries its real findings. The test is "would this mislead a workflow driven by
the file, or instruct a pattern our code or conventions contradict" — a contradiction that would change what a reader
does, not a difference in phrasing. The definition carries both sides of the test (an example in, an example out).

**The routes are named, as one invited decision each.** A finding with no permissible remedy is noise, so each item
names the decision it invites — **report upstream** (the repo has a convention for this, with a reproduction),
**re-vendor** at a newer upstream version, or **change our usage**. It names the one the evidence points to, not a menu:
the shared report contract forbids offering alternatives, and the auditors' advisories already carry an owner question
under that contract, so an item states a recommendation the owner can accept, redirect, or reject.

## Risks / Trade-offs

- **Noise.** The main risk; bounded by the harm test and by the class being advisory (so a weak item is a line the owner
  dismisses, not a false "fix"). The definition states both an in-test and an out-of-test example.
- **Scope confusion with the skipped set.** The change must be explicit that the files stay out of the _fix_ scope and
  only enter the _observation_ scope; the tasks verify the skip rule text is unchanged.
- **Overlap with the upstream-report practice.** The practice says report a gap upstream when identified; this class is
  how the auditor _identifies_ one. The definition cross-references rather than restates.

## Migration Plan

Not applicable — no behavior, code, or deployment change. The definition takes effect on the next OpenCode reload; the
change is verified by reading the definition and the spec, plus a smoke-run seeded with a known third-party mismatch.
