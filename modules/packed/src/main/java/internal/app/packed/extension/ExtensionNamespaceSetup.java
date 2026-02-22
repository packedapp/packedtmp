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
package internal.app.packed.extension;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import app.packed.component.ComponentRealm;
import app.packed.extension.Extension;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.namespace.NamespaceSetup;
import internal.app.packed.oldnamespace.OldNamespaceSetup;
import internal.app.packed.service.ServiceBindingSetup;

/** A single instance of this class exists per extension per application. */
public final class ExtensionNamespaceSetup extends NamespaceSetup {

    /** Whether or not this type of extension is still configurable. */
    boolean isConfigurable = true;

    /** A model of the extension. */
    final ExtensionClassModel model;

    public final String extensionName;

    public final Deque<OldNamespaceSetup> namespacesToClose = new ArrayDeque<>();

    /**
     * Extensions resolver services when the application closes. The main argument is that they should very rarely fail to
     * resolve.
     */
    public ArrayList<ServiceBindingSetup> servicesToResolve = new ArrayList<>();

    /**
     * Creates a new ExtensionTree.
     *
     * @param container
     *            the root container
     * @param extensionType
     *            the type of extension
     */
    ExtensionNamespaceSetup(ApplicationSetup application, Class<? extends Extension<?>> extensionType) {
        super(application.rootContainer(), extensionType);
        this.model = ExtensionClassModel.of(extensionType);

        String name = model.name();

        int suffix = 1;
        while (application.extensionNames.putIfAbsent(name, extensionType) != null) {
            name = model.name() + suffix++;
        }
        this.extensionName = name;
        // TODO should be able to reuse the AbstractNode tree thingy here, instead of a seperate field
        // TODO insert into tree propertly
        super.name = "$" + name;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentRealm owner() {
        return model.realm();
    }
}