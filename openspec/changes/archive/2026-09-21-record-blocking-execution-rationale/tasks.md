# Tasks — record the blocking-execution rationale

## 1. Establish the facts

- [x] 1.1 Gateway: remove `ShowcaseBlockingExecutionConfigurer`, run `ShowcaseRestControllerCT` — 9 of 76 fail, all
      `400` → `500` on validation paths, zero BlockHound hits; restore and confirm 76/76 green.
- [x] 1.2 Query service: remove `ShowcaseQueryConfigurer`, run the BlockHound-guarded `ShowcaseQueryControllerIT` — 3 of
      10 fail (`400`/`404` → `503`), zero BlockHound hits; restore and confirm 10/10 green.
- [x] 1.3 Confirm each experiment left the tree clean (nothing from either removal carried into the change).

## 2. Record it

- [x] 2.1 `AGENTS.md` — a Gotcha bullet: both configurers are load-bearing, with the per-service measured effect and the
      absence of BlockHound hits, next to the BlockHound test-tier convention.
- [x] 2.2 `docs/ideas.md` — the parked removal idea leaves (explored and decided against); verify only that entry is
      removed and its section siblings remain.

## 3. Verification

- [x] 3.1 `spotlessCheck` green; no line over 120 in the touched files.
- [x] 3.2 `openspec validate --all` passes with the change present.
- [x] 3.3 The audit reports are left unedited (they are the run's record), and no other doc still calls either
      configurer removable.
