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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import app.packed.bundle.Bundles;
import app.packed.bundle.ContainerBundle;
import app.packed.inject.Injector;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.LifecycleState;
import app.packed.lifecycle.OnInitialize;
import packed.internal.container.ContainerBuilder;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * The main purpose of this interface is to manage and control the life cycle of components.
 * <p>
 * To create a new Container first create a new . When the builder has been configured, a new Container instance can be
 * created by calling which returns a new container in the {@link LifecycleState#INITIALIZED} state.
 * <p>
 * The container can now be started either by calling a method that requires a running container, for example,
 * {@link #with(Class)}. Or by calling {@link #start()} which will <b>asynchronously</b> start the container. Calling
 * either of these methods will move the container into the {@link LifecycleState#STARTING} state. When the container
 * and all of its components have been properly started the container will move to the {@link LifecycleState#RUNNING}
 * state. Which indicates that the container is ready for use. Calls by other threads to the container before it is
 * fully started will block until the container transitions from the {@link LifecycleState#STARTING} state.
 * <p>
 * A <b>Container</b> can be shut down, which will cause it to stop accepting any form of new work. What exactly is
 * meant by the term 'work' depends on the type of the container, for example, a database-like structure extending this
 * interface might not allow mutation or retrieval operations of data. While a network-like structure might not allow
 * sending of packets between different hosts.
 * <p>
 * After being shut down, the container will eventually terminate, at which point no tasks are actively executing within
 * the container, no tasks are awaiting execution, and the container will accept no new work in any form.
 * <p>
 * Unless otherwise specified a container implementation is safe for access by multiple threads.
 */
// listeners <- altsaa vil vel vaere maerkelig at automatisk starte den via listeners???
// ------------ Det er jo fuldt lovligt via containerBuilder.
public interface Container extends Injector {

    /**
     * Returns a stream of all the components in the container in no particular order.
     * <p>
     * Invoking this method is equivalent to calling {@code container.root().components()}.
     *
     * @return a stream of all the components in the container in no particular order
     * @see Component#stream()
     */
    default ComponentStream components() {
        return root().stream();
    }

    /**
     * Returns a component corresponding to the specified the path.
     * <p>
     * Show example with String
     * <p>
     * Show example with Path
     *
     * @param path
     *            the path of the component. Typically a {@link String} or a {@link ComponentPath}.
     * @return the component if present, otherwise an empty optional
     * @throws NullPointerException
     *             if the specified path is null
     */
    Optional<Component> getComponent(CharSequence path);

    /**
     * Returns the name of this container.
     * <p>
     * If no name was explicitly when configuring the container, a unique (on a best-effort basis) name was generated.
     *
     * @return the name of this container
     */
    String getName();

    // ListenerManager listenerManager(); // listeners()...???, container.jobs().

    /**
     * Returns the root component of this container.
     *
     * @return the root component of this container
     */
    Component root();

    /**
     * Initiates an orderly asynchronously shutdown of the container. In which currently running tasks will be executed, but
     * no new tasks will be started. Invocation has no additional effect if the container has already been shut down.
     * <p>
     * There are (currently) no method similar to {@link ExecutorService#shutdownNow()}.
     *
     * @return a future that can be used to query whether the container has completed shutdown. Or is still in the process
     *         of shutting down the container.
     */
    // Maybe shutdown().cancel() <- can be similar to ExecutorService#shutdownNow
    CompletableFuture<Container> shutdown();

    /**
     * Initiates an orderly asynchronously shutdown of the container because of an exceptional condition. Invocation has no
     * additional effect if the container has already been shut down.
     *
     * @param cause
     *            the cause of the shutdown
     * @return a future that can be used to query whether the container has completed shutdown. Or is still in the process
     *         of shutting down the container.
     */
    CompletableFuture<Container> shutdown(Throwable cause);

    /**
     * Initiates an asynchronously startup of the container. Normally, there is no need to call this methods since most
     * methods on the container will lazily start the container whenever it is needed. For example, invoking
     * {@link #with(Class)} will automatically start the container if it has not already been started by another action.
     * <p>
     * If the container is in the process of being initialized when invoking this method, for example, from a method
     * annotated with {@link OnInitialize}. The container will automatically be started immediately after it have been
     * constructed.
     * <p>
     * Invocation has no additional effect if the container has already been started or shut down.
     *
     * @return a future that can be used to query whether the container has completed startup or is still in the process of
     *         starting up. Can also be used to retrieve any exception that might have prevented the container in starting
     *         properly
     */
    // Gem den væk på state()
    // Det eneste er at der altsaa er nogen forskel her paa container og component. Da man ikke stoppe en komponent....

    CompletableFuture<Container> start();

    LifecycleOperations<? extends Container> state();

    /**
     * Creates a new container from the specified bundle.
     *
     * @param bundle
     *            the bundle to create the container from
     * @return a new container
     * @throws RuntimeException
     *             if the container could not be created
     */
    static Container of(ContainerBundle bundle) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new container from a bundle of the specified type.
     *
     * @param bundleType
     *            the type of bundle to create the container from
     * @return a new container
     * @throws RuntimeException
     *             if the container could not be created
     */
    static Container of(Class<? extends ContainerBundle> bundleType) {
        return of(Bundles.instantiate(bundleType));
    }

    static Container of(Consumer<? super ContainerConfiguration> configurator) {
        requireNonNull(configurator, "configurator is null");
        ContainerBuilder c = new ContainerBuilder(InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF));
        configurator.accept(c);
        return c.build();
    }
}

/// **
// * Performs the given action for each component in this container. Components are visited using depth first order.
/// With
// * the root component being the first component visited. Followed by its children in any order.
// * <p>
// * Any exception thrown by the specified action is relayed to the caller.
// * <p>
// * Invoking this method is equivalent to:
// *
// * <pre>
// * container.getComponent("/").forEach(action);
// * </pre>
// *
// * @param action
// * the action to perform
// * @throws NullPointerException
// * if the specified action is null
// * @see Component#forEach(Consumer)
// */
// default void forEachComponent(Consumer<? super Component> action) {
// root().forEach(action);
// }
