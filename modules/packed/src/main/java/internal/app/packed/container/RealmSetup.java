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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.container.Realm;
import app.packed.framework.Nullable;
import internal.app.packed.bean.BeanMemberAccessor;

/**
 * Configuration of a realm.
 */
public abstract sealed class RealmSetup permits ExtensionTreeSetup, AssemblySetup {

    /** The current module accessor, updated via {@link #lookup(Lookup)} */
    @Nullable
    private BeanMemberAccessor accessor;

    // Maaske vi flytter vi den til ContainerRealmSetup
    // Hvis man har brug for Lookup i en extension... Saa maa man bruge Factory.of(Class).lookup());
    // Jaaa, men det klare jo ogsaa @JavaBaseSupport
    public final BeanMemberAccessor beanAccessor() {
        BeanMemberAccessor r = accessor;
        if (r == null) {
            this.accessor = r = BeanMemberAccessor.defaultFor(realmType());
        }
        return r;
    }

    /**
     * @return
     */
    public abstract boolean isConfigurable();

    /**
     * @param lookup
     *            the lookup to use
     * @see Assembly#lookup(Lookup)
     * @see AbstractComposer#lookup(Lookup)
     */
    public void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        this.accessor = beanAccessor().withLookup(lookup);
    }

    public abstract Realm realm();

    /**
     * Returns the type that was used to create this realm.
     *
     * @return the type that was used to create this realm.
     */
    // rename to lookupClass()???;
    public abstract Class<?> realmType();

}
