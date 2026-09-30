# Spec Delta

## MODIFIED Requirements

### Requirement: The local toolchain is diagnosed without the build

The repository SHALL provide a diagnostic that probes the local machine's toolchain and reports each prerequisite's
status, so a contributor can learn what is missing in one pass instead of discovering it through a failing task. The
diagnostic SHALL run with nothing but a POSIX shell — it SHALL NOT require Java, Gradle, a network, or the project's
dependencies — so it can diagnose the build's own prerequisites, including Java itself. It SHALL cover three classes of
prerequisite: **tool presence**, **minimum versions**, and **repo state** (the setup steps beyond a bare install). It
SHALL report, per prerequisite, whether it is satisfied and, for anything actionable, the install or setup command that
resolves it. The set of probes SHALL be **declared once** — one row per probe naming it and its class — and the
declaration's **shape SHALL be validated where it is owned**, so a malformed row cannot make a probe invisible. The
diagnostic SHALL report **every declared probe** on any machine, and the declared set SHALL be asserted by a check that
runs in the build and verifies both the declaration's shape and that the diagnostic actually reports every probe it
declares — so a probe silently changing class, or failing to reach the code, fails rather than passing unnoticed.

#### Scenario: A satisfied prerequisite is reported as satisfied

- **WHEN** the diagnostic runs on a machine that has a prerequisite installed at a supported version
- **THEN** that prerequisite is reported as satisfied, with its detected version where one is readable

#### Scenario: A missing prerequisite names its remedy

- **WHEN** the diagnostic runs on a machine missing a prerequisite
- **THEN** that prerequisite is reported as missing, together with the command that installs it

#### Scenario: A too-old prerequisite is distinguished from a missing one

- **WHEN** a prerequisite is installed but below its minimum supported version
- **THEN** the diagnostic reports it as too old — stating the detected version, the required minimum, and the upgrade
  command — rather than reporting it as missing or satisfied

#### Scenario: The diagnostic runs on a machine without Java

- **WHEN** the diagnostic runs on a machine where Java is absent or too old, so the Gradle build cannot start
- **THEN** it still runs to completion and reports Java's status along with the other prerequisites

#### Scenario: An unreadable version is reported as unknown, never as satisfied

- **WHEN** a prerequisite's executable is present but its version cannot be read (the probe prints nothing it can parse)
- **THEN** the diagnostic reports that prerequisite's version as unknown rather than reporting it satisfied, so a
  missing version reading is never indistinguishable from a version that meets the floor

#### Scenario: Every declared probe is reported

- **WHEN** the diagnostic runs, on any machine and whatever is installed
- **THEN** it reports one status line for every probe in its declaration, so a probe that never reaches the reporting
  code is a detectable omission rather than a silent absence

#### Scenario: A malformed declaration fails rather than hiding a probe

- **WHEN** the probe declaration is malformed — a row with the wrong number of columns, a blank or duplicated key, or a
  value outside what a column allows
- **THEN** the diagnostic fails loudly naming the row, and the check fails too, rather than a probe disappearing from
  the set the diagnostic reports

#### Scenario: A probe cannot erase its own documentation

- **WHEN** a probe is declared as not documented in the README while a Prerequisites row documents it, or declared as
  documented while its row is absent
- **THEN** the check fails on the disagreement, because the probe-to-row mapping is anchored to the README's own rows
  rather than to the declaration — so a probe cannot drop out of the documented list by declaring itself out of it

#### Scenario: The check runs in the build and asserts the declaration

- **WHEN** the build's default verification runs
- **THEN** the check asserts the declaration's shape, that the diagnostic reports every declared probe, and the set's
  agreement with the README and the spec's required set — without requiring a network, a container, or an installed
  prerequisite
