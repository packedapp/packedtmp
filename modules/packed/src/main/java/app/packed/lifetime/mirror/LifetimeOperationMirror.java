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
package app.packed.lifetime.mirror;

import java.util.List;

import app.packed.operation.OperationMirror;

/**
 *
 */
// Er operationer som udgangspunkt hirakiske?
// Er det ene speciel operations type?
// Eller er det case to case
public class LifetimeOperationMirror extends /* Nested */ OperationMirror {

    public boolean isAvailableExternal() {
        return true;
    }

    
    public boolean isAvailableInternal() {
        // Launching operation is never available internal
        // Ved ikke om de giver mening de her operationer
        // Det er jo kun explicit. Hvis nu en traad fejler
        // saa er det jo ogsaa internt
        return true;
    }
    
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
