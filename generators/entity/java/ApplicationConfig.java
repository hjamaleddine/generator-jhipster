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
package io.github.jhipster.generator.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jhipster.generator.entity.enums.ApplicationType;
import io.github.jhipster.generator.entity.enums.DatabaseType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Application configuration loaded from .yo-rc.json.
 * Contains the main JHipster application settings needed for entity generation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String baseName;
    private String applicationType;
    private String databaseType;
    private String prodDatabaseType;
    private String devDatabaseType;
    private String clientFramework;
    private Boolean reactive;
    private Boolean skipClient;
    private Boolean skipServer;
    private Boolean skipUserManagement;
    private List<String> entities;

    // User generation settings
    private Boolean generateBuiltInUserEntity = true;
    private Boolean generateBuiltInAuthorityEntity = true;

    public ApplicationConfig() {
    }

    /**
     * Loads application configuration from .yo-rc.json file.
     */
    public static ApplicationConfig load(Path yoRcPath) throws IOException {
        if (!Files.exists(yoRcPath)) {
            return new ApplicationConfig();
        }

        String content = Files.readString(yoRcPath);
        JsonNode root = MAPPER.readTree(content);

        // JHipster config is nested under "generator-jhipster" key
        JsonNode jhipsterNode = root.get("generator-jhipster");
        if (jhipsterNode == null) {
            return new ApplicationConfig();
        }

        return MAPPER.treeToValue(jhipsterNode, ApplicationConfig.class);
    }

    // Getters and Setters
    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    @JsonProperty("applicationType")
    public String getApplicationTypeString() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public ApplicationType getApplicationType() {
        if (applicationType == null) {
            return ApplicationType.MONOLITH;
        }
        try {
            return ApplicationType.fromString(applicationType);
        } catch (IllegalArgumentException e) {
            return ApplicationType.MONOLITH;
        }
    }

    @JsonProperty("databaseType")
    public String getDatabaseTypeString() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public DatabaseType getDatabaseType() {
        if (databaseType == null) {
            return DatabaseType.SQL;
        }
        try {
            return DatabaseType.fromString(databaseType);
        } catch (IllegalArgumentException e) {
            return DatabaseType.SQL;
        }
    }

    public String getProdDatabaseType() {
        return prodDatabaseType;
    }

    public void setProdDatabaseType(String prodDatabaseType) {
        this.prodDatabaseType = prodDatabaseType;
    }

    public String getDevDatabaseType() {
        return devDatabaseType;
    }

    public void setDevDatabaseType(String devDatabaseType) {
        this.devDatabaseType = devDatabaseType;
    }

    public String getClientFramework() {
        return clientFramework;
    }

    public void setClientFramework(String clientFramework) {
        this.clientFramework = clientFramework;
    }

    public Boolean getReactive() {
        return reactive;
    }

    public void setReactive(Boolean reactive) {
        this.reactive = reactive;
    }

    public boolean isReactive() {
        return Boolean.TRUE.equals(reactive);
    }

    public Boolean getSkipClient() {
        return skipClient;
    }

    public void setSkipClient(Boolean skipClient) {
        this.skipClient = skipClient;
    }

    public Boolean getSkipServer() {
        return skipServer;
    }

    public void setSkipServer(Boolean skipServer) {
        this.skipServer = skipServer;
    }

    public Boolean getSkipUserManagement() {
        return skipUserManagement;
    }

    public void setSkipUserManagement(Boolean skipUserManagement) {
        this.skipUserManagement = skipUserManagement;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public Boolean getGenerateBuiltInUserEntity() {
        return generateBuiltInUserEntity;
    }

    public void setGenerateBuiltInUserEntity(Boolean generateBuiltInUserEntity) {
        this.generateBuiltInUserEntity = generateBuiltInUserEntity;
    }

    public boolean isGenerateBuiltInUserEntity() {
        return Boolean.TRUE.equals(generateBuiltInUserEntity) && !Boolean.TRUE.equals(skipUserManagement);
    }

    public Boolean getGenerateBuiltInAuthorityEntity() {
        return generateBuiltInAuthorityEntity;
    }

    public void setGenerateBuiltInAuthorityEntity(Boolean generateBuiltInAuthorityEntity) {
        this.generateBuiltInAuthorityEntity = generateBuiltInAuthorityEntity;
    }

    public boolean isGenerateBuiltInAuthorityEntity() {
        return Boolean.TRUE.equals(generateBuiltInAuthorityEntity) && !Boolean.TRUE.equals(skipUserManagement);
    }

    // Utility methods
    public boolean isSqlDatabase() {
        return getDatabaseType() == DatabaseType.SQL;
    }

    public boolean isNoSqlDatabase() {
        return getDatabaseType().isNoSql();
    }

    public boolean isMicroservice() {
        return getApplicationType() == ApplicationType.MICROSERVICE;
    }

    public boolean isGateway() {
        return getApplicationType() == ApplicationType.GATEWAY;
    }

    public boolean isMonolith() {
        return getApplicationType() == ApplicationType.MONOLITH;
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" +
               "baseName='" + baseName + '\'' +
               ", applicationType='" + applicationType + '\'' +
               ", databaseType='" + databaseType + '\'' +
               ", clientFramework='" + clientFramework + '\'' +
               ", reactive=" + reactive +
               ", entities=" + (entities != null ? entities.size() : 0) +
               '}';
    }
}
