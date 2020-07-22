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
package app.packed.element;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;

/**
 * A component is generally thought of as being immutable.
 */
// boolean inImage()?
public interface Component {

    /**
     * Returns an unmodifiable view of all of this component's children.
     *
     * @return an unmodifiable view of all of this component's children
     */
    Collection<Component> children();

    /**
     * Returns the configuration site of this component.
     * 
     * @return the configuration site of this component
     */
    // Could also be an attribute... Som kun er tilgaengelig nogengang..
    ConfigSite configSite();

    /**
     * Returns the container that this component is a part of. If this component is itself a container, returns this.
     * 
     * @return the container that this component is a part of
     */
    Component container();

    /**
     * Returns the depth of the component in a tree of components.
     * 
     * @return the depth of the component in a tree of components
     */
    int depth();

    /**
     * If this component is a part of extension, returns the extension. Otherwise returns empty.
     * 
     * @return any extension this component belongs to
     */
    // Could also be an attribute???
    Optional<Class<? extends Extension>> extension();

    /**
     * Returns this components parent. Or {@link Optional#empty()} if root.
     * 
     * @return this components parent or empty if root.
     */
    Optional<Component> parent();

    /**
     * Returns the path of this component.
     * 
     * @return the path of this component
     */
    ComponentPath path();

    // Optional for stuff that is not in same system??
    ComponentRelation relationTo(Component to);

    /**
     * Returns a stream consisting of this component and all of its descendants in any order. Various {@link Option options}
     * can be specified to precisely control the behaviour of the stream.
     *
     * @param options
     *            options for specifying the behaviour of the stream
     * 
     * @return a component stream consisting of this component and all of its descendants in any order
     */
    ComponentStream stream(ComponentStream.Option... options);

    /**
     * 
     * 
     * <p>
     * This operation does not allocate any objects internally.
     * 
     * @implNote Implementations of this method should never generate object (which is a bit difficult
     * @param action
     *            oops
     */
    // We want to take some options I think. But not as a options
    // Well it is more or less the same options....
    // Tror vi laver options om til en klasse. Og saa har to metoder.
    // Og dropper varargs..
    void traverse(Consumer<? super Component> action);

    /**
     * Returns the underlying type of component. This is typically specified when creating the component.
     * 
     * @return the type used when constructing the component
     */
    Class<?> type(); // Container == Container.class?

}
