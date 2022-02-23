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
import app.packed.container.Assembly;
import app.packed.container.Composer;
import packed.internal.component.ComponentSetup;

/**
 * Configuration of a realm.
 */
// BuildRealm???? Is this runtime at all???
public abstract sealed class RealmSetup permits ExtensionApplicationRegion,ComponentInstaller {

    /** The current module accessor, updated via {@link #lookup(Lookup)} */
    private RealmAccessor accessor;

    /** The current active component in the realm. */
    protected ComponentSetup currentComponent;

    /** Whether or not this realm is closed. */
    protected boolean isClosed;

    // Maaske vi flytter vi den til ContainerRealmSetup
    // Hvis man har brug for Lookup i en extension... Saa maa man bruge Factory.of(Class).lookup());
    // Jaaa, men det klare jo ogsaa @JavaBaseSupport
    public RealmAccessor accessor() {
        RealmAccessor r = accessor;
        if (r == null) {
            this.accessor = r = RealmAccessor.defaultFor(realmType());
        }
        return r;
    }

    public void checkOpen() {
        // Tror maaske hellere vi skal kalde newOperation
        if (isClosed) {
            throw new IllegalStateException();
        }
    }

    public ComponentSetup currentComponent() {
        return currentComponent;
    }

    public void newOperation() {
        if (currentComponent != null) {
            currentComponent.onWired();
            currentComponent = null;
        }
    }

    /**
     * Returns the type that was used to create this realm.
     * 
     * @return the type that was used to create this realm.
     */
    public abstract Class<?> realmType();

    /**
     * @param lookup
     *            the lookup to use
     * @see Assembly#lookup(Lookup)
     * @see Composer#lookup(Lookup)
     */
    public void lookup(@Nullable Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        this.accessor = accessor().withLookup(lookup);
    }

    public void wireCommit(ComponentSetup component) {
        currentComponent = component;
//
//        // TODO: Move to class I think
//        if (component instanceof ContainerSetup container) {
//            if (container.parent == null || container.parent.realm != this) {
//                rootContainers.add(container);
//            }
//        }
    }

    public void wirePrepare() {
        if (isClosed) {
            throw new IllegalStateException();
        }
        // We need to finish the existing wiring before adding new
        if (currentComponent != null) {
            currentComponent.onWired();
            currentComponent = null;
        }
    }

}
