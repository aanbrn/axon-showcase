## REMOVED Requirements

### Requirement: The guard's checks are covered by tests run in the build

**Reason**: the requirement scoped coverage to the pre-commit guard's defect classes, while the capability ships twelve
checks — the guard's refusals, ten build checks, and the manual-pass readiness check — of which only the guard's owed a
test. The retitled requirement in the `ADDED` block covers every check the capability ships.

**Migration**: both scenarios carry over unchanged; the retitled requirement adds two scenarios — a mode absent from the
mapping, and one mapped to a class that never names it.

## ADDED Requirements

### Requirement: Every check the capability ships is covered by tests run in the build

Each check this capability ships SHALL be covered by an automated test that exercises it — the pre-commit guard's defect
classes, the build checks it owns, and the manual-pass readiness check — and those tests SHALL run in the standard
`check` task, so a disabled or weakened check fails the build. The test suite SHALL map every mode the checker exposes
to the test class whose source names the mode and its checker function's call form.

#### Scenario: The checker's tests run in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it runs the checker's automated tests

#### Scenario: A disabled check fails a test

- **WHEN** a check is disabled or its logic regresses
- **THEN** at least one test that exercises that defect class fails

#### Scenario: A mode added without a test fails the build

- **WHEN** the checker exposes a mode that the test module's mapping does not name, or names a class that has no test
- **THEN** the mapping control fails and names the mode

#### Scenario: A mode mapped to a class that never names it fails the build

- **WHEN** the mapping names a class that exists and carries tests, but its source does not name the mode, or does not
  name its checker function's call form
- **THEN** the mapping control fails and names the mode
