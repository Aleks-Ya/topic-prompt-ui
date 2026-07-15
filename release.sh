#!/bin/sh
# Release the current SNAPSHOT version: run tests, tag the release, and bump to the next SNAPSHOT.
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$SCRIPT_DIR"

VERSION_FILE="src/main/resources/topicpromptui/version.txt"

if [ -n "$(git status --porcelain)" ]; then
    echo "Error: working tree is not clean. Commit or stash changes before releasing." >&2
    exit 1
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$BRANCH" != "main" ]; then
    echo "Error: must be on 'main' branch to release (currently on '$BRANCH')." >&2
    exit 1
fi

CURRENT=$(cat "$VERSION_FILE")
case "$CURRENT" in
    *-SNAPSHOT) ;;
    *)
        echo "Error: version.txt ('$CURRENT') is not a SNAPSHOT version; nothing to release." >&2
        exit 1
        ;;
esac

RELEASE_VERSION=${CURRENT%-SNAPSHOT}
NEXT_SNAPSHOT="$((RELEASE_VERSION + 1))-SNAPSHOT"

echo "Releasing version $RELEASE_VERSION (next snapshot will be $NEXT_SNAPSHOT)"

echo "Running full test suite (unit + integration)..."
./gradlew test

echo "Setting release version $RELEASE_VERSION..."
printf '%s' "$RELEASE_VERSION" > "$VERSION_FILE"
git add "$VERSION_FILE"
git commit -m "Bump version to $RELEASE_VERSION"

echo "Tagging v$RELEASE_VERSION..."
git tag "v$RELEASE_VERSION"

echo "Setting next snapshot version $NEXT_SNAPSHOT..."
printf '%s' "$NEXT_SNAPSHOT" > "$VERSION_FILE"
git add "$VERSION_FILE"
git commit -m "Set snapshot version"

echo
echo "Release complete: v$RELEASE_VERSION"
echo "Push manually when ready:"
echo "  git push"
echo "  git push origin v$RELEASE_VERSION"
