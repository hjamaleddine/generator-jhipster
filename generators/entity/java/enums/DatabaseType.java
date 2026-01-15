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
package io.github.jhipster.generator.entity.enums;

/**
 * Enumeration of database types supported by JHipster.
 */
public enum DatabaseType {
    SQL("sql"),
    MONGODB("mongodb"),
    CASSANDRA("cassandra"),
    COUCHBASE("couchbase"),
    NEO4J("neo4j"),
    NO("no");

    private final String value;

    DatabaseType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isSql() {
        return this == SQL;
    }

    public boolean isNoSql() {
        return this == MONGODB || this == CASSANDRA || this == COUCHBASE || this == NEO4J;
    }

    public boolean supportsRelationships() {
        return this != CASSANDRA;
    }

    public static DatabaseType fromString(String value) {
        for (DatabaseType type : values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown database type: " + value);
    }

    public static DatabaseType fromDBValue(String dbValue) {
        if (dbValue == null || dbValue.isEmpty()) {
            return NO;
        }
        switch (dbValue.toLowerCase()) {
            case "mysql":
            case "mariadb":
            case "postgresql":
            case "oracle":
            case "mssql":
            case "h2disk":
            case "h2memory":
                return SQL;
            case "mongodb":
                return MONGODB;
            case "cassandra":
                return CASSANDRA;
            case "couchbase":
                return COUCHBASE;
            case "neo4j":
                return NEO4J;
            default:
                return NO;
        }
    }
}
