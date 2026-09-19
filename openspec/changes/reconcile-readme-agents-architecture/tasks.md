# Tasks — reconcile the architecture description across README and AGENTS.md

## 1. The component-count ambiguity

- [x] 1.1 Changed the README `## Architecture` table's lead-in to "four services and the web UI", agreeing with
      `AGENTS.md`, `openspec/config.yaml`, and the tree's five directories.
- [x] 1.2 Confirmed it was the only deficient copy: `README.md:90` was the sole "four components"; `README.md:421`
      already says "the four services, and the web UI", and `AGENTS.md:522` / `openspec/config.yaml:9` already use the
      disambiguating phrasing.

## 2. The ports

- [x] 2.1 No change owed: the `bootRun` task→port lines are actionable, the `application.yml` `server.port` is the
      value's source, and `AGENTS.md`'s "Ports:" paragraph documents deployment ports (compose debug, published web UI,
      nginx `stub_status`) that no service config holds — so it is not a duplicate.

## 3. The idea

- [x] 3.1 Removed the idea from `docs/ideas.md` — under the header's third disposition, **explored and decided against**
      (its premise was disproved), not "implemented". The removal took exactly the entry's 14 lines; the durable lesson
      is captured in `AGENTS.md`.

## 4. Verification

- [x] 4.1 Re-ran the diff with a digit-tolerant pattern: both inventories match `settings.gradle.kts`'s 19 includes with
      zero missing and zero extra. The apparent omission was the buggy `showcase-[a-z-]+` detector truncating
      `…resilience4j…` at its digit — no inventory edit was owed.
- [x] 4.2 `spotlessApply`/`spotlessCheck` green; no line over 120.
- [x] 4.3 `check -PskipITs -Pcoverage.gate.enabled=false` green; `openspec validate --all` 23/23 with the change as
      `skip_specs`.
