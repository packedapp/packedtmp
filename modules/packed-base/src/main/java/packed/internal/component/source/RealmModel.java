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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import packed.internal.util.LookupUtil;
import packed.internal.util.LookupValue;

/** A model of a realm, typically based on a subclass of {@link Assembly}. */
final class RealmModel extends RealmLookup {

    /** A cache of realm models. */
    private static final ClassValue<RealmModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected RealmModel computeValue(Class<?> type) {
            return new RealmModel(type);
        }
    };

    private MethodHandles.@Nullable Lookup cachedLookup;

    /** The default lookup object, if using MethodHandles.lookup() from inside a bundle. */
    @Nullable
    private volatile ExplicitLookup defaultLookup;

    /** A cache of lookup values, in 99 % of all cases this will hold no more than 1 value. */
    private final LookupValue<ExplicitLookup> lookups = new LookupValue<>() {

        @Override
        protected ExplicitLookup computeValue(Lookup lookup) {
            return new ExplicitLookup(RealmModel.this, lookup);
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
    private RealmModel(Class<?> realmType) {
        this.type = requireNonNull(realmType);
    }

    @Override
    MethodHandles.Lookup lookup() {
        // Making a lookup for the realm.
        MethodHandles.Lookup l = cachedLookup;
        if (l == null) {
            l = cachedLookup = MethodHandles.lookup();
            // This does not work properly with Java 16...
            // And have no idea why I originally put it in.
            //l = cachedLookup = l.in(type);
        }
        return l;
    }

    /** {@inheritDoc} */
    @Override
    RealmModel realm() {
        return this;
    }

    public RealmLookup withLookup(Lookup lookup) {
        // Use default access (this) if we specify null lookup

        // We need to check this in a separate class. Because from Java 13.
        // There are two classes in a lookup object.
        if (lookup == null) {
            return this;
        } else if (lookup.lookupClass() == type && LookupUtil.isLookupDefault(lookup)) {
            // The default lookup is just BundleImpl { MethodHandles.lookup()}
            ExplicitLookup cl = defaultLookup;
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
        return MODELS.get(sourceType);
    }

    /** A realm that makes use of a explicitly registered lookup object, for example, via ContainerBundle#lookup(Lookup). */
    private static final class ExplicitLookup extends RealmLookup {

        /** The actual lookup object we are wrapping. */
        private final Lookup lookup;

        private final RealmModel realm;

        private ExplicitLookup(RealmModel realm, Lookup lookup) {
            this.realm = requireNonNull(realm);
            this.lookup = requireNonNull(lookup);
        }

        /** {@inheritDoc} */
        @Override
        Lookup lookup() {
            return lookup;
        }

        /** {@inheritDoc} */
        @Override
        RealmModel realm() {
            return realm;
        }
    }
}
