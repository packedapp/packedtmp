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

import app.packed.component.Bundle;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Helper class to access non-public members in {@link Bundle}. */
public final class BundleHelper {

    public static final BundleHelper BUNDLE_CONSUMED = new BundleHelper();

    /** A VarHandle that can access Bundle#configuration. */
    private static final VarHandle VH_BUNDLE_CONFIGURATION = LookupUtil.vhPrivateOther(MethodHandles.lookup(), Bundle.class, "configuration", Object.class);

    /** A VarHandle used from {@link #getDriver(Bundle)} to access the driver field from a {@link Bundle}. */
    private static final VarHandle VH_BUNDLE_DRIVER = LookupUtil.vhPrivateOther(MethodHandles.lookup(), Bundle.class, "driver", PackedComponentDriver.class);

    /** A MethodHandle that can invoke Bundle#configure. */
    private static final MethodHandle MH_BUNDLE_CONFIGURE = LookupUtil.mhVirtualPrivate(MethodHandles.lookup(), Bundle.class, "configure", void.class);

    /** No instances for you. */
    private BundleHelper() {}

    public static void configure(Bundle<?> bundle, Object configuration) {
        // We perform a compare and exchange with configuration. Guarding against
        // concurrent usage of this bundle.

        Object existing = VH_BUNDLE_CONFIGURATION.compareAndExchange(bundle, null, configuration);
        if (existing == null) {
            try {
                // Invokes app.packed.component.Bundle#configure()
                MH_BUNDLE_CONFIGURE.invoke(bundle);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            } finally {
                // sets Bundle.configuration to a marker that indicates the bundle has been consumed
                VH_BUNDLE_CONFIGURATION.setVolatile(bundle, BundleHelper.BUNDLE_CONSUMED);
            }
        } else if (existing instanceof BundleHelper) {
            // Bundle has already been used successfully or unsuccessfully
            throw new IllegalStateException("This bundle has already been used, type = " + bundle.getClass());
        } else {
            // Can be this thread or another thread that is already using the bundle.
            throw new IllegalStateException("This bundle is currently being used elsewhere, type = " + bundle.getClass());
        }

        // Do we want to cache exceptions?
        // Do we want better error messages, for example, This bundle has already been used to create an artifactImage
        // Do we want to store the calling thread in case of recursive linking..

        // We should have some way to mark it failed????
        // If configure() fails. The ContainerConfiguration still works...
        /// Well we should probably catch the exception from where ever we call his method
    }

    /**
     * Extracts the component driver from the specified bundle.
     * 
     * @param bundle
     *            the bundle to extract the component driver from
     * @return the specified bundle's component driver
     * @see #VH_BUNDLE_DRIVER
     */
    public static <C> PackedComponentDriver<? extends C> getDriver(Bundle<C> bundle) {
        requireNonNull(bundle, "bundle is null");
        return (PackedComponentDriver<? extends C>) VH_BUNDLE_DRIVER.get(bundle);
    }
}
