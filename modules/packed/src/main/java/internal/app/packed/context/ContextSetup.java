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
package internal.app.packed.context;

import java.util.IdentityHashMap;

import app.packed.context.Context;

// Implementerings muligheder
//// With parent
////

//// Hvad med nested operations??? Er de i context???????
//// Embedded operations er selvfoelgelig

// Der er forskel paa invocation context, og de contexts man er i.

/**
 *
 */
// All contexts skal vel saettes i templates???

// BeanLifetimeOperationContext er vel i virkeligheden en context operation.
// Som er til raadighed for dens nested boern

// ContainerContext er til raadighed for alle dependencies (alle depender on BaseExtension, lol)
// Samtidig kan alle invoke med den..
public final class ContextSetup {

    private final IdentityHashMap<Class<? extends Context<?>>, ContextEntry> m = new IdentityHashMap<>();

    public boolean isInContext(Class<? extends Context<?>> contextClass) {
        return m.containsKey(contextClass);
    }

    static class ContextEntry {

    }
}
