/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.generators.liquibase;

import io.github.jhipster.generator.config.GeneratorContext;
import io.github.jhipster.generator.config.JHipsterConfig;
import io.github.jhipster.generator.core.BaseApplicationGenerator;
import io.github.jhipster.generator.core.GeneratorPriority;
import io.github.jhipster.generator.model.EntityConfig;
import io.github.jhipster.generator.model.FieldConfig;
import io.github.jhipster.generator.model.RelationshipConfig;
import io.github.jhipster.generator.template.JavaCodeBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Liquibase Generator.
 * Generates Liquibase changelogs for database migrations.
 */
public class LiquibaseGenerator extends BaseApplicationGenerator {

    private static final DateTimeFormatter CHANGELOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public LiquibaseGenerator(GeneratorContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "liquibase";
    }

    @Override
    protected void registerTasks() {
        super.registerTasks();

        registerTask(GeneratorPriority.WRITING, "writingLiquibase", this::writing);
    }

    private void writing() throws Exception {
        log.info("Writing Liquibase configuration");

        writeLiquibaseConfiguration();
        writeMasterChangelog();
        writeInitialSchema();
        writeUserChangelog();
        writeAuthorityChangelog();
        writeAuditEventChangelog();

        // Generate changelogs for all entities
        List<EntityConfig> entities = context.getEntities();
        for (EntityConfig entity : entities) {
            if (!entity.isBuiltIn()) {
                writeEntityChangelog(entity);
            }
        }
    }

    private void writeLiquibaseConfiguration() throws Exception {
        JHipsterConfig config = getConfig();

        JavaCodeBuilder builder = new JavaCodeBuilder();
        builder.packageDeclaration(config.getPackageName() + ".config");

        builder.addImports(
            "liquibase.integration.spring.SpringLiquibase",
            "org.springframework.beans.factory.annotation.Qualifier",
            "org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties",
            "org.springframework.boot.context.properties.EnableConfigurationProperties",
            "org.springframework.context.annotation.Bean",
            "org.springframework.context.annotation.Configuration",
            "org.springframework.core.env.Environment",
            "org.springframework.core.env.Profiles",
            "tech.jhipster.config.JHipsterConstants",
            "tech.jhipster.config.liquibase.AsyncSpringLiquibase",
            "javax.sql.DataSource",
            "java.util.concurrent.Executor"
        );

        builder.javadoc("Liquibase configuration.");
        builder.annotation("Configuration");
        builder.annotation("EnableConfigurationProperties", "LiquibaseProperties.class");

        builder.classDeclaration("public", "LiquibaseConfiguration", null);

        builder.field("private final", "Environment", "env");
        builder.line();

        builder.constructor("public", "LiquibaseConfiguration", "Environment env");
        builder.statement("this.env = env");
        builder.closeMethod();
        builder.line();

        builder.annotation("Bean");
        builder.methodSignature("public", "SpringLiquibase", "liquibase",
            "@Qualifier(\"taskExecutor\") Executor executor",
            "DataSource dataSource",
            "LiquibaseProperties liquibaseProperties");
        builder.statement("SpringLiquibase liquibase = new AsyncSpringLiquibase(executor, env)");
        builder.statement("liquibase.setDataSource(dataSource)");
        builder.statement("liquibase.setChangeLog(\"classpath:config/liquibase/master.xml\")");
        builder.statement("liquibase.setContexts(liquibaseProperties.getContexts())");
        builder.statement("liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema())");
        builder.statement("liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema())");
        builder.statement("liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace())");
        builder.statement("liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable())");
        builder.statement("liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable())");
        builder.statement("liquibase.setDropFirst(liquibaseProperties.isDropFirst())");
        builder.statement("liquibase.setChangeLogParameters(liquibaseProperties.getParameters())");
        builder.statement("liquibase.setRollbackFile(liquibaseProperties.getRollbackFile())");
        builder.statement("liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate())");
        builder.ifStatement("env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE))");
        builder.statement("liquibase.setShouldRun(false)");
        builder.elseStatement();
        builder.statement("liquibase.setShouldRun(liquibaseProperties.isEnabled())");
        builder.closeIf();
        builder.returnStatement("liquibase");
        builder.closeMethod();

        builder.closeClass();

        writeFile(getMainJavaPath() + "config/LiquibaseConfiguration.java", builder.build());
    }

    private void writeMasterChangelog() throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<databaseChangeLog\n");
        xml.append("    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n");
        xml.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n");
        xml.append("                        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n");
        xml.append("\n");
        xml.append("    <property name=\"now\" value=\"now()\" dbms=\"h2,mysql,mariadb\"/>\n");
        xml.append("    <property name=\"now\" value=\"current_timestamp\" dbms=\"postgresql\"/>\n");
        xml.append("    <property name=\"floatType\" value=\"float4\" dbms=\"postgresql,h2\"/>\n");
        xml.append("    <property name=\"floatType\" value=\"float\" dbms=\"mysql,mariadb\"/>\n");
        xml.append("    <property name=\"clobType\" value=\"clob\" dbms=\"h2,mysql,mariadb,postgresql\"/>\n");
        xml.append("    <property name=\"blobType\" value=\"blob\" dbms=\"h2,mysql,mariadb\"/>\n");
        xml.append("    <property name=\"blobType\" value=\"bytea\" dbms=\"postgresql\"/>\n");
        xml.append("    <property name=\"uuidType\" value=\"uuid\" dbms=\"h2,postgresql\"/>\n");
        xml.append("    <property name=\"uuidType\" value=\"varchar(36)\" dbms=\"mysql,mariadb\"/>\n");
        xml.append("\n");
        xml.append("    <include file=\"config/liquibase/changelog/00000000000000_initial_schema.xml\" relativeToChangelogFile=\"false\"/>\n");
        xml.append("\n");
        xml.append("    <!-- jhipster-needle-liquibase-add-changelog - JHipster will add liquibase changelogs here -->\n");
        xml.append("\n");
        xml.append("    <!-- jhipster-needle-liquibase-add-constraints-changelog - JHipster will add liquibase constraints changelogs here -->\n");
        xml.append("\n");
        xml.append("</databaseChangeLog>\n");

        writeFile(getLiquibasePath() + "master.xml", xml.toString());
    }

    private void writeInitialSchema() throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<databaseChangeLog\n");
        xml.append("    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n");
        xml.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n");
        xml.append("                        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n");
        xml.append("\n");

        // User table
        xml.append("    <changeSet id=\"00000000000000\" author=\"jhipster\">\n");
        xml.append("        <createSequence sequenceName=\"sequence_generator\" startValue=\"1050\" incrementBy=\"50\"/>\n");
        xml.append("    </changeSet>\n\n");

        xml.append("    <changeSet id=\"00000000000001\" author=\"jhipster\">\n");
        xml.append("        <createTable tableName=\"jhi_user\">\n");
        xml.append("            <column name=\"id\" type=\"bigint\">\n");
        xml.append("                <constraints primaryKey=\"true\" nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"login\" type=\"varchar(50)\">\n");
        xml.append("                <constraints unique=\"true\" nullable=\"false\" uniqueConstraintName=\"ux_user_login\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"password_hash\" type=\"varchar(60)\"/>\n");
        xml.append("            <column name=\"first_name\" type=\"varchar(50)\"/>\n");
        xml.append("            <column name=\"last_name\" type=\"varchar(50)\"/>\n");
        xml.append("            <column name=\"email\" type=\"varchar(191)\">\n");
        xml.append("                <constraints unique=\"true\" nullable=\"true\" uniqueConstraintName=\"ux_user_email\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"image_url\" type=\"varchar(256)\"/>\n");
        xml.append("            <column name=\"activated\" type=\"boolean\" valueBoolean=\"false\">\n");
        xml.append("                <constraints nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"lang_key\" type=\"varchar(10)\"/>\n");
        xml.append("            <column name=\"activation_key\" type=\"varchar(20)\"/>\n");
        xml.append("            <column name=\"reset_key\" type=\"varchar(20)\"/>\n");
        xml.append("            <column name=\"reset_date\" type=\"timestamp\"/>\n");
        xml.append("            <column name=\"created_by\" type=\"varchar(50)\">\n");
        xml.append("                <constraints nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"created_date\" type=\"timestamp\"/>\n");
        xml.append("            <column name=\"last_modified_by\" type=\"varchar(50)\"/>\n");
        xml.append("            <column name=\"last_modified_date\" type=\"timestamp\"/>\n");
        xml.append("        </createTable>\n");
        xml.append("    </changeSet>\n\n");

        // Authority table
        xml.append("    <changeSet id=\"00000000000002\" author=\"jhipster\">\n");
        xml.append("        <createTable tableName=\"jhi_authority\">\n");
        xml.append("            <column name=\"name\" type=\"varchar(50)\">\n");
        xml.append("                <constraints primaryKey=\"true\" nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("        </createTable>\n");
        xml.append("    </changeSet>\n\n");

        // User-Authority join table
        xml.append("    <changeSet id=\"00000000000003\" author=\"jhipster\">\n");
        xml.append("        <createTable tableName=\"jhi_user_authority\">\n");
        xml.append("            <column name=\"user_id\" type=\"bigint\">\n");
        xml.append("                <constraints nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"authority_name\" type=\"varchar(50)\">\n");
        xml.append("                <constraints nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("        </createTable>\n");
        xml.append("\n");
        xml.append("        <addPrimaryKey columnNames=\"user_id, authority_name\" tableName=\"jhi_user_authority\"/>\n");
        xml.append("\n");
        xml.append("        <addForeignKeyConstraint baseColumnNames=\"authority_name\"\n");
        xml.append("                                 baseTableName=\"jhi_user_authority\"\n");
        xml.append("                                 constraintName=\"fk_authority_name\"\n");
        xml.append("                                 referencedColumnNames=\"name\"\n");
        xml.append("                                 referencedTableName=\"jhi_authority\"/>\n");
        xml.append("\n");
        xml.append("        <addForeignKeyConstraint baseColumnNames=\"user_id\"\n");
        xml.append("                                 baseTableName=\"jhi_user_authority\"\n");
        xml.append("                                 constraintName=\"fk_user_id\"\n");
        xml.append("                                 referencedColumnNames=\"id\"\n");
        xml.append("                                 referencedTableName=\"jhi_user\"/>\n");
        xml.append("    </changeSet>\n\n");

        // Persistent Audit Event table
        xml.append("    <changeSet id=\"00000000000004\" author=\"jhipster\">\n");
        xml.append("        <createTable tableName=\"jhi_persistent_audit_event\">\n");
        xml.append("            <column name=\"event_id\" type=\"bigint\">\n");
        xml.append("                <constraints primaryKey=\"true\" nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"principal\" type=\"varchar(50)\">\n");
        xml.append("                <constraints nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"event_date\" type=\"timestamp\"/>\n");
        xml.append("            <column name=\"event_type\" type=\"varchar(255)\"/>\n");
        xml.append("        </createTable>\n");
        xml.append("\n");
        xml.append("        <createTable tableName=\"jhi_persistent_audit_evt_data\">\n");
        xml.append("            <column name=\"event_id\" type=\"bigint\">\n");
        xml.append("                <constraints nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"name\" type=\"varchar(150)\">\n");
        xml.append("                <constraints nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"value\" type=\"varchar(255)\"/>\n");
        xml.append("        </createTable>\n");
        xml.append("\n");
        xml.append("        <addPrimaryKey columnNames=\"event_id, name\" tableName=\"jhi_persistent_audit_evt_data\"/>\n");
        xml.append("\n");
        xml.append("        <createIndex indexName=\"idx_persistent_audit_event\" tableName=\"jhi_persistent_audit_event\">\n");
        xml.append("            <column name=\"principal\"/>\n");
        xml.append("            <column name=\"event_date\"/>\n");
        xml.append("        </createIndex>\n");
        xml.append("\n");
        xml.append("        <createIndex indexName=\"idx_persistent_audit_evt_data\" tableName=\"jhi_persistent_audit_evt_data\">\n");
        xml.append("            <column name=\"event_id\"/>\n");
        xml.append("        </createIndex>\n");
        xml.append("\n");
        xml.append("        <addForeignKeyConstraint baseColumnNames=\"event_id\"\n");
        xml.append("                                 baseTableName=\"jhi_persistent_audit_evt_data\"\n");
        xml.append("                                 constraintName=\"fk_evt_pers_audit_evt_data\"\n");
        xml.append("                                 referencedColumnNames=\"event_id\"\n");
        xml.append("                                 referencedTableName=\"jhi_persistent_audit_event\"/>\n");
        xml.append("    </changeSet>\n\n");

        // Load initial data
        xml.append("    <changeSet id=\"00000000000005\" author=\"jhipster\" context=\"faker\">\n");
        xml.append("        <loadData file=\"config/liquibase/data/authority.csv\"\n");
        xml.append("                  separator=\";\"\n");
        xml.append("                  tableName=\"jhi_authority\">\n");
        xml.append("            <column name=\"name\" type=\"string\"/>\n");
        xml.append("        </loadData>\n");
        xml.append("    </changeSet>\n\n");

        xml.append("    <changeSet id=\"00000000000006\" author=\"jhipster\" context=\"faker\">\n");
        xml.append("        <loadData file=\"config/liquibase/data/user.csv\"\n");
        xml.append("                  separator=\";\"\n");
        xml.append("                  tableName=\"jhi_user\">\n");
        xml.append("            <column name=\"id\" type=\"numeric\"/>\n");
        xml.append("            <column name=\"activated\" type=\"boolean\"/>\n");
        xml.append("            <column name=\"created_date\" type=\"timestamp\"/>\n");
        xml.append("        </loadData>\n");
        xml.append("    </changeSet>\n\n");

        xml.append("    <changeSet id=\"00000000000007\" author=\"jhipster\" context=\"faker\">\n");
        xml.append("        <loadData file=\"config/liquibase/data/user_authority.csv\"\n");
        xml.append("                  separator=\";\"\n");
        xml.append("                  tableName=\"jhi_user_authority\">\n");
        xml.append("            <column name=\"user_id\" type=\"numeric\"/>\n");
        xml.append("        </loadData>\n");
        xml.append("    </changeSet>\n");

        xml.append("\n</databaseChangeLog>\n");

        writeFile(getLiquibasePath() + "changelog/00000000000000_initial_schema.xml", xml.toString());
    }

    private void writeUserChangelog() throws Exception {
        // Write user CSV data
        StringBuilder csv = new StringBuilder();
        csv.append("id;login;password_hash;first_name;last_name;email;image_url;activated;lang_key;created_by;created_date;last_modified_by;last_modified_date\n");
        csv.append("1;admin;$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC;Administrator;Administrator;admin@localhost;;true;en;system;2024-01-01T00:00:00Z;system;null\n");
        csv.append("2;user;$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7F2YVqiLi;User;User;user@localhost;;true;en;system;2024-01-01T00:00:00Z;system;null\n");

        writeFile(getLiquibasePath() + "data/user.csv", csv.toString());
    }

    private void writeAuthorityChangelog() throws Exception {
        // Write authority CSV data
        StringBuilder csv = new StringBuilder();
        csv.append("name\n");
        csv.append("ROLE_ADMIN\n");
        csv.append("ROLE_USER\n");

        writeFile(getLiquibasePath() + "data/authority.csv", csv.toString());

        // Write user_authority CSV data
        StringBuilder userAuthorityCsv = new StringBuilder();
        userAuthorityCsv.append("user_id;authority_name\n");
        userAuthorityCsv.append("1;ROLE_ADMIN\n");
        userAuthorityCsv.append("1;ROLE_USER\n");
        userAuthorityCsv.append("2;ROLE_USER\n");

        writeFile(getLiquibasePath() + "data/user_authority.csv", userAuthorityCsv.toString());
    }

    private void writeAuditEventChangelog() throws Exception {
        // Audit event tables are included in initial schema
    }

    public void writeEntityChangelog(EntityConfig entity) throws Exception {
        String timestamp = LocalDateTime.now().format(CHANGELOG_DATE_FORMAT);
        String changelogFile = timestamp + "_added_entity_" + entity.getName() + ".xml";

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<databaseChangeLog\n");
        xml.append("    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n");
        xml.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n");
        xml.append("                        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n");
        xml.append("\n");

        String tableName = camelToSnake(entity.getName());

        xml.append("    <changeSet id=\"").append(timestamp).append("-1\" author=\"jhipster\">\n");
        xml.append("        <createTable tableName=\"").append(tableName).append("\">\n");

        // Primary key
        xml.append("            <column name=\"id\" type=\"bigint\">\n");
        xml.append("                <constraints primaryKey=\"true\" nullable=\"false\"/>\n");
        xml.append("            </column>\n");

        // Fields
        for (FieldConfig field : entity.getFields()) {
            String columnName = camelToSnake(field.getFieldName());
            String columnType = getColumnType(field);

            xml.append("            <column name=\"").append(columnName).append("\" type=\"").append(columnType).append("\"");

            if (field.isRequired()) {
                xml.append(">\n");
                xml.append("                <constraints nullable=\"false\"/>\n");
                xml.append("            </column>\n");
            } else {
                xml.append("/>\n");
            }
        }

        // Audit columns if entity extends AbstractAuditingEntity
        xml.append("            <column name=\"created_by\" type=\"varchar(50)\">\n");
        xml.append("                <constraints nullable=\"false\"/>\n");
        xml.append("            </column>\n");
        xml.append("            <column name=\"created_date\" type=\"timestamp\"/>\n");
        xml.append("            <column name=\"last_modified_by\" type=\"varchar(50)\"/>\n");
        xml.append("            <column name=\"last_modified_date\" type=\"timestamp\"/>\n");

        xml.append("        </createTable>\n");
        xml.append("    </changeSet>\n");

        // Add constraints for relationships in a separate changeset
        if (!entity.getRelationships().isEmpty()) {
            xml.append("\n");
            xml.append("    <changeSet id=\"").append(timestamp).append("-2\" author=\"jhipster\">\n");

            for (RelationshipConfig rel : entity.getRelationships()) {
                if ("many-to-one".equals(rel.getRelationshipType()) ||
                    ("one-to-one".equals(rel.getRelationshipType()) && rel.isOwnerSide())) {
                    String columnName = camelToSnake(rel.getRelationshipName()) + "_id";
                    String refTableName = camelToSnake(rel.getOtherEntityName());

                    xml.append("        <addColumn tableName=\"").append(tableName).append("\">\n");
                    xml.append("            <column name=\"").append(columnName).append("\" type=\"bigint\"/>\n");
                    xml.append("        </addColumn>\n");
                    xml.append("\n");
                    xml.append("        <addForeignKeyConstraint baseColumnNames=\"").append(columnName).append("\"\n");
                    xml.append("                                 baseTableName=\"").append(tableName).append("\"\n");
                    xml.append("                                 constraintName=\"fk_").append(tableName).append("_").append(columnName).append("\"\n");
                    xml.append("                                 referencedColumnNames=\"id\"\n");
                    xml.append("                                 referencedTableName=\"").append(refTableName).append("\"/>\n");
                }
            }

            xml.append("    </changeSet>\n");
        }

        xml.append("\n</databaseChangeLog>\n");

        writeFile(getLiquibasePath() + "changelog/" + changelogFile, xml.toString());
    }

    private String getColumnType(FieldConfig field) {
        String type = field.getFieldType();
        switch (type) {
            case "String":
                int maxLength = field.getMaxLength() != null ? field.getMaxLength() : 255;
                return "varchar(" + maxLength + ")";
            case "Integer":
                return "integer";
            case "Long":
                return "bigint";
            case "Float":
                return "${floatType}";
            case "Double":
                return "double";
            case "BigDecimal":
                return "decimal(21,2)";
            case "Boolean":
                return "boolean";
            case "LocalDate":
                return "date";
            case "Instant":
            case "ZonedDateTime":
                return "timestamp";
            case "Duration":
                return "bigint";
            case "UUID":
                return "${uuidType}";
            case "byte[]":
                return "${blobType}";
            case "TextBlob":
                return "${clobType}";
            default:
                if (field.isEnum()) {
                    return "varchar(255)";
                }
                return "varchar(255)";
        }
    }

    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private String getLiquibasePath() {
        return context.getBasePath().toString() + "/src/main/resources/config/liquibase/";
    }
}
