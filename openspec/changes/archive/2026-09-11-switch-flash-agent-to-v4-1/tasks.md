## 1. Switch the flash model pins to deepseek-v4.1-flash

- [x] 1.1 Update `.opencode/opencode.json` `model` and `small_model` to `opencode-go/deepseek-v4.1-flash` and verify
      both keys read the new value
- [x] 1.2 Update the flash-pinned subagents' frontmatter to `opencode-go/deepseek-v4.1-flash` (`review-quick.md`,
      `lesson-capture.md`, `experience-analyzer.md`) and verify no `deepseek-v4-flash` (without a `-vision-exp` suffix)
      pins remain in `.opencode/agent/`
- [x] 1.3 Update `.github/workflows/opencode.yml` `model` input to `opencode-go/deepseek-v4.1-flash` and verify the
      workflow's actionlint lint still passes
- [x] 1.4 Update the `AGENTS.md` agent gotchas naming the cheap main-agent model to `deepseek-v4.1-flash` and verify no
      stale `deepseek-v4-flash` (without a `-vision-exp` suffix) references remain in `AGENTS.md` and `README.md`

## 2. Verify

- [x] 2.1 Confirm the remaining `deepseek-v4-flash` references are only the vision agent's `-vision-exp` pin (and
      historical archived-change content), and run `./gradlew spotlessApply spotlessCheck` plus
      `openspec validate --changes` cleanly
