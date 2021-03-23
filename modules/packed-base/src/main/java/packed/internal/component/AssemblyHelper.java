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

    /** A marker object to indicate that the assembly has been used. */
    public static final Object ASSEMBLY_USED = new Object();

    /** A handle that can invoke {@link Assembly#build()}. */
    private static final MethodHandle MH_ASSEMBLY_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Assembly.class, "build", void.class);

    /** A handle that can access Assembly#configuration. */
    private static final VarHandle VH_ASSEMBLY_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Assembly.class, "configuration",
            Object.class);

    /** A handle that can access Assembly#driver. */
    private static final VarHandle VH_ASSEMBLY_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Assembly.class, "driver",
            PackedComponentDriver.class);

    /** No instances for you. */
    private AssemblyHelper() {}

    /**
     * Extracts the component driver from the specified assembly.
     * 
     * @param assembly
     *            the assembly to extract the component driver from
     * @return the component driver of the specified assembly
     */
    static <C extends ComponentConfiguration> PackedComponentDriver<? extends C> getDriver(Assembly<C> assembly) {
        requireNonNull(assembly, "assembly is null");
        return (PackedComponentDriver<? extends C>) VH_ASSEMBLY_DRIVER.get(assembly);
    }

    /**
     * Invokes {@link Assembly#build()} on the specified assembly. Setting the assembly's configuration to the specified
     * configuration.
     * 
     * @param assembly
     *            the assembly for which to invoke the build method
     * @param configuration
     *            the configuration object to set before invoking the build method
     * @see BuildSetup#buildFromAssembly(PackedApplicationDriver, Assembly, app.packed.component.Wirelet[], boolean,
     *      boolean)
     * @see ComponentSetup#link(Assembly, app.packed.component.Wirelet...)
     */
    static void invokeBuild(Assembly<?> assembly, Object configuration) {
        // We perform a compare and exchange. Guarding against concurrent usage of this assembly.
        // I actually don't think we need to use volatile...IDK
        // I'm pretty sure you will get an exception one way or the other...
        Object existing = VH_ASSEMBLY_CONFIGURATION.compareAndExchange(assembly, null, configuration);
        if (existing == null) {
            try {
                MH_ASSEMBLY_BUILD.invoke(assembly); // Invokes Assembly#build()
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            } finally {
                // sets Assembly.configuration to a marker that indicates the assembly has been consumed
                VH_ASSEMBLY_CONFIGURATION.setVolatile(assembly, AssemblyHelper.ASSEMBLY_USED);
            }
        } else if (existing == ASSEMBLY_USED) {
            // Assembly has already been used (successfully or unsuccessfully)
            throw new IllegalStateException("This assembly has already been used, type = " + assembly.getClass());
        } else {
            // Can be this thread or another thread that is already using the assembly.
            throw new IllegalStateException("This assembly is currently being used elsewhere, type = " + assembly.getClass());
        }
    }
}
