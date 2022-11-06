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
package internal.app.packed.application;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.container.Extension;

/**
 *
 */

//Ideen er vi share den her mellem EB og Builder
//Vi kan gaa far Builder->EB man aldrig den anden vej
//PackedEB er altid super condensed
//Builderen laver en copy on write

public final class PackedBridge<E> {
    
    final Class<? extends Extension<?>> extensionClass;
    
    PackedBridge(Class<? extends Extension<?>> extensionClass) {
        this.extensionClass = extensionClass;
    }

    /**
     * @param action
     * @return
     */
    public PackedBridge<E> onUse(Consumer<E> action) {
        return null;
    }
    
    public List<Class<?>> invocationArguments() {
        return List.of();
    }
    
    public Set<Key<?>> keys() {
        return Set.of();
    }
    
    
    // ExtensionBean -> T
    public class Extractor {
        
    }
}
