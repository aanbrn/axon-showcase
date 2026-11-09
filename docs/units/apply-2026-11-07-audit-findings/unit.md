# Unit: apply-2026-11-07-audit-findings

Workspace:
`unit=apply-2026-11-07-audit-findings; path=/Users/afanasyev/Development/axon-showcase-apply-2026-11-07-audit-findings; branch=apply-2026-11-07-audit-findings; writer=ses_e42e413a2ffeuPZ6Kvtwr9Pukg`

State: closed

The unit applies the 2026-11-07 audit report's spec-corpus findings: the two structural defects in
`openspec/specs/showcase/quality/commit-hygiene/spec.md`. The report's other findings are separate units — the
config-guard guidance correction is `apply-2026-11-07-audit-doc-findings` and the dead `handlebars` platform constraint
is `remove-dead-handlebars-constraint` — per `showcase/quality/agent-skills`'s split requirement, which the owner's
decision of 2026-11-09 confirmed after the technical advisor raised it. Its plan and tasks live in
`openspec/changes/apply-2026-11-07-audit-findings/`.
