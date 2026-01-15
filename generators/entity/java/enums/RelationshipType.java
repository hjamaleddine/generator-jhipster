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
 * Enumeration of relationship types between entities.
 */
public enum RelationshipType {
    ONE_TO_ONE("one-to-one"),
    ONE_TO_MANY("one-to-many"),
    MANY_TO_ONE("many-to-one"),
    MANY_TO_MANY("many-to-many");

    private final String value;

    RelationshipType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isToMany() {
        return this == ONE_TO_MANY || this == MANY_TO_MANY;
    }

    public boolean isOwnerSide() {
        return this == MANY_TO_ONE || this == MANY_TO_MANY;
    }

    public static RelationshipType fromString(String value) {
        for (RelationshipType type : values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value.replace("-", "_"))) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown relationship type: " + value);
    }
}
