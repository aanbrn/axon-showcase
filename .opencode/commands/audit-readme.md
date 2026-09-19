---
description:
  Audit the repository's human-facing README for accuracy against the repository, fidelity to its documented design
  intent, and coverage of the human-visible capabilities the system offers, with the pro-model readme-auditor subagent
---

Run the README audit to catch what no gate sees: the human-facing showcase's content is unformatted-checked but not
fact-checked.

1. Invoke the `readme-auditor` subagent (`.opencode/agent/readme-auditor.md`) on the repository — it reads `README.md`
   in full, verifies each claim against the artifacts the README describes (`AGENTS.md`, the build files,
   `gradle/libs.versions.toml`, `helm/values/`, the workflows, the source, and the spec corpus), and audits three axes:
   accuracy/consistency, design-intent fidelity, and coverage/experience surfacing.
2. Present its report in the contract it returns (the verdict line first, then the findings grouped by the three axes,
   then the advisory section, each budgeted per item).
3. Ask the user which findings to apply — an advisory item (subjective prose or structure, for the user's judgment) is
   never "fixed" automatically, and a finding the README's own convention records as deliberate (e.g. the OpenSpec-flow
   diagram's asymmetry) is not a defect. Do not edit files without their go-ahead.
4. Apply the approved changes, run `./gradlew spotlessApply` for the formatter-wrapped files (`README.md` and any other
   markdown the audit edited), then re-run the `review-quick` subagent over the resulting diff before reporting the
   audit done.
