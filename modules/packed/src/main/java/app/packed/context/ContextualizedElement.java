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

import java.util.Set;

import app.packed.service.Key;

/**
 * Something operates within a static context.
 * <p>
 * By static we mean that it can be determined at build time to always be present. It may be possible that contexts are
 * added dynamically at runtime. For example, by starting a transaction in raw code.
 * 
 * 
 */
// An element that can operate within a context
public interface ContextualizedElement {

    /** {@return a set of keys that are available to all operations within this component.} */
    default Set<Key<?>> contextKeys() {
        return Set.copyOf(contexts().stream().flatMap(m -> m.keys().stream()).toList());
    }

    /** {@return an immutable set of any contexts that the element operates within.} */

    // Operation will also inherit for beans
    default Set<ContextSpanMirror> contexts() {
        return Set.of();
    }
}

// Fx for operationMirror, the contexts that only available in the operation
// Tror maaske bare den forvirre mere en den gavner
//default Set<ContextSpanMirror> declaredContexts() {
//    return Set.of();
//}
