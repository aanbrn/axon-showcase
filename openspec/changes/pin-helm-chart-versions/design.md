## Context

See `proposal.md` for the motivation. The version catalog (`gradle/libs.versions.toml`) has three floating
major-line chart pins — `prometheus-community-stack = "77.x.x"`, `grafana-tempo = "1.x.x"`,
`bitnami-common = "2.x.x"` — while the infra charts (postgres `16.7.27`, kafka `31.5.0`, opensearch `2.0.10`) are
already concrete and gate-verified by `verifyInfraImageVersions`. The observability charts (kps, tempo) have no
test-surface coupling, so the existing image-drift gate does not — and need not — cover them. The manual `helm
install` commands in `AGENTS.md` currently pin no versions at all, contradicting the catalog.

## Goals / Non-Goals

**Goals:**
- Make every Helm chart coordinate in the catalog concrete, pinning current behavior without a major jump.
- Document the "all Helm chart pins are concrete" convention in `AGENTS.md`.
- Make the documented manual `helm install` path use the same pinned versions as `helmInstallToLocal`.

**Non-Goals:**
- Not changing the infra chart pins or the `verifyInfraImageVersions` gate.
- Not adding a build gate that rejects floating chart pins (documented convention only, per decision).
- Not upgrading any chart to a newer major line (e.g. kps 77 → 88); that is a separate, deliberate bump.

## Decisions

**Decision: pin to the newest version within the current major line.**
`prometheus-community-stack` → `77.14.0`, `grafana-tempo` → `1.24.4`, and `bitnami-common` → `2.41.0`. Rationale:
these resolve the floating pins to the exact version they currently deploy, so the change is behavior-preserving — no
chart version actually moves. Alternatives considered:
- *Pin to the newest available version* (e.g. kps `88.x`) — a major jump is a separate decision with its own upgrade
  risk; not the job of "make pins concrete".
- *Leave observability charts floating* — reproduces the status quo and defeats the later update-check goal.

**Decision: document the convention in AGENTS.md, not a build gate.**
The rule "every Helm chart coordinate in the catalog is concrete" is recorded next to the existing infra image-version
note, and the manual `helm install` commands gain `--version` flags matching the catalog. Rationale: a gate would be
redundant ceremony for three coordinates today, and the user chose to keep it a documented convention for now. The
later update-check change can revisit enforcement if desired. Alternatives considered:
- *Extend `verifyInfraImageVersions`* to reject floating chart pins — the task is scoped to infra image drift; adding
  a general chart-pin rule mixes concerns.

**Decision: update the spec scenario wording, not a new capability.**
The concrete-pin rule is a modification of the existing "Infrastructure image references are single-sourced"
requirement in `showcase/quality/infra-image-versions`, extended from infra charts to all catalog chart coordinates.
Alternatives considered: a new `helm-chart-pinning` capability — the rule is one sentence on an existing requirement,
not a new behavioral area.

## Risks / Trade-offs

- [The chosen concrete versions could be stale by the time the change merges (a newer `77.14.x` or `1.24.x` exists)] →
  Intentional: the change pins current behavior; the later update-check surfaces any newer version for a deliberate
  bump.
- [The manual `helm install` commands duplicate the catalog versions, so they could drift again] → The convention note
  and the later update-check make drift visible; the commands are explicitly the manual fallback next to the
  `helmInstallToLocal` alternative.
- [A future maintainer reintroduces a floating pin] → The documented convention and the spec scenario call it out; a
  build gate can be added later if it recurs.

## Migration Plan

1. Update the three catalog coordinates to concrete versions.
2. Add the convention note and `--version` flags to `AGENTS.md`.
3. Verify `helmInstallToLocal` still installs (or `helm dependency build` resolves `common` at `2.41.0`), then run
   `check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`.
4. Rollback: revert the three catalog lines and the `AGENTS.md` edits.

## Open Questions

None.