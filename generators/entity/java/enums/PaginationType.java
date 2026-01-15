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
 * Enumeration of pagination types for entity listing.
 */
public enum PaginationType {
    NO("no"),
    PAGINATION("pagination"),
    INFINITE_SCROLL("infinite-scroll");

    private final String value;

    PaginationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean hasPagination() {
        return this != NO;
    }

    public static PaginationType fromString(String value) {
        for (PaginationType type : values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value.replace("-", "_"))) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown pagination type: " + value);
    }
}
