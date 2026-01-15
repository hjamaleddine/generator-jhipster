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
 * Enumeration of field types supported by JHipster entity generator.
 */
public enum FieldType {
    // Common DB Types
    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    FLOAT("Float"),
    DOUBLE("Double"),
    BIG_DECIMAL("BigDecimal"),
    LOCAL_DATE("LocalDate"),
    INSTANT("Instant"),
    ZONED_DATE_TIME("ZonedDateTime"),
    DURATION("Duration"),
    BOOLEAN("Boolean"),
    ENUM("Enum"),
    UUID("UUID"),
    LOCAL_TIME("LocalTime"),

    // Relational Only DB Types
    BYTES("byte[]"),
    BYTE_BUFFER("ByteBuffer");

    private final String javaType;

    FieldType(String javaType) {
        this.javaType = javaType;
    }

    public String getJavaType() {
        return javaType;
    }

    public boolean isNumeric() {
        return this == INTEGER || this == LONG || this == FLOAT || this == DOUBLE || this == BIG_DECIMAL;
    }

    public boolean isTemporal() {
        return this == LOCAL_DATE || this == INSTANT || this == ZONED_DATE_TIME || this == DURATION || this == LOCAL_TIME;
    }

    public boolean isBlob() {
        return this == BYTES || this == BYTE_BUFFER;
    }

    public static FieldType fromString(String value) {
        for (FieldType type : values()) {
            if (type.name().equalsIgnoreCase(value) || type.javaType.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown field type: " + value);
    }
}
