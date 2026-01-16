/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.neo4j;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.RelationshipConfig;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Spring Data Neo4j Generator.
 * Generates Neo4j configuration, entities as nodes, and repositories.
 * Equivalent to spring-data-neo4j/generator.ts.
 */
public class SpringDataNeo4jGenerator extends BaseApplicationGenerator {

    public SpringDataNeo4jGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "spring-data-neo4j";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.CONFIGURING, "configuringNeo4j", this::configuring);
        registerTask(GeneratorPriority.WRITING, "writingNeo4j", this::writing);
        registerTask(GeneratorPriority.WRITING_ENTITIES, "writingNeo4jEntities", this::writingEntities);
    }

    private void configuring() {
        log.info("Configuring Neo4j");

        JHipsterConfig config = getConfig();

        context.setConfigValue("neo4jUri", "bolt://localhost:7687");
        context.setConfigValue("neo4jDatabase", config.getLowerBaseName());
    }

    private void writing() throws Exception {
        log.info("Writing Neo4j configuration files");

        writeNeo4jConfiguration();
        writeNeo4jMigrationConfiguration();
        writeInitialMigration();
        writeTestContainersConfiguration();
    }

    private void writingEntities() throws Exception {
        log.info("Writing Neo4j entities");

        for (EntityConfig entity : context.getEntities()) {
            writeNeo4jNode(entity);
            writeNeo4jRepository(entity);
        }
    }

    private void writeNeo4jConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.neo4j.driver.Driver",
            "org.springframework.boot.autoconfigure.neo4j.Neo4jProperties",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.data.neo4j.config.EnableNeo4jAuditing",
            "org.springframework.data.neo4j.core.DatabaseSelectionProvider",
            "org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager",
            "org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories",
            "org.springframework.transaction.PlatformTransactionManager",
            "org.springframework.transaction.annotation.EnableTransactionManagement"
        );

        builder.javadoc("Neo4j configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableNeo4jRepositories", "basePackages = \"" + config.getPackageName() + ".repository\"");
        builder.annotation("EnableNeo4jAuditing", "auditorAwareRef = \"springSecurityAuditorAware\"");
        builder.annotation("EnableTransactionManagement");

        builder.classDeclaration("public", "DatabaseConfiguration", null);

        builder.line("private final Neo4jProperties neo4jProperties;");
        builder.line();

        builder.methodSignature("public", null, "DatabaseConfiguration", "Neo4jProperties neo4jProperties");
        builder.statement("this.neo4jProperties = neo4jProperties");
        builder.closeMethod();
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "PlatformTransactionManager", "transactionManager",
            "Driver driver, DatabaseSelectionProvider databaseSelectionProvider");
        builder.returnStatement("new Neo4jTransactionManager(driver, databaseSelectionProvider)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "DatabaseSelectionProvider", "databaseSelectionProvider");
        builder.line("String database = neo4jProperties.getDatabase();");
        builder.line("if (database != null && !database.isEmpty()) {");
        builder.indent();
        builder.returnStatement("DatabaseSelectionProvider.createStaticDatabaseSelectionProvider(database)");
        builder.outdent();
        builder.line("}");
        builder.returnStatement("DatabaseSelectionProvider.getDefaultSelectionProvider()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/DatabaseConfiguration.java", builder.build());
    }

    private void writeNeo4jMigrationConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "ac.simons.neo4j.migrations.core.Migrations",
            "ac.simons.neo4j.migrations.core.MigrationsConfig",
            "ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration",
            "org.neo4j.driver.Driver",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.boot.autoconfigure.AutoConfigureAfter",
            "org.springframework.boot.autoconfigure.condition.ConditionalOnBean",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.core.io.ResourceLoader"
        );

        builder.javadoc("Neo4j migrations configuration.");
        builder.annotation("Configuration");
        builder.annotation("AutoConfigureAfter", "MigrationsAutoConfiguration.class");
        builder.annotation("ConditionalOnBean", "Driver.class");

        builder.classDeclaration("public", "Neo4jMigrationsConfiguration", null);

        builder.line("private static final Logger log = LoggerFactory.getLogger(Neo4jMigrationsConfiguration.class);");
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "Migrations", "neo4jMigrations", "Driver driver, ResourceLoader resourceLoader");
        builder.line("log.info(\"Configuring Neo4j migrations\");");
        builder.line("MigrationsConfig config = MigrationsConfig.builder()");
        builder.line("    .withLocationsToScan(\"classpath:config/neo4j/migrations\")");
        builder.line("    .build();");
        builder.line();
        builder.line("Migrations migrations = new Migrations(config, driver);");
        builder.line("migrations.apply();");
        builder.returnStatement("migrations");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/Neo4jMigrationsConfiguration.java", builder.build());
    }

    private void writeInitialMigration() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder cypher = new StringBuilder();
        cypher.append("// V0001__Initial_setup.cypher\n\n");

        cypher.append("// Create constraints for unique identifiers\n");
        cypher.append("CREATE CONSTRAINT user_login IF NOT EXISTS FOR (u:User) REQUIRE u.login IS UNIQUE;\n");
        cypher.append("CREATE CONSTRAINT user_email IF NOT EXISTS FOR (u:User) REQUIRE u.email IS UNIQUE;\n\n");

        if (!config.isSkipUserManagement()) {
            cypher.append("// Create default admin user\n");
            cypher.append("MERGE (admin:User {login: 'admin'})\n");
            cypher.append("SET admin.password = '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC',\n");
            cypher.append("    admin.firstName = 'Administrator',\n");
            cypher.append("    admin.lastName = 'Administrator',\n");
            cypher.append("    admin.email = 'admin@localhost',\n");
            cypher.append("    admin.activated = true,\n");
            cypher.append("    admin.langKey = 'en',\n");
            cypher.append("    admin.createdBy = 'system',\n");
            cypher.append("    admin.createdDate = datetime()\n");
            cypher.append("MERGE (adminRole:Authority {name: 'ROLE_ADMIN'})\n");
            cypher.append("MERGE (userRole:Authority {name: 'ROLE_USER'})\n");
            cypher.append("MERGE (admin)-[:HAS_AUTHORITY]->(adminRole)\n");
            cypher.append("MERGE (admin)-[:HAS_AUTHORITY]->(userRole);\n\n");

            cypher.append("// Create default user\n");
            cypher.append("MERGE (user:User {login: 'user'})\n");
            cypher.append("SET user.password = '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K',\n");
            cypher.append("    user.firstName = 'User',\n");
            cypher.append("    user.lastName = 'User',\n");
            cypher.append("    user.email = 'user@localhost',\n");
            cypher.append("    user.activated = true,\n");
            cypher.append("    user.langKey = 'en',\n");
            cypher.append("    user.createdBy = 'system',\n");
            cypher.append("    user.createdDate = datetime()\n");
            cypher.append("MERGE (userRole:Authority {name: 'ROLE_USER'})\n");
            cypher.append("MERGE (user)-[:HAS_AUTHORITY]->(userRole);\n");
        }

        writeFile(getMainResourcesPath() + "config/neo4j/migrations/V0001__Initial_setup.cypher", cypher.toString());
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
            "org.testcontainers.containers.Neo4jContainer",
            "org.testcontainers.utility.DockerImageName"
        );

        builder.javadoc("TestContainers configuration for Neo4j.");
        builder.classDeclaration("public", "Neo4jTestContainer", null, "InitializingBean", "DisposableBean");

        builder.line("private static final Logger log = LoggerFactory.getLogger(Neo4jTestContainer.class);");
        builder.line();
        builder.line("private Neo4jContainer<?> neo4jContainer;");
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "destroy");
        builder.line("if (neo4jContainer != null && neo4jContainer.isRunning()) {");
        builder.indent();
        builder.line("neo4jContainer.stop();");
        builder.outdent();
        builder.line("}");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "afterPropertiesSet");
        builder.line("if (neo4jContainer != null && neo4jContainer.isRunning()) {");
        builder.indent();
        builder.returnStatement(null);
        builder.outdent();
        builder.line("}");
        builder.line();
        builder.line("neo4jContainer = new Neo4jContainer<>(DockerImageName.parse(\"neo4j:5\"))");
        builder.line("    .withoutAuthentication()");
        builder.line("    .withReuse(true);");
        builder.line();
        builder.line("neo4jContainer.start();");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "Neo4jContainer<?>", "getNeo4jContainer");
        builder.returnStatement("neo4jContainer");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getTestJavaPath() + "config/Neo4jTestContainer.java", builder.build());
    }

    private void writeNeo4jNode(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".domain");

        builder.addImports(
            "org.springframework.data.neo4j.core.schema.GeneratedValue",
            "org.springframework.data.neo4j.core.schema.Id",
            "org.springframework.data.neo4j.core.schema.Node",
            "org.springframework.data.neo4j.core.schema.Property",
            "org.springframework.data.neo4j.core.schema.Relationship",
            "org.springframework.data.neo4j.core.support.UUIDStringGenerator",
            "jakarta.validation.constraints.*",
            "java.io.Serializable"
        );

        String className = entity.getName();

        builder.javadoc("A " + className + " Neo4j node.");
        builder.annotation("Node", "\"" + className + "\"");

        builder.classDeclaration("public", className, null, "Serializable");

        builder.line("private static final long serialVersionUID = 1L;");
        builder.line();

        builder.annotation("Id");
        builder.annotation("GeneratedValue", "generatorClass = UUIDStringGenerator.class");
        builder.field("private", "String", "id");
        builder.line();

        for (FieldConfig field : entity.getFields()) {
            if (field.isRequired()) {
                builder.annotation("NotNull");
            }
            builder.annotation("Property", "\"" + field.getFieldName() + "\"");
            builder.field("private", getJavaType(field), field.getFieldName());
            builder.line();
        }

        // Relationships
        for (RelationshipConfig rel : entity.getRelationships()) {
            writeNeo4jRelationship(builder, rel);
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

        // equals, hashCode, toString
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

        builder.closeClass();

        writeFile(getMainJavaPath() + "domain/" + className + ".java", builder.build());
    }

    private void writeNeo4jRelationship(JavaCodeBuilder builder, RelationshipConfig rel) {
        String relType = rel.getRelationshipType();
        String otherEntity = capitalize(rel.getOtherEntityName());
        String fieldName = rel.getRelationshipName();
        String relName = toUpperSnakeCase(fieldName);

        if ("one-to-many".equals(relType) || "many-to-many".equals(relType)) {
            builder.addImport("java.util.HashSet");
            builder.addImport("java.util.Set");
            builder.annotation("Relationship", "type = \"" + relName + "\", direction = Relationship.Direction.OUTGOING");
            builder.line("private Set<" + otherEntity + "> " + fieldName + " = new HashSet<>();");
        } else {
            builder.annotation("Relationship", "type = \"" + relName + "\", direction = Relationship.Direction.OUTGOING");
            builder.line("private " + otherEntity + " " + fieldName + ";");
        }
        builder.line();
    }

    private void writeNeo4jRepository(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();
        String className = entity.getName();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".repository");

        builder.addImports(
            config.getPackageName() + ".domain." + className,
            "org.springframework.data.neo4j.repository.Neo4jRepository",
            "org.springframework.stereotype.Repository"
        );

        builder.javadoc("Spring Data Neo4j repository for the " + className + " entity.");
        builder.annotation("Repository");

        builder.line("public interface " + className + "Repository extends Neo4jRepository<" + className + ", String> {");
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

    private String toUpperSnakeCase(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }
}
