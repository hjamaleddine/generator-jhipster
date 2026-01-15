/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * JHipster application configuration.
 * Represents the content of .yo-rc.json generator-jhipster section.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JHipsterConfig {

    // Application
    @JsonProperty("baseName")
    private String baseName = "jhipster";

    @JsonProperty("applicationType")
    private String applicationType = "microservice";

    @JsonProperty("packageName")
    private String packageName = "com.mycompany.myapp";

    @JsonProperty("packageFolder")
    private String packageFolder;

    @JsonProperty("serverPort")
    private Integer serverPort = 8080;

    @JsonProperty("reactive")
    private Boolean reactive = false;

    // Authentication
    @JsonProperty("authenticationType")
    private String authenticationType = "jwt";

    @JsonProperty("skipUserManagement")
    private Boolean skipUserManagement = false;

    // Database
    @JsonProperty("databaseType")
    private String databaseType = "sql";

    @JsonProperty("devDatabaseType")
    private String devDatabaseType = "h2Disk";

    @JsonProperty("prodDatabaseType")
    private String prodDatabaseType = "postgresql";

    // Build
    @JsonProperty("buildTool")
    private String buildTool = "maven";

    // Service Discovery
    @JsonProperty("serviceDiscoveryType")
    private String serviceDiscoveryType = "consul";

    // Feign
    @JsonProperty("feignClient")
    private Boolean feignClient = false;

    // Cache
    @JsonProperty("cacheProvider")
    private String cacheProvider = "ehcache";

    @JsonProperty("enableHibernateCache")
    private Boolean enableHibernateCache = true;

    // Search
    @JsonProperty("searchEngine")
    private String searchEngine = "no";

    // Message Broker
    @JsonProperty("messageBroker")
    private String messageBroker = "no";

    // WebSocket
    @JsonProperty("websocket")
    private String websocket = "no";

    // Client
    @JsonProperty("skipClient")
    private Boolean skipClient = true;

    @JsonProperty("clientFramework")
    private String clientFramework;

    // i18n
    @JsonProperty("enableTranslation")
    private Boolean enableTranslation = false;

    @JsonProperty("nativeLanguage")
    private String nativeLanguage = "en";

    // Testing
    @JsonProperty("testFrameworks")
    private String[] testFrameworks = new String[0];

    // DTO/Service
    @JsonProperty("dto")
    private String dto = "mapstruct";

    @JsonProperty("service")
    private String service = "serviceImpl";

    @JsonProperty("pagination")
    private String pagination = "pagination";

    // Misc
    @JsonProperty("jhiPrefix")
    private String jhiPrefix = "jhi";

    @JsonProperty("entitySuffix")
    private String entitySuffix = "";

    @JsonProperty("dtoSuffix")
    private String dtoSuffix = "DTO";

    @JsonProperty("jwtSecretKey")
    private String jwtSecretKey;

    // Additional properties
    private Map<String, Object> additionalProperties = new HashMap<>();

    // Getters and Setters

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageFolder() {
        if (packageFolder == null && packageName != null) {
            return packageName.replace('.', '/');
        }
        return packageFolder;
    }

    public void setPackageFolder(String packageFolder) {
        this.packageFolder = packageFolder;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public Boolean getReactive() {
        return reactive;
    }

    public void setReactive(Boolean reactive) {
        this.reactive = reactive;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public Boolean getSkipUserManagement() {
        return skipUserManagement;
    }

    public void setSkipUserManagement(Boolean skipUserManagement) {
        this.skipUserManagement = skipUserManagement;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getDevDatabaseType() {
        return devDatabaseType;
    }

    public void setDevDatabaseType(String devDatabaseType) {
        this.devDatabaseType = devDatabaseType;
    }

    public String getProdDatabaseType() {
        return prodDatabaseType;
    }

    public void setProdDatabaseType(String prodDatabaseType) {
        this.prodDatabaseType = prodDatabaseType;
    }

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public String getServiceDiscoveryType() {
        return serviceDiscoveryType;
    }

    public void setServiceDiscoveryType(String serviceDiscoveryType) {
        this.serviceDiscoveryType = serviceDiscoveryType;
    }

    public Boolean getFeignClient() {
        return feignClient;
    }

    public void setFeignClient(Boolean feignClient) {
        this.feignClient = feignClient;
    }

    public String getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(String cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public Boolean getEnableHibernateCache() {
        return enableHibernateCache;
    }

    public void setEnableHibernateCache(Boolean enableHibernateCache) {
        this.enableHibernateCache = enableHibernateCache;
    }

    public String getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(String searchEngine) {
        this.searchEngine = searchEngine;
    }

    public String getMessageBroker() {
        return messageBroker;
    }

    public void setMessageBroker(String messageBroker) {
        this.messageBroker = messageBroker;
    }

    public String getWebsocket() {
        return websocket;
    }

    public void setWebsocket(String websocket) {
        this.websocket = websocket;
    }

    public Boolean getSkipClient() {
        return skipClient;
    }

    public void setSkipClient(Boolean skipClient) {
        this.skipClient = skipClient;
    }

    public String getClientFramework() {
        return clientFramework;
    }

    public void setClientFramework(String clientFramework) {
        this.clientFramework = clientFramework;
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

    public String[] getTestFrameworks() {
        return testFrameworks;
    }

    public void setTestFrameworks(String[] testFrameworks) {
        this.testFrameworks = testFrameworks;
    }

    public String getDto() {
        return dto;
    }

    public void setDto(String dto) {
        this.dto = dto;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPagination() {
        return pagination;
    }

    public void setPagination(String pagination) {
        this.pagination = pagination;
    }

    public String getJhiPrefix() {
        return jhiPrefix;
    }

    public void setJhiPrefix(String jhiPrefix) {
        this.jhiPrefix = jhiPrefix;
    }

    public String getEntitySuffix() {
        return entitySuffix;
    }

    public void setEntitySuffix(String entitySuffix) {
        this.entitySuffix = entitySuffix;
    }

    public String getDtoSuffix() {
        return dtoSuffix;
    }

    public void setDtoSuffix(String dtoSuffix) {
        this.dtoSuffix = dtoSuffix;
    }

    public String getJwtSecretKey() {
        return jwtSecretKey;
    }

    public void setJwtSecretKey(String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    // Computed properties

    public String getMainClass() {
        return capitalize(baseName) + "App";
    }

    public String getLowerBaseName() {
        return baseName.toLowerCase();
    }

    public String getUpperBaseName() {
        return baseName.toUpperCase();
    }

    public String getCapitalizedBaseName() {
        return capitalize(baseName);
    }

    public boolean isMicroservice() {
        return "microservice".equals(applicationType);
    }

    public boolean isGateway() {
        return "gateway".equals(applicationType);
    }

    public boolean isMonolith() {
        return "monolith".equals(applicationType);
    }

    public boolean isSql() {
        return "sql".equals(databaseType);
    }

    public boolean isJwt() {
        return "jwt".equals(authenticationType);
    }

    public boolean isOauth2() {
        return "oauth2".equals(authenticationType);
    }

    public boolean hasServiceDiscovery() {
        return serviceDiscoveryType != null && !"no".equals(serviceDiscoveryType);
    }

    public boolean isConsul() {
        return "consul".equals(serviceDiscoveryType);
    }

    public boolean isEureka() {
        return "eureka".equals(serviceDiscoveryType);
    }

    public String getEndpointPrefix() {
        if (isMicroservice()) {
            return "services/" + getLowerBaseName();
        }
        return "";
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
