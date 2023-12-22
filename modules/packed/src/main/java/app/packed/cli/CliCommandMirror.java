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

import java.util.List;

import app.packed.namespace.sandbox.NamespaceOperationMirror;

/**
 * A mirror for a CLI command operation.
 */
public class CliCommandMirror extends NamespaceOperationMirror {

    final CliExtensionNamespaceOperator namespace;

    final CliCommand command;

    CliCommandMirror(CliExtensionNamespaceOperator namespace, CliCommand command) {
        this.command = command;
        this.namespace = namespace;
    }

    public List<String> names() {
        return List.of(command.name());
    }

    /** {@return the namespace this command is part of.} */
    @Override
    public CliNamespaceMirror namespace() {
        return namespace.mirror();
    }
}
