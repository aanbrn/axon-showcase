# Design — add an independent technical advisor

## Context

The owner's objective is an independent technical consultation inside OpenCode: evaluate a proposal, an implementation,
a disagreement, or a next step without manually transferring context to an external advisor. `proposal.md` in this
change directory states the gap. The repository already has three bounded reviewers — `review-quick`, `review-thorough`,
and `architecture-auditor` — and none of them owns this question: the quick review screens supplied artifacts, the
thorough review judges an implementation against its plan, and the architecture audit holds recorded decisions against
the system. The advisor evaluates the decision and the direction, including whether to keep or stop, and it recommends
rather than reports findings.

## Goals / Non-Goals

**Goals**

- An on-demand `technical-advisor` subagent and an explicit `/consult` command.
- A read-only, bounded contract enforced by permissions, not only by prose.
- A focused handoff, fresh child sessions for independent consultations, and continuation for a follow-up.
- A report that starts with the recommendation, keeps uncertainty visible, and is not authorization.
- A pinned model and effort with one supported override path.
- Spec, `AGENTS.md`, README, and parked-idea coverage that stays coherent with the new agent.

**Non-Goals**

- Automatic consultation on every change, or any change to the review gate's routing.
- A replacement for `review-quick`, `review-thorough`, or `architecture-auditor`.
- Implementation, publication, or approval authority for the advisor.
- A new session launcher, provider adapter, or authentication path.
- A live `xhigh` experiment or a scenario matrix.
- README or repository content in a language other than English.

## Decisions

### D1 — A distinct on-demand advisor, not a widened reviewer

The role is a decision question, not an artifact review: "given this objective, is this the right direction, and what
should happen next?".

- Widening `review-thorough` was rejected: its subject is an implementation against its planning artifacts, its output
  is findings, and it participates in the review gate; making it recommend direction would make it review its own
  subject and lose independence.
- Widening `architecture-auditor` was rejected: its subject is recorded architecture and rationale, not an arbitrary
  decision in flight.
- An external advisor over HTTP was rejected: it reintroduces the manual context transfer this change removes and adds
  an integration the repository does not need.

### D2 — `/consult` delegates through the `subagent` tool; no `agent:` command frontmatter

The invoking agent must compose a focused handoff from its own context and retain the child session identifier, so the
command body instructs the main agent to invoke the `technical-advisor` subagent.

- `agent: technical-advisor` with `subagent: true` was rejected: the child would receive the command's static template
  as its prompt, so the parent could not supply the handoff fields or keep the identifier for a follow-up.
- A command-level `model` was rejected: a command model overrides the session's model for the command run, so the main
  agent would switch to the advisor's model instead of delegating to it.
- A custom session launcher was rejected: the installed `subagent` tool already creates a child session, accepts a
  `model` override with a `#variant`, and returns a session identifier a later call can continue.

### D3 — Read-only is a tool-policy restriction, and shell is denied because it cannot be constrained

The agent frontmatter carries an ordered ruleset: a wildcard `deny`, then allows for `read`, `glob`, `grep`, and
`skill`. The wildcard deny removes the edit/write/patch tools, shell, the MCP tools (including the GitHub server's write
tools), `webfetch`, `websearch`, `question`, `subagent`, and `execute` (Code Mode).

- The `.env` read denies are re-asserted after the broad `read` allow: the last matching rule wins, and the repository's
  global policy denies `.env` before an agent's `read *` allow would re-open it.
- Shell was removed rather than allowlisted. A prefix allowlist cannot make inspection read-only: `git diff`,
  `git show`, and `git log` accept `--output=<file>`, and `git grep -O<cmd>` runs a pager command. Any of them carries a
  write or an execution through an allowed prefix (verified statically against the installed Git).
- The advisor therefore inspects the working tree with `read`, `glob`, and `grep`, and receives diff hunks, changed
  paths, revision context, and history excerpts in the handoff.
- V1 `permission`/`tools` frontmatter was rejected: the installed V2 documents both as legacy, and the new definition
  should use the installed version's supported mechanism.
- Project-config permissions were rejected: the repository keeps `.opencode/opencode.json` loadable by the V1 consumer,
  and a V2 `permissions` array there would break that load; it would also apply beyond this agent.

Three levels, kept distinct:

- **Configured restrictions** — the wildcard deny and the allowed actions above. The `edit` deny alone would not exclude
  writes through a shell; shell is therefore denied as a tool class, and no write-capable tool remains configured. A
  saved "always allow" cannot override the wildcard deny: the installed engine checks a configured deny first and only
  then appends saved rules, so the restriction does not depend on the owner's approval history.
- **Behavioral instructions** — the definition tells the advisor not to target `.env`, not to implement or publish, and
  to rely on the handoff for history. These are instructions, not enforcement.
- **Residual limits** — the `.env` deny covers the `read` tool, while `grep` is not path-scoped by the permission model,
  so a search could still surface `.env` content. This is tool policy, not OS-level isolation.

A V1 consumer's agent schema has no `permissions` field and collects unknown keys into `options`, so the boundary does
not apply there. The advisor is therefore a V2-local agent; the CI definition probe still passes because it asserts
inventory and description, not tool policy. The agent body repeats the boundary as a second layer.

### D4 — `openai/gpt-6.1-sol`, default effort `high`, `xhigh` only on explicit request

The agent sets `model: openai/gpt-6.1-sol#high`. The installed V2 agent schema accepts a `provider/model#variant`
selection, and the model catalog lists `high` and `xhigh` among its effort values.

- A top-level `variant: high` field was rejected: the V2 agent schema has no such field (the V1 schema does), and a dual
  field is unverified.
- A provider-level reasoning default was rejected: it would apply outside the advisor.
- The override path is the installed `subagent` tool's `model` parameter, written `openai/gpt-6.1-sol#xhigh`; the
  command passes it only when the owner explicitly asks for extra-high effort in the request. No flag or argument is
  invented, and effort is never raised automatically. `/consult` and direct delegation both reach the same configured
  subagent, so both receive the default `high` from the definition; the override is supplied on the delegation in either
  path.
- An unavailable model or effort is reported with a concrete alternative for the owner; the invoking agent does not
  substitute one silently.
- V1 compatibility is a stated limitation: V1 takes a separate `variant` field and would not interpret the `#high`
  suffix, so the advisor is not a V1-consumer agent. The command states the V2 prerequisite and the definition qualifies
  its permission denials as V2; the README explains the version boundary once.

### D5 — Fresh session per decision, continuation only for the same decision

An independent consultation creates a fresh child session. A follow-up about the same decision continues it by passing
the saved `sessionID`; if the identifier is unavailable, the caller starts a fresh consultation and says so.

Isolation is described precisely rather than claimed absolutely:

- **Not shared:** the parent conversation as a whole and an undifferentiated tool-output dump. The handoff supplies
  selected relevant excerpts, source references, and prior findings with their provenance and disposition; the advisor
  reassesses them, including any conclusion a supplied finding carries, rather than adopting it.
- **Shared:** the project `AGENTS.md` and any configured instruction sources (the repository configures none), the
  available skills, the global OpenCode configuration, and the provider account. The handoff does not suppress project
  instructions; the advisor classifies the question against the subject-pack table and loads the applicable packs
  itself.

Because the advisor has no shell under the OpenCode V2 ruleset, the handoff also carries the diff hunks, changed paths,
base and head revisions, and any pre-change or history excerpts the decision needs. The advisor reads the working tree
for current content and does not claim a command's output it was not given.

The alternative — one long-lived advisor session per branch — was rejected: accumulated context erodes independence and
the session becomes a second main conversation.

### D6 — The advisor joins the shared report contract, with its recommendation as the verdict

`AGENTS.md` requires a new report-producing subagent's arrival to update the shared report contract's bearer list, so
the delta carries a `MODIFIED` block. The contract's first scenario is adapted: a findings report states whether
anything remains and its class counts, while the advisor's first line is its recommendation, followed by the counts of
its observation classes. The remaining scenarios apply unchanged: per-item budget, collapsed set-asides, bounded-pass
disclosure, and one recommendation rather than a menu.

The alternative — leaving the advisor outside the contract — was rejected: the bearer list would read as exhaustive and
the advisor's report shape would drift from the other agents'.

### D7 — Language is scoped to the advisor's report and its handoff

Owner-facing reports use the language of the owner's current request unless another language is requested; an
agent-facing report and the inter-agent handoff are English; identifiers and exact quotations are unchanged. This scopes
the rule to the consultation rather than introducing a new repository-wide language convention.

Always-English was rejected because the owner reads the report; always-request-language was rejected because inter-agent
handoffs stay English.

### D8 — The advisor never runs an experiment; an authorized experiment routes to the caller

Under the OpenCode V2 ruleset the advisor has no shell and runs no experiment. A build, test suite, benchmark, or
behavioral experiment is proposed with the decision it resolves and its expected cost; the invoking agent runs it only
after the owner authorizes it.

Allowing the advisor to run an authorized experiment was rejected: it has no shell under V2, and granting one for the
experiment would make the read-only boundary conditional and harder to verify.

### D9 — No automatic consultation and no review-gate change

The advisor is invoked only by `/consult` or an explicit direct delegation. No workflow step calls it, and the review
gate's routing is untouched.

Auto-consulting every proposal was rejected: it spends model calls on questions with no contested decision, and a
mandatory advisor would become a gate the owner did not ask for.

### D10 — Configuration lives in the definitions; README carries the human-facing how; AGENTS.md carries the trigger

The agent definition owns the role, boundaries, handoff, and report shape; the command owns the invocation procedure and
the override path; the spec owns the behavior. README gains the role, one example, the session model, the default effort
with a link to the command, the OpenAI sign-in comparison and connection instruction (D12), and the language behavior.
`AGENTS.md` gains only what an agent must know before consulting. The provider setup lives in README only; neither
`AGENTS.md` nor the definition repeats it.

Duplicating the full procedure in `AGENTS.md` was rejected: it is always loaded, and the single-sourcing rule makes a
second copy a drift surface.

### D11 — The direct-OpenAI pin is a model choice, not a claim about the local account

The owner requires exactly `openai/gpt-6.1-sol`, so the advisor is pinned outside the `opencode-go/*` line that the
other agents use. A model-pin bump therefore has one more exception to keep out of a naive sweep, and the README
sentence that claims every agent uses `opencode-go/*` is corrected.

The provider documentation establishes two OpenAI sign-in routes with different billing models:

- **ChatGPT sign-in** is subscription access, subject to the plan's model availability and usage limits. OpenCode's
  OpenAI provider page recommends ChatGPT Plus or Pro and offers browser sign-in; OpenAI's authentication page describes
  it as sign-in with ChatGPT for subscription access.
- **An API key** uses usage-based access billed through the OpenAI Platform at standard API rates. OpenAI's page states
  that API-key use is priced at standard API rates rather than included ChatGPT plan credits, so a ChatGPT subscription
  does not include API-key usage.
- Both are distinct from OpenCode Go, the OpenCode subscription for its own coding models.

The owner reports ChatGPT Plus, automatic credit replenishment enabled, and connected applications — including OpenCode
— permitted to consume credits. Those account facts are owner-reported, not independently verified. What has not been
inspected is the stored credential's method and the accounting of an individual request: whether it drew on included
plan usage or additional ChatGPT credits. `opencode auth list` shows a stored OpenAI credential but not its method; the
change reads neither credentials nor account settings.

The definition carries the same boundary in one sentence: it requires the advisor to distinguish owner-reported account
information from independently verified facts and not to infer authentication, billing, or per-request cost from the
model pin alone.

Using an `opencode-go/*` model was rejected: it does not satisfy the requirement.

### D12 — The README explains both OpenAI sign-in routes, with a conditional recommendation

The README's advisor subsection adds a two-row comparison (ChatGPT sign-in vs API key), a connection instruction that
names the route rather than a version-specific method label, and the primary-doc links. It recommends ChatGPT sign-in
first only for a contributor who already has an eligible ChatGPT plan and access to the model; it states that this can
use included plan usage and, where available and enabled, additional ChatGPT credits; it names ChatGPT's "Allow other
apps to use credits after reaching your usage limit" setting; it keeps credits distinct from separately billed API-key
usage; and it distinguishes both from OpenCode Go. It does not call OAuth universally better or unlimited, and it says a
successful call confirms access for that request but not whether it consumed included usage or credits. The owner's
personal plan and auto-replenishment settings stay out of the README.

The README also states that the advisor configuration is V2-only: V1 does not enforce its `permissions` field or
interpret the `provider/model#variant` effort selection equivalently. The setup guidance lives in README only. A general
provider guide was rejected: the repository needs only the choice for this agent, and the docs are linked.

## README plan

Under the agent table, add:

**OpenAI sign-in for `/consult`.** The technical advisor uses the `openai` provider, which is separate from the OpenCode
Go plan the other agents use. OpenCode supports two OpenAI sign-in routes; connect one with `/connect` (or
`opencode auth login openai`) and choose the ChatGPT sign-in or API-key method your installed version lists.

| Route                   | What it uses                                                                                                                                                | When to choose it                                                                      |
| ----------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| ChatGPT sign-in (OAuth) | Included ChatGPT plan usage; additional ChatGPT credits where your plan offers them and connected-app credit use is enabled, after the included usage limit | First if you already have an eligible ChatGPT plan and it includes the advisor's model |
| OpenAI API key          | Separately billed OpenAI API usage at standard API rates; a ChatGPT subscription does not include API-key usage                                             | If you do not have an eligible ChatGPT plan, or it does not include the model          |

Additional ChatGPT credits are separate from OpenAI API-key billing. If your plan offers credits, ChatGPT's "Allow other
apps to use credits after reaching your usage limit" setting controls whether connected applications may use them;
enabling it lets a request draw on credits rather than only included usage.

The method labels are version-specific. After connecting, `/models` lists the catalog, but a model's presence does not
by itself prove your plan includes it; a successful call confirms access for that request, but does not establish
whether it consumed included usage or additional credits. See
[Sign in with ChatGPT](https://learn.chatgpt.com/docs/sign-in-with-chatgpt),
[OpenCode providers — OpenAI](https://opencode.ai/docs/providers/#openai),
[OpenCode v2 provider connections](https://opencode.ai/v2/docs/cli/providers), and
[OpenAI authentication](https://learn.chatgpt.com/docs/auth).

**Design note, not README text:** the V1 provider page labels the OAuth method "ChatGPT Plus/Pro"; the V2 page shows a
different example. The instruction therefore names the route, and implementation checks the installed version's labels
before writing. Do not paste this note into README.

## Risks / Trade-offs

- **The reported account details are not independently verified.** The owner reports ChatGPT Plus, automatic credit
  replenishment, and connected-app credit use; the stored credential's method and per-request accounting are not
  inspected. Mitigation: the design labels the account facts owner-reported, the README stays general and conditional,
  and no credential or account setting is read.
- **The advisor becomes a de-facto gate.** A persuasive recommendation can be read as approval. Mitigation: the
  command's presentation rule, the report's explicit "recommendation is not authorization" framing, and no routing
  change.
- **A V1 cloud or ACP session treats the definition as unconstrained.** Mitigation: the advisor is documented as a
  V2-local agent, its observability is on demand, and the body restates the boundary; a future V1-compatible policy is a
  separate decision.
- **Shell is denied, so history and diff context depend on the handoff.** Mitigation: the handoff contract names the
  diff hunks, revision context, and history excerpts, and the advisor reads the working tree for current content.
- **Model retirement.** `openai/gpt-6.1-sol` may be retired; the pin then fails at invocation. Mitigation: the model-pin
  bullet's resolve-first rule applies, and this change records the pin as a deliberate exception.
- **A stale session identifier silently continues the wrong decision.** Mitigation: the command keys continuation on the
  same decision and starts fresh when the identifier is unavailable.
- **The smoke questions are self-referential.** Mitigation: each asks for a repository read whose answer is
  independently checkable, and the evidence is mapped to the requirements it covers.

## Claims table

Each row is a fact this change states about its evidence, with the command that derives it. Re-derive after the unit's
last edit to its own artifacts.

| Claim                                                                                                                      | Source / command                                                                                                                                                                      | What it establishes, and its limit                                                                                                                                                                                                                                                                                                                                                                 |
| -------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `openai/gpt-6.1-sol` is in the installed model catalog                                                                     | `opencode models \| grep -x 'openai/gpt-6.1-sol'`                                                                                                                                     | The id resolves in the installed V2; it does not prove a provider request succeeds.                                                                                                                                                                                                                                                                                                                |
| The model's supported effort values are `low`, `medium`, `high`, `xhigh`, `max`                                            | `python3 -c "import json,os;d=json.load(open(os.path.expanduser('~/.cache/opencode/models.json')));print(d['openai']['models']['gpt-6.1-sol']['reasoning_options'])"`                 | `high` and `xhigh` are valid variant ids; the provider request body is not inspected.                                                                                                                                                                                                                                                                                                              |
| An OpenAI credential is stored                                                                                             | `opencode auth list`                                                                                                                                                                  | The provider is connected with a stored credential (`OpenAI  OpenAI  stored`); the ChatGPT-OAuth flow specifically is owner-reported.                                                                                                                                                                                                                                                              |
| A scratch project resolves the draft agent, its `#high` selection, and its permissions                                     | a scratch project (`git init`, `.opencode/opencode.json`, and the draft at `.opencode/agent/technical-advisor.md`) then `opencode debug agents`                                       | The entry loads with `{providerID: openai, id: gpt-6.1-sol, variant: high}` and 15 merged rules: six base and global rules, then the wildcard deny, then the read (with `.env` denies), glob, grep, and skill allows, and a built-in trailing `browser` deny. The global shell allow precedes the wildcard deny and no shell allow follows it, so shell is denied; a provider request is not made. |
| A prefix shell allowlist cannot make git inspection read-only                                                              | `git diff --output=/dev/null`, `git show --output=/dev/null`, and `git log --output=/dev/null` (all exit 0 on Git 2.54.0), and `git help grep` (documents `-O/--open-files-in-pager`) | The options accept a file or command argument, so an allowed prefix can write or execute; shell is denied rather than allowlisted.                                                                                                                                                                                                                                                                 |
| A saved approval cannot override a configured deny                                                                         | opencode v2.1.2 `packages/core/src/permission.ts`: `if (denied(input, rules)) return { effect: "deny" }` precedes `[...rules, ...savedRules()]`                                       | A configured deny short-circuits before saved rules are appended, so a saved allow cannot re-open shell; the source is the evidence.                                                                                                                                                                                                                                                               |
| OpenCode offers a ChatGPT sign-in and an API-key OpenAI method, with version-specific labels                               | `https://opencode.ai/docs/providers/#openai` and `https://opencode.ai/v2/docs/cli/providers`                                                                                          | The V1 page names "ChatGPT Plus/Pro" and "Manually enter API Key"; the V2 page lists provider-specific method IDs and shows a different example label, so the README names the route, not a label.                                                                                                                                                                                                 |
| OpenAI documents subscription access and separately billed API-key usage                                                   | `https://learn.chatgpt.com/docs/auth`                                                                                                                                                 | ChatGPT sign-in is subscription access; API-key use is billed at standard API rates rather than included ChatGPT plan credits, so a subscription does not include API-key usage.                                                                                                                                                                                                                   |
| The owner's account details are owner-reported; the credential method and per-request accounting are not inspected         | owner-provided account (2026-10-21) and `opencode auth list`                                                                                                                          | The owner reports ChatGPT Plus, automatic credit replenishment enabled, and connected applications (OpenCode included) permitted to consume credits; the stored credential's method and whether a request used included usage or credits are not inspected.                                                                                                                                        |
| The sign-in page documents included plan usage, app limits, and the credits setting                                        | `https://learn.chatgpt.com/docs/sign-in-with-chatgpt`                                                                                                                                 | It covers eligible Plus/Pro plan usage, the "Use your ChatGPT plan" permission, app limits, and "Allow other apps to use credits after reaching your usage limit"; the owner's enablement, auto-replenishment, and per-request accounting are not inspected.                                                                                                                                       |
| The V2 agent schema accepts `permissions` and a `provider/model#variant` selection                                         | `gh api repos/anomalyco/opencode/contents/packages/schema/src/config/agent.ts?ref=v2.1.2 --jq .content \| base64 -d` and `.../config/model.ts`                                        | The frontmatter fields and the variant syntax are supported by the installed version's schema.                                                                                                                                                                                                                                                                                                     |
| The installed `subagent` tool exposes `sessionID` and a `model` override that accepts `provider/model#variant`             | the installed session's `subagent` tool definition                                                                                                                                    | Continuation and the `xhigh` path use the tool's own parameters; the provider request body is not inspected.                                                                                                                                                                                                                                                                                       |
| OpenCode adds project `AGENTS.md`, configured instructions, and skill guidance to a session                                | `https://opencode.ai/v2/docs/instructions` and `https://opencode.ai/v2/docs/agents`                                                                                                   | Project instructions reach a child session automatically; no global `AGENTS.md` exists on this host and the repo configures no `instructions` entries, so the project file is the only ambient instruction source.                                                                                                                                                                                 |
| The V1 agent schema has no `permissions` field and collects unknown keys into `options`; it has a separate `variant` field | `gh api repos/anomalyco/opencode/contents/packages/core/src/v1/config/agent.ts?ref=v1.19.3 --jq .content \| base64 -d`                                                                | The V1 boundary gap and the `#high` limitation are derived from V1's own schema, not assumed.                                                                                                                                                                                                                                                                                                      |
| The CI definition probe asserts inventory and description, not tool policy                                                 | `sed -n '106,200p' .github/workflows/ci.yml`                                                                                                                                          | A V1-unenforced `permissions` key does not fail the probe; the definition still has to resolve with its description.                                                                                                                                                                                                                                                                               |
| README currently says the repo's agents are pinned to `opencode-go/*`                                                      | `git show HEAD:README.md \| grep -n "pins its agents to"`                                                                                                                             | The sentence this change must correct (pre-change state).                                                                                                                                                                                                                                                                                                                                          |
| The AGENTS.md model-pin bullet names only the vision pin as the deliberate exception                                       | `git show HEAD:AGENTS.md \| grep -n "exclude the vision agent's pin"`                                                                                                                 | The bullet needs the advisor's pin added to its exceptions.                                                                                                                                                                                                                                                                                                                                        |
| The shared report contract's bearer list names the review/lesson-capture agents and the four auditors                      | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md \| sed -n '1311,1352p'`                                                                                           | The list the `MODIFIED` delta extends (pre-change state).                                                                                                                                                                                                                                                                                                                                          |
| No parked idea covers this advisor                                                                                         | `grep -in 'technical-advisor' docs/ideas.md`                                                                                                                                          | No match: there is no parked design to adopt. `advisor`/`consult` hits are the word `advisory` in unrelated notes.                                                                                                                                                                                                                                                                                 |
| No existing command selects an agent through frontmatter                                                                   | `grep -rn '^agent:' .opencode/commands \|\| true`                                                                                                                                     | No precedent for `agent:` in this repository; the prose-delegation shape is the established one.                                                                                                                                                                                                                                                                                                   |
| The parked vendored-skill note claims every project-authored pin is `opencode-go/deepseek-*`                               | `git show HEAD:docs/ideas.md \| grep -n "every project-authored model pin"`                                                                                                           | The note this change makes wrong again; it gains the advisor as an exception.                                                                                                                                                                                                                                                                                                                      |
| The README diagrams do not depict the agent inventory                                                                      | `grep -n 'mermaid' README.md` and reading the three blocks                                                                                                                            | No diagram becomes inaccurate, so none is edited.                                                                                                                                                                                                                                                                                                                                                  |
| The baseline validation passes                                                                                             | `openspec validate --all`                                                                                                                                                             | The corpus is valid before the delta: warnings only (requirement length), no failure. The warning count is not stated because it moves with the corpus.                                                                                                                                                                                                                                            |

## Scan candidates

`scripts/claim-scan.py --base origin/main` seeds the table above. Its remaining candidates are dismissed by kind, not
enumerated line by line:

- **`[absolute]` — normative quantifiers.** `only`, `never`, `every`, `always`, and `none` in the proposal, spec, and
  definitions state an obligation or a scope, not an evidential fact; the text that carries the obligation is the check.
  The two factual families among them — the absence of a parked idea and the absence of `agent:` frontmatter — are rows
  above.
- **`[path]` — locators.** File paths, commands, model ids, and URLs inside the procedures and the table's source column
  are instructions or citations, not claims about a running system; the fact each supports is a row above.
- **`[cross-ref]` — identifier groups.** `model/effort/access`, `edit/write/patch`, and similar tokens are
  cross-references, not assertions.
- **`[count]` — record fields, task labels, and sourced figures.** The numbers are task labels (`1.1`, `4.2`), the round
  and compliance-miss fields in `docs/review-rounds.tsv`, and figures or exit codes inside a claims-table row that names
  its source (the merged-rule count, the Git option probes). A count stated as a claim is already a row.

## Unsupported requirements and owner decisions

**Unsupported, recorded rather than worked around**

- A V1 consumer does not enforce the V2 `permissions` ruleset; read-only holds only in the installed V2. The advisor is
  therefore documented as a V2-local, on-demand agent.
- A live `xhigh` consultation is not planned; the variant id is established statically, but the provider's request body
  is not inspected.
- The owner's account facts — ChatGPT Plus, automatic credit replenishment enabled, connected applications including
  OpenCode permitted to consume credits — are owner-reported; the stored credential's method and an individual request's
  accounting are not independently inspected. `opencode auth list` shows a stored credential but not its method; the
  change reads neither credentials nor account settings.
- The official sign-in page documents eligible plan usage, the "Use your ChatGPT plan" permission, app limits, and the
  "Allow other apps to use credits after reaching your usage limit" setting. The owner's enablement of that setting, the
  auto-replenishment state, and the per-request accounting remain owner-reported or uninspected.
- Shell is denied for this initial version because a prefix allowlist admits `--output` and pager-command options; the
  advisor receives diff and history excerpts in the handoff.
- The `.env` deny covers the `read` tool; `grep` is not path-scoped by the permission model, a residual channel.
- The advisor has no shell under the OpenCode V2 ruleset, so it cannot run an authorized experiment; the experiment
  routes to the invoking agent (D8).

**Owner decisions requested at the manual review**

1. Approve the two smoke questions below, or replace them.
2. Approve the planning artifacts for implementation.

No approval is requested for the reported account settings; they stay out of the general README.

## Verification plan

Static inspection first:

- `opencode debug agents` shows `technical-advisor` with `openai/gpt-6.1-sol`, variant `high`, and the `permissions`
  ruleset (model selection, permissions, effort configuration).
- The V1 consumer's definition probe runs in CI on a pull request and asserts that every on-disk agent and command
  resolves with its description; locally, `opencode debug agents` is the agent check, and the command's discovery rests
  on that probe plus the `.opencode/commands/*.md` scan.

Two short smoke invocations, each mapped to the requirements it evidences:

1. **Owner-facing Russian consultation, with one harmless repository read.** Question: "Стоит ли сохранять отдельную
   команду `/consult`, или достаточно прямой делегации к техническому советнику?" It must read the command definition,
   answer in Russian with the recommendation first, and edit nothing. Evidences: the owner-facing language rule, the
   report shape, and the focused handoff. It does **not** evidence the configured model or effort (the static
   `opencode debug agents` inspection does) and it does **not** prove the permission engine denies a call (the static
   ruleset is the evidence for configuration; enforcement is the platform's documented last-match-wins mechanism, not a
   smoke observation).
2. **English agent-facing follow-up about the same decision, continuing the session.** Evidences: session continuation,
   the English agent-facing rule, and the report boundary. Reuses invocation 1's evidence for the report shape and the
   handoff; model, effort, and the configured policy remain the static inspection's evidence.

The smoke invokes the advisor with the command's handoff. The owner separately reports the TUI `/consult` expansion
works after a restart; the command's discovery is covered by the CI probe. The advisor behavior is the smoke's evidence,
and the command expansion is owner-reported.

If a smoke invocation fails, diagnose from its output before proposing another call; no retry campaign, no additional
`xhigh` run, no repeated language trials. A denial control (attempting an edit) is not part of the plan: the definition
instructs the advisor not to edit, so an attempt inside an existing smoke may never issue the tool call the permission
engine would deny. The static ruleset establishes the configured policy, and the report records call-time enforcement as
unexercised.

## Review records

The pre-write derivation pass is recorded by this claims table. Review rounds are appended to `docs/review-rounds.tsv`
with the reviewer's session identifier, transition, and (for a replacement) reason; the change's review task records the
deep-review routing outcome after implementation.
