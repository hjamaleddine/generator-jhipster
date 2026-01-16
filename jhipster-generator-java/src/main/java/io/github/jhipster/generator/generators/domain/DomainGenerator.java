/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.domain;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.PrimaryKeyConfig;
import io.github.jhipster.generator.model.RelationshipConfig;
import io.github.jhipster.generator.support.JavaReservedKeywords;
import io.github.jhipster.generator.template.JavaCodeBuilder;

import java.util.*;

/**
 * Domain Generator.
 * Generates JPA entity classes.
 * Equivalent to java/generators/domain/generator.ts.
 */
public class DomainGenerator extends BaseApplicationGenerator {

    public DomainGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "domain";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.PREPARING, "preparingDomain", this::preparing);
    }

    private void preparing() {
        log.info("Preparing domain generator");
    }

    @Override
    protected void preparingEachEntity(EntityConfig entity) {
        log.debug("Preparing entity: {}", entity.getName());

        // Check for reserved Java keywords
        if (JavaReservedKeywords.isReserved(entity.getName())) {
            log.warn("Entity name '{}' is a Java reserved keyword", entity.getName());
        }

        // Set entity domain layer flag
        context.setConfigValue("entityDomainLayer_" + entity.getName(), true);

        // Initialize primary key if not set
        if (entity.getPrimaryKey() == null) {
            FieldConfig idField = entity.getIdField();
            if (idField != null) {
                PrimaryKeyConfig pk = new PrimaryKeyConfig(idField.getFieldName(), idField.getJavaFieldType());
                pk.setField(idField);
                entity.setPrimaryKey(pk);
            } else {
                entity.setPrimaryKey(PrimaryKeyConfig.defaultLong());
            }
        }

        // Set computed names
        if (entity.getPersistClass() == null) {
            entity.setPersistClass(entity.getName());
        }
        if (entity.getPersistInstance() == null) {
            entity.setPersistInstance(JavaCodeBuilder.decapitalize(entity.getName()));
        }
    }

    @Override
    protected void preparingEachEntityField(EntityConfig entity, FieldConfig field) {
        log.debug("Preparing field: {}.{}", entity.getName(), field.getFieldName());

        // Check for reserved keywords
        if (JavaReservedKeywords.isReserved(field.getFieldName())) {
            log.warn("Field name '{}' is a Java reserved keyword", field.getFieldName());
        }

        // Set Java bean method name
        if (field.getFieldInJavaBeanMethod() == null) {
            field.setFieldInJavaBeanMethod(JavaCodeBuilder.capitalize(field.getFieldName()));
        }

        // Set validation required flag
        field.setFieldValidationRequired(field.hasValidationRule("required"));

        // Set content type flag for blobs
        field.setFieldWithContentType(field.isBlob() && !field.isTextBlob());
    }

    @Override
    protected void preparingEachEntityRelationship(EntityConfig entity, RelationshipConfig relationship) {
        log.debug("Preparing relationship: {}.{}", entity.getName(), relationship.getRelationshipName());

        // Check for reserved keywords
        if (JavaReservedKeywords.isReserved(relationship.getRelationshipName())) {
            log.warn("Relationship name '{}' is a Java reserved keyword", relationship.getRelationshipName());
        }

        // Set computed names
        if (relationship.getRelationshipFieldName() == null) {
            relationship.setRelationshipFieldName(relationship.getRelationshipName());
        }

        // Link to other entity if exists
        EntityConfig otherEntity = getEntity(relationship.getOtherEntityName());
        if (otherEntity != null) {
            relationship.setOtherEntity(otherEntity);

            // Find and link the other side of the relationship
            if (relationship.getOtherEntityRelationshipName() != null) {
                for (RelationshipConfig otherRel : otherEntity.getRelationships()) {
                    if (otherRel.getRelationshipName().equals(relationship.getOtherEntityRelationshipName())) {
                        relationship.setOtherRelationship(otherRel);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void postPreparingEachEntity(EntityConfig entity) {
        log.debug("Post-preparing entity: {}", entity.getName());

        // Check for cyclic required relationships
        checkCyclicRelationships(entity);
    }

    @Override
    protected void writingEntity(EntityConfig entity) throws Exception {
        if (Boolean.TRUE.equals(entity.getSkipServer())) {
            log.info("Skipping server generation for entity: {}", entity.getName());
            return;
        }

        log.info("Writing entity: {}", entity.getName());

        // Write domain class
        writeEntityClass(entity);

        // Write enum classes
        writeEnumClasses(entity);
    }

    private void writeEntityClass(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();
        String packageName = entity.getEntityAbsolutePackage() != null
            ? entity.getEntityAbsolutePackage()
            : config.getPackageName();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".domain");

        // Imports
        addEntityImports(builder, entity);

        // Class Javadoc
        if (entity.getJavadoc() != null) {
            builder.javadoc(entity.getJavadoc());
        } else {
            builder.javadoc("A " + entity.getPersistClass() + ".");
        }

        // Annotations
        builder.annotation("Entity");
        builder.annotation("Table", "name = \"" + entity.getEntityTableName() + "\"");
        builder.annotation("SuppressWarnings", "\"common-java:DuplicatedBlocks\"");

        // Class declaration
        builder.classDeclaration("public", entity.getPersistClass(), null, "Serializable");

        // Serial version UID
        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        // Primary key field
        writePrimaryKeyField(builder, entity);

        // Regular fields
        for (FieldConfig field : entity.getFields()) {
            if (!Boolean.TRUE.equals(field.getId())) {
                writeField(builder, entity, field);
            }
        }

        // Relationship fields
        for (RelationshipConfig rel : entity.getRelationships()) {
            writeRelationshipField(builder, entity, rel);
        }

        builder.lineComment("jhipster-needle-entity-add-field - JHipster will add fields here");
        builder.line();

        // Getters and Setters
        writeFieldAccessors(builder, entity);
        writeRelationshipAccessors(builder, entity);

        builder.lineComment("jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here");
        builder.line();

        // equals, hashCode, toString
        writeEqualsHashCodeToString(builder, entity);

        builder.closeClass();

        String entityPath = entity.getEntityPackage() != null && !entity.getEntityPackage().isEmpty()
            ? entity.getEntityPackage().replace('.', '/') + "/"
            : "";
        writeFile(getMainJavaPath() + entityPath + "domain/" + entity.getPersistClass() + ".java", builder.build());
    }

    private void addEntityImports(JavaCodeBuilder builder, EntityConfig entity) {
        // JPA imports
        builder.addImport("jakarta.persistence.*");
        builder.addImport("jakarta.validation.constraints.*");

        // Java imports
        builder.addImport("java.io.Serializable");

        // Type-specific imports
        for (FieldConfig field : entity.getFields()) {
            switch (field.getFieldType()) {
                case "BigDecimal":
                    builder.addImport("java.math.BigDecimal");
                    break;
                case "LocalDate":
                    builder.addImport("java.time.LocalDate");
                    break;
                case "Instant":
                    builder.addImport("java.time.Instant");
                    break;
                case "ZonedDateTime":
                    builder.addImport("java.time.ZonedDateTime");
                    break;
                case "Duration":
                    builder.addImport("java.time.Duration");
                    break;
                case "UUID":
                    builder.addImport("java.util.UUID");
                    break;
            }
        }

        // Collection imports for relationships
        if (entity.hasCollectionRelationships()) {
            builder.addImport("java.util.HashSet");
            builder.addImport("java.util.Set");
        }

        // Jackson imports
        boolean hasOtherSideIgnore = entity.getRelationships().stream()
            .anyMatch(r -> r.getOtherRelationship() != null);
        if (hasOtherSideIgnore) {
            builder.addImport("com.fasterxml.jackson.annotation.JsonIgnoreProperties");
        }
    }

    private void writePrimaryKeyField(JavaCodeBuilder builder, EntityConfig entity) {
        PrimaryKeyConfig pk = entity.getPrimaryKey();
        if (pk == null) return;

        builder.annotation("Id");

        // Auto-generation strategy
        if (pk.isLong() || pk.isInteger()) {
            builder.annotation("GeneratedValue", "strategy = GenerationType.SEQUENCE, generator = \"sequenceGenerator\"");
            builder.annotation("SequenceGenerator", "name = \"sequenceGenerator\"");
        } else if (pk.isUUID()) {
            builder.annotation("GeneratedValue", "strategy = GenerationType.UUID");
        }

        builder.annotation("Column", "name = \"" + pk.getName() + "\"");
        builder.field("private", pk.getType(), pk.getName());
        builder.line();
    }

    private void writeField(JavaCodeBuilder builder, EntityConfig entity, FieldConfig field) {
        // Field Javadoc
        if (field.getJavadoc() != null) {
            builder.javadoc(field.getJavadoc());
        }

        // Validation annotations
        writeValidationAnnotations(builder, field);

        // Column annotation
        if (field.isEnumField()) {
            builder.annotation("Enumerated", "EnumType.STRING");
        }

        StringBuilder columnAttr = new StringBuilder();
        columnAttr.append("name = \"").append(field.getColumnName()).append("\"");
        if (field.isFieldValidationRequired()) {
            columnAttr.append(", nullable = false");
        }
        builder.annotation("Column", columnAttr.toString());

        // Field declaration
        builder.field("private", field.getJavaFieldType(), field.getFieldName());
        builder.line();

        // Content type field for blobs
        if (field.isFieldWithContentType()) {
            builder.annotation("Column", "name = \"" + field.getColumnName() + "_content_type\"");
            builder.field("private", "String", field.getFieldName() + "ContentType");
            builder.line();
        }
    }

    private void writeValidationAnnotations(JavaCodeBuilder builder, FieldConfig field) {
        if (!field.hasValidation()) return;

        if (field.hasValidationRule("required")) {
            builder.annotation("NotNull");
        }

        if (field.hasValidationRule("minlength") || field.hasValidationRule("maxlength")) {
            StringBuilder sizeAttr = new StringBuilder();
            if (field.getFieldValidateRulesMinlength() != null) {
                sizeAttr.append("min = ").append(field.getFieldValidateRulesMinlength());
            }
            if (field.getFieldValidateRulesMaxlength() != null) {
                if (sizeAttr.length() > 0) sizeAttr.append(", ");
                sizeAttr.append("max = ").append(field.getFieldValidateRulesMaxlength());
            }
            builder.annotation("Size", sizeAttr.toString());
        }

        if (field.hasValidationRule("min") && field.getFieldValidateRulesMin() != null) {
            builder.annotation("Min", "value = " + field.getFieldValidateRulesMin());
        }

        if (field.hasValidationRule("max") && field.getFieldValidateRulesMax() != null) {
            builder.annotation("Max", "value = " + field.getFieldValidateRulesMax());
        }

        if (field.hasValidationRule("pattern") && field.getFieldValidateRulesPattern() != null) {
            builder.annotation("Pattern", "regexp = \"" + JavaCodeBuilder.escapeString(field.getFieldValidateRulesPattern()) + "\"");
        }
    }

    private void writeRelationshipField(JavaCodeBuilder builder, EntityConfig entity, RelationshipConfig rel) {
        // Javadoc
        if (rel.getJavadoc() != null) {
            builder.javadoc(rel.getJavadoc());
        }

        String otherEntityClass = rel.getOtherEntity() != null
            ? rel.getOtherEntity().getPersistClass()
            : JavaCodeBuilder.capitalize(rel.getOtherEntityName());

        // JPA annotation
        if (rel.isOneToMany()) {
            writeOneToManyAnnotation(builder, rel, otherEntityClass);
        } else if (rel.isManyToOne()) {
            writeManyToOneAnnotation(builder, rel);
        } else if (rel.isManyToMany()) {
            writeManyToManyAnnotation(builder, rel, otherEntityClass);
        } else if (rel.isOneToOne()) {
            writeOneToOneAnnotation(builder, rel, otherEntityClass);
        }

        // JsonIgnoreProperties
        if (rel.getOtherEntity() != null && !rel.getOtherEntity().getRelationships().isEmpty()) {
            StringBuilder ignoreProps = new StringBuilder();
            ignoreProps.append("value = {");
            List<String> props = new ArrayList<>();
            for (RelationshipConfig otherRel : rel.getOtherEntity().getRelationships()) {
                props.add("\"" + otherRel.getPropertyName() + "\"");
            }
            ignoreProps.append(String.join(", ", props));
            ignoreProps.append("}, allowSetters = true");
            builder.annotation("JsonIgnoreProperties", ignoreProps.toString());
        }

        // Field declaration
        if (rel.isCollection()) {
            builder.field("private", "Set<" + otherEntityClass + ">", rel.getRelationshipFieldNamePlural(), "new HashSet<>()");
        } else {
            builder.field("private", otherEntityClass, rel.getRelationshipFieldName());
        }
        builder.line();
    }

    private void writeOneToManyAnnotation(JavaCodeBuilder builder, RelationshipConfig rel, String otherEntityClass) {
        StringBuilder attr = new StringBuilder();
        attr.append("mappedBy = \"").append(rel.getOtherEntityRelationshipName()).append("\"");
        builder.annotation("OneToMany", attr.toString());
    }

    private void writeManyToOneAnnotation(JavaCodeBuilder builder, RelationshipConfig rel) {
        if (rel.isRequired()) {
            builder.annotation("ManyToOne", "optional = false");
            builder.annotation("NotNull");
        } else {
            builder.annotation("ManyToOne");
        }
        builder.annotation("JoinColumn", "name = \"" + rel.getColumnName() + "\"");
    }

    private void writeManyToManyAnnotation(JavaCodeBuilder builder, RelationshipConfig rel, String otherEntityClass) {
        if (rel.isOwnerSide()) {
            builder.annotation("ManyToMany");
            String joinTableName = rel.getJoinTable() != null ? rel.getJoinTable().getName()
                : "rel_" + JavaCodeBuilder.decapitalize(rel.getRelationshipName()) + "__" + rel.getOtherEntityName().toLowerCase();
            builder.annotation("JoinTable",
                "name = \"" + joinTableName + "\", " +
                "joinColumns = @JoinColumn(name = \"" + rel.getColumnName() + "\"), " +
                "inverseJoinColumns = @JoinColumn(name = \"" + rel.getOtherEntityName().toLowerCase() + "_id\")");
        } else {
            builder.annotation("ManyToMany", "mappedBy = \"" + rel.getOtherEntityRelationshipName() + "\"");
        }
    }

    private void writeOneToOneAnnotation(JavaCodeBuilder builder, RelationshipConfig rel, String otherEntityClass) {
        if (rel.isOwnerSide()) {
            if (rel.isRequired()) {
                builder.annotation("OneToOne", "optional = false");
                builder.annotation("NotNull");
            } else {
                builder.annotation("OneToOne");
            }
            builder.annotation("JoinColumn", "unique = true, name = \"" + rel.getColumnName() + "\"");
        } else {
            builder.annotation("OneToOne", "mappedBy = \"" + rel.getOtherEntityRelationshipName() + "\"");
        }
    }

    private void writeFieldAccessors(JavaCodeBuilder builder, EntityConfig entity) {
        // Primary key getter/setter
        PrimaryKeyConfig pk = entity.getPrimaryKey();
        if (pk != null) {
            writeGetter(builder, pk.getType(), pk.getName(), pk.getNameCapitalized());
            writeSetter(builder, pk.getType(), pk.getName(), pk.getNameCapitalized());
            if (Boolean.TRUE.equals(entity.getFluentMethods())) {
                writeFluentSetter(builder, entity.getPersistClass(), pk.getType(), pk.getName(), pk.getNameCapitalized());
            }
        }

        // Field getters/setters
        for (FieldConfig field : entity.getFields()) {
            if (Boolean.TRUE.equals(field.getId())) continue;

            writeGetter(builder, field.getJavaFieldType(), field.getFieldName(), field.getFieldInJavaBeanMethod());
            writeSetter(builder, field.getJavaFieldType(), field.getFieldName(), field.getFieldInJavaBeanMethod());

            if (Boolean.TRUE.equals(entity.getFluentMethods())) {
                writeFluentSetter(builder, entity.getPersistClass(), field.getJavaFieldType(), field.getFieldName(), field.getFieldInJavaBeanMethod());
            }

            // Content type accessors
            if (field.isFieldWithContentType()) {
                writeGetter(builder, "String", field.getFieldName() + "ContentType", field.getFieldInJavaBeanMethod() + "ContentType");
                writeSetter(builder, "String", field.getFieldName() + "ContentType", field.getFieldInJavaBeanMethod() + "ContentType");
            }
        }
    }

    private void writeRelationshipAccessors(JavaCodeBuilder builder, EntityConfig entity) {
        for (RelationshipConfig rel : entity.getRelationships()) {
            String otherEntityClass = rel.getOtherEntity() != null
                ? rel.getOtherEntity().getPersistClass()
                : JavaCodeBuilder.capitalize(rel.getOtherEntityName());

            if (rel.isCollection()) {
                String fieldName = rel.getRelationshipFieldNamePlural();
                String methodName = rel.getRelationshipNameCapitalizedPlural();
                String type = "Set<" + otherEntityClass + ">";

                writeGetter(builder, type, fieldName, methodName);
                writeSetter(builder, type, fieldName, methodName);

                if (Boolean.TRUE.equals(entity.getFluentMethods())) {
                    writeFluentSetter(builder, entity.getPersistClass(), type, fieldName, methodName);
                    writeAddRemoveMethods(builder, entity, rel, otherEntityClass);
                }
            } else {
                String fieldName = rel.getRelationshipFieldName();
                String methodName = rel.getRelationshipNameCapitalized();

                writeGetter(builder, otherEntityClass, fieldName, methodName);
                writeSetter(builder, otherEntityClass, fieldName, methodName);

                if (Boolean.TRUE.equals(entity.getFluentMethods())) {
                    writeFluentSetter(builder, entity.getPersistClass(), otherEntityClass, fieldName, methodName);
                }
            }
        }
    }

    private void writeGetter(JavaCodeBuilder builder, String type, String fieldName, String methodName) {
        builder.methodSignature("public", type, "get" + methodName);
        builder.returnStatement("this." + fieldName);
        builder.closeMethod();
        builder.line();
    }

    private void writeSetter(JavaCodeBuilder builder, String type, String fieldName, String methodName) {
        builder.methodSignature("public", "void", "set" + methodName, type + " " + fieldName);
        builder.statement("this." + fieldName + " = " + fieldName);
        builder.closeMethod();
        builder.line();
    }

    private void writeFluentSetter(JavaCodeBuilder builder, String className, String type, String fieldName, String methodName) {
        builder.methodSignature("public", className, fieldName, type + " " + fieldName);
        builder.statement("this." + fieldName + " = " + fieldName);
        builder.returnStatement("this");
        builder.closeMethod();
        builder.line();
    }

    private void writeAddRemoveMethods(JavaCodeBuilder builder, EntityConfig entity, RelationshipConfig rel, String otherEntityClass) {
        String singularName = rel.getRelationshipNameCapitalized();
        String otherEntityVar = JavaCodeBuilder.decapitalize(otherEntityClass);
        String fieldName = rel.getRelationshipFieldNamePlural();

        // Add method
        builder.methodSignature("public", entity.getPersistClass(), "add" + singularName, otherEntityClass + " " + otherEntityVar);
        builder.statement("this." + fieldName + ".add(" + otherEntityVar + ")");
        builder.returnStatement("this");
        builder.closeMethod();
        builder.line();

        // Remove method
        builder.methodSignature("public", entity.getPersistClass(), "remove" + singularName, otherEntityClass + " " + otherEntityVar);
        builder.statement("this." + fieldName + ".remove(" + otherEntityVar + ")");
        builder.returnStatement("this");
        builder.closeMethod();
        builder.line();
    }

    private void writeEqualsHashCodeToString(JavaCodeBuilder builder, EntityConfig entity) {
        PrimaryKeyConfig pk = entity.getPrimaryKey();

        // equals
        builder.annotation("Override");
        builder.methodSignature("public", "boolean", "equals", "Object o");
        builder.ifStatement("this == o");
        builder.returnStatement("true");
        builder.closeIf();
        builder.ifStatement("!(o instanceof " + entity.getPersistClass() + ")");
        builder.returnStatement("false");
        builder.closeIf();
        if (pk != null) {
            builder.returnStatement("get" + pk.getNameCapitalized() + "() != null && get" + pk.getNameCapitalized() +
                "().equals(((" + entity.getPersistClass() + ") o).get" + pk.getNameCapitalized() + "())");
        } else {
            builder.returnStatement("false");
        }
        builder.closeMethod();
        builder.line();

        // hashCode
        builder.annotation("Override");
        builder.methodSignature("public", "int", "hashCode");
        builder.returnStatement("getClass().hashCode()");
        builder.closeMethod();
        builder.line();

        // toString
        builder.lineComment("prettier-ignore");
        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        StringBuilder toStringBody = new StringBuilder();
        toStringBody.append("\"").append(entity.getPersistClass()).append("{\" +\n");
        if (pk != null) {
            toStringBody.append("            \"").append(pk.getName()).append("=\" + get")
                .append(pk.getNameCapitalized()).append("() +\n");
        }
        for (FieldConfig field : entity.getFields()) {
            if (Boolean.TRUE.equals(field.getId())) continue;
            String quote = field.isNumeric() || field.isBoolean() ? "" : "'";
            toStringBody.append("            \", ").append(field.getFieldName()).append("=")
                .append(quote).append("\" + get").append(field.getFieldInJavaBeanMethod()).append("()");
            if (!quote.isEmpty()) {
                toStringBody.append(" + \"'\"");
            }
            toStringBody.append(" +\n");
        }
        toStringBody.append("            \"}\"");
        builder.returnStatement(toStringBody.toString());
        builder.closeMethod();
    }

    private void writeEnumClasses(EntityConfig entity) throws Exception {
        for (FieldConfig field : entity.getFields()) {
            if (field.isEnumField()) {
                writeEnumClass(entity, field);
            }
        }
    }

    private void writeEnumClass(EntityConfig entity, FieldConfig field) throws Exception {
        JHipsterConfig config = getConfig();
        String packageName = entity.getEntityAbsolutePackage() != null
            ? entity.getEntityAbsolutePackage()
            : config.getPackageName();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".domain.enumeration");

        builder.javadoc("The " + field.getFieldType() + " enumeration.");
        builder.enumDeclaration("public", field.getFieldType());

        Map<String, String> enumValues = field.getEnumValues();
        List<String> keys = new ArrayList<>(enumValues.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = enumValues.get(key);
            String suffix = (i < keys.size() - 1) ? "," : ";";
            if (!key.equals(value)) {
                builder.line(key + "(\"" + value + "\")" + suffix);
            } else {
                builder.line(key + suffix);
            }
        }

        // If values differ from keys, add value field and constructor
        boolean hasCustomValues = enumValues.entrySet().stream()
            .anyMatch(e -> !e.getKey().equals(e.getValue()));

        if (hasCustomValues) {
            builder.line();
            builder.field("private final", "String", "value");
            builder.line();
            builder.constructor("", field.getFieldType(), "String value");
            builder.statement("this.value = value");
            builder.closeMethod();
            builder.line();
            builder.methodSignature("public", "String", "getValue");
            builder.returnStatement("value");
            builder.closeMethod();
        }

        builder.closeClass();

        String entityPath = entity.getEntityPackage() != null && !entity.getEntityPackage().isEmpty()
            ? entity.getEntityPackage().replace('.', '/') + "/"
            : "";
        writeFile(getMainJavaPath() + entityPath + "domain/enumeration/" + field.getFieldType() + ".java", builder.build());
    }

    private void checkCyclicRelationships(EntityConfig entity) {
        Set<String> visited = new HashSet<>();
        checkCyclicRelationshipsRecursive(entity, visited, new ArrayList<>());
    }

    private void checkCyclicRelationshipsRecursive(EntityConfig entity, Set<String> visited, List<String> path) {
        if (visited.contains(entity.getName())) {
            if (path.contains(entity.getName())) {
                log.warn("Cyclic relationship detected: {} -> {}", String.join(" -> ", path), entity.getName());
            }
            return;
        }

        visited.add(entity.getName());
        path.add(entity.getName());

        for (RelationshipConfig rel : entity.getRelationships()) {
            if (rel.isRequired() && rel.getOtherEntity() != null) {
                checkCyclicRelationshipsRecursive(rel.getOtherEntity(), visited, new ArrayList<>(path));
            }
        }
    }
}
