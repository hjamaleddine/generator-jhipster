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
package io.github.jhipster.generator.domain.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Information about an enumeration field for code generation.
 */
public class EnumInfo {

    private String enumName;
    private String enumInstance;
    private List<EnumValue> enumValues = new ArrayList<>();
    private boolean enumWithCustomValues;
    private String packageName;
    private String entityAbsolutePackage;
    private String entityJavaPackageFolder;
    private String frontendAppName;
    private String clientRootFolder;

    public EnumInfo() {
    }

    public EnumInfo(String enumName) {
        this.enumName = enumName;
        this.enumInstance = JavaBeanUtils.toCamelCase(enumName);
    }

    /**
     * Creates EnumInfo from a field configuration.
     *
     * @param fieldType      the field type (enum name)
     * @param fieldValues    the comma-separated enum values
     * @param clientRootFolder the client root folder
     * @return the EnumInfo
     */
    public static EnumInfo fromField(String fieldType, String fieldValues, String clientRootFolder) {
        EnumInfo info = new EnumInfo(fieldType);
        info.setClientRootFolder(clientRootFolder);

        if (fieldValues != null && !fieldValues.isEmpty()) {
            info.parseEnumValues(fieldValues);
        }

        return info;
    }

    /**
     * Parses enum values from a comma-separated string.
     * Supports format: VALUE or VALUE(customValue) or VALUE (comment)
     *
     * @param fieldValues comma-separated values
     */
    public void parseEnumValues(String fieldValues) {
        enumValues.clear();
        enumWithCustomValues = false;

        String[] values = fieldValues.split(",");
        for (String value : values) {
            value = value.trim();
            if (value.isEmpty()) {
                continue;
            }

            EnumValue enumValue = new EnumValue();

            // Check for custom value format: VALUE(customValue)
            int parenStart = value.indexOf('(');
            int parenEnd = value.indexOf(')');

            if (parenStart > 0 && parenEnd > parenStart) {
                enumValue.setName(value.substring(0, parenStart).trim());
                enumValue.setCustomValue(value.substring(parenStart + 1, parenEnd).trim());
                enumWithCustomValues = true;
            } else {
                enumValue.setName(value);
            }

            enumValues.add(enumValue);
        }
    }

    // Getters and Setters
    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
        this.enumInstance = JavaBeanUtils.toCamelCase(enumName);
    }

    public String getEnumInstance() {
        return enumInstance;
    }

    public void setEnumInstance(String enumInstance) {
        this.enumInstance = enumInstance;
    }

    public List<EnumValue> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<EnumValue> enumValues) {
        this.enumValues = enumValues;
    }

    public boolean isEnumWithCustomValues() {
        return enumWithCustomValues;
    }

    public void setEnumWithCustomValues(boolean enumWithCustomValues) {
        this.enumWithCustomValues = enumWithCustomValues;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    /**
     * Gets the full package for the enum.
     *
     * @return the enum package
     */
    public String getEnumPackage() {
        String basePackage = entityAbsolutePackage != null ? entityAbsolutePackage : packageName;
        return basePackage + ".domain.enumeration";
    }

    /**
     * Gets the full class name for the enum.
     *
     * @return the enum class name
     */
    public String getEnumClassName() {
        return getEnumPackage() + "." + enumName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumInfo enumInfo = (EnumInfo) o;
        return Objects.equals(enumName, enumInfo.enumName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enumName);
    }

    @Override
    public String toString() {
        return "EnumInfo{" +
               "enumName='" + enumName + '\'' +
               ", enumValues=" + enumValues.size() +
               ", enumWithCustomValues=" + enumWithCustomValues +
               '}';
    }

    /**
     * Represents a single enum value with optional custom value.
     */
    public static class EnumValue {
        private String name;
        private String customValue;
        private String comment;

        public EnumValue() {
        }

        public EnumValue(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCustomValue() {
            return customValue;
        }

        public void setCustomValue(String customValue) {
            this.customValue = customValue;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public boolean hasCustomValue() {
            return customValue != null && !customValue.isEmpty();
        }

        @Override
        public String toString() {
            if (hasCustomValue()) {
                return name + "(" + customValue + ")";
            }
            return name;
        }
    }
}
