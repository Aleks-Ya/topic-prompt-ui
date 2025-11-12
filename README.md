# Topic Prompt UI

## TODO
1. Add `Delete` button for the Themes
2. Add `Rename` button for the Themes
3. Add a filter (search) field for History
4. Add a "Copy" button for Question
5. Expand the Question and Answer view to full screen
6. Replace Gemini Pro with Gemini Ultra.
7. Add a new Theme dialog: disable the OK button if the theme title is empty
8. Fix floating unit-tests: `TemperatureTest`
9. Limit length of text in History ComboBox
10. Bug: Ctrl-Alt-V within the Theme ComboBox
11. Bug: Ctrl-Alt-Up/Down switch Interaction in random order
12. Utilize "Structured output" from `gpt-4o-2024-08-06` model (https://openai.com/index/introducing-structured-outputs-in-the-api/)

## Unit-tests
Compile: `./gradlew compileTestJava`
Run: `./gradlew test`

## Install on Ubuntu
1. Use Java 25: `sdk use java 25.0.1-zulu`
2. Build distribution and deploy to `/home/aleks/installed/GptUI`:
    1. With tests: `./gradlew installLocally`
    2. Without tests: `./gradlew -x test installLocally`
3. Add to `PATH` in `~/.bashrc`: `export PATH=$PATH:/home/aleks/installed/GptUI/bin`
4. Add a menu icon by `MenuLibre` application:
    1. Icon: `/home/aleks/installed/GptUI/bin/icon.png`
    2. Name: `Topic Prompt UI`
    3. Command: `/home/aleks/installed/GptUI/bin/GptUi`
    4. Advanced:
        1. Start VM Class: `gptui.view.GptUiApplication`
5. Tokens: `~/.gpt/config.properties`. Properties:
    1. `openai.token`
    2. `gcp.api.key`
6. Log file: `~/.gpt/console.log`
