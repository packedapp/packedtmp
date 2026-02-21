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

import app.packed.extension.Extension;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.oldnamespace.OldNamespaceSetup;
import internal.app.packed.service.ServiceBindingSetup;

/** A single instance of this class exists per extension per application. */
public final class ExtensionInstanceTree {

    /**
     * The extension id. This id may be used when ordering extensions if there are multiple extensions with the same
     * canonically name and extension depth.
     */
    final int applicationExtensionId;

    /** Whether or not this type of extension is still configurable. */
    boolean isConfigurable = true;

    /** A model of the extension. */
    final ExtensionClassModel model;

    public final String name;

    public final Deque<OldNamespaceSetup> namespacesToClose = new ArrayDeque<>();

    /**
     * Extensions resolver services when the application closes. The main argument is that they should very rarely fail to resolve.
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
    ExtensionInstanceTree(ApplicationSetup application, Class<? extends Extension<?>> extensionType) {
        this.applicationExtensionId = application.extensionIdCounter++;
        this.model = ExtensionClassModel.of(extensionType);
        String name = model.name();
        int suffix = 1;
        while (application.extensions.putIfAbsent(name, extensionType) != null) {
            name = model.name() + suffix++;
        }
        this.name = name;
    }
}