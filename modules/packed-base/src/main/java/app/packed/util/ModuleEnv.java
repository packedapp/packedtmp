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
package app.packed.util;

import java.lang.annotation.Annotation;

import app.packed.bundle.Bundle;

/**
 * Provides an environment per module
 */
// ModuleEnv, ModuleEnvironment

// Taenker om man kan have flere....
// F.eks. et der er enabled naar man koerer tests....
// Og hvad med unnamed modules...
// GlobalEnv
/// Skal module env vaere public????
// GlobalEnv ... Der kan kun vaere en af disse.....
// GlovalEvn.with(Class<? extends ModuleEnv>)
// Attributable??????
public abstract class ModuleEnv {

    protected ModuleEnv() {
        this(false);// Module only is that we should only check stuff from bundles with the module?
    }

    // Is always module only!!!!
    // But We may allow. registerGlobalListener(...) from the module that defines the listener interface
    protected ModuleEnv(boolean moduleOnly) {}

    protected ModuleEnv(Class<? extends Bundle> bundles) {}

    /**
     * Registered a qualifier annotation that is not annotated with
     * <p>
     * Sometimes you just do not have
     * 
     * @param qualifierType
     *            type
     */
    protected final void registerUnannotatedQualifier(Class<? extends Annotation> qualifierType) {}

    protected final void registerUnannotatedHook(Class<?> qualifierType) {
        // annotation -> Field, Method
        // interface, class -> Nu begynder sgu at vaere dum.... Maaske vi ikke har interface hooks alligevel
    }

    protected final void registerUnannotatedListenerType(Class<?> qualifierType) {}

    /**
     * Registers a bundle root
     * 
     * @param bundle
     *            bundle
     */
    // Maaske noget med instantiation istedet for
    // Registers the bundle, it will always be included in the native image generation....
    // Primary reflection will be activated...
    // Basically we need to register all the bundle roots
    protected final void registerRootBundle(Class<? extends Bundle> bundle) {

    }

    // global, module, specific bundles,...

    // Noget med activation... Hvis dette module bliver loaded saa skal det her proceceres

    // En native-test maade, saa vi kan teste at alt virker uden vi skal lave et image foerst....
    /// native-verify..... (Tests that everything is working with regards to native image...
}
