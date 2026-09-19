# Design — reconcile the architecture description across README and AGENTS.md

## Context

The parked idea (`docs/ideas.md`) records that the README and `AGENTS.md` state the same architectural facts twice and
have drifted, and asks which copy is canonical. **Re-deriving the drift against the repository overturned the idea's own
example**, and this design records what actually remains rather than the premise it started from:

- **The module inventory is complete in both files.** The idea's cited drift was an omission of
  `showcase-resilience4j-extension`; a correct comparison of both inventories against `settings.gradle.kts`'s
  `include(...)` set finds **no missing module in either**. The apparent omission was an artifact of the detection regex
  (`showcase-[a-z-]+` truncates the name at its digit), not a documentation defect — the detector was wrong, not the
  docs.
- **The component-count framing is a real, but minor, ambiguity.** The README's `## Architecture` table says "four
  components" and lists four rows; its `## Project Structure` tree lists five service/gateway directories including the
  web UI. `AGENTS.md` and `openspec/config.yaml` both already use the disambiguating phrasing ("four services and a web
  UI"), so the README's table lead-in is the one copy that lacks it.
- **The HTTP ports are stated in several places** — the README's Getting Started block, `AGENTS.md`'s Local Development
  block, and each service's `application.yml` — plus `AGENTS.md`'s "Ports:" paragraph, which carries facts (the compose
  debug ports, the web UI's published port, nginx `stub_status`) that no `application.yml` holds.

## Goals / Non-Goals

**Goals**: resolve the component-count ambiguity at its one deficient copy; decide the ports' canonical home and apply
it where it genuinely duplicates.

**Non-Goals**: generating the module inventory (it is complete and its descriptions are human prose — see D1); any
behavior change; re-cutting the README's structure, which is deliberate.

## Decisions

### D1 — No generator, and no inventory change

The parked idea asked whether the inventory is worth generating from the Gradle module list. Two findings settle it
against generating: the inventory is already **complete** (so there is no completeness defect to fix), and while most
modules do carry `project.description`, the tree's annotations are human-facing prose tuned per entry — a generator
would trade that for a mechanical string to solve a problem that does not exist. The inventory stays hand-written and
complete.

### D2 — The component count: adopt the repo's existing phrasing at the one copy that lacks it

`AGENTS.md` and `openspec/config.yaml` already say "four services and a web UI"; the README's table lead-in says "four
components" while its tree shows five directories. The fix is to align the README with the phrasing the repository
already uses, not to invent a new taxonomy ("four CQRS components plus the UI" would be a new label: the gateway is an
edge, not a CQRS core, so the phrase needs a source it lacks). The table itself is left at four rows: the UI is not a
CQRS component (it talks to the gateway), and the tree already lists it as a fifth directory, so the changed lead-in
names it without the table needing a fifth row — adding one would re-create the very double-count the phrasing avoids.

### D3 — The ports: no genuine duplicate, so no cross-reference

On inspection **no prose port restatement proved to be a genuine duplicate**, so this change edits none. The `bootRun`
task→port lines are actionable and belong where a reader runs them; the `application.yml` `server.port` is the value's
source; and `AGENTS.md`'s "Ports:" paragraph documents deployment-specific ports (the compose debug ports, the published
web UI port, nginx `stub_status`) that no service config holds. The apparent "three copies" conflated three different
purposes, not one fact restated three times.

### D4 — `skip_specs`

No capability describes the README's structure or `AGENTS.md`'s inventory; the reconciliation is documentation truth.

## Risks / Trade-offs

- **The remaining scope is small** — one README lead-in sentence. That is the honest size. Widening it to justify the
  change's origin (rewriting the inventory, adding a generator) would be the best-executed version of the wrong idea,
  which is the very rule the parked idea's own reconciliation is an instance of.
