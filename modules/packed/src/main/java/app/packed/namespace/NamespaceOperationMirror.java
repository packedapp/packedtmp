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
package app.packed.namespace;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;

/**
 * An operation within a namespace.
 */

// Hmm, how is this different from a resource????????????????
// Im not sure this class exists have this here...
// Instead we have

// Yes, er vi altid tilknyttet en bean???
// Maske er operation Optional
@Deprecated
abstract class NamespaceOperationMirror extends OperationMirror {

    /**
     * @param handle
     */
    public NamespaceOperationMirror(OperationHandle<?> handle) {
        super(handle);
    }

    public abstract NamespaceMirror<?> namespace();
}
