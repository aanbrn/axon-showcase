# Unit: bump-dependencies-2026-11-09

Workspace:
`unit=bump-dependencies-2026-11-09; path=/Users/afanasyev/Development/axon-showcase-bump-dependencies-2026-11-09; branch=bump-dependencies-2026-11-09; writer=ses_e42e73653ffeujhWjXVqjEPkCc`

State: closed

The unit takes the stable catalog and web-UI updates the weekly dependency check reports: the minor and patch rows in
`gradle/libs.versions.toml`, the in-range npm rows reconciled with `npm update`, and the `build-logic` plugin pin no
other unit owns. Its plan and tasks live in `openspec/changes/bump-dependencies-2026-11-09/`.

The three majors the reports name (`db-scheduler` 17, `@hookform/resolvers` 7, `jsdom` 31) and the Gradle wrapper
(`9.8.2` -> `9.9.0`) are Non-Goals. The `helm-plugin` / `helm-releases-plugin` `3.3.1` pin is excluded because the
`adopt-plugin-retried-fetches` unit took it, merged as `0deca079` while this unit was in flight.
