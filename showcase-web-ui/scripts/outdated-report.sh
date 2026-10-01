#!/bin/bash
# Writes the web UI's raw npm update table and the npm exit code for the npmOutdated Gradle task and its workflow.
# The task filters this table against the suppression list into build/npm-outdated.txt (the report the task logs and
# the workflow reads), so this file keeps npm's own output. stdout/stderr are separated so a failed lookup is
# distinguishable from "no updates"; the script exits 0 either way, so the task never fails on available updates.
set -uo pipefail

mkdir -p build
npm outdated > build/npm-outdated-raw.txt 2> build/npm-outdated.err.txt
echo $? > build/npm-outdated.exit
