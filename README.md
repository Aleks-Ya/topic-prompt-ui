# Topic Prompt UI

## Run from IDE
Run `gptui.GptUiMain` class.

## Unit-tests
Compile: `./gradlew compileTestJava`
Run unit-tests (including integration tests): `./gradlew test`
Run unit-tests (skip integration tests): `./gradlew -PskipIntegrationTests test`

## Link
`./gradlew -x test clean jlink`

## Code quality
SonarCloud project: https://sonarcloud.io/project/overview?id=Aleks-Ya_topic-prompt-ui

## Install on Ubuntu
1. Release a new version: `./release.sh` (runs tests, tags the release, bumps to the next SNAPSHOT — then push manually as printed)
2. Use Java 25: `sdk use java 25.0.3-zulu`
3. Build distribution and deploy to `/home/aleks/installed/GptUI`:
    1. With tests: `./gradlew installLocally`
    2. Without tests: `./gradlew -x test installLocally`
    3. Or, to also stop a currently running instance first: `./deploy-local.sh`
4. Add to `PATH` in `~/.bashrc`: `export PATH=$PATH:/home/aleks/installed/GptUI/bin`
5. Add a menu icon by `MenuLibre` application:
    1. Icon: `/home/aleks/installed/GptUI/bin/icon.png`
    2. Name: `Topic Prompt UI`
    3. Command: `/home/aleks/installed/GptUI/bin/GptUi`
    4. Advanced:
        1. Start VM Class: `gptui.view.GptUiApplication`
6. Tokens: `~/.gpt/config.properties`. Properties:
    1. `openai.token`
    2. `gcp.api.key`
    3. `claude.api.key`
7. Log file: `~/.gpt/console.log`
