---
name: push
description: >-
  Push `main` branch, wait for GitHub Actions to finish, fix if it fails.
---

# Push main and watch CI

Push the current `main` branch to `origin` and make sure GitHub Actions goes green.

## 1. Pre-flight

- Confirm the current branch is `main` and the working tree is clean (`git status`, `git branch --show-current`). If there are uncommitted changes, stop and ask the user whether to commit them first — don't push unrelated dirty state.
- Confirm local `main` is not behind `origin/main` (`git fetch origin main && git status`). If it's behind, stop and ask before proceeding (don't rebase or force-push without asking).

## 2. Push

```bash
git push origin main
```

## 3. Wait for GitHub Actions

The workflow (`.github/workflows/gradle.yml`, job `build`) triggers on every push. After pushing, find the run for the commit just pushed and watch it to completion:

```bash
SHA=$(git rev-parse HEAD)
# The run may take a few seconds to be scheduled after the push.
until RUN_ID=$(gh run list --branch main --commit "$SHA" --limit 1 --json databaseId --jq '.[0].databaseId' 2>/dev/null) && [ -n "$RUN_ID" ]; do
  sleep 3
done
gh run watch "$RUN_ID" --exit-status
```

`gh run watch --exit-status` blocks until the run finishes and exits non-zero if it failed.

## 4. If it fails

- Inspect the failing logs: `gh run view "$RUN_ID" --log-failed`.
- Diagnose the root cause from the actual error (compile error, test failure, Sonar quality gate, etc.) — don't guess.
- Reproduce and fix locally. Useful commands (see repo `CLAUDE.md`):
  - Compile: `./gradlew compileTestJava`
  - Unit tests only, matches CI (`-PskipIntegrationTests`): `./gradlew -PskipIntegrationTests test`
  - A single failing test class: `./gradlew test --tests "fully.Qualified.ClassName"`
  - Authoritative failure detail: `build/test-results/test/TEST-<FullyQualifiedClassName>.xml`
- Commit the fix as a new commit (not `--amend`, not `--no-verify`), push again, then repeat step 3 to watch the new run.
- Keep iterating until the run succeeds. Never force-push or skip hooks just to make CI pass.

## 5. Report

Report whether CI passed on the first try or required fixes, and give the run URL (`gh run view "$RUN_ID" --web`).
