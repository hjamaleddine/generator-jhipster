/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.model;

/**
 * Primary key configuration for an entity.
 */
public class PrimaryKeyConfig {

    private String name = "id";
    private String type = "Long";
    private String nameCapitalized = "Id";
    private boolean composite = false;
    private boolean derived = false;
    private FieldConfig field;

    public PrimaryKeyConfig() {
    }

    public PrimaryKeyConfig(String name, String type) {
        this.name = name;
        this.type = type;
        this.nameCapitalized = capitalize(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameCapitalized = capitalize(name);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNameCapitalized() {
        return nameCapitalized;
    }

    public void setNameCapitalized(String nameCapitalized) {
        this.nameCapitalized = nameCapitalized;
    }

    public boolean isComposite() {
        return composite;
    }

    public void setComposite(boolean composite) {
        this.composite = composite;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    public FieldConfig getField() {
        return field;
    }

    public void setField(FieldConfig field) {
        this.field = field;
    }

    // Type helpers

    public boolean isLong() {
        return "Long".equals(type);
    }

    public boolean isUUID() {
        return "UUID".equals(type);
    }

    public boolean isString() {
        return "String".equals(type);
    }

    public boolean isInteger() {
        return "Integer".equals(type);
    }

    public String getJdbcType() {
        switch (type) {
            case "Long":
                return "BIGINT";
            case "Integer":
                return "INTEGER";
            case "UUID":
                return "UUID";
            case "String":
                return "VARCHAR(255)";
            default:
                return "BIGINT";
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static PrimaryKeyConfig defaultLong() {
        return new PrimaryKeyConfig("id", "Long");
    }

    public static PrimaryKeyConfig defaultUUID() {
        return new PrimaryKeyConfig("id", "UUID");
    }
}
