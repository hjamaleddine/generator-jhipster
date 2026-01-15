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
package io.github.jhipster.generator.entity.model;

import io.github.jhipster.generator.entity.enums.DatabaseType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runtime data for entity generation process.
 * This class holds the transient state during entity generation, separate from
 * the persistent EntityConfig.
 */
public class EntityData {

    private String name;
    private String filename;
    private boolean configExisted;
    private boolean entityExisted;
    private boolean configurationFileExists;
    private boolean useConfigurationFile;
    private boolean regenerate;
    private String updateEntity = "regenerate";
    private boolean skipServer;
    private boolean skipClient;
    private DatabaseType databaseType;
    private String clientFramework;
    private boolean reactive;

    // Microservice specific
    private String microserviceName;
    private String microservicePath;
    private String microserviceFileName;
    private String clientRootFolder;
    private boolean skipUiGrouping;
    private boolean useMicroserviceJson;

    // Entity name variants
    private String entityNameCapitalized;
    private String entityNamePlural;
    private String entityNamePluralizedAndSpinalCased;

    // Tracking enums during generation
    private List<String> enums = new ArrayList<>();
    private boolean existingEnum;

    // Constructor
    public EntityData() {
    }

    public EntityData(String name) {
        this.name = name;
        this.entityNameCapitalized = capitalize(name);
    }

    // Factory method
    public static EntityData create(String name, Path configDir) {
        EntityData data = new EntityData(name);
        data.filename = configDir.resolve(name + ".json").toString();
        return data;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.entityNameCapitalized = capitalize(name);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isConfigExisted() {
        return configExisted;
    }

    public void setConfigExisted(boolean configExisted) {
        this.configExisted = configExisted;
    }

    public boolean isEntityExisted() {
        return entityExisted;
    }

    public void setEntityExisted(boolean entityExisted) {
        this.entityExisted = entityExisted;
    }

    public boolean isConfigurationFileExists() {
        return configurationFileExists;
    }

    public void setConfigurationFileExists(boolean configurationFileExists) {
        this.configurationFileExists = configurationFileExists;
    }

    public boolean isUseConfigurationFile() {
        return useConfigurationFile;
    }

    public void setUseConfigurationFile(boolean useConfigurationFile) {
        this.useConfigurationFile = useConfigurationFile;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public void setRegenerate(boolean regenerate) {
        this.regenerate = regenerate;
    }

    public String getUpdateEntity() {
        return updateEntity;
    }

    public void setUpdateEntity(String updateEntity) {
        this.updateEntity = updateEntity;
    }

    public boolean isSkipServer() {
        return skipServer;
    }

    public void setSkipServer(boolean skipServer) {
        this.skipServer = skipServer;
    }

    public boolean isSkipClient() {
        return skipClient;
    }

    public void setSkipClient(boolean skipClient) {
        this.skipClient = skipClient;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getClientFramework() {
        return clientFramework;
    }

    public void setClientFramework(String clientFramework) {
        this.clientFramework = clientFramework;
    }

    public boolean isReactive() {
        return reactive;
    }

    public void setReactive(boolean reactive) {
        this.reactive = reactive;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }

    public void setMicroserviceName(String microserviceName) {
        this.microserviceName = microserviceName;
    }

    public String getMicroservicePath() {
        return microservicePath;
    }

    public void setMicroservicePath(String microservicePath) {
        this.microservicePath = microservicePath;
    }

    public String getMicroserviceFileName() {
        return microserviceFileName;
    }

    public void setMicroserviceFileName(String microserviceFileName) {
        this.microserviceFileName = microserviceFileName;
    }

    public String getClientRootFolder() {
        return clientRootFolder;
    }

    public void setClientRootFolder(String clientRootFolder) {
        this.clientRootFolder = clientRootFolder;
    }

    public boolean isSkipUiGrouping() {
        return skipUiGrouping;
    }

    public void setSkipUiGrouping(boolean skipUiGrouping) {
        this.skipUiGrouping = skipUiGrouping;
    }

    public boolean isUseMicroserviceJson() {
        return useMicroserviceJson;
    }

    public void setUseMicroserviceJson(boolean useMicroserviceJson) {
        this.useMicroserviceJson = useMicroserviceJson;
    }

    public String getEntityNameCapitalized() {
        return entityNameCapitalized;
    }

    public void setEntityNameCapitalized(String entityNameCapitalized) {
        this.entityNameCapitalized = entityNameCapitalized;
    }

    public String getEntityNamePlural() {
        return entityNamePlural;
    }

    public void setEntityNamePlural(String entityNamePlural) {
        this.entityNamePlural = entityNamePlural;
    }

    public String getEntityNamePluralizedAndSpinalCased() {
        return entityNamePluralizedAndSpinalCased;
    }

    public void setEntityNamePluralizedAndSpinalCased(String entityNamePluralizedAndSpinalCased) {
        this.entityNamePluralizedAndSpinalCased = entityNamePluralizedAndSpinalCased;
    }

    public List<String> getEnums() {
        return enums;
    }

    public void setEnums(List<String> enums) {
        this.enums = enums;
    }

    public boolean isExistingEnum() {
        return existingEnum;
    }

    public void setExistingEnum(boolean existingEnum) {
        this.existingEnum = existingEnum;
    }

    // Utility methods
    public void addEnum(String enumName) {
        if (enums == null) {
            enums = new ArrayList<>();
        }
        if (!enums.contains(enumName)) {
            enums.add(enumName);
        } else {
            existingEnum = true;
        }
    }

    public boolean shouldAddFields() {
        return "add".equals(updateEntity);
    }

    public boolean shouldRemoveFields() {
        return "remove".equals(updateEntity);
    }

    public boolean shouldRegenerate() {
        return "regenerate".equals(updateEntity);
    }

    public boolean shouldAbort() {
        return "none".equals(updateEntity);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public String toString() {
        return "EntityData{" +
               "name='" + name + '\'' +
               ", configExisted=" + configExisted +
               ", entityExisted=" + entityExisted +
               ", updateEntity='" + updateEntity + '\'' +
               ", databaseType=" + databaseType +
               '}';
    }
}
