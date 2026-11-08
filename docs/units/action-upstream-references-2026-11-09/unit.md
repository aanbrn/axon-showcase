# Unit: action-upstream-references-2026-11-09

Workspace:
`unit=action-upstream-references-2026-11-09; path=/Users/afanasyev/Development/axon-showcase-action-upstream-references-2026-11-09; branch=action-upstream-references-2026-11-09; writer=ses_e42e46be1ffeQR0fg7Gj1MGzIY`

State: closed

Action the CI-prepared "Upstream references" issue (#343): re-verify each closed reference's disposition and its
recorded retirement condition against the pinned versions, add the OpenSpec diagnostic/behavior fix split and the
helm-plugin `#145` closure, and stop the scanner reporting local `docs/units/**/process-defects.md#N` anchors as
unresolved upstream references. The helm-plugin `#240`/`#241` work was delivered by the `adopt-plugin-retried-fetches`
unit (merged as #872) and is not re-opened here.

## Decisions

- The 2026-11-07 audit report's config-guard suggested rewrite is not applied literally: its `could not parse` finding
  is already satisfied by the live past-tense text (the list header still reads "Each guard retires on its own
  condition", but the item itself says "retired when this repository's pin reached `v1.16.0`", and `ci.yml` carries only
  the list-shape grep), and the `#1891` sentence it repeats is the one this unit corrects. The report stays as a dated
  record; a unit applying that finding re-derives it against the live bullet.
