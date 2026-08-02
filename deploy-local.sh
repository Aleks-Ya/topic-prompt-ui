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

# The app copies each prompt template into <appData>/templates only if it isn't already there
# (PromptFactoryImpl.render), so stale copies would shadow template changes in the new build.
# Delete them here; the app re-copies the fresh versions from resources on the next request.
TEMPLATES_DIR="$HOME/.topic-prompt-ui/templates"
if [ -d "$TEMPLATES_DIR" ]; then
    echo "Clearing cached prompt templates in $TEMPLATES_DIR..."
    rm -f "$TEMPLATES_DIR"/*.ftl
fi

echo "Starting TopicPromptUI..."
nohup "$HOME/installed/TopicPromptUI/bin/TopicPromptUI" >/dev/null 2>&1 &
