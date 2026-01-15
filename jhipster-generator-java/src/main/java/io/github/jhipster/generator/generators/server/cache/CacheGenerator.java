/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.server.cache;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.template.JavaCodeBuilder;

/**
 * Cache Generator.
 * Generates cache configuration for Ehcache, Caffeine, Redis, or Hazelcast.
 */
public class CacheGenerator extends BaseApplicationGenerator {

    public CacheGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "cache";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingCache", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing cache configuration");

        JHipsterConfig config = getConfig();
        String cacheProvider = config.getCacheProvider();

        if (cacheProvider == null || "no".equals(cacheProvider)) {
            log.info("No cache provider configured, skipping cache generation");
            return;
        }

        switch (cacheProvider) {
            case "ehcache":
                writeEhcacheConfiguration();
                break;
            case "caffeine":
                writeCaffeineConfiguration();
                break;
            case "redis":
                writeRedisConfiguration();
                break;
            case "hazelcast":
                writeHazelcastConfiguration();
                break;
            default:
                log.warn("Unknown cache provider: {}", cacheProvider);
        }

        writeCacheConfiguration();
    }

    private void writeCacheConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer",
            "org.springframework.cache.annotation.EnableCaching",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "tech.jhipster.config.JHipsterProperties"
        );

        String cacheProvider = config.getCacheProvider();
        if ("ehcache".equals(cacheProvider)) {
            builder.addImports(
                "org.hibernate.cache.jcache.ConfigSettings",
                "org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer"
            );
        } else if ("hazelcast".equals(cacheProvider)) {
            builder.addImports(
                "com.hazelcast.config.Config",
                "com.hazelcast.config.EvictionConfig",
                "com.hazelcast.config.EvictionPolicy",
                "com.hazelcast.config.MapConfig",
                "com.hazelcast.config.MaxSizePolicy",
                "com.hazelcast.core.Hazelcast",
                "com.hazelcast.core.HazelcastInstance"
            );
        }

        builder.javadoc("Cache configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableCaching");

        builder.classDeclaration("public", "CacheConfiguration", null);

        builder.field("private final", "JHipsterProperties", "jHipsterProperties");
        builder.line();

        builder.constructor("public", "CacheConfiguration", "JHipsterProperties jHipsterProperties");
        builder.statement("this.jHipsterProperties = jHipsterProperties");
        builder.closeMethod();

        if ("ehcache".equals(cacheProvider)) {
            writeEhcacheBeans(builder);
        } else if ("caffeine".equals(cacheProvider)) {
            writeCaffeineBeans(builder);
        } else if ("hazelcast".equals(cacheProvider)) {
            writeHazelcastBeans(builder);
        } else if ("redis".equals(cacheProvider)) {
            writeRedisBeans(builder);
        }

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/CacheConfiguration.java", builder.build());
    }

    private void writeEhcacheConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("        xmlns=\"http://www.ehcache.org/v3\"\n");
        xml.append("        xsi:schemaLocation=\"http://www.ehcache.org/v3 http://www.ehcache.org/schema/ehcache-core-3.0.xsd\">\n");
        xml.append("\n");
        xml.append("    <cache-template name=\"default\">\n");
        xml.append("        <expiry>\n");
        xml.append("            <ttl unit=\"seconds\">3600</ttl>\n");
        xml.append("        </expiry>\n");
        xml.append("        <heap unit=\"entries\">100</heap>\n");
        xml.append("    </cache-template>\n");
        xml.append("\n");
        xml.append("    <!-- Add your caches here -->\n");
        xml.append("    <cache alias=\"").append(config.getPackageName()).append(".domain.User\" uses-template=\"default\"/>\n");
        xml.append("    <cache alias=\"").append(config.getPackageName()).append(".domain.Authority\" uses-template=\"default\"/>\n");
        xml.append("    <cache alias=\"usersByLogin\" uses-template=\"default\"/>\n");
        xml.append("    <cache alias=\"usersByEmail\" uses-template=\"default\"/>\n");
        xml.append("\n");
        xml.append("</config>\n");

        writeFile(getMainResourcesPath() + "config/ehcache/ehcache.xml", xml.toString());
    }

    private void writeCaffeineConfiguration() throws Exception {
        // Caffeine is configured programmatically, no additional files needed
    }

    private void writeRedisConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "org.springframework.boot.autoconfigure.data.redis.RedisProperties",
            "org.springframework.cache.annotation.EnableCaching",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.data.redis.cache.RedisCacheConfiguration",
            "org.springframework.data.redis.cache.RedisCacheManager",
            "org.springframework.data.redis.connection.RedisConnectionFactory",
            "org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer",
            "org.springframework.data.redis.serializer.RedisSerializationContext",
            "java.time.Duration"
        );

        builder.javadoc("Redis cache configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableCaching");

        builder.classDeclaration("public", "RedisCacheConfiguration", null);

        builder.annotation("Bean");
        builder.methodSignature("public", "RedisCacheManager", "cacheManager", "RedisConnectionFactory connectionFactory");
        builder.statement("org.springframework.data.redis.cache.RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()\n" +
            "            .entryTtl(Duration.ofMinutes(60))\n" +
            "            .disableCachingNullValues()\n" +
            "            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))");
        builder.returnStatement("RedisCacheManager.builder(connectionFactory)\n" +
            "            .cacheDefaults(cacheConfig)\n" +
            "            .transactionAware()\n" +
            "            .build()");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/RedisCacheConfiguration.java", builder.build());
    }

    private void writeHazelcastConfiguration() throws Exception {
        // Hazelcast is configured programmatically in CacheConfiguration
    }

    private void writeEhcacheBeans(JavaCodeBuilder builder) throws Exception {
        JHipsterConfig config = getConfig();

        builder.line();
        builder.annotation("Bean");
        builder.methodSignature("public", "HibernatePropertiesCustomizer", "hibernatePropertiesCustomizer",
            "javax.cache.CacheManager cacheManager");
        builder.returnStatement("hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager)");
        builder.closeMethod();
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "JCacheManagerCustomizer", "cacheManagerCustomizer");
        builder.returnStatement("cm -> {\n" +
            "            // Configure caches here\n" +
            "        }");
        builder.closeMethod();
    }

    private void writeCaffeineBeans(JavaCodeBuilder builder) throws Exception {
        builder.addImports(
            "com.github.benmanes.caffeine.cache.Caffeine",
            "org.springframework.cache.CacheManager",
            "org.springframework.cache.caffeine.CaffeineCacheManager",
            "java.util.concurrent.TimeUnit"
        );

        builder.line();
        builder.annotation("Bean");
        builder.methodSignature("public", "CacheManager", "cacheManager");
        builder.statement("CaffeineCacheManager cacheManager = new CaffeineCacheManager()");
        builder.statement("cacheManager.setCaffeine(Caffeine.newBuilder()\n" +
            "            .expireAfterWrite(jHipsterProperties.getCache().getCaffeine().getTimeToLiveSeconds(), TimeUnit.SECONDS)\n" +
            "            .maximumSize(jHipsterProperties.getCache().getCaffeine().getMaxEntries()))");
        builder.returnStatement("cacheManager");
        builder.closeMethod();
    }

    private void writeHazelcastBeans(JavaCodeBuilder builder) throws Exception {
        builder.line();
        builder.annotation("Bean");
        builder.methodSignature("public", "HazelcastInstance", "hazelcastInstance", "JHipsterProperties jHipsterProperties");
        builder.statement("Config config = new Config()");
        builder.statement("config.setInstanceName(\"" + getConfig().getBaseName() + "\")");
        builder.statement("config.getNetworkConfig().setPort(5701)");
        builder.statement("config.getNetworkConfig().setPortAutoIncrement(true)");
        builder.line();
        builder.statement("// Configure map\n" +
            "        MapConfig mapConfig = new MapConfig()\n" +
            "            .setName(\"default\")\n" +
            "            .setEvictionConfig(new EvictionConfig()\n" +
            "                .setEvictionPolicy(EvictionPolicy.LRU)\n" +
            "                .setMaxSizePolicy(MaxSizePolicy.PER_NODE)\n" +
            "                .setSize(100))");
        builder.statement("config.addMapConfig(mapConfig)");
        builder.line();
        builder.returnStatement("Hazelcast.newHazelcastInstance(config)");
        builder.closeMethod();
    }

    private void writeRedisBeans(JavaCodeBuilder builder) throws Exception {
        builder.addImports(
            "org.springframework.data.redis.connection.RedisConnectionFactory",
            "org.springframework.data.redis.core.RedisTemplate",
            "org.springframework.data.redis.serializer.StringRedisSerializer"
        );

        builder.line();
        builder.annotation("Bean");
        builder.methodSignature("public", "RedisTemplate<String, Object>", "redisTemplate",
            "RedisConnectionFactory connectionFactory");
        builder.statement("RedisTemplate<String, Object> template = new RedisTemplate<>()");
        builder.statement("template.setConnectionFactory(connectionFactory)");
        builder.statement("template.setKeySerializer(new StringRedisSerializer())");
        builder.returnStatement("template");
        builder.closeMethod();
    }

    private String getMainResourcesPath() {
        return context.getBasePath().toString() + "/src/main/resources/";
    }
}
