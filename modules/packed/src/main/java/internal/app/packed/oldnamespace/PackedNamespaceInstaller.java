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
package internal.app.packed.oldnamespace;

import app.packed.namespaceold.OldNamespaceHandle;
import app.packed.namespaceold.NamespaceInstaller;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.component.AbstractComponentInstaller;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.oldnamespace.OldNamespaceSetup.NamespaceKey;

/** Implementation of {@link NamespaceTemplate.Installer} */
public final class PackedNamespaceInstaller<H extends OldNamespaceHandle<?, ?>> extends AbstractComponentInstaller<OldNamespaceSetup, PackedNamespaceInstaller<H>>
        implements NamespaceInstaller<H> {

    public OldNamespaceHandle<?, ?> handle;
    final NamespaceKey nk;

    final AuthoritySetup<?> owner;

    final ExtensionSetup root;

    /** The template for the new namespace. */
    final PackedNamespaceTemplate<?> template;

    public PackedNamespaceInstaller(PackedNamespaceTemplate<?> template, ExtensionSetup root, AuthoritySetup<?> owner, NamespaceKey nk) {
        this.template = template;
        this.root = root;
        this.owner = owner;
        this.nk = nk;
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationSetup application(OldNamespaceSetup component) {
        return component.root.container.application;
    }

    /** {@inheritDoc} */
    @Override
    public H install() {
        return OldNamespaceSetup.newNamespace(this);
    }

}
