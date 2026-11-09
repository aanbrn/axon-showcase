# Unit: bump-buildpacks-2026-11-09

Workspace:
`unit=bump-buildpacks-2026-11-09; path=/Users/afanasyev/Development/axon-showcase-bump-buildpacks-2026-11-09; branch=bump-buildpacks-2026-11-09; writer=ses_e42e5a9ffffeAigEgivK4Z3gVk`

State: closed

A change unit: its planning artifacts live under `openspec/changes/bump-buildpacks-2026-11-09/`.

The unit actions the buildpack update check's rows (issue #211, re-resolved by the check's own run on 2026-11-09): the
jammy and noble builders, the health-checker buildpack, and the web UI's nginx and procfile buildpacks, with each
builder's bundled buildpack versions confirmed before the pins are chosen.
