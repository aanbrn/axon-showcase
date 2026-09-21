# Fix the audit report PR's title

## Why

The `audit` workflow's prompt asked the agent to "begin your final response with a mention of the repository owner",
because the OpenCode GitHub action uses that response as the pull request body. But the action also derives the title by
asking a model to summarise the whole response in under 40 characters, so a response that opens with the mention tends
to summarise into a title about the mention: report PR #333 was titled
`cc @aanbrn — 2026-09-21 audit report ready for review.`, demoting the report's subject into it. The prompt conflated
the two outputs, and the title mechanism was undocumented in the repository.

## What Changes

- **`.github/workflows/audit.yml`** — the prompt now tells the agent that the action derives the title by summarising
  the response (under 40 characters) and uses the response as the body: put `cc @<owner>` on a body line and keep the
  response's substance about the report, so the generated title names it rather than the mention. The owner mention
  still lands in the body, as the requirement specifies.
- **`AGENTS.md`** — the `/oc` action paragraph records the action's PR-building behaviour (the body is the response; the
  title is a ≤40-character model summary of it), so a future workflow prompt that relies on it states the derivation
  instead of assuming a copied line.
- **`docs/ideas.md`** — a parked entry recording the end-to-end verification (dispatch the workflow, or wait for the
  next scheduled run, and confirm the generated title names the report), since no gate can check it.

## Impact

- **Specs**: one `MODIFIED` delta to `showcase/quality/merge-governance` —
  `Repository audits run on a schedule and on demand` gains the title/body distinction, so the requirement cannot be
  satisfied by a title-carrying mention. All five scenarios are carried.
- **Code**: none — a workflow prompt and docs.
- **Observability**: the next scheduled audit run's PR title is the check; the workflow is `workflow_dispatch`-enabled,
  so it can be dispatched to verify (a dispatch verification cannot live as a change-dir task —
  `openspec/changes/archive/` is invisible and no gate reads it — so it is named in the report and parked if not run at
  the merge).
