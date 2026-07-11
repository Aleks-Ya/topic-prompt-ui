# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Topic Prompt UI (`GptUi`) is a JavaFX desktop client for sending prompts to OpenAI, GCP (Gemini), and Claude APIs, organizing the resulting Q&A into themed history, and rendering answers (grammar/openai/claude/GCP variants) side by side. Built as a Java Platform Module System (JPMS) application with Guice for dependency injection.

## Commands

- Run from IDE: run the `gptui.GptUiMain` class (module `GptUi.main`), or use the `GptUiMain local run` run config.
- Compile tests: `./gradlew compileTestJava`
- Run all tests (unit + integration): `./gradlew test`
- Run unit tests only (skip `*IT.java`): `./gradlew -PskipIntegrationTests test`
- Run a single test class: `./gradlew test --tests "gptui.ui.question.SendFactTest"`
- Build native image via jlink: `./gradlew -x test clean jlink`
- Full local install (builds, tests, deploys to `~/installed/GptUI`): `./gradlew installLocally` (or `./gradlew -x test installLocally` to skip tests)
- Redeploy the locally installed app safely: `./deploy-local.sh` — stops any running `~/installed/GptUI` instance (graceful `SIGTERM`, matched via `pkill -f "installed/GptUI/bin"`) before running `./gradlew -x test installLocally`, since `installLocally` deletes the old install directory without stopping a running process first.

UI tests use TestFX (`ApplicationTest`) and need a display; on headless CI (see `.github/workflows/gradle.yml`) they run under Xvfb.

Integration tests (`*IT.java`, e.g. `OpenAiApiIT`, `GcpApiIT`, `SoundServiceIT`) hit real external services/APIs and require credentials in `~/.gpt/config.properties` (`openai.token`, `gcp.api.key`); they are excluded by `-PskipIntegrationTests` and are not run in CI.

## Architecture

Strict layered MVVM, one-way dependency flow: **view → viewmodel → mediator → model → core**. Each layer only talks to the layer directly below/adjacent to it.

- `gptui.core.ai` — `AiApi` interface with two implementations (`openai`, `gcp`), each with its own Guice module, request/response DTOs, and Freemarker-based prompt building.
- `gptui.core.storagefilesystem` — `StorageFilesystem`: JSON-on-disk persistence (via Gson) for `Interaction`s and `Theme`s, under `<appData>/storage`.
- `gptui.ui.model` — business/domain layer: `StateModel` (current interaction/theme selection state), `StorageModel` (wraps `StorageFilesystem`), `QuestionModel` (orchestrates sending questions to `AiApi` per `AnswerType`), `ConfigModel`, `FileModel`, `ClipboardModel`, `HistorySearchModel` (Lucene-backed search over history). Each has its own subpackage with an interface, an `*Impl`, and a Guice `*Module`.
- `gptui.ui.viewmodel.mediator` — the hub. `MediatorImpl` implements six narrow mediator interfaces (`HistoryMediator`, `QuestionMediator`, `ThemeMediator`, `AnswerMediator`, `GptUiMediator`, `GptUiApplicationMediator`), one per viewmodel area. Viewmodels never touch models directly; they call through their specific mediator interface. This is the layer to touch when a user action needs to fan out across multiple viewmodels (e.g. `displayCurrentInteraction()` refreshes history, question, theme, and all four answer panes).
- `gptui.ui.viewmodel.{answer,history,question,theme,ui,uiapp}` — one subpackage per UI area, each following the same triad: `*VmController` (what the view calls), `*VmMediator` (what the mediator calls back into), `*VmImpl` (implements both), `*Properties` (JavaFX observable properties bound by FXML), `*Module` (Guice bindings). There are 4 parallel `AnswerVm` instances (`AnswerVmModule.GRAMMAR/OPEN_AI/CLAUDE/GCP`), all bound via `@Named` qualifiers to the same interfaces.
- `gptui.ui.view` — FXML controllers (`GptUiController`, `HistoryController`, `QuestionController`, `ThemeController`, `AnswerController`) plus `GptUiApplication`, the JavaFX `Application` entrypoint that boots the Guice context (`RootModule`) and loads `GptUi.fxml`.
- `gptui.RootModule` — top-level Guice module, installs `ModelModule`, `ViewModelModule`, `ViewModule` in that order. `binder().requireExplicitBindings()` is set, so every injected type must be explicitly bound in some module.
- `docs/*.puml` — PlantUML class diagrams (`Class Diagram.puml`, `Class Diagram 2.puml`, `Class Diagram 3.puml`, `packages.puml`, `Threads.puml`) documenting this layering; consult these before making cross-layer changes.

### AnswerType / prompt templates

`AnswerType` (`GRAMMAR`, `OPEN_AI`, `CLAUDE`, `GCP`) drives parallel behavior throughout the stack: 4 `AnswerVm` bindings, 4 answer panes in the FXML, and 4 Freemarker prompt templates in `src/main/resources/gptui/ui/model/question/prompt/` (`definition-*.ftl`, `question-*.ftl`, plus `grammar.ftl`/`fact-grammar.ftl`). `PromptFactory` builds the actual prompt text sent to `AiApi` from these templates.

### JPMS module-info

`src/main/java/module-info.java` explicitly `exports`/`opens` every package that Guice, Gson, or FXML need reflective/runtime access to. **When adding a new package, or a new class that Guice/Gson/FXML must reach reflectively, add the corresponding `exports`/`opens` entry** or the app will fail at runtime with module-access errors, not compile errors.

### Testing conventions

- `BaseGptUiTest` (extends TestFX `ApplicationTest`) boots the real `GptUiApplication` with `Modules.override(new RootModule()).with(new TestRootModule())` — `TestRootModule` swaps in `MockOpenAiApi`/`MockGcpApi`, `SoundServiceMock`, and an in-memory Jimfs filesystem. Most UI-level tests (`gptui.ui.*`) extend this and drive the app through TestFX robot calls against real FXML scenes.
- Model-level tests (e.g. `StorageFilesystemTest`, `StateModelTest`, `StorageModelTest`) test a single model directly, often with Jimfs for filesystem isolation.
- `*IT.java` suffix marks integration tests that call real external APIs; keep new integration tests under this naming convention so `-PskipIntegrationTests` excludes them correctly.
