---
name: check-and-test-authoring
description:
  Authoring a check, test, or gate that can actually fail — such as an unreachable bad state, a lenient parser, a
  control that misses its read surface, an input set narrower than what it reads, or an assertion that cannot see the
  emitted value — each with its confirmation and incident evidence.
---

# Checks and tests that can actually fail

This is the reference that `AGENTS.md`'s check-and-test rule points to. Load it before you write or change a test, a
check, or a gate.

Record a new authoring mode here, and correct an existing mode in its own section. A rule about interpreting evidence —
a search's completeness, a glob's vacuity, a reproduction's validity, a report's "nothing to report" — belongs in
`AGENTS.md`'s `### Evidence and search`; a lesson with both halves is split between the two, so neither destination
restates the other's rule.

## The invariant

A check is evidence only once it has been shown to fail: a clean run, an empty result, or an unmoved control proves
nothing until the check hits a known positive. Each section below is one way such a check passes while verifying
nothing.

## The bad state the check rejects is unreachable

- **A check whose bad state the task it depends on already rejects can never fire — the tool's own validation is the
  gate, and the check is dead code that reads as coverage.** `helm package` rejects a chart whose declared dependency is
  missing (the strict lint too), so an assertion over the packaged chart cannot fire in the real graph; confirm a new
  check's rejected state is reachable before authoring it. captured: verify-chart-packaging

## A transcribed declaration drifts silently

- **A declaration transcribed from a recorded artifact drifts silently — verify it by re-deriving it from that artifact
  and asserting equality, and assert its completeness against the real consumer's data.** A tool's declared table
  transcribed by hand from another artifact's table carries a mistyped entry with no other symptom: the copy is asserted
  equal to the source at test time (so a typo fails the build rather than mis-normalizing a figure quoted into a
  committed document), and its coverage of the real input is asserted against that input, the agreement shape
  `scripts/test-doctor.sh` checks for the doctor's `probe_table`. A figure that no longer reproduces from the grown
  corpus is asserted over the test's own fixture, with the live corpus verified by a relative run. captured:
  normalize-the-review-round-digest

## "It parses" is not "it is one valid document"

- **A parser that returns "no exception" is not a gate that rejects the input class it claims to — know what your parser
  silently accepts.** `DashboardJsonRules` treated a successful `mapper.readTree(file)` as "valid JSON", but Jackson
  returns a `MissingNode` for an empty/blank file rather than throwing, so an empty dashboard passed the gate;
  `readTree` is lenient in a second way — it accepts trailing or multiple documents (`{"a":1}x`, `{}{}`) unless
  `FAIL_ON_TRAILING_TOKENS` is enabled, so "the file parses" is weaker than "the file is one valid document". The rule's
  initial tests (valid / truncated / two-malformed / no-files) never covered either class, so both gaps passed the suite
  and surfaced only from the live known-bad control (zeroing the file, then appending a trailing byte) — caught and
  fixed before the gate shipped, not by the tests. Before trusting a parse-as-validation gate, enumerate the input
  classes it must reject (empty, blank, trailing content, a second document) and exercise each. For a rule-based gate —
  a regex, a shell `case` glob, or a numeric comparison — find those classes with a **mutation battery** over the rule's
  branches (a character class, a quantifier, a trailing delimiter, a comparison operator) and read which fixtures flip
  verdict: a branch whose mutation flips no fixture is unexercised, so add the discriminating fixture rather than one
  fixture per review round. Enumerate the fixtures at the rule's boundaries — the longest accepted form and one
  component past it (`3.12.0`, `3.12.` for a `major[.minor]` grammar) — and name, per case, the mutation that flips it.
  A surviving branch no live input can reach is not a gap but an accepted surface (see the deliberately conservative
  check bullet) — record the equivalence and the inputs that make it unreachable, rather than closing it, since the
  fixture would test the rule's grammar, not the check's behaviour. captured: validate-dashboard-json captured:
  verify-documented-pins captured: adopt-python-3-12-minimum

## The control must perturb the surface the check actually reads

- **A control must perturb the surface the check actually reads.** `reconcile-showcase-cache-default`'s control perturbs
  the yml placeholder because `applicationYmlPlaceholdersBindDocumentedDefaults` boots `application.yml` and never binds
  the Java field — reverting the field instead would have proved nothing, since that test passes regardless. Match the
  control's injection point to the test's binding source, confirm the assertion fails (a control that runs without
  failing has not exercised the check), and prove the control's own setup actually perturbed its target — assert the
  anchor occurs exactly once, or diff the surface before and after — before reading its outcome at all. And when the
  surface the check reads enumerates alternatives — a brace pattern (`*.spec.{ts,tsx}`), an alternation, or one rule
  entry per selector — exercise each with its own case, since one arm proves only that arm:
  `enforce-web-ui-naming-conventions`'s control proved the `*.spec.ts` arm of the file rule but not `*.spec.tsx` until
  the quick review added it. A tool that constructs its own input can manufacture the anomaly it appears to detect: the
  config-rules control unquoted a `config.yaml` rule item that was already unquoted _and_ carried no `: `, and a later
  control said to unquote an `operations.*.guidance` item when every one is unquoted and colon-free (select the item on
  the surface you are exercising that carries `: ` — the shape whose unquoted parsing breaks; resolve it in
  `config.yaml`, not from recall — and edit one to carry it where the surface has none), so each edit no-opped and the
  silence was misread as a defect in the guard; a throwaway `python` `replace` left an unterminated quote whose
  whole-file `could not parse … Missing closing 'quote` warning was briefly read as a wording defect — the wording was
  faithful, though it did expose a real gap (a malformed edit yields `could not parse … ignoring it.`, which the CI
  probe's grep did not match, so an unparseable config passed the job (exit 0)), since widened to catch both. When a
  scratch script's result surprises you, print or diff the input it actually produced before drawing a conclusion from
  it — a before/after diff proves the edit _landed_, not that it was the _intended_ one. Two further modes defeat the
  "confirm the assertion fails" check: the check may **read an intermediary the edit does not touch** —
  `verifyExecutableBits` reads the git index (`git ls-files -s`), so `chmod 644 scripts/doctor.sh` left the gate green
  until the mode change was `git add`-ed, so perturb the surface the check reads, not the one a tool writes; and a
  control in a pipeline reads the **sink's** status, not the subject's (`cmd | tail; echo $?` reports `tail`), so a
  control whose exit code you did not take from the command under test proves nothing. A control harness for a workflow
  `run:` block must execute that step's own shell with the collaborator substituted — no test covers the block and here
  only actionlint reads it (no shellcheck) — and be syntax-checked itself (`bash -n`) before its exit codes are trusted.
  captured: widen-config-probe-to-guidance captured: enforce-web-ui-naming-conventions captured:
  check-toolchain-prerequisites captured: retry-the-actionlint-download

## The fixture must reach the state the assertion claims

- **A fixture that reaches the subject by a neighbouring path proves nothing about the path under test: drive the
  operation the branch belongs to, and confirm by neutering that one mechanism.** `idempotent-projection-writes`'
  integration case claimed the guard script's false branch (an older event must not regress the document) but replayed a
  _create_, and the script runs only on an update: OpenSearch rejected the duplicate create before any script executed,
  the document was unchanged for the conflict reason, and the case passed with the guard never run. Drive the branch's
  own operation (the fix replays a lower-sequence _started_ event through the update path), then delete or invert that
  one mechanism and confirm the case goes red: a fixture on a neighbouring path still passes with the mechanism removed.
  Where the tier cannot produce the state, move that half to the tier that can and state which half each covers: the
  unit recorded that a real OpenSearch container cannot be made to reject one item on demand, so the ordering half runs
  at the component tier over a stubbed response while the integration case asserts only the merged document. captured:
  idempotent-projection-writes

## A stand-in that does not hold the contract it replaces measures a different subject

- **A control whose stand-in ignores the production source's contract measures the operator's occupancy, not the
  pipeline's bound.** The projector's buffer control feeds a demand-ignoring publisher, so it measures the buffer
  operator's occupancy; that equals the pipeline's effective bound only because the requirement counts every downstream
  stage's demand — the batch window and the write prefetch — and a second, demand-respecting control feeds a source
  below the capacity and completes without overflowing. Reproduce the production source's contract (demand, ordering,
  delivery guarantees) in a steady-state control, and name every downstream stage whose demand joins the bound, since
  the configured value alone is not the bound the operator experiences. captured: projector-operability

- **A stand-in that accepts a call missing a parameter the operation's effect depends on hides the omission from every
  case using it — require that parameter in the stand-in, not only in the assertion.** `reconnect-mcp`'s fixture
  accepted `experimental.mcp.connect` without `location[directory]`, so the suite stayed green while a later edit could
  have dropped the argument that decides _which_ workspace's server is mutated (the client's own default is `$HOME`);
  the fixture now rejects a connect missing either `server` or `location[directory]`, and one case asserts both, so
  control C (the call without the location) goes red across five cases. Enumerate the parameters whose absence changes
  the effect — the target, scope, destination, or identity a call acts on — and require each; a parameter that only
  refines a message can stay unenforced. captured: reconnect-failed-mcp

## A positive control must not use an input the subject excludes

- **A positive control's input must be one the subject does not itself exclude — plant a value the check's own config
  allows or suppresses and the control "passes" for the wrong reason.** A scanner, lint rule, or baseline filter carries
  its own allowlist or suppression list, so the canonical example value you reach for may be on it: the source-secret
  scan's first known-bad control planted the AWS docs example key `AKIAIOSFODNN7EXAMPLE`, which gitleaks' default config
  allowlists, so the scan reported "no leaks found" and the control proved nothing — reproduced: that value exits 0,
  while a high-entropy generic token (`api_key = "a1b2…"`) trips a rule and exits 1. Pick a control input the subject's
  own rules do not already excuse (check the tool's default config, not a well-known sample), and read the control's
  outcome from the subject's exit status, not a piped summary. captured: scan-source-for-secrets

## A check's input set must be as wide as what it reads

- **A check whose input set is narrower than what it reads is not a check — whether the set varies by task graph or
  omits a file the tool reads.** `verifyModuleDependencies` inspected 36 edges standalone and 47 under `check`, the
  11-edge difference being exactly the project dependencies declared inside `testing { suites { … } }` blocks: a suite's
  configurations hold their dependencies only once its test tasks are realized, which `check`'s own
  `dependsOn(testing.suites…)` caused incidentally. Four fixes failed (eager suite realization, `evaluationDependsOn`,
  collecting suite edges where the `testing` accessor is in scope, reading every configuration) before the scope was
  corrected: the invariant protects what ships and a suite's edges reach no artifact, so the walk reads only the
  production source sets — `main` and `testFixtures`, declared directly in build scripts and thus eager. Have the report
  enumerate every inspected item, not just its count — the "Inspected edges" list is what made the two invocations
  diffable and named the missing 11 as a set; run a new verification standalone and through `check` and diff its report.
  The same rule covers a _declared_ set: the web module's `npmTest` loaded `eslint.config.js` at runtime while declaring
  only `src`, so a config-only regression restored the cached test result and ran nothing — and `npmBuild` already
  omitted `index.html` and the lockfile that owns the installed `node_modules`. Derive the whole set in one pass
  (configs, entry html, `package.json`, `package-lock.json`) rather than one round per surfaced file, and prove it by
  editing an input and confirming the task re-executes rather than reading its cached status as green. A check's _view_
  of its subject is part of that scope: when the spec states the predicate as a property of an artifact — an archived
  change dir that _carries_ delta specs — read the artifact's own contents, not the subset that happens to be staged,
  since a predicate that read the staged set let a partially-staged archive change (one file added) escape the guard
  entirely. A shell loop is a third mode: in zsh an unquoted stored value does not word-split (`for f in $(…)` still
  does), so `files=$(git diff --name-only); for f in $files` runs its body **once** over the joined string and the check
  reads one file; split it with `${(f)files}` or loop the substitution directly. captured:
  enforce-web-ui-import-boundaries captured: guard-archive-spec-sync captured: diagnose-rest-controller-ct-hang

## A zero-item extraction fails only where the contract requires the subject

- **A gate that extracts items from its input passes vacuously when the extraction yields none — reject the zero-item
  case in the rules and test it, but only where the owning contract requires the extracted subject to exist.** The
  condition is the contract, not the gate: `verifyInstallCommands` parses `helm install … --version` lines from the one
  guidance document the requirement names as the manual install block, so deleting that block must fail
  (`InstallCommandRules.noCommandsReason`) — it left the task green, and only the deep review found it. Its collected
  sibling had the same hole from the other side: `verifyDocumentedPins` reported no findings when no scanned document
  stated a pin, so the declared document's pins could vanish green (`DocumentedPinRules.unstated`, requiring every
  catalog pin in the document the requirement names). Where the contract only says "a doc that states a pin must state
  it correctly", an empty result is legitimate and must stay legitimate: `DocumentedPinRulesTests.noPins` is that
  allowed case, and asserting presence there would be the over-wide input-set error in the other direction. Prove both
  by removing the block or the pin the required document carries and requiring a named failure; mutating a value proves
  the parse, not the presence. The search-side form — a glob or filter that matches nothing is vacuous, not clean — is
  `AGENTS.md`'s `### Evidence and search` bullet; this is the authoring half. captured:
  extract-deployment-operations-guidance

## Assert what the subject emits, not only what it declares

- **An assertion about an intermediate — a declaration's content, or an internal helper's behaviour — cannot see what
  the consumer sees; assert what the subject _emits_ at the entry point the gate invokes, not only what it declares or
  what a helper raises.** Both abandoned fixes of `scripts/test-doctor.sh` (five and seven review rounds) read the
  doctor's probe declaration and asserted things about the data; each round found another way a probe could slip out of
  the reported set, the last being a `kind` flipped `tool`→`state` that made the probe **vanish from the doctor's output
  while the check still reported "13 probes" and "required probes match"** — the declaration was unchanged, so a content
  assertion saw nothing wrong. Run the subject and require every declared item in its output, and drive the entry point
  the gate runs (`main([...])`) asserting its exit status and message, since a fixture on an internal helper leaves the
  entry point's error handling untested — a malformed `--expect` raised an unhandled `ValueError` traceback while the
  fixture asserted the helper raised. captured: check-review-round-contiguity Keep a host-dependent exit code out of the
  assertion (capture with `set +e`, assert the output only), since a gate that depends on it fails PRs for causes they
  cannot remediate. captured: declare-probes-with-shape-and-behavioural-checks Require the whole of what it emits, not
  only the part a change targets: a consumer that classifies its input has states beyond row-present/row-absent, so a
  filter's removal test stays green while it reclassifies its neighbours — the first `DependencyUpdateRules` clause
  emptied `dependencyUpdates`' up-to-date section of all 83 catalog-owned entries, since the plugin reads a coordinate
  whose every candidate is rejected as unresolved. captured: align-dependency-report-version-authority

## A conservative check owes the false negatives it accepts

- **A deliberately conservative check owes the false-negative surface it accepts — and a gap cheap to close is closed,
  not recorded.** A check narrowed to avoid false positives under-reports by design, so what it does not catch must be
  stated where its outcome is recorded (the design, and the spec where the outcome is spec'd), and a clean run is never
  full coverage. Recording is for what cannot be closed: the cron check closes its day-of-week `7`-as-`0` miss (the
  `commit-hygiene` spec records the normalization) while recording its stdlib-only scan's unparseable YAML forms, and
  the conflict-marker check records its lone-`=======` exclusion. An exemption boundary is the same surface, and its
  **position** is part of the check: enforce it, not only the boundary's presence, since a marker moved past a row
  exempts the row it passes (`verifyReviewSessionContinuity` rejects a row above the marker whose field count is not the
  pre-fields width). captured: check-unique-cron-schedules (#436) captured: track-review-session-continuity
