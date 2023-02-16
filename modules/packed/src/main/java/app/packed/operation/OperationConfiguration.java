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

import static java.util.Objects.requireNonNull;

import app.packed.extension.operation.OperationHandle;

/**
 *
 */
// Hmm, fungere ikke super godt med Bean.findOperation();
// Med mindre vi giver muligheden for at saette det ala operation mirror
public class OperationConfiguration {

    /** The operation handle. */
    private final OperationHandle handle;

    /**
     * Create a new bean configuration using the specified handle.
     *
     * @param handle
     *            the bean handle
     */
    public OperationConfiguration(OperationHandle handle) {
        this.handle = requireNonNull(handle, "handle is null");
    }

    protected final void checkConfigurable() {

    }

    protected final OperationHandle handle() {
        return handle;
    }

    OperationConfiguration runBefore(Runnable runnable) {
        return this;
    }

    /** {@return the target of the operation.} */
    public OperationTarget target() {
        return handle.target();
    }
}
