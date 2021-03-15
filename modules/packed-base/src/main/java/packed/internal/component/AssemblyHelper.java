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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Helper class to access non-public members in {@link Assembly}. */
public final class AssemblyHelper {

    /** An object that marks that an assembly has been used. */
    public static final Object ASSEMBLY_USED = new Object();

    /** A VarHandle that can access Bundle#configuration. */
    private static final VarHandle VH_BUNDLE_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Assembly.class, "configuration",
            Object.class);

    /** A VarHandle used from {@link #getDriver(Assembly)} to access the driver field from a {@link Assembly}. */
    private static final VarHandle VH_BUNDLE_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Assembly.class, "driver",
            PackedComponentDriver.class);

    /** A MethodHandle that can invoke Bundle#configure. */
    private static final MethodHandle MH_BUNDLE_CONFIGURE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Assembly.class, "build", void.class);

    /** No instances for you. */
    private AssemblyHelper() {}

    /**
     * @param assembly
     *            the assembly for which to invoke the build method
     * @param configuration
     *            the configuration object to set before invoking the build method
     */
    static void invokeBuild(Assembly<?> assembly, Object configuration) {
        // We perform a compare and exchange. Guarding against concurrent usage of this assembly.
        // I actually don't think we need to use volatile...IDK
        // I'm pretty sure you will get an exception one way or the other... 
        Object existing = VH_BUNDLE_CONFIGURATION.compareAndExchange(assembly, null, configuration);
        if (existing == null) {
            try {
                MH_BUNDLE_CONFIGURE.invoke(assembly); // Invokes Assembly#configure()
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            } finally {
                // sets Assembly.configuration to a marker that indicates the assembly has been consumed
                VH_BUNDLE_CONFIGURATION.setVolatile(assembly, AssemblyHelper.ASSEMBLY_USED);
            }
        } else if (existing == ASSEMBLY_USED) {
            // Bundle has already been used (successfully or unsuccessfully)
            throw new IllegalStateException("This assembly has already been used, type = " + assembly.getClass());
        } else {
            // Can be this thread or another thread that is already using the bundle.
            throw new IllegalStateException("This assembly is currently being used elsewhere, type = " + assembly.getClass());
        }
    }

    /**
     * Extracts the component driver from the specified bundle.
     * 
     * @param assembly
     *            the bundle to extract the component driver from
     * @return the specified bundle's component driver
     * @see #VH_BUNDLE_DRIVER
     */
    static <C extends ComponentConfiguration> PackedComponentDriver<? extends C> getDriver(Assembly<C> assembly) {
        requireNonNull(assembly, "assembly is null");
        return (PackedComponentDriver<? extends C>) VH_BUNDLE_DRIVER.get(assembly);
    }
}
