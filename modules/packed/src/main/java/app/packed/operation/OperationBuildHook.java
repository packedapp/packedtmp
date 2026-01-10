/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.operation;

import app.packed.build.hook.BuildHook;

/**
 * A build hook for operations.
 */
public non-sealed abstract class OperationBuildHook extends BuildHook {

    // Ideen er vi vil fjerne tilfoeje annoteringer
    // Ideel Set burde vi ogsaa kunne indsaette parameter with default values, ect
    public void mutate(OperationType operation) {

    }

    /**
     * When an application has finished building this method is called to check.
     * <p>
     *
     * on because it should be a notification thingy, or should we reserve on to Async
     *
     * onSuccess??? verify?
     *
     * @param mirror
     *            a mirror of the assembly to verify
     *
     * @see AssemblyMirror#containers()
     */
    // I don't like verify, because we might just write some debug info
    public void onBuildSuccess(OperationMirror mirror) {} // onBuild

    // Hmm, not super nice name because it is actually just last chance
    public void onConfigured(OperationConfiguration configuration) {}

    /**
     * Invoked immediately after a new operation is created. But before the configuration object is returned to the user.
     *
     * @param configuration
     *            the configuration of the new operation
     */
    public void onNew(OperationConfiguration configuration) {}
}