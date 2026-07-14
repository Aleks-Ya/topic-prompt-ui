#!/bin/sh
# PostToolUse hook: render the edited/written PlantUML file when a .puml file is edited/written.
f=$(jq -r '.tool_input.file_path // empty')
case "$f" in
  *.puml) plantuml -o "${CLAUDE_PROJECT_DIR}/build/diagrams" "$f" ;;
esac
