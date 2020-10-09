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
package packed.internal.component.source;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.component.Bundle;
import app.packed.inject.Factory;
import packed.internal.util.LookupUtil;
import packed.internal.util.LookupValue;
import packed.internal.util.ThrowableUtil;

/** A model of a realm, typically based on a subclass of {@link Bundle}. */
final class RealmModel extends SourceModelLookup {

    /** Calls package-private method Factory.toMethodHandle(Lookup). */
    private static final MethodHandle FACTORY_TO_METHOD_HANDLE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Factory.class, "toMethodHandle",
            MethodHandle.class, Lookup.class);

    /** A cache of model. */
    private static final ClassValue<RealmModel> MODEL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected RealmModel computeValue(Class<?> type) {
            // Is either a bundle, or a ContainerConfigurator subclass
            return new RealmModel((Class<? extends Bundle<?>>) type);
        }
    };

    /** A cache of component models that have been accessed without a lookup object. */
    // most likely they will have the same class loader as the container source
    private final ClassValue<SourceModel> componentsNoLookup = new ClassValue<>() {

        @Override
        protected SourceModel computeValue(Class<?> type) {
            return SourceModel.newInstance(RealmModel.this, RealmModel.this.newClassProcessor(type, true));
        }
    };

    /** The default lookup object, if using MethodHandles.lookup() from inside a bundle. */
    private volatile SourceModelLookup defaultLookup;

    /** A cache of lookup values, in 99 % of all cases this will hold no more than 1 value. */
    private final LookupValue<ExplicitLookup> lookups = new LookupValue<>() {

        @Override
        protected ExplicitLookup computeValue(Lookup lookup) {
            return new ExplicitLookup(RealmModel.this, lookup);
        }
    };

    /**
     * Creates a new container source model.
     * 
     * @param realmType
     *            the source type
     */
    private RealmModel(Class<? extends Bundle<?>> realmType) {
        this.type = requireNonNull(realmType);
    }

    private final Class<?> type;

    private MethodHandles.Lookup cachedLookup;

    @Override
    MethodHandles.Lookup lookup() {
        // Making a lookup for the realm.
        MethodHandles.Lookup l = cachedLookup;
        if (l == null) {
            l = MethodHandles.lookup();
            l = cachedLookup = l.in(type);
        }
        return l;
    }

    /** {@inheritDoc} */
    @Override
    public SourceModel modelOf(Class<?> componentType) {
        return componentsNoLookup.get(componentType);
    }

    @Override
    public MethodHandle toMethodHandle(Factory<?> factory) {
        try {
            return (MethodHandle) FACTORY_TO_METHOD_HANDLE.invoke(factory, lookup());
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    public SourceModelLookup withLookup(Lookup lookup) {
        // Use default access (this) if we specify null lookup

        // We need to check this in a separate class. Because from Java 13.
        // There are two classes in a lookup object.
        if (lookup == null) {
            return this;
        } else if (lookup.lookupClass() == type && LookupUtil.isLookupDefault(lookup)) {
            // The default lookup is just BundleImpl { MethodHandles.lookup()}
            SourceModelLookup cl = defaultLookup;
            if (cl != null) {
                return cl;
            }
            return defaultLookup = lookups.get(lookup);
        } else {
            return lookups.get(lookup);
        }
    }

    /**
     * Returns a container source model for the specified type
     * 
     * @param sourceType
     *            the container source type
     * @return a container source model for the specified type
     */
    public static RealmModel of(Class<?> sourceType) {
        return MODEL_CACHE.get(sourceType);
    }

    /** A realm that makes use of a explicitly registered lookup object, for example, via ContainerBundle#lookup(Lookup). */
    private static final class ExplicitLookup extends SourceModelLookup {

        /** A cache of component class descriptors. */
        private final ClassValue<SourceModel> components = new ClassValue<>() {

            @Override
            protected SourceModel computeValue(Class<?> type) {
                return SourceModel.newInstance(realm, ExplicitLookup.this.newClassProcessor(type, true));
            }
        };

        /** The actual lookup object we are wrapping. */
        private final Lookup lookup;

        private final RealmModel realm;

        private ExplicitLookup(RealmModel realm, Lookup lookup) {
            this.realm = requireNonNull(realm);
            this.lookup = requireNonNull(lookup);
        }

        /** {@inheritDoc} */
        @Override
        public SourceModel modelOf(Class<?> componentType) {
            return components.get(componentType);
        }

        @Override
        public MethodHandle toMethodHandle(Factory<?> factory) {
            try {
                return (MethodHandle) FACTORY_TO_METHOD_HANDLE.invoke(factory, lookup);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        /** {@inheritDoc} */
        @Override
        Lookup lookup() {
            return lookup;
        }
    }
}
