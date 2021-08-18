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

import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.extension.Extension;
import packed.internal.component.PackedComponentDriver;
import packed.internal.container.ContainerSetup;
import packed.internal.container.PackedContainerDriver;
import packed.internal.util.LookupUtil;

/**
 * An assembly is Packed's main way to configure applications and their parts.
 * <p>
 * This class is rarely extended directly by end-users. But provides means for power users to extend the basic
 * functionality of Packed.
 * <p>
 * An assembly is a thin wrapper that encapsulates a {@link ContainerDriver} and the configuration of a component
 * provided by the driver. This class is mainly used through one of its subclasses such as {@link BaseAssembly}.
 * <p>
 * Assemblies are composable via linking.
 * 
 * <p>
 * Packed will never attempt to member scan the subclasses.
 * <p>
 * An assembly can only be used a single time. Trying to use it more than once will fail with
 * {@link IllegalStateException}.
 * 
 * 
 * @param <C>
 *            the underlying component configuration this assembly wraps
 * @see CommonContainerAssembly
 * @see BaseAssembly
 */
public abstract class Assembly<C extends ContainerConfiguration> {

    /** A marker configuration object to indicate that an assembly has already been used to build something. */
    private static final ContainerConfiguration USED = new ContainerConfiguration();

    /** A var handle that can update the #configuration field in ContainerConfiguration. */
    private static final VarHandle VH_CONFIGURATION = LookupUtil.lookupVarHandle(MethodHandles.lookup(), "configuration", ContainerConfiguration.class);

    /**
     * The configuration of the underlying container.
     * <p>
     * The value of this field goes through 3 states:
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the assembly is not use or has not yet been used.</li>
     * <li>Then, as a part of the build process, it is initialized with the actual container configuration.</li>
     * <li>Finally, {@link #USED} is set to indicate that the assembly has been used.</li>
     * </ul>
     * <p>
     * This field is updated via var handle {@link #VH_CONFIGURATION}.
     */
    @Nullable
    private ContainerConfiguration configuration;

    /**
     * This assembly's container driver.
     * <p>
     * This field is read by {@link PackedComponentDriver#getDriver(Assembly)} via a varhandle.
     */
    @SuppressWarnings("unused")
    private final PackedContainerDriver<? extends C> driver;

    /**
     * Creates a new assembly using the specified component driver.
     * 
     * @param driver
     *            the component driver
     */
    // Could we allow to override the driver when linking?
    // I think we need to define the exact usecases
    protected Assembly(ContainerDriver<? extends C> driver) {
        this.driver = requireNonNull((PackedContainerDriver<? extends C>) driver, "driver is null");
    }

    /**
     * Invoked by the runtime as part of the build process. This is where you should compose the application
     * <p>
     * This method will never be invoked more than once on an assembly instance.
     * <p>
     * Note: This method should never be invoked directly by the user.
     */
    protected abstract void build();

    /**
     * Checks that the assembly has not already been used. This method is typically used
     * 
     * {@link #build()} method has not already been invoked. This is typically used to make sure that users of extensions do
     * not try to configure the extension after it has been configured.
     * 
     * <p>
     * This method is a simple wrapper that just invoked {@link ContainerConfiguration#checkBuildNotStarted()}.
     * 
     * @throws IllegalStateException
     *             if {@link #build()} has been invoked
     * @see ContainerConfiguration#checkBuildNotStarted()
     */
    // Before build is started?? or do we allow to call these method
    // checkPreBuild()? // checkConfigurable()
    // Det er ogsaa inde hooks er kaldt
    //// Ideen er at vi kan kalde metode fra configurations metoder
    protected final void checkBuildNotStarted() {
        // Why not just test configuration == null????

        // Tror vi skal expose noget state fra ContainerConfiguration, vi kan checke
        configuration().container().realm.checkOpen();
    }

    /**
     * Returns this configuration of the container.
     * <p>
     * This method must only be called from within the {@link #build()} method.
     * 
     * @return thie configuration of the container
     * @throws IllegalStateException
     *             if called from outside of the {@link #build()} method
     */
    @SuppressWarnings("unchecked")
    protected final C configuration() {
        Object c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of an assembly");
        } else if (c == USED) {
            throw new IllegalStateException("This method must be called from within the #build() method of an assembly.");
        }
        return (C) c;
    }

    /**
     * Invoked by the runtime (via a MethodHandle). This method is mostly machinery that makes sure that the assembly is not
     * used more than once.
     * 
     * @param configuration
     *            the configuration to use for the assembling process
     */
    @SuppressWarnings("unused")
    private void doBuild(C configuration) {
        // I'm not we need volatile here
        Object existing = VH_CONFIGURATION.compareAndExchange(this, null, configuration);
        if (existing == null) {
            ContainerSetup cs = configuration.container();

            try {
                // Run AssemblyHook.onPreBuild if hooks are present
                cs.preBuild(configuration);

                // Call the build method implemented by the user
                build();

                // Run AssemblyHook.onPostBuild if hooks are present
                cs.postBuild(configuration);
            } finally {
                // Sets #configuration to a marker object that indicates the assembly has been used
                VH_CONFIGURATION.setVolatile(this, USED);
            }
        } else if (existing == USED) {
            // Assembly has already been used (successfully or unsuccessfully)
            throw new IllegalStateException("This assembly has already been used, assembly = " + getClass());
        } else {
            // Can be this thread (recursively called) or another thread that is already using the assembly.
            throw new IllegalStateException("This assembly is currently being used elsewhere, assembly = " + getClass());
        }
    }

    /**
     * {@return an unmodifiable view of every extension that is currently used in this assembly.}
     * 
     * @see ContainerConfiguration#extensionsTypes()
     * @see #use(Class)
     */
    protected final Set<Class<? extends Extension>> extensionsTypes() {
        return configuration().extensionsTypes();
    }

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     */
    // Hvorfor er det her ikke paa ContainerConfiguration????
    // fx hooks ville gerne bruge den
    // Hvis vi bare declare det final
    protected final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        configuration().container().realm.setLookup(lookup);
    }

    /**
     * Sets the name of the container. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name
     * is case sensitive.
     * <p>
     * This method should be called as the first thing when configuring a container.
     * <p>
     * If no name is set using this method. A name will be assigned to the container when the container is initialized, in
     * such a way that it will have a unique name among other sibling container.
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
        configuration().named(name);
    }

    /**
     * {@return the path of the container}
     * 
     * @see ContainerConfiguration#path()
     */
    protected final NamespacePath path() {
        return configuration().path();
    }

    /**
     * 
     * @param <W>
     *            the type of wirelets to select
     * @param wireletClass
     *            the type of wirelets to select
     * @return
     * @see ContainerConfiguration#selectWirelets(Class)
     */
    protected final <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return configuration().selectWirelets(wireletClass);
    }

    /**
     * Returns an instance of the specified extension class.
     * <p>
     * If this is first time this method is called (with the specified extension type). An instantiate of the specified
     * extension will be created, and subsequent invocations will return the same instance.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the extension class to return an instance of
     * @return an instance of the specified extension class
     * @throws IllegalStateException
     *             if called after the container is no longer configurable
     * @see ContainerConfiguration#use(Class)
     */
    protected final <T extends Extension> T use(Class<T> extensionType) {
        return configuration().use(extensionType);
    }
}
//Or ContainerAssembly... ligesom Image vs ApplicationImage
//Men saa skal det vel ogsaa hedde ContainerWirelets....
//Altsaa man bruger den jo naermest aldrig, kun andre
