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

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.inject.Factory;
import packed.internal.hooks.usesite.BootstrappedClassModel;
import packed.internal.hooks.usesite.BootstrappedSourcedClassModel;
import packed.internal.invoke.OpenClass;
import packed.internal.util.LookupUtil;
import packed.internal.util.LookupValue;
import packed.internal.util.ThrowableUtil;

/**
 * This class exists because we have two ways to access the members of a component instance. One where the users provide
 * a {@link Lookup} object, for example, via {@link Assembly#lookup(Lookup)}. And another where users use a module
 * descriptor to provide access.
 */
public abstract class RealmAccessor {

    /** Calls package-private method Factory.toMethodHandle(Lookup). */
    private static final MethodHandle MH_FACTORY_TO_METHOD_HANDLE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Factory.class, "toMethodHandle",
            MethodHandle.class, Lookup.class);

    /** A cache of class models per accessor. */
    private final ClassValue<BootstrappedClassModel> components = new ClassValue<>() {

        @Override
        protected BootstrappedClassModel computeValue(Class<?> type) {
            OpenClass oc = OpenClass.of(lookup(), type);
            return BootstrappedSourcedClassModel.newModel(oc, null);
        }
    };

    private Lookup lookup() {
        if (this instanceof ModuleLookupAccessor lookupAccessor) {
            return lookupAccessor.lookup;
        } else {
            return ((ModuleOpenedAccessor) this).lookup();
        }
    }

    public final BootstrappedClassModel modelOf(Class<?> componentType) {
        return components.get(componentType);
    }

    /**
     * Extracts a method handle from the specified factory.s
     * 
     * @param factory
     *            the factory to extract a method handle for
     * @return the method handle
     */
    public final MethodHandle toMethodHandle(Factory<?> factory) {
        try {
            return (MethodHandle) MH_FACTORY_TO_METHOD_HANDLE.invoke(factory, lookup());
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /**
     * Creates a new accessor from the specified lookup.
     * 
     * @param lookup
     *            the lookup to use
     * @return the new accessor
     */
    abstract RealmAccessor withLookup(Lookup lookup);

    /**
     * Returns a container source model for the specified type
     * 
     * @param sourceType
     *            the container source type
     * @return a container source model for the specified type
     */
    static RealmAccessor defaultFor(Class<?> sourceType) {
        return ModuleOpenedAccessor.MODELS.get(sourceType);
    }

    /**
     * A accessor that relies on the user explicitly providing a {@link Lookup} object, for example, via
     * Assembly#lookup(Lookup).
     */
    private static final class ModuleLookupAccessor extends RealmAccessor {

        /** The parent accessor. */
        private final ModuleOpenedAccessor defaultAccessor;

        /** The lookup object provided by the user. */
        private final Lookup lookup;

        private ModuleLookupAccessor(ModuleOpenedAccessor defaultAccessor, Lookup lookup) {
            this.defaultAccessor = requireNonNull(defaultAccessor);
            this.lookup = requireNonNull(lookup);
        }

        @Override
        RealmAccessor withLookup(Lookup lookup) {
            return defaultAccessor.withLookup(lookup);
        }
    }

    /**
     * An accessor that uses relies on a module being open to Packed. Either via a module descriptor, or via command line
     * arguments such as {@code add-opens}.
     */
    private static final class ModuleOpenedAccessor extends RealmAccessor {

        /** A cache of accessor. */
        private static final ClassValue<RealmAccessor.ModuleOpenedAccessor> MODELS = new ClassValue<>() {

            /** {@inheritDoc} */
            @Override
            protected RealmAccessor.ModuleOpenedAccessor computeValue(Class<?> type) {
                return new ModuleOpenedAccessor(type);
            }
        };

        private MethodHandles.@Nullable Lookup cachedLookup;

        /** The default lookup object, if using MethodHandles.lookup() from inside a assembly. */
        @Nullable
        private volatile ModuleLookupAccessor defaultLookup;

        /** A cache of lookup values, in 99 % of all cases this will hold no more than 1 value. */
        private final LookupValue<ModuleLookupAccessor> lookups = new LookupValue<>() {

            @Override
            protected ModuleLookupAccessor computeValue(Lookup lookup) {
                return new ModuleLookupAccessor(ModuleOpenedAccessor.this, lookup);
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
        private ModuleOpenedAccessor(Class<?> realmType) {
            this.type = requireNonNull(realmType);
        }

        
        private MethodHandles.Lookup lookup() {
            // Making a lookup for the realm.
            MethodHandles.Lookup l = cachedLookup;
            if (l == null) {
                l = cachedLookup = MethodHandles.lookup();

                // See also here teleport method here
                // https://github.com/forax/record-util/blob/master/src/main/java/com/github/forax/recordutil/TraitImpl.java

                // Problemet er jo her at den type vi skal bruge er jo en anden
                l.lookupClass().getModule().addReads(type.getModule());

                // Ved ikke om det
                try {
                    l = cachedLookup = l.in(type);// MethodHandles.privateLookupIn(type, l);
                } catch (Exception ignore) {
                    // e.printStackTrace();
                }
            }
            return l;
        }

        /**
         * @param lookup
         *            the lookup object
         * @return the new realm
         */
        @Override
        RealmAccessor withLookup(Lookup lookup) {
            // Use default access (this) if we specify null lookup

            // We need to check this in a separate class. Because from Java 13.
            // There are two classes in a lookup object.
            if (lookup == null) {
                return this;
            } else if (lookup.lookupClass() == type && LookupUtil.isLookupDefault(lookup)) {
                // The default lookup is just AssemblyImpl { MethodHandles.lookup()}
                ModuleLookupAccessor cl = defaultLookup;
                if (cl != null) {
                    return cl;
                }
                return defaultLookup = lookups.get(lookup);
            } else {
                return lookups.get(lookup);
            }
        }
    }
}
