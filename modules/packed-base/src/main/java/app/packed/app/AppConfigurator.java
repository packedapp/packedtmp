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
package app.packed.app;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.bundle.Bundle;
import app.packed.bundle.WiringOperation;
import app.packed.container.ComponentInstaller;
import app.packed.container.Container;
import app.packed.container.ContainerActionable;
import app.packed.inject.Injector;
import app.packed.inject.SimpleInjectorConfigurator;
import app.packed.lifecycle.LifecycleState;
import app.packed.util.Nullable;

/**
 * A configuration object for a {@link Container}. This interface is typically used when configuring a new container via
 * {@link Container#of(Consumer)}.
 * <p>
 * This interface extends {@link SimpleInjectorConfigurator} with functionality for:
 * <ul>
 * <li>Installing components that should be available from the container, at least one component must be installed</li>
 * <li>Registering callbacks that will be executed on certain syncpoints</li>
 * <li>Setting a name for the container.</li>
 * </ul>
 */

// Istedet for et interface kan det jo ligesaa godt bare vaere en klasse der wrapper en ComponentConfiguration...
public interface AppConfigurator extends SimpleInjectorConfigurator, ComponentInstaller {

    /**
     * Returns the name of the container or null if the name has not been set.
     *
     * @return the name of the container or null if the name has not been set
     * @see #setName(String)
     * @see Container#name()
     */
    @Nullable
    String getName();

    // default void wireContainer(Class<? extends Bundle> bundleType, WiringOperation... stages) {
    // wireContainer(Bundles.instantiate(bundleType), stages);
    // }

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
     * Sets the (nullable) description of this injector, the description can later be obtained via
     * {@link Injector#description()}.
     *
     * @param description
     *            a (nullable) description of this injector
     * @return this configuration
     * @see #getDescription()
     * @see Injector#description()
     */
    @Override
    AppConfigurator setDescription(@Nullable String description);

    /**
     * Sets the {@link Container#name() name} of the container. The name must consists only of alphanumeric characters and
     * '_' or '-'. The name is case sensitive.
     * <p>
     * If no name is set using this method a unique name (among sibling containers) is generated at build time.
     *
     * @param name
     *            the name of the container
     * @throws IllegalArgumentException
     *             if the specified name is the empty string or if the name contains other characters then alphanumeric
     *             characters and '_' or '-'
     * @see #getName()
     * @see Container#name()
     */
    void setName(@Nullable String name);

    /**
     * Installs the specified container bundle in the container. The container created from the bundle, will have the latest
     * installed component as parent of the root in the new container. If no components have been installed, the root of the
     * bundle will be the root of the container. And no more components can be installed
     * 
     * @param bundle
     *            the bundle to install
     * @param stages
     *            import export stages
     */
    void wireContainer(Bundle bundle, WiringOperation... stages);

    // change of() <- to async start (this includes bundles then, but then we cannot create a bundled container, without
    // starting it)
    // IDeen er at have

    // autoStart
    // dontAutoStart
    // startBlocking
    // ????

}
