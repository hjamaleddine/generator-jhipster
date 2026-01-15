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

/**
 * Java implementation of the JHipster Domain Layer Generator.
 *
 * <p>This package provides a Java port of the TypeScript-based domain generator,
 * responsible for generating JPA entity classes, test files, and enumeration classes.</p>
 *
 * <h2>Package Structure</h2>
 * <ul>
 *   <li>{@code io.github.jhipster.generator.domain} - Main generator classes</li>
 *   <li>{@code io.github.jhipster.generator.domain.model} - Entity, field, relationship models</li>
 *   <li>{@code io.github.jhipster.generator.domain.support} - Utility classes</li>
 * </ul>
 *
 * <h2>Main Classes</h2>
 * <ul>
 *   <li>{@link io.github.jhipster.generator.domain.DomainGenerator} - Main generator entry point</li>
 *   <li>{@link io.github.jhipster.generator.domain.EntityServerFiles} - File configuration</li>
 *   <li>{@link io.github.jhipster.generator.domain.model.EntityContext} - Entity context for templates</li>
 *   <li>{@link io.github.jhipster.generator.domain.model.FieldContext} - Field context for templates</li>
 *   <li>{@link io.github.jhipster.generator.domain.model.RelationshipContext} - Relationship context</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create generator
 * DomainGenerator generator = new DomainGenerator.Builder()
 *     .destinationPath("/path/to/project")
 *     .packageName("com.example.app")
 *     .generateEntities(true)
 *     .generateEnums(true)
 *     .useJakartaValidation(true)
 *     .build();
 *
 * // Create entity context
 * EntityContext entity = new EntityContext("Product");
 * entity.addField(new FieldContext("name", "String"));
 * entity.addField(new FieldContext("price", "BigDecimal"));
 *
 * // Add entity and run
 * generator.addEntity(entity);
 * generator.run();
 * }</pre>
 *
 * <h2>Lifecycle Phases</h2>
 * <p>The generator follows these phases (matching the TypeScript version):</p>
 * <ol>
 *   <li><strong>PREPARING_EACH_ENTITY</strong> - Validate entity names, set domain layer flag</li>
 *   <li><strong>PREPARING_EACH_ENTITY_FIELD</strong> - Validate field names, compute Java bean names</li>
 *   <li><strong>PREPARING_EACH_ENTITY_RELATIONSHIP</strong> - Validate relationship names, compute bean names</li>
 *   <li><strong>POST_PREPARING_EACH_ENTITY</strong> - Check for cyclic required relationships</li>
 *   <li><strong>WRITING</strong> - Generate base test utility files</li>
 *   <li><strong>WRITING_ENTITIES</strong> - Generate entity domain classes and enums</li>
 * </ol>
 *
 * <h2>Generated Files</h2>
 * <p>For each entity, the generator produces:</p>
 * <ul>
 *   <li>{@code src/main/java/.../domain/Entity.java} - Main JPA entity class</li>
 *   <li>{@code src/test/java/.../domain/EntityTest.java} - Unit test</li>
 *   <li>{@code src/test/java/.../domain/EntityTestSamples.java} - Test sample data</li>
 *   <li>{@code src/test/java/.../domain/EntityAsserts.java} - Test assertions</li>
 *   <li>{@code src/main/java/.../domain/enumeration/*.java} - Enum classes (if applicable)</li>
 * </ul>
 *
 * @see io.github.jhipster.generator.domain.DomainGenerator
 * @since 1.0.0
 */
package io.github.jhipster.generator.domain;
