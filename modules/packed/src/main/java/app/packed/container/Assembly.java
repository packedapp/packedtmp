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

import app.packed.application.ApplicationBuildInfo;
import app.packed.base.Nullable;
import internal.app.packed.container.AssemblyUserRealmSetup;
import internal.app.packed.util.LookupUtil;

/**
 * Assemblies are the main way that applications are configured in Packed.
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
 * <p>
 * Subclasses of this class supports 2 type based annotations. . Which
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
public abstract class Assembly {

    /** A var handle that can update the {@link #configuration} field in this class. */
    private static final VarHandle VH_CONFIGURATION = LookupUtil.lookupVarHandle(MethodHandles.lookup(), "configuration", ContainerConfiguration.class);

    /**
     * The configuration object all calls to.
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
    protected final ApplicationBuildInfo applicationInfo() {
        return container().container.application.info;
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
     * Checks that {@link #build()} has not already been invoked by the framework.
     * <p>
     * This method is typically used by assemblies that define configuration methods that must be called before
     * {@link #build()}. Making sure that the assembly is still in a state to be configurable.
     * 
     * @throws IllegalStateException
     *             if {@link #build()} has already been invoked
     */
    protected final void checkConfigurable() {
        if (configuration != null) {
            throw new IllegalStateException("#build has already been called on the Assembly");
        }
    }

    /**
     * Returns the configuration of the <strong>root</strong> container defined by this assembly.
     * <p>
     * This method must be called from within the {@link #build()} method.
     * 
     * @return the configuration of the root container
     * @throws IllegalStateException
     *             if called from outside of the {@link #build()} method
     */
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
     * @param realm
     *            the realm used to call container hooks
     * @param configuration
     *            the configuration to use for the assembling process
     */
    @SuppressWarnings("unused")
    private void doBuild(AssemblyUserRealmSetup realm, ContainerConfiguration configuration) {
        // Do we really need to guard against concurrent usage of an assembly?
        Object existing = VH_CONFIGURATION.compareAndExchange(this, null, configuration);
        if (existing == null) {
            try {
                // Run AssemblyHook.onPreBuild if hooks are present
                realm.preBuild(configuration);

                // Call the actual build() method
                build();

                // Run AssemblyHook.onPostBuild if hooks are present
                realm.postBuild(configuration);
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

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * <p>
     * Lookup obejcts are used throughout any beans defined within this assembly. Not only in the root contianer
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
        container().container.userRealm.lookup(lookup);
    }
}
