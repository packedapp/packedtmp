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
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.component.Bundle;
import app.packed.inject.Factory;
import packed.internal.classscan.OpenClass;
import packed.internal.methodhandle.LookupUtil;
import packed.internal.util.LookupValue;
import packed.internal.util.Model;
import packed.internal.util.ThrowableUtil;

/** A model of a realm, typically based on a subclass of {@link Bundle}. */
public final class RealmModel extends Model implements SourceModelLookup {

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
    private final LookupValue<PerLookup> lookups = new LookupValue<>() {

        @Override
        protected PerLookup computeValue(Lookup lookup) {
            return new PerLookup(RealmModel.this, lookup);
        }
    };

    /**
     * Creates a new container source model.
     * 
     * @param realmType
     *            the source type
     */
    private RealmModel(Class<? extends Bundle<?>> realmType) {
        super(realmType);
    }

    /** {@inheritDoc} */
    @Override
    public SourceModel modelOf(Class<?> componentType) {
        return componentsNoLookup.get(componentType);
    }

    /** {@inheritDoc} */
    @Override
    public OpenClass newClassProcessor(Class<?> clazz, boolean registerNatives) {
        return new OpenClass(MethodHandles.lookup(), clazz, registerNatives);
    }

    @Override
    public MethodHandle toMethodHandle(Factory<?> factory) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            return (MethodHandle) FACTORY_TO_METHOD_HANDLE.invoke(factory, lookup);
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
        } else if (lookup.lookupClass() == modelType() && LookupUtil.isLookupDefault(lookup)) {
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

    /** A component lookup class wrapping a {@link Lookup} object. */
    private static final class PerLookup implements SourceModelLookup {

        /** A cache of component class descriptors. */
        private final ClassValue<SourceModel> components = new ClassValue<>() {

            @Override
            protected SourceModel computeValue(Class<?> type) {
                return SourceModel.newInstance(parent, PerLookup.this.newClassProcessor(type, true));
            }
        };

        /** The actual lookup object we are wrapping. */
        private final Lookup lookup;

        private final RealmModel parent;

        private PerLookup(RealmModel parent, Lookup lookup) {
            this.parent = requireNonNull(parent);
            this.lookup = requireNonNull(lookup);
        }

        /** {@inheritDoc} */
        @Override
        public SourceModel modelOf(Class<?> componentType) {
            return components.get(componentType);
        }

        /** {@inheritDoc} */
        @Override
        public OpenClass newClassProcessor(Class<?> clazz, boolean registerNatives) {
            return new OpenClass(lookup, clazz, registerNatives);
        }

        @Override
        public MethodHandle toMethodHandle(Factory<?> factory) {
            try {
                return (MethodHandle) FACTORY_TO_METHOD_HANDLE.invoke(factory, lookup);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
    }
}
