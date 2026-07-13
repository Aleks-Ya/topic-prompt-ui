---
name: deploy
description: >-
  Deploy GptUi locally: `/deploy` - current version, `/deploy 66` - tag `v66`.
---

# Deploy

Deploy GptUi locally. An optional version argument (e.g. `66`) selects the git tag `v66` to
deploy instead of whatever's currently checked out.

## No argument

Just redeploy whatever is currently checked out:

```bash
./deploy-local.sh
```

## Version argument given (e.g. `66` → tag `v66`)

1. Confirm the tag exists: `git rev-parse -q --verify refs/tags/vN`. If it doesn't, stop and
   report the error — don't guess at a different tag.
2. Record how to get back afterward: current branch via `git branch --show-current`. If that's
   empty (detached HEAD), record the current commit via `git rev-parse HEAD` instead.
3. Check `git status --short`. If it shows anything, stash it with a clearly labeled message
   (`git stash push -u -m "deploy skill: pre-deploy stash for vN"`) and remember a stash was made.
4. `git checkout vN`.
5. Run `./deploy-local.sh`.
6. Check back out whatever was recorded in step 2 (branch name, or the commit hash if it was
   detached HEAD).
7. If a stash was made in step 3, `git stash pop` it.

If any step fails (bad tag, dirty-checkout conflict, stash-pop conflict, deploy script failure),
stop and report the problem rather than pushing through — in particular, never pop a stash onto
a tree in a state that would silently conflict or lose changes.
