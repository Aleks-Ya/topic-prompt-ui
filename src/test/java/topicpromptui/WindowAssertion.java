package topicpromptui;

import topicpromptui.core.domain.AiProvider;
import topicpromptui.core.domain.AnswerType;
import topicpromptui.core.domain.Interaction;
import topicpromptui.core.domain.Topic;
import topicpromptui.ui.viewmodel.InteractionItem;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.assertj.core.api.SoftAssertions;
import org.testfx.util.WaitForAsyncUtils;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.WHITE;

public class WindowAssertion {
    private BaseTopicPromptUiTest app;
    private Node focus;
    private int historySizeFiltered;
    private int historySizeFull;
    private boolean historyDeleteButtonDisabled;
    private Interaction historySelectedItem;
    // null means "same as historySelectedItem": the model current interaction only diverges from the
    // combo selection when the history filter text hides the current interaction from the items.
    private Interaction modelCurrentInteraction;
    private List<Interaction> historyItems;
    private String historyFilterText = "";
    private int topicSize;
    private Topic topicSelectedItem;
    private List<Topic> topicItems;
    private boolean topicRenameButtonDisabled;
    // null means "same as topicRenameButtonDisabled": both buttons disable on the same
    // "no current topic" condition, so most tests don't need to set this separately.
    private Boolean topicDeleteButtonDisabled;
    private Boolean filterHistorySelected;
    private String questionText;
    private String questionStyle;
    private Boolean isEnteringNewQuestion;
    private String modelEditedQuestion;
    // The defaults; a test that selects another provider for a pane overrides them with answerProviders(...).
    private AiProvider[] answerProviders = {AiProvider.OPEN_AI_GRAMMAR, AiProvider.OPEN_AI, AiProvider.XAI, AiProvider.GCP};
    // Literals, not AiProvider.displayName(), so the assertion independently verifies which text the
    // selected provider shows in the pane's ComboBox.
    private static final Map<AiProvider, String> PROVIDER_TEXTS = new EnumMap<>(Map.of(
            AiProvider.OPEN_AI, "OpenAI", AiProvider.OPEN_AI_GRAMMAR, "GPT mini",
            AiProvider.CLAUDE, "Claude", AiProvider.GCP, "Gemini", AiProvider.XAI, "Grok"));
    // Literals rather than production constants (AnswerVmImpl.hotkeyDigitMap, the descr() prefixes), so
    // the assertion stays an independent statement of what the panes must show.
    private static final Map<AnswerType, String> ANSWER_LABELS = new EnumMap<>(Map.of(
            AnswerType.GRAMMAR, "Grammar", AnswerType.AI_1, "Ai1", AnswerType.AI_2, "Ai2", AnswerType.AI_3, "Ai3"));
    private static final Map<AnswerType, Integer> ANSWER_DIGITS = new EnumMap<>(Map.of(
            AnswerType.GRAMMAR, 1, AnswerType.AI_1, 2, AnswerType.AI_2, 3, AnswerType.AI_3, 4));
    private final Map<AnswerType, AnswerInfo> answers = new EnumMap<>(Map.of(
            AnswerType.GRAMMAR, new AnswerInfo(), AnswerType.AI_1, new AnswerInfo(),
            AnswerType.AI_2, new AnswerInfo(), AnswerType.AI_3, new AnswerInfo()));
    private String testName = "Initialize";
    private String clipboard = null;

    public class AnswerInfo {
        private String text;
        private Color circleColor;

        public WindowAssertion text(String text) {
            this.text = text;
            return WindowAssertion.this;
        }

        public void circleColor(Color circleColor) {
            this.circleColor = circleColor;
        }
    }

    public static WindowAssertion builder() {
        return new WindowAssertion();
    }

    public WindowAssertion work(String testName, Runnable work) {
        assertApp();
        this.testName = testName;
        work.run();
        return this;
    }

    public WindowAssertion app(BaseTopicPromptUiTest app) {
        this.app = app;
        return this;
    }

    public WindowAssertion focus(Node focus) {
        this.focus = focus;
        return this;
    }

    public WindowAssertion clipboard(String clipboard) {
        this.clipboard = clipboard;
        return this;
    }

    public WindowAssertion historySize(int historySizeFiltered, int historySizeFull) {
        this.historySizeFiltered = historySizeFiltered;
        this.historySizeFull = historySizeFull;
        return this;
    }

    public WindowAssertion historyDeleteButtonDisabled(boolean historyDeleteButtonDisabled) {
        this.historyDeleteButtonDisabled = historyDeleteButtonDisabled;
        return this;
    }

    public WindowAssertion historySelectedItem(Interaction historySelectedItem) {
        this.historySelectedItem = historySelectedItem;
        return this;
    }

    public WindowAssertion modelCurrentInteraction(Interaction modelCurrentInteraction) {
        this.modelCurrentInteraction = modelCurrentInteraction;
        return this;
    }

    public WindowAssertion historyFilterText(String historyFilterText) {
        this.historyFilterText = historyFilterText;
        return this;
    }

    public WindowAssertion historyItems(List<Interaction> historyItems) {
        this.historyItems = historyItems;
        return this;
    }

    public WindowAssertion historyItems(Interaction... historyItems) {
        this.historyItems = Arrays.asList(historyItems);
        return this;
    }

    public WindowAssertion topicSize(int topicSize) {
        this.topicSize = topicSize;
        return this;
    }

    public WindowAssertion topicSelectedItem(Topic topicSelectedItem) {
        this.topicSelectedItem = topicSelectedItem;
        return this;
    }

    public WindowAssertion topicItems(Topic... topicItems) {
        this.topicItems = Arrays.asList(topicItems);
        return this;
    }

    public WindowAssertion topicRenameButtonDisabled(boolean topicRenameButtonDisabled) {
        this.topicRenameButtonDisabled = topicRenameButtonDisabled;
        return this;
    }

    public WindowAssertion topicDeleteButtonDisabled(boolean topicDeleteButtonDisabled) {
        this.topicDeleteButtonDisabled = topicDeleteButtonDisabled;
        return this;
    }

    public WindowAssertion topicFilterHistorySelected(Boolean filterHistorySelected) {
        this.filterHistorySelected = filterHistorySelected;
        return this;
    }

    public WindowAssertion questionText(String questionText) {
        this.questionText = questionText;
        return this;
    }

    public WindowAssertion questionStyle(String questionStyle) {
        this.questionStyle = questionStyle;
        return this;
    }

    public WindowAssertion modelEditedQuestion(String modelEditedQuestion) {
        this.modelEditedQuestion = modelEditedQuestion;
        return this;
    }

    public WindowAssertion modelIsEnteringNewQuestion(Boolean isEnteringNewQuestion) {
        this.isEnteringNewQuestion = isEnteringNewQuestion;
        return this;
    }

    public AnswerInfo grammarA() {
        return answers.get(AnswerType.GRAMMAR);
    }

    public AnswerInfo ai1A() {
        return answers.get(AnswerType.AI_1);
    }

    public AnswerInfo ai2A() {
        return answers.get(AnswerType.AI_2);
    }

    public AnswerInfo ai3A() {
        return answers.get(AnswerType.AI_3);
    }

    public WindowAssertion answerProviders(AiProvider grammar, AiProvider ai1, AiProvider ai2, AiProvider ai3) {
        answerProviders = new AiProvider[]{grammar, ai1, ai2, ai3};
        return this;
    }

    public WindowAssertion answerCircleColors(Color answerGrammarCircleColor, Color answerAi1CircleColor,
                                              Color answerAi2CircleColor, Color answerAi3CircleColor) {
        grammarA().circleColor(answerGrammarCircleColor);
        ai1A().circleColor(answerAi1CircleColor);
        ai2A().circleColor(answerAi2CircleColor);
        ai3A().circleColor(answerAi3CircleColor);
        return this;
    }

    private static String nodeFullId(Node node) {
        var ids = new ArrayList<String>();
        while (node != null) {
            ids.add(node.getId() != null ? node.getId() : "null");
            node = node.getParent();
        }
        return String.join("/", ids);
    }

    private String descr(String description) {
        return testName != null && testName.isBlank() ? testName + "/" + description : description;
    }

    public WindowAssertion assertApp() {
        WaitForAsyncUtils.waitForFxEvents();
        var soft = new SoftAssertions();
        {
            soft.assertThat(app.scene().getFocusOwner()).as(descr("Focus"))
                    .withRepresentation(node -> node != null ?
                            format("%s[ids=%s]", node.getClass().getSimpleName(), nodeFullId((Node) node)) : "null")
                    .isEqualTo(focus);
        }
        {
            var history = app.history();
            soft.assertThat(history.label().getText()).as(descr("History/Label/Text"))
                    .isEqualTo("Question history (" + historySizeFiltered + "/" + historySizeFull + "):");
            soft.assertThat(history.labelTemplate().getText()).as(descr("History/LabelTemplate/Text"))
                    .isEqualTo("Question history (" + historySizeFull + "/" + historySizeFull + "):");
            soft.assertThat(history.labelTemplate().isVisible()).as(descr("History/LabelTemplate/Visible")).isFalse();
            soft.assertThat(history.labelTemplate().getWidth()).as(descr("History/LabelTemplate/ReservesLabelWidth"))
                    .isGreaterThanOrEqualTo(history.label().getWidth());
            soft.assertThat(history.deleteButton().getText()).as(descr("History/DeleteButton/Text")).isEqualTo("Delete");
            soft.assertThat(history.comboBox().getItems()).as(descr("History/ComboBox/Items")).hasSize(historySizeFiltered);
            soft.assertThat(history.deleteButton().isDisabled()).as(descr("History/DeleteButton/Disabled")).isEqualTo(historyDeleteButtonDisabled);
            var historySelectedItemId = historySelectedItem != null ? historySelectedItem.id() : null;
            var cbSelectedItem = history.comboBox().getSelectionModel().getSelectedItem();
            var cbSelectedItemStr = cbSelectedItem != null ? cbSelectedItem.interaction() != null ? cbSelectedItem.interaction().toString() : null : null;
            soft.assertThat(cbSelectedItemStr).as(descr("History/ComboBox/SelectedItem"))
                    .isEqualTo(app.storage.readInteraction(historySelectedItemId).map(Interaction::toString).orElse(null));
            soft.assertThat(history.comboBox().getItems().stream().map(InteractionItem::interaction)).as(descr("History/ComboBox/Items"))
                    .containsExactlyElementsOf(historyItems);
            soft.assertThat(history.filterTextField().getText()).as(descr("History/FilterTextField/Text")).isEqualTo(historyFilterText);
            soft.assertThat(history.filterClearButton().isVisible()).as(descr("History/FilterClearButton/Visible"))
                    .isEqualTo(!historyFilterText.isEmpty());
            var expModelCurrentInteraction = modelCurrentInteraction != null ? modelCurrentInteraction : historySelectedItem;
            var expModelCurrentInteractionId = expModelCurrentInteraction != null ? expModelCurrentInteraction.id() : null;
            soft.assertThat(app.stateModel.getCurrentInteractionId()).as(descr("Model/CurrentInteractionId")).isEqualTo(expModelCurrentInteractionId);
            soft.assertThat(app.stateModel.getFilteredHistory()).as(descr("Model/History/Items")).containsExactlyElementsOf(historyItems);
        }

        {
            var topic = app.topic();
            soft.assertThat(topic.label().getText()).as(descr("Topic/Label/Text")).isEqualTo("_Topic (" + topicItems.size() + "):");
            soft.assertThat(topic.comboBox().getItems()).as(descr("Topic/ComboBox/ItemsSize")).hasSize(topicSize);
            var topicSelectedItemTitle = topicSelectedItem != null ? topicSelectedItem.title() : null;
            soft.assertThat(topic.comboBox().getSelectionModel().getSelectedItem()).as(descr("Topic/ComboBox/SelectedItem"))
                    .isEqualTo(topicSelectedItem);
            soft.assertThat(topic.comboBox().getItems()).as(descr("Topic/ComboBox/Items")).containsExactlyElementsOf(topicItems);
            soft.assertThat(topic.filterHistoryCheckBox().isSelected()).as(descr("Topic/Label/Text")).isEqualTo(filterHistorySelected);
            soft.assertThat(topic.renameButton().isDisabled()).as(descr("Topic/RenameButton/Disabled")).isEqualTo(topicRenameButtonDisabled);
            soft.assertThat(topic.deleteButton().getText()).as(descr("Topic/DeleteButton/Text")).isEqualTo("🗑");
            soft.assertThat(topic.deleteButton().isDisabled()).as(descr("Topic/DeleteButton/Disabled"))
                    .isEqualTo(topicDeleteButtonDisabled != null ? topicDeleteButtonDisabled : topicRenameButtonDisabled);
            var topicTitle = app.stateModel.getCurrentTopic() != null ? app.stateModel.getCurrentTopic().title() : null;
            soft.assertThat(topicTitle).as(descr("Topic/Model/CurrentTopic")).isEqualTo(topicSelectedItemTitle);
        }

        {
            var question = app.question();
            soft.assertThat(question.label().getText()).as(descr("Question/Label/Text")).isEqualTo("Question:");
            soft.assertThat(question.questionButton().getText()).as(descr("Question/Button/Text")).isEqualTo("_Question");
            soft.assertThat(question.definitionButton().getText()).as(descr("Definition/Button/Text")).isEqualTo("_Definition");
            soft.assertThat(question.grammarButton().getText()).as(descr("Grammar/Button/Text")).isEqualTo("_Grammar");
            soft.assertThat(question.factButton().getText()).as(descr("Fact/Button/Text")).isEqualTo("_Fact");
            soft.assertThat(question.regenerateButton().getText()).as(descr("Regenerate/Button/Text")).isEqualTo("_Resend");
            soft.assertThat(question.textArea().getText()).as(descr("Question/TextArea/Text")).isEqualTo(questionText);
            soft.assertThat(question.textArea().getStyleClass()).as(descr("Question/TextArea/Style")).contains(questionStyle);
            soft.assertThat(app.stateModel.getEditedQuestion()).as(descr("Question/Model/EditedQuestion")).isEqualTo(modelEditedQuestion);
            soft.assertThat(app.stateModel.isEnteringNewQuestion()).as(descr("Question/Model/IsEnteringNewQuestion")).isEqualTo(isEnteringNewQuestion);
        }

        for (var answerType : AnswerType.values()) {
            var label = "Answer/" + ANSWER_LABELS.get(answerType);
            var answer = app.answer(answerType);
            var expected = answers.get(answerType);
            var expectedProvider = answerProviders[answerType.ordinal()];
            var providerComboBox = answer.providerComboBox();
            soft.assertThat(providerComboBox.getValue()).as(descr(label + "/ProviderComboBox/Value")).isEqualTo(expectedProvider);
            soft.assertThat(providerComboBox.getConverter().toString(providerComboBox.getValue()))
                    .as(descr(label + "/ProviderComboBox/Text")).isEqualTo(PROVIDER_TEXTS.get(expectedProvider));
            soft.assertThat(providerComboBox.getItems()).as(descr(label + "/ProviderComboBox/Items"))
                    .containsExactly(AiProvider.values());
            soft.assertThat(answer.button().getText()).as(descr(label + "/Button/Text")).isEqualTo("ⓘ");
            soft.assertThat(answer.copyButton().getText()).as(descr(label + "/CopyButton/Text")).isEqualTo("Copy _" + ANSWER_DIGITS.get(answerType));
            soft.assertThat(answer.regenerateButton().getText()).as(descr(label + "/RegenerateButton/Text")).isEqualTo("⟳");
            soft.assertThat(answer.expandButton().getText()).as(descr(label + "/ExpandButton/Text")).isEqualTo("⛶");
            app.verifyWebViewBody(soft, descr(label + "/WebView/Body"), answer.webView(), expected.text);
            soft.assertThat(colorToString(answer.circle().getFill())).as(descr(label + "/Circle/Fill")).isEqualTo(colorToString(expected.circleColor));
        }

        {
            try {
                if (clipboard != null) {
                    soft.assertThat(Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor))
                            .as(descr("Clipboard"))
                            .isEqualTo("<html><head></head><body>" + clipboard + "</body></html>");
                }
            } catch (UnsupportedFlavorException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        soft.assertAll();
        return this;
    }

    private final Map<Paint, String> colors = Map.of(
            RED, "RED",
            BLUE, "BLUE",
            GREEN, "GREEN",
            WHITE, "WHITE"
    );

    private String colorToString(Paint color) {
        return colors.getOrDefault(color, color.toString());
    }
}

