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
 * Field context for domain layer generation.
 * Contains all field information needed for template rendering.
 */
public class FieldContext {

    // Field identification
    private String fieldName;
    private String fieldNameCapitalized;
    private String fieldType;
    private String javaFieldType;
    private String propertyName;
    private String propertyJavaBeanName;

    // Field configuration
    private boolean id;
    private boolean transientField;  // 'transient' is a reserved keyword
    private boolean javaInherited;
    private boolean fieldIsEnum;
    private String fieldValues;  // For enums
    private String fieldTypeBlobContent;  // For blobs: image, any, text

    // Content type for blobs
    private boolean fieldWithContentType;

    // Validation rules
    private List<String> fieldValidateRules = new ArrayList<>();
    private Integer fieldValidateRulesMinlength;
    private Integer fieldValidateRulesMaxlength;
    private String fieldValidateRulesPattern;
    private Number fieldValidateRulesMin;
    private Number fieldValidateRulesMax;
    private Integer fieldValidateRulesMinbytes;
    private Integer fieldValidateRulesMaxbytes;
    private boolean fieldValidationRequired;

    // Documentation
    private String fieldJavadoc;
    private String fieldApiDescription;

    // DTO mapping
    private String propertyDtoJavaType;

    // Getter/setter method naming
    private String fieldInJavaBeanMethod;

    // Type flags
    private boolean fieldTypeNumeric;
    private boolean fieldTypeTemporal;
    private boolean fieldTypeBlob;

    public FieldContext() {
    }

    public FieldContext(String fieldName, String fieldType) {
        setFieldName(fieldName);
        setFieldType(fieldType);
    }

    /**
     * Prepares computed properties after field configuration.
     */
    public void prepare() {
        // Set property name variants
        if (propertyName == null) {
            propertyName = fieldName;
        }
        propertyJavaBeanName = JavaBeanUtils.javaBeanCase(propertyName);
        fieldInJavaBeanMethod = JavaBeanUtils.javaBeanCase(fieldName);

        // Determine Java type
        if (javaFieldType == null) {
            javaFieldType = determineJavaFieldType();
        }

        // Set type flags
        fieldTypeNumeric = isNumericType(javaFieldType);
        fieldTypeTemporal = isTemporalType(javaFieldType);
        fieldTypeBlob = "byte[]".equals(javaFieldType) || "ByteBuffer".equals(javaFieldType);

        // Check for content type field
        fieldWithContentType = fieldTypeBlob && !"text".equals(fieldTypeBlobContent);

        // Validation required flag
        fieldValidationRequired = fieldValidateRules.contains("required");
    }

    private String determineJavaFieldType() {
        if (fieldIsEnum) {
            return fieldType;  // Enum name
        }

        switch (fieldType.toUpperCase()) {
            case "STRING":
                return "text".equals(fieldTypeBlobContent) ? "String" : "String";
            case "INTEGER":
                return "Integer";
            case "LONG":
                return "Long";
            case "FLOAT":
                return "Float";
            case "DOUBLE":
                return "Double";
            case "BIGDECIMAL":
            case "BIG_DECIMAL":
                return "BigDecimal";
            case "LOCALDATE":
            case "LOCAL_DATE":
                return "LocalDate";
            case "INSTANT":
                return "Instant";
            case "ZONEDDATETIME":
            case "ZONED_DATE_TIME":
                return "ZonedDateTime";
            case "LOCALTIME":
            case "LOCAL_TIME":
                return "LocalTime";
            case "DURATION":
                return "Duration";
            case "BOOLEAN":
                return "Boolean";
            case "UUID":
                return "UUID";
            case "BYTES":
                return "byte[]";
            case "BYTEBUFFER":
            case "BYTE_BUFFER":
                return "ByteBuffer";
            default:
                return fieldType;
        }
    }

    private boolean isNumericType(String type) {
        return "Integer".equals(type) || "Long".equals(type) ||
               "Float".equals(type) || "Double".equals(type) ||
               "BigDecimal".equals(type) || "Short".equals(type) ||
               "Byte".equals(type);
    }

    private boolean isTemporalType(String type) {
        return "LocalDate".equals(type) || "Instant".equals(type) ||
               "ZonedDateTime".equals(type) || "LocalTime".equals(type) ||
               "Duration".equals(type);
    }

    // Getters and Setters

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
        this.fieldNameCapitalized = capitalize(fieldName);
    }

    public String getFieldNameCapitalized() {
        return fieldNameCapitalized;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getJavaFieldType() {
        return javaFieldType;
    }

    public void setJavaFieldType(String javaFieldType) {
        this.javaFieldType = javaFieldType;
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

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public boolean isTransientField() {
        return transientField;
    }

    public void setTransientField(boolean transientField) {
        this.transientField = transientField;
    }

    public boolean isJavaInherited() {
        return javaInherited;
    }

    public void setJavaInherited(boolean javaInherited) {
        this.javaInherited = javaInherited;
    }

    public boolean isFieldIsEnum() {
        return fieldIsEnum;
    }

    public void setFieldIsEnum(boolean fieldIsEnum) {
        this.fieldIsEnum = fieldIsEnum;
    }

    public String getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(String fieldValues) {
        this.fieldValues = fieldValues;
    }

    public String getFieldTypeBlobContent() {
        return fieldTypeBlobContent;
    }

    public void setFieldTypeBlobContent(String fieldTypeBlobContent) {
        this.fieldTypeBlobContent = fieldTypeBlobContent;
    }

    public boolean isFieldWithContentType() {
        return fieldWithContentType;
    }

    public void setFieldWithContentType(boolean fieldWithContentType) {
        this.fieldWithContentType = fieldWithContentType;
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

    public boolean isFieldValidationRequired() {
        return fieldValidationRequired;
    }

    public String getFieldJavadoc() {
        return fieldJavadoc;
    }

    public void setFieldJavadoc(String fieldJavadoc) {
        this.fieldJavadoc = fieldJavadoc;
    }

    public String getFieldApiDescription() {
        return fieldApiDescription;
    }

    public void setFieldApiDescription(String fieldApiDescription) {
        this.fieldApiDescription = fieldApiDescription;
    }

    public String getPropertyDtoJavaType() {
        return propertyDtoJavaType;
    }

    public void setPropertyDtoJavaType(String propertyDtoJavaType) {
        this.propertyDtoJavaType = propertyDtoJavaType;
    }

    public String getFieldInJavaBeanMethod() {
        return fieldInJavaBeanMethod;
    }

    public void setFieldInJavaBeanMethod(String fieldInJavaBeanMethod) {
        this.fieldInJavaBeanMethod = fieldInJavaBeanMethod;
    }

    public boolean isFieldTypeNumeric() {
        return fieldTypeNumeric;
    }

    public boolean isFieldTypeTemporal() {
        return fieldTypeTemporal;
    }

    public boolean isFieldTypeBlob() {
        return fieldTypeBlob;
    }

    // Utility methods

    public boolean hasValidation() {
        return fieldValidateRules != null && !fieldValidateRules.isEmpty();
    }

    public boolean hasValidationRule(String rule) {
        return fieldValidateRules != null && fieldValidateRules.contains(rule);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldContext that = (FieldContext) o;
        return Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName);
    }

    @Override
    public String toString() {
        return "FieldContext{" +
               "fieldName='" + fieldName + '\'' +
               ", fieldType='" + fieldType + '\'' +
               ", javaFieldType='" + javaFieldType + '\'' +
               '}';
    }
}
