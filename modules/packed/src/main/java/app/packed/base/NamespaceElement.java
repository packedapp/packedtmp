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
package app.packed.base;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.component.ComponentMirrorStream;
import app.packed.component.ComponentMirrorStream.Option;

/**
 * A component is the basic entity in Packed. Much like everything is a is one of the defining features of Unix, and its
 * derivatives. In packed everything is a component.
 */

// IDK.

// Maaske har man ikke lokalt adgang til fx at liste alle actors.
// Maaske bliver ting lave virtuelt hvis de ikke eksistere?

public interface NamespaceElement {

    /**
     * Returns an unmodifiable view of all of this component's children.
     *
     * @return an unmodifiable view of all of this component's children
     */
    Collection<NamespaceElement> children();

    /**
     * Returns the configuration site of this component.
     * 
     * @return the configuration site of this component
     */
//    /**
//     * Returns the configuration site of this application.
//     * <p>
//     * If this application was created from an {@link Image image}, this method will return the site where the image was
//     * created. Unless the AI.Wiring option is used when construction the application.
//     * 
//     * @return the configuration site of this application
//     */
    // ConfigSite configSite();

    /**
     * Returns the distance to the root component. The root component having depth 0.
     * 
     * @return the distance to the root component
     */
    int depth();

    /**
     * Returns the name of this component.
     * <p>
     * If no name is explicitly set by the user when configuring the component. The runtime will automatically generate a
     * name that is unique among other components with the same parent.
     *
     * @return the name of this component
     */
    String name();

    /**
     * Returns the parent component of this component. Or empty if this component has no parent.
     * 
     * @return the parent component of this component. Or empty if this component has no parent
     */
    Optional<NamespaceElement> parent();

    /**
     * Returns the path of this component.
     *
     * @return the path of this component
     */
    NamespacePath path();

    // add Optional<Component> tryResolve(CharSequence path);
    // Syntes ikke vi skal have baade tryResolve or resolve...
    NamespaceElement resolve(CharSequence path);

    /**
     * Returns a stream consisting of this component and all of its descendants in any order.
     *
     * @param options
     *            specifying the order and contents of the stream
     * 
     * @return a component stream consisting of this component and all of its descendants in any order
     */
    Stream<? extends NamespaceElement> stream(ComponentMirrorStream.Option... options);

    /**
     * Returns the root component of the namespace this component is located in.
     * 
     * @return the root component
     */
    default NamespaceElement root() {
        NamespaceElement c = this;
        Optional<NamespaceElement> p = parent();
        while (!p.isEmpty()) {
            c = p.get();
            p = c.parent();
        }
        return c;
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
    default void traverse(Consumer<? super NamespaceElement> action) {
        stream(Option.maxDepth(1)).forEach(action);
    }

    default Optional<NamespaceElement> tryResolve(CharSequence path) {
        throw new UnsupportedOperationException();
    }

}

///**
// * <p>
// * This operation does not allocate any objects internally.
// * 
// * @implNote Implementations of this method should never generate object (which is a bit difficult
// * @param action
// *            oops
// */
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
///**
//* If this component is a part of extension, returns the extension. Otherwise returns empty.
//* 
//* @return any extension this component belongs to
//*/
//// Don't really like this... It strongly ties a container to a component.
//// As extensions are children of containers always...
//// But then again ComponentStream.Option contains stuff about containers ect.
//// Maybe model it as an attribute
//Optional<Class<? extends Extension>> extension();

///**
//* Returns the type of component.
//* 
//* @return the type of component
//*/
//ComponentDescriptor model();

//  /**
//* Registers an action that will be performed whenever a name is assigned to the component.
//* <p>
//* This method is mainly used by extensions.
//* 
//* @param action
//*            the action to be performed when the name of the component is finalized
//*/
//default void onNamed(Consumer<? super ComponentConfiguration> action) {
//throw new UnsupportedOperationException();
//}
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
