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

/**
 * Options for entity generation.
 * These correspond to the command-line options available for the entity generator.
 */
public class GeneratorOptions {

    // Behavior flags
    private boolean force = false;
    private boolean regenerate = false;
    private boolean defaults = false;
    private boolean singleEntity = false;

    // Skip options
    private Boolean skipServer;
    private Boolean skipClient;
    private Boolean skipDbChangelog;
    private Boolean skipCheckLengthOfIdentifier;
    private Boolean skipUiGrouping;

    // Database options
    private String databaseType;

    // Client options
    private String angularSuffix;
    private String clientRootFolder;

    // Table options
    private String tableName;

    // Constructors
    public GeneratorOptions() {
    }

    // Getters and Setters
    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public void setRegenerate(boolean regenerate) {
        this.regenerate = regenerate;
    }

    public boolean isDefaults() {
        return defaults;
    }

    public void setDefaults(boolean defaults) {
        this.defaults = defaults;
    }

    public boolean isSingleEntity() {
        return singleEntity;
    }

    public void setSingleEntity(boolean singleEntity) {
        this.singleEntity = singleEntity;
    }

    public Boolean getSkipServer() {
        return skipServer;
    }

    public void setSkipServer(Boolean skipServer) {
        this.skipServer = skipServer;
    }

    public Boolean getSkipClient() {
        return skipClient;
    }

    public void setSkipClient(Boolean skipClient) {
        this.skipClient = skipClient;
    }

    public Boolean getSkipDbChangelog() {
        return skipDbChangelog;
    }

    public void setSkipDbChangelog(Boolean skipDbChangelog) {
        this.skipDbChangelog = skipDbChangelog;
    }

    public Boolean getSkipCheckLengthOfIdentifier() {
        return skipCheckLengthOfIdentifier;
    }

    public void setSkipCheckLengthOfIdentifier(Boolean skipCheckLengthOfIdentifier) {
        this.skipCheckLengthOfIdentifier = skipCheckLengthOfIdentifier;
    }

    public Boolean getSkipUiGrouping() {
        return skipUiGrouping;
    }

    public void setSkipUiGrouping(Boolean skipUiGrouping) {
        this.skipUiGrouping = skipUiGrouping;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getAngularSuffix() {
        return angularSuffix;
    }

    public void setAngularSuffix(String angularSuffix) {
        this.angularSuffix = angularSuffix;
    }

    public String getClientRootFolder() {
        return clientRootFolder;
    }

    public void setClientRootFolder(String clientRootFolder) {
        this.clientRootFolder = clientRootFolder;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    // Builder pattern
    public static GeneratorOptionsBuilder builder() {
        return new GeneratorOptionsBuilder();
    }

    public static class GeneratorOptionsBuilder {
        private final GeneratorOptions options = new GeneratorOptions();

        public GeneratorOptionsBuilder force(boolean force) {
            options.setForce(force);
            return this;
        }

        public GeneratorOptionsBuilder regenerate(boolean regenerate) {
            options.setRegenerate(regenerate);
            return this;
        }

        public GeneratorOptionsBuilder defaults(boolean defaults) {
            options.setDefaults(defaults);
            return this;
        }

        public GeneratorOptionsBuilder skipServer(boolean skipServer) {
            options.setSkipServer(skipServer);
            return this;
        }

        public GeneratorOptionsBuilder skipClient(boolean skipClient) {
            options.setSkipClient(skipClient);
            return this;
        }

        public GeneratorOptionsBuilder skipDbChangelog(boolean skipDbChangelog) {
            options.setSkipDbChangelog(skipDbChangelog);
            return this;
        }

        public GeneratorOptionsBuilder databaseType(String databaseType) {
            options.setDatabaseType(databaseType);
            return this;
        }

        public GeneratorOptionsBuilder tableName(String tableName) {
            options.setTableName(tableName);
            return this;
        }

        public GeneratorOptionsBuilder angularSuffix(String angularSuffix) {
            options.setAngularSuffix(angularSuffix);
            return this;
        }

        public GeneratorOptionsBuilder clientRootFolder(String clientRootFolder) {
            options.setClientRootFolder(clientRootFolder);
            return this;
        }

        public GeneratorOptions build() {
            return options;
        }
    }

    @Override
    public String toString() {
        return "GeneratorOptions{" +
               "force=" + force +
               ", regenerate=" + regenerate +
               ", skipServer=" + skipServer +
               ", skipClient=" + skipClient +
               ", databaseType='" + databaseType + '\'' +
               '}';
    }
}
