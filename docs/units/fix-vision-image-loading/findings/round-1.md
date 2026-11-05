- [test-control] openspec/changes/fix-vision-image-loading/design.md:51-54 — the negative probe's 404 page is an HTML
  error page the browser renders, so the "no pixels" branch never fires.
- [missing-scenario] openspec/changes/fix-vision-image-loading/design.md:44-47 — the failure-reporting rule adds a
  reportable outcome the spec's scenario does not carry, so `skip_specs` was wrong.
- [claim-unreproducible] openspec/changes/fix-vision-image-loading/design.md:88 — the claims row and task 2.2 attribute
  the definition-inventory check to `./gradlew check`, which does not run it.
- [claim-unreproducible] openspec/changes/fix-vision-image-loading/design.md:87 — the probe row cites a `tasks.md`
  record that does not exist, and its figure describes a different screenshot.
- [claim-unreproducible] openspec/changes/fix-vision-image-loading/design.md:85 — the spec grep over-matches
  `revision`/`provision` and omits the third site, so the row's result misdescribes the source.
- [claim-unreproducible] openspec/changes/fix-vision-image-loading/design.md:86 — the `grep -n "vision subagent"` source
  exits 1, since `AGENTS.md` writes it with backticks.
- [incomplete-enumeration] openspec/changes/fix-vision-image-loading/design.md:90-93 — the `count` dismissal covers
  table figures only, while the scan's count candidates are task identifiers and the 404.
- [structured-writing] openspec/changes/fix-vision-image-loading/proposal.md:13-16 — one bullet chains four
  independently actionable rules.
