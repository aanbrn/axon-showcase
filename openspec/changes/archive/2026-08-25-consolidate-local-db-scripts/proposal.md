# Proposal: Consolidate the local PostgreSQL management scripts

## Why

`db-init.sh` and `db-drop.sh` provision the native PostgreSQL database (`showcase` role, `showcase-events` DB) for
running the command-service locally via `bootRun` without Docker. Two rough edges for that debug workflow: `db-init.sh`
is not re-runnable (fails on an existing role or database, while `db-drop.sh` is `--if-exists`-safe), and the two-file
split forces a manual `drop && init` reset loop.

## What Changes

- Replace `db-init.sh` and `db-drop.sh` with a single `db.sh` exposing subcommands:
  - `db.sh init` — idempotent: creates the `showcase` role (with `CREATEDB`) and the `showcase-events` database
    (UTF-8, `en_US.UTF-8` locale, from `template0`) only when absent, using `psql` existence checks.
  - `db.sh drop` — idempotent: drops the database and role (`--if-exists`), preserving current behavior.
  - `db.sh reset` — `drop` then `init`.
  - `db.sh` with no/unknown subcommand prints usage.
- Harden the script: `set -euo pipefail`, step-by-step echo messages, and a note that it targets a native PostgreSQL on
  `localhost:5432` reachable as the current OS user.
- Delete `db-init.sh` and `db-drop.sh`; update the AGENTS.md and README references to the new subcommands.
- No change to the provisioned names (`showcase` / `showcase-events`), which still match the command-service defaults.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. A developer-tooling refactor with no externally observable behavior change.

## Impact

- **Code (tooling)**: `db.sh` (new); `db-init.sh`, `db-drop.sh` (deleted).
- **Docs**: `AGENTS.md` (DB scripts section), `README.md` (local development).
- **Build**: unchanged.
- **Tests**: none; verified with `bash -n` syntax check and the idempotency logic.