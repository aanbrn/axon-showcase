# Design: Fix the per-service compose tasks

## Context

`docker-conventions` is applied at the root and — through `spring-boot-conventions` — by exactly the four service
modules, so the broken per-service tasks exist only there. The web UI module ships a compose service (`web-ui`) but does
not apply the plugin, so it has no per-service tasks at all; that gap is noted here and left alone, because this change
fixes a failure rather than adding capability.

## Decisions

**An explicit module-to-service map, in `build-logic`, as a pure function with unit tests.** Compose cannot express the
mapping — a service is named, not derived — so it has to be written down somewhere, and `docker-compose.yml` is the
source of truth it duplicates. Putting it beside the rules object that the module-graph check uses keeps it testable
without a Gradle runtime, and the test pins each module's service, so a module or service rename that breaks the pairing
fails there instead of at a developer's first `composeUp`.

**Register the per-service tasks only where a service exists.** A module with no service now gets no task rather than a
task that fails, which is what the issue asks for. Nothing changes for the root's stack-wide tasks: they pass no service
and keep addressing the whole project.

**Leave the web UI's missing per-service tasks alone.** They are absent rather than broken, and adding them is new
capability with its own verification.
