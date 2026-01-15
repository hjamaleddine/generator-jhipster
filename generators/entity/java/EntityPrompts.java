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

import io.github.jhipster.generator.entity.enums.*;
import io.github.jhipster.generator.entity.model.EntityConfig;
import io.github.jhipster.generator.entity.model.EntityData;
import io.github.jhipster.generator.entity.model.FieldConfig;
import io.github.jhipster.generator.entity.model.RelationshipConfig;
import io.github.jhipster.generator.entity.support.EntityValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Interactive prompts for entity generation.
 * Handles user input for creating and modifying entities.
 */
public class EntityPrompts {

    private final BufferedReader reader;
    private final PrintStream out;
    private final EntityConfig entityConfig;
    private final EntityData entityData;
    private final List<String> existingEntities;

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BOLD = "\u001B[1m";

    public EntityPrompts(EntityConfig entityConfig, EntityData entityData, List<String> existingEntities) {
        this(entityConfig, entityData, existingEntities,
             new BufferedReader(new InputStreamReader(System.in)), System.out);
    }

    public EntityPrompts(EntityConfig entityConfig, EntityData entityData, List<String> existingEntities,
                        BufferedReader reader, PrintStream out) {
        this.entityConfig = entityConfig;
        this.entityData = entityData;
        this.existingEntities = existingEntities != null ? existingEntities : new ArrayList<>();
        this.reader = reader;
        this.out = out;
    }

    /**
     * Asks for microservice JSON path if running in gateway mode.
     */
    public void askForMicroserviceJson(ApplicationType applicationType, DatabaseType databaseType) throws IOException {
        if (applicationType != ApplicationType.GATEWAY || entityData.isConfigExisted()) {
            return;
        }

        if (databaseType != DatabaseType.NO) {
            boolean useMicroserviceJson = askYesNo(
                "Do you want to generate this entity from an existing microservice?", true);
            if (!useMicroserviceJson) {
                return;
            }
        }

        String microservicePath = askInput(
            "Enter the path to the microservice root directory:",
            entityConfig.getMicroservicePath());

        entityConfig.setMicroservicePath(microservicePath);
        entityData.setMicroservicePath(microservicePath);
        print(GREEN + "\nFound the configuration file, entity can be automatically generated!\n" + RESET);
    }

    /**
     * Asks user what they want to do with an existing entity.
     */
    public void askForUpdate(boolean force) throws IOException {
        if (force || entityData.isRegenerate() || !entityData.isUseConfigurationFile()) {
            entityData.setUpdateEntity("regenerate");
            return;
        }

        print("\n" + WHITE + "================= " + RESET + entityData.getName() + WHITE + " =================" + RESET);

        int choice = askChoice(
            "Do you want to update the entity? This will replace the existing files for this entity, all your custom code will be overwritten",
            Arrays.asList(
                "Yes, re generate the entity",
                "Yes, add more fields and relationships",
                "Yes, remove fields and relationships",
                "No, exit"
            ),
            0);

        switch (choice) {
            case 0:
                entityData.setUpdateEntity("regenerate");
                break;
            case 1:
                entityData.setUpdateEntity("add");
                break;
            case 2:
                entityData.setUpdateEntity("remove");
                break;
            case 3:
                entityData.setUpdateEntity("none");
                throw new EntityGenerationAbortedException("Aborting entity update, no changes were made.");
        }
    }

    /**
     * Asks for fields to add to the entity.
     */
    public void askForFields(boolean useDefaults) throws IOException {
        if (useDefaults || (entityData.isUseConfigurationFile() && !entityData.shouldAddFields())) {
            return;
        }

        if (entityData.shouldAddFields()) {
            logFieldsAndRelationships();
        }

        askForField();
    }

    /**
     * Recursively asks for field definitions.
     */
    private void askForField() throws IOException {
        print(GREEN + "\nGenerating field #" + (entityConfig.getFields().size() + 1) + "\n" + RESET);

        boolean addField = askYesNo("Do you want to add a field to your entity?", true);
        if (!addField) {
            logFieldsAndRelationships();
            return;
        }

        // Ask for field name
        Set<String> existingFieldNames = getExistingFieldNames();
        String fieldName = askInputWithValidation(
            "What is the name of your field?",
            input -> EntityValidator.validateFieldName(
                input, existingFieldNames,
                entityData.getClientFramework(),
                entityData.getDatabaseType() == DatabaseType.SQL && !entityData.isReactive()
            ).orElse(null));

        // Ask for field type
        FieldType fieldType = askFieldType();

        FieldConfig.FieldConfigBuilder fieldBuilder = FieldConfig.builder()
            .fieldName(fieldName)
            .fieldType(fieldType);

        // Handle enum type
        if (fieldType == FieldType.ENUM) {
            askForEnumDetails(fieldBuilder);
        }

        // Handle blob type
        if (fieldType == FieldType.BYTES || fieldType == FieldType.BYTE_BUFFER) {
            BlobType blobType = askBlobType(fieldType == FieldType.BYTES);
            fieldBuilder.blobContent(blobType);
        }

        // Ask for validations (except for ByteBuffer)
        if (fieldType != FieldType.BYTE_BUFFER) {
            askForFieldValidations(fieldBuilder, fieldType);
        }

        entityConfig.addField(fieldBuilder.build());

        logFieldsAndRelationships();
        askForField();
    }

    /**
     * Asks for fields to remove from the entity.
     */
    public void askForFieldsToRemove() throws IOException {
        if (!entityData.isUseConfigurationFile() || !entityData.shouldRemoveFields()
            || entityConfig.getFields().isEmpty()) {
            return;
        }

        List<String> fieldNames = entityConfig.getFields().stream()
            .map(FieldConfig::getFieldName)
            .collect(Collectors.toList());

        List<String> fieldsToRemove = askMultipleChoice(
            "Please choose the fields you want to remove",
            fieldNames);

        if (fieldsToRemove.isEmpty()) {
            return;
        }

        boolean confirm = askYesNo("Are you sure to remove these fields?", true);
        if (confirm) {
            print(RED + "\nRemoving fields: " + String.join(", ", fieldsToRemove) + "\n" + RESET);
            fieldsToRemove.forEach(entityConfig::removeField);
        }
    }

    /**
     * Asks for relationships to add to the entity.
     */
    public void askForRelationships(ApplicationType applicationType, boolean generateBuiltInUserEntity) throws IOException {
        if (entityData.isUseConfigurationFile() && !entityData.shouldAddFields()) {
            return;
        }
        if (entityData.getDatabaseType() == DatabaseType.CASSANDRA) {
            return;
        }

        askForRelationship(applicationType, generateBuiltInUserEntity);
    }

    /**
     * Recursively asks for relationship definitions.
     */
    private void askForRelationship(ApplicationType applicationType, boolean generateBuiltInUserEntity) throws IOException {
        print(GREEN + "\nGenerating relationships to other entities\n" + RESET);

        boolean addRelationship = askYesNo("Do you want to add a relationship to another entity?", true);
        if (!addRelationship) {
            logFieldsAndRelationships();
            return;
        }

        // Build list of available entities
        List<String> availableEntities = new ArrayList<>(existingEntities);
        if (generateBuiltInUserEntity && !availableEntities.contains("User")) {
            availableEntities.add("User");
        }

        if (availableEntities.isEmpty()) {
            print(RED + "No other entities available for relationships.\n" + RESET);
            return;
        }

        // Ask for other entity
        String otherEntityName = askChoiceString(
            "What is the other entity?",
            availableEntities,
            0);

        // Ask for relationship name
        String defaultRelationshipName = Character.toLowerCase(otherEntityName.charAt(0)) + otherEntityName.substring(1);
        Set<String> existingFieldNames = getExistingFieldNames();
        String relationshipName = askInputWithValidation(
            "What is the name of the relationship?",
            defaultRelationshipName,
            input -> EntityValidator.validateRelationshipName(input, existingFieldNames).orElse(null));

        // Ask for relationship type
        List<String> relationshipTypes = new ArrayList<>(Arrays.asList(
            "many-to-one", "many-to-many", "one-to-one"));
        if (!isBuiltInUser(otherEntityName)) {
            relationshipTypes.add("one-to-many");
        }
        String relationshipType = askChoiceString(
            "What is the type of the relationship?",
            relationshipTypes,
            0);

        RelationshipConfig.RelationshipConfigBuilder relBuilder = RelationshipConfig.builder()
            .relationshipName(relationshipName)
            .otherEntityName(otherEntityName)
            .relationshipType(relationshipType)
            .leftSide();

        // Ask for @MapsId for one-to-one with SQL
        if ("one-to-one".equals(relationshipType) && entityData.getDatabaseType() == DatabaseType.SQL) {
            boolean useMapsId = askYesNo("Do you want to use JPA Derived Identifier - @MapsId?", false);
            relBuilder.useMapsId(useMapsId);
        }

        // Ask for bidirectional
        boolean askBidirectional = !isBuiltInUser(otherEntityName) && "many-to-one".equals(relationshipType);
        if (askBidirectional) {
            boolean bidirectional = askYesNo("Do you want to generate a bidirectional relationship?", true);
            relBuilder.bidirectional(bidirectional);
            if (bidirectional) {
                String otherRelName = askInput(
                    "What is the name of this relationship in the other entity?",
                    Character.toLowerCase(entityData.getName().charAt(0)) + entityData.getName().substring(1));
                relBuilder.otherEntityRelationshipName(otherRelName);
            }
        } else if (!isBuiltInUser(otherEntityName) && !"many-to-one".equals(relationshipType)) {
            relBuilder.bidirectional(true);
            String otherRelName = askInput(
                "What is the name of this relationship in the other entity?",
                Character.toLowerCase(entityData.getName().charAt(0)) + entityData.getName().substring(1));
            relBuilder.otherEntityRelationshipName(otherRelName);
        }

        // Ask for display field
        String defaultDisplayField = "User".equals(otherEntityName) ? "login" : "id";
        String otherEntityField = askInput(
            "When you display this relationship on client-side, which field from '" + otherEntityName + "' do you want to use?",
            defaultDisplayField);
        relBuilder.otherEntityField(otherEntityField);

        // Ask for validation
        if (!"one-to-many".equals(relationshipType)) {
            boolean addValidation = askYesNo("Do you want to add any validation rules to this relationship?", false);
            if (addValidation) {
                boolean required = askYesNo("Is this relationship required?", false);
                if (required) {
                    relBuilder.required();
                }
            }
        }

        entityConfig.addRelationship(relBuilder.build());

        askForRelationship(applicationType, generateBuiltInUserEntity);
    }

    /**
     * Asks for relationships to remove from the entity.
     */
    public void askForRelationsToRemove() throws IOException {
        if (!entityData.isUseConfigurationFile() || !entityData.shouldRemoveFields()
            || entityConfig.getRelationships().isEmpty()) {
            return;
        }
        if (entityData.getDatabaseType() == DatabaseType.CASSANDRA) {
            return;
        }

        List<String> relNames = entityConfig.getRelationships().stream()
            .map(r -> r.getRelationshipName() + ":" + r.getRelationshipType())
            .collect(Collectors.toList());

        List<String> relsToRemove = askMultipleChoice(
            "Please choose the relationships you want to remove",
            relNames);

        if (relsToRemove.isEmpty()) {
            return;
        }

        boolean confirm = askYesNo("Are you sure to remove these relationships?", true);
        if (confirm) {
            print(RED + "\nRemoving relationships: " + String.join(", ", relsToRemove) + "\n" + RESET);
            for (String rel : relsToRemove) {
                String[] parts = rel.split(":");
                entityConfig.removeRelationship(parts[0], parts[1]);
            }
        }
    }

    /**
     * Asks for service layer configuration.
     */
    public void askForService() throws IOException {
        if (entityData.isUseConfigurationFile() || entityData.isSkipServer()) {
            return;
        }

        int choice = askChoice(
            "Do you want to use separate service class for your business logic?",
            Arrays.asList(
                "No, the REST controller should use the repository directly",
                "Yes, generate a separate service class",
                "Yes, generate a separate service interface and implementation"
            ),
            0);

        switch (choice) {
            case 0:
                entityConfig.setService(ServiceType.NO.getValue());
                break;
            case 1:
                entityConfig.setService(ServiceType.SERVICE_CLASS.getValue());
                break;
            case 2:
                entityConfig.setService(ServiceType.SERVICE_IMPL.getValue());
                break;
        }
    }

    /**
     * Asks for DTO configuration.
     */
    public void askForDTO() throws IOException {
        if (entityData.isUseConfigurationFile() || entityData.isSkipServer()
            || ServiceType.NO.getValue().equals(entityConfig.getService())) {
            return;
        }

        int choice = askChoice(
            "Do you want to use a Data Transfer Object (DTO)?",
            Arrays.asList(
                "No, use the entity directly",
                "Yes, generate a DTO with MapStruct"
            ),
            0);

        entityConfig.setDto(choice == 0 ? MapperType.NO.getValue() : MapperType.MAPSTRUCT.getValue());
    }

    /**
     * Asks for filtering configuration.
     */
    public void askForFiltering() throws IOException {
        if (entityData.isUseConfigurationFile() || entityData.isSkipServer()
            || entityData.getDatabaseType() != DatabaseType.SQL
            || ServiceType.NO.getValue().equals(entityConfig.getService())) {
            return;
        }

        int choice = askChoice(
            "Do you want to add filtering?",
            Arrays.asList(
                "Not needed",
                "Dynamic filtering for the entities with JPA Static metamodel"
            ),
            0);

        entityConfig.setJpaMetamodelFiltering(choice == 1);
    }

    /**
     * Asks if entity should be read-only.
     */
    public void askForReadOnly() throws IOException {
        if (entityData.isUseConfigurationFile()) {
            return;
        }

        boolean readOnly = askYesNo("Is this entity read-only?", false);
        entityConfig.setReadOnly(readOnly);
    }

    /**
     * Asks for pagination configuration.
     */
    public void askForPagination() throws IOException {
        if (entityData.isUseConfigurationFile()) {
            return;
        }
        if (entityData.getDatabaseType() == DatabaseType.CASSANDRA) {
            return;
        }

        int choice = askChoice(
            "Do you want pagination and sorting on your entity?",
            Arrays.asList(
                "No",
                "Yes, with pagination links and sorting headers",
                "Yes, with infinite scroll and sorting headers"
            ),
            0);

        switch (choice) {
            case 0:
                entityConfig.setPagination(PaginationType.NO.getValue());
                break;
            case 1:
                entityConfig.setPagination(PaginationType.PAGINATION.getValue());
                break;
            case 2:
                entityConfig.setPagination(PaginationType.INFINITE_SCROLL.getValue());
                break;
        }

        print(GREEN + "\nEverything is configured, generating the entity...\n" + RESET);
    }

    // Helper methods

    private FieldType askFieldType() throws IOException {
        List<String> fieldTypes = Arrays.asList(
            "String", "Integer", "Long", "Float", "Double", "BigDecimal",
            "LocalDate", "Instant", "ZonedDateTime", "Duration", "Boolean",
            "Enumeration (Java enum type)", "UUID", "LocalTime",
            entityData.getDatabaseType() == DatabaseType.CASSANDRA ? "[BETA] Blob (ByteBuffer)" : "[BETA] Blob"
        );

        int choice = askChoice("What is the type of your field?", fieldTypes, 0);

        switch (choice) {
            case 0: return FieldType.STRING;
            case 1: return FieldType.INTEGER;
            case 2: return FieldType.LONG;
            case 3: return FieldType.FLOAT;
            case 4: return FieldType.DOUBLE;
            case 5: return FieldType.BIG_DECIMAL;
            case 6: return FieldType.LOCAL_DATE;
            case 7: return FieldType.INSTANT;
            case 8: return FieldType.ZONED_DATE_TIME;
            case 9: return FieldType.DURATION;
            case 10: return FieldType.BOOLEAN;
            case 11: return FieldType.ENUM;
            case 12: return FieldType.UUID;
            case 13: return FieldType.LOCAL_TIME;
            case 14: return entityData.getDatabaseType() == DatabaseType.CASSANDRA
                ? FieldType.BYTE_BUFFER : FieldType.BYTES;
            default: return FieldType.STRING;
        }
    }

    private void askForEnumDetails(FieldConfig.FieldConfigBuilder builder) throws IOException {
        String enumName = askInputWithValidation(
            "What is the class name of your enumeration?",
            input -> EntityValidator.validateEnumName(input).orElse(null));

        entityData.addEnum(enumName);
        builder.fieldType(enumName);

        String prompt = entityData.isExistingEnum()
            ? "What are the new values of your enumeration (separated by comma, no spaces)?\n" +
              "The new values will replace the old ones.\nNothing will be done if there are no new values."
            : "What are the values of your enumeration (separated by comma, no spaces)?";

        String enumValues = askInputWithValidation(prompt, input -> {
            if (input.isEmpty() && entityData.isExistingEnum()) {
                return null; // Allow empty for existing enum
            }
            return EntityValidator.validateEnumValues(input).orElse(null);
        });

        if (!enumValues.isEmpty()) {
            builder.enumValues(enumValues.toUpperCase());
        }

        entityData.setExistingEnum(false);
    }

    private BlobType askBlobType(boolean includeText) throws IOException {
        List<String> blobTypes = new ArrayList<>(Arrays.asList("An image", "A binary file"));
        if (includeText) {
            blobTypes.add("A CLOB (Text field)");
        }

        int choice = askChoice("What is the content of the Blob field?", blobTypes, 0);

        switch (choice) {
            case 0: return BlobType.IMAGE;
            case 1: return BlobType.ANY;
            case 2: return BlobType.TEXT;
            default: return BlobType.ANY;
        }
    }

    private void askForFieldValidations(FieldConfig.FieldConfigBuilder builder, FieldType fieldType) throws IOException {
        boolean addValidation = askYesNo("Do you want to add validation rules to your field?", false);
        if (!addValidation) {
            return;
        }

        List<String> validationOptions = new ArrayList<>(Arrays.asList("Required", "Unique"));

        if (fieldType == FieldType.STRING) {
            validationOptions.add("Minimum length");
            validationOptions.add("Maximum length");
            validationOptions.add("Regular expression pattern");
        } else if (fieldType.isNumeric()) {
            validationOptions.add("Minimum");
            validationOptions.add("Maximum");
        }

        List<String> selectedRules = askMultipleChoice(
            "Which validation rules do you want to add?",
            validationOptions);

        for (String rule : selectedRules) {
            switch (rule) {
                case "Required":
                    builder.required();
                    break;
                case "Unique":
                    builder.unique();
                    break;
                case "Minimum length":
                    int minLength = askIntInput("What is the minimum length of your field?", 0);
                    builder.minLength(minLength);
                    break;
                case "Maximum length":
                    int maxLength = askIntInput("What is the maximum length of your field?", 20);
                    builder.maxLength(maxLength);
                    break;
                case "Regular expression pattern":
                    String pattern = askInput("What is the regular expression pattern you want to apply?", "^[a-zA-Z0-9]*$");
                    builder.pattern(pattern);
                    break;
                case "Minimum":
                    Number min = fieldType == FieldType.FLOAT || fieldType == FieldType.DOUBLE || fieldType == FieldType.BIG_DECIMAL
                        ? askDoubleInput("What is the minimum of your field?", 0.0)
                        : askIntInput("What is the minimum of your field?", 0);
                    builder.min(min);
                    break;
                case "Maximum":
                    Number max = fieldType == FieldType.FLOAT || fieldType == FieldType.DOUBLE || fieldType == FieldType.BIG_DECIMAL
                        ? askDoubleInput("What is the maximum of your field?", 100.0)
                        : askIntInput("What is the maximum of your field?", 100);
                    builder.max(max);
                    break;
            }
        }
    }

    private Set<String> getExistingFieldNames() {
        Set<String> names = new HashSet<>();
        names.add("id");
        entityConfig.getFields().forEach(f ->
            names.add(EntityValidator.toSnakeCase(f.getFieldName())));
        return names;
    }

    private boolean isBuiltInUser(String entityName) {
        return "User".equalsIgnoreCase(entityName);
    }

    private void logFieldsAndRelationships() {
        if (entityConfig.getFields().isEmpty() && entityConfig.getRelationships().isEmpty()) {
            return;
        }

        print("\n" + RED + WHITE + "================= " + RESET + entityData.getName() + WHITE + " =================" + RESET);

        if (!entityConfig.getFields().isEmpty()) {
            print(WHITE + "Fields" + RESET);
            entityConfig.getFields().forEach(field -> {
                print(RED + field.getFieldName() + RESET + WHITE + " (" + field.getFieldType() +
                      (field.getFieldTypeBlobContent() != null ? " " + field.getFieldTypeBlobContent() : "") +
                      ") " + RESET + CYAN + formatValidationRules(field) + RESET);
            });
            print("");
        }

        if (!entityConfig.getRelationships().isEmpty()) {
            print(WHITE + "Relationships" + RESET);
            entityConfig.getRelationships().forEach(rel -> {
                String validation = rel.isRequired() ? "required" : "";
                print(RED + rel.getRelationshipName() + RESET + WHITE + " (" +
                      capitalize(rel.getOtherEntityName()) + ") " + RESET +
                      CYAN + rel.getRelationshipType() + " " + validation + RESET);
            });
            print("");
        }
    }

    private String formatValidationRules(FieldConfig field) {
        if (!field.hasValidation()) {
            return "";
        }
        List<String> rules = new ArrayList<>();
        for (String rule : field.getFieldValidateRules()) {
            switch (rule) {
                case "required":
                case "unique":
                    rules.add(rule);
                    break;
                case "minlength":
                    rules.add("minlength='" + field.getFieldValidateRulesMinlength() + "'");
                    break;
                case "maxlength":
                    rules.add("maxlength='" + field.getFieldValidateRulesMaxlength() + "'");
                    break;
                case "pattern":
                    rules.add("pattern='" + field.getFieldValidateRulesPattern() + "'");
                    break;
                case "min":
                    rules.add("min='" + field.getFieldValidateRulesMin() + "'");
                    break;
                case "max":
                    rules.add("max='" + field.getFieldValidateRulesMax() + "'");
                    break;
            }
        }
        return String.join(" ", rules);
    }

    // Console interaction methods

    private void print(String message) {
        out.println(message);
    }

    private String readLine() throws IOException {
        String line = reader.readLine();
        return line != null ? line.trim() : "";
    }

    private boolean askYesNo(String question, boolean defaultValue) throws IOException {
        String defaultStr = defaultValue ? "Y/n" : "y/N";
        print(question + " (" + defaultStr + "): ");
        String input = readLine();
        if (input.isEmpty()) {
            return defaultValue;
        }
        return input.toLowerCase().startsWith("y");
    }

    private String askInput(String question, String defaultValue) throws IOException {
        String prompt = defaultValue != null
            ? question + " (" + defaultValue + "): "
            : question + ": ";
        print(prompt);
        String input = readLine();
        return input.isEmpty() && defaultValue != null ? defaultValue : input;
    }

    private String askInputWithValidation(String question, Function<String, String> validator) throws IOException {
        return askInputWithValidation(question, null, validator);
    }

    private String askInputWithValidation(String question, String defaultValue,
                                          Function<String, String> validator) throws IOException {
        while (true) {
            String input = askInput(question, defaultValue);
            String error = validator.apply(input);
            if (error == null) {
                return input;
            }
            print(RED + error + RESET);
        }
    }

    private int askIntInput(String question, int defaultValue) throws IOException {
        while (true) {
            String input = askInput(question, String.valueOf(defaultValue));
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                print(RED + "Please enter a valid number" + RESET);
            }
        }
    }

    private double askDoubleInput(String question, double defaultValue) throws IOException {
        while (true) {
            String input = askInput(question, String.valueOf(defaultValue));
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                print(RED + "Please enter a valid number" + RESET);
            }
        }
    }

    private int askChoice(String question, List<String> choices, int defaultChoice) throws IOException {
        print(question);
        for (int i = 0; i < choices.size(); i++) {
            String marker = i == defaultChoice ? BOLD + ">" + RESET : " ";
            print(marker + " " + (i + 1) + ") " + choices.get(i));
        }
        print("Enter choice [" + (defaultChoice + 1) + "]: ");

        String input = readLine();
        if (input.isEmpty()) {
            return defaultChoice;
        }

        try {
            int choice = Integer.parseInt(input) - 1;
            if (choice >= 0 && choice < choices.size()) {
                return choice;
            }
        } catch (NumberFormatException ignored) {
        }

        print(RED + "Invalid choice, using default" + RESET);
        return defaultChoice;
    }

    private String askChoiceString(String question, List<String> choices, int defaultChoice) throws IOException {
        int choice = askChoice(question, choices, defaultChoice);
        return choices.get(choice);
    }

    private List<String> askMultipleChoice(String question, List<String> choices) throws IOException {
        print(question + " (comma-separated numbers):");
        for (int i = 0; i < choices.size(); i++) {
            print("  " + (i + 1) + ") " + choices.get(i));
        }
        print("Enter choices: ");

        String input = readLine();
        if (input.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> selected = new ArrayList<>();
        for (String part : input.split(",")) {
            try {
                int index = Integer.parseInt(part.trim()) - 1;
                if (index >= 0 && index < choices.size()) {
                    selected.add(choices.get(index));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return selected;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Exception thrown when entity generation is aborted by user.
     */
    public static class EntityGenerationAbortedException extends RuntimeException {
        public EntityGenerationAbortedException(String message) {
            super(message);
        }
    }
}
