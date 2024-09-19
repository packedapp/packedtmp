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
package app.packed.cli;

import java.util.LinkedHashMap;

import app.packed.bean.BeanInstallationException;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceTemplate;
import app.packed.operation.OperationTemplate;

/**
 * A CLI domain is a domain where all CLI commands are unique. Typically there is never more than one per application.
 */
final class CliNamespaceHandle extends NamespaceHandle<CliExtension, CliNamespaceConfiguration> {

    /** The default namespace template. */
    static final NamespaceTemplate TEMPLATE = NamespaceTemplate.of(CliNamespaceHandle.class, c -> {});

    /** All the commands within the namespace. */
    final LinkedHashMap<String, CliCommandHandle> oldCommands = new LinkedHashMap<>();

    CliNamespaceHandle(NamespaceTemplate.Installer installer) {
        super(installer);
    }

    /** {@inheritDoc} */
    @Override
    protected CliNamespaceMirror newNamespaceMirror() {
        return new CliNamespaceMirror(this);
    }

    @Override
    protected CliNamespaceConfiguration newNamespaceConfiguration(CliExtension e) {
        return new CliNamespaceConfiguration(this, e);
    }

    void process(CliExtension extension, CliCommand c, BeanMethod method) {
        CliCommandHandle h = null;
        // For each name check that it doesn't exists in commands already
        if (isInApplicationLifetime(extension)) {
            h = method.newOperation(OperationTemplate.defaults()).install(i -> new CliCommandHandle(i, this));

//            h.specializeMirror(() -> new CliCommandMirror(h, this));

            // OperationTemplate.
            // h.namespace(this)

            // h.configuration -> CliCommandConfiguration
            // check Launched
        } else {
            // EntryPoint.LaunchLifetime
        }

        // I think all this is stored in CliCommandConfiguration
        h.command = c;
        if (oldCommands.putIfAbsent(c.name()[0], h) != null) {
            throw new BeanInstallationException("Multiple cli commands with the same name, name = " + c.name());
        }
        // OT.DEFAULTS.entryPoint();
    }
}
