/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.springdata;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.RelationshipConfig;
import io.github.jhipster.generator.template.JavaCodeBuilder;

import java.util.*;

/**
 * Spring Data Relational Generator.
 * Generates JPA repositories, services, DTOs, mappers, and REST controllers.
 * Equivalent to spring-data-relational/generator.ts.
 */
public class SpringDataRelationalGenerator extends BaseApplicationGenerator {

    public SpringDataRelationalGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "spring-data-relational";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.PREPARING, "preparingSpringData", this::preparing);
        registerTask(GeneratorPriority.WRITING, "writingSpringData", this::writing);
    }

    private void preparing() {
        log.info("Preparing Spring Data Relational generator");
    }

    private void writing() throws Exception {
        log.info("Writing Spring Data Relational infrastructure");

        // Write Liquibase master changelog
        writeLiquibaseMaster();
    }

    @Override
    protected void preparingEachEntity(EntityConfig entity) {
        log.debug("Preparing entity for Spring Data: {}", entity.getName());

        // Set JPQL instance name
        context.setConfigValue("jpqlInstanceName_" + entity.getName(),
            JavaCodeBuilder.decapitalize(entity.getPersistClass()));
    }

    @Override
    protected void writingEntity(EntityConfig entity) throws Exception {
        if (Boolean.TRUE.equals(entity.getSkipServer()) || Boolean.TRUE.equals(entity.getReadOnly())) {
            if (Boolean.TRUE.equals(entity.getReadOnly())) {
                log.info("Entity {} is read-only, skipping full generation", entity.getName());
            }
            return;
        }

        log.info("Writing Spring Data files for entity: {}", entity.getName());

        // Write repository
        writeRepository(entity);

        // Write service interface and implementation
        if (entity.hasService()) {
            writeService(entity);
            if (entity.hasServiceImpl()) {
                writeServiceImpl(entity);
            }
        }

        // Write DTO
        if (entity.hasDto()) {
            writeDto(entity);
            writeMapper(entity);
        }

        // Write criteria class for filtering
        if (entity.hasFiltering()) {
            writeCriteria(entity);
            writeQueryService(entity);
        }

        // Write REST controller
        writeResource(entity);

        // Write Liquibase changelog for entity
        writeLiquibaseChangelog(entity);
    }

    private void writeRepository(EntityConfig entity) throws Exception {
        JHipsterConfig config = getConfig();
        String packageName = getEntityPackage(entity);

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".repository");

        String pkType = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getType() : "Long";
        String entityClass = entity.getPersistClass();

        builder.addImports(
            packageName + ".domain." + entityClass,
            "org.springframework.data.jpa.repository.JpaRepository",
            "org.springframework.data.jpa.repository.JpaSpecificationExecutor",
            "org.springframework.stereotype.Repository"
        );

        builder.javadoc("Spring Data JPA repository for the " + entityClass + " entity.");
        builder.annotation("Repository");

        builder.interfaceDeclaration("public", entityClass + "Repository",
            "JpaRepository<" + entityClass + ", " + pkType + ">",
            "JpaSpecificationExecutor<" + entityClass + ">");

        builder.closeClass();

        String entityPath = getEntityPath(entity);
        writeFile(getMainJavaPath() + entityPath + "repository/" + entityClass + "Repository.java", builder.build());
    }

    private void writeService(EntityConfig entity) throws Exception {
        String packageName = getEntityPackage(entity);
        String entityClass = entity.getPersistClass();
        String dtoClass = entity.hasDto() ? entity.getDtoClass() : entityClass;

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".service");

        builder.addImport(packageName + ".domain." + entityClass);
        if (entity.hasDto()) {
            builder.addImport(packageName + ".service.dto." + dtoClass);
        }
        builder.addImport("java.util.Optional");
        if (entity.hasPagination()) {
            builder.addImports("org.springframework.data.domain.Page", "org.springframework.data.domain.Pageable");
        } else {
            builder.addImport("java.util.List");
        }

        builder.javadoc("Service Interface for managing {@link " + entityClass + "}.");

        builder.interfaceDeclaration("public", entityClass + "Service");

        // Save method
        builder.javadoc("Save a " + entity.getPersistInstance() + ".",
            "@param " + entity.getPersistInstance() + " the entity to save.",
            "@return the persisted entity.");
        builder.abstractMethod("", dtoClass, "save", dtoClass + " " + entity.getPersistInstance());
        builder.line();

        // Update method
        builder.javadoc("Update a " + entity.getPersistInstance() + ".",
            "@param " + entity.getPersistInstance() + " the entity to update.",
            "@return the persisted entity.");
        builder.abstractMethod("", dtoClass, "update", dtoClass + " " + entity.getPersistInstance());
        builder.line();

        // Find all method
        if (entity.hasPagination()) {
            builder.javadoc("Get all entities.",
                "@param pageable the pagination information.",
                "@return the list of entities.");
            builder.abstractMethod("", "Page<" + dtoClass + ">", "findAll", "Pageable pageable");
        } else {
            builder.javadoc("Get all entities.",
                "@return the list of entities.");
            builder.abstractMethod("", "List<" + dtoClass + ">", "findAll");
        }
        builder.line();

        // Find one method
        String pkType = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getType() : "Long";
        builder.javadoc("Get one entity by id.",
            "@param id the id of the entity.",
            "@return the entity.");
        builder.abstractMethod("", "Optional<" + dtoClass + ">", "findOne", pkType + " id");
        builder.line();

        // Delete method
        builder.javadoc("Delete the entity by id.",
            "@param id the id of the entity.");
        builder.abstractMethod("", "void", "delete", pkType + " id");

        builder.closeClass();

        String entityPath = getEntityPath(entity);
        writeFile(getMainJavaPath() + entityPath + "service/" + entityClass + "Service.java", builder.build());
    }

    private void writeServiceImpl(EntityConfig entity) throws Exception {
        String packageName = getEntityPackage(entity);
        String entityClass = entity.getPersistClass();
        String entityInstance = entity.getPersistInstance();
        String dtoClass = entity.hasDto() ? entity.getDtoClass() : entityClass;
        String pkType = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getType() : "Long";

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".service.impl");

        builder.addImports(
            packageName + ".domain." + entityClass,
            packageName + ".repository." + entityClass + "Repository",
            packageName + ".service." + entityClass + "Service",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.annotation.Transactional",
            "java.util.Optional"
        );

        if (entity.hasDto()) {
            builder.addImports(
                packageName + ".service.dto." + dtoClass,
                packageName + ".service.mapper." + entityClass + "Mapper"
            );
        }

        if (entity.hasPagination()) {
            builder.addImports("org.springframework.data.domain.Page", "org.springframework.data.domain.Pageable");
        } else {
            builder.addImports("java.util.List", "java.util.stream.Collectors");
        }

        builder.javadoc("Service Implementation for managing {@link " + entityClass + "}.");
        builder.annotation("Service");
        builder.annotation("Transactional");

        builder.classDeclaration("public", entityClass + "ServiceImpl", null, entityClass + "Service");

        builder.staticFinalField("Logger", "log", "LoggerFactory.getLogger(" + entityClass + "ServiceImpl.class)");
        builder.line();

        builder.field("private final", entityClass + "Repository", entityInstance + "Repository");
        if (entity.hasDto()) {
            builder.field("private final", entityClass + "Mapper", entityInstance + "Mapper");
        }
        builder.line();

        // Constructor
        if (entity.hasDto()) {
            builder.constructor("public", entityClass + "ServiceImpl",
                entityClass + "Repository " + entityInstance + "Repository",
                entityClass + "Mapper " + entityInstance + "Mapper");
            builder.statement("this." + entityInstance + "Repository = " + entityInstance + "Repository");
            builder.statement("this." + entityInstance + "Mapper = " + entityInstance + "Mapper");
        } else {
            builder.constructor("public", entityClass + "ServiceImpl",
                entityClass + "Repository " + entityInstance + "Repository");
            builder.statement("this." + entityInstance + "Repository = " + entityInstance + "Repository");
        }
        builder.closeMethod();
        builder.line();

        // Save method
        builder.annotation("Override");
        builder.methodSignature("public", dtoClass, "save", dtoClass + " " + entityInstance + "DTO");
        builder.statement("log.debug(\"Request to save " + entityClass + " : {}\", " + entityInstance + "DTO)");
        if (entity.hasDto()) {
            builder.statement(entityClass + " " + entityInstance + " = " + entityInstance + "Mapper.toEntity(" + entityInstance + "DTO)");
            builder.statement(entityInstance + " = " + entityInstance + "Repository.save(" + entityInstance + ")");
            builder.returnStatement(entityInstance + "Mapper.toDto(" + entityInstance + ")");
        } else {
            builder.returnStatement(entityInstance + "Repository.save(" + entityInstance + "DTO)");
        }
        builder.closeMethod();
        builder.line();

        // Update method
        builder.annotation("Override");
        builder.methodSignature("public", dtoClass, "update", dtoClass + " " + entityInstance + "DTO");
        builder.statement("log.debug(\"Request to update " + entityClass + " : {}\", " + entityInstance + "DTO)");
        if (entity.hasDto()) {
            builder.statement(entityClass + " " + entityInstance + " = " + entityInstance + "Mapper.toEntity(" + entityInstance + "DTO)");
            builder.statement(entityInstance + " = " + entityInstance + "Repository.save(" + entityInstance + ")");
            builder.returnStatement(entityInstance + "Mapper.toDto(" + entityInstance + ")");
        } else {
            builder.returnStatement(entityInstance + "Repository.save(" + entityInstance + "DTO)");
        }
        builder.closeMethod();
        builder.line();

        // Find all method
        builder.annotation("Override");
        builder.annotation("Transactional", "readOnly = true");
        if (entity.hasPagination()) {
            builder.methodSignature("public", "Page<" + dtoClass + ">", "findAll", "Pageable pageable");
            builder.statement("log.debug(\"Request to get all " + entityClass + "s\")");
            if (entity.hasDto()) {
                builder.returnStatement(entityInstance + "Repository.findAll(pageable).map(" + entityInstance + "Mapper::toDto)");
            } else {
                builder.returnStatement(entityInstance + "Repository.findAll(pageable)");
            }
        } else {
            builder.methodSignature("public", "List<" + dtoClass + ">", "findAll");
            builder.statement("log.debug(\"Request to get all " + entityClass + "s\")");
            if (entity.hasDto()) {
                builder.returnStatement(entityInstance + "Repository.findAll().stream().map(" + entityInstance + "Mapper::toDto).collect(Collectors.toList())");
            } else {
                builder.returnStatement(entityInstance + "Repository.findAll()");
            }
        }
        builder.closeMethod();
        builder.line();

        // Find one method
        builder.annotation("Override");
        builder.annotation("Transactional", "readOnly = true");
        builder.methodSignature("public", "Optional<" + dtoClass + ">", "findOne", pkType + " id");
        builder.statement("log.debug(\"Request to get " + entityClass + " : {}\", id)");
        if (entity.hasDto()) {
            builder.returnStatement(entityInstance + "Repository.findById(id).map(" + entityInstance + "Mapper::toDto)");
        } else {
            builder.returnStatement(entityInstance + "Repository.findById(id)");
        }
        builder.closeMethod();
        builder.line();

        // Delete method
        builder.annotation("Override");
        builder.methodSignature("public", "void", "delete", pkType + " id");
        builder.statement("log.debug(\"Request to delete " + entityClass + " : {}\", id)");
        builder.statement(entityInstance + "Repository.deleteById(id)");
        builder.closeMethod();

        builder.closeClass();

        String entityPath = getEntityPath(entity);
        writeFile(getMainJavaPath() + entityPath + "service/impl/" + entityClass + "ServiceImpl.java", builder.build());
    }

    private void writeDto(EntityConfig entity) throws Exception {
        String packageName = getEntityPackage(entity);
        String entityClass = entity.getPersistClass();
        String dtoClass = entity.getDtoClass();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".service.dto");

        builder.addImport("java.io.Serializable");
        builder.addImport("java.util.Objects");

        // Add field type imports
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
                case "UUID":
                    builder.addImport("java.util.UUID");
                    break;
            }
            if (field.isEnumField()) {
                builder.addImport(packageName + ".domain.enumeration." + field.getFieldType());
            }
        }

        if (entity.hasFiltering()) {
            builder.addImport("jakarta.validation.constraints.*");
        }

        builder.javadoc("A DTO for the {@link " + packageName + ".domain." + entityClass + "} entity.");
        builder.annotation("SuppressWarnings", "\"common-java:DuplicatedBlocks\"");

        builder.classDeclaration("public", dtoClass, null, "Serializable");

        // Fields
        String pkType = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getType() : "Long";
        String pkName = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getName() : "id";
        builder.field("private", pkType, pkName);
        builder.line();

        for (FieldConfig field : entity.getFields()) {
            if (!Boolean.TRUE.equals(field.getId())) {
                builder.field("private", field.getJavaFieldType(), field.getFieldName());
                if (field.isFieldWithContentType()) {
                    builder.field("private", "String", field.getFieldName() + "ContentType");
                }
            }
        }

        // Relationship DTOs (simplified - just IDs)
        for (RelationshipConfig rel : entity.getRelationships()) {
            if (!rel.isCollection()) {
                String otherPkType = "Long";
                if (rel.getOtherEntity() != null && rel.getOtherEntity().getPrimaryKey() != null) {
                    otherPkType = rel.getOtherEntity().getPrimaryKey().getType();
                }
                builder.field("private", otherPkType, rel.getRelationshipFieldName() + "Id");
            }
        }

        builder.line();

        // Getters and setters
        writeGetter(builder, pkType, pkName, JavaCodeBuilder.capitalize(pkName));
        writeSetter(builder, pkType, pkName, JavaCodeBuilder.capitalize(pkName));

        for (FieldConfig field : entity.getFields()) {
            if (!Boolean.TRUE.equals(field.getId())) {
                writeGetter(builder, field.getJavaFieldType(), field.getFieldName(), field.getFieldInJavaBeanMethod());
                writeSetter(builder, field.getJavaFieldType(), field.getFieldName(), field.getFieldInJavaBeanMethod());
                if (field.isFieldWithContentType()) {
                    writeGetter(builder, "String", field.getFieldName() + "ContentType", field.getFieldInJavaBeanMethod() + "ContentType");
                    writeSetter(builder, "String", field.getFieldName() + "ContentType", field.getFieldInJavaBeanMethod() + "ContentType");
                }
            }
        }

        for (RelationshipConfig rel : entity.getRelationships()) {
            if (!rel.isCollection()) {
                String otherPkType = "Long";
                if (rel.getOtherEntity() != null && rel.getOtherEntity().getPrimaryKey() != null) {
                    otherPkType = rel.getOtherEntity().getPrimaryKey().getType();
                }
                writeGetter(builder, otherPkType, rel.getRelationshipFieldName() + "Id", rel.getRelationshipNameCapitalized() + "Id");
                writeSetter(builder, otherPkType, rel.getRelationshipFieldName() + "Id", rel.getRelationshipNameCapitalized() + "Id");
            }
        }

        // equals, hashCode, toString
        builder.annotation("Override");
        builder.methodSignature("public", "boolean", "equals", "Object o");
        builder.ifStatement("this == o");
        builder.returnStatement("true");
        builder.closeIf();
        builder.ifStatement("!(o instanceof " + dtoClass + ")");
        builder.returnStatement("false");
        builder.closeIf();
        builder.line(dtoClass + " that = (" + dtoClass + ") o;");
        builder.returnStatement("Objects.equals(" + pkName + ", that." + pkName + ")");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "int", "hashCode");
        builder.returnStatement("Objects.hash(" + pkName + ")");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"" + dtoClass + "{\" + \"" + pkName + "=\" + " + pkName + " + \"}\"");
        builder.closeMethod();

        builder.closeClass();

        String entityPath = getEntityPath(entity);
        writeFile(getMainJavaPath() + entityPath + "service/dto/" + dtoClass + ".java", builder.build());
    }

    private void writeMapper(EntityConfig entity) throws Exception {
        String packageName = getEntityPackage(entity);
        String entityClass = entity.getPersistClass();
        String dtoClass = entity.getDtoClass();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".service.mapper");

        builder.addImports(
            packageName + ".domain." + entityClass,
            packageName + ".service.dto." + dtoClass,
            "org.mapstruct.Mapper",
            "org.mapstruct.Mapping"
        );

        builder.javadoc("Mapper for the entity {@link " + entityClass + "} and its DTO {@link " + dtoClass + "}.");
        builder.annotation("Mapper", "componentModel = \"spring\"");

        builder.interfaceDeclaration("public", entityClass + "Mapper");

        builder.abstractMethod("", entityClass, "toEntity", dtoClass + " dto");
        builder.line();
        builder.abstractMethod("", dtoClass, "toDto", entityClass + " entity");

        builder.closeClass();

        String entityPath = getEntityPath(entity);
        writeFile(getMainJavaPath() + entityPath + "service/mapper/" + entityClass + "Mapper.java", builder.build());
    }

    private void writeCriteria(EntityConfig entity) throws Exception {
        String packageName = getEntityPackage(entity);
        String entityClass = entity.getPersistClass();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".service.criteria");

        builder.addImports(
            "java.io.Serializable",
            "tech.jhipster.service.Criteria",
            "tech.jhipster.service.filter.*"
        );

        builder.javadoc("Criteria class for the {@link " + packageName + ".domain." + entityClass + "} entity.");

        builder.classDeclaration("public", entityClass + "Criteria", null, "Serializable", "Criteria");

        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        // Filter fields
        String pkType = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getType() : "Long";
        builder.field("private", getFilterType(pkType), entity.getPrimaryKey() != null ? entity.getPrimaryKey().getName() : "id");

        for (FieldConfig field : entity.getFields()) {
            if (!Boolean.TRUE.equals(field.getId())) {
                builder.field("private", getFilterType(field.getJavaFieldType()), field.getFieldName());
            }
        }

        builder.line();

        // Default constructor
        builder.constructor("public", entityClass + "Criteria");
        builder.closeMethod();

        // Copy constructor
        builder.constructor("public", entityClass + "Criteria", entityClass + "Criteria other");
        String pkName = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getName() : "id";
        builder.statement("this." + pkName + " = other." + pkName + " == null ? null : other." + pkName + ".copy()");
        for (FieldConfig field : entity.getFields()) {
            if (!Boolean.TRUE.equals(field.getId())) {
                builder.statement("this." + field.getFieldName() + " = other." + field.getFieldName() + " == null ? null : other." + field.getFieldName() + ".copy()");
            }
        }
        builder.closeMethod();

        // Copy method
        builder.annotation("Override");
        builder.methodSignature("public", entityClass + "Criteria", "copy");
        builder.returnStatement("new " + entityClass + "Criteria(this)");
        builder.closeMethod();

        // Getters and setters for filters
        writeGetter(builder, getFilterType(pkType), pkName, JavaCodeBuilder.capitalize(pkName));
        writeSetter(builder, getFilterType(pkType), pkName, JavaCodeBuilder.capitalize(pkName));

        for (FieldConfig field : entity.getFields()) {
            if (!Boolean.TRUE.equals(field.getId())) {
                writeGetter(builder, getFilterType(field.getJavaFieldType()), field.getFieldName(), field.getFieldInJavaBeanMethod());
                writeSetter(builder, getFilterType(field.getJavaFieldType()), field.getFieldName(), field.getFieldInJavaBeanMethod());
            }
        }

        builder.closeClass();

        String entityPath = getEntityPath(entity);
        writeFile(getMainJavaPath() + entityPath + "service/criteria/" + entityClass + "Criteria.java", builder.build());
    }

    private void writeQueryService(EntityConfig entity) throws Exception {
        String packageName = getEntityPackage(entity);
        String entityClass = entity.getPersistClass();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".service");

        String dtoClass = entity.hasDto() ? entity.getDtoClass() : entityClass;

        builder.addImports(
            packageName + ".domain." + entityClass,
            packageName + ".domain." + entityClass + "_",
            packageName + ".repository." + entityClass + "Repository",
            packageName + ".service.criteria." + entityClass + "Criteria",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.data.domain.Page",
            "org.springframework.data.domain.Pageable",
            "org.springframework.data.jpa.domain.Specification",
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.annotation.Transactional",
            "tech.jhipster.service.QueryService"
        );

        if (entity.hasDto()) {
            builder.addImports(
                packageName + ".service.dto." + dtoClass,
                packageName + ".service.mapper." + entityClass + "Mapper"
            );
        }

        builder.javadoc("Service for executing complex queries for {@link " + entityClass + "} entities in the database.",
            "The main input is a {@link " + entityClass + "Criteria} which gets converted to {@link Specification}.");
        builder.annotation("Service");
        builder.annotation("Transactional", "readOnly = true");

        builder.classDeclaration("public", entityClass + "QueryService", "QueryService<" + entityClass + ">");

        builder.staticFinalField("Logger", "log", "LoggerFactory.getLogger(" + entityClass + "QueryService.class)");
        builder.line();

        String entityInstance = entity.getPersistInstance();
        builder.field("private final", entityClass + "Repository", entityInstance + "Repository");
        if (entity.hasDto()) {
            builder.field("private final", entityClass + "Mapper", entityInstance + "Mapper");
        }
        builder.line();

        // Constructor
        if (entity.hasDto()) {
            builder.constructor("public", entityClass + "QueryService",
                entityClass + "Repository " + entityInstance + "Repository",
                entityClass + "Mapper " + entityInstance + "Mapper");
            builder.statement("this." + entityInstance + "Repository = " + entityInstance + "Repository");
            builder.statement("this." + entityInstance + "Mapper = " + entityInstance + "Mapper");
        } else {
            builder.constructor("public", entityClass + "QueryService",
                entityClass + "Repository " + entityInstance + "Repository");
            builder.statement("this." + entityInstance + "Repository = " + entityInstance + "Repository");
        }
        builder.closeMethod();
        builder.line();

        // Find by criteria method
        builder.javadoc("Return a {@link Page} of {@link " + dtoClass + "} which matches the criteria from the database.",
            "@param criteria The object which holds all the filters.",
            "@param page The page information.",
            "@return the matching entities.");
        builder.annotation("Transactional", "readOnly = true");
        builder.methodSignature("public", "Page<" + dtoClass + ">", "findByCriteria",
            entityClass + "Criteria criteria", "Pageable page");
        builder.statement("log.debug(\"find by criteria : {}, page: {}\", criteria, page)");
        builder.statement("final Specification<" + entityClass + "> specification = createSpecification(criteria)");
        if (entity.hasDto()) {
            builder.returnStatement(entityInstance + "Repository.findAll(specification, page).map(" + entityInstance + "Mapper::toDto)");
        } else {
            builder.returnStatement(entityInstance + "Repository.findAll(specification, page)");
        }
        builder.closeMethod();
        builder.line();

        // Count method
        builder.javadoc("Return the number of matching entities in the database.",
            "@param criteria The object which holds all the filters.",
            "@return the number of matching entities.");
        builder.annotation("Transactional", "readOnly = true");
        builder.methodSignature("public", "long", "countByCriteria", entityClass + "Criteria criteria");
        builder.statement("log.debug(\"count by criteria : {}\", criteria)");
        builder.statement("final Specification<" + entityClass + "> specification = createSpecification(criteria)");
        builder.returnStatement(entityInstance + "Repository.count(specification)");
        builder.closeMethod();
        builder.line();

        // Create specification method
        builder.javadoc("Function to convert {@link " + entityClass + "Criteria} to a {@link Specification}.",
            "@param criteria The object which holds all the filters.",
            "@return the matching {@link Specification}.");
        builder.methodSignature("protected", "Specification<" + entityClass + ">", "createSpecification",
            entityClass + "Criteria criteria");
        builder.statement("Specification<" + entityClass + "> specification = Specification.where(null)");
        builder.ifStatement("criteria != null");

        String pkName = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getName() : "id";
        builder.ifStatement("criteria.get" + JavaCodeBuilder.capitalize(pkName) + "() != null");
        builder.statement("specification = specification.and(buildRangeSpecification(criteria.get" +
            JavaCodeBuilder.capitalize(pkName) + "(), " + entityClass + "_." + pkName + "))");
        builder.closeIf();

        for (FieldConfig field : entity.getFields()) {
            if (!Boolean.TRUE.equals(field.getId())) {
                builder.ifStatement("criteria.get" + field.getFieldInJavaBeanMethod() + "() != null");
                String specMethod = getSpecificationMethod(field.getJavaFieldType());
                builder.statement("specification = specification.and(" + specMethod + "(criteria.get" +
                    field.getFieldInJavaBeanMethod() + "(), " + entityClass + "_." + field.getFieldName() + "))");
                builder.closeIf();
            }
        }

        builder.closeIf();
        builder.returnStatement("specification");
        builder.closeMethod();

        builder.closeClass();

        String entityPath = getEntityPath(entity);
        writeFile(getMainJavaPath() + entityPath + "service/" + entityClass + "QueryService.java", builder.build());
    }

    private void writeResource(EntityConfig entity) throws Exception {
        String packageName = getEntityPackage(entity);
        String entityClass = entity.getPersistClass();
        String entityInstance = entity.getPersistInstance();
        String dtoClass = entity.hasDto() ? entity.getDtoClass() : entityClass;
        String pkType = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getType() : "Long";

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(packageName + ".web.rest");

        builder.addImports(
            packageName + ".service." + entityClass + "Service",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.http.ResponseEntity",
            "org.springframework.web.bind.annotation.*",
            "jakarta.validation.Valid",
            "java.net.URI",
            "java.net.URISyntaxException",
            "java.util.Optional"
        );

        if (entity.hasDto()) {
            builder.addImport(packageName + ".service.dto." + dtoClass);
        } else {
            builder.addImport(packageName + ".domain." + entityClass);
        }

        if (entity.hasPagination()) {
            builder.addImports(
                "org.springframework.data.domain.Page",
                "org.springframework.data.domain.Pageable",
                "org.springframework.http.HttpHeaders",
                "tech.jhipster.web.util.PaginationUtil"
            );
        } else {
            builder.addImport("java.util.List");
        }

        String endpointPrefix = getConfig().isMicroservice() ? "/api" : "/api";

        builder.javadoc("REST controller for managing {@link " + packageName + ".domain." + entityClass + "}.");
        builder.annotation("RestController");
        builder.annotation("RequestMapping", "\"" + endpointPrefix + "\"");

        builder.classDeclaration("public", entityClass + "Resource", null);

        builder.staticFinalField("Logger", "log", "LoggerFactory.getLogger(" + entityClass + "Resource.class)");
        builder.staticFinalField("String", "ENTITY_NAME", "\"" + entityInstance + "\"");
        builder.line();

        builder.field("private final", entityClass + "Service", entityInstance + "Service");
        builder.line();

        builder.constructor("public", entityClass + "Resource", entityClass + "Service " + entityInstance + "Service");
        builder.statement("this." + entityInstance + "Service = " + entityInstance + "Service");
        builder.closeMethod();
        builder.line();

        // Create endpoint
        builder.javadoc("POST  /" + entityInstance + "s : Create a new " + entityInstance + ".",
            "@param " + entityInstance + " the " + entityInstance + " to create.",
            "@return the created entity.");
        builder.annotation("PostMapping", "\"/" + entityInstance + "s\"");
        builder.methodSignature("public", "ResponseEntity<" + dtoClass + ">", "create" + entityClass,
            "@Valid @RequestBody " + dtoClass + " " + entityInstance);
        builder.statement("log.debug(\"REST request to save " + entityClass + " : {}\", " + entityInstance + ")");
        builder.statement(dtoClass + " result = " + entityInstance + "Service.save(" + entityInstance + ")");
        builder.tryBlock();
        builder.returnStatement("ResponseEntity.created(new URI(\"/api/" + entityInstance + "s/\" + result.getId())).body(result)");
        builder.catchBlock("URISyntaxException", "e");
        builder.statement("throw new RuntimeException(e)");
        builder.closeTryCatch();
        builder.closeMethod();
        builder.line();

        // Update endpoint
        builder.javadoc("PUT  /" + entityInstance + "s/{id} : Updates an existing " + entityInstance + ".",
            "@param id the id of the " + entityInstance + " to update.",
            "@param " + entityInstance + " the " + entityInstance + " to update.",
            "@return the updated entity.");
        builder.annotation("PutMapping", "\"/" + entityInstance + "s/{id}\"");
        builder.methodSignature("public", "ResponseEntity<" + dtoClass + ">", "update" + entityClass,
            "@PathVariable " + pkType + " id",
            "@Valid @RequestBody " + dtoClass + " " + entityInstance);
        builder.statement("log.debug(\"REST request to update " + entityClass + " : {}, {}\", id, " + entityInstance + ")");
        builder.statement(dtoClass + " result = " + entityInstance + "Service.update(" + entityInstance + ")");
        builder.returnStatement("ResponseEntity.ok().body(result)");
        builder.closeMethod();
        builder.line();

        // Get all endpoint
        builder.javadoc("GET  /" + entityInstance + "s : get all " + entityInstance + "s.",
            "@return the list of entities.");
        builder.annotation("GetMapping", "\"/" + entityInstance + "s\"");
        if (entity.hasPagination()) {
            builder.methodSignature("public", "ResponseEntity<List<" + dtoClass + ">>", "getAll" + entityClass + "s",
                "Pageable pageable");
            builder.statement("log.debug(\"REST request to get a page of " + entityClass + "s\")");
            builder.statement("Page<" + dtoClass + "> page = " + entityInstance + "Service.findAll(pageable)");
            builder.statement("HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, \"/api/" + entityInstance + "s\")");
            builder.returnStatement("ResponseEntity.ok().headers(headers).body(page.getContent())");
        } else {
            builder.methodSignature("public", "List<" + dtoClass + ">", "getAll" + entityClass + "s");
            builder.statement("log.debug(\"REST request to get all " + entityClass + "s\")");
            builder.returnStatement(entityInstance + "Service.findAll()");
        }
        builder.closeMethod();
        builder.line();

        // Get one endpoint
        builder.javadoc("GET  /" + entityInstance + "s/{id} : get the \"id\" " + entityInstance + ".",
            "@param id the id of the " + entityInstance + " to retrieve.",
            "@return the entity.");
        builder.annotation("GetMapping", "\"/" + entityInstance + "s/{id}\"");
        builder.methodSignature("public", "ResponseEntity<" + dtoClass + ">", "get" + entityClass,
            "@PathVariable " + pkType + " id");
        builder.statement("log.debug(\"REST request to get " + entityClass + " : {}\", id)");
        builder.statement("Optional<" + dtoClass + "> " + entityInstance + " = " + entityInstance + "Service.findOne(id)");
        builder.returnStatement(entityInstance + ".map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build())");
        builder.closeMethod();
        builder.line();

        // Delete endpoint
        builder.javadoc("DELETE  /" + entityInstance + "s/{id} : delete the \"id\" " + entityInstance + ".",
            "@param id the id of the " + entityInstance + " to delete.",
            "@return the status.");
        builder.annotation("DeleteMapping", "\"/" + entityInstance + "s/{id}\"");
        builder.methodSignature("public", "ResponseEntity<Void>", "delete" + entityClass,
            "@PathVariable " + pkType + " id");
        builder.statement("log.debug(\"REST request to delete " + entityClass + " : {}\", id)");
        builder.statement(entityInstance + "Service.delete(id)");
        builder.returnStatement("ResponseEntity.noContent().build()");
        builder.closeMethod();

        builder.closeClass();

        String entityPath = getEntityPath(entity);
        writeFile(getMainJavaPath() + entityPath + "web/rest/" + entityClass + "Resource.java", builder.build());
    }

    private void writeLiquibaseMaster() throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<databaseChangeLog\n");
        xml.append("    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n");
        xml.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n");
        xml.append("\n");
        xml.append("    <property name=\"now\" value=\"now()\" dbms=\"h2\"/>\n");
        xml.append("    <property name=\"now\" value=\"current_timestamp\" dbms=\"postgresql\"/>\n");
        xml.append("\n");
        xml.append("    <!-- Add changelogs here -->\n");
        xml.append("    <!-- jhipster-needle-liquibase-add-changelog - JHipster will add liquibase changelogs here -->\n");
        xml.append("\n");
        xml.append("</databaseChangeLog>\n");

        writeFile(getMainResourcesPath() + "config/liquibase/master.xml", xml.toString());
    }

    private void writeLiquibaseChangelog(EntityConfig entity) throws Exception {
        String tableName = entity.getEntityTableName();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<databaseChangeLog\n");
        xml.append("    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n");
        xml.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n");
        xml.append("\n");
        xml.append("    <changeSet id=\"").append(System.currentTimeMillis()).append("-1\" author=\"jhipster\">\n");
        xml.append("        <createTable tableName=\"").append(tableName).append("\">\n");

        // Primary key column
        String pkName = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getName() : "id";
        String pkType = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getJdbcType() : "BIGINT";
        xml.append("            <column name=\"").append(pkName).append("\" type=\"").append(pkType).append("\">\n");
        xml.append("                <constraints primaryKey=\"true\" nullable=\"false\"/>\n");
        xml.append("            </column>\n");

        // Other columns
        for (FieldConfig field : entity.getFields()) {
            if (!Boolean.TRUE.equals(field.getId())) {
                String columnType = getColumnType(field);
                xml.append("            <column name=\"").append(field.getColumnName()).append("\" type=\"").append(columnType).append("\"");
                if (field.isFieldValidationRequired()) {
                    xml.append(">\n");
                    xml.append("                <constraints nullable=\"false\"/>\n");
                    xml.append("            </column>\n");
                } else {
                    xml.append("/>\n");
                }

                if (field.isFieldWithContentType()) {
                    xml.append("            <column name=\"").append(field.getColumnName()).append("_content_type\" type=\"VARCHAR(255)\"/>\n");
                }
            }
        }

        xml.append("        </createTable>\n");
        xml.append("    </changeSet>\n");
        xml.append("\n");
        xml.append("</databaseChangeLog>\n");

        String timestamp = String.valueOf(System.currentTimeMillis());
        writeFile(getMainResourcesPath() + "config/liquibase/changelog/" + timestamp + "_added_entity_" + entity.getName() + ".xml", xml.toString());
    }

    // Helper methods

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

    private String getEntityPackage(EntityConfig entity) {
        return entity.getEntityAbsolutePackage() != null
            ? entity.getEntityAbsolutePackage()
            : getConfig().getPackageName();
    }

    private String getEntityPath(EntityConfig entity) {
        if (entity.getEntityPackage() != null && !entity.getEntityPackage().isEmpty()) {
            return entity.getEntityPackage().replace('.', '/') + "/";
        }
        return "";
    }

    private String getFilterType(String javaType) {
        switch (javaType) {
            case "Long":
                return "LongFilter";
            case "Integer":
                return "IntegerFilter";
            case "String":
                return "StringFilter";
            case "Boolean":
                return "BooleanFilter";
            case "BigDecimal":
                return "BigDecimalFilter";
            case "LocalDate":
                return "LocalDateFilter";
            case "Instant":
                return "InstantFilter";
            case "ZonedDateTime":
                return "ZonedDateTimeFilter";
            case "UUID":
                return "UUIDFilter";
            default:
                return "Filter<" + javaType + ">";
        }
    }

    private String getSpecificationMethod(String javaType) {
        switch (javaType) {
            case "String":
                return "buildStringSpecification";
            default:
                return "buildRangeSpecification";
        }
    }

    private String getColumnType(FieldConfig field) {
        switch (field.getFieldType()) {
            case "String":
                Integer maxLen = field.getFieldValidateRulesMaxlength();
                return "VARCHAR(" + (maxLen != null ? maxLen : 255) + ")";
            case "Integer":
                return "INTEGER";
            case "Long":
                return "BIGINT";
            case "Float":
                return "REAL";
            case "Double":
                return "DOUBLE";
            case "BigDecimal":
                return "DECIMAL(21,2)";
            case "Boolean":
                return "BOOLEAN";
            case "LocalDate":
                return "DATE";
            case "Instant":
            case "ZonedDateTime":
                return "TIMESTAMP";
            case "UUID":
                return "UUID";
            case "TextBlob":
                return "TEXT";
            case "Blob":
            case "AnyBlob":
            case "ImageBlob":
                return "BLOB";
            default:
                if (field.isEnumField()) {
                    return "VARCHAR(255)";
                }
                return "VARCHAR(255)";
        }
    }
}
