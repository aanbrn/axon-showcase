## Context

The lesson capture is the pass that writes process lessons into `AGENTS.md`, and it has been doing so without leaving a
trace of where each rule came from. Measured on the current file: 65 of its 142 top-level bullets arrived through
capture/audit/process commits, 46 of them capture-class, and those 65 are concentrated in Gotchas (41) and Conventions
(21). Classing those 46 by subject, roughly 26 are meta — `A subagent is only invocable through a trigger`,
`Justify a new auditor by a distinct artifact/property`,
`external_directory and permission.bash are separate permission keys` — and roughly 20 are product lessons that merely
arrived through a capture (host state versus a pin, verifying a buildpack by running the image rather than a green
build).

Two failure modes follow. A reader cannot tell a rule about the agent from a fact about the product, so the corpus reads
as one flat body of guidance and its meta half is invisible. And a rule's origin survives only in git, where a markdown
reflow re-attributes it: the Spotless formatting commit owns 123 lines of the file by reflowing text it did not write.

## Goals / Non-Goals

**Goals**

- Every newly captured rule names the change (and, post-merge, the PR) that produced it.
- The tooling audit surfaces meta rules as a distinct, counted class, with the origin of each.
- Provenance is readable without git, and recoverable through git for the rules written before this change.

**Non-Goals**

- Retro-marking the 46 existing capture-introduced bullets — the audit reports their origin from git history instead, so
  the historical tail is covered without a large mechanical diff.
- Splitting `AGENTS.md` into product and process files — this change makes the distinction visible and attributable;
  where the content lives is a separate decision.
- Classifying every bullet as meta or product — the audit reports the meta rules it identifies, not a full taxonomy.

## Decisions

**The marker is in the prose, in a greppable form: `captured: <change>`.** The alternatives were to rely on git blame
alone (rejected: it decays under every reflow, which is how the current file lost its provenance) and to keep a
structured index beside `AGENTS.md` (rejected: a second file that must be maintained and can drift from the prose it
describes). A `captured:` token is expected to survive Prettier's reflow — the token contains no space for the formatter
to break at, so a wrapped line still carries it whole — which is why a plain `grep -n "captured:"` finds every marked
rule, and task 4.5 verifies it on a paragraph long enough to actually rewrap.

**The accretion class is its own section, not folded into advisory.** The advisory class is bounded by a harm test for
third-party inconsistency; an accreted meta rule is neither third-party nor inconsistent — it is in scope, it may be
correct and load-bearing, and it is simply about the agent rather than the product. Folding it into advisory would bury
it in a class whose items invite a decision (report upstream, re-vendor, change our usage) that does not apply.

**Detection is marker-first, git-second.** The marker is authoritative for rules written after this change; `git blame`
and `git log -S` recover the origin of the ones written before it, and of any rule whose marker a human edit dropped.
The auditor reports the origin it established either way, and states which source it used, so a reader can tell an
established origin from an inferred one.

**The verdict line names the accretion count.** The shared report contract makes an audit's first line state its counts;
a report that gains a class names it there, or the first line silently understates what follows.

## Risks / Trade-offs

- **A marker on every captured rule adds prose weight.** Bounded by the existing rule that each addition names the
  bullet it extends — the marker is a suffix on a rule that already exists, not a new rule.
- **The audit's meta/product split is judgment, not a gate.** Two auditors could class a borderline rule differently;
  the criterion (a rule about the agent, its tooling, the per-change workflow, or the documentation, as opposed to a
  fact about the product) is stated in the definition so the class is reproducible, and each item carries its origin so
  the main agent can disagree.
- **A marker is prose, so nothing enforces it.** The audit is the check: a newly captured rule with no marker is visible
  as a rule whose origin only git can supply.
