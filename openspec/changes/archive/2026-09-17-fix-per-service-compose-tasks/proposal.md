# Proposal: Fix the per-service compose tasks

## Why

The per-service `compose*` tasks fail with `no such service`. `docker-conventions` derived the compose service from the
Gradle project name, so `:showcase-api-gateway:composeUp` addressed a service called `showcase-api-gateway`, while
`docker-compose.yml` names it `api-gateway` — a name that exists in no compose file. The stack-wide tasks were
unaffected because they pass no service at all, which is why only the per-service variants were broken.

## What Changes

- Map each service module to its compose service — `showcase-api-gateway` → `api-gateway`, and likewise for the other
  three services and the web UI — instead of passing the module name, and register the per-service tasks only for
  modules that have one, so a module without a service gets no task rather than a Compose failure.
- Document the per-service form where the compose tasks are described, and remove the implemented idea from
  `docs/ideas.md`.

## Capabilities

### New Capabilities

<!-- none — `skip_specs: true`; no requirement describes the developer-facing compose tasks. -->

### Modified Capabilities

<!-- none — the specs that mention Compose (`infra-image-versions`, `deployment/web-ui`, `gateway/rest-api`)
describe the deployed system and the image-tag single-sourcing, not these Gradle tasks. -->

## Impact

- **Build**: `build-logic` — a new `ComposeServices` mapping with unit tests, and `docker-conventions` using it.
- **Developer experience**: `./gradlew :showcase-api-gateway:composeUp` (and the other per-service variants) address
  their service, bringing it up with its Compose dependency graph.
- **Docs**: `AGENTS.md` and `README.md` describe the per-service form; `docs/ideas.md` loses the implemented idea.
