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

import java.util.Set;

/**
 * Utility class for Java Bean naming conventions and reserved keywords.
 */
public final class JavaBeanUtils {

    /**
     * Java reserved keywords (uppercase for case-insensitive comparison).
     */
    private static final Set<String> JAVA_RESERVED_KEYWORDS = Set.of(
        "ABSTRACT", "CONTINUE", "FOR", "NEW", "SWITCH",
        "ASSERT", "DEFAULT", "GOTO", "PACKAGE", "SYNCHRONIZED",
        "BOOLEAN", "DO", "IF", "PRIVATE", "THIS",
        "BREAK", "DOUBLE", "IMPLEMENTS", "PROTECTED", "THROW",
        "BYTE", "ELSE", "IMPORT", "PUBLIC", "THROWS",
        "CASE", "ENUM", "INSTANCEOF", "RETURN", "TRANSIENT",
        "CATCH", "EXTENDS", "INT", "SHORT", "TRY",
        "CHAR", "FINAL", "INTERFACE", "STATIC", "VOID",
        "CLASS", "FINALLY", "LONG", "STRICTFP", "VOLATILE",
        "CONST", "FLOAT", "NATIVE", "SUPER", "WHILE"
    );

    private JavaBeanUtils() {
        // Utility class
    }

    /**
     * Checks if the given keyword is a Java reserved keyword.
     *
     * @param keyword the keyword to check
     * @return true if the keyword is reserved
     */
    public static boolean isReservedJavaKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        return JAVA_RESERVED_KEYWORDS.contains(keyword.toUpperCase());
    }

    /**
     * Convert to Java bean name case.
     *
     * Handle the specific case when the second letter is capitalized.
     * See http://stackoverflow.com/questions/2948083/naming-convention-for-getters-setters-in-java
     *
     * @param beanName the bean name
     * @return the Java bean case name
     */
    public static String javaBeanCase(String beanName) {
        if (beanName == null || beanName.isEmpty()) {
            return beanName;
        }

        // If second letter is uppercase, return as-is
        if (beanName.length() > 1) {
            char secondLetter = beanName.charAt(1);
            if (Character.isUpperCase(secondLetter)) {
                return beanName;
            }
        }

        // Otherwise, capitalize first letter
        return Character.toUpperCase(beanName.charAt(0)) + beanName.substring(1);
    }

    /**
     * Converts a string to camelCase.
     *
     * @param str the input string
     * @return camelCase version
     */
    public static String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Converts a string to PascalCase (UpperCamelCase).
     *
     * @param str the input string
     * @return PascalCase version
     */
    public static String toPascalCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Converts a camelCase string to snake_case.
     *
     * @param str the input string
     * @return snake_case version
     */
    public static String toSnakeCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Converts a string to SCREAMING_SNAKE_CASE for constants.
     *
     * @param str the input string
     * @return SCREAMING_SNAKE_CASE version
     */
    public static String toScreamingSnakeCase(String str) {
        return toSnakeCase(str).toUpperCase();
    }

    /**
     * Gets the getter method name for a property.
     *
     * @param propertyName the property name
     * @param isBoolean    whether the property is boolean type
     * @return getter method name
     */
    public static String getGetterName(String propertyName, boolean isBoolean) {
        String prefix = isBoolean ? "is" : "get";
        return prefix + javaBeanCase(propertyName);
    }

    /**
     * Gets the setter method name for a property.
     *
     * @param propertyName the property name
     * @return setter method name
     */
    public static String getSetterName(String propertyName) {
        return "set" + javaBeanCase(propertyName);
    }

    /**
     * Pluralizes a word using simple English rules.
     *
     * @param word the word to pluralize
     * @return pluralized word
     */
    public static String pluralize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        // Simple pluralization rules
        if (word.endsWith("s") || word.endsWith("x") || word.endsWith("z") ||
            word.endsWith("ch") || word.endsWith("sh")) {
            return word + "es";
        } else if (word.endsWith("y") && word.length() > 1 &&
                   !isVowel(word.charAt(word.length() - 2))) {
            return word.substring(0, word.length() - 1) + "ies";
        } else {
            return word + "s";
        }
    }

    private static boolean isVowel(char c) {
        return "aeiouAEIOU".indexOf(c) != -1;
    }

    /**
     * Gets the main class name from a base name.
     *
     * @param baseName the base name of the application
     * @return the main class name
     */
    public static String getMainClassName(String baseName) {
        if (baseName == null || baseName.isEmpty()) {
            return "Application";
        }

        // Remove hyphens and underscores, capitalize each part
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : baseName.toCharArray()) {
            if (c == '-' || c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        String main = result.toString();

        // Validate that it's acceptable for Java
        if (main.matches("^[A-Z][a-zA-Z0-9_]*$")) {
            return main;
        }

        return "Application";
    }
}
