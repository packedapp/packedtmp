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
import internal.app.packed.bean.BeanProps;
import internal.app.packed.bean.BeanSetup;

/**
 * Configuration of a realm.
 */
public abstract sealed class RealmSetup permits ExtensionRealmSetup, AssemblySetup {

    /** The current module accessor, updated via {@link #lookup(Lookup)} */
    @Nullable
    private BeanMemberAccessor accessor;

    /** The current active component in the realm. */
    @Nullable
    private BeanOrContainerSetup currentComponent;

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

    public boolean isCurrent(BeanOrContainerSetup component) {
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
    // rename to lookupClass()???;
    public abstract Class<?> realmType();

    /**
     * @see BeanProps#build()
     * @see ContainerConfiguration#link(ContainerHandle, Assembly, Wirelet...)
     * @see RealmSetup#close()
     */
    // TODO add for PackedContainerHandleBuilder
    public void wireCurrentComponent() {
        if (currentComponent != null) {
            if (currentComponent instanceof BeanSetup bs) {
                bs.onWired();
            }
            currentComponent = null;
        }
    }

    /**
     * Called from the constructor of ComponentSetup whenever a new component is created. {@link #wireCurrentComponent()}
     * must have previously been called unless the component is the first component in the realm.
     * 
     * @param newComponent
     *            the new component
     */
    public void wireNew(BeanOrContainerSetup newComponent) {
        assert (currentComponent == null);
        // next is not fully formed but called from the constructor of ComponentSetup
        currentComponent = requireNonNull(newComponent);
    }

    public void checkIsCurrent(Object o) {
        if (o != currentComponent) {
            String errorMsg;
            // if (realm.container == this) {
            errorMsg = "This operation must be called as the first thing in Assembly#build()";
            // } else {
            // errorMsg = "This operation must be called immediately after the component has been wired";
            // }
            // is it just named(), in that case we should say it explicityly instead of just saying "this operation"
            throw new IllegalStateException(errorMsg);
        }
    }
}
//public interface RealmConfiguration {
//
//    // The current component that is being wired
//    // empty if the realm is no longer configurable
//    Optional<NamespacePath> activeComponent(); // ComponentConfiguration??? Det er jo internt i realmen
//
//    boolean isConfigurable();
//
//    // Vil helst ikke have extensions til at bruge dem...
//    void lookup(MethodHandles.Lookup lookup);
//}
