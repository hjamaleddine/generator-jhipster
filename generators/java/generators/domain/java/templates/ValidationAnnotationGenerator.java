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
package io.github.jhipster.generator.domain.templates;

import io.github.jhipster.generator.domain.model.FieldContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates Jakarta validation annotations for entity fields.
 * This is the Java equivalent of field_validators.ejs template.
 */
public class ValidationAnnotationGenerator {

    private static final int MAX_VALUE = 2147483647;

    private final FieldContext field;
    private final boolean reactive;

    public ValidationAnnotationGenerator(FieldContext field) {
        this(field, false);
    }

    public ValidationAnnotationGenerator(FieldContext field, boolean reactive) {
        this.field = field;
        this.reactive = reactive;
    }

    /**
     * Generates validation annotations for the field.
     *
     * @return list of annotation strings
     */
    public List<String> generate() {
        List<String> validators = new ArrayList<>();

        if (!field.hasValidation()) {
            return validators;
        }

        boolean isBlob = field.isFieldTypeBlob();

        // Required validation (NotNull)
        if (field.isFieldValidationRequired() && !isBlob) {
            if (reactive) {
                validators.add("@NotNull(message = \"must not be null\")");
            } else {
                validators.add("@NotNull");
            }
        }

        // Size validation (minlength/maxlength)
        Integer minLength = field.getFieldValidateRulesMinlength();
        Integer maxLength = field.getFieldValidateRulesMaxlength();

        if (minLength != null && maxLength == null) {
            validators.add("@Size(min = " + minLength + ")");
        } else if (maxLength != null && minLength == null) {
            validators.add("@Size(max = " + maxLength + ")");
        } else if (minLength != null && maxLength != null) {
            validators.add("@Size(min = " + minLength + ", max = " + maxLength + ")");
        }

        // Min validation
        Number min = field.getFieldValidateRulesMin();
        if (min != null) {
            if (isDecimalType()) {
                validators.add("@DecimalMin(value = \"" + min + "\")");
            } else {
                String suffix = needsLongSuffix(min) ? "L" : "";
                validators.add("@Min(value = " + min + suffix + ")");
            }
        }

        // Max validation
        Number max = field.getFieldValidateRulesMax();
        if (max != null) {
            if (isDecimalType()) {
                validators.add("@DecimalMax(value = \"" + max + "\")");
            } else {
                String suffix = needsLongSuffix(max) ? "L" : "";
                validators.add("@Max(value = " + max + suffix + ")");
            }
        }

        // Pattern validation
        String pattern = field.getFieldValidateRulesPattern();
        if (pattern != null && !pattern.isEmpty()) {
            // Escape backslashes for Java string
            String javaPattern = escapePatternForJava(pattern);
            validators.add("@Pattern(regexp = \"" + javaPattern + "\")");
        }

        return validators;
    }

    /**
     * Generates validation annotations as a formatted string.
     *
     * @return formatted string of annotations
     */
    public String generateAsString() {
        List<String> validators = generate();
        if (validators.isEmpty()) {
            return "";
        }
        return String.join("\n    ", validators) + "\n";
    }

    /**
     * Generates validation annotations with proper indentation.
     *
     * @param indent the indentation string
     * @return formatted string of annotations
     */
    public String generateAsString(String indent) {
        List<String> validators = generate();
        if (validators.isEmpty()) {
            return "";
        }
        return indent + String.join("\n" + indent, validators) + "\n";
    }

    /**
     * Gets the list of required Jakarta validation imports for this field.
     *
     * @return list of import statements
     */
    public List<String> getRequiredImports() {
        List<String> imports = new ArrayList<>();

        if (!field.hasValidation()) {
            return imports;
        }

        boolean isBlob = field.isFieldTypeBlob();

        if (field.isFieldValidationRequired() && !isBlob) {
            imports.add("jakarta.validation.constraints.NotNull");
        }

        Integer minLength = field.getFieldValidateRulesMinlength();
        Integer maxLength = field.getFieldValidateRulesMaxlength();
        if (minLength != null || maxLength != null) {
            imports.add("jakarta.validation.constraints.Size");
        }

        Number min = field.getFieldValidateRulesMin();
        if (min != null) {
            if (isDecimalType()) {
                imports.add("jakarta.validation.constraints.DecimalMin");
            } else {
                imports.add("jakarta.validation.constraints.Min");
            }
        }

        Number max = field.getFieldValidateRulesMax();
        if (max != null) {
            if (isDecimalType()) {
                imports.add("jakarta.validation.constraints.DecimalMax");
            } else {
                imports.add("jakarta.validation.constraints.Max");
            }
        }

        String pattern = field.getFieldValidateRulesPattern();
        if (pattern != null && !pattern.isEmpty()) {
            imports.add("jakarta.validation.constraints.Pattern");
        }

        return imports;
    }

    // Helper methods

    private boolean isDecimalType() {
        String type = field.getJavaFieldType();
        return "Float".equals(type) || "Double".equals(type) || "BigDecimal".equals(type);
    }

    private boolean needsLongSuffix(Number value) {
        if ("Long".equals(field.getJavaFieldType())) {
            return true;
        }
        if (value instanceof Long) {
            return value.longValue() > MAX_VALUE;
        }
        if (value instanceof Integer) {
            return value.intValue() > MAX_VALUE;
        }
        return false;
    }

    private String escapePatternForJava(String pattern) {
        // Double-escape backslashes for Java string literals
        return pattern.replace("\\", "\\\\");
    }
}
