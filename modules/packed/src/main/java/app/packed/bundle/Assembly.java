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
import java.lang.invoke.VarHandle;
import java.util.Set;

import app.packed.application.ApplicationDescriptor;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.extension.Extension;
import packed.internal.bundle.BundleSetup;
import packed.internal.bundle.PackedBundleDriver;
import packed.internal.util.LookupUtil;

/**
 * An assembly is Packed's main way to configure applications and their parts.
 * <p>
 * This class is rarely extended directly by end-users. But provides means for power users to extend the basic
 * functionality of Packed.
 * <p>
 * An assembly is a thin wrapper that encapsulates a {@link BundleDriver} and the configuration of a component provided
 * by the driver. This class is mainly used through one of its subclasses such as {@link BaseAssembly}.
 * <p>
 * Assemblies are composable via linking.
 * 
 * <p>
 * Packed never performs any kind of reflection based introspection of bundle implementations.
 * <p>
 * Instances of this class can only be used a single time. Trying to use it more than once will fail with
 * {@link IllegalStateException}.
 * 
 * @see BaseAssembly
 */
public abstract class Assembly {

    /** A marker configuration object, indicating that an assembly has already been used. */
    private static final BundleConfiguration USED = new BundleConfiguration();

    /** A var handle that can update the {@link #configuration()} field in this class. */
    private static final VarHandle VH_CONFIGURATION = LookupUtil.lookupVarHandle(MethodHandles.lookup(), "configuration", BundleConfiguration.class);

    /**
     * The configuration object we delegate all calls to.
     * <p>
     * The value of this field goes through 3 states:
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the bundle instance has not yet been used to build anything.</li>
     * <li>Then, as a part of the build process, it is initialized with the actual bundle configuration object.</li>
     * <li>Finally, {@link #USED} is set to indicate that the assembly has been used.</li>
     * </ul>
     * <p>
     * This field is updated via var handle {@link #VH_CONFIGURATION}.
     */
    @Nullable
    private BundleConfiguration configuration;

    /**
     * The driver of this assembly.
     * <p>
     * This field is read by {@link PackedBundleDriver#getDriver(Assembly)} via a var handle.
     */
    @SuppressWarnings("unused")
    private final PackedBundleDriver driver;

    /**
     * Creates a new assembly using the specified bundle driver.
     * 
     * @param driver
     *            the driver to used to create the bundle
     */
    protected Assembly(BundleDriver driver) {
        this.driver = requireNonNull((PackedBundleDriver) driver, "driver is null");
    }

    /** {@return a descriptor for the application being built.} */
    protected final ApplicationDescriptor application() {
        return configuration().application();
    }

    /**
     * Invoked by the runtime as part of the build process. This is where you should compose the application
     * <p>
     * This method will never be invoked more than once on a bundle instance.
     * <p>
     * Note: This method should never be invoked directly by the user.
     */
    protected abstract void build();

    /**
     * Checks that {@link #build()} has not been called by the framework. the assembly has not already been used. This
     * method is typically used
     * 
     * {@link #build()} method has not already been invoked. This is typically used to make sure that users of extensions do
     * not try to configure the extension after it has been configured.
     * 
     * <p>
     * This method is a simple wrapper that just invoked {@link BundleConfiguration#checkBuildNotStarted()}.
     * 
     * @throws IllegalStateException
     *             if {@link #build()} has been invoked
     * @see BundleConfiguration#checkBuildNotStarted()
     */
    // Before build is started?? or do we allow to call these method
    // checkPreBuild()? // checkConfigurable()
    // Det er ogsaa inde hooks er kaldt
    //// Ideen er at vi kan kalde metode fra configurations metoder
    protected final void checkBuildNotStarted() {
        // Why not just test configuration == null????

        // Tror vi skal expose noget state fra ContainerConfiguration, vi kan checke
        configuration().bundle().realm.checkOpen();
    }

    /**
     * Returns the configuration object for this assembly.
     * <p>
     * This method must only be called from within the {@link #build()} method.
     * 
     * @return the wrapped configuration object
     * @throws IllegalStateException
     *             if called from outside of the {@link #build()} method
     */
    protected final BundleConfiguration configuration() {
        BundleConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of an assembly");
        } else if (c == USED) {
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
    private void doBuild(BundleConfiguration configuration) {
        // I'm not we need volatile here
        Object existing = VH_CONFIGURATION.compareAndExchange(this, null, configuration);
        if (existing == null) {
            BundleSetup cs = configuration.bundle();

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
     * {@return an unmodifiable view of the extensions that are currently used in this bundle.}
     * 
     * @see BundleConfiguration#extensionsTypes()
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
    // ??? Is this beans only????? Should we put it there? IDK
    // Maaske... Kunne vaere super fedt jo hvis vi kunne
    // Mnahhh, altsaa hvis vi faar noget FN paa et tidspunkt.
    // Hvor vi tager reflection (a.la. Factory.ofMethod) saa
    // skal vi jo ogsaa bruge den der. IDK
    protected final void lookup(Lookup lookup) {
        configuration().lookup(lookup);
    }

    /**
     * Sets the name of the bundle. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name is
     * case sensitive.
     * <p>
     * This method should be called as the first thing when configuring a bundle.
     * <p>
     * If no name is set using this method. The framework will automatically assign a name to the bundle, in such a way that
     * it will have a unique name among other sibling container.
     *
     * @param name
     *            the name of the bundle
     * @see BundleConfiguration#named(String)
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
     * @see BundleConfiguration#path()
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
     * @see BundleConfiguration#selectWirelets(Class)
     */
    protected final <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return configuration().selectWirelets(wireletClass);
    }

    /**
     * Returns an extension instance of the specified type.
     * <p>
     * The framework will lazily create a single instance of a particular extension. Returning the same instance for any
     * subsequent calls.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension instance of the requested type
     * @throws IllegalStateException
     *             if called after the bundle is no longer configurable
     * @see BundleConfiguration#use(Class)
     */
    protected final <T extends Extension> T use(Class<T> extensionType) {
        return configuration().use(extensionType);
    }
}