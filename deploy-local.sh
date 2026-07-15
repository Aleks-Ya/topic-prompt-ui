#!/bin/sh
# Stop any running local install of TopicPromptUI, then rebuild and redeploy it.
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$SCRIPT_DIR"

PATTERN="installed/TopicPromptUI/bin"

if pgrep -f "$PATTERN" >/dev/null 2>&1; then
    echo "Stopping running TopicPromptUI instance..."
    pkill -f "$PATTERN"

    i=0
    while pgrep -f "$PATTERN" >/dev/null 2>&1; do
        i=$((i + 1))
        if [ "$i" -ge 10 ]; then
            echo "TopicPromptUI is still running after 10s; leaving it as-is." >&2
            break
        fi
        sleep 1
    done
else
    echo "No running TopicPromptUI instance found."
fi

echo "Building and installing..."
./gradlew -x test installLocally

echo "Starting TopicPromptUI..."
nohup "$HOME/installed/TopicPromptUI/bin/TopicPromptUI" >/dev/null 2>&1 &
