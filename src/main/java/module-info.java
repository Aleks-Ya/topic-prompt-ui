module GptUi.main {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.media;

    requires java.net.http;
    requires jakarta.inject;

    requires org.slf4j;
    requires jul.to.slf4j;
    requires ch.qos.logback.classic;
    requires java.naming;
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
    exports gptui.view;
    exports gptui.model;
    exports gptui.model.storage;
    exports gptui.model.question;
    exports gptui.model.question.openai;
    exports gptui.model.question.gcp;
    exports gptui.model.question.question;
    exports gptui.model.question.openai.responses;
    exports gptui.model.question.prompt;
    exports gptui.model.question.sound;
    exports gptui.model.config;
    exports gptui.model.state;
    exports gptui.model.file;
    exports gptui.model.clipboard;
    exports gptui.model.search;
    exports gptui.util;

    opens gptui.model.storage to com.google.guice;
    opens gptui.model.question to com.google.gson, com.google.guice;
    opens gptui.model.question.openai to com.google.gson, com.google.guice;
    opens gptui.model.question.gcp to com.google.gson, com.google.guice;
    opens gptui.model.question.question to com.google.gson, com.google.guice;
    opens gptui.model.config to com.google.guice, javafx.fxml;
    opens gptui.view to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.model to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.viewmodel to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.model.state to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.model.file to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.model.clipboard to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.model.search to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.viewmodel.question to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.viewmodel.theme to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.viewmodel.history to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.viewmodel.answer to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.viewmodel.ui to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.viewmodel.uiapp to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.viewmodel.mediator to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.model.question.openai.responses to com.google.gson, com.google.guice;
    opens gptui.model.question.prompt to com.google.gson, com.google.guice;
    opens gptui.util to com.google.gson, com.google.guice, javafx.fxml;
    opens gptui.model.question.sound to com.google.gson, com.google.guice;
}
