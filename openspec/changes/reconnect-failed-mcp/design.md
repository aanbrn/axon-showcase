# Design

## Context

- **The failure.** On 2026-10-26 the host lost DNS. At 08:00:41Z `~/.local/share/opencode/log/opencode.log` recorded two
  `mcp connect failed server=playwright status.error="Request timed out"` entries with `http.span=346145` and `345709`,
  immediately after `getaddrinfo ENOTFOUND opencode.ai` failures. A sibling worktree reconnected later; the primary
  checkout did not — `opencode mcp list` still reported `✗ playwright failed: Request timed out` roughly 2.5 hours
  later, and sessions created in that checkout after the outage had no Playwright tools (claims rows 1–3).
- **Why the attempt stalls.** The server is configured as `npx -y @playwright/mcp@latest` (`.opencode/opencode.json`),
  and `npx` resolves the selector against the npm registry on every start (claims rows 4–5). With DNS down the
  resolution cannot finish, so the connect stalls until OpenCode's client gives up (claims row 4).
- **The pin is not the fix.** An exact version was measured in an isolated cache: both `@latest` and `@0.0.83` fail with
  `ECONNREFUSED` against an unreachable registry even with the package warm, and the empty-cache control fails the same
  way (claims row 5). Pinning buys no offline start, so `.opencode/opencode.json` stays unchanged.
- **The repair already exists in the API.** `POST /api/experimental/mcp/{server}/connect` with a `location[directory]`
  deep-object query returned `204` in 1.4 s and flipped Playwright to `connected`; its 25 tools then appeared in an
  already-open session (claims row 6). The CLI does not expose that operation (`opencode mcp` offers only
  `list|add|auth|logout`), but `opencode api` is a generic OpenAPI caller that reaches it without this repository
  knowing the service's address or password (claims rows 7–8).
- **Nothing in the repository records the remedy.** `git grep -ni 'mcp'` over `openspec/specs/` returns only the
  technical advisor's permission clauses, and no guidance file names a recovery route (claims row 9).

## Goals / Non-Goals

**Goals:** after a connectivity failure, show which MCP servers are down and recover them in place — one named server,
or the whole `failed` set — without restarting OpenCode or starting a new session; make every result verified and every
attempt bounded; give the agent a trigger and give a person a documented route.

**Non-Goals:** touching a server whose status is not `failed` (a recovery is not a licence to mutate unrelated MCP
services); starting, restarting, or reconfiguring the OpenCode service; changing the MCP configuration or pinning its
package; running automatically on wake; authenticating an MCP server; launching a browser.

## Decisions

**1. The transport is the native `opencode api` client, not a direct HTTP client.** `opencode api <operationId>`
resolves the service, authenticates, and encodes `--param location[directory]=…` itself (claims row 7).

- Rejected: reading `~/.local/state/opencode/service.json` and calling the endpoint with `urllib`. It duplicates
  discovery and authentication, needs the password in memory, and would have to re-derive the location encoding — the
  advisor's own change condition was to shrink to policy and verification when the native client suffices.
- Rejected: `lsof`/PID probing for the service port. Unnecessary once the client attaches to the background service.

**2. Three forms: a report, one named server, or `--all`.** With no target the tool reports every server's status and
changes nothing, which is the shape a person needs after a wake — more than one server can fail in the same event (two
did on 2026-10-26), and the report names them for free because `mcp.list` already carries them. With a name it touches
only that server. With `--all` it repairs exactly the servers whose status is `failed`, one attempt each, and leaves
`connected` and ineligible servers untouched — the status itself selects the set, so the earlier worry about mutating
"unrelated services" is answered by the eligibility rule rather than by forcing one name per invocation. `--all` with a
name is a usage error.

**3. A verified result, one attempt, and a bounded deadline per attempt.** The tool reads `mcp.list` first: a
`connected` server is a successful no-op, a `failed` one submits exactly one `connect` and is then polled until
`connected` or its deadline (60 s by default, one monotonic budget shared by every request that attempt makes), and any
other status is reported as ineligible with no attempt, since `needs-auth` or `disabled` names a condition a reconnect
cannot repair. It never submits a second `connect` for the same server, and a `--all` run whose first server fails still
attempts the rest; the run's exit is non-zero if any of them did not confirm.

- A `204` is an acknowledgement, not success: only an observed `connected` status reports success (claims row 6, and the
  test that keeps a server failed after the acknowledgement).
- A request that outlives the deadline reports **outcome unknown** rather than failure, because stopping the client does
  not cancel the server's own attempt.

**4. Exit codes and one line per server, so a caller can branch.**

| Outcome                                                                               | Exit |
| ------------------------------------------------------------------------------------- | ---: |
| Every targeted server confirmed `connected`, or (with no target) none of them failed  |    0 |
| A target unknown, ineligible, still not connected, or unconfirmed; or a failed report |    1 |
| Usage error, transport failure, or a malformed response before any attempt            |    2 |

The transport row applies to a failure **before** an attempt is sent. Once a `connect` has been sent, any failure —
including a timeout on that call — reports an unconfirmed outcome (exit 1), because stopping the client does not cancel
the server's own attempt.

Output is one line per server — `playwright: failed -> connected (reconnected)`, `playwright: connected (no-op)`,
`playwright: failed (deadline exceeded; connection outcome unknown)`, or, from a report, `github: connected` — with
sanitized diagnostics on stderr. The tool never sees the service password, so it cannot print it.

**5. The directory defaults to the repository root, not the working directory.** `opencode api mcp.list` defaults its
location to `$HOME` and returns the servers configured there, and a subdirectory path returns an empty `data` list
(claims rows 10–11) — either default would report the wrong workspace. The tool resolves `git rev-parse --show-toplevel`
and accepts `--directory` for another checkout.

**6. Tests drive the entry point against a fixture `opencode` executable on `PATH`.** The native client is the
transport, so the tool's own subject is its arguments, parsing, deadline, exit contract, and the request sequence it
emits; a fixture that records its invocations exercises exactly that (claims row 12), and one case keeps the real client
honest by requiring the live check recorded in `tasks.md`.

- Rejected: a stdlib HTTP stub. It would test a transport the tool does not own.
- Rejected: mocking `subprocess.run` in-process. A recorded argv sequence is what the fixture observes, and it keeps the
  timeout handling in the real `subprocess` path.

**7. The command definition points at the tool rather than restating its procedure**, following `/seed-claims`: the
script owns the exit contract, the deadline, and the request sequence, and one copy of them is one place to drift from.

## Risks / Trade-offs

- **The endpoint is under `/api/experimental/`** → it can change without notice. Mitigation: a non-zero `opencode api`
  exit or a non-JSON answer before an attempt is a transport failure (exit 2), and the same after one is an unconfirmed
  outcome (exit 1); either way the tool fails rather than reporting a false success.
- **A `204` may be read as success by a future editor** → the acknowledgement test keeps a server failed after the
  `connect` and requires a non-zero exit, so an implementation that treats acknowledgement as recovery fails the suite.
- **A person may read the note as "restart is no longer needed after adding an MCP server"** → the README keeps its
  existing startup sentence and adds recovery as a separate paragraph; the two name different conditions.
- **The tool could grow into a service manager** (start the server, reconnect everything, run on wake) → the
  requirement's scenarios bound it to a report, one named server, or the `failed` set, with one attempt each and an
  observed result.
- **`--all` multiplies the wall-clock cost** → each attempt gets its own `--deadline`, so a run is bounded by one
  discovery budget plus one attempt budget per failed server; the report form shows the set before anyone asks for the
  repair.

## Claims table

| #   | Claim                                                                                                         | Source / command                                                                                                                                                                                  | Result                                                                                                                                                                                 |
| --- | ------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The connect failure is logged with a 346 s span and immediately follows a DNS failure                         | `grep -n 'message="mcp connect failed" server=playwright' ~/.local/share/opencode/log/opencode.log`                                                                                               | two entries at `2026-10-26T08:00:41.735Z` and `.737Z`, both `status.error="Request timed out"` with `http.span=346145`/`345709`, after `getaddrinfo ENOTFOUND opencode.ai` at 08:00:39 |
| 2   | The primary checkout stayed failed long after connectivity returned, with no retry                            | `opencode mcp list` (primary checkout, 10:33Z); `GET /api/mcp?location[directory]=…`                                                                                                              | `✗ playwright  failed: Request timed out` at 10:33Z, ~2.5 h after the 08:00 attempt; no `mcp connected server=playwright` entry for that directory after 08:00                         |
| 3   | Sessions created after the outage in that checkout had no Playwright tools                                    | session `ses_e884408e9ffeE8HmdsjrJA8W4N` created 10:22:49Z; its tool catalog                                                                                                                      | 111 tools over four MCP namespaces (`amplicode`, `github`, `opencode`, `steroid`); no `playwright` namespace until the manual connect                                                  |
| 4   | `npx` resolves the selector against the registry at every start                                               | `time npx -y @playwright/mcp@latest --version` (warm cache, online)                                                                                                                               | `Version 0.0.83` in 1.34 s; the offline runs in row 5 fail on the registry request itself                                                                                              |
| 5   | An exact pin does not survive an unreachable registry, warm cache or not                                      | scratch cache; `npm_config_registry=http://127.0.0.1:9 npm_config_fetch_retries=0 npm_config_fetch_timeout=2000 npx -y <spec> --version` for `@latest` and `@0.0.83`, plus an empty-cache control | warm: `@latest` rc=1, `@0.0.83` rc=1, both `ECONNREFUSED` on `/@playwright%2fmcp`; control rc=1 the same; online both rc=0                                                             |
| 6   | The connect endpoint recovers the server, and recovery reaches an open session                                | `POST /api/experimental/mcp/playwright/connect?location[directory]=/Users/afanasyev/Development/axon-showcase`; `GET /api/mcp?…` afterwards                                                       | `204` in 1.4 s; status `failed` → `connected`; the open session's catalog then carried 25 `playwright` tools                                                                           |
| 7   | The CLI exposes no connect subcommand, and `opencode api` reaches the operation with the deep-object location | `opencode mcp --help`; `opencode api mcp.list --param 'location[directory]=<path>'`                                                                                                               | subcommands `list`, `add`, `auth`, `logout`; the `api` call returns the four servers with their statuses in 0.08 s                                                                     |
| 8   | `opencode api` reports an HTTP error through its exit status                                                  | `opencode api experimental.mcp.connect --param server=does-not-exist --param 'location[directory]=<path>'; echo $?`                                                                               | exit 1, body `{"_tag":"McpServerNotFoundError",…}` then `HTTP 404 Not Found`                                                                                                           |
| 9   | At the unit's base, no requirement or guidance covers MCP recovery                                            | `git grep -ni 'mcp' 02f839d9 -- openspec/specs`; `git grep -ni 'mcp\b' 02f839d9 -- .opencode AGENTS.md README.md ':!.opencode/skills/axon4to5-*' ':!.opencode/skills/ast-grep'`                   | specs: the advisor's permission clauses only; guidance: the config entry and the setup-command/skill references, no recovery route                                                     |
| 10  | `mcp.list` without a location answers for `$HOME`, not for the checkout                                       | `cd <repo>; opencode api mcp.list`                                                                                                                                                                | `{"location":{"directory":"/Users/afanasyev"},"data":[…]}` — without `playwright`                                                                                                      |
| 11  | A subdirectory location returns no servers                                                                    | `opencode api mcp.list --param 'location[directory]=<repo>/showcase-web-ui'`                                                                                                                      | exit 0, `{"…","data":[]}`                                                                                                                                                              |
| 12  | The Python suites are `Exec` tasks over the tool and its test, registered in `check`                          | `sed -n '503,560p' build.gradle.kts`; `sed -n '664,690p' build.gradle.kts`                                                                                                                        | `testClaimScan`/`testReviewRounds`/`testReviewRouting` declare both files as inputs with `outputs.upToDateWhen { false }`, and `check` depends on each                                 |
| 13  | The repository's declared Python floor is 3.12                                                                | `grep -n '3\.12' scripts/doctor.sh .github/workflows/ci.yml`                                                                                                                                      | `doctor.sh:156` declares the probe row `python3 \| required \| tool \| yes \| python3 \| --version \| 3.12`; `ci.yml:65-69` provisions `python-version: '3.12'`                        |

## Claims-scan dismissals

`python3 scripts/claim-scan.py` seeds the table above. Every candidate it prints is either a row there or dismissed here
by its provenance rather than by an exhaustive list of files — such a list is the enumeration a later edit drops a
member of, as three review rounds showed.

- **A candidate inside a file this change authors or adds to** — the tool, its suite, the command, the change's own
  artifacts, the README paragraph, and the Gradle task registration — names the change's own mechanism: its exit codes,
  deadlines, fixture names, the paths it adds, or the alternative it rejects. A claims row would duplicate the artifact
  under review.
- **A candidate on a pre-existing line of a file the change edits** — `README.md`'s prerequisites table and
  `AGENTS.md`'s tooling enumeration — names text the change did not write: `git diff -U0` shows formatter padding plus
  the one added prerequisite value on the former, and the one added pair on the latter.
- **A candidate on the unit's own row in `docs/review-rounds.tsv`** is the review's record of this unit.
- **A count, an absolute, or a path in the change's reasoning or in `docs/ideas.md`** is mapped to the claims row that
  derives it: the 346 s span and the ~2.5 h interval to rows 1–2, the "resolves the selector at every start" wording to
  row 4, and the connect endpoint to row 6.

## Open Questions

None that change the approach. Whether OpenCode gains an automatic retry upstream is outside this unit; the upstream
report is a separate unit the owner approved.
