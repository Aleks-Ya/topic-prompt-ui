# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Claude Code memory

Durable facts, decisions, and conventions that Claude Code learns while working in this repo are committed here rather than kept only in the private per-user auto-memory store — that keeps them shared across machines and contributors instead of stuck on one person's disk. When you learn something worth remembering long-term about this project, add it to the relevant section of this file (and commit it) instead of (or in addition to) saving it to local memory.

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
- Redeploy the locally installed app safely: `./deploy-local.sh` — stops any running `~/installed/GptUI` instance (graceful `SIGTERM`, matched via `pkill -f "installed/GptUI/bin"`, polls up to 10s for exit) before running `./gradlew -x test installLocally`, since the `installLocally` Gradle task deletes the old `~/installed/GptUI` directory and copies in the new jlink image but never stops a running process first — a bare `installLocally` while the app is running deletes its jars/launcher out from under it. Prefer this script over calling `./gradlew installLocally` directly when redeploying locally.

UI tests use TestFX (`ApplicationTest`) and need a display; on headless CI (see `.github/workflows/gradle.yml`) they run under Xvfb.

Integration tests (`*IT.java`, e.g. `OpenAiApiIT`, `ClaudeApiIT`, `GcpApiIT`, `SoundServiceIT`) hit real external services/APIs and require credentials in `~/.gpt/config.properties` (`openai.token`, `claude.api.key`, `gcp.api.key`); they are excluded by `-PskipIntegrationTests` and are not run in CI. If `~/.gpt/config.properties` on the local machine already has all three credentials populated, these tests are directly runnable via `./gradlew test` — don't assume they're unrunnable and skip straight to `-PskipIntegrationTests`; try running the relevant `*ApiIT` class first.

## Code quality

SonarCloud project: https://sonarcloud.io/project/overview?id=Aleks-Ya_topic-prompt-ui — check here for code quality/coverage metrics and issues.

## Architecture

Strict layered MVVM, one-way dependency flow: **view → viewmodel → mediator → model → core**. Each layer only talks to the layer directly below/adjacent to it.

- `gptui.core.ai` — `AiApi` interface with three provider implementations (`openai`, `claude`, `gcp`), each with its own Guice module, request/response DTOs, and Freemarker-based prompt building. Model names are hardcoded per binding inside each `*Module` (`OpenAiModule`, `ClaudeModule`, `GcpModule`) via manual `new XApiImpl(model)` + `toInstance(...)` (not compiled into the impl classes) — this lets `OpenAiModule` bind two `AiApi` instances (`AiModule.OPEN_AI` and `AiModule.OPEN_AI_GRAMMAR`) with different models, so `AnswerType.GRAMMAR` can use a cheaper/faster model than `AnswerType.OPEN_AI`.
- **Reasoning effort** is likewise hardcoded per binding: each `*ApiImpl` constructor takes an extra effort/thinking-level enum parameter (OpenAI: `ReasoningEffort` in `openai/RequestBody.java`; Claude: `Effort` nested under `output_config` in `claude/RequestBody.java`; GCP: `ThinkingLevel` nested under `generationConfig.thinkingConfig` in `gcp/RequestBody.java`), set as a `private static final` constant in the corresponding `*Module`. Passing `null` omits the field from the JSON request body entirely (Gson's default `new Gson()` skips null fields, no `serializeNulls()` configured) so the provider falls back to its own default effort/thinking level instead of an explicit value.
- **Model metadata and token usage are persisted for troubleshooting.** `AiApi.send(...)` returns `AiResponse(text, responseId, modelId, modelParams, inputTokens, outputTokens, totalTokens)` — `modelId`/`modelParams` come from each `*ApiImpl`'s own `model`/`effort` constructor fields (not echoed-back response data, since GCP's response never repeats the model), and the token counts are parsed from each provider's real usage JSON (`openai/ResponseBody.Usage` — `input_tokens`/`output_tokens`/`total_tokens`; `claude/ResponseBody.Usage` — `input_tokens`/`output_tokens`, with `totalTokens` computed as their sum since Claude's API doesn't return one; `gcp/ResponseBody.UsageMetadata` — `promptTokenCount`/`candidatesTokenCount`/`totalTokenCount`, where `totalTokenCount` can exceed `prompt+candidates` because Gemini's thinking models fold internal "thoughts" tokens into the total without breaking them out separately). `QuestionModelImpl` copies all 5 fields onto the stored `Answer` via `Answer.withModelInfo(...)` alongside the existing `withResponseId(...)` call, in both `requestAnswer` and `requestFollowUpAnswer`.
- `gptui.core.storagefilesystem` — `StorageFilesystem`: JSON-on-disk persistence (via Gson) for `Interaction`s and `Theme`s, under `<appData>/storage`.
- `gptui.ui.model` — business/domain layer: `StateModel` (current interaction/theme selection state), `StorageModel` (wraps `StorageFilesystem`), `QuestionModel` (orchestrates sending questions to `AiApi` per `AnswerType`), `ConfigModel`, `FileModel`, `ClipboardModel`, `HistorySearchModel` (Lucene-backed search over history). Each has its own subpackage with an interface, an `*Impl`, and a Guice `*Module`.
- `gptui.ui.viewmodel.mediator` — the hub. `MediatorImpl` implements six narrow mediator interfaces (`HistoryMediator`, `QuestionMediator`, `ThemeMediator`, `AnswerMediator`, `GptUiMediator`, `GptUiApplicationMediator`), one per viewmodel area. Viewmodels never touch models directly; they call through their specific mediator interface. This is the layer to touch when a user action needs to fan out across multiple viewmodels (e.g. `displayCurrentInteraction()` refreshes history, question, theme, and all four answer panes).
- `gptui.ui.viewmodel.{answer,history,question,theme,ui,uiapp}` — one subpackage per UI area, each following the same triad: `*VmController` (what the view calls), `*VmMediator` (what the mediator calls back into), `*VmImpl` (implements both), `*Properties` (JavaFX observable properties bound by FXML), `*Module` (Guice bindings). There are 4 parallel `AnswerVm` instances (`AnswerVmModule.GRAMMAR/OPEN_AI/CLAUDE/GCP`), all bound via `@Named` qualifiers to the same interfaces.
- `gptui.ui.view` — FXML controllers (`GptUiController`, `HistoryController`, `QuestionController`, `ThemeController`, `AnswerController`) plus `GptUiApplication`, the JavaFX `Application` entrypoint that boots the Guice context (`RootModule`) and loads `GptUi.fxml`.
- `gptui.RootModule` — top-level Guice module, installs `ModelModule`, `ViewModelModule`, `ViewModule` in that order. `binder().requireExplicitBindings()` is set, so every injected type must be explicitly bound in some module.
- `docs/*.puml` — PlantUML class diagrams (`Class Diagram.puml`, `Class Diagram 2.puml`, `Class Diagram 3.puml`, `packages.puml`, `Threads.puml`) documenting this layering; consult these before making cross-layer changes.

### AnswerType / prompt templates

`AnswerType` (`GRAMMAR`, `OPEN_AI`, `CLAUDE`, `GCP`) drives parallel behavior throughout the stack: 4 `AnswerVm` bindings, 4 answer panes in the FXML, and 4 Freemarker prompt templates in `src/main/resources/gptui/ui/model/question/prompt/` (`definition-*.ftl`, `question-*.ftl`, plus `grammar.ftl`/`fact-grammar.ftl`). `PromptFactory` builds the actual prompt text sent to `AiApi` from these templates.

### Grammar checking

The user is not a native English speaker, so the app always grammar-checks their questions. The app never auto-corrects the question before sending it — the original text is what's sent to the AI models; the corrected version is only ever displayed in the `Grammar` answer panel for reference, and the user can choose to manually re-send the corrected version as a new question if they want.

There are 4 question-submission buttons in the UI: **Question**, **Definition**, **Fact**, **Grammar**. `Grammar` sends only a grammar-check request. `Question`, `Definition`, and `Fact` each send two things: a grammar-check request, and the question request (original text, unmodified) fanned out to all 3 AI models (OpenAI, Claude, GCP). A separate **Resend** button repeats the last request, for recovering from failures.

### Follow-up requests

`Interaction` has a nullable `parentInteractionId` field (links a follow-up to the interaction it continues) and `Answer` has a nullable `responseId` field (each provider's response id — OpenAI `resp_...`, Claude `msg_...`, GCP `responseId`, all confirmed live). The thread model is a chain of separate linked `Interaction`s (one per turn, `parentInteractionId` pointing at the previous turn), not a multi-turn container inside a single `Interaction` — this keeps the 1-file-per-interaction storage layout and the 4-parallel-`AnswerVm` architecture unchanged.

**UI trigger**: a "Follow-up" `CheckBox` (`fx:id="followUpCheckBox"` in `Question.fxml`, `QuestionVmProperties.followUpCheckBoxSelected`) sits next to the Resend button, mirroring the existing `filterHistoryCheckBox`/`bindBidirectional` pattern in `Theme.fxml`. When checked, clicking **Question**/**Definition**/**Fact** creates a new `Interaction` whose `parentInteractionId` is whatever's currently displayed/selected (`mediator.getCurrentInteractionId()`) instead of `null` — `QuestionVmImpl.createNewInteractionAndRequestAnswers` computes this and passes it through `StateModel`/`QuestionMediator.createInteraction(InteractionType, InteractionId parentInteractionId)`. **Grammar** is unaffected (grammar-checking is never a follow-up).

Key design point: **Resend and each answer pane's ⟳ regenerate button needed zero code changes.** They already funnel through the single `QuestionModel.requestAnswer(interactionId, answerType, callback)` regardless of which interaction is targeted; `requestAnswer` itself now checks `interaction.parentInteractionId() != null && answerType != GRAMMAR` at the top and internally delegates to `requestFollowUpAnswer` when true. The follow-up/non-follow-up distinction is a pure data concern (what's stored on the `Interaction`), not something the mediator/viewmodel/view layers need to know about.

`requestFollowUpAnswer` deliberately skips `PromptFactory`/FreeMarker templating and sends `interaction.question()` verbatim as the new turn's text — the templated framing (theme, instructions) was already established in the first message of the conversation via `FollowUpHistoryBuilder`'s reconstructed history, so re-wrapping the follow-up text in the same template would be redundant. Only the very first message of a conversation (sent through plain `requestAnswer`) is templated; every subsequent follow-up turn is raw user text.

Follow-up interactions are marked in the History combo box with a `↳ ` prefix (`InteractionItem.toString()`, `gptui.ui.viewmodel`) so users can tell them apart from independent interactions.

**Hotkey (`Alt-U`) is a Scene accelerator, not an FXML mnemonic** — deliberately, because `CheckBox` mnemonics are broken in this JavaFX version: the Scene's automatic underscore-mnemonic system dispatches a plain `ActionEvent` to the target rather than calling the control's `fire()`, and `CheckBox`'s "toggle `selected`" logic lives specifically inside its overridden `fire()`, so mnemonic activation silently does nothing (confirmed empirically — reproduced with multiple different mnemonic letters, ruling out a text-parsing quirk specific to "U" or the hyphen in "Follow-up"). `Button` mnemonics work fine because `onAction` *is* the whole behavior, so a raw `ActionEvent` is sufficient. The fix: `followUpCheckBox` keeps `mnemonicParsing="false"`, and `MediatorImpl.addShortcuts` registers `Alt-U` as a `KeyCodeCombination` in `scene.getAccelerators()` (the same mechanism already used there for `Ctrl-Alt-Up/Down`, `Ctrl-Alt-V`, `Esc`, `Ctrl-Enter`), wired to a new `QuestionVmMediator.toggleFollowUp()` that flips `followUpCheckBoxSelected` directly. If a future checkbox/toggle needs a hotkey, follow this same accelerator pattern rather than an FXML mnemonic.

`responseId` is captured purely as informational/debugging metadata, **not** used for conversation continuation. OpenAI's Responses API supports resuming via `previous_response_id`, but OpenAI's server-side response retention is time-limited (~30 days), so relying on it would silently break follow-ups to older interactions. Instead, all three providers reconstruct full conversation history by walking the `parentInteractionId` chain and reusing each ancestor `Answer`'s existing `prompt`/`answerMd`.

The actual sending mechanism is built and tested, at the model/core layer only (`gptui.core.ai`, `gptui.ui.model.question.question`) — no `gptui.ui.view`/`gptui.ui.viewmodel` changes:
- `AiApi.send(String)` is now a `default` method delegating to the real abstract method `AiApi.send(List<ConversationTurn> turns)` (`ConversationTurn(Speaker, content)`, `Speaker` is `USER`/`MODEL`). Each provider impl maps `Speaker` to its own role vocabulary (OpenAI/Claude: `user`/`assistant`; GCP: `user`/`model`) and always builds the full turn list — OpenAI's `RequestBody.input` is `List<InputItem>` (Responses API accepts an array of `{role, content}` items, not just a plain string), Claude/GCP already had array-shaped request bodies.
- `FollowUpHistoryBuilder` (package-private, in `gptui.ui.model.question.question`, mirrors the `FormatConverter` self-bind-in-`QuestionModule` pattern) walks the `parentInteractionId` chain via `StorageModel` and reconstructs the turn list for a given `AnswerType`, throwing `IllegalStateException` if an ancestor lacks a `SUCCESS` answer for that type.
- `QuestionModel.requestFollowUpAnswer(InteractionId, AnswerType, Runnable callback)` mirrors `requestAnswer`'s async structure but requires a non-null `parentInteractionId`, builds history via `FollowUpHistoryBuilder`, appends the new prompt as a final `USER` turn, and calls `send(turns)`. `GRAMMAR` is intentionally unsupported (throws) since grammar-checking isn't a conversation.

Test-only note: any test exercising `QuestionModelImpl` (real or follow-up) must run with a live JavaFX toolkit, since `updateAnswer` calls `Platform.runLater` — extend `ApplicationTest` directly (see `RequestFollowUpAnswerTest`, mirroring `ClipboardModelTest`) rather than plain `BaseTest`, without booting the full `GptUiApplication`/FXML scene like `BaseGptUiTest` does. `BaseMockApi` also now exposes `getTurnsSendHistory()` (full `List<ConversationTurn>` per call, not just the last message's text) for asserting multi-turn payloads were built correctly.

### JPMS module-info

`src/main/java/module-info.java` explicitly `exports`/`opens` every package that Guice, Gson, or FXML need reflective/runtime access to. **When adding a new package, or a new class that Guice/Gson/FXML must reach reflectively, add the corresponding `exports`/`opens` entry** or the app will fail at runtime with module-access errors, not compile errors.

### Testing conventions

- `BaseGptUiTest` (extends TestFX `ApplicationTest`) boots the real `GptUiApplication` with `Modules.override(new RootModule()).with(new TestRootModule())` — `TestRootModule` swaps in `MockOpenAiApi`/`MockGcpApi`, `SoundServiceMock`, and an in-memory Jimfs filesystem. Most UI-level tests (`gptui.ui.*`) extend this and drive the app through TestFX robot calls against real FXML scenes.
- Model-level tests (e.g. `StorageFilesystemTest`, `StateModelTest`, `StorageModelTest`) test a single model directly, often with Jimfs for filesystem isolation.
- `*IT.java` suffix marks integration tests that call real external APIs; keep new integration tests under this naming convention so `-PskipIntegrationTests` excludes them correctly.
- After `./gradlew test`, prefer reading `build/test-results/test/TEST-<FullyQualifiedClassName>.xml` for authoritative `tests`/`failures`/`errors` counts and captured `<system-out>` output — piping a full run through `tail -N` can truncate away the exact section you need (e.g. an `*IT` class that finishes early in a long combined run).
- When changing a hardcoded AI model name constant in `OpenAiModule`/`ClaudeModule`/`GcpModule`, verify it against the real provider API via the matching `*ApiIT` (e.g. `./gradlew test --tests "gptui.core.ai.openai.OpenAiApiIT"`) before considering the change done — an invalid model name compiles fine but is rejected at request time (e.g. `model_not_found`).
