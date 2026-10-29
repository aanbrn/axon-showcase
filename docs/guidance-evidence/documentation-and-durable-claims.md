# Guidance evidence: `### Documentation & durable claims`

Incident records for the converted rules of `AGENTS.md`'s `### Documentation & durable claims`, one section per rule. A
section's `Rule:` paragraph repeats the root rule's leading text verbatim and is the pairing key; the section title is
only an address. The agent-tooling audit owns this file: it reports a section whose `Rule:` paragraph matches no live
rule, and a rule whose `evidence:` token names this record when no section matches that rule. The file holds no guidance
of its own.

## paragraph-boundary

Rule: **Keep em-dashes paired when inserting a clause into a sentence that already uses them.**

The `audit.yml` and `buildpack-updates.yml` paragraphs in the README's CI section were fused — the word `paragraphs`
ended one source line and began the next — and the fusion rode `main` through `spotlessCheck` for several changes after
`5fc1a09` (PR #320) introduced it, until a drive-by fix separated them. No gate sees a fused paragraph: it is a prose
defect, not a parse error.

`captured: notify-owner-from-the-audit-report (#327)`

## documented-numbers

Rule: **Verify documented infrastructure/deployment numbers against the config files, not memory.**

A README rewrite claimed "the API gateway's two replicas" — only `commandService.replicaCount` is 2 in
`helm/values/axon-showcase/values-local.yaml`, and the gateway defaults to 1 — "36 panels", the raw top-level count with
row separators included rather than the real panel count, and "four services and a gateway", which double-counted a
table listing four components. A quick review against the repository's own files caught all three.

`captured: re-measure-load-test-baseline (#429)`

## partition-boundary

Rule: **A two-way split of an enumerated set can also sum to its total and still be wrong when its boundary is
hand-drawn:**

A scan of the markdown target's completeness re-listed exclusions from memory and counted the vendored `axon4to5-*` and
generated `openspec-*` skill files as gated, while the target's own `target(...)` / `targetExclude(...)` entries put
them on the excluded side (the project-authored skills stay gated) — and both splits summed to the same total, so the
arithmetic check could not catch it.

The chart mode came from the netpol templates, which comment the same release-wide `common.labels.matchLabels` selector
as both "from other pods in the release" and "selected peers"/"cluster peers".

`captured: reconcile-netpol-spec-peer-wording`

## lint-set

Rule: **`workflowLint` runs `actionlint` alone**

Two artifacts asserted "actionlint + shellcheck": the `add-actionlint-gate` proposal, which named a spec scenario, and a
later change's verification story.

The "one level down" mode came from a placement rule first drafted as "executables live in `scripts/`": it was corrected
for the repo-root wrappers, then re-broken by the module-local pair, while the sibling clauses of the same rule
enumerated 2/2 and 3/3 so its `scripts/` colon-list (5 of 8) read as exhaustive.

The count mode came from an early draft of the `make-captured-rules-traceable` design, which wrote "46 of them
capture-class, and those 65 are concentrated in Gotchas (41) and Conventions (21)" — 41 + 21 = 62, so the split could
not belong to the 46; it was the 65-bullet set the same sentence also named, and the mismatch was invisible until the
review did the addition.

## point-in-time-count

Rule: **Distinguish a point-in-time count from a durable one:**

The `add-specs-auditor-agent` proposal said the corpus held "157 requirements"; the change's own delta made the main
spec hold 158, and the README still stated a hard "22 capability specs". The `specs-auditor` definition's "a corpus …
that grows with every archived change" is the shape-describing form the rule asks for.

## claim-site-sweep

Rule: **A doc-consistency sweep is scoped by the convention or the claim, not by the review's findings list — and a
claim about the code is verified against the code.**

The sweep scopes below come from these episodes. `fix-javadoc-consistency` wrote a `@param elasticsearchConverter`
reading "OpenSearch results to entities" for a converter that maps entities → OpenSearch; the field's Javadoc and its
`mapObject(ShowcaseEntity…)` call sites said so, and the implementation quick review caught it. The same change had to
add a field Javadoc the review never enumerated (`ShowcaseProjector.METER_NAME_PREFIX`), caught by the owner.

`apply-specs-audit-findings` normalized one coordinate spelling in the `spring-data-opensearch` requirement and left the
identical `org.springdoc: springdoc-openapi-starter-webflux-ui` spelling lower in the same file, deferred as "a future
audit" when a grep would have caught it; `fix-springdoc-coordinate-spelling` followed. A
`reconcile-showcase-cache-default` review corrected a reversed direction in the design's Context, and the same claim's
residue in the Decisions section — plus a "tenfold" that was a hundredfold — was caught only in the next pass.

Replacing the commit discipline left four restatements: two gotchas, the docs-refresh convention, and `docs/ideas.md`'s
header. The diagram bullet's `diagramPadding` rule survived in `diagrammer.md` until the quick review. A widened check's
describers went stale: the 2026-10-11 audit found `toolingUpdates`' worked example stale once the smoke's `pack` pin
gained its own entry, and `bump-tooling-pins-2026-10-16` left `/opsx-tool-update`'s "the check detects a release from
`ci.yml` alone" in place.

The web UI's Playwright e2e suite was added while `AGENTS.md` still said "the only e2e suite is
`showcase-api-gateway`'s", and the stale claim survived until a later cleanup pass (PR #116). The absence mode: the
`buildpackUpdates` check in #149 falsified the "`dependencyUpdates` and `helmUpdates` ignore bare `[versions]` entries"
sentence #148 had added one change earlier. The rename mode: the first audit found `ShowcaseApiController*` references a
rename had left behind, and the workflow-file rename `snyk.yml` → `dependency-security.yml` also lived in `SECURITY.md`,
ADR-0006, and `build.gradle.kts`'s `toolingUpdates` pin. The enumeration mode: `AGENTS.md` said the `ci.yml` and
`e2e.yml` workflows "additionally extend `gradle-home-cache-includes` with `nodejs`" while the npm-monitoring change
added that include to `dependency-updates.yml` and `dependency-security.yml` too. The non-doc mode: ADR-0009's "without
Axon Server" left `helm/chart/src/main/helm/values.yaml` still calling the `db-scheduler` settings "Axon Server
scheduler settings".

`captured: bump-tooling-pins-2026-10-16`, `captured: make-the-capture-selective`,
`captured: monitor-web-ui-npm-dependencies (#395)`, `captured: verify-unique-review-rounds`

## config-default-surfaces

Rule: **A configuration-default change names every test assertion that pins the value and every doc that describes it,
on each surface it is declared.**

The `reconcile-showcase-cache-default` plan initially missed that `allPropertiesHaveDocumentedDefaults` asserts the Java
field, so the change would have failed it, and that the yml-wiring test's missing `showcaseCache` assertion was the gap
which let the drift pass.

For the image's declared deployment environment, the keep-alive mirrors its own default and the origins bake the empty
fail-closed override of a local-development default. `right-size-chart-resources` changed the chart defaults: the Target
bullet and the Result paragraph of `docs/load-tests/2026-09-26.md` both described the old ones in the present tense, the
plan enumerated only the Target bullet, and review caught the Result paragraph. The URL mode was
`fix-gateway-query-service-url`.

`captured: fix-gateway-cors-allowed-headers (#359)`, `captured: right-size-chart-resources`,
`captured: fix-gateway-query-service-url`, `captured: guard-archive-spec-sync`

## claim-strength

Rule: **Doc claims must match their source and their strength — quote verbatim or paraphrase explicitly, and reserve
"enforced" for a real gate.**

The self-learning README section described `AGENTS.md` rules in quotes; `/review-thorough` caught a reworded rule
rendered as a verbatim quote, an "enforced" that no gate backs, and an overstated process claim.

The variant mode: the README's proposal-stage draft PR is a documented variant of the branch rules. The subject mode: a
README draft closed with "… and a practice of reporting gaps in the libraries and tools _it_ depends on", which bound
the project's upstream-report practice to OpenCode — the bullet's subject — rather than to the project; naming the
subject fixed it. The qualifier mode: the `reshape-the-capture-loop` rewrite of the capture trigger lost its lead's main
verb and folded the implementation capture under the merge condition; the PR's test plan records the review catching it.
The mechanism mode: the blocking-execution mechanism's evidence point is the gateway's not-found test passing (its
`fetchById` is stubbed, so the handler maps the error directly), while the query-service's mirror (`404` → `503`) went
with its HTTP controller (`grpc-query-transport`), so a second evidence point had to be re-derived rather than cited
from memory; both the surviving evidence point and the removed one are recorded in `design.md` of
`extract-specd-rationale-from-agents-md`, which moved the rule into the specs it belongs to.

`captured: record-blocking-execution-rationale (#335)`, `captured: grpc-query-transport`

## evidence-provenance

Rule: **A durable artifact may assert only what the repository can evidence — a history that lives only in the
conversation is not repo history.**

An earlier draft of this bullet cited a `/var/folders/**` config attempt — a pattern proposed in conversation but never
written to a config file — and asserted an unobserved `setup-hosts.sh` outcome; a review pass caught both.

The provenance mode: a retrospective draft cited the `openspec archive` gotcha's parenthetical as a second
lost-spec-sync occurrence, but the incident's own capture #498 had added it. The commit mode: an early draft of the
`make-captured-rules-traceable` artifacts called `c62feda` one of "two Spotless commits" that reflowed `AGENTS.md`, but
`c62feda` created the file (164 insertions, and `AGENTS.md` is absent at its parent); a review caught it.

`captured: store-2026-10-05-retrospective`

The record-home clause: `adopt-worktree-isolation`'s `Workspace:` line was cited by four rules with no stated home, so
no later session could establish that the primary had been released and every tree read as occupied; the deep review's
first pass caught it, and the fix made the Git registry the source and the record evidence with a home.

`captured: adopt-worktree-isolation`

## external-surface

Rule: **A claim about a surface outside the repository — a registry's contents, a repository setting, a live URL — has
no config file or commit behind it, so its evidence is a query whose output you have:**

`#266`'s repeated review rounds caught several unverified external assertions — a GitHub docs URL written from memory, a
false "Docker Hub carries nothing" against five live repositories, wrong dates; the owner deleted those repositories
mid-session, invalidating the entry between its writing and its review.

The live-lookup mode: `helmUpdates`'s rows are a live `helm search repo` result; the same tick's evidence read "only
`prometheus-community-stack`" in one run and also showed `bitnami-kafka: 31.5.0 -> 32.4.3` in another, so the tick rests
on the run succeeding plus the parse pinned by unit tests, never on any row.

`captured: rework-the-helm-suppression-list`

## per-item-control

Rule: **When a script generates many claims at once, the method that derived them is not their evidence:**

The `retro-mark-captured-rules` backfill's first attempt named 8 origins whose commit contained no text of the rule at
all — including `ddaa51e`, claimed for the log-assertion rule whose text it never mentions — because the range and the
phrase were never required to be about the same rule. Adding a per-item control — the candidate origin's added lines had
to contain the rule's own opening phrase — substantiated 30+ origins independently.

`captured: retro-mark-captured-rules`

## single-variable

Rule: **A comparison between two runs or files that differ in more than one dimension cannot attribute the difference to
either — isolate the variable before naming a cause.**

Comparing `ci.yml`'s `Cache mode: write` with the `opencode` workflow's read-only run attributed the difference to the
latter's `permissions:` block, but the two differ in two ways — the trigger (`push`/`pull_request` vs the comment
triggers) and the blocks' contents (`ci.yml`: a top-level `contents: read`; `opencode.yml`: a job-level
`id-token: write` plus reads) — and a grep anchored to an indented `permissions:` had hidden `ci.yml`'s top-level one.
GitHub's dependency-caching reference names the trigger, so a fix built on the misattribution (granting
`actions: write`) would have widened the token for nothing.

The two-dimension mode: probing one malformed frontmatter on an agent and a different one on a command read the results
as kind-keyed ("command → exit 1") when the status depends on the resolved YAML shape, not the kind —
`guard-agent-loader-read-path` fixed the design only by re-deriving at a held-constant shape; a later review round then
could not reproduce the silent-drop row because it chose a different shape, and read its clean run as a refutation of a
true fact. The route mode: a `commons-lang3` probe of `main`'s `implementation` on a custom suite's compile classpath
"confirmed" the opposite, because the coordinate arrived through the suite's declared `project(":showcase-test")`.

`captured: guard-agent-loader-read-path`, `captured: fix-mapstruct-suite-warning`

## reproduction

Rule: **A reproduction in an outward-facing artifact is itself part of the claim — write it so a reader reruns it to the
same output, and rerun the exact sequence before posting.**

A `Fission-AI/OpenSpec#1892` draft showed `openspec validate --all` exiting 0, but the delta-less change its own
`new change` had created makes `validate` exit 1; in a directory with no planning shape the same malformed config
hard-errors with exit 1 instead of warning. The `#1892` draft took repeated review passes, each catching a different
mis-reproduction, because the review gate reads the artifact while no gate reruns its transcript.

## upstream-references

Rule: **An upstream issue reference is a status claim, not a citation — resolve it, and treat a closure as a trigger to
check rather than an answer.**

When the upstream-reference report was parked, a review found two of four cited references already closed while both
constraint notes still read as open: `ben-manes/gradle-versions-plugin#755` (closed 2026-08-06 by PR #1060) and
`spring-projects/spring-data-elasticsearch#3334` (closed 2026-08-30 by PR #3337). `#3334`'s entry then named a release
that later published, and the meta-rule's own "the periodic corpus sweep is parked" pointer outlived the sweep's build.
`#755` closed through a change to platform-sourced constraints (`satisfiesDeclaredBound`, "nothing changes by default"),
so the spurious row from `checkBuildEnvironmentConstraints` remains even though the fix shipped in 0.60.0.

The first `hold-back-nginx-buildpack` draft blamed an arm64 build-extraction failure on `paketo-buildpacks/nginx#1340`,
whose body documents only the runtime symptom (an AArch64 `nginx` in an amd64 image) and states that the
`io.buildpacks.buildpackage.metadata` label is present on both slices; the draft also contradicted the host-state
gotcha's account of that signature. A planned task to "comment on `micromatch/braces#70`" was unachievable because #70
is locked (comments disabled); the actionable reference was the fix PR `micromatch/braces#72`, which later closed
unmerged.

**Closure tracking for the references moved here.** `#755` stays cited in ADR-0007, `README.md`, and `## Build & Test`;
`#3334` in `docs/ideas.md` and the `projection-model` spec; `#1340` in the `deployment-operations` pack's
`## Docker images` and `## Deployment, Helm & Kubernetes` sections, where its close-out clause lives;
`micromatch/braces#72` in ADR-0014. `micromatch/braces#70` is cited only here: it is locked, its disposition is the
unmerged fix PR. The acceptance that PR was meant to fix is **closed**: the advisory was withdrawn upstream on
2026-10-23, the suppression entry was then removed (the list is empty), and ADR-0014 records both — no retirement
condition remains live for either reference.

`captured: suppress-braces-npm-advisory`, `captured: refresh-upstream-references-2026-10-05`,
`captured: record-gatling-report-block`, `captured: refresh-upstream-references-2026-10-19`
