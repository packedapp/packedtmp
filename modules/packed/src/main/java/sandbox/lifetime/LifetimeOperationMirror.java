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
package sandbox.lifetime;

import java.util.List;

import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.CompositeOperationMirror;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;

/**
 * An operation whose sole purpose is to create and/or destroy lifetimes.
 * <p>
 * Ot
 *
 */
// Ved ikke om den kommer med som en egentlig operation
// Giver god mening med, vi bliver ihvertfald noedt til at gentaenke
// OperationTarget, Skal vi have en nested type?

// Altsaa hvis en @Get laver en ny bean saa er det jo ikke en LifetimeOperation
// WebGetOperationMirror ved jo ikke om den laver en ny lifetime...

// Og saa alligevel.. @Get -> Kalder jo ogsaa Bean.factory operationen
// Har vi nogle prefix operationer?

// Virker den kun hvis man har create and destroy a.la. Session.
// Men App.run() med kun en operations


public final class LifetimeOperationMirror extends CompositeOperationMirror {

    /**
     * @param handle
     */
    public LifetimeOperationMirror(OperationHandle<?> handle) {
        super(handle);
    }

    // Wehether or not this lifetime operation concerns the bean that defines it.
    // Or a container
    // Men
    public boolean managesOwnBean() {
        return false; //managedLifetime.isContainer(); maybe we don't need a method
    }

    // Hmmm.. IDK Hvorfor skal den vaere paa en seperat bean (for beans?)
    // Kan vel ligges paa en ExtensionBean taenker jeg???
    public LifetimeMirror lifetime() {
        // Altsaa application vil nogle gange laver flere jo.
        throw new UnsupportedOperationException();
    }
//
//    public boolean isAvailableExternal() {
//        return true;
//    }
//
//    public boolean isAvailableInternal() {
//        // Launching operation is never available internal
//        // Ved ikke om de giver mening de her operationer
//        // Det er jo kun explicit. Hvis nu en traad fejler
//        // saa er det jo ogsaa internt
//        return true;
//    }

    // OperationPlan (me thinks)
    // SingleThreaded
    public List<OperationMirror> operations() {
        // Her er taenkt alle operationer der bliver koert som result
        // En slags composite operation
        // Kan jo baade vaere serielt og parallelt.

        // Maaske har vi en specielt "liste"
        // Maaske er det bare aabent for interapition

        // Kan alle operationer vaere composites????
        return List.of();
    }
}
