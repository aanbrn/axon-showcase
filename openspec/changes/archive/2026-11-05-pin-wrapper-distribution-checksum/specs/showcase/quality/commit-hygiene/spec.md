## ADDED Requirements

### Requirement: The wrapper's distribution checksum is verified by the build

The standard `check` task SHALL verify that `gradle/wrapper/gradle-wrapper.properties` pins its distribution with a
`distributionSha256Sum` of 64 hexadecimal characters and a `distributionUrl` whose archive name ends `-all.zip`, so a
bump that drops or mangles the pin fails the build rather than leaving the wrapper accepting an unverified download.

#### Scenario: The standard check verifies the wrapper's checksum

- **WHEN** the standard `check` task runs
- **THEN** it includes the wrapper-checksum verification

#### Scenario: A properties file without a checksum fails the build

- **WHEN** `gradle/wrapper/gradle-wrapper.properties` carries no `distributionSha256Sum`
- **THEN** the verification fails and names the missing pin

#### Scenario: A malformed checksum fails the build

- **WHEN** the pinned value is not 64 hexadecimal characters, or the `distributionUrl` does not name a `-all.zip`
  archive
- **THEN** the verification fails and names the offending value

#### Scenario: A clean wrapper passes

- **WHEN** the properties pin a 64-hex checksum and an `-all.zip` distribution
- **THEN** the wrapper-checksum verification passes
