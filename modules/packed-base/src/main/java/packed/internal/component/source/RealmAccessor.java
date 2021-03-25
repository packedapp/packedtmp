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

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.inject.Factory;
import packed.internal.invoke.ClassMemberAccessor;
import packed.internal.util.LookupUtil;
import packed.internal.util.LookupValue;
import packed.internal.util.ThrowableUtil;

/**
 * This class exists because we have two ways to access the members of a component instance. One with a {@link Lookup}
 * object, and one using whatever power a module descriptor has given us.
 */
// Realm Accessor
public abstract class RealmAccessor {

    /** Calls package-private method Factory.toMethodHandle(Lookup). */
    private static final MethodHandle MH_FACTORY_TO_METHOD_HANDLE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Factory.class, "toMethodHandle",
            MethodHandle.class, Lookup.class);

    /** A cache of component class descriptors. */
    private final ClassValue<ClassSourceModel> components = new ClassValue<>() {

        @Override
        protected ClassSourceModel computeValue(Class<?> type) {
            ClassMemberAccessor oc = ClassMemberAccessor.of(lookup(), type);
            return new ClassSourceModel.Builder(oc).build();
            //return ClassSourceModel.newModel(oc);
        }
    };

    abstract Lookup lookup();

    final ClassSourceModel modelOf(Class<?> componentType) {
        return components.get(componentType);
    }

    final MethodHandle toMethodHandle(Factory<?> factory) {
        try {
            return (MethodHandle) MH_FACTORY_TO_METHOD_HANDLE.invoke(factory, lookup());
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    public abstract RealmAccessor withLookup(Lookup lookup);

    /**
     * A realm that makes use of a explicitly registered lookup object, for example, via ContainerAssembly#lookup(Lookup).
     */
    private static final class WithLookup extends RealmAccessor {

        /** The actual lookup object we are wrapping. */
        private final Lookup lookup;

        private final WithModuleInfo parent;

        private WithLookup(WithModuleInfo realm, Lookup lookup) {
            this.parent = requireNonNull(realm);
            this.lookup = requireNonNull(lookup);
        }

        /** {@inheritDoc} */
        @Override
        Lookup lookup() {
            return lookup;
        }

        @Override
        public RealmAccessor withLookup(Lookup lookup) {
            return parent.withLookup(lookup);
        }
    }

    /** A model of a realm, typically based on a subclass of {@link Assembly}. */
    public static final class WithModuleInfo extends RealmAccessor {

        /** A cache of realm models. */
        private static final ClassValue<RealmAccessor.WithModuleInfo> MODELS = new ClassValue<>() {

            /** {@inheritDoc} */
            @Override
            protected RealmAccessor.WithModuleInfo computeValue(Class<?> type) {
                return new WithModuleInfo(type);
            }
        };

        private MethodHandles.@Nullable Lookup cachedLookup;

        /** The default lookup object, if using MethodHandles.lookup() from inside a assembly. */
        @Nullable
        private volatile WithLookup defaultLookup;

        /** A cache of lookup values, in 99 % of all cases this will hold no more than 1 value. */
        private final LookupValue<WithLookup> lookups = new LookupValue<>() {

            @Override
            protected WithLookup computeValue(Lookup lookup) {
                return new WithLookup(WithModuleInfo.this, lookup);
            }
        };

        /** The realm type, typically a subclass of {@link Assembly}. */
        final Class<?> type;

        /**
         * Creates a new model.
         * 
         * @param realmType
         *            the realm type
         */
        private WithModuleInfo(Class<?> realmType) {
            this.type = requireNonNull(realmType);
        }

        @Override
        MethodHandles.Lookup lookup() {
            // Making a lookup for the realm.
            MethodHandles.Lookup l = cachedLookup;
            if (l == null) {
                l = cachedLookup = MethodHandles.lookup();

                // See also here teleport method here
                // https://github.com/forax/record-util/blob/master/src/main/java/com/github/forax/recordutil/TraitImpl.java

                l.lookupClass().getModule().addReads(type.getModule());
                try {
                    l = cachedLookup = MethodHandles.privateLookupIn(type, l);
                } catch (IllegalAccessException ignore) {
                    //e.printStackTrace();
                }
            }
            return l;
        }

        /**
         * @param lookup
         *            the lookup object
         * @return the new realm
         */
        public RealmAccessor withLookup(Lookup lookup) {
            // Use default access (this) if we specify null lookup

            // We need to check this in a separate class. Because from Java 13.
            // There are two classes in a lookup object.
            if (lookup == null) {
                return this;
            } else if (lookup.lookupClass() == type && LookupUtil.isLookupDefault(lookup)) {
                // The default lookup is just AssemblyImpl { MethodHandles.lookup()}
                WithLookup cl = defaultLookup;
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
        public static RealmAccessor.WithModuleInfo of(Class<?> sourceType) {
            return MODELS.get(sourceType);
        }
    }
}
