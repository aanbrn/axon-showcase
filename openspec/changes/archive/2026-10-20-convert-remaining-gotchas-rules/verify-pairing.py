"""Pairing check between AGENTS.md rules and docs/guidance-evidence records (the auditor's four steps)."""
import re
import sys
from pathlib import Path

root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
repo = root

def norm(s):
    return re.sub(r"\s+", " ", s).strip()

agents_path = repo / "AGENTS.md"
record_dir = repo / "docs" / "guidance-evidence"

lines = agents_path.read_text(encoding="utf-8").split("\n")
rules = []  # (subsection, lead, body, token)
subsection = None
starts = [i for i, l in enumerate(lines) if l.startswith("- ")]
for idx, st in enumerate(starts):
    end = starts[idx + 1] if idx + 1 < len(starts) else len(lines)
    body = "\n".join(lines[st:end])
    lead = re.match(r"- \*\*(.+?)\*\*", body, flags=re.S)
    if not lead:
        continue
    token = re.search(r"(?m)^  evidence: ([\w-]+)$", body)
    rules.append((lead.group(1), body, token.group(1) if token else None))

records = {}
for path in sorted(record_dir.glob("*.md")):
    text = path.read_text(encoding="utf-8")
    for part in re.split(r"(?m)^(?=## )", text)[1:]:
        slug = re.match(r"## ([\w-]+)", part).group(1)
        key = re.search(r"Rule: \*\*(.+?)\*\*", part, flags=re.S)
        records[(path.stem, slug)] = norm(key.group(1)) if key else None

findings = []
# step 1: every token names a record file
for lead, body, token in rules:
    if token and not (record_dir / (token + ".md")).is_file():
        findings.append(("token-names-missing-record", norm(lead), token))

# step 2: a token-bearing rule has a matching section in its named record
for lead, body, token in rules:
    if not token:
        continue
    matching = [k for k, key in records.items() if k[0] == token and key == norm(lead)]
    if not matching:
        findings.append(("token-with-no-section", norm(lead), token))

# step 3: every section matches exactly one live rule lead
live = [norm(lead) for lead, _, _ in rules]
for (stem, slug), key in records.items():
    hits = sum(1 for l in live if l == key)
    if hits != 1:
        findings.append(("section-with-no-single-rule", stem + "#" + slug, hits))

# step 4: a section matching a live rule that carries no token
for (stem, slug), key in records.items():
    owners = [(lead, token) for lead, _, token in rules if norm(lead) == key]
    if owners and owners[0][1] is None:
        findings.append(("unpaired-entry", stem + "#" + slug, "rule has no token"))

print("rules:", len(rules), "with token:", sum(1 for _, _, t in rules if t), "sections:", len(records))
if findings:
    for kind, where, detail in findings:
        print("FINDING", kind, "|", where, "|", detail)
    sys.exit(1)
print("no pairing findings")
