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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.application.ApplicationImage;
import app.packed.application.BuildInfo;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.component.WireletHandle;
import app.packed.container.Extension.Subtension;
import app.packed.inject.Factory;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ExtensionSetup;

/**
 * A configuration object for an {@link Extension}.
 * <p>
 * Normally all configuration of extensions are done through the various protected final methods available when
 * extending {@link Extension}. However, for complex extensions where the logic cannot easily fit into a single class.
 * An extension configuration can be passed around. Make the fff available
 * 
 * <p>
 * An instance of this interface is normally acquired via {@link Extension#configuration()} or via constructor injected
 * it into any subclass of {@link Extension} (or any of its minors).
 * <p>
 * Since the extension itself defines most methods in this interface via protected final methods. This interface is
 * typically used in order to provide these methods to code that is defined outside of the actual extension
 * implementation, for example, code that is placed in another package.
 * <p>
 * <strong>Note:</strong> Instances of this class should never be exposed to end-users.
 */
// Does not extend CC as install/installinstance used parent as target
// Det er jo ikke rigtig tilfaeldet mere... efter vi har lavet om...
public /* sealed */ interface ExtensionConfiguration {

    // ComponentAttributes

    /**
     * Returns information about the build this extension is a part of.
     * 
     * @return information about the build this extension is a part of
     */
    BuildInfo build(); // I don't know if it should die...

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after {@link Extension#onComplete()} has been invoked by the runtime.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    void checkConfigurable();

    /**
     * Checks that child containers has been aded
     */
    // checkContainerFree, checkNoChildContainers
    void checkExtendable();

    /**
     * Returns the extension class.
     * 
     * @return the extension class
     */
    Class<? extends Extension> extensionClass();

    /**
     * Returns the extension instance.
     * 
     * @return the extension instance
     * @throws InternalExtensionException
     *             if trying to call this method from the constructor of the extension
     */
    Extension extensionInstance();

    // Will install the class in the specified Container

    // maybe userInstall
    // or maybe we just have userWire()
    // customWire
    // For hvorfor skal brugen installere en alm component via denne extension???
    // Vi skal vel altid have en eller anden specific component driver
    // BaseComponentConfiguration containerInstall(Class<?> factory);

    BaseComponentConfiguration install(Class<?> factory);

    BaseComponentConfiguration install(Factory<?> factory);

    /**
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see ContainerConfiguration#installInstance(Object)
     */
    BaseComponentConfiguration installInstance(Object instance);

    /**
     * Returns whether or not the extension is part of an {@link ApplicationImage}.
     * <p>
     * This can be used to clean up data structures that was only remember that people might still inspect the image
     * 
     * @return whether or not the extension is part of an image
     */
    boolean isPartOfImage(); // BoundaryTypes

    /**
     * Returns whether or not the specified extension is currently used by this extension, other extensions or user code.
     * 
     * @param extensionClass
     *            the extension to test
     * @return true if the extension is currently in use, otherwise false
     * @see Extension#isInUse(Class)
     */
    boolean isUsed(Class<? extends Extension> extensionClass);

    default <E extends Subtension> void lazyUse(Class<E> extensionClass, Consumer<E> action) {
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
     * @throws InternalExtensionException
     *             if called from outside of {@link Extension#onComplete()} (if wiring a container)
     * @see Extension#onComplete()
     */
    Component link(Assembly<?> assembly, Wirelet... wirelets);

    /**
     * Returns the path of the extension. The path of the extension's container, can be obtained by calling
     * <code>path().parent().get()</code>.
     * 
     * @return the path of the extension
     */
    NamespacePath path();

    /**
     * Returns an subtension instance for the specified subtension class. The specified type must be among the extension's
     * dependencies as specified via.... Otherwise an {@link InternalExtensionException} is thrown.
     * <p>
     * This method is not available from the constructor of an extension. If you need to call it from the constructor, you
     * can instead declare a dependency on {@link ExtensionConfiguration} and call
     * {@link ExtensionConfiguration#use(Class)}.
     * <p>
     * This method works similar to {@link ContainerConfiguration#use(Class)}.
     * 
     * @param <E>
     *            the type of subtension to return
     * @param subtensionClass
     *            the type of subtension that should be returned
     * @return the subtension
     * @throws IllegalStateException
     *             If invoked from the constructor of an extension. Or if the underlying container is no longer configurable
     *             and the subtensions underlying extension have not already been created
     * 
     * @see ContainerConfiguration#use(Class)
     * @see #isUsed(Class)
     */
    <E extends Subtension> E use(Class<E> subtensionClass);

    // Ideen er lidt at det er paa den her maade at extensionen
    // registrere bruger objekter...
    <C extends ComponentConfiguration> C userWire(ComponentDriver<C> driver, Wirelet... wirelets);

    /**
     * Wires a new child component using the specified driver
     * 
     * @param <C>
     *            the type of configuration returned by the driver
     * @param driver
     *            the driver to use for creating the component
     * @param wirelets
     *            any wirelets that should be used when creating the component
     * @return a configuration for the component
     */
    <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets);

    /**
     * @param <T>
     *            the type of wirelet to return a handle for
     * @param wireletType
     *            the type of wirelet to return a handle for
     * @return
     */
    <T extends Wirelet> WireletHandle<T> wirelets(Class<T> wireletType);

    @Nullable
    private static ExtensionSetup getExtensionSetup(MethodHandles.Lookup lookup, Component containerComponent) {
        requireNonNull(lookup, "containerComponent is null");

        // We only allow to call in directly on the container itself
        if (!containerComponent.modifiers().isContainerOld()) {
            throw new IllegalArgumentException("The specified component '" + containerComponent.path() + "' must have the Container modifier, modifiers = "
                    + containerComponent.modifiers());
        }

        // lookup.lookupClass() must point to the extension that should be extracted
        if (lookup.lookupClass() == Extension.class || !Extension.class.isAssignableFrom(lookup.lookupClass())) {
            throw new IllegalArgumentException("The lookupClass() of the specified lookup object must be a proper subclass of "
                    + Extension.class.getCanonicalName() + ", was " + lookup.lookupClass());
        }

        @SuppressWarnings("unchecked")
        Class<? extends Extension> extensionClass = (Class<? extends Extension>) lookup.lookupClass();
        // Must have full access to the extension class
        if (!lookup.hasFullPrivilegeAccess()) {
            throw new IllegalArgumentException("The specified lookup object must have full privilege access to " + extensionClass
                    + ", try creating a new lookup object using MethodHandles.privateLookupIn(lookup, " + extensionClass.getSimpleName() + ".class)");
        }

        ComponentSetup compConf = ComponentSetup.unadapt(lookup, containerComponent);
        return compConf.container.getExtensionContext(extensionClass);
    }

    /**
     * Typically used, for example, for testing.
     * 
     * The specified lookup must have the extension as its {@link Lookup#lookupClass()}. And
     * {@link Lookup#hasPrivateAccess()} must return true.
     * 
     * <p>
     * Calling this method after a container has been fully initialized will fail with {@link IllegalStateException}. As
     * containers never retain extensions at runtime. I don't even know if you can call it doing initialization
     * 
     * @param caller
     *            a lookup object for an extension class with {@link Lookup#hasFullPrivilegeAccess() full privilege access}
     * @param containerComponent
     *            the component to extract the configuration from.
     * @return the configuration of the extension if it has been configured, otherwise empty
     * @throws IllegalStateException
     *             if calling this method at runtime
     * @throws IllegalArgumentException
     *             if the {@link Lookup#lookupClass()} of the specified caller does not extend{@link Extension}. Or if the
     *             specified lookup object does not have full privileges
     */
    // Maybe take an extension class anyway. Then users to not need to teleport if called from outside of the extension
    // We should probably check that is has module access?

    // I don't know the exact extend we need these now. Previously, for example, ServiceContract made use of it
    // But now I think we will extract the information from component attributes
    @SuppressWarnings("unused")
    private static Optional<ExtensionConfiguration> lookupConfiguration(MethodHandles.Lookup caller, Class<? super Extension> extensionClass,
            Component containerComponent) {
        requireNonNull(caller, "caller is null");
        return Optional.ofNullable(getExtensionSetup(caller, containerComponent));
    }

    /**
     * @param <T>
     *            the type of extension to return
     * @param caller
     *            a lookup object for the specified extension class with {@link Lookup#hasFullPrivilegeAccess() full
     *            privilege access}
     * @param extensionClass
     *            the extension that we are trying to find
     * @param containerComponent
     *            the container component
     * @return the extension, or empty if no extension of the specified type is registered in the container
     */
    // We current dont use then
    @SuppressWarnings({ "unchecked", "unused" })
    private static <T extends Extension> Optional<T> lookupExtension(MethodHandles.Lookup caller, Class<T> extensionClass, Component containerComponent) {
        requireNonNull(caller, "caller is null");
        requireNonNull(extensionClass, "extensionClass is null");
        if (caller.lookupClass() != extensionClass) {
            throw new IllegalArgumentException(
                    "The specified lookup object must have the specified extensionClass " + extensionClass + " as lookupClass, was " + caller.lookupClass());
        }

        ExtensionSetup eb = getExtensionSetup(caller, containerComponent);
        return eb == null ? Optional.empty() : Optional.of((T) eb.extensionInstance());
    }
}
