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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.container.Assembly;
import app.packed.framework.Nullable;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.LookupValue;

/**
 * This class exists because we have two ways to access the members of a bean:
 * <ul>
 * <li>{@link ModuleDescriptorAccessor}, which uses a module-info.java descriptor to provide access to bean
 * members.</li>
 * <li>{@link ModuleLookupAccessor}, which uses {@link Lookup} object, for example, via {@link Assembly#lookup(Lookup)}
 * to provide access to bean members.</li>
 * </ul>
 */
public abstract sealed class BeanMemberAccessor {

    Lookup lookup() {
        if (this instanceof ModuleLookupAccessor lookupAccessor) {
            return lookupAccessor.lookup;
        } else {
            return ((ModuleDescriptorAccessor) this).lookup();
        }
    }

    /**
     * Creates a new accessor from the specified lookup.
     * 
     * @param lookup
     *            the lookup to use
     * @return the new accessor
     */
    public abstract BeanMemberAccessor withLookup(Lookup lookup);

    /**
     * Returns a container source model for the specified type
     * 
     * @param sourceType
     *            the container source type
     * @return a container source model for the specified type
     */
    public static BeanMemberAccessor defaultFor(Class<?> sourceType) {
        return ModuleDescriptorAccessor.MODELS.get(sourceType);
    }

    /**
     * An accessor that uses relies on a module being open to Packed. Either via a module descriptor, or via command line
     * arguments such as {@code add-opens}.
     */
    private static final class ModuleDescriptorAccessor extends BeanMemberAccessor {

        /** A cache of accessor. */
        private static final ClassValue<BeanMemberAccessor.ModuleDescriptorAccessor> MODELS = new ClassValue<>() {

            /** {@inheritDoc} */
            @Override
            protected BeanMemberAccessor.ModuleDescriptorAccessor computeValue(Class<?> type) {
                return new ModuleDescriptorAccessor(type);
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
                return new ModuleLookupAccessor(ModuleDescriptorAccessor.this, lookup);
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
        private ModuleDescriptorAccessor(Class<?> realmType) {
            this.type = requireNonNull(realmType);
        }

        /** {@inheritDoc} */
        @Override
        MethodHandles.Lookup lookup() {
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
        public BeanMemberAccessor withLookup(Lookup lookup) {
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

    /**
     * A accessor that relies on the user explicitly providing a {@link Lookup} object, for example, via
     * Assembly#lookup(Lookup).
     */
    private static final class ModuleLookupAccessor extends BeanMemberAccessor {

        /** The parent accessor. */
        private final ModuleDescriptorAccessor defaultAccessor;

        /** The lookup object provided by the user. */
        private final Lookup lookup;

        private ModuleLookupAccessor(ModuleDescriptorAccessor defaultAccessor, Lookup lookup) {
            this.defaultAccessor = requireNonNull(defaultAccessor);
            this.lookup = requireNonNull(lookup);
        }

        @Override
        public BeanMemberAccessor withLookup(Lookup lookup) {
            return defaultAccessor.withLookup(lookup);
        }
    }
}
