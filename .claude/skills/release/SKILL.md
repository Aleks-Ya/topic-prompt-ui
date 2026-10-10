---
name: release
description: "Cut a release with `./release.sh`: run all tests, tag `vN`, bump to the next SNAPSHOT. Doesn't push."
disable-model-invocation: true
---

# Release

Cut a release by running `./release.sh` (see repo `CLAUDE.md` for what it does). It runs the full
`./gradlew test` including the `*ApiIT` integration tests, so start it in the background and wait
for it to finish once rather than polling.

```bash
./release.sh
```

## On success

Report the released version and the `vN` tag. Suggest `/push` (pushes `main` with `--tags` and
watches CI) and optionally `/deploy N` — don't run either unasked.

## On failure

The script fails before committing anything, so re-running it is safe.

- Pre-flight error (dirty tree, not on `main`, `version.txt` not a SNAPSHOT): report it and stop.
- Test failure: find the failing classes from `build/test-results/test/TEST-*.xml`.
    - If only `*ApiIT` LLM-output graders failed, they're known to be flaky: re-run each failing
      class once (`./gradlew test --tests "<fully.qualified.ClassName>"`). If they pass, re-run
      `./release.sh`.
    - Anything else (unit/UI test failure, or an `*ApiIT` that fails again): report the failure
      and stop.

Never edit code or tests just to force a release through.
