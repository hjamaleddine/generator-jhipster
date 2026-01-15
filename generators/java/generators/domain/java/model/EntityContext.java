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
package io.github.jhipster.generator.domain.model;

import java.util.*;

/**
 * Context containing all entity information needed for domain layer generation.
 * This consolidates entity data with application configuration for template rendering.
 */
public class EntityContext {

    // Entity identification
    private String entityName;
    private String entityNameCapitalized;
    private String entityNamePlural;
    private String entityInstance;
    private String entityInstancePlural;
    private String persistClass;
    private String entityClass;
    private String entityAbsoluteClass;

    // Package information
    private String packageName;
    private String entityPackage;
    private String entityAbsolutePackage;
    private String entityJavaPackageFolder;
    private String javaPackageSrcDir;
    private String javaPackageTestDir;

    // Entity configuration
    private boolean entityDomainLayer = true;
    private boolean embedded;
    private boolean builtIn;
    private boolean skipServer;
    private boolean readOnly;
    private boolean updatableEntity = true;
    private boolean fluentMethods = true;

    // DTO configuration
    private boolean dtoMapstruct;
    private String dtoClass;

    // Primary key
    private PrimaryKeyInfo primaryKey;

    // Fields and relationships
    private List<FieldContext> fields = new ArrayList<>();
    private List<RelationshipContext> relationships = new ArrayList<>();

    // Other entities referenced by relationships
    private Set<EntityContext> otherEntities = new HashSet<>();

    // Computed flags based on fields
    private boolean anyFieldIsBigDecimal;
    private boolean anyFieldIsInstant;
    private boolean anyFieldIsLocalDate;
    private boolean anyFieldIsZonedDateTime;
    private boolean anyFieldIsLocalTime;
    private boolean anyFieldIsDuration;
    private boolean anyFieldIsUUID;
    private boolean entityContainsCollectionField;
    private boolean relationshipsContainOtherSideIgnore;
    private boolean otherEntityPrimaryKeyTypesIncludesUUID;

    // Validation flags
    private boolean useJakartaValidation;
    private boolean useJacksonIdentityInfo;

    // API documentation
    private String entityJavadoc;
    private String entityApiDescription;
    private boolean importApiModelProperty;

    // Test configuration
    private String skipJunitTests;
    private boolean hasCyclicRequiredRelationship;

    // Unique enums used by this entity
    private Map<String, Object> uniqueEnums = new LinkedHashMap<>();

    // Client configuration
    private String frontendAppName;
    private String clientRootFolder;

    public EntityContext() {
    }

    public EntityContext(String entityName) {
        setEntityName(entityName);
    }

    // Computed property methods

    /**
     * Analyzes fields and sets computed flags.
     */
    public void computeFieldFlags() {
        anyFieldIsBigDecimal = fields.stream().anyMatch(f -> "BigDecimal".equals(f.getJavaFieldType()));
        anyFieldIsInstant = fields.stream().anyMatch(f -> "Instant".equals(f.getJavaFieldType()));
        anyFieldIsLocalDate = fields.stream().anyMatch(f -> "LocalDate".equals(f.getJavaFieldType()));
        anyFieldIsZonedDateTime = fields.stream().anyMatch(f -> "ZonedDateTime".equals(f.getJavaFieldType()));
        anyFieldIsLocalTime = fields.stream().anyMatch(f -> "LocalTime".equals(f.getJavaFieldType()));
        anyFieldIsDuration = fields.stream().anyMatch(f -> "Duration".equals(f.getJavaFieldType()));
        anyFieldIsUUID = fields.stream().anyMatch(f -> "UUID".equals(f.getJavaFieldType()));

        // Collect unique enums
        uniqueEnums.clear();
        fields.stream()
            .filter(FieldContext::isFieldIsEnum)
            .forEach(f -> uniqueEnums.put(f.getFieldType(), f));
    }

    /**
     * Analyzes relationships and sets computed flags.
     */
    public void computeRelationshipFlags() {
        entityContainsCollectionField = relationships.stream()
            .anyMatch(RelationshipContext::isCollection);

        relationshipsContainOtherSideIgnore = relationships.stream()
            .anyMatch(RelationshipContext::isIgnoreOtherSideProperty);

        // Check for cyclic required relationships
        hasCyclicRequiredRelationship = checkForCyclicRequiredRelationships();

        if (hasCyclicRequiredRelationship) {
            skipJunitTests = "Cyclic required relationships detected";
        }
    }

    private boolean checkForCyclicRequiredRelationships() {
        // Simplified cyclic check - in practice, this needs graph traversal
        return relationships.stream()
            .filter(RelationshipContext::isRequired)
            .anyMatch(r -> {
                EntityContext other = r.getOtherEntity();
                if (other == null) return false;
                return other.getRelationships().stream()
                    .filter(RelationshipContext::isRequired)
                    .anyMatch(otherRel -> entityName.equals(otherRel.getOtherEntityName()));
            });
    }

    // Getters and Setters

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
        this.entityNameCapitalized = capitalize(entityName);
        this.entityInstance = uncapitalize(entityName);
        this.persistClass = entityNameCapitalized;
        this.entityClass = entityNameCapitalized;
    }

    public String getEntityNameCapitalized() {
        return entityNameCapitalized;
    }

    public String getEntityNamePlural() {
        return entityNamePlural;
    }

    public void setEntityNamePlural(String entityNamePlural) {
        this.entityNamePlural = entityNamePlural;
        this.entityInstancePlural = uncapitalize(entityNamePlural);
    }

    public String getEntityInstance() {
        return entityInstance;
    }

    public String getEntityInstancePlural() {
        return entityInstancePlural;
    }

    public String getPersistClass() {
        return persistClass;
    }

    public void setPersistClass(String persistClass) {
        this.persistClass = persistClass;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }

    public String getEntityAbsoluteClass() {
        return entityAbsoluteClass;
    }

    public void setEntityAbsoluteClass(String entityAbsoluteClass) {
        this.entityAbsoluteClass = entityAbsoluteClass;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
        if (this.entityAbsolutePackage == null) {
            this.entityAbsolutePackage = packageName;
        }
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
    }

    public String getEntityAbsolutePackage() {
        return entityAbsolutePackage;
    }

    public void setEntityAbsolutePackage(String entityAbsolutePackage) {
        this.entityAbsolutePackage = entityAbsolutePackage;
    }

    public String getEntityJavaPackageFolder() {
        return entityJavaPackageFolder;
    }

    public void setEntityJavaPackageFolder(String entityJavaPackageFolder) {
        this.entityJavaPackageFolder = entityJavaPackageFolder;
    }

    public String getJavaPackageSrcDir() {
        return javaPackageSrcDir;
    }

    public void setJavaPackageSrcDir(String javaPackageSrcDir) {
        this.javaPackageSrcDir = javaPackageSrcDir;
    }

    public String getJavaPackageTestDir() {
        return javaPackageTestDir;
    }

    public void setJavaPackageTestDir(String javaPackageTestDir) {
        this.javaPackageTestDir = javaPackageTestDir;
    }

    public boolean isEntityDomainLayer() {
        return entityDomainLayer;
    }

    public void setEntityDomainLayer(boolean entityDomainLayer) {
        this.entityDomainLayer = entityDomainLayer;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public boolean isSkipServer() {
        return skipServer;
    }

    public void setSkipServer(boolean skipServer) {
        this.skipServer = skipServer;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isUpdatableEntity() {
        return updatableEntity;
    }

    public void setUpdatableEntity(boolean updatableEntity) {
        this.updatableEntity = updatableEntity;
    }

    public boolean isFluentMethods() {
        return fluentMethods;
    }

    public void setFluentMethods(boolean fluentMethods) {
        this.fluentMethods = fluentMethods;
    }

    public boolean isDtoMapstruct() {
        return dtoMapstruct;
    }

    public void setDtoMapstruct(boolean dtoMapstruct) {
        this.dtoMapstruct = dtoMapstruct;
    }

    public String getDtoClass() {
        return dtoClass;
    }

    public void setDtoClass(String dtoClass) {
        this.dtoClass = dtoClass;
    }

    public PrimaryKeyInfo getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKeyInfo primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<FieldContext> getFields() {
        return fields;
    }

    public void setFields(List<FieldContext> fields) {
        this.fields = fields;
    }

    public void addField(FieldContext field) {
        this.fields.add(field);
    }

    public List<RelationshipContext> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<RelationshipContext> relationships) {
        this.relationships = relationships;
    }

    public void addRelationship(RelationshipContext relationship) {
        this.relationships.add(relationship);
    }

    public Set<EntityContext> getOtherEntities() {
        return otherEntities;
    }

    public void setOtherEntities(Set<EntityContext> otherEntities) {
        this.otherEntities = otherEntities;
    }

    public boolean isAnyFieldIsBigDecimal() {
        return anyFieldIsBigDecimal;
    }

    public boolean isAnyFieldIsInstant() {
        return anyFieldIsInstant;
    }

    public boolean isAnyFieldIsLocalDate() {
        return anyFieldIsLocalDate;
    }

    public boolean isAnyFieldIsZonedDateTime() {
        return anyFieldIsZonedDateTime;
    }

    public boolean isAnyFieldIsLocalTime() {
        return anyFieldIsLocalTime;
    }

    public boolean isAnyFieldIsDuration() {
        return anyFieldIsDuration;
    }

    public boolean isAnyFieldIsUUID() {
        return anyFieldIsUUID;
    }

    public boolean isEntityContainsCollectionField() {
        return entityContainsCollectionField;
    }

    public boolean isRelationshipsContainOtherSideIgnore() {
        return relationshipsContainOtherSideIgnore;
    }

    public boolean isOtherEntityPrimaryKeyTypesIncludesUUID() {
        return otherEntityPrimaryKeyTypesIncludesUUID;
    }

    public void setOtherEntityPrimaryKeyTypesIncludesUUID(boolean otherEntityPrimaryKeyTypesIncludesUUID) {
        this.otherEntityPrimaryKeyTypesIncludesUUID = otherEntityPrimaryKeyTypesIncludesUUID;
    }

    public boolean isUseJakartaValidation() {
        return useJakartaValidation;
    }

    public void setUseJakartaValidation(boolean useJakartaValidation) {
        this.useJakartaValidation = useJakartaValidation;
    }

    public boolean isUseJacksonIdentityInfo() {
        return useJacksonIdentityInfo;
    }

    public void setUseJacksonIdentityInfo(boolean useJacksonIdentityInfo) {
        this.useJacksonIdentityInfo = useJacksonIdentityInfo;
    }

    public String getEntityJavadoc() {
        return entityJavadoc;
    }

    public void setEntityJavadoc(String entityJavadoc) {
        this.entityJavadoc = entityJavadoc;
    }

    public String getEntityApiDescription() {
        return entityApiDescription;
    }

    public void setEntityApiDescription(String entityApiDescription) {
        this.entityApiDescription = entityApiDescription;
    }

    public boolean isImportApiModelProperty() {
        return importApiModelProperty;
    }

    public void setImportApiModelProperty(boolean importApiModelProperty) {
        this.importApiModelProperty = importApiModelProperty;
    }

    public String getSkipJunitTests() {
        return skipJunitTests;
    }

    public void setSkipJunitTests(String skipJunitTests) {
        this.skipJunitTests = skipJunitTests;
    }

    public boolean isHasCyclicRequiredRelationship() {
        return hasCyclicRequiredRelationship;
    }

    public Map<String, Object> getUniqueEnums() {
        return uniqueEnums;
    }

    public String getFrontendAppName() {
        return frontendAppName;
    }

    public void setFrontendAppName(String frontendAppName) {
        this.frontendAppName = frontendAppName;
    }

    public String getClientRootFolder() {
        return clientRootFolder;
    }

    public void setClientRootFolder(String clientRootFolder) {
        this.clientRootFolder = clientRootFolder;
    }

    // Utility methods

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityContext that = (EntityContext) o;
        return Objects.equals(entityName, that.entityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityName);
    }

    @Override
    public String toString() {
        return "EntityContext{" +
               "entityName='" + entityName + '\'' +
               ", fields=" + fields.size() +
               ", relationships=" + relationships.size() +
               ", entityDomainLayer=" + entityDomainLayer +
               '}';
    }
}
