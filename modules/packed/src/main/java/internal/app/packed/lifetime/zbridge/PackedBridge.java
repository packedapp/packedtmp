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
package internal.app.packed.lifetime.zbridge;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.binding.Key;
import app.packed.extension.Extension;

/**
 *
 */

//Ideen er vi share den her mellem EB og Builder
//Vi kan gaa far Builder->EB man aldrig den anden vej
//PackedEB er altid super condensed
//Builderen laver en copy on write

public final class PackedBridge<E> {

    public final Class<? extends Extension<?>> extensionClass;

    PackedBridge(Class<? extends Extension<?>> extensionClass) {
        this.extensionClass = extensionClass;
    }

    public List<Class<?>> invocationArguments() {
        return List.of();
    }

    public Set<Key<?>> keys() {
        return Set.of();
    }

    /**
     * @param action
     * @return
     */
    public PackedBridge<E> onUse(Consumer<? super E> action) {
        return null;
    }

    // ExtensionBean -> T
    public class Extractor {
        Key<?> key;
        
        // Must be resolved in lifetime container...
        Set<Key<?>> requirements; // must only make use of services... Or maybe just resolve it as OperationType
        // Hvor bliver det her en synthetic metode???
        // Paa beanen? Ja det maa det jo vaere...
        // Hvis vi har flere dependencies... kan det jo ikke vaere paa extension beanen...
        // 
        
        MethodHandle extractor;
    }

}
