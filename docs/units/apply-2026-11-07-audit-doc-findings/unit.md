# Unit: apply-2026-11-07-audit-doc-findings

Workspace:
`unit=apply-2026-11-07-audit-doc-findings; path=/Users/afanasyev/Development/axon-showcase-apply-2026-11-07-audit-doc-findings; branch=apply-2026-11-07-audit-doc-findings; writer=ses_e42e413a2ffeuPZ6Kvtwr9Pukg`

State: closed

The unit applies the 2026-11-07 audit report's agent-tooling finding: the stale config-guard tracking bullet in
`AGENTS.md`. The owner's decision of 2026-11-09 split it from the spec-corpus fixes, which are the unit
`apply-2026-11-07-audit-findings`, because `showcase/quality/agent-skills` requires findings of different routes or
scopes to be separate units. No capability spec states the bullet's rule, so the change declares `skip_specs`. Its plan
and tasks live in `openspec/changes/apply-2026-11-07-audit-doc-findings/`.
