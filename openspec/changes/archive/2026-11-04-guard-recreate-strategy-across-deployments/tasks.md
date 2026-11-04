# Tasks — guard the `Recreate` strategy on every service Deployment

The unit's record is `docs/units/guard-recreate-strategy-across-deployments/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `scripts/claim-scan.py`; each candidate is
      resolved into the claims table below or dismissed beside it. Dismissals, one reason per kind: `absolute` — the
      flagged words are this proposal's and the design's own statements, each backed by a row; `count` — the figures are
      the table's rows; `path` — the paths are this change's artifacts and the files it edits.

## 1. One rule, one place

- [x] 1.1 `helm/chart/src/main/helm/templates/_helpers.tpl`: define `axon-showcase.deploymentStrategy` — its doc comment
      (the expected dot, and the projection template's rationale: a type-only server-side apply keeps the live
      `rollingUpdate` block and the API rejects the pair), `strategy:` with the rendered mapping at two spaces,
      `rollingUpdate: null` at the same level when the type is `Recreate`, and the `updateStrategy` truthiness guard
      inside the helper.
- [x] 1.2 The five service Deployments — `projection-service`, `command-service`, `query-service`, `api-gateway`,
      `web-ui` — replace their `strategy:` render with
      `{{- include "axon-showcase.deploymentStrategy" (dict "value" .Values.<service>.updateStrategy "context" $) | nindent 2 }}`,
      so the projection service's local guard **and its rationale comment** move into the helper and the other four gain
      both.

## 2. The renders

- [x] 2.1 `scripts/render-chart-strategies.py`: render the collected chart with every service on one strategy via
      `helm template --output-dir`, resolving the CLI from the Gradle plugin's cache at the catalog's Helm version
      (passed as `--client-version`), then `$HELM_EXECUTABLE`, then `helm` on `PATH`, then the newest cached client;
      exit 3 with a stated reason when none resolves.
- [x] 2.2 `build.gradle.kts`: register `renderChartStrategiesRecreate` and `renderChartStrategiesRollingUpdate` as
      `verification` `Exec` tasks over the script and the collected chart directory, depending on
      `:helm:chart:fetchMainChartDependencies`, so the renders run after the chart is collected and the client extracted
      (the plugin's own `HelmTemplate` does not register outside its DSL).

## 3. The check

- [x] 3.1 `scripts/verify-chart-strategies.py`: parse each Deployment's `spec.strategy` mapping (comments excluded) and
      assert — a `Recreate` render sets `type: Recreate` and `rollingUpdate: null`, a `RollingUpdate` render sets its
      type and not `rollingUpdate: null`, and a missing or strategy-less Deployment is a violation.
- [x] 3.2 `scripts/test-verify-chart-strategies.py`: both directions, a stray guard, a wrong type, an omitted
      Deployment, and a render tree with two chart roots, over fixture trees (8 tests, no Helm), including a commented
      guard and a nested wrong shape.
- [x] 3.3 `build.gradle.kts`: register `testVerifyChartStrategies` and `verifyChartStrategies` (inputs: both render
      directories; `dependsOn` both renders) and add both to `check`.

## 4. The specification

- [x] 4.1 The change's delta: a `MODIFIED` block on `helm-chart`'s "Service deployments" adding the `Recreate` scenario
      and the reason the rendered field is required.

## 5. Documentation and the parked entry

- [x] 5.1 `.opencode/skills/deployment-operations/SKILL.md`, "Chart validation": refresh the paragraph with the new
      check and the render path it reads.
- [x] 5.2 `docs/ideas.md`, the `## 2026-10-24` section: remove only the "Guard every Deployment template's `Recreate`
      strategy…" bullet; the section keeps its other six entries.

## 6. Verify

- [x] 6.1 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes, including the renders, the rules tests and
      `verifyChartStrategies`; `helm lint` stays strict and green; `openspec validate --all` passes.
- [x] 6.2 The check fails when one call site's include is replaced by the bare `strategy:` render (mutation), and passes
      when restored.
- [x] 6.3 The review gate runs over the change. Routing on the implemented diff `REQUIRED`
      (`infrastructure-or-deployment`, the chart paths); the deep review ran, its five items are applied, and the
      closing quick round is clean after the advisor's two corrections.

## Claims table

Each source is read at the unit's base revision `0d6fee83`.

| Claim                                                                           | Source or command                                                                                                                                                                                                                                                                                                     | Result                                                                                                                                                                                                                                   |
| ------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| One template guards the strategy; four render it bare                           | `git grep -l "rollingUpdate: null" 0d6fee83 -- helm/chart/src/main/helm/templates`                                                                                                                                                                                                                                    | `1` file — `projection-service/deployment.yaml` — against five templates rendering `strategy`                                                                                                                                            |
| Every service exposes `updateStrategy` with `Recreate` documented               | `git grep -c "updateStrategy.type Deployment update strategy type" 0d6fee83 -- helm/chart/src/main/helm/values.yaml`                                                                                                                                                                                                  | `5`, `web-ui` included                                                                                                                                                                                                                   |
| The projection service's default is already `Recreate`                          | `git show 0d6fee83:helm/chart/src/main/helm/values.yaml \| sed -n '679,690p'`                                                                                                                                                                                                                                         | the block reads `type: Recreate`, so the negative render must set `RollingUpdate` explicitly                                                                                                                                             |
| The plugin downloads its own Helm client                                        | `git show 0d6fee83:build-logic/src/main/kotlin/helm-conventions.gradle.kts \| sed -n '1,20p'`                                                                                                                                                                                                                         | `downloadClient { enabled = true }` with the catalog's version                                                                                                                                                                           |
| The plugin provides a `HelmTemplate` task taking values and an output directory | extract the resolved plugin's sources, then grep the tree: `unzip -q -o "$(find ~/.gradle/caches/modules-2 -name 'helm-plugin-3.3.0-sources.jar' \| head -1)" '*.kt' -d "$TMPDIR/opencode/helm-plugin-src" && grep -rn "val chart: Property<String>\|val outputDir\|--set-string" "$TMPDIR/opencode/helm-plugin-src"` | `AbstractHelmInstallationCommandTask.kt:54: val chart: Property<String> =`; `HelmTemplate.kt:95: val outputDir: DirectoryProperty =`; `command/HelmValueOptions.kt:25` explains the `--set-string`/`--set` partition of the `values` map |
| CI installs no Helm client                                                      | `git show 0d6fee83:.github/workflows/ci.yml \| grep -ci "helm"`                                                                                                                                                                                                                                                       | `0`                                                                                                                                                                                                                                      |
| Helm is an optional host tool                                                   | `git show 0d6fee83:scripts/doctor.sh \| grep -n "helm"`                                                                                                                                                                                                                                                               | the probe line marks it `optional`                                                                                                                                                                                                       |
| `check` already runs the chart packaging                                        | `git grep -n "helmPackageMainChart" 0d6fee83 -- build.gradle.kts`                                                                                                                                                                                                                                                     | `1` hit, in `check`'s `dependsOn` list                                                                                                                                                                                                   |
| The upgrade rejection is recorded                                               | `git grep -n "server-side apply" 0d6fee83 -- helm/chart/src/main/helm/templates docs/adr/0022-projection-failure-policy.md`                                                                                                                                                                                           | the projection template's comment and ADR-0022's decision 6, both naming the retained `rollingUpdate` block                                                                                                                              |
| The `2026-10-24` section holds other entries, so the heading stays              | `git show 0d6fee83:docs/ideas.md \| sed -n '/^## 2026-10-24/,/^## /p' \| grep -c '^- '`                                                                                                                                                                                                                               | `7`                                                                                                                                                                                                                                      |
