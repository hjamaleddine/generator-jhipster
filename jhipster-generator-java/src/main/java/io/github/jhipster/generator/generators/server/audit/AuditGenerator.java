/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.server.audit;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Audit Generator.
 * Generates audit infrastructure for JPA entities.
 * Equivalent to spring-data-relational audit components.
 */
public class AuditGenerator extends BaseApplicationGenerator {

    public AuditGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "audit";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingAudit", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing audit infrastructure");

        writeAbstractAuditingEntity();
        writeSpringSecurityAuditorAware();
        writeAuditEventEntity();
        writePersistentAuditEventRepository();
        writeAuditEventConverter();
        writeCustomAuditEventRepository();
    }

    private void writeAbstractAuditingEntity() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".domain");

        builder.addImports(
            "com.fasterxml.jackson.annotation.JsonIgnoreProperties",
            "jakarta.persistence.Column",
            "jakarta.persistence.EntityListeners",
            "jakarta.persistence.MappedSuperclass",
            "org.springframework.data.annotation.CreatedBy",
            "org.springframework.data.annotation.CreatedDate",
            "org.springframework.data.annotation.LastModifiedBy",
            "org.springframework.data.annotation.LastModifiedDate",
            "org.springframework.data.jpa.domain.support.AuditingEntityListener",
            "java.io.Serializable",
            "java.time.Instant"
        );

        builder.javadoc("Base abstract class for entities which will hold definitions for created, last modified, created by,\\nlast modified by attributes.");
        builder.annotation("MappedSuperclass");
        builder.annotation("EntityListeners", "AuditingEntityListener.class");
        builder.annotation("JsonIgnoreProperties", "value = { \"createdBy\", \"createdDate\", \"lastModifiedBy\", \"lastModifiedDate\" }, allowGetters = true");

        builder.classDeclaration("public abstract", "AbstractAuditingEntity<T>", null, "Serializable");

        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        // createdBy
        builder.annotation("CreatedBy");
        builder.annotation("Column", "name = \"created_by\", nullable = false, length = 50, updatable = false");
        builder.field("private", "String", "createdBy");
        builder.line();

        // createdDate
        builder.annotation("CreatedDate");
        builder.annotation("Column", "name = \"created_date\", updatable = false");
        builder.field("private", "Instant", "createdDate", "Instant.now()");
        builder.line();

        // lastModifiedBy
        builder.annotation("LastModifiedBy");
        builder.annotation("Column", "name = \"last_modified_by\", length = 50");
        builder.field("private", "String", "lastModifiedBy");
        builder.line();

        // lastModifiedDate
        builder.annotation("LastModifiedDate");
        builder.annotation("Column", "name = \"last_modified_date\"");
        builder.field("private", "Instant", "lastModifiedDate", "Instant.now()");
        builder.line();

        // Abstract getId method
        builder.line("public abstract Long getId();");
        builder.line();

        // Getters and Setters
        builder.methodSignature("public", "String", "getCreatedBy");
        builder.returnStatement("createdBy");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setCreatedBy", "String createdBy");
        builder.statement("this.createdBy = createdBy");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "Instant", "getCreatedDate");
        builder.returnStatement("createdDate");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setCreatedDate", "Instant createdDate");
        builder.statement("this.createdDate = createdDate");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getLastModifiedBy");
        builder.returnStatement("lastModifiedBy");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setLastModifiedBy", "String lastModifiedBy");
        builder.statement("this.lastModifiedBy = lastModifiedBy");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "Instant", "getLastModifiedDate");
        builder.returnStatement("lastModifiedDate");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setLastModifiedDate", "Instant lastModifiedDate");
        builder.statement("this.lastModifiedDate = lastModifiedDate");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "domain/AbstractAuditingEntity.java", builder.build());
    }

    private void writeSpringSecurityAuditorAware() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config.audit");

        builder.addImports(
            config.getPackageName() + ".security.SecurityUtils",
            "org.springframework.data.domain.AuditorAware",
            "org.springframework.stereotype.Component",
            "java.util.Optional"
        );

        builder.javadoc("Implementation of {@link AuditorAware} based on Spring Security.");
        builder.annotation("Component");

        builder.classDeclaration("public", "SpringSecurityAuditorAware", null, "AuditorAware<String>");

        builder.annotation("Override");
        builder.methodSignature("public", "Optional<String>", "getCurrentAuditor");
        builder.returnStatement("Optional.of(SecurityUtils.getCurrentUserLogin().orElse(\"system\"))");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/audit/SpringSecurityAuditorAware.java", builder.build());
    }

    private void writeAuditEventEntity() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".domain");

        builder.addImports(
            "jakarta.persistence.*",
            "jakarta.validation.constraints.NotNull",
            "java.io.Serializable",
            "java.time.Instant",
            "java.util.HashMap",
            "java.util.Map"
        );

        builder.javadoc("Persistent audit event entity.");
        builder.annotation("Entity");
        builder.annotation("Table", "name = \"jhi_persistent_audit_event\"");

        builder.classDeclaration("public", "PersistentAuditEvent", null, "Serializable");

        builder.staticFinalField("long", "serialVersionUID", "1L");
        builder.line();

        builder.annotation("Id");
        builder.annotation("GeneratedValue", "strategy = GenerationType.SEQUENCE, generator = \"sequenceGenerator\"");
        builder.annotation("SequenceGenerator", "name = \"sequenceGenerator\"");
        builder.annotation("Column", "name = \"event_id\"");
        builder.field("private", "Long", "id");
        builder.line();

        builder.annotation("NotNull");
        builder.annotation("Column", "nullable = false");
        builder.field("private", "String", "principal");
        builder.line();

        builder.annotation("Column", "name = \"event_date\"");
        builder.field("private", "Instant", "auditEventDate");
        builder.line();

        builder.annotation("Column", "name = \"event_type\"");
        builder.field("private", "String", "auditEventType");
        builder.line();

        builder.annotation("ElementCollection");
        builder.annotation("MapKeyColumn", "name = \"name\"");
        builder.annotation("Column", "name = \"value\"");
        builder.annotation("CollectionTable", "name = \"jhi_persistent_audit_evt_data\", joinColumns = @JoinColumn(name = \"event_id\")");
        builder.field("private", "Map<String, String>", "data", "new HashMap<>()");
        builder.line();

        // Getters and setters
        builder.methodSignature("public", "Long", "getId");
        builder.returnStatement("id");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setId", "Long id");
        builder.statement("this.id = id");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getPrincipal");
        builder.returnStatement("principal");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setPrincipal", "String principal");
        builder.statement("this.principal = principal");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "Instant", "getAuditEventDate");
        builder.returnStatement("auditEventDate");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setAuditEventDate", "Instant auditEventDate");
        builder.statement("this.auditEventDate = auditEventDate");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "String", "getAuditEventType");
        builder.returnStatement("auditEventType");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setAuditEventType", "String auditEventType");
        builder.statement("this.auditEventType = auditEventType");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "Map<String, String>", "getData");
        builder.returnStatement("data");
        builder.closeMethod();
        builder.line();

        builder.methodSignature("public", "void", "setData", "Map<String, String> data");
        builder.statement("this.data = data");
        builder.closeMethod();
        builder.line();

        // equals, hashCode, toString
        builder.annotation("Override");
        builder.methodSignature("public", "boolean", "equals", "Object o");
        builder.ifStatement("this == o");
        builder.returnStatement("true");
        builder.closeIf();
        builder.ifStatement("!(o instanceof PersistentAuditEvent)");
        builder.returnStatement("false");
        builder.closeIf();
        builder.returnStatement("id != null && id.equals(((PersistentAuditEvent) o).id)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "int", "hashCode");
        builder.returnStatement("getClass().hashCode()");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "String", "toString");
        builder.returnStatement("\"PersistentAuditEvent{\" +\n" +
            "                \"id=\" + id +\n" +
            "                \", principal='\" + principal + '\\'' +\n" +
            "                \", auditEventDate=\" + auditEventDate +\n" +
            "                \", auditEventType='\" + auditEventType + '\\'' +\n" +
            "                '}'");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "domain/PersistentAuditEvent.java", builder.build());
    }

    private void writePersistentAuditEventRepository() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".repository");

        builder.addImports(
            config.getPackageName() + ".domain.PersistentAuditEvent",
            "org.springframework.data.domain.Page",
            "org.springframework.data.domain.Pageable",
            "org.springframework.data.jpa.repository.JpaRepository",
            "java.time.Instant",
            "java.util.List"
        );

        builder.javadoc("Spring Data JPA repository for the {@link PersistentAuditEvent} entity.");

        builder.line("public interface PersistentAuditEventRepository extends JpaRepository<PersistentAuditEvent, Long> {");
        builder.indent();
        builder.line();
        builder.line("List<PersistentAuditEvent> findByPrincipal(String principal);");
        builder.line();
        builder.line("List<PersistentAuditEvent> findByAuditEventDateAfter(Instant after);");
        builder.line();
        builder.line("List<PersistentAuditEvent> findByPrincipalAndAuditEventDateAfter(String principal, Instant after);");
        builder.line();
        builder.line("List<PersistentAuditEvent> findByPrincipalAndAuditEventDateAfterAndAuditEventType(String principal, Instant after, String type);");
        builder.line();
        builder.line("Page<PersistentAuditEvent> findAllByAuditEventDateBetween(Instant fromDate, Instant toDate, Pageable pageable);");
        builder.line();
        builder.line("List<PersistentAuditEvent> findByAuditEventDateBefore(Instant before);");
        builder.outdent();
        builder.line("}");

        writeFile(getMainJavaPath() + "repository/PersistentAuditEventRepository.java", builder.build());
    }

    private void writeAuditEventConverter() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config.audit");

        builder.addImports(
            config.getPackageName() + ".domain.PersistentAuditEvent",
            "org.springframework.boot.actuate.audit.AuditEvent",
            "org.springframework.security.web.authentication.WebAuthenticationDetails",
            "org.springframework.stereotype.Component",
            "java.util.*"
        );

        builder.javadoc("Converts {@link PersistentAuditEvent} to {@link AuditEvent} and vice versa.");
        builder.annotation("Component");

        builder.classDeclaration("public", "AuditEventConverter", null);

        // convertToAuditEvent
        builder.javadoc("Convert a list of {@link PersistentAuditEvent} to a list of {@link AuditEvent}.");
        builder.methodSignature("public", "List<AuditEvent>", "convertToAuditEvent", "Iterable<PersistentAuditEvent> persistentAuditEvents");
        builder.ifStatement("persistentAuditEvents == null");
        builder.returnStatement("Collections.emptyList()");
        builder.closeIf();
        builder.statement("List<AuditEvent> auditEvents = new ArrayList<>()");
        builder.statement("persistentAuditEvents.forEach(e -> auditEvents.add(convertToAuditEvent(e)))");
        builder.returnStatement("auditEvents");
        builder.closeMethod();
        builder.line();

        // convertToAuditEvent single
        builder.javadoc("Convert a {@link PersistentAuditEvent} to an {@link AuditEvent}.");
        builder.methodSignature("public", "AuditEvent", "convertToAuditEvent", "PersistentAuditEvent persistentAuditEvent");
        builder.ifStatement("persistentAuditEvent == null");
        builder.returnStatement("null");
        builder.closeIf();
        builder.returnStatement("new AuditEvent(persistentAuditEvent.getAuditEventDate(), persistentAuditEvent.getPrincipal(),\n" +
            "            persistentAuditEvent.getAuditEventType(), convertDataToObjects(persistentAuditEvent.getData()))");
        builder.closeMethod();
        builder.line();

        // convertToPersistentAuditEvent
        builder.javadoc("Convert an {@link AuditEvent} to a {@link PersistentAuditEvent}.");
        builder.methodSignature("public", "PersistentAuditEvent", "convertToPersistentAuditEvent", "AuditEvent auditEvent");
        builder.ifStatement("auditEvent == null");
        builder.returnStatement("null");
        builder.closeIf();
        builder.statement("PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent()");
        builder.statement("persistentAuditEvent.setPrincipal(auditEvent.getPrincipal())");
        builder.statement("persistentAuditEvent.setAuditEventType(auditEvent.getType())");
        builder.statement("persistentAuditEvent.setAuditEventDate(auditEvent.getTimestamp())");
        builder.statement("persistentAuditEvent.setData(convertDataToStrings(auditEvent.getData()))");
        builder.returnStatement("persistentAuditEvent");
        builder.closeMethod();
        builder.line();

        // convertDataToObjects
        builder.javadoc("Convert data map to objects.");
        builder.methodSignature("public", "Map<String, Object>", "convertDataToObjects", "Map<String, String> data");
        builder.statement("Map<String, Object> results = new HashMap<>()");
        builder.ifStatement("data != null");
        builder.statement("data.forEach((key, value) -> results.put(key, value))");
        builder.closeIf();
        builder.returnStatement("results");
        builder.closeMethod();
        builder.line();

        // convertDataToStrings
        builder.javadoc("Convert data map to strings.");
        builder.methodSignature("public", "Map<String, String>", "convertDataToStrings", "Map<String, Object> data");
        builder.statement("Map<String, String> results = new HashMap<>()");
        builder.ifStatement("data != null");
        builder.forStatement("Map.Entry<String, Object> entry : data.entrySet()");
        builder.ifStatement("entry.getValue() instanceof WebAuthenticationDetails");
        builder.statement("WebAuthenticationDetails authenticationDetails = (WebAuthenticationDetails) entry.getValue()");
        builder.statement("results.put(\"remoteAddress\", authenticationDetails.getRemoteAddress())");
        builder.statement("results.put(\"sessionId\", authenticationDetails.getSessionId())");
        builder.elseStatement();
        builder.statement("results.put(entry.getKey(), Objects.toString(entry.getValue()))");
        builder.closeIf();
        builder.closeFor();
        builder.closeIf();
        builder.returnStatement("results");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/audit/AuditEventConverter.java", builder.build());
    }

    private void writeCustomAuditEventRepository() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".repository");

        builder.addImports(
            config.getPackageName() + ".config.audit.AuditEventConverter",
            config.getPackageName() + ".domain.PersistentAuditEvent",
            "org.slf4j.Logger",
            "org.slf4j.LoggerFactory",
            "org.springframework.boot.actuate.audit.AuditEvent",
            "org.springframework.boot.actuate.audit.AuditEventRepository",
            "org.springframework.stereotype.Repository",
            "org.springframework.transaction.annotation.Propagation",
            "org.springframework.transaction.annotation.Transactional",
            "java.time.Instant",
            "java.util.*"
        );

        builder.javadoc("Custom implementation of Spring Boot's {@link AuditEventRepository}.");
        builder.annotation("Repository");

        builder.classDeclaration("public", "CustomAuditEventRepository", null, "AuditEventRepository");

        builder.staticFinalField("int", "EVENT_DATA_COLUMN_MAX_LENGTH", "255");
        builder.line();

        builder.field("private final", "Logger", "log", "LoggerFactory.getLogger(CustomAuditEventRepository.class)");
        builder.line();

        builder.field("private final", "PersistentAuditEventRepository", "persistenceAuditEventRepository");
        builder.field("private final", "AuditEventConverter", "auditEventConverter");
        builder.line();

        builder.constructor("public", "CustomAuditEventRepository",
            "PersistentAuditEventRepository persistenceAuditEventRepository",
            "AuditEventConverter auditEventConverter");
        builder.statement("this.persistenceAuditEventRepository = persistenceAuditEventRepository");
        builder.statement("this.auditEventConverter = auditEventConverter");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.methodSignature("public", "List<AuditEvent>", "find", "String principal", "Instant after", "String type");
        builder.statement("Iterable<PersistentAuditEvent> persistentAuditEvents = persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfterAndAuditEventType(principal, after, type)");
        builder.returnStatement("auditEventConverter.convertToAuditEvent(persistentAuditEvents)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Override");
        builder.annotation("Transactional", "propagation = Propagation.REQUIRES_NEW");
        builder.methodSignature("public", "void", "add", "AuditEvent event");
        builder.ifStatement("!\"AUTHORIZATION_FAILURE\".equals(event.getType()) && !\"AUTHENTICATION_SUCCESS\".equals(event.getType()) && !\"AUTHENTICATION_FAILURE\".equals(event.getType())");
        builder.returnStatement(null);
        builder.closeIf();
        builder.statement("PersistentAuditEvent persistentAuditEvent = auditEventConverter.convertToPersistentAuditEvent(event)");
        builder.statement("persistentAuditEvent.setData(truncate(persistentAuditEvent.getData()))");
        builder.statement("persistenceAuditEventRepository.save(persistentAuditEvent)");
        builder.closeMethod();
        builder.line();

        // truncate method
        builder.javadoc("Truncate event data that might exceed column length.");
        builder.methodSignature("private", "Map<String, String>", "truncate", "Map<String, String> data");
        builder.statement("Map<String, String> results = new HashMap<>()");
        builder.ifStatement("data != null");
        builder.forStatement("Map.Entry<String, String> entry : data.entrySet()");
        builder.statement("String value = entry.getValue()");
        builder.ifStatement("value != null");
        builder.statement("int length = value.length()");
        builder.ifStatement("length > EVENT_DATA_COLUMN_MAX_LENGTH");
        builder.statement("value = value.substring(0, EVENT_DATA_COLUMN_MAX_LENGTH)");
        builder.statement("log.warn(\"Event data for {} too long ({}) has been truncated to {}\", entry.getKey(), length, EVENT_DATA_COLUMN_MAX_LENGTH)");
        builder.closeIf();
        builder.closeIf();
        builder.statement("results.put(entry.getKey(), value)");
        builder.closeFor();
        builder.closeIf();
        builder.returnStatement("results");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "repository/CustomAuditEventRepository.java", builder.build());
    }
}
