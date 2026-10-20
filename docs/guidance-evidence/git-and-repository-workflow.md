# Guidance evidence: `### Git & repository workflow`

Incident records for the converted rules of `AGENTS.md`'s `### Git & repository workflow`, one section per rule. A
section's `Rule:` paragraph repeats the root rule's leading text verbatim and is the pairing key; the section title is
only an address. The agent-tooling audit owns this file: it reports a section whose `Rule:` paragraph matches no live
rule, and a rule whose `evidence:` token names this record when no section matches that rule. The file holds no guidance
of its own.

## occupied-path

Rule: **A write to an already-occupied path replaces the file silently: check the path before creating a "new" file.**

`ShowcaseEventStreamControllerTests.java` was assumed new when `keep-sse-stream-alive` (#301) added a unit test for the
SSE keep-alive. It dated from #43 and held two tests — event wrapping, and REMOVED-type preservation — and the
whole-file write destroyed both. Nobody noticed until the suite failed for a missing method.

`captured: keep-sse-stream-alive (#301)`

## archive-incomplete

Rule: **A change merged without its archive is incomplete — do not merge the implementation PR and defer the archive.**

The `remove-redis-client-label` change was merged on its own branch and sat unarchived until the owner pointed it out;
the archive commit had been deferred past the merge.

## staged-set-sweep

Rule: **`git add <dir>` / `git add -A` can sweep untracked generated artifacts into the commit — inspect the staged set
first.**

The `rework-idea-setup` commit swept in a `scripts/__pycache__/*.pyc` that had to be removed and ignored, which is why
the repository now ignores `__pycache__/`. Separately, a review caught the staged set missing corrections made after the
last `git add`; that commit would have shipped half the fix.
