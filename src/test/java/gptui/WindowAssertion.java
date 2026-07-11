package gptui;

import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.Theme;
import gptui.ui.viewmodel.InteractionItem;
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
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.WHITE;

public class WindowAssertion {
    private BaseGptUiTest app;
    private Node focus;
    private int historySizeFiltered;
    private int historySizeFull;
    private boolean historyDeleteButtonDisabled;
    private Interaction historySelectedItem;
    private List<Interaction> historyItems;
    private int themeSize;
    private Theme themeSelectedItem;
    private List<Theme> themeItems;
    private Boolean filterHistorySelected;
    private String questionText;
    private String questionStyle;
    private Boolean isEnteringNewQuestion;
    private String modelEditedQuestion;
    private final AnswerInfo grammarAnswer = new AnswerInfo();
    private final AnswerInfo openAiAnswer = new AnswerInfo();
    private final AnswerInfo claudeAnswer = new AnswerInfo();
    private final AnswerInfo gcpAnswer = new AnswerInfo();
    private String testName = "Initialize";
    private String clipboard = null;

    public class AnswerInfo {
        private String text;
        private Color circleColor;
        private Integer temperatureText;
        private Integer temperatureSpinner;

        public WindowAssertion text(String text) {
            this.text = text;
            return WindowAssertion.this;
        }

        public void circleColor(Color circleColor) {
            this.circleColor = circleColor;
        }

        public void temperatureText(Integer temperature) {
            this.temperatureText = temperature;
        }

        public void temperatureSpinner(Integer temperature) {
            this.temperatureSpinner = temperature;
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

    public WindowAssertion app(BaseGptUiTest app) {
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

    public WindowAssertion historyItems(List<Interaction> historyItems) {
        this.historyItems = historyItems;
        return this;
    }

    public WindowAssertion historyItems(Interaction... historyItems) {
        this.historyItems = Arrays.asList(historyItems);
        return this;
    }

    public WindowAssertion themeSize(int themeSize) {
        this.themeSize = themeSize;
        return this;
    }

    public WindowAssertion themeSelectedItem(Theme themeSelectedItem) {
        this.themeSelectedItem = themeSelectedItem;
        return this;
    }

    public WindowAssertion themeItems(Theme... themeItems) {
        this.themeItems = Arrays.asList(themeItems);
        return this;
    }

    public WindowAssertion themeFilterHistorySelected(Boolean filterHistorySelected) {
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
        return grammarAnswer;
    }

    public AnswerInfo openAiA() {
        return openAiAnswer;
    }

    public AnswerInfo claudeA() {
        return claudeAnswer;
    }

    public AnswerInfo gcpA() {
        return gcpAnswer;
    }

    public WindowAssertion answerCircleColors(Color answerGrammarCircleColor, Color answerOpenAiCircleColor,
                                              Color answerClaudeCircleColor, Color answerGcpCircleColor) {
        grammarA().circleColor(answerGrammarCircleColor);
        openAiA().circleColor(answerOpenAiCircleColor);
        claudeA().circleColor(answerClaudeCircleColor);
        gcpA().circleColor(answerGcpCircleColor);
        return this;
    }

    public WindowAssertion answerTextTemperatures(Integer grammarTemperature, Integer openAiTemperature,
                                                  Integer claudeTemperature, Integer gcpTemperature) {
        grammarA().temperatureText(grammarTemperature);
        openAiA().temperatureText(openAiTemperature);
        claudeA().temperatureText(claudeTemperature);
        gcpA().temperatureText(gcpTemperature);
        return this;
    }

    public WindowAssertion answerSpinnerTemperatures(Integer grammarTemperature, Integer openAiTemperature,
                                                     Integer claudeTemperature, Integer gcpTemperature) {
        grammarA().temperatureSpinner(grammarTemperature);
        openAiA().temperatureSpinner(openAiTemperature);
        claudeA().temperatureSpinner(claudeTemperature);
        gcpA().temperatureSpinner(gcpTemperature);
        return this;
    }

    public WindowAssertion answerTextTemperaturesAllEmpty() {
        return answerTextTemperatures(null,
                null, null, null);
    }

    public WindowAssertion answerTextTemperaturesDefault() {
        return answerTextTemperatures(50, 60, 70, 100);
    }

    public WindowAssertion answerSpinnerTemperaturesDefault() {
        return answerSpinnerTemperatures(50, 60, 70, 100);
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
            soft.assertThat(app.stateModel.getCurrentInteractionId()).as(descr("Model/CurrentInteractionId")).isEqualTo(historySelectedItemId);
            soft.assertThat(app.stateModel.getFilteredHistory()).as(descr("Model/History/Items")).containsExactlyElementsOf(historyItems);
        }

        {
            var theme = app.theme();
            soft.assertThat(theme.label().getText()).as(descr("Theme/Label/Text")).isEqualTo("_Theme (" + themeItems.size() + "):");
            soft.assertThat(theme.comboBox().getItems()).as(descr("Theme/ComboBox/ItemsSize")).hasSize(themeSize);
            var themeSelectedItemTitle = themeSelectedItem != null ? themeSelectedItem.title() : null;
            soft.assertThat(theme.comboBox().getSelectionModel().getSelectedItem()).as(descr("Theme/ComboBox/SelectedItem"))
                    .isEqualTo(themeSelectedItem);
            soft.assertThat(theme.comboBox().getItems()).as(descr("Theme/ComboBox/Items")).containsExactlyElementsOf(themeItems);
            soft.assertThat(theme.filterHistoryCheckBox().isSelected()).as(descr("Theme/Label/Text")).isEqualTo(filterHistorySelected);
            var themeTitle = app.stateModel.getCurrentTheme() != null ? app.stateModel.getCurrentTheme().title() : null;
            soft.assertThat(themeTitle).as(descr("Theme/Model/CurrentTheme")).isEqualTo(themeSelectedItemTitle);
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
            soft.assertThat(question.textArea().getStyle()).as(descr("Question/TextArea/Style")).isEqualTo(questionStyle);
            soft.assertThat(app.stateModel.getEditedQuestion()).as(descr("Question/Model/EditedQuestion")).isEqualTo(modelEditedQuestion);
            soft.assertThat(app.stateModel.isEnteringNewQuestion()).as(descr("Question/Model/IsEnteringNewQuestion")).isEqualTo(isEnteringNewQuestion);
        }

        {
            var answer = app.grammarAnswer();
            soft.assertThat(answer.label().getText()).as(descr("Answer/Grammar/Label/Text")).isEqualTo("Grammar\nanswer:");
            soft.assertThat(answer.copyButton().getText()).as(descr("Answer/Grammar/CopyButton/Text")).isEqualTo("Copy _1");
            soft.assertThat(answer.regenerateButton().getText()).as(descr("Answer/Grammar/RegenerateButton/Text")).isEqualTo("⟳");
            app.verifyWebViewBody(soft, descr("Answer/Grammar/WebView/Body"), answer.webView(), grammarAnswer.text);
            soft.assertThat(colorToString(answer.circle().getFill())).as(descr("Answer/Grammar/Circle/Fill")).isEqualTo(colorToString(grammarAnswer.circleColor));
            soft.assertThat(answer.temperatureText().getText()).as(descr("Answer/Grammar/Temperature/Text"))
                    .isEqualTo(temperatureToString(grammarA().temperatureText));
            soft.assertThat(answer.temperatureSpinner().getValue()).as(descr("Answer/Grammar/TemperatureSpinner/Value"))
                    .isEqualTo(temperatureToInteger(grammarA().temperatureSpinner));
        }

        {
            var answer = app.openAiAnswer();
            soft.assertThat(answer.label().getText()).as(descr("Answer/OpenAI/Label/Text")).isEqualTo("OpenAI\nanswer:");
            soft.assertThat(answer.copyButton().getText()).as(descr("Answer/OpenAI/CopyButton/Text")).isEqualTo("Copy _2");
            soft.assertThat(answer.regenerateButton().getText()).as(descr("Answer/OpenAI/RegenerateButton/Text")).isEqualTo("⟳");
            app.verifyWebViewBody(soft, descr("Answer/OpenAI/WebView/Body"), answer.webView(), openAiA().text);
            soft.assertThat(colorToString(answer.circle().getFill())).as(descr("Answer/OpenAI/Circle/Fill")).isEqualTo(colorToString(openAiA().circleColor));
            soft.assertThat(answer.temperatureText().getText()).as(descr("Answer/OpenAI/Temperature/Text"))
                    .isEqualTo(temperatureToString(openAiA().temperatureText));
            soft.assertThat(answer.temperatureSpinner().getValue()).as(descr("Answer/OpenAI/TemperatureSpinner/Value"))
                    .isEqualTo(temperatureToInteger(openAiA().temperatureSpinner));
        }

        {
            var answer = app.claudeAnswer();
            soft.assertThat(answer.label().getText()).as(descr("Answer/Claude/Label/Text")).isEqualTo("Claude\nanswer:");
            soft.assertThat(answer.copyButton().getText()).as(descr("Answer/Claude/CopyButton/Text")).isEqualTo("Copy _3");
            soft.assertThat(answer.regenerateButton().getText()).as(descr("Answer/Claude/RegenerateButton/Text")).isEqualTo("⟳");
            app.verifyWebViewBody(soft, descr("Answer/Claude/WebView/Body"), answer.webView(), claudeA().text);
            soft.assertThat(colorToString(answer.circle().getFill())).as(descr("Answer/Claude/Circle/Fill")).isEqualTo(colorToString(claudeA().circleColor));
            soft.assertThat(answer.temperatureText().getText()).as(descr("Answer/Claude/Temperature/Text"))
                    .isEqualTo(temperatureToString(claudeA().temperatureText));
            soft.assertThat(answer.temperatureSpinner().getValue()).as(descr("Answer/Claude/TemperatureSpinner/Value"))
                    .isEqualTo(temperatureToInteger(claudeA().temperatureSpinner));
        }

        {
            var answer = app.gcpAnswer();
            soft.assertThat(answer.label().getText()).as(descr("Answer/GCP/Label/Text")).isEqualTo("GCP\nanswer:");
            soft.assertThat(answer.copyButton().getText()).as(descr("Answer/GCP/CopyButton/Text")).isEqualTo("Copy _4");
            soft.assertThat(answer.regenerateButton().getText()).as(descr("Answer/GCP/RegenerateButton/Text")).isEqualTo("⟳");
            app.verifyWebViewBody(soft, descr("Answer/GCP/WebView/Body"), answer.webView(), gcpA().text);
            soft.assertThat(colorToString(answer.circle().getFill())).as(descr("Answer/GCP/Circle/Fill")).isEqualTo(colorToString(gcpA().circleColor));
            soft.assertThat(answer.temperatureText().getText()).as(descr("Answer/GCP/Temperature/Text"))
                    .isEqualTo(temperatureToString(gcpA().temperatureText));
            soft.assertThat(answer.temperatureSpinner().getValue()).as(descr("Answer/GCP/TemperatureSpinner/Value"))
                    .isEqualTo(temperatureToInteger(gcpA().temperatureSpinner));
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

    private String temperatureToString(Integer temperature) {
        return temperature != null ? temperature + "°" : "";
    }

    private Integer temperatureToInteger(Integer temperature) {
        return temperature != null ? temperature : 0;
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

