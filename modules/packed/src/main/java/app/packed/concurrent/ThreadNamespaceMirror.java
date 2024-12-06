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
package app.packed.concurrent;

import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceMirror;

/**
 * A mirror for a thread namespace.
 * <p>
 * A application
 */
// We must have one per extension...
// I mean we have a service namespace per extension.
// So would probably be strange to share one for extensions?
public class ThreadNamespaceMirror extends NamespaceMirror<BaseExtension> {

    /**
     * @param handle
     */
    public ThreadNamespaceMirror(NamespaceHandle<BaseExtension, ?> handle) {
        super(handle);
    }

}
