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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.component.Bundle;
import app.packed.component.WireableComponentDriver;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Helper class to access non-public members in {@link Bundle}. */
public final class BundleConfiguration {

    public static final BundleConfiguration CONSUMED_SUCCESFULLY = new BundleConfiguration();

    /** A VarHandle that can access Bundle#configuration. */
    private static final VarHandle VH_BUNDLE_CONFIGURATION = LookupUtil.vhPrivateOther(MethodHandles.lookup(), Bundle.class, "configuration", Object.class);

    /** A VarHandle used from {@link #driverOf(Bundle)} to access the driver field of a bundle. */
    private static final VarHandle VH_BUNDLE_DRIVER = LookupUtil.vhPrivateOther(MethodHandles.lookup(), Bundle.class, "driver", WireableComponentDriver.class);

    /** A MethodHandle that can invoke Bundle#configure. */
    private static final MethodHandle MH_BUNDLE_CONFIGURE = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), Bundle.class, "configure", void.class);

    private BundleConfiguration() {}

    // Maaske skal den tage en driver og en node???
    // Configuration
    public static void configure(Bundle<?> bundle, Object configuration) {

        // We perform a compare and exchange with configuration. Guarding against
        // concurrent usage of this bundle.

        Object existing = VH_BUNDLE_CONFIGURATION.compareAndExchange(bundle, null, configuration);
        if (existing == null) {
            try {
                MH_BUNDLE_CONFIGURE.invoke(bundle); // Invokes app.packed.component.Bundle#configure();
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            } finally {
                VH_BUNDLE_CONFIGURATION.setVolatile(bundle, BundleConfiguration.CONSUMED_SUCCESFULLY);
            }
        } else if (existing instanceof BundleConfiguration) {
            // Bundle has already been used succesfullly or unsuccesfully
            throw new IllegalStateException("This bundle has already been used, type = " + bundle.getClass());
        } else {
            // Can be this thread or another thread that is already using the bundle.
            throw new IllegalStateException("This bundle is already being used elsewhere, type = " + bundle.getClass());
        }

        // Do we want to cache exceptions?
        // Do we want better error messages, for example, This bundle has already been used to create an artifactImage
        // Do we want to store the calling thread in case of recursive linking..

        // We should have some way to mark it failed????
        // If configure() fails. The ContainerConfiguration still works...
        /// Well we should probably catch the exception from where ever we call his method
    }

    /**
     * Returns the component driver the specified bundle wraps.
     * 
     * @param bundle
     *            the bundle to extract a component driver from
     * @return the specified bundle's component driver
     */
    public static PackedComponentDriver<?> driverOf(Bundle<?> bundle) {
        return (PackedComponentDriver<?>) VH_BUNDLE_DRIVER.get(bundle);
    }
}
