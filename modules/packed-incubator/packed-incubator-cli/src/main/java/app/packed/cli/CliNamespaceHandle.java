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
package app.packed.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.OnVariable;
import app.packed.component.ComponentRealm;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceInstaller;
import app.packed.namespace.NamespaceTemplate;

/**
 * A CLI domain is a domain where all CLI commands are unique. Typically there is never more than one per application.
 */
final class CliNamespaceHandle extends NamespaceHandle<CliExtension, CliNamespaceConfiguration> {

    /** The default namespace template. */
    static final NamespaceTemplate<CliNamespaceHandle> TEMPLATE = NamespaceTemplate.of(CliNamespaceHandle.class, CliNamespaceHandle::new);

    /** All the commands within the namespace. */
    final LinkedHashMap<String, CliCommandOperationHandle> commands = new LinkedHashMap<>();

    final List<CliOptionMirror> options = new ArrayList<>();

    CliNamespaceHandle(NamespaceInstaller<?> installer) {
        super(installer);
    }

    @Override
    protected CliNamespaceConfiguration newNamespaceConfiguration(CliExtension e, ComponentRealm actor) {
        return new CliNamespaceConfiguration(this, e, actor);
    }

    /** {@inheritDoc} */
    @Override
    protected CliNamespaceMirror newNamespaceMirror() {
        return new CliNamespaceMirror(this);
    }

    void process(CliExtension extension, CliCommand c, BeanIntrospector.OnMethod method) {
        CliCommandOperationHandle h = null;
        for (String n : c.name()) {
            if (commands.containsKey(n)) {
                throw new BeanInstallationException("Multiple cli commands with the same name, name = " + c.name());
            }
        }

        // For each name check that it doesn't exists in commands already
        if (isInApplicationLifetime(extension)) {
            h = method.newOperation().install(i -> new CliCommandOperationHandle(i, this));

            // OperationTemplate.
            // h.namespace(this)

            // h.configuration -> CliCommandConfiguration
            // check Launched
        } else {
            // EntryPoint.LaunchLifetime
        }

        // populate from annotation instead
       // h.command = c;

        if (commands.putIfAbsent(c.name()[0], h) != null) {
            throw new BeanInstallationException("Multiple cli commands with the same name, name = " + c.name());
        }
        // OT.DEFAULTS.entryPoint();
    }

    /**
     * @param extension
     * @param c
     * @param onVariable
     */
    void onCliOptionAnnotation(CliExtension extension, CliOption annotation, OnVariable onVariable) {
        throw new UnsupportedOperationException();
    }
}
