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
import app.packed.cli.CliCommand.Builder;
import app.packed.extension.BeanElement.BeanMethod;
import sandbox.extension.domain.NamespaceOperator;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationTemplate;

/**
 * A CLI domain is a domain where all CLI commands are unique. Typically there is never more than one per application.
 */
class CliExtensionDomain extends NamespaceOperator<CliExtension> {

    /** All the commands within the domain. */
    final LinkedHashMap<String, CliC> commands = new LinkedHashMap<>();

    /**
     * @param names
     */
    public Builder addCommand(String[] names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CliNamespaceMirror mirror() {
        return new CliNamespaceMirror();
    }

    void process(CliExtension extension, CliCommand c, BeanMethod method) {
        OperationHandle h = null;
        if (isInApplicationLifetime(extension)) {
            h = method.newOperation(OperationTemplate.defaults());

            // check Launched
        } else {

            // EntryPoint.LaunchLifetime
        }

        CliC cd = new CliC(this, c, h);
        if (commands.putIfAbsent(c.name()[0], cd) != null) {
            throw new BeanInstallationException("Multiple cli commands with the same name, name = " + c.name());
        }
        h.specializeMirror(() -> new CliCommandMirror(cd));
        // OT.DEFAULTS.entryPoint();
    }
}
