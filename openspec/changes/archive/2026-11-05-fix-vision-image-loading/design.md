# Design — vision obtains pixels before it reports

## Context

`vision` is the repository's only visual-review route (`AGENTS.md`, "Visual review goes to the `vision` subagent"), and
the `agent-skills` requirement "Diagrams and screenshots are produced by their specialist subagents" states what it must
produce — a structured review of the image in three groups — not how it obtains the pixels. Its definition
(`.opencode/agent/vision.md`) says only "Load the artifact yourself with the browser tool: the caller names an
`http(s)://` URL", which a run in this session took as "navigate to the URL": the page loaded (correct title, 1600×1000)
but the browser returned a wrapper and an accessibility snapshot, and `webfetch` refused the image content type, so the
subagent had no pixels and said so.

A probe isolated the cause: the same subagent, given the explicit two-step procedure — navigate, then call the browser's
screenshot tool — read the screenshot correctly, down to the five rows and the `2/2` cell. The toolchain works; the
definition stops one step short.

The advisor's consultation (owner-authorized) settled the shape: a definition-only change with an explicit acquisition
procedure and failure handling, no `AGENTS.md` edit, no local-file or data-URL fallback machinery, and verification by
two subagent invocations.

## Goals / Non-Goals

**Goals**

- Make the definition state how the pixels are obtained, in the order they are obtained.
- Make a failure to obtain pixels a reported outcome rather than a silent empty review.

**Non-Goals**

- Changing `AGENTS.md`'s URL hand-off rule, the report's three groups, the model pin, or the read-only boundary.
- Adding a fallback route (a local file path, a data URL, `webfetch`) that the demonstrated route makes unnecessary.
- Fixing how screenshots are produced or served.

## Decisions

1. **The definition carries an ordered acquisition procedure.** Navigate to the `http(s)://` URL; call the browser's
   screenshot tool on the loaded page and inspect the image it returns; state any scaling or incomplete-frame limitation
   that affects what was asked. The numbered order is the point — the definition previously named the browser but not
   the capture, which is what a reader fills in with "navigate". Alternative rejected: a single added sentence ("take a
   screenshot"), which states the fact but not the order and not what counts as evidence.
2. **A snapshot is named as not-evidence.** The procedure says navigation metadata and an accessibility snapshot are not
   visual evidence, because what a page says about an image is not a description of its pixels — the exact misreading
   that produced the empty review here.
3. **Failure is an outcome, not a silence.** When the navigation fails or the capture returns no image, the subagent
   reports that the visual review was not performed, with the tools' own outputs under the existing "Not assessable"
   group, rather than returning a clean review of nothing. That is the one trigger the delta, this decision, and task
   1.2 state.
4. **`AGENTS.md` is unchanged.** Its rule states the hand-off (a URL, because `file://` is blocked) and remains true;
   the mechanism belongs to the definition that executes it, per the single-sourcing convention. Alternative rejected:
   repeating the capture step in `AGENTS.md`, which would give the procedure a second copy to drift.
5. **Verification runs the procedure as a subagent, twice.** One hand-off of a URL that serves a real screenshot,
   checking the review's facts against the image's actual content; and one of a URL on a closed port, where navigation
   fails and no pixels can arrive, checking that the reply reports the failure with the tools' outputs rather than a
   review. A missing path would not do: `python3 -m http.server` answers it with an HTML error page, which the browser
   renders and the screenshot tool captures, so the failure branch would never fire. Alternative rejected: a filesystem
   or config assertion, which cannot see whether the model obtained pixels.

6. **The failed-acquisition outcome takes a spec delta; the procedure does not.** The requirement's scenario enumerates
   what a review reports and is silent on a run that obtains nothing, so the failure rule adds a reportable outcome it
   does not carry: the delta `MODIFIED`s "Diagrams and screenshots are produced by their specialist subagents" with
   every existing scenario plus one for that case. The acquisition steps stay in the definition, because the requirement
   states the outcome and not the mechanism. Alternative rejected: `skip_specs`, which the addition of an outcome rules
   out.

## Risks / Trade-offs

- **A screenshot of a page showing an image adds one rendering layer.** The procedure requires the scaling or
  incomplete-frame limitation to be stated, so a review never claims more than the captured pixels show.
- **The definition is guidance, not a gate.** A run that ignores the procedure can still return an empty review; the
  verification probes the saved definition's behaviour, and the failure-reporting rule makes the silent case visible
  when followed.
- **The probes exercise one of step 4's two failure branches.** The closed port makes the navigation fail, so the
  capture-never-runs branch is verified; a successful navigation whose capture returns no image is not, and remains
  covered by the rule in the definition rather than by an observed run.
- **Two probe invocations cost model calls.** They are the only evidence that the definition changes behaviour, and the
  advisor named them as the verification.

## Verification

```bash
# Serve a real screenshot, and point the second probe at a port nothing listens on
python3 -m http.server 18099 --bind 127.0.0.1 &   # from the repository root
# then hand each URL to the vision subagent, uncoached:
#   http://127.0.0.1:18099/.playwright-mcp/scrape-final.png   -> a review naming the eight entries and Up
#   http://127.0.0.1:18098/anything                            -> an explicit "not performed" with the tools' outputs
./gradlew check -PskipITs -Pcoverage.gate.enabled=false
openspec validate --all
```

## Claims table

| Claim                                                                                                            | Source                                                                                                 | Result                                                                                                                                                         |
| ---------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The definition names the browser but never the capture                                                           | `git show b3402345:.opencode/agent/vision.md \| grep -n "browser\|screenshot\|snapshot"`               | lines 15–16 name the browser and the URL only; no screenshot call, and the word `screenshot` appears only in the surrounding prose about what an image is      |
| The spec states the review's outcome and shape, and its only mechanism statement is the caller-side serving rule | ``git grep -n 'the `vision` subagent' b3402345 -- openspec/specs``                                     | two sites — the screenshot scenario and the Mermaid-render scenario, which names the caller serving the render; neither states how the subagent obtains pixels |
| `AGENTS.md` states the hand-off as a URL and does not name a capture                                             | ``grep -n '`vision` subagent' -A2 AGENTS.md``                                                          | 1387-1389: "loads only an `http(s)://` URL … serve a local artifact over HTTP and pass its URL"                                                                |
| In the diagnosis probe, navigation alone returned no pixels and an explicit screenshot returned an image         | The diagnosis probe recorded on task 2.1                                                               | navigation returned a page wrapper and an accessibility snapshot; navigation plus a screenshot returned the table's five rows and its cells                    |
| The definition loads and the gates pass with it edited                                                           | The `build` job's definition-inventory step (`.github/workflows/ci.yml`) and `openspec validate --all` | the CI step asserts every definition resolves with its metadata; the local check is the frontmatter diff on task 2.4                                           |

Dismissals, one reason per kind: `path` — what the change names is its own artifacts, the file it edits, and the local
artifacts and media type its probe record cites; `absolute` — each flagged word sits in a statement a table row covers
or in the change's own prohibition; `count` — the flagged figures are the table's rows and the task identifiers the plan
numbers; `cross-ref` — the flagged commands and paths are this change's own verification instructions.

## Consequences

- A URL hand-off now produces a review of the pixels or an explicit refusal, and never an empty review read as "nothing
  wrong".
- The mechanism lives in one place; `AGENTS.md` keeps the hand-off rule it already states.
