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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.config.ConfigSite;
import app.packed.inject.Injector;

/**
 *
 */
public interface Component /* extends Taggable */ {

    /**
     * Returns an immutable view of this component's children. To remove or add children at runtime, use this component's
     * installer.
     *
     * @return an immutable view of this component's children
     */
    Collection<Component> children();

    /**
     * Returns the configuration site of the component.
     * 
     * @return the configuration site of the component
     */
    ConfigSite configurationSite();

    /**
     * Returns the container that this component is installed in.
     *
     * @return the container that this component is installed in
     */
    Container container();

    default void install(Consumer<? super ComponentInstaller> installer) {
        // Maybe have a runtime component installer???
        // maybe just allow prototypes for now...
    }

    /**
     * Returns the description of this component Or null if no description has been set
     *
     * @return the description of this component. Or null if no description has been set
     *
     * @see ComponentServiceConfiguration#setDescription(String)
     */
    Optional<String> description();

    /**
     * Returns the private injector of this component.
     *
     * @return the private injector of this component
     */
    // Privatesss?????Syntes skal hedde det samme, Bliver maaske lazy initialiseret efter startup
    // Maaske skal vi bare extende Injector.....
    Injector injector();

    /**
     * Returns the component instance.
     *
     * @return the component instance
     * @throws IllegalStateException
     *             if invoking this method before the component has been initialized from another thread then the thread
     *             that is initializing the component.
     */
    Object instance();

    /**
     * Returns the name of this component.
     * <p>
     * If no name was explicitly set when configuring the component. A unique name (among other components with the same
     * parent) was automatically generated.
     *
     * @return the name of this component
     *
     * @see ComponentServiceConfiguration#setName(String)
     */
    String name();

    /**
     * Returns the path of this component.
     *
     * @return the path of this component
     */
    ComponentPath path();

    /**
     * Returns a component stream consisting of this component and all of its descendants in any order.
     *
     * @return a component stream consisting of this component and all of its descendants in any order
     */
    ComponentStream stream();

    // installContainer(Bundle...) <-man kan ikke ting fra den paa runtime... kun den anden vej...
}
