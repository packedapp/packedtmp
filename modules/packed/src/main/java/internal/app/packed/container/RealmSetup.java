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

import app.packed.base.Nullable;
import app.packed.container.AbstractComposer;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.UserOrExtension;
import app.packed.container.Wirelet;
import internal.app.packed.bean.BeanMemberAccessor;
import internal.app.packed.bean.PackedBeanHandleInstaller;
import internal.app.packed.component.ComponentSetup;

/**
 * Configuration of a realm.
 */
public abstract sealed class RealmSetup permits ExtensionRealmSetup, AssemblySetup {

    /** The current module accessor, updated via {@link #lookup(Lookup)} */
    @Nullable
    private BeanMemberAccessor accessor;

    /** The current active component in the realm. */
    @Nullable
    private ComponentSetup currentComponent;

    /** Whether or not this realm is configurable. */
    private boolean isClosed;

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

    void close() {
        wireCurrentComponent();
        isClosed = true;
    }

    /** {@return whether or not the realm is closed.} */
    public final boolean isClosed() {
        return isClosed;
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

    public abstract UserOrExtension realm();

    /**
     * Returns the type that was used to create this realm.
     * 
     * @return the type that was used to create this realm.
     */
    public abstract Class<?> realmType();

    /**
     * @see PackedBeanHandleInstaller#build()
     * @see ContainerConfiguration#link(ContainerHandle, Assembly, Wirelet...)
     * @see RealmSetup#close()
     */
    // TODO add for PackedContainerHandleBuilder
    public void wireCurrentComponent() {
        if (currentComponent != null) {
            currentComponent.onWired();
            currentComponent = null;
        }
    }

    /**
     * Called from the constructor of ComponentSetup whenever a new component is created. {@link #wireCurrentComponent()} must have
     * previously been called unless the component is the first component in the realm.
     * 
     * @param newComponent
     *            the new component
     */
    public void wireNew(ComponentSetup newComponent) {
        assert (currentComponent == null);
        // next is not fully formed but called from the constructor of ComponentSetup
        currentComponent = requireNonNull(newComponent);
    }
}
