#!/bin/sh
# Creates a unit's sibling worktree and prepares it for IntelliJ in one operation, so creation and preparation cannot be
# separated by a unit edit. The primary checkout is resolved from this script's own repository, never from the caller's
# working directory; the tree is created against it explicitly, with `--no-track`, from a freshly fetched origin/main.
#
# Usage:
#   ./scripts/new-worktree.sh <unit>              # a new unit: branch <unit> from origin/main
#   ./scripts/new-worktree.sh --continue <branch> # a tree on an existing branch (git's DWIM creates the local branch)
#
# Readiness: when the primary carries .idea/, the tree is ready only if the setup run exits zero AND its Python stage
# reports CONFIGURED. Without .idea/ the tree is created and the report says no IDE preparation was owed. A failed
# preparation keeps the tree and exits non-zero; the script never deletes or recreates one.
#
# Exit: 0 ready (or nothing owed); 1 the tree exists but is not ready; 2 usage or git error.
# The session is not moved: run session_move into the tree before writing in it.

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

usage() {
    cat >&2 <<'EOF'
usage: new-worktree.sh <unit>
       new-worktree.sh --continue <branch>
EOF
    exit 2
}

[ "$#" -ge 1 ] || usage

mode=new
if [ "$1" = "--continue" ]; then
    mode=continue
    [ "$#" -eq 2 ] || usage
    name=$2
else
    [ "$#" -eq 1 ] || usage
    name=$1
    case "$name" in
        -*)
            echo "error: a unit name cannot start with '-'" >&2
            exit 2
            ;;
    esac
fi
[ -n "$name" ] || usage

primary=$(git -C "$SCRIPT_DIR/.." worktree list --porcelain | sed -n 's/^worktree //p' | head -n 1)
[ -n "$primary" ] || {
    echo "error: cannot resolve the primary checkout from $SCRIPT_DIR" >&2
    exit 2
}
primary=$(CDPATH= cd -- "$primary" && pwd)
parent=$(dirname -- "$primary")
slug=$(printf '%s' "$name" | tr '/' '-')
destination=$parent/axon-showcase-$slug

if [ -e "$destination" ]; then
    echo "error: $destination already exists; refusing to take over an existing tree" >&2
    exit 2
fi

echo "Fetching origin in $primary"
if ! git -C "$primary" fetch --quiet --prune origin; then
    echo "error: git fetch failed in $primary" >&2
    exit 2
fi

if [ "$mode" = new ]; then
    echo "Creating $destination on a new branch $name from origin/main"
    if ! git -C "$primary" worktree add --no-track -b "$name" "$destination" origin/main; then
        echo "error: git worktree add failed; no tree was created at $destination" >&2
        exit 2
    fi
else
    echo "Creating $destination on the existing branch $name"
    if ! git -C "$primary" worktree add "$destination" "$name"; then
        echo "error: git worktree add failed; no tree was created at $destination" >&2
        exit 2
    fi
fi

if [ -f "$primary/.python-version" ] && [ ! -f "$destination/.python-version" ]; then
    cp "$primary/.python-version" "$destination/.python-version"
    echo "Inherited .python-version ($(cat "$destination/.python-version")) from the primary checkout"
fi

echo "Tree: $destination (branch $(git -C "$destination" branch --show-current))"

if [ ! -d "$primary/.idea" ]; then
    echo "The primary checkout carries no .idea/ — no IntelliJ preparation was owed."
    echo "Run session_move into $destination before writing in it."
    exit 0
fi

log=$(mktemp "${TMPDIR:-/tmp}/new-worktree.XXXXXX")
trap 'rm -f "$log"' EXIT INT TERM

setup_status=0
if ( cd "$destination" && ./scripts/setup-idea.sh ) >"$log" 2>&1; then
    setup_status=0
else
    setup_status=$?
fi
cat "$log"

python_line=$(grep -E '^Python: [A-Z_]+' "$log" | tail -n 1 || true)
if [ -z "$python_line" ]; then
    python_line="Python: (no stage line printed)"
fi

ready=yes
if [ "$setup_status" -ne 0 ]; then
    ready=no
fi
case "$python_line" in
    "Python: CONFIGURED"*) : ;;
    *) ready=no ;;
esac

if [ "$ready" = yes ]; then
    echo "Tree ready: $destination ($(git -C "$destination" branch --show-current))"
    echo "Run session_move into $destination before writing in it."
    exit 0
fi

echo "Tree NOT ready: $destination" >&2
echo "  setup exit: $setup_status" >&2
echo "  $python_line" >&2
echo "  The tree is kept; fix the cause above and run ./scripts/setup-idea.sh in it, then continue." >&2
echo "  Never write in it until the preparation succeeds and the Python stage reports CONFIGURED." >&2
exit 1
