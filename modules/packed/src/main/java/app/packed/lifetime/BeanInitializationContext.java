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
package app.packed.lifetime;

import java.util.Optional;

import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import app.packed.runtime.StopInfo;

/**
 *
 */
// Ogsaa tilgaengelig fra factory, constructor.

// Den kan ikke rigtig noget. Maaske vi bare skal droppe den
interface BeanInitializationContext extends Context<BaseExtension> {

    /**
     * @return
     */
    boolean isManaged();

    /**
     * @param action
     * @throws if
     *             already late
     */
    void runAfterDependencies(Runnable action);

    // still not convinced
    void runOnInitializationFailure(Runnable action);

    // Den giver ingen mening vil jeg mene.
    // Er det kun for application's lifetimen?
    // Skal vel ikke have den for entity bean?
    // For 3 timer siden genstartede applicationen
    // Tror vi maa konfiguere applicationen til at gemme det
    Optional<StopInfo> restartedFrom();
}
