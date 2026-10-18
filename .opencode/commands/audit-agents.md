---
description:
  Audit the project-owned agent tooling (AGENTS.md + project-authored .opencode/ files) with the pro-model
  agents-auditor subagent, including the always-loaded instruction budget and the authoring-system gaps that keep
  producing a defect
---

Run the agent-tooling audit to check the project's guidance and agent files for consistency and conciseness.

1. Invoke the `agents-auditor` subagent (`.opencode/agent/agents-auditor.md`) on the repository — it reads `AGENTS.md`
   and the project-authored `.opencode/` files in full, and also reads the generated (OpenSpec instruction files) and
   vendored (`axon4to5-*`) ones to report any that contradict how the repository uses them; it checks each `AGENTS.md`
   entry against the knowledge-authoring standard (the `agent-skills` spec), reports a passage that violates the
   Structured Writing Standard with its practical impact and a minimally invasive restructure, and traces a recurring
   defect to the mechanism that produced it (an authoring-system gap, or an advisory item where that mechanism is
   excluded); it measures the always-loaded instruction budget — the file's size and its size by section, the effective
   bound with its source, the first rule beyond the derived threshold, the growth since the newest report under
   `docs/audits/`, the bytes safely removable, the rules that must stay loaded and the rules safe to load on demand, and
   the extraction candidates with their retrieval-path results; it reports the accreted meta rules with their origins
   (the `captured:` marker, or `git blame` / `git log -S`), and its verdict counts the accreted rules as distinct meta
   rule units carrying a `captured:` marker and states how many are new since the newest report under `docs/audits/`;
   and it reports an **extraction opportunity** — a source unit whose destination does not exist yet, and which
   therefore cannot yet pass the retrieval-path test — as a design recommendation excluded from the verdict, in the
   ledger's own subsection. It never edits an excluded file.
2. Present its report in the contract it returns (the verdict line first, then findings budgeted per item and grouped by
   severity, plus separate advisory (third-party inconsistency), accretion (meta rules with their origins), and
   authoring-system gap (a recurring defect's mechanism) classes, the merge, removal, and route candidates counted in
   the verdict line, and the separate `Instruction-budget ledger` section with its extraction candidates and its
   `Extraction opportunities` subsection, which is excluded from the verdict counts), presenting each item as the shared
   report contract requires — do not re-summarize it.
3. Ask the user which findings to apply — do not edit files without their go-ahead.
4. Apply the approved findings, run `./gradlew spotlessApply` for the formatter-wrapped files the audit edited
   (`AGENTS.md` and the project-authored `.opencode/` markdown — the audit's `README.md` copy is fixed by the change's
   docs sweep, and its spec-side copy routes to the corpus owner), then re-run the `review-quick` subagent over the
   resulting diff before reporting the audit done.
