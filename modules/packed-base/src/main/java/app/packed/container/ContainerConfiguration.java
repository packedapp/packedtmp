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
package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.inject.Factory;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.TypeLiteral;
import app.packed.lifecycle.LifecycleState;
import app.packed.util.InvalidDeclarationException;

/**
 * The configuration of a container. An instance of this class is typically acquired by calling
 * {@link Container#of(Consumer)}.
 */
public interface ContainerConfiguration extends InjectorConfiguration {

    <T> ComponentConfiguration<T> install(Class<T> implementation);

    <T> ComponentConfiguration<T> install(Factory<T> factory);

    /**
     * Install the specified component instance and makes it available as a service with the key
     * {@code instance.getClass()}.
     * <p>
     * If this install operation is the first install operation. The component will be installed as the root component of
     * the container. All subsequent install operations on this configuration will have the component as its parent. If you
     * wish to have a specific component as a parent, use the various install methods on the {@link ComponentConfiguration}
     * instance returned on each install can be used to specify a specific parent.
     * <p>
     * Unlike {@link #installAndBind(Object)}, components installed via this method is <b>not</b> automatically made
     * available as services that can be injected into other components.
     *
     * @param instance
     *            the component instance to install
     * @return the component configuration
     * @see Component#install(Object)
     * @throws InvalidDeclarationException
     *             if some property of the instance prevents it from being installed as a component
     */
    <T> ComponentConfiguration<T> install(T instance);

    <T> ComponentConfiguration<T> install(TypeLiteral<T> implementation);

    /**
     * @param state
     *            the lifecycle state
     * @return
     */
    // Every container has 6 build-in syncpoints
    // INITIALIZING (ALWAYS TRUE, INJECT NOT SUPPORTED by outside threads)
    // INITIALIZED
    // STARTING
    // RUNNING
    // STOPPING
    // TERMINATED
    default ContainerActionable on(LifecycleState... state) {
        String[] params = new String[state.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = requireNonNull(state[i], "index " + i + " is null").toString();
        }
        return on(params);
    }

    /**
     *
     * @param syncpoints
     *            the point to synchronoize on
     * @return
     * @throws IllegalArgumentException
     *             if the arguments contain an unknown syncpoint
     */
    // Skal nok lige sikre os at folk ved det er OR og ikke AND

    default ContainerActionable on(String... syncpoints) {
        throw new UnsupportedOperationException();
    }

    default ContainerActionable onInitializing() {
        return on(LifecycleState.INITIALIZING);
    }

    default ContainerActionable onStarting() {
        return on(LifecycleState.STARTING);
    }

    default ContainerActionable onStopping() {
        return on(LifecycleState.STOPPING);
    }
}
