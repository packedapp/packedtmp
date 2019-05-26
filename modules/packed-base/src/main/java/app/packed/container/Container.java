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

import java.util.Optional;

import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.inject.Injector;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.LifecycleState;

/**
 * The main purpose of this interface is to manage and control the life cycle of components.
 * <p>
 * To create a new Container first create a new . When the builder has been configured, a new Container instance can be
 * created by calling which returns a new container in the {@link LifecycleState#INITIALIZED} state.
 * <p>
 * The container can now be started either by calling a method that requires a running container, for example,
 * {@link #use(Class)}. Or by calling start which will <b>asynchronously</b> start the container. Calling either of
 * these methods will move the container into the {@link LifecycleState#STARTING} state. When the container and all of
 * its components have been properly started the container will move to the {@link LifecycleState#RUNNING} state. Which
 * indicates that the container is ready for use. Calls by other threads to the container before it is fully started
 * will block until the container transitions from the {@link LifecycleState#STARTING} state.
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

// Det er altid meningen at containere er indad facing.... Ligesom Componenten....

// App+Injector er udad facing,
// Componenter er som udgangspunkt ogsaa indad facing. Maaske endda paa containere niveau.
// D.v.s. med mindre man explicit exposer dem til andre containere saa er de private indenfor containere..
// Containere er ordnet i et tree. Componenter i et multi trae

// Syntes maaske ikke den skal extende Injector...

// Services can have injector injected??? Only Components can have a Container???

public interface Container extends Injector {

    /**
     * Returns the application that this container belongs to.
     * 
     * @return the application that this container belongs to
     */
    App app();

    // ListenerManager listenerManager(); // listeners()...???, container.jobs().

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
    String name();

    // Path... PackedPath, URL????? Just like 192.168.3.3 <- Is clearly an ip adress 192.155.3.2/foo is clearly a website

    LifecycleOperations<? extends Container> state();
}

/// **
// * Returns the root component of this container.
// *
// * @return the root component of this container
// */
// Component root();
/// **
// * Returns a stream of all the components in the container in no particular order.
// * <p>
// * Invoking this method is equivalent to calling {@code container.root().components()}.
// *
// * @return a stream of all the components in the container in no particular order
// * @see Component#stream()
// */
// default ComponentStream components() {
// return root().stream();
// }
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
