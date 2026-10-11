# Proposal

## Why

The scheduled `audit` workflow hung for 2h13m on 2026-10-10 before being cancelled (healthy runs take 10–29m). The
`bash` permission rule `"*git push*": "ask"` in `.opencode/opencode.json` matches any command whose text merely
_contains_ the phrase, and the hang was a read-only `grep` quoting `git push`. In an unattended run no one can answer
the prompt, and the job carried no `timeout-minutes`, so nothing bounded it — the run was cancelled by hand at 2h13m
(GitHub's 360-minute default was the only backstop). The class is not a one-off: the `/oc` workflow — the same action —
has had runs cancelled at 42m and 17m, both on `external_directory` asks (a related instance, since granted in the
config).

## What Changes

- `.opencode/opencode.json`: narrow the `bash` guard from the substring pattern `"*git push*"` to the parsed-command
  form `"git push *"`, so a read-only command that quotes the phrase no longer asks, while a `git push` command — bare,
  with arguments, or chained — still does.
- `.github/workflows/audit.yml`: bound the `audit` job with `timeout-minutes: 60`, so a future unanswered prompt fails
  within the bound and leaves a downloadable log rather than hanging to the platform ceiling.
- Record the lesson in `AGENTS.md`, and report the action's unbounded-hang gap upstream with a close-out clause.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. The scheduled audits' "unattended" behavior is already specified
(`showcase/quality/agent-skills`, `showcase/quality/merge-governance`), so this is code→spec alignment; the
permission-rule matching and the job timeout are tooling mechanisms that change no specified scenario's outcome. (Cf.
`retire-opencode-permission-plugin`, `fix-audit-workflow-report-formatting`.)

## Impact

- Files: `.opencode/opencode.json`, `.github/workflows/audit.yml`, `AGENTS.md`.
- No Java, build, test, or deployment impact; the audit workflow's triggers, permissions, and report are unchanged.
