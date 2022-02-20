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
import java.lang.invoke.VarHandle;
import java.util.Set;

import app.packed.application.ApplicationDescriptor;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.ComponentRealm;
import app.packed.extension.Extension;
import app.packed.hooks3.BeanHook;
import packed.internal.container.AssemblyRealmSetup;
import packed.internal.util.LookupUtil;

/**
 * An assembly is Packed's main way to configure applications and their parts.
 * <p>
 * The assembly configures 1 or more containers.
 * <p>
 * This class is rarely extended directly by end-users. But provides means for power users to extend the basic
 * functionality of Packed.
 * <p>
 * An assembly is a thin wrapper that encapsulates the configuration of a container provided by the driver. This class
 * is mainly used through one of its subclasses such as {@link BaseAssembly}.
 * <p>
 * Assemblies are composable via linking.
 * 
 * 
 * <p>
 * Subclasses of this class supports 2 type based annotations. {@link ContainerHook} and {@link BeanHook}. Which
 * controls how containers and beans are added respectively.
 * <p>
 * Packed does not support any annotations on fields or methods. And will never perform any kind of reflection based
 * introspection of subclasses.
 * <p>
 * An assembly can never be used more than once. Trying to do so will result in an {@link IllegalStateException} being
 * thrown.
 * 
 * @see BaseAssembly
 */
public abstract non-sealed class Assembly implements ComponentRealm {

    /** A var handle that can update the {@link #container()} field in this class. */
    private static final VarHandle VH_CONFIGURATION = LookupUtil.lookupVarHandle(MethodHandles.lookup(), "configuration", ContainerConfiguration.class);

    /**
     * The configuration object we delegate all calls to.
     * <p>
     * The value of this field goes through 3 states:
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the container instance has not yet been used to build
     * anything.</li>
     * <li>Then, as a part of the build process, it is initialized with the actual container configuration object.</li>
     * <li>Finally, {@link #USED} is set to indicate that the assembly has been used.</li>
     * </ul>
     * <p>
     * This field is updated via var handle {@link #VH_CONFIGURATION}.
     */
    @Nullable
    private ContainerConfiguration configuration;

    /** {@return a descriptor for the application being built.} */
    protected final ApplicationDescriptor application() {
        return container().container.application.descriptor;
    }

    /**
     * Invoked by the runtime as part of the build process. This is where you should compose the application
     * <p>
     * This method will never be invoked more than once for a given assembly instance.
     * <p>
     * Note: This method should never be invoked directly by the user.
     */
    protected abstract void build();

    /**
     * Checks that {@link #build()} has not yet been invoked by the framework. the assembly has not already been used. This
     * method is typically used
     * 
     * {@link #build()} method has not already been invoked. This is typically used to make sure that users of extensions do
     * not try to configure the extension after it has been configured.
     * 
     * @throws IllegalStateException
     *             if {@link #build()} has already been invoked
     * @see ContainerConfiguration#checkPreBuild()
     */
    protected final void checkPreBuild() {
        if (configuration != null) {
            throw new IllegalStateException("#build has already been called on the Assembly");
        }
    }

    /**
     * Returns the configuration of the root container of this assembly.
     * <p>
     * This method must only be called from within the {@link #build()} method.
     * 
     * @return the container configuration object
     * @throws IllegalStateException
     *             if called from outside of the {@link #build()} method
     */
    // maybe rename to container???? and then we .realm()
    // and .application()? where application is limited for extension realm assemblies
    // only application().descriptor() works
    // saa skal Extension.configuration() vel omnavngives til .extension() + .realm() IDKx

    // Problemet er at vi har ContainerExtension...
    protected final ContainerConfiguration container() {
        ContainerConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of an assembly");
        } else if (c == ContainerConfiguration.USED) {
            throw new IllegalStateException("This method must be called from within the #build() method of an assembly.");
        }
        return c;
    }

    /**
     * Invoked by the runtime (via a MethodHandle). This method is mostly machinery that makes sure that the assembly is not
     * used more than once.
     * 
     * @param configuration
     *            the configuration to use for the assembling process
     */
    @SuppressWarnings("unused")
    private void doBuild(AssemblyRealmSetup realm, ContainerConfiguration configuration) {
        // Do we really need to guard against concurrent usage of an assembly?
        Object existing = VH_CONFIGURATION.compareAndExchange(this, null, configuration);
        if (existing == null) {
            try {
                // Run AssemblyHook.onPreBuild if hooks are present
                realm.assemblyModel.preBuild(configuration);

                // Call the actual build() method
                build();

                // Run AssemblyHook.onPostBuild if hooks are present
                realm.assemblyModel.postBuild(configuration);
            } finally {
                // Sets #configuration to a marker object that indicates the assembly has been used
                VH_CONFIGURATION.setVolatile(this, ContainerConfiguration.USED);
            }
        } else if (existing == ContainerConfiguration.USED) {
            // Assembly has already been used (successfully or unsuccessfully)
            throw new IllegalStateException("This assembly has already been used, assembly = " + getClass());
        } else {
            // Can be this thread (recursively called) or another thread that is already using the assembly.
            throw new IllegalStateException("This assembly is currently being used elsewhere, assembly = " + getClass());
        }
    }

    protected final void embed(Assembly assembly) {
        container().embed(assembly);
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used.}
     * 
     * @see ContainerConfiguration#extensionsTypes()
     * @see #use(Class)
     */
    protected final Set<Class<? extends Extension<?>>> extensionTypes() {
        return container().extensionTypes();
    }

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     */
    // ??? Is this beans only????? Should we put it there? IDK
    // Maaske... Kunne vaere super fedt jo hvis vi kunne
    // Mnahhh, altsaa hvis vi faar noget FN paa et tidspunkt.
    // Hvor vi tager reflection (a.la. Factory.ofMethod) saa
    // skal vi jo ogsaa bruge den der. IDK
    protected final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        container().container.realm.lookup(lookup);
    }

    /**
     * Sets the name of the container. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name
     * is case sensitive.
     * <p>
     * This method should be called as the first thing when configuring a container.
     * <p>
     * If no name is set using this method. The framework will automatically assign a name to the container, in such a way
     * that it will have a unique name among other sibling container.
     *
     * @param name
     *            the name of the container
     * @see ContainerConfiguration#named(String)
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @throws IllegalStateException
     *             if called from outside {@link #build()}
     */
    protected final void named(String name) {
        container().named(name);
    }

    /**
     * {@return the path of the container}
     * 
     * @see ContainerConfiguration#path()
     */
    protected final NamespacePath path() {
        return container().path();
    }

    /**
     * 
     * @param <W>
     *            the type of wirelets to select
     * @param wireletClass
     *            the type of wirelets to select
     * @return a wirelet selection
     * @see ContainerConfiguration#selectWirelets(Class)
     */
    protected final <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return container().selectWirelets(wireletClass);
    }

    /**
     * Returns an instance of the specified extension type.
     * <p>
     * The framework will lazily create a single instance of a particular extension when requested. Returning the same
     * instance for subsequent calls.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension instance of the requested type
     * @throws IllegalStateException
     *             if called after the container is no longer configurable
     * @see ContainerConfiguration#use(Class)
     */
    protected final <T extends Extension<T>> T use(Class<T> extensionType) {
        return container().use(extensionType);
    }
}
