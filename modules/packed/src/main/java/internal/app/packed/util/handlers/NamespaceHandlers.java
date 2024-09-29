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

import app.packed.namespace.NamespaceHandle;
import internal.app.packed.namespace.NamespaceSetup;

/**
 *
 */
public final class NamespaceHandlers extends Handlers {

    /** A handle for invoking the protected method {@link NamespaceHandle#onNamespaceClose()}. */
    private static final MethodHandle MH_HANDLE_ON_NAMESPACE_CLOSE =method(MethodHandles.lookup(), NamespaceHandle.class, "onNamespaceClose",
            void.class);

    /** A handle that can access {@link NamespaceHandle#namespace}. */
    private static final VarHandle VH_NAMESPACE_HANDLE_TO_SETUP = field(MethodHandles.lookup(), NamespaceHandle.class, "namespace", NamespaceSetup.class);

    /** Call {@link Extension#onAssemblyClose()}. */
    public static void invokeNamespaceOnNamespaceClose(NamespaceHandle<?, ?> handle) {
        try {
            MH_HANDLE_ON_NAMESPACE_CLOSE.invokeExact(handle);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    public static NamespaceSetup getNamespaceHandleNamespace(NamespaceHandle<?, ?> handle) {
        return (NamespaceSetup) VH_NAMESPACE_HANDLE_TO_SETUP.get(handle);
    }
}
