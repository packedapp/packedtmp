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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import app.packed.binding.Key;
import app.packed.extension.Extension;
import app.packed.framework.Nullable;
import internal.app.packed.container.ExtensionSetup;

/**
 *
 */

// Ban FileExtension

// Callbacks on Extensions

// Vi installere altid en parent extension foerst...
// Hvorefter vi sagtens kan

// Vi har et samlet Key<?> set. Maa have unikke noegler
// Vi har en ArgumentList List<Class<?>> som bliver added til invocation type

// Er fixed for en ApplicationDriver
// Er "variable" for en Session (altsaa bliver lavet for en one time use)
public final class ContainerLifetimeLaunch {

    List<PackedBridge<?>> bridges;

    // Must check this map, when installing extensions in a container lifetime root

    // We also have forbidden extensions from layers
    @Nullable
    private final Map<Class<? extends Extension<?>>, Consumer<? super Extension<?>>> onUse = null;

    void installed(ExtensionSetup es) {
        var c = onUse.get(es.extensionType);
        if (c != null) {
            c.accept(es.instance());
        }
    }

    // An internal builder
    public final class Builder {

        final HashSet<Key<?>> keys = new HashSet<>();

        public Builder managed() {
            return this;
        }
        
        void add(PackedBridge<?> bridge) {
            // if contains any keys -> fail
            // or if provides the same service -> fail
            
        }
    }
}
