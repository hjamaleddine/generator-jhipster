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

import java.util.HashMap;
import java.util.Map;

/**
 * Field configuration for an entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldConfig {

    @JsonProperty("fieldName")
    private String fieldName;

    @JsonProperty("fieldType")
    private String fieldType;

    @JsonProperty("fieldTypeBlobContent")
    private String fieldTypeBlobContent;

    @JsonProperty("fieldValidateRules")
    private String[] fieldValidateRules = new String[0];

    @JsonProperty("fieldValidateRulesMin")
    private Integer fieldValidateRulesMin;

    @JsonProperty("fieldValidateRulesMax")
    private Integer fieldValidateRulesMax;

    @JsonProperty("fieldValidateRulesMinlength")
    private Integer fieldValidateRulesMinlength;

    @JsonProperty("fieldValidateRulesMaxlength")
    private Integer fieldValidateRulesMaxlength;

    @JsonProperty("fieldValidateRulesPattern")
    private String fieldValidateRulesPattern;

    @JsonProperty("javadoc")
    private String javadoc;

    @JsonProperty("id")
    private Boolean id = false;

    @JsonProperty("fieldValues")
    private String fieldValues;

    // Computed properties
    private String javaFieldType;
    private String fieldInJavaBeanMethod;
    private String columnName;
    private String columnType;
    private boolean fieldValidationRequired;
    private boolean fieldWithContentType;
    private boolean enumField;
    private String enumFileName;
    private Map<String, String> enumValues = new HashMap<>();

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

    public String[] getFieldValidateRules() {
        return fieldValidateRules;
    }

    public void setFieldValidateRules(String[] fieldValidateRules) {
        this.fieldValidateRules = fieldValidateRules;
    }

    public Integer getFieldValidateRulesMin() {
        return fieldValidateRulesMin;
    }

    public void setFieldValidateRulesMin(Integer fieldValidateRulesMin) {
        this.fieldValidateRulesMin = fieldValidateRulesMin;
    }

    public Integer getFieldValidateRulesMax() {
        return fieldValidateRulesMax;
    }

    public void setFieldValidateRulesMax(Integer fieldValidateRulesMax) {
        this.fieldValidateRulesMax = fieldValidateRulesMax;
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

    public String getJavadoc() {
        return javadoc;
    }

    public void setJavadoc(String javadoc) {
        this.javadoc = javadoc;
    }

    public Boolean getId() {
        return id;
    }

    public void setId(Boolean id) {
        this.id = id;
    }

    public String getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(String fieldValues) {
        this.fieldValues = fieldValues;
    }

    // Computed properties getters/setters

    public String getJavaFieldType() {
        if (javaFieldType != null) {
            return javaFieldType;
        }
        return mapToJavaType(fieldType);
    }

    public void setJavaFieldType(String javaFieldType) {
        this.javaFieldType = javaFieldType;
    }

    public String getFieldInJavaBeanMethod() {
        if (fieldInJavaBeanMethod != null) {
            return fieldInJavaBeanMethod;
        }
        return capitalize(fieldName);
    }

    public void setFieldInJavaBeanMethod(String fieldInJavaBeanMethod) {
        this.fieldInJavaBeanMethod = fieldInJavaBeanMethod;
    }

    public String getColumnName() {
        if (columnName != null) {
            return columnName;
        }
        return toSnakeCase(fieldName);
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public boolean isFieldValidationRequired() {
        return fieldValidationRequired || hasValidationRule("required");
    }

    public void setFieldValidationRequired(boolean fieldValidationRequired) {
        this.fieldValidationRequired = fieldValidationRequired;
    }

    public boolean isFieldWithContentType() {
        return fieldWithContentType || isBlob();
    }

    public void setFieldWithContentType(boolean fieldWithContentType) {
        this.fieldWithContentType = fieldWithContentType;
    }

    public boolean isEnumField() {
        return enumField || (fieldValues != null && !fieldValues.isEmpty());
    }

    public void setEnumField(boolean enumField) {
        this.enumField = enumField;
    }

    public String getEnumFileName() {
        return enumFileName != null ? enumFileName : fieldType;
    }

    public void setEnumFileName(String enumFileName) {
        this.enumFileName = enumFileName;
    }

    public Map<String, String> getEnumValues() {
        if (enumValues.isEmpty() && fieldValues != null) {
            parseEnumValues();
        }
        return enumValues;
    }

    public void setEnumValues(Map<String, String> enumValues) {
        this.enumValues = enumValues;
    }

    // Helper methods

    public boolean hasValidationRule(String rule) {
        if (fieldValidateRules == null) return false;
        for (String r : fieldValidateRules) {
            if (rule.equals(r)) return true;
        }
        return false;
    }

    public boolean hasValidation() {
        return fieldValidateRules != null && fieldValidateRules.length > 0;
    }

    public boolean isString() {
        return "String".equals(fieldType);
    }

    public boolean isInteger() {
        return "Integer".equals(fieldType);
    }

    public boolean isLong() {
        return "Long".equals(fieldType);
    }

    public boolean isFloat() {
        return "Float".equals(fieldType);
    }

    public boolean isDouble() {
        return "Double".equals(fieldType);
    }

    public boolean isBigDecimal() {
        return "BigDecimal".equals(fieldType);
    }

    public boolean isLocalDate() {
        return "LocalDate".equals(fieldType);
    }

    public boolean isInstant() {
        return "Instant".equals(fieldType);
    }

    public boolean isZonedDateTime() {
        return "ZonedDateTime".equals(fieldType);
    }

    public boolean isDuration() {
        return "Duration".equals(fieldType);
    }

    public boolean isUUID() {
        return "UUID".equals(fieldType);
    }

    public boolean isBoolean() {
        return "Boolean".equals(fieldType);
    }

    public boolean isBlob() {
        return fieldType != null && (fieldType.endsWith("Blob") || "byte[]".equals(getJavaFieldType()));
    }

    public boolean isTextBlob() {
        return "TextBlob".equals(fieldType);
    }

    public boolean isImageBlob() {
        return "ImageBlob".equals(fieldType) || "image".equals(fieldTypeBlobContent);
    }

    public boolean isAnyBlob() {
        return "AnyBlob".equals(fieldType) || "any".equals(fieldTypeBlobContent);
    }

    public boolean isNumeric() {
        return isInteger() || isLong() || isFloat() || isDouble() || isBigDecimal();
    }

    public boolean isTemporal() {
        return isLocalDate() || isInstant() || isZonedDateTime() || isDuration();
    }

    private String mapToJavaType(String type) {
        if (type == null) return "String";

        switch (type) {
            case "String":
            case "Integer":
            case "Long":
            case "Float":
            case "Double":
            case "Boolean":
                return type;
            case "BigDecimal":
                return "BigDecimal";
            case "LocalDate":
                return "LocalDate";
            case "Instant":
                return "Instant";
            case "ZonedDateTime":
                return "ZonedDateTime";
            case "Duration":
                return "Duration";
            case "UUID":
                return "UUID";
            case "Blob":
            case "AnyBlob":
            case "ImageBlob":
                return "byte[]";
            case "TextBlob":
                return "String";
            default:
                // Assume it's an enum type
                return type;
        }
    }

    private void parseEnumValues() {
        if (fieldValues == null || fieldValues.isEmpty()) return;

        for (String value : fieldValues.split(",")) {
            value = value.trim();
            if (value.contains("(")) {
                String key = value.substring(0, value.indexOf("(")).trim();
                String val = value.substring(value.indexOf("(") + 1, value.indexOf(")")).trim();
                enumValues.put(key, val);
            } else {
                enumValues.put(value, value);
            }
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static String toSnakeCase(String str) {
        if (str == null) return null;
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
