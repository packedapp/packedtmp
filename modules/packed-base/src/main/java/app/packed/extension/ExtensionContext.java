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
package app.packed.extension;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationImage;
import app.packed.component.BaseBeanConfiguration;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.component.WireletSelection;
import app.packed.container.BaseContainerConfiguration;
import app.packed.container.ContainerAssembly;
import app.packed.container.ContainerMirror;
import app.packed.container.ContainerWirelet;
import app.packed.extension.Extension.Subtension;
import app.packed.extension.old.ExtensionBeanConnection;
import app.packed.inject.Factory;

/**
 * A context object for an {@link Extension}.
 * <p>
 * Normally all configuration of extensions are done via the protected final methods declared on {@link Extension}.
 * However, for complex extensions where the logic cannot easily fit into a single class. An extension context instance
 * can be passed around in order to invoke the needed methods.
 * <p>
 * Since the extension itself defines most methods in this interface via protected final methods. This interface is
 * typically used in order to provide these methods to code that is defined outside of the actual extension
 * implementation, for example, code that is placed in another package.
 * <p>
 * An instance of this interface is normally acquired via {@link Extension#configuration()} or by constructor injecting
 * it into an {@link Extension} subclass.
 * <p>
 * <strong>Note:</strong> Instances of this interface should never be exposed to end-users.
 */
public /* sealed */ interface ExtensionContext {

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after {@link Extension#onComplete()} has been invoked by the runtime.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    void checkIsPreCompletion();

    /**
     * Checks that Checks that child containers has been aded
     */
    void checkIsPreLinkage();

    default <E extends ExtensionMember<?>> Stream<ExtensionBeanConnection<E>> findAllAncestors(Class<E> ancestorType) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param <E>
     *            the type of ancestor to find
     * @param ancestorType
     *            the type of ancestor to find
     * @return
     */
    <E> ExtensionBeanConnection<E> findAncestor(Class<E> ancestorType);

    // Supporter Extension, AutoBean or common interface
    // all must be in same module
    /**
     * Attempts to find a parent of the specified type.
     * 
     * @param <E>
     *            the type of parent to find
     * @param parentType
     * @return
     * @throws ClassCastException
     *             if a parent was found but it was not of the specified type
     */
    // Vi skal vaek fra optional syntes jeg... IDK Giver det mening at have et resultat inde i en optional...
    // Ja det goer det jo saadan set...
    <E> Optional<ExtensionBeanConnection<E>> findParent(Class<E> parentType);

    // A new instance. Ligesom install(bean)

    // Hvad sker der hvis det er en platform extensor, som allerede er installeret og navngivet???
    BaseBeanConfiguration install(Class<?> implementation);

    // maybe userInstall
    // or maybe we just have userWire()
    // customWire
    // For hvorfor skal brugen installere en alm component via denne extension???
    // Vi skal vel altid have en eller anden specific component driver
    // BaseComponentConfiguration containerInstall(Class<?> factory);
    BaseBeanConfiguration install(Factory<?> factory);

    // Will install the class in the specified Container
    // Hvad hvis vi bare vil finde en extension...
    // Maaske skal vi ikke fejle... Men bare ikke reportere noget...

    /**
     * 
     * Extensors installed via this method shou
     * 
     * @param instance
     *            the extensor instance to install
     * @return the configuration of the extensor
     * @see #installExtensor(Class, Wirelet...)
     * @see #installExtensor(Factory, Wirelet...)
     */
    BaseBeanConfiguration installInstance(Object instance);

    /**
     * Returns whether or not the specified extension type is disabled in the container from where this extension is used.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return true if disabled, otherwise false
     */
    default boolean isExtensionDisabled(Class<? extends Extension> extensionType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not the specified extension is used by this extension, other extensions, or user code in the same
     * container as this extension.
     * 
     * @param extensionType
     *            the extension type to test
     * @return {@code true} if the extension is currently in use, otherwise {@code false}
     * @see Extension#isExtensionUsed(Class)
     * @implNote Packed does not perform detailed tracking on which extensions use other extensions. As a consequence it
     *           cannot give a more detailed answer about who is using a particular extension
     */
    boolean isExtensionUsed(Class<? extends Extension> extensionType);

    /**
     * Returns whether or not the extension is part of an {@link ApplicationImage}.
     * <p>
     * This can be used to clean up data structures that was only remember that people might still inspect the image
     * 
     * @return whether or not the extension is part of an image
     */
    // Problemet her med build target... er at en sub application kan definere et image...
    // Maaske bare build target...
    boolean isPartOfImage(); // BoundaryTypes

    default <E extends Subtension> void lazyUse(Class<E> extensionType, Consumer<E> action) {
        // Iff at some point the extension is activated... Run the specific action
        // fx .lazyUse(ConfigurationExtension.Sub.class, c->c.registerConfSchema(xxx));

        // Kunne maaske hellere have en annoteret metode

        // Skal nok ogsaa have en version der checker her og nu.
        // Maaske der returnere en Optional
        // Altsaa hvis vi registere en configuration sche
        throw new UnsupportedOperationException();
    }

    /**
     * Links the specified assembly. This method must be called from {@link Extension#onComplete()}. Other
     * 
     * <p>
     * Creates a new container with this extensions container as its parent by linking the specified assembly. The new
     * container will have this extension as owner. Thus will be hidden from normal view
     * <p>
     * The parent component of the linked assembly will have the container of this extension as its parent.
     * 
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a model of the component that was linked
     * @throws InternalExtensionException
     *             if called from outside of {@link Extension#onComplete()} (if wiring a container)
     * @see Extension#onComplete()
     */
    // hvad goer vi med link af bruger assembly vs extension selv...
    ContainerMirror link(ContainerAssembly<?> assembly, Wirelet... wirelets);

    /**
     * Selects all container wirelets of the specified type.
     * <p>
     * The wirelets selected...
     * 
     * <p>
     * This does not include potential runtime wirelets which the user might specify when launching from an image. Maaske
     * skal vi have en isRuntimeWireletsAllowed (if root and is image)
     * 
     * @param <T>
     *            the type of wirelets to select
     * @param wireletType
     *            the type of wirelets to return a selection for
     * @return a wirelet selection of the specified type
     * @throws IllegalArgumentException
     *             if the specified wirelet class is not a proper subclass of {@link ContainerWirelet}. Or if the specified
     *             class is not located in the same module as the extension itself
     */
    <T extends ContainerWirelet> WireletSelection<T> selectWirelets(Class<T> wireletType);

    /**
     * Returns an subtension instance for the specified subtension class. The specified type must be among the extension's
     * dependencies as specified via....
     * <p>
     * This method is not available from the constructor of an extension. If you need to call it from the constructor, you
     * can instead declare a dependency on {@link ExtensionContext} and call {@link ExtensionContext#use(Class)}.
     * <p>
     * This method works similar to {@link BaseContainerConfiguration#use(Class)}.
     * 
     * @param <E>
     *            the type of subtension to return
     * @param subtensionClass
     *            the type of subtension that should be returned
     * @return the subtension
     * @throws IllegalStateException
     *             If invoked from the constructor of an extension. Or if the underlying container is no longer configurable
     *             and the subtensions underlying extension have not already been created
     * @throws InternalExtensionException
     *             if the specified subtension's extension is not a direct dependency of this extension
     * 
     * @see BaseContainerConfiguration#use(Class)
     * @see #isExtensionUsed(Class)
     */
    <E extends Subtension> E use(Class<E> subtensionClass);

    /**
     * 
     * @param <C>
     *            the type of component configuration that is being returned to the user
     * @param driver
     *            the component driver created by the extension
     * @param wirelets
     *            optional wirelets provided by the user (or the extension itself)
     * @return a component configuration object that can be returned to the user
     * @throws InternalExtensionException
     *             if the specified driver is not created by the extension itself
     */
    <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets);
}
///**
//* Returns the extension instance.
//* 
//* @return the extension instance
//* @throws InternalExtensionException
//*             if trying to call this method from the constructor of the extension
//*/
//// Lad os se om den kan bruges fra hooks... eller lignende
//Extension instance();

//default boolean isConnected() {
//// isInterConnected?
//// isJoined (sounds very permanent)
//throw new UnsupportedOperationException();
//}
//
//boolean isConnectedInSameApplication();
//
//boolean isConnectedWithParent();

// Previously used for getting hold of an extension from a mirror..
// I don't think we need this anymore after mirrors

///**
//* Typically used, for example, for testing.
//* 
//* The specified lookup must have the extension as its {@link Lookup#lookupClass()}. And
//* {@link Lookup#hasPrivateAccess()} must return true.
//* 
//* <p>
//* Calling this method after a container has been fully initialized will fail with {@link IllegalStateException}. As
//* containers never retain extensions at runtime. I don't even know if you can call it doing initialization
//* 
//* @param caller
//*            a lookup object for an extension class with {@link Lookup#hasFullPrivilegeAccess() full privilege access}
//* @param containerComponent
//*            the component to extract the configuration from.
//* @return the configuration of the extension if it has been configured, otherwise empty
//* @throws IllegalStateException
//*             if calling this method at runtime
//* @throws IllegalArgumentException
//*             if the {@link Lookup#lookupClass()} of the specified caller does not extend{@link Extension}. Or if the
//*             specified lookup object does not have full privileges
//*/
//// Maybe take an extension class anyway. Then users to not need to teleport if called from outside of the extension
//// We should probably check that is has module access?
//
//// I don't know the exact extend we need these now. Previously, for example, ServiceContract made use of it
//// But now I think we will extract the information from component attributes
//@SuppressWarnings("unused")
//private static Optional<ExtensionConfiguration> lookupConfiguration(MethodHandles.Lookup caller, Class<? super Extension> extensionClass,
//     ComponentMirror containerComponent) {
// requireNonNull(caller, "caller is null");
// return Optional.ofNullable(ExtensionSetup.extractExtensionSetup(caller, containerComponent));
//}
//

//default <C extends ComponentConfiguration> C subWire(Class<? extends Extension> subtension, ComponentDriver<C> driver, Wirelet... wirelets) {
//    /// Maaske har vi alligevel brug for SubtensionConfiguration...
//    // Ellers kan vi jo impresonate alle extensions.
//
//    // Hvis vi goer det... syntes jeg vi skal smide den ud i en seperat klasse..
//    throw new UnsupportedOperationException();
//}

///**
//* @param <T>
//*            the type of extension to return
//* @param caller
//*            a lookup object for the specified extension class with {@link Lookup#hasFullPrivilegeAccess() full
//*            privilege access}
//* @param extensionClass
//*            the extension that we are trying to find
//* @param containerComponent
//*            the container component
//* @return the extension, or empty if no extension of the specified type is registered in the container
//*/
//// We current dont use then
//@SuppressWarnings({ "unchecked", "unused" })
//// Maaske kan vi goere noget smart fra mirrors...
//// Bare checke at de er samme module..
//private static <T extends Extension> Optional<T> lookupExtension(MethodHandles.Lookup caller, Class<T> extensionClass, ComponentMirror containerComponent) {
// requireNonNull(caller, "caller is null");
// requireNonNull(extensionClass, "extensionClass is null");
// if (caller.lookupClass() != extensionClass) {
//     throw new IllegalArgumentException(
//             "The specified lookup object must have the specified extensionClass " + extensionClass + " as lookupClass, was " + caller.lookupClass());
// }
//
// ExtensionSetup eb = ExtensionSetup.extractExtensionSetup(caller, containerComponent);
// return eb == null ? Optional.empty() : Optional.of((T) eb.extensionInstance());
//}