/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.util.function.Consumer;

import app.packed.context.Context;
import app.packed.context.ContextualizedElementMirror;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.operation.OperationSetup;

/** An element that may operate within a context. */
public sealed interface ContextualizedComponentSetup permits OperationSetup, BeanSetup {

    @Nullable
    ContextSetup findContext(Class<? extends Context<?>> contextClass);

    void forEachContext(Consumer<? super ContextSetup> action);

    /** {@return a mirror for the element.} */
    ContextualizedElementMirror mirror();
}

////Hvad med nested operations??? Er de i context???????
////Embedded operations er selvfoelgelig
// Men nested, altsaa med mindre vi supportere det paa en eller anden maade

//Der er forskel paa invocation context, og de contexts man er i.

//All contexts skal vel saettes i templates???
