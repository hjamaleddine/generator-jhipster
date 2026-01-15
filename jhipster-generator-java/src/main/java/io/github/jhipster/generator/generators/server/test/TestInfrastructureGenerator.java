/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.server.test;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Test Infrastructure Generator.
 * Generates test utilities, annotations, and configuration.
 */
public class TestInfrastructureGenerator extends BaseApplicationGenerator {

    public TestInfrastructureGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "test-infrastructure";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingTestInfrastructure", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing test infrastructure");

        writeIntegrationTestAnnotation();
        writeTestUtil();
        writeTestSecurityConfiguration();
        writeTestContainersSpringContextCustomizer();
        writeApplicationYmlTest();
    }

    private void writeIntegrationTestAnnotation() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName());

        builder.addImports(
            "java.lang.annotation.*",
            "org.springframework.boot.test.context.SpringBootTest",
            "org.springframework.test.annotation.DirtiesContext"
        );

        builder.javadoc("Base composite annotation for integration tests.");
        builder.annotation("Target", "ElementType.TYPE");
        builder.annotation("Retention", "RetentionPolicy.RUNTIME");
        builder.annotation("SpringBootTest", "classes = { " + config.getMainClassName() + ".class }");
        builder.annotation("DirtiesContext", "classMode = DirtiesContext.ClassMode.AFTER_CLASS");

        builder.line("public @interface IntegrationTest {");
        builder.line("}");

        writeFile(getTestJavaPath() + "IntegrationTest.java", builder.build());
    }

    private void writeTestUtil() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".web.rest");

        builder.addImports(
            "com.fasterxml.jackson.annotation.JsonInclude",
            "com.fasterxml.jackson.databind.ObjectMapper",
            "com.fasterxml.jackson.databind.SerializationFeature",
            "com.fasterxml.jackson.datatype.jsr310.JavaTimeModule",
            "org.hamcrest.Description",
            "org.hamcrest.TypeSafeDiagnosingMatcher",
            "org.hamcrest.TypeSafeMatcher",
            "org.springframework.format.datetime.standard.DateTimeFormatterRegistrar",
            "org.springframework.format.support.DefaultFormattingConversionService",
            "org.springframework.format.support.FormattingConversionService",
            "jakarta.persistence.EntityManager",
            "jakarta.persistence.TypedQuery",
            "jakarta.persistence.criteria.CriteriaBuilder",
            "jakarta.persistence.criteria.CriteriaQuery",
            "jakarta.persistence.criteria.Root",
            "java.io.IOException",
            "java.math.BigDecimal",
            "java.time.ZonedDateTime",
            "java.time.format.DateTimeParseException",
            "java.util.List",
            "static org.assertj.core.api.Assertions.assertThat"
        );

        builder.javadoc("Utility class for testing REST controllers.");

        builder.classDeclaration("public final", "TestUtil", null);

        builder.field("private static final", "ObjectMapper", "mapper", "createObjectMapper()");
        builder.line();

        // createObjectMapper
        builder.methodSignature("private static", "ObjectMapper", "createObjectMapper");
        builder.statement("ObjectMapper mapper = new ObjectMapper()");
        builder.statement("mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)");
        builder.statement("mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)");
        builder.statement("mapper.registerModule(new JavaTimeModule())");
        builder.returnStatement("mapper");
        builder.closeMethod();
        builder.line();

        // convertObjectToJsonBytes
        builder.javadoc("Convert an object to JSON byte array.");
        builder.methodSignature("public static", "byte[]", "convertObjectToJsonBytes", "Object object");
        builder.statement("throws IOException");
        builder.returnStatement("mapper.writeValueAsBytes(object)");
        builder.closeMethod();
        builder.line();

        // createByteArray
        builder.javadoc("Create a byte array with a specific size filled with specified data.");
        builder.methodSignature("public static", "byte[]", "createByteArray", "int size", "String data");
        builder.statement("byte[] byteArray = new byte[size]");
        builder.forStatement("int i = 0; i < size; i++");
        builder.statement("byteArray[i] = Byte.parseByte(data, 2)");
        builder.closeFor();
        builder.returnStatement("byteArray");
        builder.closeMethod();
        builder.line();

        // createFormattingConversionService
        builder.javadoc("Create a {@link FormattingConversionService} which uses ISO date format.");
        builder.methodSignature("public static", "FormattingConversionService", "createFormattingConversionService");
        builder.statement("DefaultFormattingConversionService dfcs = new DefaultFormattingConversionService()");
        builder.statement("DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar()");
        builder.statement("registrar.setUseIsoFormat(true)");
        builder.statement("registrar.registerFormatters(dfcs)");
        builder.returnStatement("dfcs");
        builder.closeMethod();
        builder.line();

        // sameInstant matcher
        builder.javadoc("Verifies the equals/hashCode contract on the domain object.");
        builder.methodSignature("public static", "<T> void", "equalsVerifier", "Class<T> clazz");
        builder.statement("throws Exception");
        builder.statement("T domainObject1 = clazz.getDeclaredConstructor().newInstance()");
        builder.statement("assertThat(domainObject1.toString()).isNotNull()");
        builder.statement("assertThat(domainObject1).isEqualTo(domainObject1)");
        builder.statement("assertThat(domainObject1).hasSameHashCodeAs(domainObject1)");
        builder.line();
        builder.statement("Object testOtherObject = new Object()");
        builder.statement("assertThat(domainObject1).isNotEqualTo(testOtherObject)");
        builder.statement("assertThat(domainObject1).isNotEqualTo(null)");
        builder.line();
        builder.statement("T domainObject2 = clazz.getDeclaredConstructor().newInstance()");
        builder.statement("assertThat(domainObject1).isNotEqualTo(domainObject2)");
        builder.closeMethod();
        builder.line();

        // findAll helper
        builder.javadoc("Find all entities of a type in the database.");
        builder.line("@SuppressWarnings(\"unchecked\")");
        builder.methodSignature("public static", "<T> List<T>", "findAll", "EntityManager em", "Class<T> clazz");
        builder.statement("CriteriaBuilder cb = em.getCriteriaBuilder()");
        builder.statement("CriteriaQuery<T> cq = cb.createQuery(clazz)");
        builder.statement("Root<T> rootEntry = cq.from(clazz)");
        builder.statement("CriteriaQuery<T> all = cq.select(rootEntry)");
        builder.statement("TypedQuery<T> allQuery = em.createQuery(all)");
        builder.returnStatement("allQuery.getResultList()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getTestJavaPath() + "web/rest/TestUtil.java", builder.build());
    }

    private void writeTestSecurityConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.boot.test.context.TestConfiguration",
            "org.springframework.context.annotation.Bean",
            "org.springframework.security.core.userdetails.User",
            "org.springframework.security.core.userdetails.UserDetails",
            "org.springframework.security.core.userdetails.UserDetailsService",
            "org.springframework.security.provisioning.InMemoryUserDetailsManager"
        );

        builder.javadoc("Test security configuration providing simple in-memory authentication.");
        builder.annotation("TestConfiguration");

        builder.classDeclaration("public", "TestSecurityConfiguration", null);

        builder.annotation("Bean");
        builder.methodSignature("public", "UserDetailsService", "userDetailsService");
        builder.statement("UserDetails user = User.withDefaultPasswordEncoder()\n" +
            "            .username(\"user\")\n" +
            "            .password(\"user\")\n" +
            "            .roles(\"USER\")\n" +
            "            .build()");
        builder.statement("UserDetails admin = User.withDefaultPasswordEncoder()\n" +
            "            .username(\"admin\")\n" +
            "            .password(\"admin\")\n" +
            "            .roles(\"USER\", \"ADMIN\")\n" +
            "            .build()");
        builder.returnStatement("new InMemoryUserDetailsManager(user, admin)");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getTestJavaPath() + "config/TestSecurityConfiguration.java", builder.build());
    }

    private void writeTestContainersSpringContextCustomizer() throws Exception {
        JHipsterConfig config = getConfig();

        // Only generate if using SQL database
        if (!isSql()) {
            return;
        }

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.beans.factory.config.ConfigurableListableBeanFactory",
            "org.springframework.boot.test.util.TestPropertyValues",
            "org.springframework.context.ConfigurableApplicationContext",
            "org.springframework.core.annotation.AnnotatedElementUtils",
            "org.springframework.test.context.ContextConfigurationAttributes",
            "org.springframework.test.context.ContextCustomizer",
            "org.springframework.test.context.ContextCustomizerFactory",
            "org.springframework.test.context.MergedContextConfiguration",
            "org.testcontainers.containers.PostgreSQLContainer",
            "java.util.*"
        );

        builder.javadoc("TestContainers context customizer for spinning up database containers during tests.");

        builder.classDeclaration("public", "TestContainersSpringContextCustomizerFactory", null, "ContextCustomizerFactory");

        builder.field("private static final", "Logger", "log", "LoggerFactory.getLogger(TestContainersSpringContextCustomizerFactory.class)");
        builder.line();

        String containerType = "postgresql".equals(config.getProdDatabaseType()) ? "PostgreSQLContainer" : "JdbcDatabaseContainer";
        builder.field("private static", containerType + "<?>", "container");
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "ContextCustomizer", "createContextCustomizer",
            "Class<?> testClass", "List<ContextConfigurationAttributes> configAttributes");
        builder.returnStatement("new SqlTestContainersSpringContextCustomizer()");
        builder.closeMethod();
        builder.line();

        // Inner class
        builder.line("private static class SqlTestContainersSpringContextCustomizer implements ContextCustomizer {");
        builder.indent();
        builder.line();
        builder.annotation("Override");
        builder.methodSignature("public", "void", "customizeContext",
            "ConfigurableApplicationContext context", "MergedContextConfiguration mergedConfig");
        builder.statement("ConfigurableListableBeanFactory beanFactory = context.getBeanFactory()");
        builder.line();
        builder.ifStatement("container == null");

        if ("postgresql".equals(config.getProdDatabaseType())) {
            builder.statement("container = new PostgreSQLContainer<>(\"postgres:15.4\")");
        } else if ("mysql".equals(config.getProdDatabaseType())) {
            builder.statement("container = new MySQLContainer<>(\"mysql:8.0.33\")");
        } else {
            builder.statement("container = new PostgreSQLContainer<>(\"postgres:15.4\")");
        }
        builder.statement("container.start()");
        builder.statement("log.info(\"Started test container: {}\", container.getJdbcUrl())");
        builder.closeIf();
        builder.line();
        builder.statement("TestPropertyValues.of(\n" +
            "                \"spring.datasource.url=\" + container.getJdbcUrl(),\n" +
            "                \"spring.datasource.username=\" + container.getUsername(),\n" +
            "                \"spring.datasource.password=\" + container.getPassword()\n" +
            "            ).applyTo(context)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "boolean", "equals", "Object o");
        builder.returnStatement("this == o || (o != null && getClass() == o.getClass())");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "int", "hashCode");
        builder.returnStatement("getClass().hashCode()");
        builder.closeMethod();

        builder.outdent();
        builder.line("}");

        builder.closeClass();

        writeFile(getTestJavaPath() + "config/TestContainersSpringContextCustomizerFactory.java", builder.build());
    }

    private void writeApplicationYmlTest() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yml = new StringBuilder();
        yml.append("# Test configuration\n");
        yml.append("spring:\n");
        yml.append("  profiles:\n");
        yml.append("    active: test\n");
        yml.append("  jpa:\n");
        yml.append("    open-in-view: false\n");
        yml.append("    hibernate:\n");
        yml.append("      ddl-auto: create-drop\n");
        yml.append("    properties:\n");
        yml.append("      hibernate.id.new_generator_mappings: true\n");
        yml.append("      hibernate.connection.provider_disables_autocommit: true\n");
        yml.append("      hibernate.cache.use_second_level_cache: false\n");
        yml.append("      hibernate.cache.use_query_cache: false\n");
        yml.append("      hibernate.generate_statistics: false\n");
        yml.append("      hibernate.jdbc.batch_size: 25\n");
        yml.append("      hibernate.order_inserts: true\n");
        yml.append("      hibernate.order_updates: true\n");
        yml.append("  datasource:\n");
        yml.append("    type: com.zaxxer.hikari.HikariDataSource\n");
        yml.append("    url: jdbc:h2:mem:").append(config.getLowerBaseName()).append(";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE\n");
        yml.append("    username: ").append(config.getLowerBaseName()).append("\n");
        yml.append("    password:\n");
        yml.append("    hikari:\n");
        yml.append("      auto-commit: false\n");
        yml.append("  liquibase:\n");
        yml.append("    contexts: test\n");
        yml.append("  mail:\n");
        yml.append("    host: localhost\n");
        yml.append("  main:\n");
        yml.append("    allow-bean-definition-overriding: true\n");
        yml.append("  messages:\n");
        yml.append("    basename: i18n/messages\n");
        yml.append("  task:\n");
        yml.append("    execution:\n");
        yml.append("      thread-name-prefix: ").append(config.getLowerBaseName()).append("-task-\n");
        yml.append("      pool:\n");
        yml.append("        core-size: 1\n");
        yml.append("        max-size: 50\n");
        yml.append("        queue-capacity: 10000\n");
        yml.append("    scheduling:\n");
        yml.append("      thread-name-prefix: ").append(config.getLowerBaseName()).append("-scheduling-\n");
        yml.append("      pool:\n");
        yml.append("        size: 1\n");
        yml.append("  thymeleaf:\n");
        yml.append("    mode: HTML\n");
        yml.append("\n");
        yml.append("server:\n");
        yml.append("  port: 10344\n");
        yml.append("  address: localhost\n");
        yml.append("\n");
        yml.append("jhipster:\n");
        yml.append("  clientApp:\n");
        yml.append("    name: '").append(config.getLowerBaseName()).append("'\n");
        yml.append("  security:\n");
        yml.append("    authentication:\n");

        if (config.isJwt()) {
            yml.append("      jwt:\n");
            yml.append("        base64-secret: dGVzdHNlY3JldGtleXRoYXRpc2xvbmdlbm91Z2hmb3J0ZXN0aW5nYW5kc2hvdWxkYmVhdGxlYXN0NTEyYml0c2xvbmc=\n");
            yml.append("        token-validity-in-seconds: 86400\n");
        }

        yml.append("  logging:\n");
        yml.append("    use-json-format: false\n");
        yml.append("    logstash:\n");
        yml.append("      enabled: false\n");

        writeFile(getTestResourcesPath() + "config/application.yml", yml.toString());
    }

    private String getTestJavaPath() {
        return context.getTestJavaPath();
    }

    private String getTestResourcesPath() {
        return context.getBasePath().toString() + "/src/test/resources/";
    }
}
