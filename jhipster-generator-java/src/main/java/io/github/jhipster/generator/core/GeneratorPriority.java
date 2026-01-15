/*
 * Copyright 2013-2025 the original author or authors from the JHipster project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.jhipster.generator.core;

/**
 * Generator execution priorities.
 * Mirrors the Yeoman generator priority system used in JHipster TypeScript.
 */
public enum GeneratorPriority {

    // Standard Yeoman priorities
    INITIALIZING(100),
    PROMPTING(200),
    CONFIGURING(300),
    COMPOSING(400),
    LOADING(500),
    PREPARING(600),
    DEFAULT(700),
    WRITING(800),
    CONFLICTS(900),
    INSTALL(1000),
    END(1100),

    // JHipster custom priorities
    COMPOSING_COMPONENT(350),
    CONFIGURING_EACH_ENTITY(310),
    LOADING_ENTITIES(510),
    PREPARING_EACH_ENTITY(610),
    PREPARING_EACH_ENTITY_FIELD(620),
    PREPARING_EACH_ENTITY_RELATIONSHIP(630),
    POST_PREPARING_EACH_ENTITY(640),
    WRITING_ENTITIES(810),
    POST_WRITING_ENTITIES(820),
    POST_WRITING(850);

    private final int order;

    GeneratorPriority(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
