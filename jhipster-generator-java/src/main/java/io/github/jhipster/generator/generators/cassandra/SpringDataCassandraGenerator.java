/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.cassandra;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Spring Data Cassandra Generator.
 * Generates Cassandra configuration, entities, and repositories.
 * Equivalent to spring-data-cassandra/generator.ts.
 */
public class SpringDataCassandraGenerator extends BaseApplicationGenerator {

    public SpringDataCassandraGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "spring-data-cassandra";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.CONFIGURING, "configuringCassandra", this::configuring);
        registerTask(GeneratorPriority.WRITING, "writingCassandra", this::writing);
        registerTask(GeneratorPriority.WRITING_ENTITIES, "writingCassandraEntities", this::writingEntities);
    }

    private void configuring() {
        log.info("Configuring Cassandra");

        JHipsterConfig config = getConfig();

        // Cassandra-specific constraints
        if ("pagination".equals(config.getPagination())) {
            log.warn("Pagination is not supported with Cassandra, switching to no pagination");
            config.setPagination("no");
        }

        // Set Cassandra configuration
        context.setConfigValue("cassandraContactPoints", "localhost");
        context.setConfigValue("cassandraPort", 9042);
        context.setConfigValue("cassandraKeyspace", config.getLowerBaseName());
    }

    private void writing() throws Exception {
        log.info("Writing Cassandra configuration files");

        // Write Cassandra configuration
        writeCassandraConfiguration();

        // Write CQL migration scripts
        writeCqlMigrations();

        // Write TestContainers configuration
        writeTestContainersConfiguration();

        // Write application YAML for Cassandra
        writeApplicationYamlCassandra();
    }

    private void writingEntities() throws Exception {
        log.info("Writing Cassandra entities");

        for (EntityConfig entity : context.getEntities()) {
            writeCassandraTable(entity);
            writeCassandraRepository(entity);
        }
    }

    private void writeCassandraConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "com.datastax.oss.driver.api.core.CqlSession",
            "org.springframework.boot.autoconfigure.cassandra.CassandraProperties",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.data.cassandra.config.AbstractCassandraConfiguration",
            "org.springframework.data.cassandra.config.SchemaAction",
            "org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification",
            "org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption",
            "org.springframework.data.cassandra.repository.config.EnableCassandraRepositories",
            "java.util.Collections",
            "java.util.List"
        );

        builder.javadoc("Cassandra configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableCassandraRepositories", "basePackages = \"" + config.getPackageName() + ".repository\"");

        builder.classDeclaration("public", "CassandraConfiguration", "AbstractCassandraConfiguration");

        builder.line("private final CassandraProperties cassandraProperties;");
        builder.line();

        builder.methodSignature("public", null, "CassandraConfiguration", "CassandraProperties cassandraProperties");
        builder.statement("this.cassandraProperties = cassandraProperties");
        builder.closeMethod();
        builder.line();

        // getKeyspaceName
        builder.annotation("Override");
        builder.methodSignature("protected", "String", "getKeyspaceName");
        builder.returnStatement("cassandraProperties.getKeyspaceName()");
        builder.closeMethod();
        builder.line();

        // getContactPoints
        builder.annotation("Override");
        builder.methodSignature("protected", "String", "getContactPoints");
        builder.returnStatement("String.join(\",\", cassandraProperties.getContactPoints())");
        builder.closeMethod();
        builder.line();

        // getPort
        builder.annotation("Override");
        builder.methodSignature("protected", "int", "getPort");
        builder.returnStatement("cassandraProperties.getPort()");
        builder.closeMethod();
        builder.line();

        // getLocalDataCenter
        builder.annotation("Override");
        builder.methodSignature("protected", "String", "getLocalDataCenter");
        builder.returnStatement("cassandraProperties.getLocalDatacenter()");
        builder.closeMethod();
        builder.line();

        // getSchemaAction
        builder.annotation("Override");
        builder.methodSignature("public", "SchemaAction", "getSchemaAction");
        builder.returnStatement("SchemaAction.CREATE_IF_NOT_EXISTS");
        builder.closeMethod();
        builder.line();

        // getKeyspaceCreations
        builder.annotation("Override");
        builder.methodSignature("protected", "List<CreateKeyspaceSpecification>", "getKeyspaceCreations");
        builder.line("CreateKeyspaceSpecification specification = CreateKeyspaceSpecification");
        builder.line("    .createKeyspace(getKeyspaceName())");
        builder.line("    .ifNotExists()");
        builder.line("    .with(KeyspaceOption.DURABLE_WRITES, true)");
        builder.line("    .withSimpleReplication(1);");
        builder.returnStatement("Collections.singletonList(specification)");
        builder.closeMethod();
        builder.line();

        // getEntityBasePackages
        builder.annotation("Override");
        builder.methodSignature("public", "String[]", "getEntityBasePackages");
        builder.returnStatement("new String[] { \"" + config.getPackageName() + ".domain\" }");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/CassandraConfiguration.java", builder.build());
    }

    private void writeCqlMigrations() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder cql = new StringBuilder();
        cql.append("-- Cassandra initialization script\n");
        cql.append("-- This script is executed on startup\n\n");

        cql.append("-- Create keyspace\n");
        cql.append("CREATE KEYSPACE IF NOT EXISTS ").append(config.getLowerBaseName()).append("\n");
        cql.append("    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};\n\n");

        cql.append("USE ").append(config.getLowerBaseName()).append(";\n\n");

        if (!config.isSkipUserManagement()) {
            cql.append("-- User management tables\n");
            cql.append("CREATE TABLE IF NOT EXISTS user (\n");
            cql.append("    id uuid PRIMARY KEY,\n");
            cql.append("    login text,\n");
            cql.append("    password_hash text,\n");
            cql.append("    first_name text,\n");
            cql.append("    last_name text,\n");
            cql.append("    email text,\n");
            cql.append("    activated boolean,\n");
            cql.append("    lang_key text,\n");
            cql.append("    activation_key text,\n");
            cql.append("    reset_key text,\n");
            cql.append("    reset_date timestamp,\n");
            cql.append("    authorities set<text>\n");
            cql.append(");\n\n");

            cql.append("CREATE INDEX IF NOT EXISTS user_login_idx ON user (login);\n");
            cql.append("CREATE INDEX IF NOT EXISTS user_email_idx ON user (email);\n\n");

            cql.append("CREATE TABLE IF NOT EXISTS user_by_login (\n");
            cql.append("    login text PRIMARY KEY,\n");
            cql.append("    id uuid\n");
            cql.append(");\n\n");

            cql.append("CREATE TABLE IF NOT EXISTS user_by_email (\n");
            cql.append("    email text PRIMARY KEY,\n");
            cql.append("    id uuid\n");
            cql.append(");\n\n");

            cql.append("-- Insert default users\n");
            cql.append("INSERT INTO user (id, login, password_hash, first_name, last_name, email, activated, lang_key, authorities)\n");
            cql.append("VALUES (uuid(), 'admin', '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC', 'Administrator', 'Administrator', 'admin@localhost', true, 'en', {'ROLE_USER', 'ROLE_ADMIN'});\n\n");

            cql.append("INSERT INTO user (id, login, password_hash, first_name, last_name, email, activated, lang_key, authorities)\n");
            cql.append("VALUES (uuid(), 'user', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'User', 'User', 'user@localhost', true, 'en', {'ROLE_USER'});\n");
        }

        writeFile(getMainResourcesPath() + "config/cql/create-keyspace.cql", cql.toString());

        // Write migration runner
        writeMigrationRunner();
    }

    private void writeMigrationRunner() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "com.datastax.oss.driver.api.core.CqlSession",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.boot.autoconfigure.cassandra.CassandraProperties",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.core.io.ClassPathResource",
            "jakarta.annotation.PostConstruct",
            "java.io.BufferedReader",
            "java.io.InputStreamReader",
            "java.nio.charset.StandardCharsets",
            "java.util.stream.Collectors"
        );

        builder.javadoc("Cassandra migration configuration.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "CassandraMigrationConfiguration", null);

        builder.line("private static final Logger log = LoggerFactory.getLogger(CassandraMigrationConfiguration.class);");
        builder.line();
        builder.line("private final CqlSession session;");
        builder.line();

        builder.methodSignature("public", null, "CassandraMigrationConfiguration", "CqlSession session");
        builder.statement("this.session = session");
        builder.closeMethod();
        builder.line();

        builder.annotation("PostConstruct");
        builder.methodSignature("public", "void", "runMigrations");
        builder.line("log.info(\"Running Cassandra migrations\");");
        builder.line("try {");
        builder.indent();
        builder.line("ClassPathResource resource = new ClassPathResource(\"config/cql/create-keyspace.cql\");");
        builder.line("String cql = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))");
        builder.line("    .lines().collect(Collectors.joining(\"\\n\"));");
        builder.line();
        builder.line("for (String statement : cql.split(\";\")) {");
        builder.indent();
        builder.line("String trimmed = statement.trim();");
        builder.line("if (!trimmed.isEmpty() && !trimmed.startsWith(\"--\")) {");
        builder.indent();
        builder.line("log.debug(\"Executing: {}\", trimmed);");
        builder.line("session.execute(trimmed);");
        builder.outdent();
        builder.line("}");
        builder.outdent();
        builder.line("}");
        builder.line("log.info(\"Cassandra migrations completed\");");
        builder.outdent();
        builder.line("} catch (Exception e) {");
        builder.indent();
        builder.line("log.error(\"Error running Cassandra migrations\", e);");
        builder.outdent();
        builder.line("}");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/CassandraMigrationConfiguration.java", builder.build());
    }

    private void writeTestContainersConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.beans.factory.DisposableBean",
            "org.springframework.beans.factory.InitializingBean",
            "org.testcontainers.containers.CassandraContainer",
            "org.testcontainers.containers.output.Slf4jLogConsumer"
        );

        builder.javadoc("TestContainers configuration for Cassandra.");
        builder.classDeclaration("public", "CassandraTestContainer", null, "InitializingBean", "DisposableBean");

        builder.line("private static final Logger log = LoggerFactory.getLogger(CassandraTestContainer.class);");
        builder.line();
        builder.line("private CassandraContainer<?> cassandraContainer;");
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "destroy");
        builder.line("if (cassandraContainer != null && cassandraContainer.isRunning()) {");
        builder.indent();
        builder.line("cassandraContainer.stop();");
        builder.outdent();
        builder.line("}");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "afterPropertiesSet");
        builder.line("if (cassandraContainer != null && cassandraContainer.isRunning()) {");
        builder.indent();
        builder.returnStatement(null);
        builder.outdent();
        builder.line("}");
        builder.line();
        builder.line("cassandraContainer = new CassandraContainer<>(\"cassandra:4.1\")");
        builder.line("    .withLogConsumer(new Slf4jLogConsumer(log))");
        builder.line("    .withReuse(true);");
        builder.line();
        builder.line("cassandraContainer.start();");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "CassandraContainer<?>", "getCassandraContainer");
        builder.returnStatement("cassandraContainer");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getTestJavaPath() + "config/CassandraTestContainer.java", builder.build());
    }

    private void writeApplicationYamlCassandra() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder yaml = new StringBuilder();
        yaml.append("# Cassandra configuration\n");
        yaml.append("spring:\n");
        yaml.append("  cassandra:\n");
        yaml.append("    keyspace-name: ").append(config.getLowerBaseName()).append("\n");
        yaml.append("    contact-points: localhost\n");
        yaml.append("    port: 9042\n");
        yaml.append("    local-datacenter: datacenter1\n");
        yaml.append("    schema-action: CREATE_IF_NOT_EXISTS\n");

        context.setConfigValue("cassandraYaml", yaml.toString());
    }

    private void writeCassandraTable(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".domain");

        builder.addImports(
            "org.springframework.data.annotation.Id",
            "org.springframework.data.cassandra.core.mapping.Column",
            "org.springframework.data.cassandra.core.mapping.PrimaryKey",
            "org.springframework.data.cassandra.core.mapping.Table",
            "jakarta.validation.constraints.*",
            "java.io.Serializable",
            "java.util.UUID"
        );

        // Add field type imports
        for (FieldConfig field : entity.getFields()) {
            addFieldImports(builder, field);
        }

        String className = entity.getName();
        String tableName = toSnakeCase(className);

        builder.javadoc("A " + className + " Cassandra table.");
        builder.annotation("Table", "value = \"" + tableName + "\"");

        builder.classDeclaration("public", className, null, "Serializable");

        builder.line("private static final long serialVersionUID = 1L;");
        builder.line();

        // Primary key
        builder.annotation("PrimaryKey");
        builder.field("private", "UUID", "id");
        builder.line();

        // Entity fields
        for (FieldConfig field : entity.getFields()) {
            writeCassandraFieldAnnotations(builder, field);
            builder.field("private", getJavaType(field), field.getFieldName());
            builder.line();
        }

        // Getters and setters
        builder.methodSignature("public", "UUID", "getId");
        builder.returnStatement("id");
        builder.closeMethod();

        builder.methodSignature("public", "void", "setId", "UUID id");
        builder.statement("this.id = id");
        builder.closeMethod();
        builder.line();

        for (FieldConfig field : entity.getFields()) {
            writeFieldGetterSetter(builder, className, field);
        }

        // equals, hashCode, toString
        writeEqualsHashCodeToString(builder, className);

        builder.closeClass();

        writeFile(getMainJavaPath() + "domain/" + className + ".java", builder.build());
    }

    private void writeCassandraFieldAnnotations(JavaCodeBuilder builder, FieldConfig field) {
        if (field.isRequired()) {
            builder.annotation("NotNull");
        }

        builder.annotation("Column", "value = \"" + toSnakeCase(field.getFieldName()) + "\"");
    }

    private void writeCassandraRepository(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();
        String className = entity.getName();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".repository");

        builder.addImports(
            config.getPackageName() + ".domain." + className,
            "org.springframework.data.cassandra.repository.CassandraRepository",
            "org.springframework.stereotype.Repository",
            "java.util.UUID"
        );

        builder.javadoc("Spring Data Cassandra repository for the " + className + " entity.");
        builder.annotation("Repository");

        builder.line("public interface " + className + "Repository extends CassandraRepository<" + className + ", UUID> {");
        builder.line();
        builder.line("}");

        writeFile(getMainJavaPath() + "repository/" + className + "Repository.java", builder.build());
    }

    private void addFieldImports(JavaCodeBuilder builder, FieldConfig field) {
        String type = field.getFieldType();
        switch (type) {
            case "LocalDate":
                builder.addImport("java.time.LocalDate");
                break;
            case "ZonedDateTime":
                builder.addImport("java.time.ZonedDateTime");
                break;
            case "Instant":
                builder.addImport("java.time.Instant");
                break;
            case "BigDecimal":
                builder.addImport("java.math.BigDecimal");
                break;
        }
    }

    private String getJavaType(FieldConfig field) {
        String type = field.getFieldType();
        switch (type) {
            case "String":
            case "Integer":
            case "Long":
            case "Float":
            case "Double":
            case "Boolean":
            case "LocalDate":
            case "ZonedDateTime":
            case "Instant":
            case "BigDecimal":
                return type;
            default:
                return type;
        }
    }

    private void writeFieldGetterSetter(JavaCodeBuilder builder, String className, FieldConfig field) {
        String fieldName = field.getFieldName();
        String fieldType = getJavaType(field);
        String capitalizedName = capitalize(fieldName);

        builder.methodSignature("public", fieldType, "get" + capitalizedName);
        builder.returnStatement(fieldName);
        builder.closeMethod();

        builder.methodSignature("public", "void", "set" + capitalizedName, fieldType + " " + fieldName);
        builder.statement("this." + fieldName + " = " + fieldName);
        builder.closeMethod();
        builder.line();
    }

    private void writeEqualsHashCodeToString(JavaCodeBuilder builder, String className) {
        builder.annotation("Override");
        builder.methodSignature("public", "boolean", "equals", "Object o");
        builder.line("if (this == o) return true;");
        builder.line("if (!(o instanceof " + className + ")) return false;");
        builder.line(className + " that = (" + className + ") o;");
        builder.returnStatement("id != null && id.equals(that.id)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "int", "hashCode");
        builder.returnStatement("getClass().hashCode()");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"" + className + "{\" + \"id=\" + id + \"}\"");
        builder.closeMethod();
    }

    private String toSnakeCase(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
