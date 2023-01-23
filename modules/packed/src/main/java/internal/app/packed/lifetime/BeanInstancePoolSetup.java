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
package internal.app.packed.lifetime;

import internal.app.packed.lifetime.runtime.PackedExtensionContext;

/**
 *
 */

// Vi kan sagtens folde bedste foraeldre ind ogsaa...
// Altsaa bruger man kun et enkelt object kan vi jo bare folde det ind...
//[ [GrandParent][Parent], O1, O2, O3]

//Der er faktisk 2 strategier her...
//RepeatableImage -> Har vi 2 pools taenker jeg... En shared, og en per instans
//Ikke repeatable.. Kav vi lave vi noget af array'et paa forhaand... F.eks. smide
//bean instancerne ind i det

//Saa maaske er pool og Lifetime to forskellige ting???
//
public final class BeanInstancePoolSetup {

    /** The size of the pool. */
    private int size;

    public PackedExtensionContext newRuntimePool() {
        return PackedExtensionContext.create(size);
    }

    /**
     * Reserves room for a single object.
     * 
     * @return the index to store the object in at runtime
     */
    BeanInstanceAccessor reserve(Class<?> type) {
        return new BeanInstanceAccessor(size++);
    }
}
