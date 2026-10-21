# Proposal

## Why

`AGENTS.md` is 293,123 bytes across 3,036 lines and loads into every session and every subagent. About 34 KB of it — the
Docker images, Helm deployment, local-stack, and environment/port guidance — is needed only when a task touches those
subjects, so every unrelated task pays for it. The 2026-10-19 audit measured these sections as safe to load on demand
but reported no verified destination; `separate-agents-md-core-and-subject-packs` has since established the subject-pack
mechanism, so the move is now a granted permission rather than a policy question.

## What Changes

- Create `.opencode/skills/deployment-operations/SKILL.md` as the on-demand destination for deployment, image-build,
  local-stack, and environment/port guidance, carrying a `Source:` line that names the replaced `AGENTS.md` headings and
  frontmatter in the three existing packs' shape.
- Move the Kafka 3.9.0 Testcontainers note into `.opencode/skills/jvm-testing/SKILL.md`, where the override's authors
  already load guidance, and leave a one-line pointer in the deployment pack for the image-tag bump.
- Remove the moved spans from `AGENTS.md` — `## Docker Images`, `## Kubernetes Deployment`, `## Local Development`,
  `## Key Environment Variables`, `### Deployment, Helm & Kubernetes`, and, in `## Build & Test`, the Helm update-check
  bullet and the DB-scripts block — leaving one pointer per vacated section.
- Add a fourth row to `## Subject guidance packs` whose trigger fires before choosing, changing, reviewing, executing,
  or diagnosing deployment, image/buildpack, local-stack, and environment/port wiring, not only before running a
  deployment.
- Name the new pack in every site that enumerates the pack set: the capture destination list in `AGENTS.md`, both
  enumerating clauses in `.opencode/agent/lesson-capture.md`, the `## Conventions` pack-summary paragraph, and
  `openspec/config.yaml`'s context block.
- Retarget `verifyInstallCommands` to read the pack's manual install block, and extend `verifyDocumentedPins`'s document
  set to include the pack; move each gate's scanned-document set into a rules object with a committed control, so the
  wiring is testable rather than asserted.
- State in the `README design intent` convention that the root carries the verification command and the classification,
  while each subject pack carries its subject's reference material — the environment/port reference now lives in the
  pack.
- Record the destination in the two requirements that name where this text lives, with `MODIFIED` deltas: the
  `infra-image-versions` clause naming the manual install commands and the Paketo pins, and the `agent-skills` capture
  scenario enumerating the subject packs. Both keep their existing scenario names.
- Verify the move in a scratch clone before the real root is touched: contract preservation, a lost-term sweep, gate
  positive controls, and a bounded retrieval matrix.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Lessons are captured after implementation" scenario enumerates the three subject
  packs as capture destinations; the enumeration gains `deployment-operations`, with the requirement's other scenarios
  unchanged.
- `showcase/quality/infra-image-versions`: the install-command clause names the `deployment-operations` pack's block
  instead of the `AGENTS.md` section it moves out of, and the Paketo-pin clause gains the pack as a documented site
  while the gate keeps scanning `AGENTS.md` and `README.md`, so its contract matches where the text lives without losing
  the coverage the root's pins had.

## Impact

- `AGENTS.md` loses the measured moved spans (34,568 B) and gains one classification row, one pointer per vacated
  section, and the widened capture/enumeration clauses — a planned net root reduction of roughly 33,000 B (~11%).
- One new maintained destination under `.opencode/skills/` at roughly the size of the moved text, plus the 401-byte
  Kafka note in the existing `jvm-testing` pack — total maintained content grows by the new pack's header, `Source:`
  line and the one pointer. A deployment task loads root plus pack and ends slightly above today's baseline, while every
  other task class saves the moved span.
- `build-logic` gains a scanned-document rules object for each of the two gates, with a committed test that fails on the
  old document set; `build.gradle.kts`'s two task registrations point at the pack.
- `.opencode/agent/lesson-capture.md`, `openspec/config.yaml`, and the README/auditor describers are swept where they
  enumerate the packs or the deployment surfaces.
- No application code, API, dependency, chart, image, or runtime behavior changes; `check` keeps its members and its
  outcome.
