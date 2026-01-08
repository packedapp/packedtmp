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
package internal.app.packed.concurrent.daemon;

import app.packed.component.ComponentRealm;
import app.packed.concurrent.ThreadNamespaceConfiguration;
import app.packed.concurrent.ThreadNamespaceMirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionHandle;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceInstaller;
import app.packed.namespace.NamespaceTemplate;

/**
 * A namespace for the thread management in Packed.
 */
public final class JobNamespaceHandle extends NamespaceHandle<BaseExtension, ThreadNamespaceConfiguration> {

    /** The default thread namespace template. */
    public static final NamespaceTemplate<JobNamespaceHandle> TEMPLATE = NamespaceTemplate.of(JobNamespaceHandle.class, JobNamespaceHandle::new);

    /**
     * Create a new handle.
     *
     * @param installer
     *            the namespace installer
     */
    protected JobNamespaceHandle(NamespaceInstaller<?> installer) {
        super(installer);
    }

    /** {@inheritDoc} */
    @Override
    protected ThreadNamespaceConfiguration newNamespaceConfiguration(BaseExtension e, ComponentRealm actor) {
        return new ThreadNamespaceConfiguration(this, e, actor);
    }

    /** {@inheritDoc} */
    @Override
    protected ThreadNamespaceMirror newNamespaceMirror() {
        return new ThreadNamespaceMirror(this);
    }

    public static JobNamespaceHandle mainHandle(ExtensionHandle<BaseExtension> handle) {
        return handle.namespaceLazy(JobNamespaceHandle.TEMPLATE);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
    }
}
