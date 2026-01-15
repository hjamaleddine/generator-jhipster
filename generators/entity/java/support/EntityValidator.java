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
package io.github.jhipster.generator.entity.support;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for validating entity names, field names, and other inputs.
 */
public final class EntityValidator {

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]*$");
    private static final Pattern ALPHANUMERIC_UNDERSCORE_PATTERN = Pattern.compile("^[a-zA-Z0-9_]*$");
    private static final Pattern STARTS_WITH_NUMBER_PATTERN = Pattern.compile("^[0-9].*$");
    private static final Pattern UPPERCASE_START_PATTERN = Pattern.compile("^[A-Z].*$");

    // Java reserved keywords
    private static final Set<String> JAVA_RESERVED_KEYWORDS = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "false", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long", "native",
        "new", "null", "package", "private", "protected", "public", "return",
        "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "true", "try", "void", "volatile", "while",
        // Java literals
        "true", "false", "null",
        // Common problematic names
        "Object", "Class", "System", "String", "Integer", "Long", "Boolean",
        "Float", "Double", "Byte", "Short", "Character", "Number", "Math",
        "Runtime", "Thread", "Throwable", "Exception", "Error"
    );

    // JHipster reserved class names
    private static final Set<String> JHIPSTER_RESERVED_KEYWORDS = Set.of(
        "Account", "UserDetails", "Authority", "AbstractAuditingEntity",
        "PersistentToken", "PersistentAuditEvent"
    );

    // Spring pagination reserved words
    private static final Set<String> PAGINATION_RESERVED_WORDS = Set.of(
        "page", "size", "sort"
    );

    // Angular reserved field names
    private static final Set<String> ANGULAR_RESERVED_FIELD_NAMES = Set.of(
        "ngOnInit", "ngOnDestroy", "ngAfterViewInit", "ngOnChanges",
        "constructor", "prototype", "length", "name"
    );

    // React reserved field names
    private static final Set<String> REACT_RESERVED_FIELD_NAMES = Set.of(
        "componentDidMount", "componentWillUnmount", "render", "state", "props",
        "constructor", "prototype", "length", "name"
    );

    private EntityValidator() {
        // Utility class
    }

    /**
     * Validates an entity name.
     *
     * @param entityName the entity name to validate
     * @param skipServer whether server generation is skipped (if true, Java keywords are not checked)
     * @return empty Optional if valid, or Optional containing error message
     */
    public static Optional<String> validateEntityName(String entityName, boolean skipServer) {
        if (entityName == null || entityName.isEmpty()) {
            return Optional.of("The entity name cannot be empty");
        }

        if (!ALPHANUMERIC_PATTERN.matcher(entityName).matches()) {
            return Optional.of("The entity name must be alphanumeric only");
        }

        if (STARTS_WITH_NUMBER_PATTERN.matcher(entityName).matches()) {
            return Optional.of("The entity name cannot start with a number");
        }

        if (entityName.endsWith("Detail")) {
            return Optional.of("The entity name cannot end with 'Detail'");
        }

        if (!skipServer && isReservedClassName(entityName)) {
            return Optional.of("The entity name cannot contain a Java or JHipster reserved keyword");
        }

        return Optional.empty();
    }

    /**
     * Validates a field name.
     *
     * @param fieldName the field name to validate
     * @param existingFields existing field names in the entity (including 'id')
     * @param clientFramework the client framework being used (angular, react, etc.)
     * @param possibleFiltering whether JPA filtering might be used
     * @return empty Optional if valid, or Optional containing error message
     */
    public static Optional<String> validateFieldName(String fieldName, Set<String> existingFields,
                                                     String clientFramework, boolean possibleFiltering) {
        if (fieldName == null || fieldName.isEmpty()) {
            return Optional.of("Your field name cannot be empty");
        }

        if (!ALPHANUMERIC_UNDERSCORE_PATTERN.matcher(fieldName).matches()) {
            return Optional.of("Your field name cannot contain special characters");
        }

        if (UPPERCASE_START_PATTERN.matcher(fieldName).matches()) {
            return Optional.of("Your field name cannot start with an upper case letter");
        }

        String fieldNameUnderscored = toSnakeCase(fieldName);
        if ("id".equals(fieldName) || existingFields.contains(fieldNameUnderscored)) {
            return Optional.of("Your field name cannot use an already existing field name");
        }

        if ("angular".equalsIgnoreCase(clientFramework) && isReservedFieldName(fieldName, ANGULAR_RESERVED_FIELD_NAMES)) {
            return Optional.of("Your field name cannot contain a Java or Angular reserved keyword");
        }

        if ("react".equalsIgnoreCase(clientFramework) && isReservedFieldName(fieldName, REACT_RESERVED_FIELD_NAMES)) {
            return Optional.of("Your field name cannot contain a Java or React reserved keyword");
        }

        if (possibleFiltering && isPaginationReservedWord(fieldName)) {
            return Optional.of("Your field name cannot be a value used as a parameter by Spring for pagination");
        }

        if (JAVA_RESERVED_KEYWORDS.contains(fieldName.toLowerCase())) {
            return Optional.of("Your field name cannot be a Java reserved keyword");
        }

        return Optional.empty();
    }

    /**
     * Validates a relationship name.
     *
     * @param relationshipName the relationship name to validate
     * @param existingFields existing field names
     * @return empty Optional if valid, or Optional containing error message
     */
    public static Optional<String> validateRelationshipName(String relationshipName, Set<String> existingFields) {
        if (relationshipName == null || relationshipName.isEmpty()) {
            return Optional.of("Your relationship name cannot be empty");
        }

        if (!ALPHANUMERIC_UNDERSCORE_PATTERN.matcher(relationshipName).matches()) {
            return Optional.of("Your relationship cannot contain special characters");
        }

        if (UPPERCASE_START_PATTERN.matcher(relationshipName).matches()) {
            return Optional.of("Your relationship cannot start with an upper case letter");
        }

        String nameUnderscored = toSnakeCase(relationshipName);
        if ("id".equals(relationshipName) || existingFields.contains(nameUnderscored)) {
            return Optional.of("Your relationship cannot use an already existing field name");
        }

        if (isReservedTableName(relationshipName)) {
            return Optional.of("Your relationship cannot contain a Java reserved keyword");
        }

        return Optional.empty();
    }

    /**
     * Validates an enum class name.
     *
     * @param enumName the enum name to validate
     * @return empty Optional if valid, or Optional containing error message
     */
    public static Optional<String> validateEnumName(String enumName) {
        if (enumName == null || enumName.isEmpty()) {
            return Optional.of("Your class name cannot be empty");
        }

        if (!ALPHANUMERIC_UNDERSCORE_PATTERN.matcher(enumName).matches()) {
            return Optional.of("Your enum name cannot contain special characters (allowed characters: A-Z, a-z, 0-9 and _)");
        }

        if (isReservedTableName(enumName)) {
            return Optional.of("Your enum name cannot contain a Java reserved keyword");
        }

        return Optional.empty();
    }

    /**
     * Validates enum values.
     *
     * @param enumValues comma-separated enum values
     * @return empty Optional if valid, or Optional containing error message
     */
    public static Optional<String> validateEnumValues(String enumValues) {
        if (enumValues == null || enumValues.isEmpty()) {
            return Optional.of("You must specify values for your enumeration");
        }

        if (!enumValues.matches("^[A-Za-z0-9_,]+$")) {
            return Optional.of("Enum values cannot contain special characters (allowed characters: A-Z, a-z, 0-9 and _)");
        }

        String[] values = enumValues.replaceAll("\\s", "").split(",");
        Set<String> uniqueValues = new java.util.HashSet<>();

        for (String value : values) {
            if (value.isEmpty()) {
                return Optional.of("Enum value cannot be empty (did you accidentally type \",\" twice in a row?)");
            }
            if (STARTS_WITH_NUMBER_PATTERN.matcher(value).matches()) {
                return Optional.of("Enum value \"" + value + "\" cannot start with a number");
            }
            if (!uniqueValues.add(value)) {
                return Optional.of("Enum values cannot contain duplicates (typed values: " + enumValues + ")");
            }
        }

        return Optional.empty();
    }

    /**
     * Checks if a number input is valid.
     */
    public static boolean isValidNumber(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            int value = Integer.parseInt(input);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a signed number input is valid.
     */
    public static boolean isValidSignedNumber(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a decimal number input is valid.
     */
    public static boolean isValidSignedDecimalNumber(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a class name is reserved.
     */
    public static boolean isReservedClassName(String name) {
        if (name == null) {
            return false;
        }
        String upperName = name.substring(0, 1).toUpperCase() + name.substring(1);
        return JAVA_RESERVED_KEYWORDS.contains(name.toLowerCase()) ||
               JHIPSTER_RESERVED_KEYWORDS.contains(upperName);
    }

    /**
     * Checks if a table name is reserved.
     */
    public static boolean isReservedTableName(String name) {
        if (name == null) {
            return false;
        }
        return JAVA_RESERVED_KEYWORDS.contains(name.toLowerCase());
    }

    /**
     * Checks if a field name is reserved for a specific framework.
     */
    public static boolean isReservedFieldName(String name, Set<String> frameworkReserved) {
        if (name == null) {
            return false;
        }
        return JAVA_RESERVED_KEYWORDS.contains(name.toLowerCase()) ||
               frameworkReserved.contains(name);
    }

    /**
     * Checks if a name is a pagination reserved word.
     */
    public static boolean isPaginationReservedWord(String name) {
        if (name == null) {
            return false;
        }
        return PAGINATION_RESERVED_WORDS.contains(name.toLowerCase());
    }

    /**
     * Converts a camelCase string to snake_case.
     */
    public static String toSnakeCase(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Converts a string to hibernate snake case format.
     */
    public static String hibernateSnakeCase(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2")
                  .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                  .toLowerCase();
    }
}
