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
package app.packed.service.bridge;

import app.packed.service.ServiceNamespaceMirror;

/**
 *
 */
public interface ServiceNamespaceBridgeMirror extends NamespaceBridgeMirror<ServiceNamespaceMirror> {
    String ACTION_REMOVED = "removed";

    // remove [Key, Operation]
    // peek [Key, Operation, Class<?> function]
    // remove [Key, Key, Operation]
    // decorate [Key, Operation, Class<?> function]

    // replace [Key, Class]
    // map [Map<Keys, Operation>, Key, OperationType]

}
// replaceOp [Map<Keys, Operation>, Key, OperationType] // Is map + replace

