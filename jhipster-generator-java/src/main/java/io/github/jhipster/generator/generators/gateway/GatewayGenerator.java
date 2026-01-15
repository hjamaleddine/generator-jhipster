/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.gateway;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Gateway Generator.
 * Generates API Gateway configuration for Spring Cloud Gateway.
 */
public class GatewayGenerator extends BaseApplicationGenerator {

    public GatewayGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "gateway";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingGateway", this::writing);
    }

    private void writing() throws Exception {
        JHipsterConfig config = getConfig();

        if (!"gateway".equals(config.getApplicationType())) {
            log.info("Not a gateway application, skipping gateway generation");
            return;
        }

        log.info("Writing Gateway configuration");

        writeGatewayConfiguration();
        writeRouteConfiguration();
        writeGatewayFilters();
        writeGatewayYaml();
    }

    private void writeGatewayConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.cloud.gateway.route.RouteLocator",
            "org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration"
        );

        builder.javadoc("Gateway configuration.\\nConfigures Spring Cloud Gateway routes and filters.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "GatewayConfiguration", null);

        builder.annotation("Bean");
        builder.methodSignature("public", "RouteLocator", "customRouteLocator", "RouteLocatorBuilder builder");
        builder.line("return builder.routes()");
        builder.indent();
        builder.lineComment("Add route configurations here");
        builder.lineComment("Example:");
        builder.lineComment(".route(\"example-service\", r -> r");
        builder.lineComment("    .path(\"/services/example/**\")");
        builder.lineComment("    .filters(f -> f.stripPrefix(2))");
        builder.lineComment("    .uri(\"lb://example-service\"))");
        builder.line(".build();");
        builder.outdent();
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/GatewayConfiguration.java", builder.build());
    }

    private void writeRouteConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".gateway");

        builder.addImports(
            "org.springframework.cloud.gateway.filter.GatewayFilter",
            "org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory",
            "org.springframework.http.server.reactive.ServerHttpRequest",
            "org.springframework.stereotype.Component"
        );

        builder.javadoc("Gateway filter for adding custom headers to requests.");
        builder.annotation("Component");

        builder.classDeclaration("public", "AddRequestHeaderGatewayFilterFactory",
            "AbstractGatewayFilterFactory<AddRequestHeaderGatewayFilterFactory.Config>");

        builder.constructor("public", "AddRequestHeaderGatewayFilterFactory");
        builder.statement("super(Config.class)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "GatewayFilter", "apply", "Config config");
        builder.line("return (exchange, chain) -> {");
        builder.indent();
        builder.statement("ServerHttpRequest request = exchange.getRequest().mutate()\n" +
            "                .header(config.getHeaderName(), config.getHeaderValue())\n" +
            "                .build()");
        builder.returnStatement("chain.filter(exchange.mutate().request(request).build())");
        builder.outdent();
        builder.line("};");
        builder.closeMethod();
        builder.line();

        // Config inner class
        builder.line("public static class Config {");
        builder.indent();
        builder.field("private", "String", "headerName");
        builder.field("private", "String", "headerValue");
        builder.line();

        builder.methodSignature("public", "String", "getHeaderName");
        builder.returnStatement("headerName");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setHeaderName", "String headerName");
        builder.statement("this.headerName = headerName");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getHeaderValue");
        builder.returnStatement("headerValue");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setHeaderValue", "String headerValue");
        builder.statement("this.headerValue = headerValue");
        builder.closeMethod();

        builder.outdent();
        builder.line("}");

        builder.closeClass();

        writeFile(getMainJavaPath() + "gateway/AddRequestHeaderGatewayFilterFactory.java", builder.build());
    }

    private void writeGatewayFilters() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".gateway");

        builder.addImports(
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.cloud.gateway.filter.GatewayFilterChain",
            "org.springframework.cloud.gateway.filter.GlobalFilter",
            "org.springframework.core.Ordered",
            "org.springframework.stereotype.Component",
            "org.springframework.web.server.ServerWebExchange",
            "reactor.core.publisher.Mono"
        );

        builder.javadoc("Global gateway filter for logging and monitoring.");
        builder.annotation("Component");

        builder.classDeclaration("public", "LoggingGlobalFilter", null, "GlobalFilter, Ordered");

        builder.field("private final", "Logger", "log", "LoggerFactory.getLogger(LoggingGlobalFilter.class)");
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "Mono<Void>", "filter", "ServerWebExchange exchange", "GatewayFilterChain chain");
        builder.statement("long startTime = System.currentTimeMillis()");
        builder.statement("String path = exchange.getRequest().getPath().value()");
        builder.statement("String method = exchange.getRequest().getMethod().name()");
        builder.line();
        builder.statement("log.debug(\"Gateway request: {} {}\", method, path)");
        builder.line();
        builder.line("return chain.filter(exchange).then(Mono.fromRunnable(() -> {");
        builder.indent();
        builder.statement("long duration = System.currentTimeMillis() - startTime");
        builder.statement("log.debug(\"Gateway response: {} {} - {} ms\", method, path, duration)");
        builder.outdent();
        builder.line("}));");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "int", "getOrder");
        builder.returnStatement("Ordered.LOWEST_PRECEDENCE");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "gateway/LoggingGlobalFilter.java", builder.build());
    }

    private void writeGatewayYaml() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("# ===================================================================\n");
        yml.append("# Spring Cloud Gateway configuration\n");
        yml.append("# ===================================================================\n\n");

        yml.append("spring:\n");
        yml.append("  cloud:\n");
        yml.append("    gateway:\n");
        yml.append("      discovery:\n");
        yml.append("        locator:\n");
        yml.append("          enabled: true\n");
        yml.append("          lower-case-service-id: true\n\n");

        yml.append("      default-filters:\n");
        yml.append("        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin\n\n");

        yml.append("      routes:\n");
        yml.append("        # Add your service routes here\n");
        yml.append("        # Example:\n");
        yml.append("        # - id: example-service\n");
        yml.append("        #   uri: lb://example-service\n");
        yml.append("        #   predicates:\n");
        yml.append("        #     - Path=/services/example/**\n");
        yml.append("        #   filters:\n");
        yml.append("        #     - StripPrefix=2\n");

        writeFile(getMainResourcesPath() + "config/gateway.yml", yml.toString());
    }

    private String getMainResourcesPath() {
        return context.getBasePath().toString() + "/src/main/resources/";
    }
}
