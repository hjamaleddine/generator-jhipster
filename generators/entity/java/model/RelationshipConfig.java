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
import io.github.jhipster.generator.entity.enums.RelationshipType;
import io.github.jhipster.generator.entity.enums.ValidationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for an entity relationship.
 * Represents the relationship definition as stored in the .jhipster/*.json configuration files.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelationshipConfig {

    private String relationshipSide = "left";
    private String relationshipName;
    private String otherEntityName;
    private String relationshipType;
    private List<String> relationshipValidateRules = new ArrayList<>();
    private String otherEntityField;
    private Boolean ownerSide;
    private Boolean id;  // For @MapsId
    private String otherEntityRelationshipName;
    private Boolean bidirectional;

    // Constructors
    public RelationshipConfig() {
    }

    public RelationshipConfig(String relationshipName, String otherEntityName, String relationshipType) {
        this.relationshipName = relationshipName;
        this.otherEntityName = otherEntityName;
        this.relationshipType = relationshipType;
    }

    // Builder pattern
    public static RelationshipConfigBuilder builder() {
        return new RelationshipConfigBuilder();
    }

    // Getters and Setters
    public String getRelationshipSide() {
        return relationshipSide;
    }

    public void setRelationshipSide(String relationshipSide) {
        this.relationshipSide = relationshipSide;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    public String getOtherEntityName() {
        return otherEntityName;
    }

    public void setOtherEntityName(String otherEntityName) {
        this.otherEntityName = otherEntityName;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public List<String> getRelationshipValidateRules() {
        return relationshipValidateRules;
    }

    public void setRelationshipValidateRules(List<String> relationshipValidateRules) {
        this.relationshipValidateRules = relationshipValidateRules != null ? relationshipValidateRules : new ArrayList<>();
    }

    public String getOtherEntityField() {
        return otherEntityField;
    }

    public void setOtherEntityField(String otherEntityField) {
        this.otherEntityField = otherEntityField;
    }

    public Boolean getOwnerSide() {
        return ownerSide;
    }

    public void setOwnerSide(Boolean ownerSide) {
        this.ownerSide = ownerSide;
    }

    public Boolean getId() {
        return id;
    }

    public void setId(Boolean id) {
        this.id = id;
    }

    public String getOtherEntityRelationshipName() {
        return otherEntityRelationshipName;
    }

    public void setOtherEntityRelationshipName(String otherEntityRelationshipName) {
        this.otherEntityRelationshipName = otherEntityRelationshipName;
    }

    public Boolean getBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(Boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    // Utility methods
    public RelationshipType getRelationshipTypeEnum() {
        return RelationshipType.fromString(relationshipType);
    }

    public boolean isRequired() {
        return relationshipValidateRules != null &&
               relationshipValidateRules.contains(ValidationRule.REQUIRED.getValue());
    }

    public boolean isOneToOne() {
        return getRelationshipTypeEnum() == RelationshipType.ONE_TO_ONE;
    }

    public boolean isOneToMany() {
        return getRelationshipTypeEnum() == RelationshipType.ONE_TO_MANY;
    }

    public boolean isManyToOne() {
        return getRelationshipTypeEnum() == RelationshipType.MANY_TO_ONE;
    }

    public boolean isManyToMany() {
        return getRelationshipTypeEnum() == RelationshipType.MANY_TO_MANY;
    }

    public boolean isToMany() {
        return isOneToMany() || isManyToMany();
    }

    public boolean usesJpaDerivedIdentifier() {
        return Boolean.TRUE.equals(id);
    }

    public boolean isBidirectional() {
        return Boolean.TRUE.equals(bidirectional);
    }

    public String getRelationshipNameCapitalized() {
        if (relationshipName == null || relationshipName.isEmpty()) {
            return relationshipName;
        }
        return Character.toUpperCase(relationshipName.charAt(0)) + relationshipName.substring(1);
    }

    public String getOtherEntityNameCapitalized() {
        if (otherEntityName == null || otherEntityName.isEmpty()) {
            return otherEntityName;
        }
        return Character.toUpperCase(otherEntityName.charAt(0)) + otherEntityName.substring(1);
    }

    public boolean isUserRelationship() {
        return "user".equalsIgnoreCase(otherEntityName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipConfig that = (RelationshipConfig) o;
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
        StringBuilder sb = new StringBuilder();
        sb.append(relationshipName)
          .append(" (").append(otherEntityName).append(") ")
          .append(relationshipType);
        if (isRequired()) {
            sb.append(" [required]");
        }
        return sb.toString();
    }

    // Builder class
    public static class RelationshipConfigBuilder {
        private final RelationshipConfig config = new RelationshipConfig();

        public RelationshipConfigBuilder relationshipName(String name) {
            config.setRelationshipName(name);
            return this;
        }

        public RelationshipConfigBuilder otherEntityName(String name) {
            config.setOtherEntityName(name.toLowerCase());
            return this;
        }

        public RelationshipConfigBuilder relationshipType(RelationshipType type) {
            config.setRelationshipType(type.getValue());
            return this;
        }

        public RelationshipConfigBuilder relationshipType(String type) {
            config.setRelationshipType(type);
            return this;
        }

        public RelationshipConfigBuilder otherEntityField(String field) {
            config.setOtherEntityField(field);
            return this;
        }

        public RelationshipConfigBuilder otherEntityRelationshipName(String name) {
            config.setOtherEntityRelationshipName(name);
            return this;
        }

        public RelationshipConfigBuilder ownerSide(boolean owner) {
            config.setOwnerSide(owner);
            return this;
        }

        public RelationshipConfigBuilder bidirectional(boolean bidirectional) {
            config.setBidirectional(bidirectional);
            return this;
        }

        public RelationshipConfigBuilder useMapsId(boolean useMapsId) {
            config.setId(useMapsId);
            return this;
        }

        public RelationshipConfigBuilder required() {
            config.getRelationshipValidateRules().add(ValidationRule.REQUIRED.getValue());
            return this;
        }

        public RelationshipConfigBuilder leftSide() {
            config.setRelationshipSide("left");
            return this;
        }

        public RelationshipConfigBuilder rightSide() {
            config.setRelationshipSide("right");
            return this;
        }

        public RelationshipConfig build() {
            return config;
        }
    }
}
