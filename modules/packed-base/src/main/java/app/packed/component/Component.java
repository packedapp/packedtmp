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
package app.packed.component;

import java.util.Collection;
import java.util.Optional;

import app.packed.config.ConfigSite;

/**
 *
 */
public interface Component {

    default Collection<?> features() {
        // Ideen er f.eks. at kunne returnere alle services en
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an immutable view of this component's children.
     *
     * @return an immutable view of this component's children
     */
    Collection<Component> children();

    /**
     * Returns the configuration site of this component.
     * 
     * @return the configuration site of this component
     */
    ConfigSite configurationSite();

    /**
     * Returns the description of this component. Or an empty optional if no description has been set
     *
     * @return the description of this component. Or an empty optional if no description has been set
     *
     * @see ComponentConfiguration#setDescription(String)
     */
    Optional<String> description();

    /**
     * Returns the name of this component.
     * <p>
     * If no name was explicitly set when configuring the component. A unique name (among other components with the same
     * parent) was automatically generated.
     *
     * @return the name of this component
     *
     * @see ComponentConfiguration#setName(String)
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
}

/// **
// * Returns the component instance.
// *
// * @return the component instance
// * @throws IllegalStateException
// * if invoking this method before the component has been initialized from another thread then the thread
// * that is initializing the component.
// */
// Object instance();
//
// default void install(Consumer<? super ComponentInstaller> installer) {
// // Maybe have a runtime component installer???
// // maybe just allow prototypes for now...
// }
/// **
// * Returns the container that this component is installed in.
// *
// * @return the container that this component is installed in
// */
// Container container();
// installContainer(Bundle...) <-man kan ikke ting fra den paa runtime... kun den anden vej...
