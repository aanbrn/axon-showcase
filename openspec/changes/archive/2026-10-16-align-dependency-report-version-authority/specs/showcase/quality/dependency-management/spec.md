## MODIFIED Requirements

### Requirement: Dependency update reporting covers only catalog-owned versions

The build's `dependencyUpdates` task SHALL report available updates only for dependencies whose version is explicitly
declared in the version catalog (`gradle/libs.versions.toml`) via an exact `version.ref`. Dependencies whose version is
inherited from a BOM or constraint — and thus not decided by the project — SHALL NOT be reported. A catalog-owned
coordinate whose declared version is not older than the candidate SHALL NOT be reported when the plugin's current
version differs from it.

#### Scenario: Catalog-owned dependency reports updates

- **WHEN** a dependency coordinate has an exact `version.ref` in the version catalog and a newer version is available
- **THEN** the report lists the available update for that coordinate

#### Scenario: BOM-inherited dependency is not reported

- **WHEN** a dependency coordinate has no exact version in the version catalog (its version comes from a BOM or
  constraint)
- **THEN** the report does not list updates for that coordinate

#### Scenario: A coordinate whose reported current disagrees with the catalog is not reported

- **WHEN** a coordinate's exact `version.ref` version is the available candidate version or a newer one, while the
  plugin reports a lower "current version" for it — the signature of a current read from a constraint floor rather than
  from what the project takes
- **THEN** the report does not list an update for that coordinate

#### Scenario: A coordinate another surface holds below the catalog's is not reported either

- **WHEN** another surface governs a catalog-owned coordinate's resolved version below the version the catalog declares
  for it, while that declared version is not older than the candidate
- **THEN** the report does not list an update for that coordinate

#### Scenario: An up-to-date coordinate keeps its place in the up-to-date section

- **WHEN** a coordinate's exact `version.ref` version is also the version the plugin reports as current
- **THEN** the report does not list an update for it, and still reports it as using the latest release version
