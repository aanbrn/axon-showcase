#!/bin/sh
# Reports every auditor whose retained report is missing, empty, or lacks the name/revision stamp the prompt requires,
# and exits non-zero when any is.
#
# Usage: ./scripts/audit-report-completeness.sh [directory] [revision]
#
# The directory defaults to `build/audit-reports`, the path the `audit` workflow's prompt writes the three reports to;
# the workflow passes the run's input revision, so a report that omits it — or whose first line does not name its
# auditor — fails instead of shipping an artifact that cannot be tied to the tree it audited.
#
# A report counts as present only when its first line names the auditor and, when a revision is given, the report
# carries that revision: an empty file or one without the stamp is the same failure as a missing one, because the
# retained artifact exists to answer "what did this auditor return for this revision". The exit status carries the
# verdict, and `::error::` annotations name each offending file.

set -eu

directory=${1:-build/audit-reports}
revision=${2:-}
status=0
for auditor in agents-auditor specs-auditor architecture-auditor; do
    file="${directory}/${auditor}.md"
    if [ ! -s "${file}" ]; then
        echo "::error::the run left no report for ${auditor} (${file} is missing or empty)"
        status=1
        continue
    fi
    first_line=$(head -n 1 "${file}")
    case "${first_line}" in
        *"${auditor}"*) ;;
        *)
            echo "::error::${file} does not open with a line naming ${auditor}"
            status=1
            continue
            ;;
    esac
    if [ -n "${revision}" ]; then
        case "$(cat "${file}")" in
            *"${revision}"*) ;;
            *)
                echo "::error::${file} does not carry the input revision ${revision}"
                status=1
                ;;
        esac
    fi
done
exit "${status}"
