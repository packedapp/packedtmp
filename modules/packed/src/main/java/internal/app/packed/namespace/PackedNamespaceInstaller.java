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

import java.util.function.Function;

import app.packed.extension.Extension;
import app.packed.namespace.NamespaceConfiguration;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceTemplate;
import app.packed.namespace.NamespaceTemplate.Installer;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.namespace.NamespaceSetup.NamespaceKey;

/**
 *
 */
public final class PackedNamespaceInstaller implements NamespaceTemplate.Installer {
    final PackedNamespaceTemplate template;
    final ExtensionSetup root;
    final AuthoritySetup owner;

    public NamespaceSetup namespace;

    public NamespaceHandle<?, ?> handle;
    private final String name;

    public PackedNamespaceInstaller(PackedNamespaceTemplate template, ExtensionSetup root, AuthoritySetup owner, String name) {
        this.template = template;
        this.root = root;
        this.owner = owner;
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public <E extends Extension<E>, H extends NamespaceHandle<E, ?>, C extends NamespaceConfiguration<E>> H install(Function<? super Installer, H> newHandle) {
        this.namespace = new NamespaceSetup(template, root, owner);
        H apply = newHandle.apply(this);
        namespace.handle = apply;
        root.container.application.namespaces.put(new NamespaceKey(template.handleClass(), name), apply);
        handle = apply;
        return apply;
    }

}
