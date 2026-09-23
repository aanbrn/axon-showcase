#!/usr/bin/env bash
set -euo pipefail

# Point core.hooksPath at the tracked hooks directory so the pre-commit guard runs on every commit. Idempotent — safe to
# re-run; a clone that has already set it is unchanged.

git config core.hooksPath scripts/git-hooks
echo "core.hooksPath set to scripts/git-hooks"
