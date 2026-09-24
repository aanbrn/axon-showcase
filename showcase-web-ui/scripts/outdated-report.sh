#!/bin/bash
# Writes the web UI's npm update report and the npm exit code for the npmOutdated Gradle task and its workflow.
# stdout/stderr are separated so a failed lookup is distinguishable from "no updates"; the script exits 0 either way,
# so the task never fails on available updates.
set -uo pipefail

mkdir -p build
npm outdated > build/npm-outdated.txt 2> build/npm-outdated.err.txt
echo $? > build/npm-outdated.exit
