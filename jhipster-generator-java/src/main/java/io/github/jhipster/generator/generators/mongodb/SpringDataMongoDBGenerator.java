/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.mongodb;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.RelationshipConfig;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Spring Data MongoDB Generator.
 * Generates MongoDB configuration, repositories, and Mongock migrations.
 * Equivalent to spring-data-mongodb/generator.ts.
 */
public class SpringDataMongoDBGenerator extends BaseApplicationGenerator {

    public SpringDataMongoDBGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "spring-data-mongodb";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.CONFIGURING, "configuringMongoDB", this::configuring);
        registerTask(GeneratorPriority.WRITING, "writingMongoDB", this::writing);
        registerTask(GeneratorPriority.WRITING_ENTITIES, "writingMongoDBEntities", this::writingEntities);
    }

    private void configuring() {
        log.info("Configuring MongoDB");

        JHipsterConfig config = getConfig();

        // Set MongoDB-specific configuration
        context.setConfigValue("mongodbUri", "mongodb://localhost:27017/" + config.getLowerBaseName());
        context.setConfigValue("mongodbDatabase", config.getLowerBaseName());
    }

    private void writing() throws Exception {
        log.info("Writing MongoDB configuration files");

        // Write MongoDB configuration
        writeMongoDBConfiguration();

        // Write Mongock configuration for migrations
        writeMongockConfiguration();

        // Write initial migration changelog
        writeInitialMigration();

        // Write audit event configuration for MongoDB
        writeAuditEventConfiguration();

        // Write TestContainers configuration
        writeTestContainersConfiguration();

        // Add MongoDB dependencies to pom.xml info
        writePomDependencies();
    }

    private void writingEntities() throws Exception {
        log.info("Writing MongoDB entities");

        for (EntityConfig entity : context.getEntities()) {
            writeMongoDocument(entity);
            writeMongoRepository(entity);
        }
    }

    private void writeMongoDBConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "com.mongodb.ConnectionString",
            "com.mongodb.MongoClientSettings",
            "com.mongodb.client.MongoClient",
            "com.mongodb.client.MongoClients",
            "org.springframework.boot.autoconfigure.mongo.MongoProperties",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.data.mongodb.MongoDatabaseFactory",
            "org.springframework.data.mongodb.config.AbstractMongoClientConfiguration",
            "org.springframework.data.mongodb.config.EnableMongoAuditing",
            "org.springframework.data.mongodb.core.MongoTemplate",
            "org.springframework.data.mongodb.core.convert.MongoCustomConversions",
            "org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener",
            "org.springframework.data.mongodb.repository.config.EnableMongoRepositories",
            "org.springframework.validation.beanvalidation.LocalValidatorFactoryBean",
            "org.bson.UuidRepresentation",
            "java.util.ArrayList",
            "java.util.List"
        );

        builder.javadoc("MongoDB configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableMongoRepositories", "\"" + config.getPackageName() + ".repository\"");
        builder.annotation("EnableMongoAuditing", "auditorAwareRef = \"springSecurityAuditorAware\"");

        builder.classDeclaration("public", "DatabaseConfiguration", "AbstractMongoClientConfiguration");

        builder.line("private final MongoProperties mongoProperties;");
        builder.line();

        // Constructor
        builder.methodSignature("public", null, "DatabaseConfiguration", "MongoProperties mongoProperties");
        builder.statement("this.mongoProperties = mongoProperties");
        builder.closeMethod();
        builder.line();

        // getDatabaseName
        builder.annotation("Override");
        builder.methodSignature("protected", "String", "getDatabaseName");
        builder.returnStatement("mongoProperties.getDatabase()");
        builder.closeMethod();
        builder.line();

        // mongoClient
        builder.annotation("Override");
        builder.annotation("Bean");
        builder.methodSignature("public", "MongoClient", "mongoClient");
        builder.line("String connectionString = mongoProperties.getUri() != null ? mongoProperties.getUri() : ");
        builder.line("    \"mongodb://\" + mongoProperties.getHost() + \":\" + mongoProperties.getPort() + \"/\" + mongoProperties.getDatabase();");
        builder.line();
        builder.line("MongoClientSettings settings = MongoClientSettings.builder()");
        builder.line("    .applyConnectionString(new ConnectionString(connectionString))");
        builder.line("    .uuidRepresentation(UuidRepresentation.STANDARD)");
        builder.line("    .build();");
        builder.line();
        builder.returnStatement("MongoClients.create(settings)");
        builder.closeMethod();
        builder.line();

        // validatingMongoEventListener
        builder.annotation("Bean");
        builder.methodSignature("public", "ValidatingMongoEventListener", "validatingMongoEventListener",
            "LocalValidatorFactoryBean factory");
        builder.returnStatement("new ValidatingMongoEventListener(factory)");
        builder.closeMethod();
        builder.line();

        // customConversions
        builder.annotation("Override");
        builder.annotation("Bean");
        builder.methodSignature("public", "MongoCustomConversions", "customConversions");
        builder.line("List<Object> converters = new ArrayList<>();");
        builder.line("converters.add(new JSR310DateConverters.DateToZonedDateTimeConverter());");
        builder.line("converters.add(new JSR310DateConverters.ZonedDateTimeToDateConverter());");
        builder.returnStatement("new MongoCustomConversions(converters)");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/DatabaseConfiguration.java", builder.build());

        // Write JSR310 date converters
        writeJSR310DateConverters();
    }

    private void writeJSR310DateConverters() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.core.convert.converter.Converter",
            "org.springframework.data.convert.ReadingConverter",
            "org.springframework.data.convert.WritingConverter",
            "java.time.ZoneId",
            "java.time.ZonedDateTime",
            "java.util.Date"
        );

        builder.javadoc("JSR-310 date converters for MongoDB.");
        builder.classDeclaration("public final", "JSR310DateConverters", null);

        builder.methodSignature("private", null, "JSR310DateConverters");
        builder.closeMethod();
        builder.line();

        // DateToZonedDateTimeConverter
        builder.annotation("ReadingConverter");
        builder.line("public static class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {");
        builder.indent();
        builder.annotation("Override");
        builder.methodSignature("public", "ZonedDateTime", "convert", "Date source");
        builder.returnStatement("source == null ? null : ZonedDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault())");
        builder.closeMethod();
        builder.outdent();
        builder.line("}");
        builder.line();

        // ZonedDateTimeToDateConverter
        builder.annotation("WritingConverter");
        builder.line("public static class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {");
        builder.indent();
        builder.annotation("Override");
        builder.methodSignature("public", "Date", "convert", "ZonedDateTime source");
        builder.returnStatement("source == null ? null : Date.from(source.toInstant())");
        builder.closeMethod();
        builder.outdent();
        builder.line("}");

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/JSR310DateConverters.java", builder.build());
    }

    private void writeMongockConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config.dbmigrations");

        builder.addImports(
            "io.mongock.runner.springboot.EnableMongock",
            "org.springframework.context.annotation.Configuration"
        );

        builder.javadoc("Mongock configuration for database migrations.");
        builder.annotation("Configuration");
        builder.annotation("EnableMongock");

        builder.classDeclaration("public", "MongockConfiguration", null);
        builder.closeClass();

        writeFile(getMainJavaPath() + "config/dbmigrations/MongockConfiguration.java", builder.build());
    }

    private void writeInitialMigration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config.dbmigrations");

        builder.addImports(
            "io.mongock.api.annotations.ChangeUnit",
            "io.mongock.api.annotations.Execution",
            "io.mongock.api.annotations.RollbackExecution",
            "org.springframework.data.mongodb.core.MongoTemplate"
        );

        if (!config.isSkipUserManagement()) {
            builder.addImports(
                config.getPackageName() + ".domain.Authority",
                config.getPackageName() + ".domain.User",
                config.getPackageName() + ".security.AuthoritiesConstants",
                "java.time.Instant",
                "java.util.Set"
            );
        }

        builder.javadoc("Initial database migration creating default data.");
        builder.annotation("ChangeUnit", "id = \"initial-setup\", order = \"001\", author = \"jhipster\"");

        builder.classDeclaration("public", "InitialSetupMigration", null);

        builder.line("private final MongoTemplate template;");
        builder.line();

        builder.methodSignature("public", null, "InitialSetupMigration", "MongoTemplate template");
        builder.statement("this.template = template");
        builder.closeMethod();
        builder.line();

        builder.annotation("Execution");
        builder.methodSignature("public", "void", "changeSet");

        if (!config.isSkipUserManagement()) {
            builder.lineComment("Create authorities");
            builder.line("Authority adminAuthority = new Authority();");
            builder.line("adminAuthority.setName(AuthoritiesConstants.ADMIN);");
            builder.line("template.save(adminAuthority);");
            builder.line();
            builder.line("Authority userAuthority = new Authority();");
            builder.line("userAuthority.setName(AuthoritiesConstants.USER);");
            builder.line("template.save(userAuthority);");
            builder.line();

            builder.lineComment("Create admin user");
            builder.line("User admin = new User();");
            builder.line("admin.setLogin(\"admin\");");
            builder.line("admin.setPassword(\"$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC\");");
            builder.line("admin.setFirstName(\"Administrator\");");
            builder.line("admin.setLastName(\"Administrator\");");
            builder.line("admin.setEmail(\"admin@localhost\");");
            builder.line("admin.setActivated(true);");
            builder.line("admin.setLangKey(\"en\");");
            builder.line("admin.setCreatedBy(\"system\");");
            builder.line("admin.setCreatedDate(Instant.now());");
            builder.line("admin.setAuthorities(Set.of(adminAuthority, userAuthority));");
            builder.line("template.save(admin);");
            builder.line();

            builder.lineComment("Create user");
            builder.line("User user = new User();");
            builder.line("user.setLogin(\"user\");");
            builder.line("user.setPassword(\"$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K\");");
            builder.line("user.setFirstName(\"User\");");
            builder.line("user.setLastName(\"User\");");
            builder.line("user.setEmail(\"user@localhost\");");
            builder.line("user.setActivated(true);");
            builder.line("user.setLangKey(\"en\");");
            builder.line("user.setCreatedBy(\"system\");");
            builder.line("user.setCreatedDate(Instant.now());");
            builder.line("user.setAuthorities(Set.of(userAuthority));");
            builder.line("template.save(user);");
        } else {
            builder.lineComment("Add initial setup logic here");
        }

        builder.closeMethod();
        builder.line();

        builder.annotation("RollbackExecution");
        builder.methodSignature("public", "void", "rollback");
        builder.lineComment("Rollback logic if needed");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/dbmigrations/InitialSetupMigration.java", builder.build());
    }

    private void writeAuditEventConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".domain");

        builder.addImports(
            "org.springframework.data.annotation.Id",
            "org.springframework.data.mongodb.core.mapping.Document",
            "org.springframework.data.mongodb.core.mapping.Field",
            "jakarta.validation.constraints.NotNull",
            "java.io.Serializable",
            "java.time.Instant",
            "java.util.HashMap",
            "java.util.Map"
        );

        builder.javadoc("Persistent audit event entity for MongoDB.");
        builder.annotation("Document", "collection = \"jhi_persistent_audit_event\"");

        builder.classDeclaration("public", "PersistentAuditEvent", null, "Serializable");

        builder.line("private static final long serialVersionUID = 1L;");
        builder.line();

        builder.annotation("Id");
        builder.field("private", "String", "id");
        builder.line();

        builder.annotation("NotNull");
        builder.annotation("Field", "\"principal\"");
        builder.field("private", "String", "principal");
        builder.line();

        builder.annotation("Field", "\"event_date\"");
        builder.field("private", "Instant", "auditEventDate");
        builder.line();

        builder.annotation("Field", "\"event_type\"");
        builder.field("private", "String", "auditEventType");
        builder.line();

        builder.annotation("Field", "\"data\"");
        builder.line("private Map<String, String> data = new HashMap<>();");
        builder.line();

        // Getters and setters
        String[] fields = {"id:String", "principal:String", "auditEventDate:Instant", "auditEventType:String"};
        for (String fieldDef : fields) {
            String[] parts = fieldDef.split(":");
            String fieldName = parts[0];
            String fieldType = parts[1];
            String capitalizedName = capitalize(fieldName);

            builder.methodSignature("public", fieldType, "get" + capitalizedName);
            builder.returnStatement(fieldName);
            builder.closeMethod();

            builder.methodSignature("public", "void", "set" + capitalizedName, fieldType + " " + fieldName);
            builder.statement("this." + fieldName + " = " + fieldName);
            builder.closeMethod();
        }

        builder.methodSignature("public", "Map<String, String>", "getData");
        builder.returnStatement("data");
        builder.closeMethod();

        builder.methodSignature("public", "void", "setData", "Map<String, String> data");
        builder.statement("this.data = data");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "domain/PersistentAuditEvent.java", builder.build());
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
            "org.testcontainers.containers.MongoDBContainer",
            "org.testcontainers.containers.output.Slf4jLogConsumer",
            "java.util.Collections"
        );

        builder.javadoc("TestContainers configuration for MongoDB.");
        builder.classDeclaration("public", "MongoDbTestContainer", null, "InitializingBean", "DisposableBean");

        builder.line("private static final Logger log = LoggerFactory.getLogger(MongoDbTestContainer.class);");
        builder.line();
        builder.line("private MongoDBContainer mongoDBContainer;");
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "destroy");
        builder.line("if (mongoDBContainer != null && mongoDBContainer.isRunning()) {");
        builder.indent();
        builder.line("mongoDBContainer.stop();");
        builder.outdent();
        builder.line("}");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "void", "afterPropertiesSet");
        builder.line("if (mongoDBContainer != null && mongoDBContainer.isRunning()) {");
        builder.indent();
        builder.returnStatement(null);
        builder.outdent();
        builder.line("}");
        builder.line();
        builder.line("mongoDBContainer = new MongoDBContainer(\"mongo:7.0\")");
        builder.line("    .withTmpFs(Collections.singletonMap(\"/data/db\", \"rw\"))");
        builder.line("    .withLogConsumer(new Slf4jLogConsumer(log))");
        builder.line("    .withReuse(true);");
        builder.line();
        builder.line("mongoDBContainer.start();");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "MongoDBContainer", "getMongoDBContainer");
        builder.returnStatement("mongoDBContainer");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getTestJavaPath() + "config/MongoDbTestContainer.java", builder.build());
    }

    private void writePomDependencies() throws Exception {
        // This would typically add dependencies to pom.xml
        // For now, we document the required dependencies
        StringBuilder deps = new StringBuilder();
        deps.append("<!-- MongoDB Dependencies for pom.xml -->\n");
        deps.append("<dependency>\n");
        deps.append("    <groupId>org.springframework.boot</groupId>\n");
        deps.append("    <artifactId>spring-boot-starter-data-mongodb</artifactId>\n");
        deps.append("</dependency>\n");
        deps.append("<dependency>\n");
        deps.append("    <groupId>io.mongock</groupId>\n");
        deps.append("    <artifactId>mongock-springboot-v3</artifactId>\n");
        deps.append("    <version>5.4.0</version>\n");
        deps.append("</dependency>\n");
        deps.append("<dependency>\n");
        deps.append("    <groupId>io.mongock</groupId>\n");
        deps.append("    <artifactId>mongodb-springdata-v4-driver</artifactId>\n");
        deps.append("    <version>5.4.0</version>\n");
        deps.append("</dependency>\n");
        deps.append("<dependency>\n");
        deps.append("    <groupId>org.testcontainers</groupId>\n");
        deps.append("    <artifactId>mongodb</artifactId>\n");
        deps.append("    <scope>test</scope>\n");
        deps.append("</dependency>\n");

        context.setConfigValue("mongodbDependencies", deps.toString());
    }

    private void writeMongoDocument(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".domain");

        builder.addImports(
            "org.springframework.data.annotation.Id",
            "org.springframework.data.mongodb.core.mapping.Document",
            "org.springframework.data.mongodb.core.mapping.Field",
            "org.springframework.data.mongodb.core.mapping.DBRef",
            "jakarta.validation.constraints.*",
            "java.io.Serializable",
            "java.util.Objects"
        );

        // Add field type imports
        for (FieldConfig field : entity.getFields()) {
            addFieldImports(builder, field);
        }

        String className = entity.getName();
        String collectionName = toSnakeCase(className);

        builder.javadoc("A " + className + " MongoDB document.");
        builder.annotation("Document", "collection = \"" + collectionName + "\"");

        builder.classDeclaration("public", className, null, "Serializable");

        builder.line("private static final long serialVersionUID = 1L;");
        builder.line();

        // ID field
        builder.annotation("Id");
        builder.field("private", "String", "id");
        builder.line();

        // Entity fields
        for (FieldConfig field : entity.getFields()) {
            writeMongoFieldAnnotations(builder, field);
            builder.field("private", getJavaType(field), field.getFieldName());
            builder.line();
        }

        // Relationships
        for (RelationshipConfig rel : entity.getRelationships()) {
            writeMongoRelationship(builder, rel);
        }

        // Getters and setters
        builder.methodSignature("public", "String", "getId");
        builder.returnStatement("id");
        builder.closeMethod();

        builder.methodSignature("public", "void", "setId", "String id");
        builder.statement("this.id = id");
        builder.closeMethod();

        builder.methodSignature("public", className, "id", "String id");
        builder.statement("this.id = id");
        builder.returnStatement("this");
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

    private void writeMongoFieldAnnotations(JavaCodeBuilder builder, FieldConfig field) {
        // Validation annotations
        if (field.isRequired()) {
            builder.annotation("NotNull");
        }
        if (field.getMinLength() != null) {
            builder.annotation("Size", "min = " + field.getMinLength());
        }
        if (field.getMaxLength() != null) {
            builder.annotation("Size", "max = " + field.getMaxLength());
        }
        if (field.getPattern() != null) {
            builder.annotation("Pattern", "regexp = \"" + field.getPattern() + "\"");
        }

        // MongoDB Field annotation
        builder.annotation("Field", "\"" + toSnakeCase(field.getFieldName()) + "\"");
    }

    private void writeMongoRelationship(JavaCodeBuilder builder, RelationshipConfig rel) {
        String relType = rel.getRelationshipType();
        String otherEntity = rel.getOtherEntityName();
        String fieldName = rel.getRelationshipName();

        builder.annotation("DBRef");

        if ("one-to-many".equals(relType) || "many-to-many".equals(relType)) {
            builder.addImport("java.util.HashSet");
            builder.addImport("java.util.Set");
            builder.line("private Set<" + capitalize(otherEntity) + "> " + fieldName + " = new HashSet<>();");
        } else {
            builder.line("private " + capitalize(otherEntity) + " " + fieldName + ";");
        }
        builder.line();
    }

    private void writeMongoRepository(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();
        String className = entity.getName();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".repository");

        builder.addImports(
            config.getPackageName() + ".domain." + className,
            "org.springframework.data.mongodb.repository.MongoRepository",
            "org.springframework.stereotype.Repository",
            "java.util.Optional"
        );

        builder.javadoc("Spring Data MongoDB repository for the " + className + " entity.");
        builder.annotation("Repository");

        builder.line("public interface " + className + "Repository extends MongoRepository<" + className + ", String> {");
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
            case "Duration":
                builder.addImport("java.time.Duration");
                break;
            case "BigDecimal":
                builder.addImport("java.math.BigDecimal");
                break;
            case "UUID":
                builder.addImport("java.util.UUID");
                break;
            case "byte[]":
                builder.addImport("java.util.Arrays");
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
                return type;
            case "LocalDate":
            case "ZonedDateTime":
            case "Instant":
            case "Duration":
            case "BigDecimal":
            case "UUID":
                return type;
            case "byte[]":
                return "byte[]";
            case "TextBlob":
                return "String";
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

        builder.methodSignature("public", className, uncapitalize(fieldName), fieldType + " " + fieldName);
        builder.statement("this." + fieldName + " = " + fieldName);
        builder.returnStatement("this");
        builder.closeMethod();
        builder.line();
    }

    private void writeEqualsHashCodeToString(JavaCodeBuilder builder, String className) {
        // equals
        builder.annotation("Override");
        builder.methodSignature("public", "boolean", "equals", "Object o");
        builder.line("if (this == o) return true;");
        builder.line("if (!(o instanceof " + className + ")) return false;");
        builder.line(className + " that = (" + className + ") o;");
        builder.returnStatement("id != null && id.equals(that.id)");
        builder.closeMethod();
        builder.line();

        // hashCode
        builder.annotation("Override");
        builder.methodSignature("public", "int", "hashCode");
        builder.returnStatement("getClass().hashCode()");
        builder.closeMethod();
        builder.line();

        // toString
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

    private String uncapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}
