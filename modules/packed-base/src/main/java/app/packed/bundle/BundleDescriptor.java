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
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.AnnotatedElement;

import app.packed.inject.Injector;
import app.packed.util.Preview;

/**
 *
 */
public class BundleDescriptor {

    private final Class<? extends Bundle> bundleType;

    /**
     * @param bundleType
     */
    BundleDescriptor(Class<? extends Bundle> bundleType) {
        this.bundleType = bundleType;
    }

    /**
     * Returns any annotations that are present on the bundle. For example, {@link Deprecated} or {@link Preview}.
     * 
     * @return any annotations that are present on the bundle
     */
    public AnnotatedElement annotations() {
        return bundleType;
    }

    /**
     * Returns the type of the bundle.
     * <p>
     * If the bundle is created on the fly, for example, via {@link Injector#of(java.util.function.Consumer)} this method
     * returns {@link Bundle}. Indicating that the bundle type could not be determined.
     *
     * @return the type of the bundle
     */
    public Class<? extends Bundle> getBundleType() {
        return bundleType;
    }

    /**
     * Returns the module that the bundle is a member of. This is normally, the module in which the class extending
     * {@link Bundle} is located.
     * <p>
     * If the bundle is in an unnamed module then the {@linkplain ClassLoader#getUnnamedModule() unnamed} {@code Module} of
     * the class loader for the bundle implementation is returned.
     *
     * @return the module that the bundle is a member of
     * @see Class#getModule()
     */
    public Module getModule() {
        return bundleType.getModule();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("bundle { type: ").append(format(bundleType));
        if (getModule().isNamed()) {
            sb.append(", module: ").append(getModule().getName());
        }

        sb.append(" }");
        return sb.toString();
    }

    /**
     * Returns a descriptor for the specified bundle.
     *
     * @param bundle
     *            the bundle to return a descriptor for
     * @return a descriptor for the specified bundle
     */
    public static BundleDescriptor of(Bundle bundle) {
        requireNonNull(bundle, "bundle is null");
        return new BundleDescriptor(bundle.getClass());
    }

    public static BundleDescriptor of(Class<? extends Bundle> bundleType) {
        return of(Bundles.instantiate(bundleType));
    }

}
