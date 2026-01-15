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
package io.github.jhipster.generator.domain.model;

/**
 * Primary key information for entity generation.
 */
public class PrimaryKeyInfo {

    private String name = "id";
    private String nameCapitalized = "Id";
    private String type = "Long";
    private boolean composite;

    public PrimaryKeyInfo() {
    }

    public PrimaryKeyInfo(String type) {
        this.type = type;
    }

    public PrimaryKeyInfo(String name, String type) {
        setName(name);
        this.type = type;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameCapitalized = capitalize(name);
    }

    public String getNameCapitalized() {
        return nameCapitalized;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isComposite() {
        return composite;
    }

    public void setComposite(boolean composite) {
        this.composite = composite;
    }

    /**
     * Checks if the primary key type is Long.
     */
    public boolean isTypeLong() {
        return "Long".equals(type);
    }

    /**
     * Checks if the primary key type is UUID.
     */
    public boolean isTypeUUID() {
        return "UUID".equals(type);
    }

    /**
     * Checks if the primary key type is String.
     */
    public boolean isTypeString() {
        return "String".equals(type);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public String toString() {
        return "PrimaryKeyInfo{" +
               "name='" + name + '\'' +
               ", type='" + type + '\'' +
               ", composite=" + composite +
               '}';
    }
}
