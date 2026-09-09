## Context

The 2026-08-14 role-group restructure moved spec files into `showcase/<role>/<capability>/` via `git mv`, which
preserves file history but never touches file content — so each spec's `#` header stayed at the old
`showcase/<capability>` path. Nothing validates the header against the capability path, so the drift passed CI silently.
Eight specs carry such stale headers today (a separate audit also found two specs with title-case headers —
`# Ide Config Specification`, `# Infra Image Versions Specification` — which are a different style, not a path mismatch,
and out of scope here).

## Goals / Non-Goals

**Goals:**

- Fix the eight stale `#` headers to match their capability paths (`# showcase/<role>/<capability> Specification`).
- Keep all requirement/scenario/behavior text unchanged.

**Non-Goals:**

- No title-case style change for the `ide-config` / `infra-image-versions` specs (they don't have a path mismatch).
- No capability path changes — only the internal titles.

## Decisions

### D1: Title-only fixes, `skip_specs: true`

Edit each of the eight specs' first line to `# showcase/<role>/<capability> Specification`, change nothing else. Like
the `rest-api` rename, this is a pure documentation fix, so the change declares `skip_specs: true`.

### D2: Scope is the eight path-mismatched specs

The two title-case specs (`ide-config`, `infra-image-versions`) are styled differently but reference no wrong path, so
they're out of scope — the change targets only the stale headers that omit the role segment.

## Risks / Trade-offs

- **No validation gate** → nothing enforces the header-path match, so this could drift again; the AGENTS.md gotcha
  (captured after the `rest-api` rename) records the rule for future spec moves. A future improvement could add a
  validation check, but that's beyond this title-fix change.
