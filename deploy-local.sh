#!/bin/sh
# Stop any running local install of GptUi, then rebuild and redeploy it.
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$SCRIPT_DIR"

PATTERN="installed/GptUI/bin"

if pgrep -f "$PATTERN" >/dev/null 2>&1; then
    echo "Stopping running GptUi instance..."
    pkill -f "$PATTERN"

    i=0
    while pgrep -f "$PATTERN" >/dev/null 2>&1; do
        i=$((i + 1))
        if [ "$i" -ge 10 ]; then
            echo "GptUi is still running after 10s; leaving it as-is." >&2
            break
        fi
        sleep 1
    done
else
    echo "No running GptUi instance found."
fi

echo "Building and installing..."
./gradlew -x test installLocally

echo "Starting GptUi..."
nohup "$HOME/installed/GptUI/bin/GptUi" >/dev/null 2>&1 &
