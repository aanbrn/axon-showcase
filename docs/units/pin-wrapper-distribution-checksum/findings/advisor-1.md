# Advisor consultation — proposal pass, round 1

- [contradiction] openspec/changes/pin-wrapper-distribution-checksum/tasks.md — the command's prose becomes incompatible
  with the pin, so the command's update must ship with it.
- [contradiction] openspec/changes/pin-wrapper-distribution-checksum/tasks.md — the command's completion criterion
  treats a properties-only diff as incomplete, though the generated jar is byte-identical when the running and target
  versions match.
- [over-claim] openspec/changes/pin-wrapper-distribution-checksum/design.md:54 — the design asserted what Gradle's
  `gradle-wrapper-upgrade` skill cannot control without having inspected any released definition of it.
- [missing-task] openspec/changes/pin-wrapper-distribution-checksum/design.md:50 — the rollback promise said "any
  failure" while the risks section acknowledged concurrency; the boundary and the retained recovery material were
  unspecified.
- [coherence] openspec/changes/pin-wrapper-distribution-checksum/design.md:73 — the tool resolved "latest stable" itself
  while the command also selected a candidate, leaving two sources with no precedence.
