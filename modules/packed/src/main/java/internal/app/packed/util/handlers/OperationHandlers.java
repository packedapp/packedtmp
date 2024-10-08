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
package internal.app.packed.util.handlers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.operation.OperationHandle;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class OperationHandlers extends Handlers {

    /** A handle that can access {@link OperationHandle#handle}. */
    private static final VarHandle VH_OPERATION_HANDLE_TO_SETUP = field(MethodHandles.lookup(), OperationHandle.class, "operation",
            OperationSetup.class);

    /**
     * Extracts an operation setup from an operation handle.
     *
     * @param handle
     *            the handle to extract from
     * @return the operation setup
     */
    public static OperationSetup getOperationHandleOperation(OperationHandle<?> handle) {
        return (OperationSetup) VH_OPERATION_HANDLE_TO_SETUP.get(handle);
    }

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_OPERATION_HANDLE_DO_CLOSE = method(MethodHandles.lookup(), OperationHandle.class, "doClose", void.class);

    /** Call {@link OperationHandle#onClose()}. */
    public static void invokeOperationHandleDoClose(OperationHandle<?> handle) {
        try {
            MH_OPERATION_HANDLE_DO_CLOSE.invokeExact(handle);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }
}
