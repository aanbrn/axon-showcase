## ADDED Requirements

### Requirement: A failed MCP server can be recovered on demand

The repository SHALL provide an on-demand tool, reachable as `/reconnect-mcp`, that reads the workspace's MCP server
statuses and either reports them or repairs the `failed` ones: with no target it reports every status and changes
nothing; with a name it affects only that server; with `--all` it repairs every `failed` server. Each repair SHALL
submit at most one connect attempt, SHALL treat an already-connected server as a no-op, and SHALL report success only
after observing `connected`.

#### Scenario: A report changes nothing

- **WHEN** the tool runs with no target
- **THEN** it prints one line per configured server in name order as `name: status`, with the client's own error in
  parentheses when it reports one, submits no connect attempt, and exits zero only when none of them is `failed`

#### Scenario: A failed server is reconnected

- **WHEN** the named server's status is `failed` and the connect attempt is followed by an observed `connected` status
- **THEN** the tool reports the transition in one line and exits zero

#### Scenario: An already connected server is left alone

- **WHEN** the named server's status is already `connected`
- **THEN** the tool reports a no-op, submits no connect attempt, and exits zero

#### Scenario: An unknown server is not mutated

- **WHEN** the given name matches no server configured for the workspace
- **THEN** the tool exits non-zero and submits no connect attempt

#### Scenario: A status that reconnection cannot repair is reported

- **WHEN** the named server's status is neither `connected` nor `failed`
- **THEN** the tool submits no connect attempt, names that status, and exits non-zero

#### Scenario: Only the named server is affected

- **WHEN** another server in the same workspace is also `failed`
- **THEN** an invocation naming one server leaves the other server's status unchanged

#### Scenario: A usage error submits nothing

- **WHEN** `--all` is combined with a server name
- **THEN** the tool exits non-zero without reading or changing any server

#### Scenario: Every failed server is repaired in turn

- **WHEN** `--all` names no server and more than one server is `failed`
- **THEN** the tool attempts each of them once, reports one line per server, and leaves every `connected` server as it
  was

### Requirement: A recovery attempt is bounded and reports only what it confirmed

The recovery tool SHALL bound each recovery attempt by one deadline shared by every request that attempt makes, SHALL
exit zero only when every targeted server is confirmed `connected`, SHALL exit non-zero for a server it cannot find,
cannot address, or cannot confirm, and SHALL keep the service's credentials and raw response bodies out of its output.

#### Scenario: An unconfirmed connection is not reported as success

- **WHEN** a server does not reach `connected` within its deadline after one connect attempt
- **THEN** the tool exits non-zero, names the outcome as unknown, and submits no second attempt for that server

#### Scenario: One unconfirmed server does not stop the others

- **WHEN** `--all` targets several failed servers and one of them does not reach `connected`
- **THEN** the tool still attempts the others and exits non-zero for the run

#### Scenario: A transport failure is reported without credentials

- **WHEN** the OpenCode client cannot be reached or answers with an error
- **THEN** the tool exits non-zero and its output carries no service credential and no raw response body
