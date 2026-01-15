/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.feign;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Feign Client Generator.
 * Generates Feign client configuration for inter-service communication.
 * Equivalent to feign-client/generator.ts.
 */
public class FeignClientGenerator extends BaseApplicationGenerator {

    public FeignClientGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "feign-client";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingFeignClient", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing Feign Client configuration");

        writeFeignConfiguration();
        writeAuthorizedFeignClient();

        if (getConfig().isJwt()) {
            writeTokenRelayRequestInterceptor();
        } else if (getConfig().isOauth2()) {
            writeOAuth2InterceptedFeignConfiguration();
        }
    }

    private void writeFeignConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.cloud.openfeign.EnableFeignClients",
            "org.springframework.cloud.openfeign.FeignClientsConfiguration",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.context.annotation.Import"
        );

        builder.javadoc("Feign configuration for inter-service communication.");
        builder.annotation("Configuration");
        builder.annotation("EnableFeignClients", "basePackages = \"" + config.getPackageName() + "\"");
        builder.annotation("Import", "FeignClientsConfiguration.class");

        builder.classDeclaration("public", "FeignConfiguration", null);

        builder.javadoc("Custom Feign configuration beans can be added here.");

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/FeignConfiguration.java", builder.build());
    }

    private void writeAuthorizedFeignClient() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".client");

        builder.addImports(
            "org.springframework.cloud.openfeign.FeignClient",
            "org.springframework.core.annotation.AliasFor",
            "java.lang.annotation.*"
        );

        builder.javadoc("Annotation to mark Feign clients that should include authorization headers.");
        builder.annotation("Retention", "RetentionPolicy.RUNTIME");
        builder.annotation("Target", "ElementType.TYPE");
        builder.annotation("Documented");
        builder.annotation("FeignClient");

        builder.line("public @interface AuthorizedFeignClient {");
        builder.indent();
        builder.line();
        builder.annotation("AliasFor", "annotation = FeignClient.class, attribute = \"name\"");
        builder.line("String name() default \"\";");
        builder.line();
        builder.annotation("AliasFor", "annotation = FeignClient.class, attribute = \"url\"");
        builder.line("String url() default \"\";");
        builder.line();
        builder.annotation("AliasFor", "annotation = FeignClient.class, attribute = \"configuration\"");
        builder.line("Class<?>[] configuration() default {};");
        builder.line();
        builder.annotation("AliasFor", "annotation = FeignClient.class, attribute = \"fallback\"");
        builder.line("Class<?> fallback() default void.class;");
        builder.line();
        builder.annotation("AliasFor", "annotation = FeignClient.class, attribute = \"fallbackFactory\"");
        builder.line("Class<?> fallbackFactory() default void.class;");
        builder.line();
        builder.annotation("AliasFor", "annotation = FeignClient.class, attribute = \"path\"");
        builder.line("String path() default \"\";");
        builder.outdent();
        builder.line("}");

        writeFile(getMainJavaPath() + "client/AuthorizedFeignClient.java", builder.build());
    }

    private void writeTokenRelayRequestInterceptor() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".client");

        builder.addImports(
            "feign.RequestInterceptor",
            "feign.RequestTemplate",
            "org.springframework.stereotype.Component",
            "org.springframework.web.context.request.RequestContextHolder",
            "org.springframework.web.context.request.ServletRequestAttributes"
        );

        builder.javadoc("Feign request interceptor to relay JWT tokens to downstream services.");
        builder.annotation("Component");

        builder.classDeclaration("public", "TokenRelayRequestInterceptor", null, "RequestInterceptor");

        builder.staticFinalField("String", "AUTHORIZATION_HEADER", "\"Authorization\"");
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "apply", "RequestTemplate template");
        builder.statement("ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes()");
        builder.ifStatement("attributes != null");
        builder.statement("String authorizationHeader = attributes.getRequest().getHeader(AUTHORIZATION_HEADER)");
        builder.ifStatement("authorizationHeader != null");
        builder.statement("template.header(AUTHORIZATION_HEADER, authorizationHeader)");
        builder.closeIf();
        builder.closeIf();
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "client/TokenRelayRequestInterceptor.java", builder.build());
    }

    private void writeOAuth2InterceptedFeignConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".client");

        builder.addImports(
            "feign.RequestInterceptor",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.security.oauth2.client.*",
            "org.springframework.security.oauth2.client.registration.ClientRegistrationRepository"
        );

        builder.javadoc("Configuration for OAuth2-enabled Feign clients.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "OAuth2InterceptedFeignConfiguration", null);

        builder.annotation("Bean");
        builder.methodSignature("public", "RequestInterceptor", "oauth2FeignRequestInterceptor",
            "OAuth2AuthorizedClientManager authorizedClientManager");
        builder.returnStatement("new OAuth2FeignRequestInterceptor(authorizedClientManager)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "OAuth2AuthorizedClientManager", "authorizedClientManager",
            "ClientRegistrationRepository clientRegistrationRepository",
            "OAuth2AuthorizedClientService authorizedClientService");
        builder.statement("AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = " +
            "new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService)");
        builder.statement("authorizedClientManager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build())");
        builder.returnStatement("authorizedClientManager");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "client/OAuth2InterceptedFeignConfiguration.java", builder.build());

        // Also write the OAuth2FeignRequestInterceptor
        writeOAuth2FeignRequestInterceptor();
    }

    private void writeOAuth2FeignRequestInterceptor() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".client");

        builder.addImports(
            "feign.RequestInterceptor",
            "feign.RequestTemplate",
            "org.springframework.security.oauth2.client.OAuth2AuthorizeRequest",
            "org.springframework.security.oauth2.client.OAuth2AuthorizedClient",
            "org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager"
        );

        builder.javadoc("OAuth2 Feign request interceptor for client credentials flow.");

        builder.classDeclaration("public", "OAuth2FeignRequestInterceptor", null, "RequestInterceptor");

        builder.staticFinalField("String", "AUTHORIZATION_HEADER", "\"Authorization\"");
        builder.staticFinalField("String", "BEARER_TOKEN_TYPE", "\"Bearer\"");
        builder.line();

        builder.field("private final", "OAuth2AuthorizedClientManager", "authorizedClientManager");
        builder.field("private final", "String", "clientRegistrationId", "\"internal\"");
        builder.line();

        builder.constructor("public", "OAuth2FeignRequestInterceptor",
            "OAuth2AuthorizedClientManager authorizedClientManager");
        builder.statement("this.authorizedClientManager = authorizedClientManager");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "apply", "RequestTemplate template");
        builder.statement("OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId).principal(\"internal\").build()");
        builder.statement("OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest)");
        builder.ifStatement("authorizedClient != null && authorizedClient.getAccessToken() != null");
        builder.statement("template.header(AUTHORIZATION_HEADER, String.format(\"%s %s\", BEARER_TOKEN_TYPE, authorizedClient.getAccessToken().getTokenValue()))");
        builder.closeIf();
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "client/OAuth2FeignRequestInterceptor.java", builder.build());
    }
}
