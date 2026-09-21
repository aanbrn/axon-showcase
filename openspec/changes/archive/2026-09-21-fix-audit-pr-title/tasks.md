# Tasks — fix the audit report PR's title

## 1. The fix

- [x] 1.1 `.github/workflows/audit.yml` — the prompt names the action's PR-building behaviour (the body is the response;
      the title is a ≤40-character model summary of it) and asks for the owner mention on a body line without leading
      the response, so the generated title names the report.
- [x] 1.2 `AGENTS.md` — the `/oc` action paragraph records the title/body mechanism with the #333 incident.

## 2. The spec delta

- [x] 2.1 A `MODIFIED` delta to `showcase/quality/merge-governance` strengthening
      `Repository audits run on a schedule and on demand` with the title/body distinction, carrying all five scenarios
      (copy the main-spec block first, then edit).

## 3. Verification

- [x] 3.1 `workflowLint` (actionlint) passes on the edited workflow; `spotlessCheck` green; no line over 120.
- [x] 3.2 `openspec validate --all` passes with the delta.
- [ ] 3.3 Run `gh workflow run audit.yml` to exercise the prompt end to end and confirm the resulting PR's title is the
      report name, not the mention (a dispatch verification cannot live as a change-dir task — archived changes are
      invisible and no gate reads them — so it is named in the report and parked if not run at the merge).
