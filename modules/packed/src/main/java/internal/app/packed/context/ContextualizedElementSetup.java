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

import app.packed.context.Context;
import app.packed.context.ContextualizedElementMirror;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.operation.OperationSetup;

/** A element that operate within a context. */
public sealed interface ContextualizedElementSetup permits OperationSetup, BeanSetup, ContainerSetup {

    /** {@return a mirror for the element.} */
    ContextualizedElementMirror mirror();

    @Nullable
   default ContextSetup findContext(Class<? extends Context<?>> contextClass) {
        throw new UnsupportedOperationException();
    }
}

//Implementerings muligheder
////With parent
////

////Hvad med nested operations??? Er de i context???????
////Embedded operations er selvfoelgelig

//Der er forskel paa invocation context, og de contexts man er i.

//All contexts skal vel saettes i templates???

//BeanLifetimeOperationContext er vel i virkeligheden en context operation.
//Som er til raadighed for dens nested boern

//ContainerContext er til raadighed for alle dependencies (alle depender on BaseExtension, lol)
//Samtidig kan alle invoke med den..

//private IdentityHashMap<Class<? extends Context<?>>, ContextSetup> m = new IdentityHashMap<>();
//
//boolean isInContext(Class<? extends Context<?>> contextClass) {
//  return m.containsKey(contextClass);
//}
