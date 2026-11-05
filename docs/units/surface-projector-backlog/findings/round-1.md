- [premise] openspec/changes/surface-projector-backlog/proposal.md:5-9 — the change claimed the dashboard never shows
  how far behind the projector is, while panel 19 (Event Age) and panel 86 already render its progress as time.
- [missing-task] openspec/changes/surface-projector-backlog/tasks.md:25-29 — nothing pinned the Prometheus exposition
  names the new panels query, whose unit suffix the Micrometer name does not show.
- [contradiction] openspec/changes/surface-projector-backlog/design.md:43 — the "no chart change" Non-Goal forbade the
  dashboard JSON edit a Goal and task 3.1 require.
- [missing-scenario] openspec/changes/surface-projector-backlog/specs/showcase/read-side/projection-service/spec.md:5-12
  — the planned failure counter is absent from the requirement's enumeration.
- [missing-scenario] openspec/changes/surface-projector-backlog/design.md:89-90 — the "no resolved partition" state was
  unspecified, and its default (0) is the healthy zero the change forbids.
- [incomplete-enumeration]
  openspec/changes/surface-projector-backlog/specs/showcase/read-side/projection-service/spec.md:61-66 — the
  unresolved-cause list omitted a partition later samples no longer return, and no task stated which partition set a
  sample visits.
- [coherence] openspec/changes/surface-projector-backlog/tasks.md:28-29 — task 1.3 claimed unit coverage of four
  publication outcomes that task 2.1 did not deliver, and no task created the seam they need.
- [coherence] openspec/changes/surface-projector-backlog/tasks.md:53 — the sample-age expression omitted the
  pre-first-sample guard its sibling panel uses, so it would render an epoch-scale age.
- [impact-omission] docs/ideas.md:311-314 — the parked entry this change implements was named by no task.
- [impact-omission] openspec/changes/surface-projector-backlog/proposal.md:39-40 — the site list was hand-written and
  omitted the deployment pack, the two new test classes, and the README Cool Story bullet.
- [missing-task] openspec/changes/surface-projector-backlog/tasks.md:16-19 — the two new properties shipped without the
  binding and rejection cases `ShowcaseProjectorPropertiesCT` carries for every other property.
