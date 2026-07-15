module TopicPromptUi.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.media;

    requires java.net.http;
    requires jakarta.inject;

    requires org.slf4j;
    requires com.google.gson;
    requires flexmark;
    requires flexmark.util.ast;
    requires flexmark.util.data;
    requires flexmark.ext.tables;

    requires com.google.guice;
    requires ignite.guice;
    requires org.apache.lucene.core;
    requires org.apache.lucene.analysis.common;
    requires org.apache.lucene.queryparser;
    requires org.controlsfx.controls;
    requires com.google.common;
    requires freemarker;

    exports topicpromptui;
    exports topicpromptui.ui.view;
    exports topicpromptui.ui.model;
    exports topicpromptui.ui.model.storage;
    exports topicpromptui.ui.model.question;
    exports topicpromptui.core.ai.openai;
    exports topicpromptui.core.ai.gcp;
    exports topicpromptui.core.ai.claude;
    exports topicpromptui.ui.model.question.question;
    exports topicpromptui.ui.model.question.prompt;
    exports topicpromptui.ui.model.question.sound;
    exports topicpromptui.ui.model.state;
    exports topicpromptui.ui.model.file;
    exports topicpromptui.ui.model.clipboard;
    exports topicpromptui.ui.model.search;
    exports topicpromptui.core.util;

    opens topicpromptui.ui.model.storage to com.google.guice;
    opens topicpromptui.ui.model.question to com.google.gson, com.google.guice;
    opens topicpromptui.core.ai.openai to com.google.gson, com.google.guice;
    opens topicpromptui.core.ai.gcp to com.google.gson, com.google.guice;
    opens topicpromptui.core.ai.claude to com.google.gson, com.google.guice;
    opens topicpromptui.ui.model.question.question to com.google.gson, com.google.guice;
    opens topicpromptui.ui.view to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.model to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.viewmodel to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.model.state to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.model.file to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.model.clipboard to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.model.search to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.viewmodel.question to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.viewmodel.topic to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.viewmodel.history to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.viewmodel.answer to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.viewmodel.ui to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.viewmodel.uiapp to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.viewmodel.mediator to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.model.question.prompt to com.google.gson, com.google.guice;
    opens topicpromptui.core.util to com.google.gson, com.google.guice, javafx.fxml;
    opens topicpromptui.ui.model.question.sound to com.google.gson, com.google.guice;
    exports topicpromptui.core.storagefilesystem;
    opens topicpromptui.core.storagefilesystem to com.google.guice;
    exports topicpromptui.core.config;
    opens topicpromptui.core.config to com.google.guice;
}
