/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster project, see https://www.jhipster.tech/
 * for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jhipster.generator.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jhipster.generator.entity.enums.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity configuration as stored in .jhipster/{EntityName}.json files.
 * This class represents the complete entity definition including fields, relationships,
 * and generation options.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Entity identification
    private String name;
    private String entityTableName;
    private String changelogDate;

    // Database configuration
    private String databaseType;
    private String prodDatabaseType;
    private String devDatabaseType;

    // Generation options
    private Boolean skipServer;
    private Boolean skipClient;
    private Boolean skipDbChangelog;
    private Boolean skipCheckLengthOfIdentifier;
    private Boolean readOnly;
    private Boolean embedded;

    // Service layer options
    private String service;
    private String dto;
    private Boolean jpaMetamodelFiltering;
    private String pagination;

    // Microservice options
    private String microserviceName;
    private String microservicePath;
    private String clientRootFolder;
    private Boolean skipUiGrouping;

    // Angular specific
    private String angularJSSuffix;

    // Fields and relationships
    private List<FieldConfig> fields = new ArrayList<>();
    private List<RelationshipConfig> relationships = new ArrayList<>();

    // Constructors
    public EntityConfig() {
    }

    public EntityConfig(String name) {
        this.name = name;
    }

    // Factory methods
    public static EntityConfig load(File file) throws IOException {
        return MAPPER.readValue(file, EntityConfig.class);
    }

    public static EntityConfig load(String json) throws IOException {
        return MAPPER.readValue(json, EntityConfig.class);
    }

    public void save(File file) throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, this);
    }

    public String toJson() throws IOException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityTableName() {
        return entityTableName;
    }

    public void setEntityTableName(String entityTableName) {
        this.entityTableName = entityTableName;
    }

    public String getChangelogDate() {
        return changelogDate;
    }

    public void setChangelogDate(String changelogDate) {
        this.changelogDate = changelogDate;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getProdDatabaseType() {
        return prodDatabaseType;
    }

    public void setProdDatabaseType(String prodDatabaseType) {
        this.prodDatabaseType = prodDatabaseType;
    }

    public String getDevDatabaseType() {
        return devDatabaseType;
    }

    public void setDevDatabaseType(String devDatabaseType) {
        this.devDatabaseType = devDatabaseType;
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

    public Boolean getSkipDbChangelog() {
        return skipDbChangelog;
    }

    public void setSkipDbChangelog(Boolean skipDbChangelog) {
        this.skipDbChangelog = skipDbChangelog;
    }

    public Boolean getSkipCheckLengthOfIdentifier() {
        return skipCheckLengthOfIdentifier;
    }

    public void setSkipCheckLengthOfIdentifier(Boolean skipCheckLengthOfIdentifier) {
        this.skipCheckLengthOfIdentifier = skipCheckLengthOfIdentifier;
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

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getDto() {
        return dto;
    }

    public void setDto(String dto) {
        this.dto = dto;
    }

    public Boolean getJpaMetamodelFiltering() {
        return jpaMetamodelFiltering;
    }

    public void setJpaMetamodelFiltering(Boolean jpaMetamodelFiltering) {
        this.jpaMetamodelFiltering = jpaMetamodelFiltering;
    }

    public String getPagination() {
        return pagination;
    }

    public void setPagination(String pagination) {
        this.pagination = pagination;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }

    public void setMicroserviceName(String microserviceName) {
        this.microserviceName = microserviceName;
    }

    public String getMicroservicePath() {
        return microservicePath;
    }

    public void setMicroservicePath(String microservicePath) {
        this.microservicePath = microservicePath;
    }

    public String getClientRootFolder() {
        return clientRootFolder;
    }

    public void setClientRootFolder(String clientRootFolder) {
        this.clientRootFolder = clientRootFolder;
    }

    public Boolean getSkipUiGrouping() {
        return skipUiGrouping;
    }

    public void setSkipUiGrouping(Boolean skipUiGrouping) {
        this.skipUiGrouping = skipUiGrouping;
    }

    public String getAngularJSSuffix() {
        return angularJSSuffix;
    }

    public void setAngularJSSuffix(String angularJSSuffix) {
        this.angularJSSuffix = angularJSSuffix;
    }

    public List<FieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<FieldConfig> fields) {
        this.fields = fields != null ? fields : new ArrayList<>();
    }

    public List<RelationshipConfig> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<RelationshipConfig> relationships) {
        this.relationships = relationships != null ? relationships : new ArrayList<>();
    }

    // Utility methods
    public void addField(FieldConfig field) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        fields.add(field);
    }

    public void removeField(String fieldName) {
        if (fields != null) {
            fields.removeIf(f -> f.getFieldName().equals(fieldName));
        }
    }

    public FieldConfig getField(String fieldName) {
        if (fields == null) {
            return null;
        }
        return fields.stream()
            .filter(f -> f.getFieldName().equals(fieldName))
            .findFirst()
            .orElse(null);
    }

    public void addRelationship(RelationshipConfig relationship) {
        if (relationships == null) {
            relationships = new ArrayList<>();
        }
        relationships.add(relationship);
    }

    public void removeRelationship(String relationshipName, String relationshipType) {
        if (relationships != null) {
            relationships.removeIf(r ->
                r.getRelationshipName().equals(relationshipName) &&
                r.getRelationshipType().equals(relationshipType));
        }
    }

    public DatabaseType getDatabaseTypeEnum() {
        if (databaseType == null) {
            return null;
        }
        return DatabaseType.fromString(databaseType);
    }

    public ServiceType getServiceTypeEnum() {
        if (service == null) {
            return ServiceType.NO;
        }
        return ServiceType.fromString(service);
    }

    public MapperType getMapperTypeEnum() {
        if (dto == null) {
            return MapperType.NO;
        }
        return MapperType.fromString(dto);
    }

    public PaginationType getPaginationTypeEnum() {
        if (pagination == null) {
            return PaginationType.NO;
        }
        return PaginationType.fromString(pagination);
    }

    public boolean hasService() {
        return getServiceTypeEnum().hasService();
    }

    public boolean hasDto() {
        return getMapperTypeEnum().hasMapper();
    }

    public boolean hasFiltering() {
        return Boolean.TRUE.equals(jpaMetamodelFiltering);
    }

    public boolean hasPagination() {
        return getPaginationTypeEnum().hasPagination();
    }

    public boolean isReadOnly() {
        return Boolean.TRUE.equals(readOnly);
    }

    public boolean isEmbedded() {
        return Boolean.TRUE.equals(embedded);
    }

    public boolean shouldSkipServer() {
        return Boolean.TRUE.equals(skipServer);
    }

    public boolean shouldSkipClient() {
        return Boolean.TRUE.equals(skipClient);
    }

    public String getNameCapitalized() {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public String getNameLowerFirst() {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public boolean hasRelationships() {
        return relationships != null && !relationships.isEmpty();
    }

    public boolean hasFields() {
        return fields != null && !fields.isEmpty();
    }

    public List<RelationshipConfig> getRelationshipsByType(RelationshipType type) {
        if (relationships == null) {
            return new ArrayList<>();
        }
        return relationships.stream()
            .filter(r -> r.getRelationshipTypeEnum() == type)
            .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityConfig that = (EntityConfig) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "EntityConfig{" +
               "name='" + name + '\'' +
               ", fields=" + (fields != null ? fields.size() : 0) +
               ", relationships=" + (relationships != null ? relationships.size() : 0) +
               ", service='" + service + '\'' +
               ", dto='" + dto + '\'' +
               ", pagination='" + pagination + '\'' +
               '}';
    }
}
