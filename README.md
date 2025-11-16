# Topic Prompt UI

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
