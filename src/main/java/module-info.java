module GptUi.main {
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

    exports gptui;
    exports gptui.ui.view;
    exports gptui.ui.model;
    exports gptui.ui.model.storage;
    exports gptui.ui.model.question;
    exports gptui.core.ai.openai;
    exports gptui.core.ai.gcp;
    exports gptui.ui.model.question.question;
    exports gptui.ui.model.question.prompt;
    exports gptui.ui.model.question.sound;
    exports gptui.ui.model.config;
    exports gptui.ui.model.state;
    exports gptui.ui.model.file;
    exports gptui.ui.model.clipboard;
    exports gptui.ui.model.search;
    exports gptui.core.util;

    opens gptui.ui.model.storage to com.google.guice;
    opens gptui.ui.model.question to com.google.gson, com.google.guice;
    opens gptui.core.ai.openai to com.google.gson, com.google.guice;
    opens gptui.core.ai.gcp to com.google.gson, com.google.guice;
    opens gptui.ui.model.question.question to com.google.gson, com.google.guice;
    opens gptui.ui.model.config to com.google.guice, javafx.fxml;
    opens gptui.ui.view to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.model to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.viewmodel to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.model.state to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.model.file to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.model.clipboard to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.model.search to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.viewmodel.question to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.viewmodel.theme to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.viewmodel.history to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.viewmodel.answer to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.viewmodel.ui to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.viewmodel.uiapp to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.viewmodel.mediator to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.model.question.prompt to com.google.gson, com.google.guice;
    opens gptui.core.util to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.ui.model.question.sound to com.google.gson, com.google.guice;
    exports gptui.core.storagefilesystem;
    opens gptui.core.storagefilesystem to com.google.guice;
}
