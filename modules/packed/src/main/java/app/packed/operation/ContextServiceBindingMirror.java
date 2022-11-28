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
package app.packed.operation;

import app.packed.operation.context.OperationContextMirror;
import app.packed.service.Key;

/**
 *
 */
// Ideen er at man kan provide nogle i en operation context...
// Som ikke noedvendigvis er binding hooks
// Er factory? En context??? Hmmm En slags jo...
public non-sealed class ContextServiceBindingMirror extends KeyBasedBindingMirror {

    /** {@return the context that provided the binding.} */
    public OperationContextMirror context() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        throw new UnsupportedOperationException();
    }
}
