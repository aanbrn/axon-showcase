#!/bin/bash
# Writes the web UI's raw `npm audit --json` report and npm's exit code for the npmAudit Gradle task.
# The task parses this report against showcase-web-ui/npm-audit-ignores.json and decides the verdict, because
# `npm audit` exits non-zero on any finding, fixable or not, so its exit code cannot distinguish a suppressed finding
# from an unsuppressed one. The script exits 0 regardless, so the task — not the shell — owns the pass/fail decision.
set -uo pipefail

mkdir -p build
npm audit --json > build/npm-audit-raw.json 2> build/npm-audit.err.txt
echo $? > build/npm-audit.exit
