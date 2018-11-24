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

import app.packed.bundle.ContainerBundle;
import app.packed.inject.AbstractInjectorStage;
import app.packed.inject.Factory;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.TypeLiteral;
import app.packed.lifecycle.LifecycleState;
import app.packed.util.InvalidDeclarationException;

/**
 * A configuration object for a {@link Container}. This interface is typically used when configuring a new container via
 * {@link Container#of(Consumer)}.
 * <p>
 * This interface extends {@link InjectorConfiguration} with functionality for:
 * <ul>
 * <li>Installing components that should be available from the container, at least one component must be installed</li>
 * <li>Registering callbacks that will be executed on certain syncpoints</li>
 * <li>Setting a name for the container.</li>
 * </ul>
 */
public interface ContainerConfiguration extends InjectorConfiguration {

    default void containerInstall(Class<? extends ContainerBundle> bundleType, AbstractInjectorStage... filters) {
        throw new UnsupportedOperationException();
    }

    default void containerInstall(ContainerBundle bundle, AbstractInjectorStage... filters) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the name of the container or null if the name has not been set.
     *
     * @return the name of the container or null if the name has not been set
     * @see #setName(String)
     * @see Container#getName()
     */
    String getName();

    /**
     * Install the specified component implementation and makes it available as a service with the specified class as the
     * key. Invoking this method is equivalent to invoking {@code Factory.findInjectable(implementation)}.
     * 
     * @param <T>
     *            the type of component to install
     * @param implementation
     *            the component implementation to install
     * @return a component configuration that can be use to configure the component in greater detail
     * @throws InvalidDeclarationException
     *             if some property of the implementation prevents it from being installed as a component
     */
    <T> ComponentConfiguration<T> install(Class<T> implementation);

    <T> ComponentConfiguration<T> install(Factory<T> factory);

    /**
     * Install the specified component instance and makes it available as a service with the key
     * {@code instance.getClass()}.
     * <p>
     * If this install operation is the first install operation. The component will be installed as the root component of
     * the container. All subsequent install operations on this configuration will have the component as its parent. If you
     * wish to use a specific component as a parent, use the various install methods on the {@link ComponentConfiguration}
     * instead of the install methods on this interface.
     *
     * @param <T>
     *            the type of component to install
     * @param instance
     *            the component instance to install
     * @return the component configuration
     * @throws InvalidDeclarationException
     *             if some property of the instance prevents it from being installed as a component
     */
    <T> ComponentConfiguration<T> install(T instance);

    /**
     * Install the specified component implementation and makes it available as a service with the specified type literal as
     * the key. Invoking this method is equivalent to invoking {@code Factory.findInjectable(implementation)}.
     * 
     * @param <T>
     *            the type of component to install
     * @param implementation
     *            the component implementation to install
     * @return component configuration that can be use to configure the component in greater detail
     * @throws InvalidDeclarationException
     *             if some property of the implementation prevents it from being installed as a component
     */
    <T> ComponentConfiguration<T> install(TypeLiteral<T> implementation);

    /**
     * @param state
     *            the lifecycle state
     * @return stuff
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
     * @return an action
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

    /**
     * Sets the {@link Container#getName() name} of the container. The name must consists only of alphanumeric characters
     * and '_' or '-'. The name is case sensitive.
     * <p>
     * If no name is set using this method a unique name (among siblings) is generated at build time.
     *
     * @param name
     *            the name of the container
     * @throws IllegalArgumentException
     *             if the specified name is the empty string or if the name contains other characters then alphanumeric
     *             characters and '_' or '-'
     * @see #getName()
     * @see Container#getName()
     */
    void setName(String name);
}
