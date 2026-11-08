# Process defects — address-kafka-client-advisories

### address-kafka-client-advisories#1 — the verification plan omitted the only suite that publishes to Kafka

- **Recorded**: 2026-11-09, by the implementing agent.
- **Stage**: proposal.
- **Expected**: the proposal's verification plan covers every path a runtime dependency change can break, and the quick
  review's "does the task cover the tests the change needs" check catches a missing one.
- **Observed**: the plan verified the change with compile, the unit/component suites, and the three services'
  integration suites. The command-service integration suites set `axon.kafka.publisher.enabled=false`, so nothing in
  that set exercised the Axon Kafka publisher. Four quick-review rounds passed the plan; the owner's authorized
  technical-advisor consultation, which read `ShowcaseProjectorIT` and `ShowcaseLiveEventStreamIT` and then the
  command-service ITs, reported the gap as a confirmed problem.
- **Evidence**: `git grep -n "axon.kafka.publisher.enabled=false" 2b4c9a00 -- '*/src/integrationTest/**'` matches four
  command-service integration suites; the gateway e2e suite was then run and passed (29 tests, 0 failures).
- **Consequence**: one extra review-and-consultation cycle before approval; the plan now runs
  `:showcase-api-gateway:e2eTest` (task 2.6), and the design states why the integration tier alone is insufficient.
- **Knowledge boundary**: observed that the quick review passed the plan across four rounds and that the advisor found
  the gap; did not determine why the reviewers accepted the verification set, and did not check whether other units'
  plans share the omission.

### address-kafka-client-advisories#2 — a live rule file was edited during the planning stage

- **Recorded**: 2026-11-09, by the implementing agent.
- **Stage**: proposal.
- **Expected**: until the proposal's approval, only the unit's own artifacts and its records are writable, so a
  `.opencode/agent/*.md` definition edit waits for implementation.
- **Observed**: a round-2 finding fitted no existing finding class, and the reviewer definition instructs the calling
  agent to add the class in the same change; the class was added to `.opencode/agent/review-quick.md` immediately, while
  the unit was still at the proposal boundary. Round 3 reported it as an `impact-omission` (the plan did not name the
  edit); the edit was then reverted out of the planning tree and recorded as task 4.7.
- **Evidence**: round 3's finding instance at `docs/units/address-kafka-client-advisories/findings/round-3.md`;
  `git diff HEAD -- .opencode/agent/review-quick.md` was empty after the revert.
- **Consequence**: one review round; the class addition is applied during implementation, and `--class-vocabulary`
  reports the ledger's `record-shape` as unresolvable until then.
- **Knowledge boundary**: observed the edit, the finding, and the revert; did not determine whether the definition's "in
  the same change" wording is intended to allow a planning-stage edit.
