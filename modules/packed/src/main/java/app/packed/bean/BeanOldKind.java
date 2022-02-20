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
package app.packed.bean;

/**
 *
 */
public enum BeanOldKind {

    /**
     * A static bean.
     * <p>
     * A bean that is never instantiated and has no state.
     */
    // Altsaa jeg ved ikke hvor meget det der state er embedded i alt...
    // Om vi kan snakke om beans der ikke har state...
    // Request er en ting... 
    // Men vi kan ikke faa injecte f.x. configuration
    // Men hvorfor ikke get(@ConfigFoo("sdfsf"), String req) <--
    FUNCTIONAL_BEAN, // STATIC_BEAN?

    /**
     * A singleton bean.
     * <p>
     * A single instance of the bean is created together with the application instance. It is coterminous with the
     * application instance itself.
     */
    CONTAINER_BEAN,

    /**
     * A lazy singleton bean.
     * <p>
     */
    LAZY_CONTAINER_BEAN, // IDK
    
    // Kan kun have 1 af hver type
    // Er visible i child lifetypes (+dependent?)
    EXTENSION_BEAN,

    /**
     * A prototype bean.
     * <p>
     * Is always created by an extension. Once initialized, the bean is no longer tracked by neither Packed or the
     * extension. And example, is a service prototype. Which is created when requested. But how, when and if the client
     * chooses to dispose of it is
     */
    PROTOTYPE_UNMANAGED,

    /**
     * A tracked bean.
     * <p>
     * Typical examples, are requests
     */
    TRACKED // Managed
}
