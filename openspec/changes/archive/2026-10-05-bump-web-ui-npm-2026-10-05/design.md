# Design

## Context

See `proposal.md` — Why. All five packages' `Wanted` already equals their `Latest` in the web UI's `npmOutdated` report:
the caret ranges declared in `package.json` (`^5.104.0`, `^26.6.3`, `^10.9.1`, `^30.1.1`, `^8.3.1`) already admit the
newer releases. So this is an in-range lockfile bump, not a `package.json` range edit. The repo's prior in-range npm
bump (`bump-gradle-and-web-ui-dependencies`, #400) took the same shape.

## Goals / Non-Goals

**Goals:**

- Take the in-range npm updates the 2026-10-04 report surfaces.

**Non-Goals:**

- `typescript` 7 (the deferred major, suppressed in `config/web-ui-updates/major-disabled.txt`) and any `package.json`
  range change.

## Decisions

### Decision: a targeted `npm update`, not a clean reinstall

`npm update <pkg>…` for the five named packages moves exactly the reported coordinates and reconciles their transitive
requirements; a clean reinstall would churn the whole lockfile for no benefit. The lockfile diff is described by what
moved.

## Risks / Trade-offs

- **`npm update` reconciles transitive dependencies beyond the named packages** → read the lockfile diff and describe it
  by what moved; run the frontend `check` (lint, format-check, type-check, Vitest).
