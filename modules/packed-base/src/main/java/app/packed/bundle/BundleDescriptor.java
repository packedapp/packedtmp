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
import java.util.Map;
import java.util.Set;

import app.packed.inject.Injector;
import app.packed.inject.Key;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Preview;
import packed.internal.inject.buildnodes.InternalBundleDescriptor;

/**
 * A bundle descriptor.
 */
public class BundleDescriptor {

    /** The type of the bundle. */
    private final Class<? extends Bundle> bundleType;

    final Map<Key<?>, ServiceDescriptor> exposedServices;

    final Set<Key<?>> optionalServices;

    final Set<Key<?>> requiredServices;

    private Services services;

    /**
     * Creates a new descriptor.
     * 
     * @param bundleType
     *            the type of the bundle
     */
    BundleDescriptor(Class<? extends Bundle> bundleType, InternalBundleDescriptor ibd) {
        this.bundleType = bundleType;
        this.exposedServices = ibd.exposedServices;
        this.optionalServices = ibd.optionalServices;
        this.requiredServices = ibd.requiredServices;
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
    public Class<? extends Bundle> bundleType() {
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
        // Gider bi bruge denne metode????? Maaske bare skip den
        return bundleType.getModule();
    }

    public Services services() {
        Services s = services;
        return s == null ? services = new Services() : s;
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
        return new BundleDescriptor(bundle.getClass(), InternalBundleDescriptor.of(bundle));
    }

    public static BundleDescriptor of(Class<? extends Bundle> bundleType) {
        return of(Bundles.instantiate(bundleType));
    }

    public class Services {
        Services() {}

        /**
         * Returns an immutable map of all services that are available for importing.
         * <p>
         * A service whose key have been remapped will have t <pre> {@code
         *  Key<Integer> -> Descriptor<Key<Integer>, "MyService>
         *  importService(Integer.class).as(new Key<@Left Integer>));
         *  Key<Integer> -> Descriptor<Key<@Left Integer>, "MyService>
         *  Note the key of the map has not changed, only the key of the descriptor.}
         * </pre> Eller ogsaa er det kun i imported servicess?????Ja det tror jeg
         *
         * @return a map of all services that available to import
         */
        public Map<Key<?>, ServiceDescriptor> exposedServices() {
            return exposedServices;
        }

        /**
         * Returns an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity.
         * 
         * @return an immutable set of all service keys that <b>can, but do have to</b> be made available to the entity
         */
        public Set<Key<?>> optionalServices() {
            return optionalServices;
        }

        /**
         * Returns an immutable set of all service keys that <b>must</b> be made available to the entity.
         * 
         * @return an immutable set of all service keys that <b>must</b> be made available to the entity
         */
        public Set<Key<?>> requiredServices() {
            return requiredServices;
        }
    }
}
