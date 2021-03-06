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
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.base.NamespacePath;
import app.packed.component.ComponentMirrorStream.Option;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMember;
import app.packed.mirror.Mirror;

/**
 * A mirror of a component.
 * <p>
 * A component is the basic entity in Packed. Much like everything is a is one of the defining features of Unix, and its
 * derivatives. In packed everything is a component.
 */
public /* sealed */ interface ComponentMirror extends Mirror {

    // ComponentDriverMirror driver(); IDK hvad skal den sige, andet end hvilken extension

    /** {@return the application this component is a part of.} */
    ApplicationMirror application();

    /** {@return an unmodifiable view of all of the children of this component.} */
    Collection<ComponentMirror> children();

    Stream<ComponentMirror> components();

    /** {@return the container this component is a part of. If the underlying component is a container, returns this.} */
    ContainerMirror container();

    /** {@return the distance to the root component, the root component having depth {@code 0}.} */
    int depth();

    default void forEachComponent(Consumer<? super ComponentMirror> action) {
        components().forEach(action);
    }

    default ComponentMirror in(ComponentScope boundary) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param scope
     *            the scope to check
     * @param other
     *            the other component to check
     * @return true if this component and the specified component is in the same specified scope, otherwise false
     */
    boolean isInSame(ComponentScope scope, ComponentMirror other);

    /**
     * Returns any extension the bean's driver is part of. All drivers are either part of an extension. Or is a build in
     * drive
     * <p>
     * Another thing is extension member, which is slightly different.
     * 
     * @return any extension the bean's driver is part of
     */
    // Hvem ejer den bean driver, der er blevet brugt til at registrere bean'en...
    // Det er samtidig ogsaa den extension (if present) som evt. ville kunne instantiere den

    // Altsaa den giver jo ogsaa mening for en funktion. Ikke rigtig for en container dog
    // Eller en TreeBean (som jeg taenker aldrig kan registreres via en extension)
    // Saa maaske skal den flyttes ned paa component

    // Tror maaske den skal op paa ComponentMirror...
    // Ved ikke om vi kan definere end ContainerDriver for en extension???
    // Det primaere er vel injection
    // Er det i virkeligheden altid ownership???
    // Har vi tilfaelde hvor vi har en ikke-standard bean driver.
    // Hvor det ikke er extension'en der soerger for instantiering
    Optional<Class<? extends Extension>> managedByExtension();

    /**
     * Returns any extension the component is a part of. Or empty if the component is wired by an user.
     * 
     * {@return empty if the component is installed by the user, otherwise the extension that the component is a member of.}
     * 
     * @see ExtensionMember
     */
    Optional<Class<? extends Extension>> memberOfExtension();

    /**
     * Returns the name of this component.
     * <p>
     * If no name is explicitly set by the user when configuring a component. Packed will automatically assign a name that
     * is unique among other components with the same parent.
     *
     * @return the name of this component
     */
    String name();

    /** {@return the parent component of this component. Or empty if a root component.} */
    Optional<ComponentMirror> parent();

    /** {@return the path of this component} */
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
    ComponentMirror.Relation relationTo(ComponentMirror other);

    // Now that we have parents...
    // add Optional<Component> tryResolve(CharSequence path);
    // Syntes ikke vi skal have baade tryResolve or resolve...
    ComponentMirror resolve(CharSequence path);

    /** {@return the root component} */
    ComponentMirror root();

    /**
     * Returns a stream consisting of this component and all of its descendants in any order.
     *
     * @param options
     *            specifying the order and contents of the stream
     * 
     * @return a component stream consisting of this component and all of its descendants in any order
     */
    ComponentMirrorStream stream(ComponentMirrorStream.Option... options);

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
    default void traverse(Consumer<? super ComponentMirror> action) {
        stream(Option.maxDepth(1)).forEach(action);
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

    default Optional<ComponentMirror> tryResolve(CharSequence path) {
        throw new UnsupportedOperationException();
    }

    /**
     * A (component mirror) relation is an unchangeable representation of a directional relationship between two components.
     * <p>
     * It is typically created via {@link ComponentMirror#relationTo(ComponentMirror)}.
     */
    public /* sealed */ interface Relation extends Iterable<ComponentMirror> {

        /**
         * -1 if {@link #source()} and {@link #target()} are not in the same system. 0 if source and target are identical.
         * 
         * @return the distance between the two components
         */
        int distance();

        /**
         * Finds the lowest common ancestor for the two components if they are in the same system. Or {@link Optional#empty()}
         * if not in the same system.
         * 
         * @return lowest common ancestor for the two components. Or empty if not in the same system
         */
        Optional<ComponentMirror> findLowestCommonAncestor();

        /**
         * Returns whether or not the two components are in the same application.
         * 
         * @return whether or not the two components are in the same application
         */
        boolean inSameApplication();

        /**
         * Returns whether or not the two components are in the same container.
         * 
         * @return whether or not the two components are in the same container
         */
        boolean inSameContainer();

        boolean isInSame(ComponentScope scope);

        /**
         * Returns whether or not the two components are in the same system. Two components are in the same system, iff they
         * have the same {@link ComponentMirror#root() system component}.
         * 
         * @return whether or not the two components are in the same system
         * @see ComponentMirror#root()
         */
        default boolean isInSameNamespace() {
            return isInSame(ComponentScope.NAMESPACE);
        }

        // Just here because it might be easier to remember...
        // isStronglyWired...
        default boolean isStronglyWired() {
            throw new UnsupportedOperationException();
        }

        default boolean isWeaklyWired() {
            throw new UnsupportedOperationException();
        }

        /**
         * The source of the relation.
         * 
         * @return the source of the relation
         */
        ComponentMirror source();

        /**
         * The target of the relation.
         * 
         * @return the target of the relation
         */
        ComponentMirror target();
    }

    // Taenker den er immutable...

    // Problemet er at vi vil have en snapshot...
    // Tager vi snapshot af attributer??? Nej det goer vi altsaa ikke...
    // Rimlig meget overhead

    // Tror topol

    // Vi skal ikke have noget live...
    // Maaske af det derfor

    // Special cases

    // Not In same system
    // -> distance = -1, inSameContainer=false, isStronglyBound=false, isWeaklyBound=false

    // Same component
    // -> distance = 0, inSameContainer=true, isStrongBound=?, isWeaklyBound=false

    // Teanker vi flytter den et andet sted end attributer...

    // Altsaa den walk man kan lave via Iterable... ville det vaere rart at kunne beskrive relationsship
    // for den.. Altsaa cr[4] er foraeldre til cr[3] og saa

    // A component in a runtime system is not the same as in the build time system.

    // I would say the restartable image of one system is not in the same system as
    // the same restartable image when we have restarted.

    // https://en.wikipedia.org/wiki/Path_(graph_theory)
    // ComponentConnection

    // A ComponentRelation is directed
    // Can we have attributes??
    // if (rel.inSameContainer())

    // En relation er jo mere end topology... Syntes

    // walkFromSource(); Syntes maaske ikke den skal extende Iteralble...
    // Maaske fromSourceIterator
    // https://en.wikipedia.org/wiki/Tree_structure
    // description()? -> Same, parent, child, descendend, ancestor,

    // default WiringStrength wiringStrength() {
//        return hasSameGuest() ? WiringStrength.STRONG : WiringStrength.WEAK;
    // }

    // interface InterExtensionRelationship {
    // from, to
    // isX
    // distance
    // }
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
