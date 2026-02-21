/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.namespaceold.OldNamespaceHandle;
import internal.app.packed.oldnamespace.OldNamespaceSetup;

/**
 * Access helper for NamespaceHandle and related classes.
 */
public abstract class NamespaceAccessHandler extends AccessHelper {

    private static final Supplier<NamespaceAccessHandler> CONSTANT = StableValue.supplier(() -> init(NamespaceAccessHandler.class, OldNamespaceHandle.class));

    public static NamespaceAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Invokes the protected onClose method on a NamespaceHandle.
     *
     * @param handle the handle
     */
    public abstract void invokeNamespaceOnNamespaceClose(OldNamespaceHandle<?, ?> handle);

    /**
     * Gets the NamespaceSetup from a NamespaceHandle.
     *
     * @param handle the handle
     * @return the namespace setup
     */
    public abstract OldNamespaceSetup getNamespaceHandleNamespace(OldNamespaceHandle<?, ?> handle);
}
