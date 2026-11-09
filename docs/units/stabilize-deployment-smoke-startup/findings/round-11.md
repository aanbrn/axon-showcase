- [stale-base] origin/main@634bc3b5 — the branch sat on `0deca079` behind #873, so `git diff --name-status origin/main`
  spanned another unit's files and the `gradle-build` pack read this round was the pre-#873 copy.
- [formatting] .github/workflows/deployment-smoke.yml:129 — the publish's `-d` payload line is 183 characters and the
  `sed` pipeline on line 130 is 122, past the manual 120-character rule for YAML the formatter does not own.
