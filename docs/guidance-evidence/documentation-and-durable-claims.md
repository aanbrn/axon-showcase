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

## consistency-sweep

Rule: **A doc-consistency sweep is scoped by the convention or the claim, not by the review's findings list — and a
claim about the code is verified against the code.**

Each of the rule's six scopes comes from an episode. `fix-javadoc-consistency` wrote a `@param elasticsearchConverter`
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

`captured: bump-tooling-pins-2026-10-16`, `captured: make-the-capture-selective`

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
`#3334` in `docs/ideas.md` and the `projection-model` spec; `#1340` in `## Docker Images` and the deployment gotcha,
where its close-out clause lives; `micromatch/braces#72` in ADR-0014. `micromatch/braces#70` is cited only here: it is
locked, its disposition is the unmerged fix PR, and the acceptance it produced is dated in ADR-0014 and bounded by the
npm-advisory suppression's own expiry, so it carries no live retirement condition.

`captured: suppress-braces-npm-advisory`, `captured: refresh-upstream-references-2026-10-05`,
`captured: record-gatling-report-block`, `captured: refresh-upstream-references-2026-10-19`
