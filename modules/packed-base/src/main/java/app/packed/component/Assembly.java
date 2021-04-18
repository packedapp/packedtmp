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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerAssembly;
import app.packed.container.ContainerConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.component.WireableComponentDriver;
import packed.internal.util.LookupUtil;

/**
 * An assembly is Assemblies are the main way to configure a component based system in Packed.
 * 
 * An assembly is a thin wrapper that encapsulates a {@link ComponentDriver} and the configuration of a component
 * provided by the driver. This class is mainly used through one of its subclasses such as {@link BaseAssembly}.
 * <p>
 * Assemblies are composable via linking.
 * 
 * <p>
 * Packed will never attempt to member scan the subclasses.
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
public abstract class Assembly<C extends ComponentConfiguration> {

    /** A marker object to indicate that the assembly has already been used. */
    private static Object USED = Assembly.class;

    /** A handle that can access #configuration. */
    private static final VarHandle VH_CONFIGURATION = LookupUtil.lookupVarHandle(MethodHandles.lookup(), "configuration", Object.class);

    /**
     * The configuration of this assembly.
     * <p>
     * The value of this field goes through 3 states:
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the assembly is not use or has not yet been used.
     * <li>Then, as a part of the build process, it is initialized with the actual configuration object of the component.
     * <li>Finally, {@link #USED} is set to indicate that the assembly has been used
     * </ul>
     * <p>
     * This field is updated via a VarHandle.
     */
    @Nullable
    private Object configuration;

    /**
     * The component driver of this assembly.
     * <p>
     * This field is read from {@link WireableComponentDriver#getDriver(Assembly)} via a varhandle.
     */
    private final WireableComponentDriver<? extends C> driver;

    /**
     * Creates a new assembly using the specified driver.
     * 
     * @param driver
     *            the component driver used to create the configuration objects this assembly wraps
     */
    protected Assembly(ComponentDriver<? extends C> driver) {
        this.driver = requireNonNull((WireableComponentDriver<? extends C>) driver, "driver is null");
        this.driver.checkBound(); // Checks that the driver does not have unbound bindings
    }

    /**
     * Compose the Implements the composition logic.
     * <p>
     * Invoked by the runtime as part of the build process.
     * <p>
     * This method should never be invoked directly by the user.
     */
    // rename to assemble? Og saa behold build til Build info
    protected abstract void build();

    /**
     * Returns this assembly's configuration object.
     * <p>
     * This method must only be called from within the {@link #build()} method.
     * 
     * @return this assembly's configuration object
     * @throws IllegalStateException
     *             if called from outside of the {@link #build()} method
     */
    @SuppressWarnings("unchecked")
    protected final C configuration() {
        Object c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of an assembly");
        } else if (c == USED) {
            throw new IllegalStateException("This method must be called from within the #build() method of the assembly.");
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
        Object existing = VH_CONFIGURATION.compareAndExchange(this, null, configuration);
        if (existing == null) {
            try {
                build();
            } finally {
                // Sets #configuration to a marker object that indicates the assembly has been used
                VH_CONFIGURATION.setVolatile(this, USED);
            }
        } else if (existing == USED) {
            // Assembly has already been used (successfully or unsuccessfully)
            throw new IllegalStateException("This assembly has already been used, assembly = " + getClass());
        } else {
            // Can be this thread or another thread that is already using the assembly.
            throw new IllegalStateException("This assembly is currently being used elsewhere, assembly = " + getClass());
        }
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

    /**
     * Checks that the assembly has not already been used. This method is typically used
     * 
     * {@link #build()} method has not already been invoked. This is typically used to make sure that users of extensions
     * does not try to configure the extension after it has been configured.
     * 
     * <p>
     * This method is a simple wrapper that just invoked {@link ContainerConfiguration#checkPreBuild()}.
     * 
     * @throws IllegalStateException
     *             if {@link #build()} has been invoked
     * @see ContainerConfiguration#checkPreBuild()
     */
    // Before build is started?? or do we allow to call these method
    // checkPreBuild()??
    protected final void checkPreBuild() {
        // Why not just test configuration == null????
        
        // Det er vel det samme som at kalde configuration()??
        ((ComponentSetup) configuration().context).realm.checkOpen();
    }

    final <T extends Wirelet> WireletSource<T> wirelets(Class<T> wirelet) {
        // Jeg ved ikke hvor tid vi har brug for den her...
        throw new UnsupportedOperationException();
    }
}
