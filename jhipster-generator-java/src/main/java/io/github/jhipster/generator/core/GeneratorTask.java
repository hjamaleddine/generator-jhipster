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
 * Represents a generator task to be executed at a specific priority.
 */
@FunctionalInterface
public interface GeneratorTask {

    /**
     * Executes the task.
     *
     * @throws Exception if task execution fails
     */
    void execute() throws Exception;

    /**
     * Returns the task name for logging purposes.
     *
     * @return the task name
     */
    default String getName() {
        return "anonymous";
    }
}
