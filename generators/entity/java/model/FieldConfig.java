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
import io.github.jhipster.generator.entity.enums.BlobType;
import io.github.jhipster.generator.entity.enums.FieldType;
import io.github.jhipster.generator.entity.enums.ValidationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for an entity field.
 * Represents the field definition as stored in the .jhipster/*.json configuration files.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldConfig {

    private String fieldName;
    private String fieldType;
    private String fieldTypeBlobContent;
    private String fieldValues;
    private boolean fieldIsEnum;
    private List<String> fieldValidateRules = new ArrayList<>();
    private Integer fieldValidateRulesMinlength;
    private Integer fieldValidateRulesMaxlength;
    private String fieldValidateRulesPattern;
    private Number fieldValidateRulesMin;
    private Number fieldValidateRulesMax;
    private Integer fieldValidateRulesMinbytes;
    private Integer fieldValidateRulesMaxbytes;

    // Constructors
    public FieldConfig() {
    }

    public FieldConfig(String fieldName, String fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    // Builder pattern methods
    public static FieldConfigBuilder builder() {
        return new FieldConfigBuilder();
    }

    // Getters and Setters
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldTypeBlobContent() {
        return fieldTypeBlobContent;
    }

    public void setFieldTypeBlobContent(String fieldTypeBlobContent) {
        this.fieldTypeBlobContent = fieldTypeBlobContent;
    }

    public String getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(String fieldValues) {
        this.fieldValues = fieldValues;
    }

    public boolean isFieldIsEnum() {
        return fieldIsEnum;
    }

    public void setFieldIsEnum(boolean fieldIsEnum) {
        this.fieldIsEnum = fieldIsEnum;
    }

    public List<String> getFieldValidateRules() {
        return fieldValidateRules;
    }

    public void setFieldValidateRules(List<String> fieldValidateRules) {
        this.fieldValidateRules = fieldValidateRules != null ? fieldValidateRules : new ArrayList<>();
    }

    public Integer getFieldValidateRulesMinlength() {
        return fieldValidateRulesMinlength;
    }

    public void setFieldValidateRulesMinlength(Integer fieldValidateRulesMinlength) {
        this.fieldValidateRulesMinlength = fieldValidateRulesMinlength;
    }

    public Integer getFieldValidateRulesMaxlength() {
        return fieldValidateRulesMaxlength;
    }

    public void setFieldValidateRulesMaxlength(Integer fieldValidateRulesMaxlength) {
        this.fieldValidateRulesMaxlength = fieldValidateRulesMaxlength;
    }

    public String getFieldValidateRulesPattern() {
        return fieldValidateRulesPattern;
    }

    public void setFieldValidateRulesPattern(String fieldValidateRulesPattern) {
        this.fieldValidateRulesPattern = fieldValidateRulesPattern;
    }

    public Number getFieldValidateRulesMin() {
        return fieldValidateRulesMin;
    }

    public void setFieldValidateRulesMin(Number fieldValidateRulesMin) {
        this.fieldValidateRulesMin = fieldValidateRulesMin;
    }

    public Number getFieldValidateRulesMax() {
        return fieldValidateRulesMax;
    }

    public void setFieldValidateRulesMax(Number fieldValidateRulesMax) {
        this.fieldValidateRulesMax = fieldValidateRulesMax;
    }

    public Integer getFieldValidateRulesMinbytes() {
        return fieldValidateRulesMinbytes;
    }

    public void setFieldValidateRulesMinbytes(Integer fieldValidateRulesMinbytes) {
        this.fieldValidateRulesMinbytes = fieldValidateRulesMinbytes;
    }

    public Integer getFieldValidateRulesMaxbytes() {
        return fieldValidateRulesMaxbytes;
    }

    public void setFieldValidateRulesMaxbytes(Integer fieldValidateRulesMaxbytes) {
        this.fieldValidateRulesMaxbytes = fieldValidateRulesMaxbytes;
    }

    // Utility methods
    public boolean hasValidation() {
        return fieldValidateRules != null && !fieldValidateRules.isEmpty();
    }

    public boolean hasValidationRule(ValidationRule rule) {
        return fieldValidateRules != null && fieldValidateRules.contains(rule.getValue());
    }

    public boolean isRequired() {
        return hasValidationRule(ValidationRule.REQUIRED);
    }

    public boolean isUnique() {
        return hasValidationRule(ValidationRule.UNIQUE);
    }

    public FieldType getFieldTypeEnum() {
        try {
            return FieldType.fromString(fieldType);
        } catch (IllegalArgumentException e) {
            // Could be an enum type
            return FieldType.ENUM;
        }
    }

    public BlobType getBlobTypeEnum() {
        if (fieldTypeBlobContent == null) {
            return null;
        }
        return BlobType.fromString(fieldTypeBlobContent);
    }

    public boolean isBlob() {
        FieldType type = getFieldTypeEnum();
        return type == FieldType.BYTES || type == FieldType.BYTE_BUFFER;
    }

    public String getFieldNameCapitalized() {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public String getFieldNameUnderscored() {
        if (fieldName == null) {
            return null;
        }
        return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldConfig that = (FieldConfig) o;
        return Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fieldName).append(" (").append(fieldType);
        if (fieldTypeBlobContent != null) {
            sb.append(" ").append(fieldTypeBlobContent);
        }
        sb.append(")");
        if (hasValidation()) {
            sb.append(" [").append(String.join(", ", fieldValidateRules)).append("]");
        }
        return sb.toString();
    }

    // Builder class
    public static class FieldConfigBuilder {
        private final FieldConfig config = new FieldConfig();

        public FieldConfigBuilder fieldName(String fieldName) {
            config.setFieldName(fieldName);
            return this;
        }

        public FieldConfigBuilder fieldType(String fieldType) {
            config.setFieldType(fieldType);
            return this;
        }

        public FieldConfigBuilder fieldType(FieldType fieldType) {
            config.setFieldType(fieldType.name());
            return this;
        }

        public FieldConfigBuilder blobContent(BlobType blobType) {
            config.setFieldTypeBlobContent(blobType.getValue());
            return this;
        }

        public FieldConfigBuilder enumValues(String values) {
            config.setFieldValues(values);
            config.setFieldIsEnum(true);
            return this;
        }

        public FieldConfigBuilder required() {
            config.getFieldValidateRules().add(ValidationRule.REQUIRED.getValue());
            return this;
        }

        public FieldConfigBuilder unique() {
            config.getFieldValidateRules().add(ValidationRule.UNIQUE.getValue());
            return this;
        }

        public FieldConfigBuilder minLength(int min) {
            config.getFieldValidateRules().add(ValidationRule.MINLENGTH.getValue());
            config.setFieldValidateRulesMinlength(min);
            return this;
        }

        public FieldConfigBuilder maxLength(int max) {
            config.getFieldValidateRules().add(ValidationRule.MAXLENGTH.getValue());
            config.setFieldValidateRulesMaxlength(max);
            return this;
        }

        public FieldConfigBuilder min(Number min) {
            config.getFieldValidateRules().add(ValidationRule.MIN.getValue());
            config.setFieldValidateRulesMin(min);
            return this;
        }

        public FieldConfigBuilder max(Number max) {
            config.getFieldValidateRules().add(ValidationRule.MAX.getValue());
            config.setFieldValidateRulesMax(max);
            return this;
        }

        public FieldConfigBuilder pattern(String pattern) {
            config.getFieldValidateRules().add(ValidationRule.PATTERN.getValue());
            config.setFieldValidateRulesPattern(pattern);
            return this;
        }

        public FieldConfig build() {
            return config;
        }
    }
}
