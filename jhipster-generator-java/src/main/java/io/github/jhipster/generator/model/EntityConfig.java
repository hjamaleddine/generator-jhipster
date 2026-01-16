/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity configuration model.
 * Represents a JHipster entity definition.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityConfig {

    @JsonProperty("name")
    private String name;

    @JsonProperty("entityTableName")
    private String entityTableName;

    @JsonProperty("dto")
    private String dto = "mapstruct";

    @JsonProperty("service")
    private String service = "serviceImpl";

    @JsonProperty("pagination")
    private String pagination = "pagination";

    @JsonProperty("readOnly")
    private Boolean readOnly = false;

    @JsonProperty("embedded")
    private Boolean embedded = false;

    @JsonProperty("skipServer")
    private Boolean skipServer = false;

    @JsonProperty("skipClient")
    private Boolean skipClient = false;

    @JsonProperty("javadoc")
    private String javadoc;

    @JsonProperty("jpaMetamodelFiltering")
    private Boolean jpaMetamodelFiltering = true;

    @JsonProperty("fluentMethods")
    private Boolean fluentMethods = true;

    @JsonProperty("fields")
    private List<FieldConfig> fields = new ArrayList<>();

    @JsonProperty("relationships")
    private List<RelationshipConfig> relationships = new ArrayList<>();

    @JsonProperty("microserviceName")
    private String microserviceName;

    @JsonProperty("clientRootFolder")
    private String clientRootFolder;

    @JsonProperty("entityPackage")
    private String entityPackage;

    // Computed properties (set during preparation)
    private String persistClass;
    private String persistInstance;
    private String restClass;
    private String restInstance;
    private String dtoClass;
    private String dtoInstance;
    private String entityAbsolutePackage;
    private String entityAbsoluteClass;
    private PrimaryKeyConfig primaryKey;

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityTableName() {
        if (entityTableName == null && name != null) {
            return toSnakeCase(name);
        }
        return entityTableName;
    }

    public void setEntityTableName(String entityTableName) {
        this.entityTableName = entityTableName;
    }

    public String getDto() {
        return dto;
    }

    public void setDto(String dto) {
        this.dto = dto;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPagination() {
        return pagination;
    }

    public void setPagination(String pagination) {
        this.pagination = pagination;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getEmbedded() {
        return embedded;
    }

    public void setEmbedded(Boolean embedded) {
        this.embedded = embedded;
    }

    public Boolean getSkipServer() {
        return skipServer;
    }

    public void setSkipServer(Boolean skipServer) {
        this.skipServer = skipServer;
    }

    public Boolean getSkipClient() {
        return skipClient;
    }

    public void setSkipClient(Boolean skipClient) {
        this.skipClient = skipClient;
    }

    public String getJavadoc() {
        return javadoc;
    }

    public void setJavadoc(String javadoc) {
        this.javadoc = javadoc;
    }

    public Boolean getJpaMetamodelFiltering() {
        return jpaMetamodelFiltering;
    }

    public void setJpaMetamodelFiltering(Boolean jpaMetamodelFiltering) {
        this.jpaMetamodelFiltering = jpaMetamodelFiltering;
    }

    public Boolean getFluentMethods() {
        return fluentMethods;
    }

    public void setFluentMethods(Boolean fluentMethods) {
        this.fluentMethods = fluentMethods;
    }

    public List<FieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<FieldConfig> fields) {
        this.fields = fields;
    }

    public List<RelationshipConfig> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<RelationshipConfig> relationships) {
        this.relationships = relationships;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }

    public void setMicroserviceName(String microserviceName) {
        this.microserviceName = microserviceName;
    }

    public String getClientRootFolder() {
        return clientRootFolder;
    }

    public void setClientRootFolder(String clientRootFolder) {
        this.clientRootFolder = clientRootFolder;
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
    }

    // Computed properties

    public String getPersistClass() {
        if (persistClass == null && name != null) {
            return name;
        }
        return persistClass;
    }

    public void setPersistClass(String persistClass) {
        this.persistClass = persistClass;
    }

    public String getPersistInstance() {
        if (persistInstance == null && name != null) {
            return decapitalize(name);
        }
        return persistInstance;
    }

    public void setPersistInstance(String persistInstance) {
        this.persistInstance = persistInstance;
    }

    public String getRestClass() {
        return restClass != null ? restClass : getPersistClass();
    }

    public void setRestClass(String restClass) {
        this.restClass = restClass;
    }

    public String getRestInstance() {
        return restInstance != null ? restInstance : getPersistInstance();
    }

    public void setRestInstance(String restInstance) {
        this.restInstance = restInstance;
    }

    public String getDtoClass() {
        if (dtoClass == null && name != null) {
            return name + "DTO";
        }
        return dtoClass;
    }

    public void setDtoClass(String dtoClass) {
        this.dtoClass = dtoClass;
    }

    public String getDtoInstance() {
        if (dtoInstance == null && name != null) {
            return decapitalize(name) + "DTO";
        }
        return dtoInstance;
    }

    public void setDtoInstance(String dtoInstance) {
        this.dtoInstance = dtoInstance;
    }

    public String getEntityAbsolutePackage() {
        return entityAbsolutePackage;
    }

    public void setEntityAbsolutePackage(String entityAbsolutePackage) {
        this.entityAbsolutePackage = entityAbsolutePackage;
    }

    public String getEntityAbsoluteClass() {
        return entityAbsoluteClass;
    }

    public void setEntityAbsoluteClass(String entityAbsoluteClass) {
        this.entityAbsoluteClass = entityAbsoluteClass;
    }

    public PrimaryKeyConfig getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKeyConfig primaryKey) {
        this.primaryKey = primaryKey;
    }

    // Helper methods

    public boolean hasDto() {
        return "mapstruct".equals(dto);
    }

    public boolean hasService() {
        return service != null && !"no".equals(service);
    }

    public boolean hasServiceImpl() {
        return "serviceImpl".equals(service);
    }

    public boolean hasPagination() {
        return pagination != null && !"no".equals(pagination);
    }

    public boolean hasInfiniteScroll() {
        return "infinite-scroll".equals(pagination);
    }

    public boolean hasFiltering() {
        return Boolean.TRUE.equals(jpaMetamodelFiltering);
    }

    public FieldConfig getIdField() {
        return fields.stream()
            .filter(f -> Boolean.TRUE.equals(f.getId()))
            .findFirst()
            .orElse(null);
    }

    public boolean hasCollectionRelationships() {
        return relationships.stream().anyMatch(RelationshipConfig::isCollection);
    }

    public boolean hasRequiredRelationships() {
        return relationships.stream().anyMatch(r -> Boolean.TRUE.equals(r.getRelationshipRequired()));
    }

    public boolean isDto() {
        return hasDto();
    }

    public boolean isServiceClass() {
        return hasService();
    }

    public boolean isFiltering() {
        return hasFiltering();
    }

    public boolean isReadOnly() {
        return Boolean.TRUE.equals(readOnly);
    }

    public boolean isBuiltIn() {
        // User and Authority are built-in entities
        return "User".equals(name) || "Authority".equals(name);
    }

    // Utility methods

    private static String decapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    private static String toSnakeCase(String str) {
        if (str == null) return null;
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
