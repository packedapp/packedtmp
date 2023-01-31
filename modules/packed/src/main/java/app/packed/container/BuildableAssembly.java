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

import app.packed.framework.Nullable;
import internal.app.packed.container.AssemblyModel;
import internal.app.packed.container.ContainerSetup;
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
 * Subclasses of this class supports 2 type based annotations. . Which controls how containers and beans are added
 * respectively.
 * <p>
 * Packed does not support any annotations on fields or methods. And will never perform any kind of reflection based
 * introspection of subclasses.
 * <p>
 * An assembly can never be used more than once. Trying to do so will result in an {@link IllegalStateException} being
 * thrown.
 *
 * @see BaseAssembly
 */
// LinkedAssembly, LinkableAssembly
public non-sealed abstract class BuildableAssembly extends Assembly {

    /** A var handle that can update the {@link #configuration} field in this class. */
    private static final VarHandle VH_CONFIGURATION = LookupUtil.findVarHandleOwn(MethodHandles.lookup(), "configuration", ContainerConfiguration.class);

    /**
     * The configuration of the container that this assembly defines.
     * <p>
     * The value of this field goes through 3 states:
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the assembly has not yet been used to build anything.</li>
     * <li>Then, as a part of the build process, it is initialized with a container configuration object.</li>
     * <li>Finally, {@link ContainerConfiguration#USED} is set to indicate that the assembly has been used.</li>
     * </ul>
     * <p>
     * This field is updated via var handle {@link #VH_CONFIGURATION}.
     */
    @Nullable
    private ContainerConfiguration configuration;

    /**
     * This method must be overridden by the application developer in order to configure the application.
     * <p>
     * This method is never invoked more than once for a given assembly instance.
     * <p>
     * Note: This method should never be invoked directly by the user.
     */
    protected abstract void build();

    /**
     * Checks that {@link #build()} has not yet been called by the framework.
     * <p>
     * This method is typically used by assemblies that define configuration methods that must be called before
     * {@link #build()} is invoked.
     *
     * @throws IllegalStateException
     *             if {@link #build()} has already been invoked
     * @see #isPreBuild()
     */
    protected final void checkIsPreBuild() {
        if (!isPreBuild()) {
            throw new IllegalStateException("This method must be called before the assembly is used to build an application");
        }
    }

    /**
     * Returns the configuration of the main container defined by this assembly.
     * <p>
     * This method can only be called from within the {@link #build()} method. Trying to call it outside of {@link #build()}
     * will throw an {@link IllegalStateException}.
     *
     * @return the configuration of the container
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
     * @param assembly
     *            the realm used to call container hooks
     * @param configuration
     *            the configuration to use for the assembling process
     */
    @SuppressWarnings("unused")
    private void doBuild(AssemblyModel assemblyModel, ContainerSetup container) {
        ContainerConfiguration configuration = new ContainerConfiguration(new ContainerHandle(container));
        // Do we really need to guard against concurrent usage of an assembly?
        Object existing = VH_CONFIGURATION.compareAndExchange(this, null, configuration);
        if (existing == null) {
            try {
                // Run AssemblyHook.onPreBuild if hooks are present
                assemblyModel.preBuild(configuration);

                // Call the actual build() method
                build();

                // Run AssemblyHook.onPostBuild if hooks are present
                assemblyModel.postBuild(configuration);
            } finally {
                // Sets #configuration to a marker object that indicates the assembly has been used
                VH_CONFIGURATION.setVolatile(this, ContainerConfiguration.USED);
            }
        } else if (existing == ContainerConfiguration.USED) {
            // Assembly has already been used (successfully or unsuccessfully)
            throw new IllegalStateException("This assembly has already been used, assembly = " + getClass());
        } else {
            // Assembly is in the process of being used. Typically happens, if an assembly is linked recursively.
            throw new IllegalStateException("This assembly is currently being used elsewhere, assembly = " + getClass());
        }
    }

    /**
     * Returns whether or not {@link #build()} has been called.
     *
     * @return {@code true} if {@link #build()} has not been called yet, otherwise {@code false}
     *
     * @see #checkIsPreBuild()
     */
    protected final boolean isPreBuild() {
        return configuration == null;
    }

    /**
     * Specifies a lookup object that the framework will use will be used when access bean members installed from within
     * this assembly.
     * <p>
     * This method can be used as an alternative
     * <p>
     * Example
     *
     * <p>
     * The lookup object passed to this method is only used internally. And only for the sake of accessing those bean
     * installed by the assembly
     * <p>
     * This method will typically never be called more than once.
     *
     * @param lookup
     *            the lookup object
     * @throws IllegalStateException
     *             if called from outside of {@link #build()}
     */
    protected final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null");
        container().handle.container.assembly.lookup(lookup);
    }
}
