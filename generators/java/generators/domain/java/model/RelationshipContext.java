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

import io.github.jhipster.generator.domain.support.JavaBeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Relationship context for domain layer generation.
 * Contains all relationship information needed for template rendering.
 */
public class RelationshipContext {

    // Relationship identification
    private String relationshipName;
    private String relationshipNameCapitalized;
    private String relationshipNameCapitalizedPlural;
    private String relationshipFieldName;
    private String relationshipFieldNamePlural;
    private String relationshipType;
    private String propertyName;
    private String propertyJavaBeanName;

    // Other entity reference
    private String otherEntityName;
    private String otherEntityNamePlural;
    private String otherEntityField;
    private EntityContext otherEntity;

    // Bidirectional relationship
    private String otherEntityRelationshipName;
    private RelationshipContext otherRelationship;
    private String relationshipReferenceField;

    // Relationship flags
    private boolean collection;
    private boolean ownerSide;
    private boolean required;
    private boolean ignoreOtherSideProperty;
    private boolean relationshipUpdateBackReference;

    // Relationship types
    private boolean relationshipOneToOne;
    private boolean relationshipOneToMany;
    private boolean relationshipManyToOne;
    private boolean relationshipManyToMany;

    // Validation rules
    private List<String> relationshipValidateRules = new ArrayList<>();

    // Documentation
    private String relationshipJavadoc;
    private String relationshipApiDescription;

    // DTO mapping
    private String propertyDtoJavaType;

    public RelationshipContext() {
    }

    public RelationshipContext(String relationshipName, String otherEntityName, String relationshipType) {
        setRelationshipName(relationshipName);
        setOtherEntityName(otherEntityName);
        setRelationshipType(relationshipType);
    }

    /**
     * Prepares computed properties after relationship configuration.
     */
    public void prepare() {
        // Set property name variants
        if (propertyName == null) {
            propertyName = relationshipName;
        }
        propertyJavaBeanName = JavaBeanUtils.javaBeanCase(propertyName);

        // Determine collection and field names
        collection = relationshipOneToMany || relationshipManyToMany;

        if (collection) {
            relationshipFieldName = relationshipName;
            relationshipFieldNamePlural = relationshipName;
            if (!relationshipFieldNamePlural.endsWith("s")) {
                relationshipFieldNamePlural = JavaBeanUtils.pluralize(relationshipFieldNamePlural);
            }
        } else {
            relationshipFieldName = relationshipName;
            relationshipFieldNamePlural = JavaBeanUtils.pluralize(relationshipName);
        }

        // Set reference field for JSON ignore
        relationshipReferenceField = collection ? relationshipFieldNamePlural : relationshipFieldName;

        // Required flag from validation rules
        required = relationshipValidateRules.contains("required");
    }

    // Getters and Setters

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
        this.relationshipNameCapitalized = capitalize(relationshipName);
        this.relationshipNameCapitalizedPlural = capitalize(JavaBeanUtils.pluralize(relationshipName));
    }

    public String getRelationshipNameCapitalized() {
        return relationshipNameCapitalized;
    }

    public String getRelationshipNameCapitalizedPlural() {
        return relationshipNameCapitalizedPlural;
    }

    public String getRelationshipFieldName() {
        return relationshipFieldName;
    }

    public void setRelationshipFieldName(String relationshipFieldName) {
        this.relationshipFieldName = relationshipFieldName;
    }

    public String getRelationshipFieldNamePlural() {
        return relationshipFieldNamePlural;
    }

    public void setRelationshipFieldNamePlural(String relationshipFieldNamePlural) {
        this.relationshipFieldNamePlural = relationshipFieldNamePlural;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;

        // Set type flags
        this.relationshipOneToOne = "one-to-one".equalsIgnoreCase(relationshipType);
        this.relationshipOneToMany = "one-to-many".equalsIgnoreCase(relationshipType);
        this.relationshipManyToOne = "many-to-one".equalsIgnoreCase(relationshipType);
        this.relationshipManyToMany = "many-to-many".equalsIgnoreCase(relationshipType);

        // Collection flag
        this.collection = relationshipOneToMany || relationshipManyToMany;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyJavaBeanName() {
        return propertyJavaBeanName;
    }

    public void setPropertyJavaBeanName(String propertyJavaBeanName) {
        this.propertyJavaBeanName = propertyJavaBeanName;
    }

    public String getOtherEntityName() {
        return otherEntityName;
    }

    public void setOtherEntityName(String otherEntityName) {
        this.otherEntityName = otherEntityName;
        this.otherEntityNamePlural = JavaBeanUtils.pluralize(otherEntityName);
    }

    public String getOtherEntityNamePlural() {
        return otherEntityNamePlural;
    }

    public String getOtherEntityField() {
        return otherEntityField;
    }

    public void setOtherEntityField(String otherEntityField) {
        this.otherEntityField = otherEntityField;
    }

    public EntityContext getOtherEntity() {
        return otherEntity;
    }

    public void setOtherEntity(EntityContext otherEntity) {
        this.otherEntity = otherEntity;
    }

    public String getOtherEntityRelationshipName() {
        return otherEntityRelationshipName;
    }

    public void setOtherEntityRelationshipName(String otherEntityRelationshipName) {
        this.otherEntityRelationshipName = otherEntityRelationshipName;
    }

    public RelationshipContext getOtherRelationship() {
        return otherRelationship;
    }

    public void setOtherRelationship(RelationshipContext otherRelationship) {
        this.otherRelationship = otherRelationship;
    }

    public String getRelationshipReferenceField() {
        return relationshipReferenceField;
    }

    public void setRelationshipReferenceField(String relationshipReferenceField) {
        this.relationshipReferenceField = relationshipReferenceField;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public boolean isOwnerSide() {
        return ownerSide;
    }

    public void setOwnerSide(boolean ownerSide) {
        this.ownerSide = ownerSide;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isIgnoreOtherSideProperty() {
        return ignoreOtherSideProperty;
    }

    public void setIgnoreOtherSideProperty(boolean ignoreOtherSideProperty) {
        this.ignoreOtherSideProperty = ignoreOtherSideProperty;
    }

    public boolean isRelationshipUpdateBackReference() {
        return relationshipUpdateBackReference;
    }

    public void setRelationshipUpdateBackReference(boolean relationshipUpdateBackReference) {
        this.relationshipUpdateBackReference = relationshipUpdateBackReference;
    }

    public boolean isRelationshipOneToOne() {
        return relationshipOneToOne;
    }

    public boolean isRelationshipOneToMany() {
        return relationshipOneToMany;
    }

    public boolean isRelationshipManyToOne() {
        return relationshipManyToOne;
    }

    public boolean isRelationshipManyToMany() {
        return relationshipManyToMany;
    }

    public List<String> getRelationshipValidateRules() {
        return relationshipValidateRules;
    }

    public void setRelationshipValidateRules(List<String> relationshipValidateRules) {
        this.relationshipValidateRules = relationshipValidateRules != null ? relationshipValidateRules : new ArrayList<>();
        this.required = this.relationshipValidateRules.contains("required");
    }

    public String getRelationshipJavadoc() {
        return relationshipJavadoc;
    }

    public void setRelationshipJavadoc(String relationshipJavadoc) {
        this.relationshipJavadoc = relationshipJavadoc;
    }

    public String getRelationshipApiDescription() {
        return relationshipApiDescription;
    }

    public void setRelationshipApiDescription(String relationshipApiDescription) {
        this.relationshipApiDescription = relationshipApiDescription;
    }

    public String getPropertyDtoJavaType() {
        return propertyDtoJavaType;
    }

    public void setPropertyDtoJavaType(String propertyDtoJavaType) {
        this.propertyDtoJavaType = propertyDtoJavaType;
    }

    // Utility methods

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipContext that = (RelationshipContext) o;
        return Objects.equals(relationshipName, that.relationshipName) &&
               Objects.equals(otherEntityName, that.otherEntityName) &&
               Objects.equals(relationshipType, that.relationshipType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationshipName, otherEntityName, relationshipType);
    }

    @Override
    public String toString() {
        return "RelationshipContext{" +
               "relationshipName='" + relationshipName + '\'' +
               ", otherEntityName='" + otherEntityName + '\'' +
               ", relationshipType='" + relationshipType + '\'' +
               ", collection=" + collection +
               '}';
    }
}
