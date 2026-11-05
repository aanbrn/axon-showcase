- [contradiction] openspec/changes/surface-projector-backlog/specs/showcase/read-side/projection-service/spec.md:71-74 —
  the "resolves no partition" scenario claimed a never-committed group leaves the observation stale and the failure
  count as evidence, while that sample succeeds.
- [missing-scenario]
  openspec/changes/surface-projector-backlog/specs/showcase/read-side/projection-service/spec.md:66-67 — the
  failed-sample clause sat inside a scenario whose WHEN is an unresolvable partition, so no scenario reached it.
- [coherence] openspec/changes/surface-projector-backlog/tasks.md:48-51 — the publication unit cases had no seam: the
  only package-private method returns the arithmetic, and the gauge writing lived in the lifecycle component.
- [docs-residue] openspec/changes/surface-projector-backlog/tasks.md:73-76 — task 3.3 amended the boundary but left
  ADR-0023's Consequences pointer to the parked idea this change removes.
- [claim-missing] openspec/changes/surface-projector-backlog/design.md:67-72 — decision 3's
  `AdminClientConfig.configNames()` behaviour claim had no claims-table row.
- [wording] openspec/changes/surface-projector-backlog/design.md:73-77 — decision 4 said an absent partition "keeps its
  series", which is not what the union buys; it is that the series is updated to NaN.
- [wording] openspec/changes/surface-projector-backlog/specs/showcase/read-side/projection-service/spec.md:10 — the
  normative clause named "the summary", a subject the requirement never defines.
- [premise] openspec/changes/surface-projector-backlog/proposal.md:10 — the Why still ended on "says nothing about the
  distance it is behind", the claim the Context corrects.
- [missing-task] openspec/changes/surface-projector-backlog/tasks.md:18-20 — the two properties' enumerating cases
  (documented defaults, yml placeholders) were not extended, though they assert every property.
