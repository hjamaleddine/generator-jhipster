/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.swagger;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Swagger/OpenAPI Generator.
 * Generates OpenAPI documentation configuration.
 */
public class SwaggerGenerator extends BaseApplicationGenerator {

    public SwaggerGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "swagger";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingSwagger", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing Swagger/OpenAPI configuration");

        writeOpenApiConfiguration();
        writeSwaggerResource();
    }

    private void writeOpenApiConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "io.swagger.v3.oas.models.OpenAPI",
            "io.swagger.v3.oas.models.info.Contact",
            "io.swagger.v3.oas.models.info.Info",
            "io.swagger.v3.oas.models.info.License",
            "io.swagger.v3.oas.models.servers.Server",
            "org.springdoc.core.models.GroupedOpenApi",
            "org.springframework.beans.factory.annotation.Value",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "java.util.List"
        );

        if (config.isJwt() || config.isOauth2()) {
            builder.addImports(
                "io.swagger.v3.oas.models.Components",
                "io.swagger.v3.oas.models.security.SecurityRequirement",
                "io.swagger.v3.oas.models.security.SecurityScheme"
            );
        }

        builder.javadoc("OpenAPI/Swagger configuration.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "OpenApiConfiguration", null);

        builder.annotation("Value", "\"${spring.application.name}\"");
        builder.field("private", "String", "applicationName");
        builder.line();

        // Main OpenAPI bean
        builder.annotation("Bean");
        builder.methodSignature("public", "OpenAPI", "customOpenAPI");

        if (config.isJwt()) {
            builder.statement("final String securitySchemeName = \"bearerAuth\"");
        }

        builder.line("return new OpenAPI()");
        builder.indent();

        if (config.isJwt()) {
            builder.line(".addSecurityItem(new SecurityRequirement().addList(securitySchemeName))");
            builder.line(".components(new Components()");
            builder.indent();
            builder.line(".addSecuritySchemes(securitySchemeName,");
            builder.indent();
            builder.line("new SecurityScheme()");
            builder.indent();
            builder.line(".name(securitySchemeName)");
            builder.line(".type(SecurityScheme.Type.HTTP)");
            builder.line(".scheme(\"bearer\")");
            builder.line(".bearerFormat(\"JWT\")");
            builder.line(".description(\"JWT Authorization header using the Bearer scheme.\")");
            builder.outdent();
            builder.line(")");
            builder.outdent();
            builder.line(")");
            builder.outdent();
        } else if (config.isOauth2()) {
            builder.line(".addSecurityItem(new SecurityRequirement().addList(\"oauth2\"))");
            builder.line(".components(new Components()");
            builder.indent();
            builder.line(".addSecuritySchemes(\"oauth2\",");
            builder.indent();
            builder.line("new SecurityScheme()");
            builder.indent();
            builder.line(".type(SecurityScheme.Type.OAUTH2)");
            builder.line(".description(\"OAuth2 Authorization\")");
            builder.outdent();
            builder.line(")");
            builder.outdent();
            builder.line(")");
            builder.outdent();
        }

        builder.line(".info(new Info()");
        builder.indent();
        builder.line(".title(\"" + config.getBaseName() + " API\")");
        builder.line(".description(\"" + config.getBaseName() + " API documentation\")");
        builder.line(".version(\"0.0.1\")");
        builder.line(".contact(new Contact()");
        builder.indent();
        builder.line(".name(\"JHipster\")");
        builder.line(".url(\"https://www.jhipster.tech\")");
        builder.line(".email(\"contact@jhipster.tech\"))");
        builder.outdent();
        builder.line(".license(new License()");
        builder.indent();
        builder.line(".name(\"Apache 2.0\")");
        builder.line(".url(\"https://www.apache.org/licenses/LICENSE-2.0.html\"))");
        builder.outdent();
        builder.outdent();
        builder.line(")");
        builder.line(".servers(List.of(");
        builder.indent();
        builder.line("new Server().url(\"/\").description(\"Default server\")");
        builder.outdent();
        builder.line("));");
        builder.outdent();

        builder.closeMethod();
        builder.line();

        // API group for main APIs
        builder.annotation("Bean");
        builder.methodSignature("public", "GroupedOpenApi", "apiGroup");
        builder.line("return GroupedOpenApi.builder()");
        builder.indent();
        builder.line(".group(\"api\")");
        builder.line(".pathsToMatch(\"/api/**\")");
        builder.line(".build();");
        builder.outdent();
        builder.closeMethod();
        builder.line();

        // Management API group
        builder.annotation("Bean");
        builder.methodSignature("public", "GroupedOpenApi", "managementGroup");
        builder.line("return GroupedOpenApi.builder()");
        builder.indent();
        builder.line(".group(\"management\")");
        builder.line(".pathsToMatch(\"/management/**\")");
        builder.line(".build();");
        builder.outdent();
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/OpenApiConfiguration.java", builder.build());
    }

    private void writeSwaggerResource() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest");

        builder.addImports(
            "org.springframework.http.ResponseEntity",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.RestController"
        );

        builder.javadoc("REST controller for Swagger/OpenAPI redirect.");
        builder.annotation("RestController");
        builder.annotation("RequestMapping", "\"/api\"");

        builder.classDeclaration("public", "SwaggerResource", null);

        builder.javadoc("Redirect to Swagger UI.");
        builder.annotation("GetMapping", "\"/swagger-redirect\"");
        builder.methodSignature("public", "ResponseEntity<Void>", "redirect");
        builder.returnStatement("ResponseEntity.status(302)\n" +
            "            .header(\"Location\", \"/swagger-ui.html\")\n" +
            "            .build()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "web/rest/SwaggerResource.java", builder.build());
    }
}
