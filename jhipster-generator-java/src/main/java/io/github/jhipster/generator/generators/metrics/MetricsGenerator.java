/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.metrics;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Metrics Generator.
 * Generates Micrometer/Prometheus metrics configuration.
 */
public class MetricsGenerator extends BaseApplicationGenerator {

    public MetricsGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "metrics";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingMetrics", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing metrics configuration");

        writeMetricsConfiguration();
        writeCustomMetrics();
        writeHealthIndicator();
    }

    private void writeMetricsConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "io.micrometer.core.instrument.MeterRegistry",
            "io.micrometer.core.instrument.Tags",
            "org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration"
        );

        builder.javadoc("Metrics configuration.\\nConfigures Micrometer metrics with common tags and custom metrics.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "MetricsConfiguration", null);

        // Common tags for all metrics
        builder.annotation("Bean");
        builder.methodSignature("public", "MeterRegistryCustomizer<MeterRegistry>", "metricsCommonTags");
        builder.line("return registry -> registry.config()");
        builder.indent();
        builder.line(".commonTags(Tags.of(");
        builder.indent();
        builder.line("\"application\", \"" + config.getBaseName() + "\",");
        builder.line("\"type\", \"" + config.getApplicationType() + "\"");
        builder.outdent();
        builder.line("));");
        builder.outdent();
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/MetricsConfiguration.java", builder.build());
    }

    private void writeCustomMetrics() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".service.metrics");

        builder.addImports(
            "io.micrometer.core.instrument.Counter",
            "io.micrometer.core.instrument.MeterRegistry",
            "io.micrometer.core.instrument.Timer",
            "org.springframework.stereotype.Service",
            "java.util.concurrent.TimeUnit"
        );

        builder.javadoc("Custom metrics service for application-specific metrics.");
        builder.annotation("Service");

        builder.classDeclaration("public", "CustomMetricsService", null);

        builder.field("private final", "MeterRegistry", "registry");
        builder.field("private final", "Counter", "loginCounter");
        builder.field("private final", "Counter", "loginFailureCounter");
        builder.field("private final", "Timer", "requestTimer");
        builder.line();

        builder.constructor("public", "CustomMetricsService", "MeterRegistry registry");
        builder.statement("this.registry = registry");
        builder.line();
        builder.statement("this.loginCounter = Counter.builder(\"app.logins.total\")\n" +
            "            .description(\"Total number of successful logins\")\n" +
            "            .tag(\"type\", \"success\")\n" +
            "            .register(registry)");
        builder.line();
        builder.statement("this.loginFailureCounter = Counter.builder(\"app.logins.total\")\n" +
            "            .description(\"Total number of failed logins\")\n" +
            "            .tag(\"type\", \"failure\")\n" +
            "            .register(registry)");
        builder.line();
        builder.statement("this.requestTimer = Timer.builder(\"app.request.duration\")\n" +
            "            .description(\"Request processing time\")\n" +
            "            .register(registry)");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Record a successful login.");
        builder.methodSignature("public", "void", "recordSuccessfulLogin");
        builder.statement("loginCounter.increment()");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Record a failed login attempt.");
        builder.methodSignature("public", "void", "recordFailedLogin");
        builder.statement("loginFailureCounter.increment()");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Record request duration.");
        builder.methodSignature("public", "void", "recordRequestDuration", "long durationMs");
        builder.statement("requestTimer.record(durationMs, TimeUnit.MILLISECONDS)");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Get a timer sample for measuring request duration.");
        builder.methodSignature("public", "Timer.Sample", "startTimer");
        builder.returnStatement("Timer.start(registry)");
        builder.closeMethod();
        builder.line();

        builder.javadoc("Stop the timer and record the duration.");
        builder.methodSignature("public", "void", "stopTimer", "Timer.Sample sample", "String endpoint");
        builder.statement("sample.stop(Timer.builder(\"app.endpoint.duration\")\n" +
            "            .tag(\"endpoint\", endpoint)\n" +
            "            .register(registry))");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "service/metrics/CustomMetricsService.java", builder.build());
    }

    private void writeHealthIndicator() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.boot.actuate.health.Health",
            "org.springframework.boot.actuate.health.HealthIndicator",
            "org.springframework.stereotype.Component"
        );

        builder.javadoc("Custom health indicator for application-specific health checks.");
        builder.annotation("Component");

        builder.classDeclaration("public", "ApplicationHealthIndicator", null, "HealthIndicator");

        builder.annotation("Override");
        builder.methodSignature("public", "Health", "health");
        builder.tryBlock();
        builder.lineComment("Add custom health checks here");
        builder.statement("boolean healthy = checkApplicationHealth()");
        builder.ifStatement("healthy");
        builder.returnStatement("Health.up()\n" +
            "                .withDetail(\"application\", \"" + config.getBaseName() + "\")\n" +
            "                .withDetail(\"status\", \"Running\")\n" +
            "                .build()");
        builder.elseStatement();
        builder.returnStatement("Health.down()\n" +
            "                .withDetail(\"application\", \"" + config.getBaseName() + "\")\n" +
            "                .withDetail(\"status\", \"Degraded\")\n" +
            "                .build()");
        builder.closeIf();
        builder.catchBlock("Exception e");
        builder.returnStatement("Health.down(e).build()");
        builder.closeTry();
        builder.closeMethod();
        builder.line();

        builder.methodSignature("private", "boolean", "checkApplicationHealth");
        builder.lineComment("Implement custom health check logic");
        builder.returnStatement("true");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/ApplicationHealthIndicator.java", builder.build());
    }
}
