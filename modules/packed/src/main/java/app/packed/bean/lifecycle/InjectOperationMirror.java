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
package app.packed.bean.lifecycle;

import static java.util.Objects.requireNonNull;

import java.util.List;

import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationInitializeHandle;

/** A mirror representing an {@link OnInitialize} operation. */
// Vi prøvede vist at samle alle Initialization handles. Men maaske er det fint at de har hver deres
public final class InjectOperationMirror extends BeanLifecycleOperationMirror {

    /** A handle for the initialization operation. */
    final LifecycleOperationInitializeHandle handle;

    /**
     * Create a new mirror.
     *
     * @param handle
     *            the operation's handle
     */
    InjectOperationMirror(LifecycleOperationInitializeHandle handle) {
        super(handle);
        this.handle = requireNonNull(handle);
    }

    // ListWithPointer bean()
    // ListWithPointer lifetime();

    // Maybe OperationMirrorList
    public List<InjectOperationMirror> friends() {
        throw new UnsupportedOperationException();
    }
    // ListWithPointer into initialization methods on the bean
}
