# Fixture (b) — the whole skip conjunction holds

Synthetic unit (not repository history). Run after the owner reloaded OpenCode, in a fresh session.

## Premise

- The unit edited `README.md` only (three prose sentences) and nothing else.
- Review-round classes: `formatting` and `wording` only; no compliance miss; no problem recorded in a `captured:` rule
  or `docs/ideas.md`.
- No candidate lesson was noticed during the work.

## Observed decision

`DECISION: skip`, `CONJUNCTION: holds`. The caller walked the four clauses against the unit's facts: the round classes
stay inside the local-implementation set; no compliance miss; the diff is `README.md` alone, which is not an authoring
surface; no problem the unit hit is recorded in either corpus.

## Skip line as written

```
- [x] Lesson capture: skipped under the skip condition in full — round classes `formatting`/`wording` only; no compliance
  miss; diff is `README.md` alone (no authoring surface); no problem hit is recorded in a `captured:` rule or
  `docs/ideas.md`.
```

The caller noted the merge-time read re-verifies a recorded skip, so the reasons must be the unit's real surfaces.
