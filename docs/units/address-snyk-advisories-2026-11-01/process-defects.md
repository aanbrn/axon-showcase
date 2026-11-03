# Process defects — address-snyk-advisories-2026-11-01

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### address-snyk-advisories-2026-11-01#1 — A finding set was narrowed to one coordinate of six

- **Recorded** — 2026-11-01, by the writing session (`ses_e6bb3d98effeNURMqTdoGwAu9f`), during the implementation.
- **Stage** — implementation (the platform constraint).
- **Expected** — a fix covers the artifact set the run's finding list enumerates, not the package name a reader
  recognizes.
- **Observed** — the run lists six `com.aayushatharva.brotli4j` artifacts (the core jar and five native ones), and the
  first fix constrained only the core jar, which left every native artifact at the vulnerable version.
- **Evidence** — the first `:load-tests:dependencyInsight --configuration gatlingRuntimeClasspath --dependency brotli4j`
  run reporting `brotli4j:1.24.0` beside five `native-*:1.23.0`, against the run log's six separate finding lines.
- **Consequence** — five of the six critical findings would have survived; caught by the resolution probe before review.
  Repaired by importing the family's BOM instead of constraining one artifact.
- **Knowledge boundary** — observed the probe's output. Did not check whether Snyk would have reported the remaining
  natives as a separate remaining finding (its run lists them separately).

- **Decision** — no-change: `gradle-build` already requires fixing the artifact set the findings enumerate with the
  family's BOM.

### address-snyk-advisories-2026-11-01#2 — An environment claim rested on one probe of several surfaces

- **Recorded** — 2026-11-01, by the writing session (`ses_e6bb3d98effeNURMqTdoGwAu9f`), during the manual-review ask.
- **Stage** — review (the ask's stated limitation).
- **Expected** — a claim that a credentialed scan cannot run locally states what was checked and on which surfaces.
- **Observed** — the ask said "locally there is no `SNYK_TOKEN`", derived from one environment-variable probe, while the
  Snyk CLI held a stored credential in `~/.config/configstore/snyk.json`; the owner asked how there could be none.
- **Evidence** — the probe (`${SNYK_TOKEN:+present}` empty) beside `~/.config/configstore/snyk.json`'s
  `INTERNAL_OAUTH_TOKEN_STORAGE` with `expiry` `2026-08-28T20:04:32+07:00`, `snyk whoami`'s
  `ERROR Authentication error (SNYK-0005)`, and an authenticated `GET /v1/user/me` returning HTTP 401.
- **Consequence** — the conclusion held (the stored OAuth token expired over two months earlier, so the local scan
  cannot authenticate), but its stated reason was wrong and the owner had to challenge it. Repaired by correcting the
  limitation to say the credential exists and is expired, and by offering a local run after `snyk auth`.
- **Knowledge boundary** — observed the local CLI state today. Did not check whether the org's exhausted monthly test
  quota (the run's own "200 private tests" message) would also refuse a newly authenticated local run.

- **Decision** — no-change: the external-claim rule already requires naming what was checked and on which surfaces.

### address-snyk-advisories-2026-11-01#3 — An aggregated finding line was read as one path

- **Recorded** — 2026-11-01, by the writing session (`ses_e6bb3d98effeNURMqTdoGwAu9f`), during the verification.
- **Stage** — implementation (the fix's completeness).
- **Expected** — a fix covers every path a finding reports, including the ones its line folds into "and N other
  path(s)".
- **Observed** — the ASM findings were read as the three versions the run printed (9.8, 9.10.1, 9.11), and the elided
  paths were not expanded; the local credentialed scan then reported ASM `9.7.1` through
  `spring-boot-starter-test > json-smart > accessors-smart`, so the first fix left five findings live.
- **Evidence** — the run's finding line "introduced by `org.jacoco:org.jacoco.ant@0.8.13` > … > `org.ow2.asm:asm@9.8`
  and 3 other path(s)", against the local scan's "in `org.ow2.asm:asm@9.7.1` … introduced by
  `spring-boot-starter-test@3.5.16` > `net.minidev:json-smart@2.5.2` > `net.minidev:accessors-smart@2.5.2` >
  `org.ow2.asm:asm@9.7.1`".
- **Consequence** — the change shipped an incomplete fix to the review gate and would have failed CI; repaired by the
  platform's `asm` constraint (task 2.3), after which the local scan reports no vulnerable paths.
- **Knowledge boundary** — observed these two runs. Did not check which of the elided paths the scheduled run had
  actually folded, or whether it would have reported the `9.7.1` path separately once the others were patched.

- **Decision** — no-change: the evidence rule already requires reading a finding's full captured output, not a truncated
  line.
