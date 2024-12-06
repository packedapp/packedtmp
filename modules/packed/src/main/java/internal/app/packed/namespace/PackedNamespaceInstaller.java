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
package internal.app.packed.namespace;

import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceInstaller;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.component.AbstractComponentInstaller;
import internal.app.packed.extension.ExtensionSetup;

/** Implementation of {@link NamespaceTemplate.Installer} */
public final class PackedNamespaceInstaller<H extends NamespaceHandle<?, ?>> extends AbstractComponentInstaller<NamespaceSetup, PackedNamespaceInstaller<H>>
        implements NamespaceInstaller<H> {

    public NamespaceHandle<?, ?> handle;
    final String name;

    final AuthoritySetup<?> owner;

    final ExtensionSetup root;

    /** The template for the new namespace. */
    final PackedNamespaceTemplate<?> template;

    public PackedNamespaceInstaller(PackedNamespaceTemplate<?> template, ExtensionSetup root, AuthoritySetup<?> owner, String name) {
        this.template = template;
        this.root = root;
        this.owner = owner;
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationSetup application(NamespaceSetup component) {
        return component.root.container.application;
    }

    /** {@inheritDoc} */
    @Override
    public H install() {
        return NamespaceSetup.newNamespace(this);
    }

}
