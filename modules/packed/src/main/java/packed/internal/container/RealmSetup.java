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

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.component.Realm;
import packed.internal.bean.BeanModelManager;
import packed.internal.component.ComponentSetup;

/**
 * Configuration of a realm.
 */
// BuildRealm???? Is this runtime at all???

// Tror maaske det her er 2 forskellige ting???

// Hvem der ejer den som vi har en af per application!!!!

//// RealmSetup :-> ApplicationRealmSetup, ExtensionRealmSetup

// Maaske er det endda ApplicationSetup .. Det er kun ExtensionRealmSetup der skal kunne lukkes


//// ContainerConfigurator : 

// Og formattet Assemly vs Composer

public abstract sealed class RealmSetup permits ExtensionRealmSetup, AssemblySetup {

    /** The current module accessor, updated via {@link #lookup(Lookup)} */
    BeanModelManager accessor;

    /** The current active component in the realm. */
    protected ComponentSetup currentComponent;

    /** Whether or not this realm is closed. */
    boolean isClosed;

    // Maaske vi flytter vi den til ContainerRealmSetup
    // Hvis man har brug for Lookup i en extension... Saa maa man bruge Factory.of(Class).lookup());
    // Jaaa, men det klare jo ogsaa @JavaBaseSupport
    public final BeanModelManager accessor() {
        BeanModelManager r = accessor;
        if (r == null) {
            this.accessor = r = BeanModelManager.defaultFor(realmType());
        }
        return r;
    }

    public abstract Realm realm();

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
