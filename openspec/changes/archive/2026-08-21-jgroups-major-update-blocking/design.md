# Design: Block major updates for JGroups with documented rationale

## Context

See proposal.md — Why. The mechanism (catalog-ownership filter + opt-in major-disabled list) already exists and is
captured in `showcase/quality/dependency-management`. This change only adds `org.jgroups` to the shipped list, with a
rationale, and removes an uncommitted ad hoc edit that added it without one.

The compatibility facts that justify the suppression, verified from the resolved dependency graph:

- `org.jgroups:jgroups` resolves to `4.2.30.Final`, unified by the `platform` BOM override between two transitive 4.x
  constraints: Axon's JGroups extension (`org.axonframework.extensions.jgroups:axon-jgroups:4.12.0`, whose own
  dependency is constrained to JGroups 4.2.x via the Axon BOM) and `org.jgroups.kubernetes:jgroups-kubernetes:1.0.17.Final`.
- `jgroups-kubernetes` (the KUBE_PING discovery protocol used by the Kubernetes deployment — the services configure
  `axon.distributed.jgroups.kube-ping.namespace`/`labels`) declares `<version.jgroups>4.2.4.Final</version.jgroups>`
  in its own POM; there is no JGroups-5-compatible `jgroups-kubernetes` release.
- Axon's JGroups extension creates the channel via `new JChannel(String)` loading an XML stack config (`tcp-nio.xml`
  from the JGroups jar, overridable), and is built and tested against JGroups 4.x only.
- JGroups 5.0 is a breaking major: JDK 17 baseline (not a blocker on Java 21), removed deprecated APIs, and changed
  the XML stack-config schema/attributes so 4.x-authored configs are not portable.

JGroups is therefore an indirect dependency whose version the project cannot move independently: a 5.x bump would run
the Axon connector against an untested library major and strand KUBE_PING discovery.

## Goals / Non-Goals

**Goals:**
- Ship `org.jgroups` in `config/dependency-updates/major-disabled.properties` with a pointer to the recorded rationale.
- Suppress the JGroups 5.x major jump from `./gradlew dependencyUpdates` while keeping 4.x minor/patch updates visible.

**Non-Goals:**
- Migrating to JGroups 5 or upgrading Axon's JGroups extension / `jgroups-kubernetes` — tracked upstream, out of scope.
- Changing the `rejectVersionIf` mechanism or the ownership filter.

## Decisions

**D1 — Use the group prefix `org.jgroups`, not exact coordinates.**
`matchesDisabled` with a bare group matches the group and its sub-groups (dot-boundary aware). One prefix covers both
`org.jgroups:jgroups` and `org.jgroups.kubernetes:jgroups-kubernetes`, both of which are locked to 4.x. Exact
`group:module` entries would need two lines and would miss future JGroups sub-modules. Rejected alternative: exact
coordinate pairs — more maintenance, no added precision given both artifacts share the same constraint.

**D2 — Drop the uncommitted edit first, then re-add via the change.**
The working-tree `org.jgroups` line is reverted so the diff belongs to this change's apply step, keeping the
properties file history traceable to the captured rationale. Rejected alternative: leaving the line in place and
adding the rationale around it — mixes an undocumented edit with the change and leaves the base commit in the
reverted state anyway.

**D3 — Record the rationale in the spec, with only a short pointer comment in the properties file.**
The project convention is "no comments in source code"; the properties file carries only the format header today. The
substantive "why" (ecosystem lock) lives in the dependency-management spec and this design doc; the file gains a
one-line pointer so the suppression is self-documenting without duplicating the analysis. Rejected alternative:
embedding the full rationale as a multi-line comment in the properties file — duplicates content that will drift.

## Risks / Trade-offs

- [A future Axon extension or `jgroups-kubernetes` release supports JGroups 5, and the entry silently hides a now-
  actionable upgrade.] → Mitigation: the spec states the entry is limited to majors the ecosystem does not support;
  re-evaluate the entry when the Axon JGroups extension or `jgroups-kubernetes` bumps its own JGroups baseline.
- [Suppressing majors could mask a JGroups 5 security fix.] → Mitigation: minor/patch updates remain reported, so
  4.x security fixes still surface; a 5.x-only fix would be flagged by the dependency security scan (`snyk`), which is
  independent of the version report.
- [4.x minor/patch updates keep being reported for a library pinned indirectly.] → Acceptable: mirroring how
  `org.axonframework`/`org.springframework` are handled, an upstream 4.x bump can be adopted by nudging the catalog or
  the BOM override when the ecosystem allows it.