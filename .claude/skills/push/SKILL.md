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

Push `main` along with any local tags that aren't on `origin` yet (e.g. the lightweight `vN` release tags created by `release.sh`, which `--follow-tags` alone would miss since that flag only covers annotated tags):

```bash
git push origin main --tags
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

## 5. Check the Sonar report

CI going green does **not** mean Sonar is clean — the `sonar` Gradle task (`.github/workflows/gradle.yml`, "SonarCloud analysis" step, runs `./gradlew -PskipIntegrationTests sonar`) just uploads the analysis and doesn't wait on the quality gate (no `waitForQualityGate` configured in `build.gradle`). Once the run is green, explicitly read the report for the SonarCloud project (`Aleks-Ya_topic-prompt-ui`, see `CLAUDE.md`). The API is readable anonymously (no `SONAR_TOKEN` needed, and none is available locally):

```bash
curl -s "https://sonarcloud.io/api/qualitygates/project_status?projectKey=Aleks-Ya_topic-prompt-ui" | jq
curl -s "https://sonarcloud.io/api/issues/search?componentKeys=Aleks-Ya_topic-prompt-ui&resolved=false&ps=100" | jq '.issues[] | {rule, severity, type, component, line, message}'
curl -s "https://sonarcloud.io/api/hotspots/search?projectKey=Aleks-Ya_topic-prompt-ui&status=TO_REVIEW&ps=100" | jq '.hotspots[] | {vulnerabilityProbability, component, line, message}'
```

For every issue/hotspot returned:

- If it's a genuine problem, fix it in code, commit as a new commit, push, and go back to step 3 to re-verify CI, then re-run the Sonar queries above once the new analysis has landed.
- If it should **not** be fixed (false positive, accepted risk, intentional pattern), don't just leave it unresolved — explicitly suppress it inline (e.g. a `// NOSONAR` comment, following the existing example in `.github/workflows/gradle.yml`, or a rule-specific suppression) with a short comment explaining why. There's no local `SONAR_TOKEN` to mark issues "Won't Fix"/"False Positive" via the SonarCloud API, so an inline suppression is what records the decision durably and stops it reappearing in future reports.
- If it's genuinely unclear whether an issue should be fixed or ignored, ask the user rather than guessing.

## 6. Report

Report whether CI passed on the first try or required fixes, whether Sonar had any issues/hotspots and how they were resolved (fixed vs. explicitly suppressed), and give the run URL (`gh run view "$RUN_ID" --web`).
