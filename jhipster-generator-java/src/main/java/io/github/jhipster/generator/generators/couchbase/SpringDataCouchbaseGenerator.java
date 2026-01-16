/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.couchbase;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Spring Data Couchbase Generator.
 * Generates Couchbase configuration, entities, and repositories.
 * Equivalent to spring-data-couchbase/generator.ts.
 */
public class SpringDataCouchbaseGenerator extends BaseApplicationGenerator {

    public SpringDataCouchbaseGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "spring-data-couchbase";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.CONFIGURING, "configuringCouchbase", this::configuring);
        registerTask(GeneratorPriority.WRITING, "writingCouchbase", this::writing);
        registerTask(GeneratorPriority.WRITING_ENTITIES, "writingCouchbaseEntities", this::writingEntities);
    }

    private void configuring() {
        log.info("Configuring Couchbase");

        JHipsterConfig config = getConfig();

        context.setConfigValue("couchbaseConnectionString", "couchbase://localhost");
        context.setConfigValue("couchbaseBucket", config.getLowerBaseName());
        context.setConfigValue("couchbaseUsername", "Administrator");
        context.setConfigValue("couchbasePassword", "password");
    }

    private void writing() throws Exception {
        log.info("Writing Couchbase configuration files");

        writeCouchbaseConfiguration();
        writeCouchmoveConfiguration();
        writeInitialMigration();
        writeTestContainersConfiguration();
    }

    private void writingEntities() throws Exception {
        log.info("Writing Couchbase entities");

        for (EntityConfig entity : context.getEntities()) {
            writeCouchbaseDocument(entity);
            writeCouchbaseRepository(entity);
        }
    }

    private void writeCouchbaseConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "com.couchbase.client.java.Cluster",
            "com.couchbase.client.java.env.ClusterEnvironment",
            "org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration",
            "org.springframework.data.couchbase.core.convert.CouchbaseCustomConversions",
            "org.springframework.data.couchbase.repository.auditing.EnableCouchbaseAuditing",
            "org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories",
            "org.springframework.transaction.annotation.EnableTransactionManagement",
            "java.util.Collections"
        );

        builder.javadoc("Couchbase configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableCouchbaseRepositories", "basePackages = \"" + config.getPackageName() + ".repository\"");
        builder.annotation("EnableCouchbaseAuditing", "auditorAwareRef = \"springSecurityAuditorAware\"");
        builder.annotation("EnableTransactionManagement");

        builder.classDeclaration("public", "DatabaseConfiguration", "AbstractCouchbaseConfiguration");

        builder.line("private final CouchbaseProperties couchbaseProperties;");
        builder.line();

        builder.methodSignature("public", null, "DatabaseConfiguration", "CouchbaseProperties couchbaseProperties");
        builder.statement("this.couchbaseProperties = couchbaseProperties");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "getConnectionString");
        builder.returnStatement("couchbaseProperties.getConnectionString()");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "getUserName");
        builder.returnStatement("couchbaseProperties.getUsername()");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "getPassword");
        builder.returnStatement("couchbaseProperties.getPassword()");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "getBucketName");
        builder.returnStatement("couchbaseProperties.getBucket().getName()");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.annotation("Bean");
        builder.methodSignature("public", "CouchbaseCustomConversions", "customConversions");
        builder.returnStatement("new CouchbaseCustomConversions(Collections.emptyList())");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/DatabaseConfiguration.java", builder.build());
    }

    private void writeCouchmoveConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "com.couchbase.client.java.Cluster",
            "com.github.couchmove.Couchmove",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration"
        );

        builder.javadoc("Couchmove migration configuration.");
        builder.annotation("Configuration");

        builder.classDeclaration("public", "CouchmoveConfiguration", null);

        builder.line("private static final Logger log = LoggerFactory.getLogger(CouchmoveConfiguration.class);");
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "Couchmove", "couchmove", "Cluster cluster, CouchbaseProperties properties");
        builder.line("log.info(\"Configuring Couchmove\");");
        builder.line("Couchmove couchmove = new Couchmove(");
        builder.line("    cluster.bucket(properties.getBucket().getName()),");
        builder.line("    \"config/couchmove/changelog\"");
        builder.line(");");
        builder.line("couchmove.migrate();");
        builder.returnStatement("couchmove");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/CouchmoveConfiguration.java", builder.build());
    }

    private void writeInitialMigration() throws Exception {
        JHipsterConfig config = getConfig();

        // Create N1QL migration file
        StringBuilder n1ql = new StringBuilder();
        n1ql.append("-- Couchbase initialization\n\n");

        if (!config.isSkipUserManagement()) {
            n1ql.append("-- Create primary index for user lookups\n");
            n1ql.append("CREATE PRIMARY INDEX IF NOT EXISTS `#primary` ON `").append(config.getLowerBaseName()).append("`;\n\n");

            n1ql.append("-- Create index for user login\n");
            n1ql.append("CREATE INDEX IF NOT EXISTS `idx_user_login` ON `").append(config.getLowerBaseName());
            n1ql.append("` (login) WHERE _class = \"").append(config.getPackageName()).append(".domain.User\";\n\n");

            n1ql.append("-- Create index for user email\n");
            n1ql.append("CREATE INDEX IF NOT EXISTS `idx_user_email` ON `").append(config.getLowerBaseName());
            n1ql.append("` (email) WHERE _class = \"").append(config.getPackageName()).append(".domain.User\";\n");
        }

        writeFile(getMainResourcesPath() + "config/couchmove/changelog/V0__initial_setup.n1ql", n1ql.toString());

        // Create JSON document for admin user
        if (!config.isSkipUserManagement()) {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"_class\": \"").append(config.getPackageName()).append(".domain.User\",\n");
            json.append("  \"id\": \"user::admin\",\n");
            json.append("  \"login\": \"admin\",\n");
            json.append("  \"password\": \"$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC\",\n");
            json.append("  \"firstName\": \"Administrator\",\n");
            json.append("  \"lastName\": \"Administrator\",\n");
            json.append("  \"email\": \"admin@localhost\",\n");
            json.append("  \"activated\": true,\n");
            json.append("  \"langKey\": \"en\",\n");
            json.append("  \"authorities\": [\"ROLE_ADMIN\", \"ROLE_USER\"]\n");
            json.append("}\n");

            writeFile(getMainResourcesPath() + "config/couchmove/changelog/V1__admin_user.json", json.toString());
        }
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
            "org.testcontainers.couchbase.BucketDefinition",
            "org.testcontainers.couchbase.CouchbaseContainer",
            "org.testcontainers.couchbase.CouchbaseService",
            "org.testcontainers.utility.DockerImageName",
            "java.time.Duration"
        );

        builder.javadoc("TestContainers configuration for Couchbase.");
        builder.classDeclaration("public", "CouchbaseTestContainer", null, "InitializingBean", "DisposableBean");

        builder.line("private static final Logger log = LoggerFactory.getLogger(CouchbaseTestContainer.class);");
        builder.line();
        builder.line("private CouchbaseContainer couchbaseContainer;");
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "destroy");
        builder.line("if (couchbaseContainer != null && couchbaseContainer.isRunning()) {");
        builder.indent();
        builder.line("couchbaseContainer.stop();");
        builder.outdent();
        builder.line("}");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "afterPropertiesSet");
        builder.line("if (couchbaseContainer != null && couchbaseContainer.isRunning()) {");
        builder.indent();
        builder.returnStatement(null);
        builder.outdent();
        builder.line("}");
        builder.line();
        builder.line("couchbaseContainer = new CouchbaseContainer(DockerImageName.parse(\"couchbase/server:7.2.0\"))");
        builder.line("    .withBucket(new BucketDefinition(\"" + config.getLowerBaseName() + "\"))");
        builder.line("    .withEnabledServices(CouchbaseService.KV, CouchbaseService.INDEX, CouchbaseService.QUERY)");
        builder.line("    .withStartupTimeout(Duration.ofMinutes(5));");
        builder.line();
        builder.line("couchbaseContainer.start();");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "CouchbaseContainer", "getCouchbaseContainer");
        builder.returnStatement("couchbaseContainer");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getTestJavaPath() + "config/CouchbaseTestContainer.java", builder.build());
    }

    private void writeCouchbaseDocument(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".domain");

        builder.addImports(
            "org.springframework.data.annotation.Id",
            "org.springframework.data.couchbase.core.mapping.Document",
            "org.springframework.data.couchbase.core.mapping.Field",
            "org.springframework.data.couchbase.core.mapping.id.GeneratedValue",
            "org.springframework.data.couchbase.core.mapping.id.GenerationStrategy",
            "jakarta.validation.constraints.*",
            "java.io.Serializable"
        );

        String className = entity.getName();
        String docType = className.toLowerCase();

        builder.javadoc("A " + className + " Couchbase document.");
        builder.annotation("Document");

        builder.classDeclaration("public", className, null, "Serializable");

        builder.line("private static final long serialVersionUID = 1L;");
        builder.line("public static final String PREFIX = \"" + docType + "::\";");
        builder.line();

        builder.annotation("Id");
        builder.annotation("GeneratedValue", "strategy = GenerationStrategy.UNIQUE");
        builder.field("private", "String", "id");
        builder.line();

        for (FieldConfig field : entity.getFields()) {
            if (field.isRequired()) {
                builder.annotation("NotNull");
            }
            builder.annotation("Field");
            builder.field("private", getJavaType(field), field.getFieldName());
            builder.line();
        }

        // Getters/setters
        builder.methodSignature("public", "String", "getId");
        builder.returnStatement("id");
        builder.closeMethod();

        builder.methodSignature("public", "void", "setId", "String id");
        builder.statement("this.id = id");
        builder.closeMethod();
        builder.line();

        for (FieldConfig field : entity.getFields()) {
            String fieldName = field.getFieldName();
            String fieldType = getJavaType(field);
            String cap = capitalize(fieldName);

            builder.methodSignature("public", fieldType, "get" + cap);
            builder.returnStatement(fieldName);
            builder.closeMethod();

            builder.methodSignature("public", "void", "set" + cap, fieldType + " " + fieldName);
            builder.statement("this." + fieldName + " = " + fieldName);
            builder.closeMethod();
            builder.line();
        }

        builder.closeClass();

        writeFile(getMainJavaPath() + "domain/" + className + ".java", builder.build());
    }

    private void writeCouchbaseRepository(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();
        String className = entity.getName();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".repository");

        builder.addImports(
            config.getPackageName() + ".domain." + className,
            "org.springframework.data.couchbase.repository.CouchbaseRepository",
            "org.springframework.stereotype.Repository"
        );

        builder.javadoc("Spring Data Couchbase repository for the " + className + " entity.");
        builder.annotation("Repository");

        builder.line("public interface " + className + "Repository extends CouchbaseRepository<" + className + ", String> {");
        builder.line();
        builder.line("}");

        writeFile(getMainJavaPath() + "repository/" + className + "Repository.java", builder.build());
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
                return type;
            default:
                return type;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
