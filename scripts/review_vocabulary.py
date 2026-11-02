"""The reviewer's canonical class list: its owner, its anchors, and the parser its consumers share.

The list itself is owned by `.opencode/agent/review-quick.md`. Two consumers read it: the build gate
(`scripts/commit-hygiene.py --class-vocabulary`), which fails a ledger row carrying a class the list does not name, and
the retrospective digest (`scripts/review-rounds.py`), which reports such a label as unmapped. Both read it through this
module rather than carrying a copy, so the anchor and the label grammar have one owner.

`canonical_classes` returns the classes and, when the list cannot be read, a reason naming what is wrong; a caller turns
that reason into its own failure (an exception in the gate, a stated limitation in the digest). Stdlib only.
"""

import re
from typing import Optional

REVIEW_QUICK_DEFINITION = ".opencode/agent/review-quick.md"
CANONICAL_LIST_OPEN = "**Give every finding one class**"
CANONICAL_LIST_CLOSE = "A finding that fits none"
CLASS_LABEL_RE = re.compile(r"^[a-z][a-z-]*$")


def canonical_classes(text: str) -> tuple[list[str], Optional[str]]:
    """The classes the definition names, and a reason naming what is wrong when the list cannot be read."""
    starts = [match.start() for match in re.finditer(re.escape(CANONICAL_LIST_OPEN), text)]
    if len(starts) != 1:
        return [], "'{0}' occurs {1} time(s) in {2}: expected exactly one canonical list".format(
            CANONICAL_LIST_OPEN, len(starts), REVIEW_QUICK_DEFINITION
        )
    end = text.find(CANONICAL_LIST_CLOSE, starts[0])
    if end < 0:
        return [], "{0} carries the canonical list's opening but not its end".format(REVIEW_QUICK_DEFINITION)
    tokens = [token.strip() for token in re.findall(r"`([^`]+)`", text[starts[0] : end])]
    if not tokens:
        return [], "the canonical list in {0} names no class".format(REVIEW_QUICK_DEFINITION)
    invalid = [token for token in tokens if not CLASS_LABEL_RE.match(token)]
    if invalid:
        return [], "the canonical list in {0} carries a token that is not a class: {1!r}".format(
            REVIEW_QUICK_DEFINITION, invalid[0]
        )
    return tokens, None
