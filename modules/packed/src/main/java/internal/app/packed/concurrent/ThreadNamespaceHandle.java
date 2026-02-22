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
package internal.app.packed.concurrent;

import app.packed.component.ComponentRealm;
import app.packed.concurrent.ThreadNamespaceConfiguration;
import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionHandle;
import app.packed.namespaceold.NamespaceInstaller;
import app.packed.namespaceold.OldNamespaceTemplate;
import app.packed.namespaceold.OldNamespaceHandle;

/**
 * A namespace for the thread management in Packed.
 */
public final class ThreadNamespaceHandle extends OldNamespaceHandle<BaseExtension, ThreadNamespaceConfiguration> {

    /** The default thread namespace template. */
    public static final OldNamespaceTemplate<ThreadNamespaceHandle> TEMPLATE = OldNamespaceTemplate.of(ThreadNamespaceHandle.class, ThreadNamespaceHandle::new);

    /**
     * Create a new handle.
     *
     * @param installer
     *            the namespace installer
     */
    protected ThreadNamespaceHandle(NamespaceInstaller<?> installer) {
        super(installer);
    }

    /** {@inheritDoc} */
    @Override
    protected ThreadNamespaceConfiguration newNamespaceConfiguration(BaseExtension e, ComponentRealm actor) {
        return new ThreadNamespaceConfiguration(this, e, actor);
    }

    public static ThreadNamespaceHandle mainHandle(ExtensionHandle<BaseExtension> handle) {
        return handle.namespaceLazy(ThreadNamespaceHandle.TEMPLATE);
    }
}
