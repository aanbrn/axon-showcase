## 1. Add the consolidated script

- [x] 1.1 Create `db.sh` with `init`, `drop`, and `reset` subcommands — idempotent `init` (psql existence checks for
      the role and database), `--if-exists` `drop`, and `reset` = drop then init — with `set -euo pipefail` and usage
      text, and verify `bash -n db.sh` passes
- [x] 1.2 Delete `db-init.sh` and `db-drop.sh`, and verify no remaining references to them outside the change

## 2. Refresh the docs

- [x] 2.1 Update the AGENTS.md DB-scripts section and README local-development references to `./db.sh init` / `./db.sh
      drop` / `./db.sh reset`

## 3. Verify the change

- [x] 3.1 Verify `bash -n` on `db.sh`, confirm the idempotency logic and subcommand dispatch, and run `openspec
      validate` on the change