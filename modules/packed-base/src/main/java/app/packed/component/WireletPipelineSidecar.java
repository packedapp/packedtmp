/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.component;

/**
 *
 */
@interface WireletPipelineSidecar {

    // Maa vaere det samme som Wirelets
    boolean requireAssemblyTime() default false;

    boolean consumeNonAssembly() default false;
}

// AssemblyTime
// ConsumeFirstBatchOfRunnable ... rest in pipeline
// All In pipelines...

// Maaske laver vi kun defineret i en pipeline...

// ServiceExtension vil det...

// Okay.. Tror vi bare comsumer starten...

// Og saa skal descriptors
// Contracts
// Og Instantiatiation...
// Tage pipelines that are runnable

// Maaske er Expandable et bedre ord....
// Should be careful
