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
package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.base.Nullable;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerAssembly;
import packed.internal.component.AssemblyHelper;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;

/**
 * Assemblies are the main way to configure a component based system in Packed.
 * 
 * An assembly is a thin wrapper that encapsulates a {@link ComponentDriver} and the configuration of a component
 * provided by the driver. This class is mainly used through one of its subclasses such as {@link BaseAssembly}.
 * <p>
 * Assemblies are composable via linking.
 * 
 * <p>
 * An assembly can only be used a single time. Trying to use it more than once will fail with
 * {@link IllegalStateException}.
 * <p>
 * This class is rarely extended directly by end-users. But provides means for power users to extend the basic
 * functionality of Packed.
 * 
 * @param <C>
 *            the underlying component configuration this assembly wraps
 * @see ContainerAssembly
 * @see BaseAssembly
 */
public abstract class Assembly<C extends ComponentConfiguration> extends Realm {

    /**
     * The configuration of this assembly.
     * <p>
     * This field is updated via a VarHandle from {@link AssemblyHelper}. The value of this field goes through 3 states:
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the assembly is not use or has not yet been used.
     * <li>Then, as a part of the build process, it is initialized with the actual configuration object of the component.
     * <li>Finally, {@link AssemblyHelper#ASSEMBLY_USED} is set to indicate that the assembly has been used
     * </ul>
     */
    @Nullable
    private Object configuration;

    /** The driver of this assembly. This field is read via a VarHandle from {@link AssemblyHelper}. */
    @SuppressWarnings("unused")
    private final PackedComponentDriver<? extends C> driver;

    /**
     * Creates a new assembly using the specified component driver.
     * 
     * @param driver
     *            the driver used for constructing the configuration of this assembly
     */
    protected Assembly(ComponentDriver<? extends C> driver) {
        this.driver = requireNonNull((PackedComponentDriver<? extends C>) driver, "driver is null");
    }

    /**
     * Compose the Implements the composition logic.
     * <p>
     * Invoked by the runtime as part of the build process.
     * <p>
     * This method should never be invoked directly by the user.
     */
    protected abstract void build(); // Invoked from AssemblyHelper#invokeAssemblyBuild

    /**
     * Returns the configuration object that this assembly wraps.
     * <p>
     * This method must only be called from within the {@link #build()} method.
     * 
     * @return the configuration object that this assembly wraps
     * @throws IllegalStateException
     *             if called from outside of the {@link #build()} method
     */
    @SuppressWarnings("unchecked")
    protected final C configuration() {
        Object c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of an assembly");
        } else if (c == AssemblyHelper.ASSEMBLY_USED) {
            throw new IllegalStateException("This method must be called from within the #build() method of the assembly.");
        }
        return (C) c;
    }

    protected final <T extends Wirelet> WireletHandle<T> wirelets(Class<T> wirelet) {
        // Jeg ved ikke hvor tid vi har brug for den her...
        throw new UnsupportedOperationException();
    }
    
    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     */
    protected final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        ((ComponentSetup) configuration().context).realm.setLookup(lookup);
    }
}
//// Design notices
// We do not support $ methods because they can be seen by all subclasses.
