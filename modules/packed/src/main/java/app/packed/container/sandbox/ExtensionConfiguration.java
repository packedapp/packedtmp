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
package app.packed.container.sandbox;

import java.util.function.Consumer;

import app.packed.application.ApplicationInfo;
import app.packed.base.NamespacePath;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.ExtensionPoint;
import app.packed.container.InternalExtensionException;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;

/**
 * A configuration of an {@link Extension}.
 * <p>
 * Normally all configuration of an extension is done via the protected methods available on {@link Extension}. However,
 * for complex extensions where the logic cannot easily fit into a single class. This configuration can be passed around
 * in order to invoke the needed methods.
 * <p>
 * An instance of this interface is normally acquired via {@link Extension#container()} or by constructor injecting
 * it into a subclass of {@link Extension}.
 * <p>
 * <strong>Note:</strong> Instances of this interface should never be exposed to end-users.
 */
// Features
// * Application Info
// * Get/check state
// * find family members
// * Get extension support
// * Get wirelets

// I sidste ende doede denne da vi fik simplificeret ServiceExtension.
// Also, vi skulle lidt at generficere den. Hvilket egentligt saa lidt aandsvagt ud
public interface ExtensionConfiguration {

    /** {@return a descriptor for the application the extension is a part of.} */
    ApplicationInfo application(); // Why not mirror for this but for container??? IDK

    /**
     * Checks that the extension is configurable within the assembly where it is being defined, throwing
     * {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer user configurable after the build method of the assembly has returned.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable.
     */
    // Hvad med extension.newContainer()??? Det er en user realm, men giver vel ikke mening at checke
    void checkConfigurable();

//    /**
//     * @param <C>
//     *            the type of composer
//     * @param composer
//     *            the composer
//     * @param action
//     *            action to execute
//     * @throws IllegalStateException
//     *             if the specified composer has already been composed
//     */
//    // Taenker det stadig ser ud som om vi kommer fra samme assembly
//    <C extends AbstractComposer> void compose(C composer, ComposerAction<? super C> action);
//
////    default ExtensionConfiguration extract(Extension<?> extension) {
////        if (extension.getExtensionType == configuration.getExtensionType) {
////            // ok
////        }
////        // fail
////    }

    /** {@return the path of the container where the extension is used.} */
    NamespacePath containerPath();

    /**
     * Returns whether or not the specified extension is currently used (in the same container) by this extension, other
     * extensions, or application code.
     * 
     * @param extensionType
     *            the extension type to test
     * @return {@code true} if the extension is currently in use, otherwise {@code false}
     * @see Extension#isExtensionUsed(Class)
     * @implNote Packed does not perform detailed tracking on which extensions use other extensions. As a consequence it
     *           cannot give a more detailed answer about who is using a particular extension
     */
    // Altsaa den snyder jo rigtig meget...
    // Eftersom man kan vaere fristet til at teste den
    boolean isExtensionUsed(Class<? extends Extension<?>> extensionType);

    /** {@return whether or not the extension instance is used by the root container of the application.} */
    boolean isRootOfApplication();

//    default boolean isRootOfLifetime() {
//        return isRootOfApplication();
//    }

    /**
     * Returns a selection of all wirelets of the specified type.
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
     *             if the specified wirelet class is not a proper subclass of {@link Wirelet}. Or if the specified class is
     *             not located in the same module as the extension itself
     */
    // Skal vi have en boolean allowsRuntimeWirelets() metode???
    <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletType);

    /**
     * Returns an extension point of the specified type.
     * <p>
     * The extension the specified extension point is a member of must be among this extension's declared dependencies.
     * <p>
     * This method works similar to {@link ContainerConfiguration#use(Class)} except it does not return the extension but
     * its support class instead.
     * 
     * @param <E>
     *            the type of extension point to return
     * @param extensionPointType
     *            the type of extension point that should be returned
     * @return an extension point of the specified type
     * @throws IllegalStateException
     *             If the underlying container is no longer configurable and the extension for which the extension point is
     *             a member of has not already been used
     * @throws InternalExtensionException
     *             if the extension for which the extension point is a member of is not a declared dependency of this
     *             extension
     * 
     * @see Extension#use(Class)
     * @see ContainerConfiguration#use(Class)
     * @see #isExtensionUsed(Class)
     */
    <E extends ExtensionPoint<?>> E use(Class<E> extensionPointType);
}

interface Zandbox {

    /**
     * Returns whether or not the specified extension type is disabled in the container from where this extension is used.
     * 
     * @param extensionType
     *            the type of extension to test
     * @return true if disabled, otherwise false
     */
    default boolean isExtensionBanned(Class<? extends Extension<?>> extensionType) {
        throw new UnsupportedOperationException();
    }

//    default <E extends OldExtensionMember<?>> Stream<ExtensionBeanConnection<E>> findAllAncestors(Class<E> ancestorType) {
//        throw new UnsupportedOperationException();
//    }

    default <E extends ExtensionPoint<?>> void lazyUse(Class<E> extensionType, Consumer<E> action) {
        // Iff at some point the extension is activated... Run the specific action
        // fx .lazyUse(ConfigurationExtension.Sub.class, c->c.registerConfSchema(xxx));

        // Kunne maaske hellere have en annoteret metode

        // Skal nok ogsaa have en version der checker her og nu.
        // Maaske der returnere en Optional
        // Altsaa hvis vi registere en configuration sche
        throw new UnsupportedOperationException();
    }

    /**
     * Registers an action than run irregardless of whether or not the build fails by throwing an exception.
     * 
     * @param runnable
     *            a runnable the will be run even if the assembly fails to build
     */
    // Taenker det er en cleanup action der bliver koert til allersidst
    // af hele buildet eller assemblien???
    // Stjael wording fra Cleaner
    // https://cr.openjdk.java.net/~mcimadamore/panama/foreign-finalize-javadoc/javadoc/jdk/incubator/foreign/ResourceScope.html
    // They use addCloseAction
    default void registerBuildCleaner(Runnable runnable /* , boolean onlyRunOnFailure */ ) {
        throw new UnsupportedOperationException();
    }

}