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
 * Java implementation of the JHipster Entity Generator.
 *
 * <p>This package provides a Java port of the TypeScript-based entity generator,
 * following the same lifecycle phases and producing equivalent output.</p>
 *
 * <h2>Package Structure</h2>
 * <ul>
 *   <li>{@code io.github.jhipster.generator.entity} - Main generator classes</li>
 *   <li>{@code io.github.jhipster.generator.entity.model} - Entity configuration models</li>
 *   <li>{@code io.github.jhipster.generator.entity.enums} - Type enumerations</li>
 *   <li>{@code io.github.jhipster.generator.entity.support} - Utility classes</li>
 * </ul>
 *
 * <h2>Main Classes</h2>
 * <ul>
 *   <li>{@link io.github.jhipster.generator.entity.EntityGenerator} - Main generator entry point</li>
 *   <li>{@link io.github.jhipster.generator.entity.EntityPrompts} - Interactive user prompts</li>
 *   <li>{@link io.github.jhipster.generator.entity.model.EntityConfig} - Entity configuration model</li>
 *   <li>{@link io.github.jhipster.generator.entity.model.FieldConfig} - Field configuration model</li>
 *   <li>{@link io.github.jhipster.generator.entity.model.RelationshipConfig} - Relationship configuration model</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * EntityGenerator generator = new EntityGenerator.Builder()
 *     .entityName("Product")
 *     .destinationPath("/path/to/project")
 *     .build();
 *
 * generator.run();
 * }</pre>
 *
 * <h2>Lifecycle Phases</h2>
 * <p>The generator follows these phases (matching the TypeScript version):</p>
 * <ol>
 *   <li><strong>INITIALIZING</strong> - Parse options and load entity configuration</li>
 *   <li><strong>PROMPTING</strong> - Ask for microservice configuration (if applicable)</li>
 *   <li><strong>LOADING</strong> - Load and validate entity configuration</li>
 *   <li><strong>POST_PREPARING</strong> - Interactive prompts for entity definition</li>
 *   <li><strong>END</strong> - Generation complete notification</li>
 * </ol>
 *
 * @see io.github.jhipster.generator.entity.EntityGenerator
 * @since 1.0.0
 */
package io.github.jhipster.generator.entity;
