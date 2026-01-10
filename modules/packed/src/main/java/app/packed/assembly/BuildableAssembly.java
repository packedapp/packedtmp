/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.assembly;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.util.Nullable;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.build.PackedBuildProcess;
import internal.app.packed.container.PackedContainerInstaller;

/**
 * Assemblies are the main way that applications are configured in Packed.
 * <p>
 * The assembly configures 1 or more containers.
 * <p>
 * This class is rarely extended directly by end-users, typically {@link BaseAssembly} is extended instead.
 * <p>
 * There are two types of annotations that can be placed on subclass of this class. AssemblyHook
 *
 * BeanHook
 * <p>
 * Packed has no features that requires annotation based fields or methods on assembly classes. And the framework will
 * never perform any kind of reflection based introspection of assembly subclasses.
 * <p>
 * An assembly can only be used once. Trying to reuse the assembly instance will result in an
 * {@link IllegalStateException} being thrown.
 *
 * @see BaseAssembly
 */
// LinkedAssembly, LinkableAssembly, ConfinedAssembly
// Navnet ligger lidt for taet op af BaseAssembly. Maaske
public non-sealed abstract class BuildableAssembly extends Assembly {

    /**
     * The configuration of the assembly.
     * <p>
     * The value of this field goes through 3 states:
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the assembly has not yet been used to build anything.</li>
     * <li>Then, as a part of the build process, it is initialized with an assembly configuration object.</li>
     * <li>Finally, {@link AssemblyConfiguration#USED} is set to indicate that the assembly has been used.</li>
     * </ul>
     * <p>
     */
    @Nullable
    private AssemblyConfiguration configuration;

    /**
     * Returns the configuration of the assembly.
     * <p>
     * This method can only be called from within the {@link #build()} method. Trying to call it outside of {@link #build()}
     * will throw an {@link IllegalStateException}.
     *
     * @return the configuration of the assembly
     * @throws IllegalStateException
     *             if called from outside of the {@link #build()} method
     */
    protected final AssemblyConfiguration assembly() {
        AssemblyConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of an assembly");
        } else if (c == AssemblyConfiguration.USED) {
            throw new IllegalStateException("This method must be called from within the #build() method of an assembly.");
        }
        return c;
    }

    /**
     * This method must be overridden by the application developer in order to configure the application.
     * <p>
     * This method will never be invoked more than once for a given assembly instance.
     * <p>
     * Note: This method should never be invoked directly by the user.
     */
    protected abstract void build();

    /** {@inheritDoc} */
    @Override
    AssemblySetup build(PackedApplicationInstaller<?> applicationInstaller, PackedContainerInstaller<?> installer) {
        AssemblyConfiguration existing = configuration;
        if (existing == null) { // assembly has not been used in a build process before
            AssemblySetup assembly = AssemblySetup.newAssembly(installer, this);
            PackedBuildProcess p = PackedBuildProcess.get();
            p.push(assembly);
            try {
                AssemblyConfiguration ac = configuration = new AssemblyConfiguration(assembly);

                try {
                    // Run AssemblyHook.beforeBuild for any hooks present
                    assembly.model.hooks.forEach(AssemblyBuildHook.class, h -> h.beforeBuild(ac));

                    // Call the actual build() method on the assembly
                    build();

                    // Run AssemblyHook.afterBuild for any hooks present
                    assembly.model.hooks.forEachReversed(AssemblyBuildHook.class, h -> h.afterBuild(ac));
                } finally {
                    // Sets #configuration to a marker object that indicates the assembly has been used
                    existing = AssemblyConfiguration.USED;
                }

                assembly.postBuild();
            } finally {
                p.pop(assembly);
            }
            return assembly;
        } else if (existing == AssemblyConfiguration.USED) {
            // Assembly has already been used (successfully or unsuccessfully)
            throw new IllegalStateException("This assembly has already been used, assembly = " + getClass());
        } else {
            // Assembly is in the process of being used. Typically happens, if an assembly is linked recursively.
            throw new IllegalStateException("This assembly is currently being used elsewhere, assembly = " + getClass());
        }
    }

    /**
     * Checks that {@link #build()} has not yet been called by the framework.
     * <p>
     * This method is typically used by assemblies that define configuration methods that can only be called before
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
     * Returns whether or not {@link #build()} has already been called.
     * <p>
     * If called from within {@link #build()} this method returns true.
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
    protected final void lookup(@Nullable Lookup lookup) {
        assembly().assembly.lookup(lookup);
    }

    final void openForTransformation(String... modules) {

    }
}
