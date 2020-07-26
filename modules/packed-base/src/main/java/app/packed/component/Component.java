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
import app.packed.container.Extension;

/**
 * A component is the basic entity in Packed. Much like everything is a is one of the defining features of Unix, and its
 * derivatives. In packed everything is a component.
 */
// ComponentDescriptor??
// add
// type...implementation
// relationTo()... Vi vil gerne have noget der er immutable....
// Altsaa strengt taget, behover vi ikke from and to...
// F.eks.
// walk 
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
    ConfigSite configSite();

    /**
     * Returns the parent component of this component. Or empty if this component has no parent.
     * 
     * @return the parent component of this component. Or empty if this component has no parent
     */
    Optional<Component> parent();

    /**
     * Returns the depth of the component in the system. The root component having depth 0.
     * 
     * @return the depth of the component in the system
     */
    int depth();

    /**
     * Returns the name of this component.
     * <p>
     * If no name is explicitly set by the user when configuring the component. The runtime will automatically generate a
     * unique name (among other components with the same parent).
     *
     * @return the name of this component
     *
     * @see SingletonConfiguration#setName(String)
     */
    String name();

    /**
     * Returns the path of this component.
     *
     * @return the path of this component
     */
    ComponentPath path();

    default Optional<Class<?>> realm() {
        // Wirelets, Artifacts does not have
        throw new UnsupportedOperationException();
    }

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
    // void traverse(Consumer<? super Component> action);

    // Naah feature er vel readonly...
    // use kan komme paa ComponentContext og maaske ComponentConfiguration?

    // To maader,
    /// Et service object der tager en ComponentContext...
    ///// Det betyder jo ogsaa at vi ikke kan have attributemap paa Component
    ///// Fordi man ikke skal kunne f.eks. schedulere uden component context'en

    //// En ComponentContext.use(XXXX class)

    /**
     * Returns a component stream consisting of this component and all of its descendants in any order.
     *
     * @param options
     *            specifying which components will be included in the stream
     * 
     * @return a component stream consisting of this component and all of its descendants in any order
     */
    ComponentStream stream(ComponentStream.Option... options);

    // returns it self if its a container
    // container().parent().container() <-- returns parent container
    // ComponentDescriptor container();

    // Alternative have en Container extends Component....
    // Maaske ikke extends Component.... Saa vi kan have
    // container aggregates
    // container().path()
    // Vi vil gerne kunne give en component, uden at give adgang til dens container..
    // same with artifac

    /**
     * Returns the description of this component. Or an empty optional if no description was set when configuring the
     * component.
     *
     * @return the description of this component. Or an empty optional if no description was set when configuring the
     *         component
     *
     * @see SingletonConfiguration#setDescription(String)
     */
    Optional<String> description();

    /**
     * If this component is a part of extension, returns the extension. Otherwise returns empty.
     * 
     * @return any extension this component belongs to
     */
    // Don't really like this... It strongly ties a container to a component.
    // As extensions are children of containers always...

    // But then again ComponentStream.Option contains stuff about containers ect.

    // Maybe model it as an attribute
    Optional<Class<? extends Extension>> extension();

    /**
     * Returns the type of component.
     * 
     * @return the type of component
     */
    ComponentDescriptor model();
}

// SystemView/Descriptor
// Contracts...

// {
// Problemet med features er at vi har nogle vi gerne vil list som vaere der. Og andre ikke.
// F.eks. All dependencies for a component... Is this really a feature??
// Dependencies for a component is the once only the component uses. For a container it is all
// required dependencies for the module
// Features vs en selvstaendig komponent....
//// Altsaa det ser jo dumt ud hvis vi har
//// /Foo
//// /Foo/Service<String>
//// /Foo/AnotherComponent

///// Dvs ogsaa scheduled jobs bliver lagt paa som meta data, som en feature
