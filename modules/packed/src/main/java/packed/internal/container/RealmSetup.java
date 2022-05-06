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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.base.Nullable;
import app.packed.component.Realm;
import app.packed.container.AbstractComposer;
import app.packed.container.Assembly;
import packed.internal.bean.BeanAccessor;
import packed.internal.component.ComponentSetup;

/**
 * Configuration of a realm.
 */
public abstract sealed class RealmSetup permits ExtensionRealmSetup, UserRealmSetup {

    /** The current module accessor, updated via {@link #lookup(Lookup)} */
    @Nullable
    private BeanAccessor accessor;

    /** The current active component in the realm. */
    @Nullable
    protected ComponentSetup currentComponent;

    /** Whether or not this realm is configurable. */
    boolean isNonConfigurable;

    // Maaske vi flytter vi den til ContainerRealmSetup
    // Hvis man har brug for Lookup i en extension... Saa maa man bruge Factory.of(Class).lookup());
    // Jaaa, men det klare jo ogsaa @JavaBaseSupport
    public final BeanAccessor beanAccessor() {
        BeanAccessor r = accessor;
        if (r == null) {
            this.accessor = r = BeanAccessor.defaultFor(realmType());
        }
        return r;
    }

    public void checkIsConfigurable() {
        // Tror maaske hellere vi skal kalde newOperation
        if (isNonConfigurable) {
            throw new IllegalStateException();
        }
    }

    public boolean isCurrent(ComponentSetup component) {
        return currentComponent == component;
    }

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
    public abstract Class<?> realmType();


    public void newOperation() {
        if (currentComponent != null) {
            currentComponent.onWired();
            currentComponent = null;
        }
    }

    
    void initialize(ContainerSetup component) {
        currentComponent = component;
    }

    
    public void wireCommit(ComponentSetup component) {
        currentComponent = component;
    }

    public void wirePrepare() {
        if (isNonConfigurable) {
            throw new IllegalStateException();
        }
        // We need to finish the existing wiring before adding new
        if (currentComponent != null) {
            currentComponent.onWired();
            currentComponent = null;
        }
    }
}
