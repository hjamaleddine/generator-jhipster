/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.support;

import java.util.Set;

/**
 * Utility class for checking Java reserved keywords.
 */
public final class JavaReservedKeywords {

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
        // Java keywords
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient",
        "try", "void", "volatile", "while",

        // Java literals
        "true", "false", "null",

        // Java restricted identifiers (Java 10+)
        "var",

        // Java module keywords (Java 9+)
        "module", "open", "opens", "requires", "exports", "provides", "uses", "to", "with",

        // Java sealed keywords (Java 17+)
        "sealed", "non-sealed", "permits", "record",

        // Common problematic names
        "Object", "Class", "String", "System", "Error", "Exception",

        // SQL reserved words commonly used
        "order", "group", "user", "table", "column", "index", "key", "value",
        "select", "insert", "update", "delete", "from", "where", "join",

        // JPA/Hibernate reserved
        "id", "version", "entity"
    );

    private JavaReservedKeywords() {
        // Utility class
    }

    /**
     * Checks if the given name is a Java reserved keyword.
     *
     * @param name the name to check
     * @return true if the name is reserved
     */
    public static boolean isReserved(String name) {
        if (name == null) {
            return false;
        }
        return RESERVED_KEYWORDS.contains(name.toLowerCase());
    }

    /**
     * Checks if the given name is a Java reserved keyword (case-sensitive).
     *
     * @param name the name to check
     * @return true if the name is reserved
     */
    public static boolean isReservedExact(String name) {
        if (name == null) {
            return false;
        }
        return RESERVED_KEYWORDS.contains(name);
    }

    /**
     * Gets all reserved keywords.
     *
     * @return set of reserved keywords
     */
    public static Set<String> getReservedKeywords() {
        return RESERVED_KEYWORDS;
    }
}
