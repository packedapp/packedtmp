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
import java.util.function.Consumer;

import app.packed.application.Application;
import app.packed.attribute.AttributedElement;
import app.packed.base.NamespacePath;
import app.packed.component.ComponentStream.Option;

/**
 * A component is the basic entity in Packed. Much like everything is a is one of the defining features of Unix, and its
 * derivatives. In packed everything is a component.
 */
public /* sealed */ interface Component extends AttributedElement {

    default Application application() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an unmodifiable view of all of this component's children.
     *
     * @return an unmodifiable view of all of this component's children
     */
    Collection<Component> children();

    /**
     * Returns the distance to the root component. The root component having depth 0.
     * 
     * @return the distance to the root component
     */
    int depth();

    default boolean hasModifier(ComponentModifier modifier) {
        return modifiers().contains(modifier);
    }

    boolean isInSame(ComponentScope scope, Component other);

    /**
     * Returns the modifiers of this component.
     * 
     * @return the modifiers of this component
     * 
     * @see #hasModifier(ComponentModifier)
     */
    ComponentModifierSet modifiers();

    /**
     * Returns the name of this component.
     * <p>
     * If no name is explicitly set by the user when configuring the component. The runtime will automatically generate a
     * name that is unique among other components with the same parent.
     *
     * @return the name of this component
     */
    String name();

    /** {@return the parent component of this component. Or empty if this component has no parent} */
    Optional<Component> parent();

    /**
     * Returns the path of this component.
     *
     * @return the path of this component
     */
    NamespacePath path();

    default void print() {
        // Super useful...
        throw new UnsupportedOperationException();
    }

    /**
     * Computes the relation from this component to the specified component.
     * 
     * @param other
     *            the other component
     * @return the relation to the other component
     */
    ComponentRelation relationTo(Component other);

    // Now that we have parents...
    // add Optional<Component> tryResolve(CharSequence path);
    // Syntes ikke vi skal have baade tryResolve or resolve...
    Component resolve(CharSequence path);

    /**
     * Returns the root component of the namespace this component is located in.
     * 
     * @return the root component
     */
    default Component root() {
        Component c = this;
        Optional<Component> p = parent();
        while (!p.isEmpty()) {
            c = p.get();
            p = c.parent();
        }
        return c;
    }

    /**
     * Returns a stream consisting of this component and all of its descendants in any order.
     *
     * @param options
     *            specifying the order and contents of the stream
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
    default void traverse(Consumer<? super Component> action) {
        stream(Option.maxDepth(1)).forEach(action);
    }

    default Optional<Component> tryResolve(CharSequence path) {
        throw new UnsupportedOperationException();
    }

//    // The returned component is always a system component
//    default Component viewAs(Object options) {
//        // F.eks. tage et system. Og saa sige vi kun vil
//        // se paa den aktuelle container
//
//        // Ideen er lidt at vi kan taege en component
//        // Og f.eks. lave den om til en rod...
//        // IDK. F.eks. hvis jeg har guests app.
//        // Saa vil jeg gerne kunne sige til brugere...
//        // Her er en clean Guest... Og du kan ikke se hvad
//        // der sker internt...
//        throw new UnsupportedOperationException();
//    }

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
