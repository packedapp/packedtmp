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

import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.BuildInfo;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Image;
import app.packed.component.Wirelet;
import app.packed.container.Extension.Subtension;
import app.packed.inject.Factory;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ExtensionSetup;

/**
 * A configuration object for extensions. An instance of this interface is available via
 * {@link Extension#configuration()} or via constructor injection into any subclass of {@link Extension}. Since the
 * extension itself defines most methods in this interface via protected final methods. This interface is typically used
 * in order to provide these methods to code that is defined outside of the actual extension implementation, for
 * example, code that is placed in another package.
 * <p>
 * <strong>Note:</strong> Instances of this class should never be exposed to end-users.
 */
// Does not extend CC as install/installinstance used parent as target
// Det er jo ikke rigtig tilfaeldet mere... efter vi har lavet om...
public /* sealed */ interface ExtensionConfiguration {

    // ComponentAttributes

    // Thinking about removing this
    // Altsaa det den er god for er at tilfoeje callbacks...
    // Men det behoever vi jo ikke have et interface for..
    BuildInfo build();

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after {@link Extension#extensionConfigured()} has been invoked by the runtime.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    void checkConfigurable();

    /**
     * Checks that child cubes has been aded
     */
    void checkIsLeafBundle();

    /**
     * Returns the extension instance.
     * 
     * @return the extension instance
     * @throws IllegalStateException
     *             if trying to call this method from the constructor of the extension
     */
    Extension extension();

    /**
     * Tells which extension class is being configured.
     * 
     * @return the extension class
     */
    Class<? extends Extension> extensionClass();

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
     * Returns whether or not the extension is part of an {@link Image}.
     * <p>
     * This can be used to clean up data structures that was only remember that people might still inspect the image
     * 
     * @return whether or not the extension is part of an image
     */
    boolean isPartOfImage(); // BoundaryTypes

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
     * Creates a new container with this extensions container as its parent by linking the specified bundle. The new
     * container will have this extension as owner. Thus will be hidden from normal view
     * <p>
     * The parent component of the linked bundle will have the cube of this extension as its parent.
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            optional wirelets
     */
    // I think the assembly needs to be annotated with @ExtensionMember IDK
    void link(Assembly<?> bundle, Wirelet... wirelets);

    /**
     * Returns the path of the extension. The path of the extension's container, can be obtained by calling
     * <code>path().parent().get()</code>.
     * 
     * @return the path of the extension
     */
    NamespacePath path();

    /**
     * Returns an extension of the specified type. The specified type must be among the extension's dependencies as
     * specified via.... Otherwise an {@link InternalExtensionException} is thrown.
     * <p>
     * This method works similar to {@link ContainerConfiguration#use(Class)}.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionClass
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             If invoked from the constructor of the extension. Or if the underlying container is no longer
     *             configurable and an extension of the specified type has not already been installed
     * @throws UnsupportedOperationException
     *             if the specified extension type is not specified when bootstrapping
     * 
     * @see ContainerConfiguration#use(Class)
     */
    <E extends Subtension> E use(Class<E> extensionClass);

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

    @Nullable
    private static ExtensionSetup getExtensionConfiguration(MethodHandles.Lookup lookup, Component containerComponent) {
        requireNonNull(lookup, "containerComponent is null");

        // We only allow to call in directly on the container itself
        if (!containerComponent.modifiers().isContainer()) {
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
     * @return an optional containing the extension if it has been configured, otherwise empty
     * @throws IllegalStateException
     *             if calling this method at runtime
     * @throws IllegalArgumentException
     *             if the {@link Lookup#lookupClass()} of the specified caller does not extend{@link Extension}. Or if the
     *             specified lookup object does not have full privileges
     */
    static Optional<ExtensionConfiguration> privateLookup(MethodHandles.Lookup caller, Component containerComponent) {
        requireNonNull(caller, "caller is null");
        return Optional.ofNullable(getExtensionConfiguration(caller, containerComponent));
    }

    /**
     * @param <T>
     *            the type of extension to return
     * @param lookup
     *            a lookup object for the specified extension class with {@link Lookup#hasFullPrivilegeAccess() full
     *            privilege access}
     * @param extensionClass
     *            the extension that we are trying to find
     * @param containerComponent
     *            the container component
     * @return the extension, or empty if no extension of the specified type is registered in the container
     */
    @SuppressWarnings("unchecked")
    static <T extends Extension> Optional<T> privateLookupExtension(MethodHandles.Lookup lookup, Class<T> extensionClass, Component containerComponent) {
        requireNonNull(lookup, "lookup is null");
        requireNonNull(extensionClass, "extensionClass is null");
        if (lookup.lookupClass() != extensionClass) {
            throw new IllegalArgumentException(
                    "The specified lookup object must have the specified extensionClass " + extensionClass + " as lookupClass, was " + lookup.lookupClass());
        }

        ExtensionSetup eb = getExtensionConfiguration(lookup, containerComponent);
        return eb == null ? Optional.empty() : Optional.of((T) eb.extension());
    }
}
