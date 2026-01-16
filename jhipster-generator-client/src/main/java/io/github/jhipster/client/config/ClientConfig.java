/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.client.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side configuration for JHipster applications.
 * Maps to .yo-rc.json client properties.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientConfig {

    // Application identifiers
    private String baseName;
    private String packageName;
    private String packageFolder;

    // Client framework selection
    @JsonProperty("clientFramework")
    private String clientFramework = "angular"; // angular, react, vue, no

    // Theme configuration
    @JsonProperty("clientTheme")
    private String clientTheme = "none"; // none, cerulean, cosmo, cyborg, etc.

    @JsonProperty("clientThemeVariant")
    private String clientThemeVariant = "primary"; // primary, dark, light

    // Build tool
    @JsonProperty("clientBundler")
    private String clientBundler = "webpack"; // webpack, vite, esbuild

    // Test framework
    @JsonProperty("clientTestFramework")
    private String clientTestFramework = "jest"; // jest, vitest

    // Development server
    @JsonProperty("devServerPort")
    private Integer devServerPort = 9000;

    // Paths
    @JsonProperty("clientSrcDir")
    private String clientSrcDir = "src/main/webapp/";

    @JsonProperty("clientTestDir")
    private String clientTestDir = "src/test/javascript/";

    // Features
    @JsonProperty("enableTranslation")
    private Boolean enableTranslation = true;

    @JsonProperty("nativeLanguage")
    private String nativeLanguage = "en";

    @JsonProperty("languages")
    private List<String> languages = new ArrayList<>(List.of("en", "fr"));

    @JsonProperty("enableI18nRTL")
    private Boolean enableI18nRTL = false;

    // Authentication
    @JsonProperty("authenticationType")
    private String authenticationType = "jwt"; // jwt, oauth2, session

    // Application type
    @JsonProperty("applicationType")
    private String applicationType = "monolith"; // monolith, microservice, gateway

    // Microfrontend
    @JsonProperty("microfrontend")
    private Boolean microfrontend = false;

    @JsonProperty("microfrontends")
    private List<MicrofrontendConfig> microfrontends = new ArrayList<>();

    // WebSocket
    @JsonProperty("websocket")
    private String websocket; // spring-websocket or null

    // Test frameworks
    @JsonProperty("testFrameworks")
    private List<String> testFrameworks = new ArrayList<>();

    // Skip flags
    @JsonProperty("skipClient")
    private Boolean skipClient = false;

    @JsonProperty("skipServer")
    private Boolean skipServer = false;

    // Entity configuration
    private List<EntityConfig> entities = new ArrayList<>();

    // Additional node dependencies
    private Map<String, String> nodeDependencies = new HashMap<>();

    // Getters and Setters

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageFolder() {
        return packageFolder;
    }

    public void setPackageFolder(String packageFolder) {
        this.packageFolder = packageFolder;
    }

    public String getClientFramework() {
        return clientFramework;
    }

    public void setClientFramework(String clientFramework) {
        this.clientFramework = clientFramework;
    }

    public String getClientTheme() {
        return clientTheme;
    }

    public void setClientTheme(String clientTheme) {
        this.clientTheme = clientTheme;
    }

    public String getClientThemeVariant() {
        return clientThemeVariant;
    }

    public void setClientThemeVariant(String clientThemeVariant) {
        this.clientThemeVariant = clientThemeVariant;
    }

    public String getClientBundler() {
        return clientBundler;
    }

    public void setClientBundler(String clientBundler) {
        this.clientBundler = clientBundler;
    }

    public String getClientTestFramework() {
        return clientTestFramework;
    }

    public void setClientTestFramework(String clientTestFramework) {
        this.clientTestFramework = clientTestFramework;
    }

    public Integer getDevServerPort() {
        return devServerPort;
    }

    public void setDevServerPort(Integer devServerPort) {
        this.devServerPort = devServerPort;
    }

    public String getClientSrcDir() {
        return clientSrcDir;
    }

    public void setClientSrcDir(String clientSrcDir) {
        this.clientSrcDir = clientSrcDir;
    }

    public String getClientTestDir() {
        return clientTestDir;
    }

    public void setClientTestDir(String clientTestDir) {
        this.clientTestDir = clientTestDir;
    }

    public Boolean getEnableTranslation() {
        return enableTranslation;
    }

    public void setEnableTranslation(Boolean enableTranslation) {
        this.enableTranslation = enableTranslation;
    }

    public String getNativeLanguage() {
        return nativeLanguage;
    }

    public void setNativeLanguage(String nativeLanguage) {
        this.nativeLanguage = nativeLanguage;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public Boolean getEnableI18nRTL() {
        return enableI18nRTL;
    }

    public void setEnableI18nRTL(Boolean enableI18nRTL) {
        this.enableI18nRTL = enableI18nRTL;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public Boolean getMicrofrontend() {
        return microfrontend;
    }

    public void setMicrofrontend(Boolean microfrontend) {
        this.microfrontend = microfrontend;
    }

    public List<MicrofrontendConfig> getMicrofrontends() {
        return microfrontends;
    }

    public void setMicrofrontends(List<MicrofrontendConfig> microfrontends) {
        this.microfrontends = microfrontends;
    }

    public String getWebsocket() {
        return websocket;
    }

    public void setWebsocket(String websocket) {
        this.websocket = websocket;
    }

    public List<String> getTestFrameworks() {
        return testFrameworks;
    }

    public void setTestFrameworks(List<String> testFrameworks) {
        this.testFrameworks = testFrameworks;
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

    public List<EntityConfig> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityConfig> entities) {
        this.entities = entities;
    }

    public Map<String, String> getNodeDependencies() {
        return nodeDependencies;
    }

    public void setNodeDependencies(Map<String, String> nodeDependencies) {
        this.nodeDependencies = nodeDependencies;
    }

    // Convenience methods

    public boolean isAngular() {
        return "angular".equals(clientFramework);
    }

    public boolean isReact() {
        return "react".equals(clientFramework);
    }

    public boolean isVue() {
        return "vue".equals(clientFramework);
    }

    public boolean hasClientFramework() {
        return clientFramework != null && !"no".equals(clientFramework);
    }

    public boolean isWebpack() {
        return "webpack".equals(clientBundler);
    }

    public boolean isVite() {
        return "vite".equals(clientBundler);
    }

    public boolean isEsbuild() {
        return "esbuild".equals(clientBundler) || "experimental-esbuild".equals(clientBundler);
    }

    public boolean isJwt() {
        return "jwt".equals(authenticationType);
    }

    public boolean isOAuth2() {
        return "oauth2".equals(authenticationType);
    }

    public boolean isSession() {
        return "session".equals(authenticationType);
    }

    public boolean isMonolith() {
        return "monolith".equals(applicationType);
    }

    public boolean isGateway() {
        return "gateway".equals(applicationType);
    }

    public boolean isMicroservice() {
        return "microservice".equals(applicationType);
    }

    public boolean hasWebsocket() {
        return websocket != null && !websocket.isEmpty();
    }

    public boolean hasCypress() {
        return testFrameworks != null && testFrameworks.contains("cypress");
    }

    public String getClientWebappDir() {
        return clientSrcDir + "app/";
    }

    public String getEnumerationsDir() {
        return getClientWebappDir() + "shared/model/enumerations/";
    }

    /**
     * Microfrontend configuration.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MicrofrontendConfig {
        private String baseName;
        private String clientRootDir;

        public String getBaseName() {
            return baseName;
        }

        public void setBaseName(String baseName) {
            this.baseName = baseName;
        }

        public String getClientRootDir() {
            return clientRootDir;
        }

        public void setClientRootDir(String clientRootDir) {
            this.clientRootDir = clientRootDir;
        }
    }

    /**
     * Entity configuration for client-side generation.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityConfig {
        private String name;
        private String entityAngularName;
        private String entityReactName;
        private String entityFileName;
        private String entityFolderName;
        private String entityModelFileName;
        private Boolean readOnly = false;
        private Boolean embedded = false;
        private Boolean builtIn = false;
        private List<FieldConfig> fields = new ArrayList<>();
        private List<RelationshipConfig> relationships = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEntityAngularName() {
            return entityAngularName;
        }

        public void setEntityAngularName(String entityAngularName) {
            this.entityAngularName = entityAngularName;
        }

        public String getEntityReactName() {
            return entityReactName;
        }

        public void setEntityReactName(String entityReactName) {
            this.entityReactName = entityReactName;
        }

        public String getEntityFileName() {
            return entityFileName;
        }

        public void setEntityFileName(String entityFileName) {
            this.entityFileName = entityFileName;
        }

        public String getEntityFolderName() {
            return entityFolderName;
        }

        public void setEntityFolderName(String entityFolderName) {
            this.entityFolderName = entityFolderName;
        }

        public String getEntityModelFileName() {
            return entityModelFileName;
        }

        public void setEntityModelFileName(String entityModelFileName) {
            this.entityModelFileName = entityModelFileName;
        }

        public Boolean getReadOnly() {
            return readOnly;
        }

        public void setReadOnly(Boolean readOnly) {
            this.readOnly = readOnly;
        }

        public Boolean getEmbedded() {
            return embedded;
        }

        public void setEmbedded(Boolean embedded) {
            this.embedded = embedded;
        }

        public Boolean getBuiltIn() {
            return builtIn;
        }

        public void setBuiltIn(Boolean builtIn) {
            this.builtIn = builtIn;
        }

        public List<FieldConfig> getFields() {
            return fields;
        }

        public void setFields(List<FieldConfig> fields) {
            this.fields = fields;
        }

        public List<RelationshipConfig> getRelationships() {
            return relationships;
        }

        public void setRelationships(List<RelationshipConfig> relationships) {
            this.relationships = relationships;
        }

        public boolean hasDateField() {
            return fields.stream().anyMatch(f ->
                "LocalDate".equals(f.getFieldType()) ||
                "Instant".equals(f.getFieldType()) ||
                "ZonedDateTime".equals(f.getFieldType()) ||
                "Duration".equals(f.getFieldType())
            );
        }
    }

    /**
     * Field configuration for entities.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldConfig {
        private String fieldName;
        private String fieldType;
        private String fieldTypeTsType;
        private Boolean required = false;
        private Boolean unique = false;
        private Integer minLength;
        private Integer maxLength;
        private String pattern;
        private Boolean fieldIsEnum = false;
        private String fieldValues; // For enums

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldType() {
            return fieldType;
        }

        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }

        public String getFieldTypeTsType() {
            return fieldTypeTsType;
        }

        public void setFieldTypeTsType(String fieldTypeTsType) {
            this.fieldTypeTsType = fieldTypeTsType;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }

        public Boolean getUnique() {
            return unique;
        }

        public void setUnique(Boolean unique) {
            this.unique = unique;
        }

        public Integer getMinLength() {
            return minLength;
        }

        public void setMinLength(Integer minLength) {
            this.minLength = minLength;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public Boolean getFieldIsEnum() {
            return fieldIsEnum;
        }

        public void setFieldIsEnum(Boolean fieldIsEnum) {
            this.fieldIsEnum = fieldIsEnum;
        }

        public String getFieldValues() {
            return fieldValues;
        }

        public void setFieldValues(String fieldValues) {
            this.fieldValues = fieldValues;
        }

        public String getTsType() {
            if (fieldTypeTsType != null) {
                return fieldTypeTsType;
            }
            return switch (fieldType) {
                case "String", "UUID" -> "string";
                case "Integer", "Long", "Float", "Double", "BigDecimal" -> "number";
                case "Boolean" -> "boolean";
                case "LocalDate", "Instant", "ZonedDateTime" -> "dayjs.Dayjs";
                case "Duration" -> "string";
                case "byte[]", "ByteBuffer" -> "string"; // Base64
                default -> fieldIsEnum ? fieldType : "any";
            };
        }
    }

    /**
     * Relationship configuration for entities.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationshipConfig {
        private String relationshipName;
        private String relationshipType; // one-to-one, one-to-many, many-to-one, many-to-many
        private String otherEntityName;
        private String otherEntityAngularName;
        private String otherEntityField;
        private Boolean ownerSide = true;
        private Boolean required = false;

        public String getRelationshipName() {
            return relationshipName;
        }

        public void setRelationshipName(String relationshipName) {
            this.relationshipName = relationshipName;
        }

        public String getRelationshipType() {
            return relationshipType;
        }

        public void setRelationshipType(String relationshipType) {
            this.relationshipType = relationshipType;
        }

        public String getOtherEntityName() {
            return otherEntityName;
        }

        public void setOtherEntityName(String otherEntityName) {
            this.otherEntityName = otherEntityName;
        }

        public String getOtherEntityAngularName() {
            return otherEntityAngularName;
        }

        public void setOtherEntityAngularName(String otherEntityAngularName) {
            this.otherEntityAngularName = otherEntityAngularName;
        }

        public String getOtherEntityField() {
            return otherEntityField;
        }

        public void setOtherEntityField(String otherEntityField) {
            this.otherEntityField = otherEntityField;
        }

        public Boolean getOwnerSide() {
            return ownerSide;
        }

        public void setOwnerSide(Boolean ownerSide) {
            this.ownerSide = ownerSide;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }

        public boolean isCollection() {
            return "one-to-many".equals(relationshipType) || "many-to-many".equals(relationshipType);
        }
    }
}
