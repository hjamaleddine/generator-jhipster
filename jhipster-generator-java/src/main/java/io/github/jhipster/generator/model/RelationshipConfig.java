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

/**
 * Relationship configuration for an entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationshipConfig {

    @JsonProperty("relationshipName")
    private String relationshipName;

    @JsonProperty("relationshipType")
    private String relationshipType;

    @JsonProperty("otherEntityName")
    private String otherEntityName;

    @JsonProperty("otherEntityRelationshipName")
    private String otherEntityRelationshipName;

    @JsonProperty("otherEntityField")
    private String otherEntityField = "id";

    @JsonProperty("relationshipValidateRules")
    private String relationshipValidateRules;

    @JsonProperty("relationshipRequired")
    private Boolean relationshipRequired = false;

    @JsonProperty("ownerSide")
    private Boolean ownerSide;

    @JsonProperty("useJPADerivedIdentifier")
    private Boolean useJPADerivedIdentifier = false;

    @JsonProperty("javadoc")
    private String javadoc;

    // Join table configuration (for ManyToMany)
    @JsonProperty("joinTable")
    private JoinTableConfig joinTable;

    // Computed properties
    private String relationshipFieldName;
    private String relationshipFieldNamePlural;
    private String relationshipNameCapitalized;
    private String relationshipNameCapitalizedPlural;
    private String otherEntityNamePlural;
    private String otherEntityNameCapitalized;
    private String columnName;
    private EntityConfig otherEntity;
    private RelationshipConfig otherRelationship;

    // Getters and Setters

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getOtherEntityName() {
        return otherEntityName;
    }

    public void setOtherEntityName(String otherEntityName) {
        this.otherEntityName = otherEntityName;
    }

    public String getOtherEntityRelationshipName() {
        return otherEntityRelationshipName;
    }

    public void setOtherEntityRelationshipName(String otherEntityRelationshipName) {
        this.otherEntityRelationshipName = otherEntityRelationshipName;
    }

    public String getOtherEntityField() {
        return otherEntityField;
    }

    public void setOtherEntityField(String otherEntityField) {
        this.otherEntityField = otherEntityField;
    }

    public String getRelationshipValidateRules() {
        return relationshipValidateRules;
    }

    public void setRelationshipValidateRules(String relationshipValidateRules) {
        this.relationshipValidateRules = relationshipValidateRules;
    }

    public Boolean getRelationshipRequired() {
        return relationshipRequired;
    }

    public void setRelationshipRequired(Boolean relationshipRequired) {
        this.relationshipRequired = relationshipRequired;
    }

    public Boolean getOwnerSide() {
        return ownerSide;
    }

    public void setOwnerSide(Boolean ownerSide) {
        this.ownerSide = ownerSide;
    }

    public Boolean getUseJPADerivedIdentifier() {
        return useJPADerivedIdentifier;
    }

    public void setUseJPADerivedIdentifier(Boolean useJPADerivedIdentifier) {
        this.useJPADerivedIdentifier = useJPADerivedIdentifier;
    }

    public String getJavadoc() {
        return javadoc;
    }

    public void setJavadoc(String javadoc) {
        this.javadoc = javadoc;
    }

    public JoinTableConfig getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(JoinTableConfig joinTable) {
        this.joinTable = joinTable;
    }

    // Computed properties

    public String getRelationshipFieldName() {
        return relationshipFieldName != null ? relationshipFieldName : relationshipName;
    }

    public void setRelationshipFieldName(String relationshipFieldName) {
        this.relationshipFieldName = relationshipFieldName;
    }

    public String getRelationshipFieldNamePlural() {
        if (relationshipFieldNamePlural != null) {
            return relationshipFieldNamePlural;
        }
        return pluralize(getRelationshipFieldName());
    }

    public void setRelationshipFieldNamePlural(String relationshipFieldNamePlural) {
        this.relationshipFieldNamePlural = relationshipFieldNamePlural;
    }

    public String getRelationshipNameCapitalized() {
        if (relationshipNameCapitalized != null) {
            return relationshipNameCapitalized;
        }
        return capitalize(relationshipName);
    }

    public void setRelationshipNameCapitalized(String relationshipNameCapitalized) {
        this.relationshipNameCapitalized = relationshipNameCapitalized;
    }

    public String getRelationshipNameCapitalizedPlural() {
        if (relationshipNameCapitalizedPlural != null) {
            return relationshipNameCapitalizedPlural;
        }
        return pluralize(getRelationshipNameCapitalized());
    }

    public void setRelationshipNameCapitalizedPlural(String relationshipNameCapitalizedPlural) {
        this.relationshipNameCapitalizedPlural = relationshipNameCapitalizedPlural;
    }

    public String getOtherEntityNamePlural() {
        if (otherEntityNamePlural != null) {
            return otherEntityNamePlural;
        }
        return pluralize(otherEntityName);
    }

    public void setOtherEntityNamePlural(String otherEntityNamePlural) {
        this.otherEntityNamePlural = otherEntityNamePlural;
    }

    public String getOtherEntityNameCapitalized() {
        if (otherEntityNameCapitalized != null) {
            return otherEntityNameCapitalized;
        }
        return capitalize(otherEntityName);
    }

    public void setOtherEntityNameCapitalized(String otherEntityNameCapitalized) {
        this.otherEntityNameCapitalized = otherEntityNameCapitalized;
    }

    public String getColumnName() {
        if (columnName != null) {
            return columnName;
        }
        return toSnakeCase(relationshipName) + "_id";
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public EntityConfig getOtherEntity() {
        return otherEntity;
    }

    public void setOtherEntity(EntityConfig otherEntity) {
        this.otherEntity = otherEntity;
    }

    public RelationshipConfig getOtherRelationship() {
        return otherRelationship;
    }

    public void setOtherRelationship(RelationshipConfig otherRelationship) {
        this.otherRelationship = otherRelationship;
    }

    // Helper methods

    public boolean isOneToOne() {
        return "one-to-one".equals(relationshipType);
    }

    public boolean isOneToMany() {
        return "one-to-many".equals(relationshipType);
    }

    public boolean isManyToOne() {
        return "many-to-one".equals(relationshipType);
    }

    public boolean isManyToMany() {
        return "many-to-many".equals(relationshipType);
    }

    public boolean isCollection() {
        return isOneToMany() || isManyToMany();
    }

    public boolean isOwnerSide() {
        if (ownerSide != null) {
            return ownerSide;
        }
        // By default, ManyToOne is owner side
        // OneToMany is not owner side
        // ManyToMany owner is determined by configuration
        // OneToOne owner is determined by configuration
        return isManyToOne();
    }

    public boolean isRequired() {
        return Boolean.TRUE.equals(relationshipRequired);
    }

    public boolean hasOtherEntityRelationship() {
        return otherEntityRelationshipName != null && !otherEntityRelationshipName.isEmpty();
    }

    public String getPropertyName() {
        return isCollection() ? getRelationshipFieldNamePlural() : relationshipName;
    }

    public String getPropertyJavaBeanName() {
        return capitalize(getPropertyName());
    }

    // Utility methods

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static String toSnakeCase(String str) {
        if (str == null) return null;
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private static String pluralize(String str) {
        if (str == null || str.isEmpty()) return str;
        if (str.endsWith("s") || str.endsWith("x") || str.endsWith("z") ||
            str.endsWith("ch") || str.endsWith("sh")) {
            return str + "es";
        }
        if (str.endsWith("y") && str.length() > 1 &&
            !isVowel(str.charAt(str.length() - 2))) {
            return str.substring(0, str.length() - 1) + "ies";
        }
        return str + "s";
    }

    private static boolean isVowel(char c) {
        return "aeiouAEIOU".indexOf(c) >= 0;
    }

    /**
     * Join table configuration for ManyToMany relationships.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JoinTableConfig {

        @JsonProperty("name")
        private String name;

        @JsonProperty("joinColumn")
        private JoinColumnConfig joinColumn;

        @JsonProperty("inverseJoinColumn")
        private JoinColumnConfig inverseJoinColumn;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public JoinColumnConfig getJoinColumn() {
            return joinColumn;
        }

        public void setJoinColumn(JoinColumnConfig joinColumn) {
            this.joinColumn = joinColumn;
        }

        public JoinColumnConfig getInverseJoinColumn() {
            return inverseJoinColumn;
        }

        public void setInverseJoinColumn(JoinColumnConfig inverseJoinColumn) {
            this.inverseJoinColumn = inverseJoinColumn;
        }
    }

    /**
     * Join column configuration.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JoinColumnConfig {

        @JsonProperty("name")
        private String name;

        @JsonProperty("referencedColumnName")
        private String referencedColumnName;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReferencedColumnName() {
            return referencedColumnName;
        }

        public void setReferencedColumnName(String referencedColumnName) {
            this.referencedColumnName = referencedColumnName;
        }
    }
}
