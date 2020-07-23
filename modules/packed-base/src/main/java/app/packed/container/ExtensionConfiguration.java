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
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import app.packed.inject.Factory;
import packed.internal.component.ComponentConfigurationToComponentAdaptor;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.PackedExtensionConfiguration;

/**
 * An instance of this interface is available via {@link Extension#configuration()} or via constructor injection into an
 * extension. Since the extension itself defines most methods in this interface via protected final methods. This
 * interface is typically used to be able to provide these methods to code that is not located on the extension
 * implementation or in the same package as the extension itself.
 * <p>
 * Instances of this class should never be exposed to end-users.
 * 
 * @apiNote In the future, if the Java language permits, {@link ExtensionConfiguration} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 * 
 */
public interface ExtensionConfiguration /* extends ComponentConfiguration */ {

    // What we are generating...
    // This can be tricky, For example, if we create an image.
    // This method will always return image,
//    default AssembleTarget buildTarget() {
//        throw new UnsupportedOperationException();
//    }

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * An extension is no longer configurable after the extension's onConfigured method has been invoked by the runtime.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    // onConfigured-> configure...
    void checkConfigurable();

    default void checkPreemble() {
        // Ideen er at man kan checke at der ikke er blevet installeret boern...
        // Men saa kan
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the config site of the container the extension is registered with.
     * 
     * @return the config site of the container the extension is registered with
     */
    ConfigSite containerConfigSite(); // parent.configSite

    /**
     * Returns the path of the container the extension is registered with.
     * 
     * @return the path of the container the extension is registered with
     */
    // maaske bare path... og saa kan man kalde path().parent.get();
    ComponentPath containerPath();

    /**
     * Returns the type of extension this context wraps.
     * 
     * @return the type of extension this context wraps
     */
    Class<? extends Extension> extensionType(); // replace with descriptor???

    <T> SingletonConfiguration<T> install(Factory<T> factory);

    /**
     * @param <T>
     *            the type of the component
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see ContainerConfiguration#installInstance(Object)
     */
    <T> SingletonConfiguration<T> installInstance(T instance);

    /**
     * Creates a new container with this extensions container as its parent by linking the specified bundle. The new
     * container will have this extension as owner. Thus will be hidden from normal view
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            any wirelets
     */
    void link(ContainerBundle bundle, Wirelet... wirelets);

    /**
     * Returns an extension of the specified type. The specified type must be among the extension's dependencies as
     * specified via.... Otherwise an {@link InternalExtensionException} is thrown.
     * <p>
     * This method works similar to {@link ContainerConfiguration#use(Class)}. However, this method checks that only
     * extensions that have been declared as dependencies via {@link ExtensionSidecar#dependencies()} are specified. This is
     * done in order to make sure that no extensions ever depend on each other.
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
     *             if the specified extension type is not specified via {@link ExtensionSidecar} on this extension.
     * 
     * @see ContainerConfiguration#use(Class)
     */
    <E extends Extension> E use(Class<E> extensionType);

    /**
     * Returns whether or not the specified extension type has been used.
     * 
     * @param extensionType
     *            the extension type to test.
     * @return whether or not the extension has been used
     */
    // Forklar noget om hvornaar man kan vaere 100% sikker paa den ikke er brugt
    // Tillader vi alle extensions??? ogsaa dem vi ikke depender paa

    // Og man kan misforstaa den som om at det er om denne extension bruger den.

    boolean isUsed(Class<? extends Extension> extensionType);

//    /**
//     * The specified extension type must be located in the same module as the module that extension this context is related
//     * to.
//     * 
//     * @param <T>
//     *            the type of extensions to return
//     * @param extensionType
//     * @return a list of all extensions of the particular type in child containers within the same artifact
//     * @throws IllegalStateException
//     *             if invoked before the child gathering phase has finished
//     * @implNote the implementation will gather extensions and create a new list on every invocation. So cache the result if
//     *           you need to, instead of calling this method multiple times with the same argument.
//     */
    // The type must also be a dependency of this type as returned by #descriptor.dependencies();

    // Returns empty if the extension is not available

    // Hmm maaske vi flytter den ud af extension... Syntes ikke den skal dukke op under metoder...
    // Maaske paa ExtensionContext istedet for...

    // Skal ogsaa have en version der tager en Bundle???
    static Optional<ExtensionConfiguration> privateAccess(MethodHandles.Lookup lookup, Component c) {
        return Optional.ofNullable(pa(lookup, c));
    }

    /**
     * @param <T>
     *            the type of extension to return
     * @param lookup
     *            a lookup object that must have full ac
     * @param extensionType
     *            the type of extension to return
     * @param c
     *            the component
     * @return stuff
     * 
     */
    @SuppressWarnings("unchecked")
    static <T extends Extension> Optional<T> privateAccessExtension(MethodHandles.Lookup lookup, Class<T> extensionType, Component c) {
        requireNonNull(lookup, "lookup is null");
        if (lookup.lookupClass() != extensionType) {
            throw new IllegalArgumentException("The specified lookup object must have " + extensionType + " as lookupClass()");
        }
        @Nullable
        PackedExtensionConfiguration pec = pa(lookup, c);
        return pec == null ? Optional.empty() : Optional.of((T) pec.instance());
    }

    @SuppressWarnings("deprecation")
    private static PackedExtensionConfiguration pa(MethodHandles.Lookup lookup, Component c) {
        requireNonNull(lookup, "lookup is null");
        if (lookup.lookupClass() == Extension.class || !Extension.class.isAssignableFrom(lookup.lookupClass())) {
            throw new IllegalArgumentException("The specified lookup object must have a subclass of " + Extension.class.getCanonicalName()
                    + " as lookupClass(), was " + lookup.lookupClass());
        }
        @SuppressWarnings("unchecked")
        Class<? extends Extension> extensionType = (Class<? extends Extension>) lookup.lookupClass();

        if (!lookup.hasPrivateAccess()) {
            throw new IllegalArgumentException("The specified lookup object must have full access to " + extensionType
                    + ", try creating a new lookup object using MethodHandle.privateLookupIn(lookup, " + extensionType.getSimpleName() + ".class)");
        } else if (!(c instanceof ComponentConfigurationToComponentAdaptor)) {
            throw new IllegalStateException("This method cannot be called on a at runtime of a container");
        }

        ComponentConfigurationToComponentAdaptor cc = (ComponentConfigurationToComponentAdaptor) c;
        PackedContainerConfiguration pcc = cc.componentConfiguration.actualContainer();
        return pcc.getExtensionContext(extensionType);
    }
}
