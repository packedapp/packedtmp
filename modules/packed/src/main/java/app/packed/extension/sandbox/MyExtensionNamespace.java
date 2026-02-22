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
package app.packed.extension.sandbox;

import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionNamespace;
import app.packed.extension.ExtensionNamespaceHandle;

/**
 *
 */
public final class MyExtensionNamespace extends ExtensionNamespace<MyExtensionNamespace, MyExtension> {

    /**
     * @param handle
     */
    protected MyExtensionNamespace(ExtensionNamespaceHandle<MyExtensionNamespace, MyExtension> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public MyExtension newExtension(ExtensionHandle<MyExtension> extensionHandle) {
        return new MyExtension(this, extensionHandle);
    }
}
