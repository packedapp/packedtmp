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

import app.packed.operation.OperationMirror;

/**
 * A mirror for a CLI command operation.
 */
public class CliCommandMirror extends OperationMirror {

    /** The command being mirrored. */
    final PackedCliCommand command;

    CliCommandMirror(PackedCliCommand command) {
        this.command = command;
    }

    public List<String> names() {
        return List.of(command.command().name());
    }

    /** {@return the namespace this command is part of.} */
    public CliNamespaceMirror namespace() {
        return command.namespace().mirror();
    }
}
