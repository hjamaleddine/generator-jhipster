/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.websocket;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Spring WebSocket Generator.
 * Generates WebSocket configuration for real-time communication.
 */
public class SpringWebSocketGenerator extends BaseApplicationGenerator {

    public SpringWebSocketGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "spring-websocket";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingWebSocket", this::writing);
    }

    private void writing() throws Exception {
        JHipsterConfig config = getConfig();

        if (!"spring-websocket".equals(config.getWebsocket())) {
            log.info("WebSocket not configured, skipping");
            return;
        }

        log.info("Writing WebSocket configuration");

        writeWebSocketConfiguration();
        writeWebSocketSecurityConfiguration();
        writeActivityService();
        writeActivityDTO();
    }

    private void writeWebSocketConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.context.annotation.Configuration",
            "org.springframework.messaging.simp.config.MessageBrokerRegistry",
            "org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker",
            "org.springframework.web.socket.config.annotation.StompEndpointRegistry",
            "org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer"
        );

        builder.javadoc("WebSocket configuration.\\nConfigures STOMP WebSocket endpoints and message broker.");
        builder.annotation("Configuration");
        builder.annotation("EnableWebSocketMessageBroker");

        builder.classDeclaration("public", "WebSocketConfiguration", null, "WebSocketMessageBrokerConfigurer");

        builder.annotation("Override");
        builder.methodSignature("public", "void", "configureMessageBroker", "MessageBrokerRegistry config");
        builder.statement("config.enableSimpleBroker(\"/topic\")");
        builder.statement("config.setApplicationDestinationPrefixes(\"/app\")");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "registerStompEndpoints", "StompEndpointRegistry registry");
        builder.statement("registry.addEndpoint(\"/websocket/tracker\")\n" +
            "            .setAllowedOriginPatterns(\"*\")\n" +
            "            .withSockJS()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/WebSocketConfiguration.java", builder.build());
    }

    private void writeWebSocketSecurityConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.context.annotation.Configuration",
            "org.springframework.messaging.simp.SimpMessageType",
            "org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry",
            "org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer"
        );

        builder.javadoc("WebSocket security configuration.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "WebSocketSecurityConfiguration",
            "AbstractSecurityWebSocketMessageBrokerConfigurer");

        builder.annotation("Override");
        builder.methodSignature("protected", "void", "configureInbound",
            "MessageSecurityMetadataSourceRegistry messages");
        builder.statement("messages\n" +
            "            .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT, SimpMessageType.OTHER).permitAll()\n" +
            "            .simpDestMatchers(\"/topic/tracker\").hasAuthority(\"ROLE_ADMIN\")\n" +
            "            .simpDestMatchers(\"/topic/**\").authenticated()\n" +
            "            .anyMessage().authenticated()");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("protected", "boolean", "sameOriginDisabled");
        builder.returnStatement("true");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/WebSocketSecurityConfiguration.java", builder.build());
    }

    private void writeActivityService() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service");

        builder.addImports(
            config.getPackageName() + ".service.dto.ActivityDTO",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.messaging.simp.SimpMessageSendingOperations",
            "org.springframework.stereotype.Service"
        );

        builder.javadoc("Service for sending WebSocket activity updates.");
        builder.annotation("Service");

        builder.classDeclaration("public", "ActivityService", null);

        builder.field("private final", "Logger", "log", "LoggerFactory.getLogger(ActivityService.class)");
        builder.field("private final", "SimpMessageSendingOperations", "messagingTemplate");
        builder.line();

        builder.constructor("public", "ActivityService", "SimpMessageSendingOperations messagingTemplate");
        builder.statement("this.messagingTemplate = messagingTemplate");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Send activity to all subscribers.");
        builder.methodSignature("public", "void", "sendActivity", "ActivityDTO activity");
        builder.statement("log.debug(\"Sending activity: {}\", activity)");
        builder.statement("messagingTemplate.convertAndSend(\"/topic/tracker\", activity)");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/ActivityService.java", builder.build());
    }

    private void writeActivityDTO() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service.dto");

        builder.addImports("java.time.Instant");

        builder.javadoc("DTO for tracking user activity.");

        builder.classDeclaration("public", "ActivityDTO", null);

        builder.field("private", "String", "sessionId");
        builder.field("private", "String", "userLogin");
        builder.field("private", "String", "ipAddress");
        builder.field("private", "String", "page");
        builder.field("private", "Instant", "time");
        builder.line();

        // Getters and setters
        String[] fields = {"sessionId", "userLogin", "ipAddress", "page"};
        for (String field : fields) {
            String capitalized = capitalize(field);
            builder.methodSignature("public", "String", "get" + capitalized);
            builder.returnStatement(field);
            builder.closeMethod();
            builder.line();

            builder.methodSignature("public", "void", "set" + capitalized, "String " + field);
            builder.statement("this." + field + " = " + field);
            builder.closeMethod();
            builder.line();
        }

        builder.methodSignature("public", "Instant", "getTime");
        builder.returnStatement("time");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setTime", "Instant time");
        builder.statement("this.time = time");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"ActivityDTO{\" +\n" +
            "                \"sessionId='\" + sessionId + '\\'' +\n" +
            "                \", userLogin='\" + userLogin + '\\'' +\n" +
            "                \", ipAddress='\" + ipAddress + '\\'' +\n" +
            "                \", page='\" + page + '\\'' +\n" +
            "                \", time=\" + time +\n" +
            "                '}'");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/dto/ActivityDTO.java", builder.build());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
