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
package app.packed.binding;

/**
 *
 */

// What about functions and their parameters?? Stuff into manual with a good story

public enum BindingKind {

    /**
     * The binding has been created because of a Hook. Either the variable is annotated with a binding hook. Or the variable
     * class is annotated with BindingHook
     *
     * @see BindingTypeHook
     * @see AnnotatedBindingHook
     **/
    HOOK,

    /**
     * A binding that was created manually.
     *
     * @see OperationHandle#manuallyBindable(int)
     * @see Op#bind(Object)
     * @see Op#bind(int, Object, Object...)
     */
    MANUAL,

    /**
     * The binding represents the dependency on a service.
     * <p>
     * Service bindings are always represented by a {@link ServiceBindingMirror} which contains the key of the service.
     * <p>
     * If a service has been overridden via {@link app.packed.bean.BeanConfiguration#overrideService(Class, Object)} the binding
     * kind is replaced with {@link #MANUAL} and an {@link app.packed.service.OverriddenServiceBindingMirror}.
     *
     * @see ServiceBindingMirror
     */
    SERVICE;
}
