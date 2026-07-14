package gptui.ui.viewmodel.question;

import com.google.inject.Singleton;
import gptui.core.util.Mdc;
import gptui.core.storagefilesystem.Interaction;
import gptui.core.storagefilesystem.InteractionType;
import gptui.ui.viewmodel.mediator.QuestionMediator;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static gptui.core.storagefilesystem.AnswerType.CLAUDE;
import static gptui.core.storagefilesystem.AnswerType.GCP;
import static gptui.core.storagefilesystem.AnswerType.GRAMMAR;
import static gptui.core.storagefilesystem.AnswerType.OPEN_AI;
import static gptui.core.storagefilesystem.InteractionType.DEFINITION;
import static gptui.core.storagefilesystem.InteractionType.FACT;
import static gptui.core.storagefilesystem.InteractionType.QUESTION;
import static gptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_EDITED;
import static gptui.ui.viewmodel.question.QuestionStyle.QUESTION_STYLE_FOLLOW_UP;

@Singleton
class QuestionVmImpl implements QuestionVmController, QuestionVmMediator {
    private static final Logger log = LoggerFactory.getLogger(QuestionVmImpl.class);
    private final QuestionVmProperties properties = new QuestionVmProperties();
    @Inject
    private QuestionMediator mediator;

    {
        properties.followUpCheckBoxSelected.addListener((observable, oldValue, newValue) -> updateQuestionTextAreaBackgroundColor());
    }

    @Override
    public void displayCurrentInteraction() {
        log.trace("displayCurrentInteraction");
        mediator.getCurrentInteractionOpt()
                .map(Interaction::question)
                .filter(question -> !question.equals(properties.questionTaText.getValue()))
                .ifPresent(question -> {
                    log.trace("Update question text: '{}'", question);
                    properties.questionTaText.setValue(question);
                    mediator.setEditedQuestion(question);
                    updateQuestionTextAreaBackgroundColor();
                });
    }

    @Override
    public void focusOnQuestionAndSelect() {
        log.debug("focusOnQuestion");
        properties.questionTaFocused.setValue(false);
        properties.questionTaFocused.setValue(true);
        properties.questionTaSelectAll.setValue(false);
        properties.questionTaSelectAll.setValue(true);
    }

    @Override
    public void onRegenerateButtonClick() {
        log.trace("onRegenerateButtonClick");
        var interactionId = mediator.getCurrentInteractionId();
        Mdc.run(interactionId, () -> {
            log.info("Regenerate question: {}", interactionId);
            mediator.requestAnswer(interactionId, CLAUDE);
            mediator.requestAnswer(interactionId, OPEN_AI);
            mediator.requestAnswer(interactionId, GRAMMAR);
            mediator.requestAnswer(interactionId, GCP);
        });
    }

    @Override
    public void onSendQuestionClick() {
        log.debug("onSendQuestionClick");
        createNewInteractionAndRequestAnswers(QUESTION);
    }

    @Override
    public void onSendDefinitionClick() {
        log.debug("onSendDefinitionClick");
        createNewInteractionAndRequestAnswers(DEFINITION);
    }

    @Override
    public void onSendGrammarClick() {
        log.debug("onSendGrammarClick");
        createNewInteractionAndRequestAnswers(InteractionType.GRAMMAR);
    }

    @Override
    public void onSendFactClick() {
        log.debug("onSendFactClick");
        createNewInteractionAndRequestAnswers(FACT);
    }

    @Override
    public void onKeyTypedQuestionTextArea() {
        log.trace("onKeyTypedQuestionTextArea");
        mediator.setEditedQuestion(properties.questionTaText.getValue());
        updateQuestionTextAreaBackgroundColor();
    }

    @Override
    public QuestionVmProperties properties() {
        return properties;
    }

    private void updateQuestionTextAreaBackgroundColor() {
        if (properties.followUpCheckBoxSelected.get()) {
            properties.questionTaStyle.set(QUESTION_STYLE_FOLLOW_UP);
        } else if (Boolean.TRUE.equals(mediator.isEnteringNewQuestion())) {
            properties.questionTaStyle.set(QUESTION_STYLE_EDITED);
        } else {
            properties.questionTaStyle.set(QuestionStyle.QUESTION_STYLE_EMPTY);
        }
    }

    @Override
    public void pasteQuestionFromClipboard() {
        log.debug("pasteQuestionFromClipboard");
        var question = mediator.getTextFromClipboard();
        properties.questionTaText.setValue(question);
        mediator.setEditedQuestion(question);
    }

    public synchronized void createNewInteractionAndRequestAnswers(InteractionType interactionType) {
        log.debug("createNewInteractionAndRequestAnswers: interactionType={}", interactionType);
        var parentInteractionId = properties.followUpCheckBoxSelected.get() ? mediator.getCurrentInteractionId() : null;
        var interactionId = mediator.createInteraction(interactionType, parentInteractionId);
        mediator.requestAnswer(interactionId, GCP);
        mediator.requestAnswer(interactionId, CLAUDE);
        mediator.requestAnswer(interactionId, OPEN_AI);
        mediator.requestAnswer(interactionId, GRAMMAR);
    }

    @Override
    public void toggleFollowUp() {
        log.trace("toggleFollowUp");
        properties.followUpCheckBoxSelected.set(!properties.followUpCheckBoxSelected.get());
    }
}

