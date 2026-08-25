#!/bin/bash
set -euo pipefail

DB_USER="showcase"
DB_NAME="showcase-events"

usage() {
  cat <<'EOF'
Manage the local PostgreSQL database for running the command-service without Docker.

Usage:
  ./db.sh init    Create the "showcase" role and "showcase-events" database if absent
  ./db.sh drop    Drop the "showcase-events" database and "showcase" role if present
  ./db.sh reset   Drop, then init

Targets a native PostgreSQL on localhost:5432 reachable as the current OS user.
EOF
}

role_exists() {
  psql -tAc "SELECT 1 FROM pg_roles WHERE rolname = '${DB_USER}'" | grep -q 1
}

database_exists() {
  psql -tAc "SELECT 1 FROM pg_database WHERE datname = '${DB_NAME}'" | grep -q 1
}

init() {
  if role_exists; then
    echo "Role '${DB_USER}' already exists"
  else
    echo "Creating role '${DB_USER}'"
    createuser -d "${DB_USER}"
  fi

  if database_exists; then
    echo "Database '${DB_NAME}' already exists"
  else
    echo "Creating database '${DB_NAME}'"
    createdb -E UTF-8 -l en_US.UTF-8 -T template0 -U "${DB_USER}" "${DB_NAME}"
  fi
}

drop() {
  echo "Dropping database '${DB_NAME}'"
  dropdb --if-exists "${DB_NAME}"
  echo "Dropping role '${DB_USER}'"
  dropuser --if-exists "${DB_USER}"
}

case "${1:-}" in
  init) init ;;
  drop) drop ;;
  reset) drop; init ;;
  *) usage; exit 1 ;;
esac