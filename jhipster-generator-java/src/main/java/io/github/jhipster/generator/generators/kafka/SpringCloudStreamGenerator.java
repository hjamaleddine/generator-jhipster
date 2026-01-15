/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.kafka;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Spring Cloud Stream Generator.
 * Generates Kafka/Pulsar messaging configuration.
 */
public class SpringCloudStreamGenerator extends BaseApplicationGenerator {

    public SpringCloudStreamGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "spring-cloud-stream";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingSpringCloudStream", this::writing);
    }

    private void writing() throws Exception {
        JHipsterConfig config = getConfig();
        String messageBroker = config.getMessageBroker();

        if (messageBroker == null || "false".equals(messageBroker) || "no".equals(messageBroker)) {
            log.info("No message broker configured, skipping Spring Cloud Stream generation");
            return;
        }

        log.info("Writing Spring Cloud Stream configuration for {}", messageBroker);

        writeKafkaConfiguration();
        writeKafkaConsumer();
        writeKafkaProducer();
        writeKafkaResource();
        writeKafkaYaml();
    }

    private void writeKafkaConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.apache.kafka.clients.admin.NewTopic",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.kafka.config.TopicBuilder"
        );

        builder.javadoc("Kafka configuration.\\nConfigures Kafka topics and bindings.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "KafkaConfiguration", null);

        builder.staticFinalField("String", "TOPIC_NAME", "\"" + config.getLowerBaseName() + "-topic\"");
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "NewTopic", "topic");
        builder.line("return TopicBuilder.name(TOPIC_NAME)");
        builder.indent();
        builder.line(".partitions(3)");
        builder.line(".replicas(1)");
        builder.line(".build();");
        builder.outdent();
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/KafkaConfiguration.java", builder.build());
    }

    private void writeKafkaConsumer() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service.kafka");

        builder.addImports(
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.kafka.annotation.KafkaListener",
            "org.springframework.stereotype.Service"
        );

        builder.javadoc("Kafka consumer service.");
        builder.annotation("Service");

        builder.classDeclaration("public", "KafkaConsumer", null);

        builder.field("private final", "Logger", "log", "LoggerFactory.getLogger(KafkaConsumer.class)");
        builder.line();

        builder.annotation("KafkaListener", "topics = \"${spring.application.name}-topic\", groupId = \"${spring.application.name}-group\"");
        builder.methodSignature("public", "void", "consume", "String message");
        builder.statement("log.debug(\"Consumed message: {}\", message)");
        builder.lineComment("Process the message here");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/kafka/KafkaConsumer.java", builder.build());
    }

    private void writeKafkaProducer() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service.kafka");

        builder.addImports(
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.beans.factory.annotation.Value",
            "org.springframework.kafka.core.KafkaTemplate",
            "org.springframework.stereotype.Service"
        );

        builder.javadoc("Kafka producer service.");
        builder.annotation("Service");

        builder.classDeclaration("public", "KafkaProducer", null);

        builder.field("private final", "Logger", "log", "LoggerFactory.getLogger(KafkaProducer.class)");
        builder.line();

        builder.annotation("Value", "\"${spring.application.name}-topic\"");
        builder.field("private", "String", "topicName");
        builder.line();

        builder.field("private final", "KafkaTemplate<String, String>", "kafkaTemplate");
        builder.line();

        builder.constructor("public", "KafkaProducer", "KafkaTemplate<String, String> kafkaTemplate");
        builder.statement("this.kafkaTemplate = kafkaTemplate");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Send a message to the default topic.");
        builder.methodSignature("public", "void", "send", "String message");
        builder.statement("log.debug(\"Sending message to topic {}: {}\", topicName, message)");
        builder.statement("kafkaTemplate.send(topicName, message)");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Send a message with a key to the default topic.");
        builder.methodSignature("public", "void", "send", "String key", "String message");
        builder.statement("log.debug(\"Sending message with key {} to topic {}: {}\", key, topicName, message)");
        builder.statement("kafkaTemplate.send(topicName, key, message)");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/kafka/KafkaProducer.java", builder.build());
    }

    private void writeKafkaResource() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest");

        builder.addImports(
            config.getPackageName() + ".service.kafka.KafkaProducer",
            "org.springframework.http.ResponseEntity",
            "org.springframework.web.bind.annotation.*"
        );

        builder.javadoc("REST controller for Kafka operations.");
        builder.annotation("RestController");
        builder.annotation("RequestMapping", "\"/api/kafka\"");

        builder.classDeclaration("public", "KafkaResource", null);

        builder.field("private final", "KafkaProducer", "kafkaProducer");
        builder.line();

        builder.constructor("public", "KafkaResource", "KafkaProducer kafkaProducer");
        builder.statement("this.kafkaProducer = kafkaProducer");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Send a message to Kafka.");
        builder.annotation("PostMapping", "\"/publish\"");
        builder.methodSignature("public", "ResponseEntity<Void>", "publish", "@RequestBody String message");
        builder.statement("kafkaProducer.send(message)");
        builder.returnStatement("ResponseEntity.ok().build()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/KafkaResource.java", builder.build());
    }

    private void writeKafkaYaml() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("# ===================================================================\n");
        yml.append("# Kafka configuration\n");
        yml.append("# ===================================================================\n\n");

        yml.append("spring:\n");
        yml.append("  kafka:\n");
        yml.append("    bootstrap-servers: localhost:9092\n");
        yml.append("    consumer:\n");
        yml.append("      group-id: ").append(config.getLowerBaseName()).append("-group\n");
        yml.append("      auto-offset-reset: earliest\n");
        yml.append("      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer\n");
        yml.append("      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer\n");
        yml.append("    producer:\n");
        yml.append("      key-serializer: org.apache.kafka.common.serialization.StringSerializer\n");
        yml.append("      value-serializer: org.apache.kafka.common.serialization.StringSerializer\n");

        writeFile(getMainResourcesPath() + "config/kafka.yml", yml.toString());
    }

    private String getMainResourcesPath() {
        return context.getBasePath().toString() + "/src/main/resources/";
    }
}
