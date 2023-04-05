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
package app.packed.context;

import java.util.Map;

import app.packed.bean.BeanMirror;
import app.packed.container.ContainerMirror;
import app.packed.operation.OperationMirror;

/**
 * An element that can operate within a {@link Context context}.
 * <p>
 * Something operates within a static context.
 * <p>
 * By static we mean that it can be determined at build time to always be present. It may be possible that contexts are
 * added dynamically at runtime. For example, by starting a transaction in raw code.
 */
public sealed interface ContextualizedElementMirror permits ContainerMirror, BeanMirror, OperationMirror {

    /** {@return an immutable set of all the contexts that the element operates within.} */
    Map<Class<? extends Context<?>>, ContextMirror> contexts();
}

///** {@return a set of keys that are available to all operations within this component.} */
//// Som default er context key'en tilgaengelig? Nej det skal man vaelge
//default Set<Key<?>> contextKeys() {
//  return Set.copyOf(contexts().values().stream().flatMap(m -> m.keys().stream()).toList());
//}
// Fx for operationMirror, the contexts that only available in the operation
// Tror maaske bare den forvirre mere en den gavner
//default Set<ContextSpanMirror> declaredContexts() {
//    return Set.of();
//}
