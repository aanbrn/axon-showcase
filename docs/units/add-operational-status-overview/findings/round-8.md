- [formatting] openspec/changes/add-operational-status-overview/design.md:135 — the re-pinned claims rows carried an
  unescaped shell pipe, splitting each into four cells against a three-column header and failing `verifyTableRows`.
- [wording] docs/units/add-operational-status-overview/unit.md:22-23 — the change-set line claimed every file was under
  the change dir and the unit dir, while three implementation files live elsewhere.
