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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.base.TreePath;
import app.packed.bundle.Extension.Subtension;
import app.packed.component.BeanConfiguration;
import app.packed.component.BuildContext;
import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.ComponentClassDriver;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentFactoryDriver;
import app.packed.component.ComponentInstanceDriver;
import app.packed.component.Image;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.inject.Factory;
import packed.internal.component.ComponentBuild;
import packed.internal.cube.ExtensionBuild;

/**
 * An instance of this interface is available via {@link Extension#configuration()} or via constructor injection into
 * any subclass of {@link Extension}. Since the extension itself defines most methods in this interface via protected
 * final methods. This interface is typically used to be able to provide these methods to code that is not located on
 * the extension implementation or in the same package as the extension itself.
 * <p>
 * Instances of this class should never be exposed to end-users.
 * 
 * @apiNote In the future, if the Java language permits, {@link ExtensionConfiguration} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
// Does not extend CC as install/installinstance used parent as target
public interface ExtensionConfiguration {

    // ComponentAttributes

    // Thinking about removing this
    // Altsaa det den er god for er at tilfoeje callbacks...
    // Men det behoever vi jo ikke have et interface for..
    BuildContext build();

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after the extension's configured method has been invoked by the runtime.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    void checkConfigurable();

    /**
     * Checks that child cubes has been aded
     */
    void checkNoChildCubes();

    /**
     * Returns the config site of the container the extension is registered with.
     * 
     * @return the config site of the container the extension is registered with
     */
    ConfigSite cubeConfigSite();

    /**
     * Returns the type of extension this context wraps.
     * 
     * @return the type of extension this context wraps
     */
    Class<? extends Extension> extensionType(); // replace with descriptor???

    <T> BeanConfiguration<T> install(Class<T> factory);

    <T> BeanConfiguration<T> install(Factory<T> factory);

    /**
     * @param <T>
     *            the type of the component
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see BundleConfiguration#installInstance(Object)
     */
    <T> BeanConfiguration<T> installInstance(T instance);

    /**
     * Returns whether or not the extension is part of an {@link Image}.
     * <p>
     * This can be used to clean up data structures that was only remember that people might still inspect the image
     * 
     * @return whether or not the extension is part of an image
     */
    boolean isPartOfImage();

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
    void link(Assembly<?> bundle, Wirelet... wirelets);

    /**
     * Returns the component path of the extension. The path of the extension's container, can be obtained by calling
     * <code>path.parent().get()</code>.
     * 
     * @return the component path of the extension
     */
    TreePath path();

    <E extends Subtension> E use(Class<E> extensionType);

    /**
     * Returns an extension of the specified type. The specified type must be among the extension's dependencies as
     * specified via.... Otherwise an {@link InternalExtensionException} is thrown.
     * <p>
     * This method works similar to {@link BundleConfiguration#use(Class)}. However, this method checks that only extensions
     * that have been declared as dependencies via {@link ExtensionSetup#dependencies()} are specified. This is done in
     * order to make sure that no extensions ever depend on each other.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             If invoked from the constructor of the extension. Or if the underlying container is no longer
     *             configurable and an extension of the specified type has not already been installed
     * @throws UnsupportedOperationException
     *             if the specified extension type is not specified via {@link ExtensionSetup} on this extension.
     * 
     * @see BundleConfiguration#use(Class)
     */
    <E extends Extension> E useOld(Class<E> extensionType);

    default <C, I> C wire(ComponentClassDriver<C, I> driver, Class<? extends I> implementation, Wirelet... wirelets) {
        ComponentDriver<C> cd = driver.bind(implementation);
        return wire(cd, wirelets);
    }

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
    <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets);

    default <C, I> C wire(ComponentFactoryDriver<C, I> driver, Factory<? extends I> implementation, Wirelet... wirelets) {
        ComponentDriver<C> cd = driver.bind(implementation);
        return wire(cd, wirelets);
    }

    default <C, I> C wireInstance(ComponentInstanceDriver<C, I> driver, I instance, Wirelet... wirelets) {
        ComponentDriver<C> cd = driver.bindInstance(instance);
        return wire(cd, wirelets);
    }

    @Nullable
    private static ExtensionBuild getExtensionBuild(MethodHandles.Lookup lookup, Component component) {
        requireNonNull(lookup, "component is null");

        // lookup.lookupClass() must point to the extension that should be extracted
        if (lookup.lookupClass() == Extension.class || !Extension.class.isAssignableFrom(lookup.lookupClass())) {
            throw new IllegalArgumentException("The lookupClass() of the specified lookup object must be a proper subclass of "
                    + Extension.class.getCanonicalName() + ", was " + lookup.lookupClass());
        }

        @SuppressWarnings("unchecked")
        Class<? extends Extension> extensionType = (Class<? extends Extension>) lookup.lookupClass();
        // Must have full access to the extension class
        if (!lookup.hasPrivateAccess()) {
            throw new IllegalArgumentException("The specified lookup object must have full access to " + extensionType
                    + ", try creating a new lookup object using MethodHandle.privateLookupIn(lookup, " + extensionType.getSimpleName() + ".class)");
        }

        // We only allow to call in directly on the container itself
        if (!component.modifiers().isContainer()) {
            throw new IllegalArgumentException(
                    "The specified component '" + component.path() + "' must have the Container modifier, modifiers = " + component.modifiers());
        }

        ComponentBuild compConf = ComponentBuild.unadapt(lookup, component);
        return compConf.cube.getExtensionContext(extensionType);
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
     *            a lookup for an extension subclass with full privileges
     * @param component
     *            the component to extract the configuration from.
     * @return an optional containing the extension if it has been configured, otherwise empty
     * @throws IllegalStateException
     *             if calling this method at runtime
     * @throws IllegalArgumentException
     *             if the {@link Lookup#lookupClass()} of the specified caller does not extend{@link Extension}. Or if the
     *             specified lookup object does not have full privileges
     */
    static Optional<ExtensionConfiguration> privateLookup(MethodHandles.Lookup caller, Component component) {
        requireNonNull(caller, "caller is null");
        return Optional.ofNullable(getExtensionBuild(caller, component));
    }

    /**
     * @param <T>
     *            the type of extension to return
     * @param lookup
     *            a lookup object that must have full ac
     * @param extensionType
     *            the type of extension to return
     * @param component
     *            the component
     * @return stuff
     * 
     */
    @SuppressWarnings("unchecked")
    static <T extends Extension> Optional<T> privateLookupExtension(MethodHandles.Lookup lookup, Class<T> extensionType, Component component) {
        requireNonNull(lookup, "lookup is null");
        if (lookup.lookupClass() != extensionType) {
            throw new IllegalArgumentException("The specified lookup object must match the specified extensionType " + extensionType + " as lookupClass()");
        }

        ExtensionBuild eb = getExtensionBuild(lookup, component);
        return eb == null ? Optional.empty() : Optional.of((T) eb.instance());
    }
}
