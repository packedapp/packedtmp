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

import java.util.Optional;
import java.util.stream.Stream;

import app.packed.namespace.OverviewMirror;
import app.packed.operation.OperationMirror;

/**
 * Represents a CLI namespace where {@link CliCommandMirror commands} and global {@link CliOption options} are unique.
 */
public final class CliOverviewMirror extends OverviewMirror<CliExtension> {

    /** The CLI namespace handle. */
    private final CliNamespaceHandle handle;

    CliOverviewMirror(CliNamespaceHandle handle) {
        this.handle = handle;
    }

    public Optional<CliCommandMirror> command(String name) {
        return Optional.ofNullable(handle.commands.get(name)).map(h -> (CliCommandMirror) h.mirror());
    }

    /** {@return all commands within the namespace.} */
    // Hmm this includes operations owned by extensions
    public OperationMirror.OfStream<CliCommandMirror> commands() {
        return operations().ofType(CliCommandMirror.class);
    }

    /** {@return all general options within the namespace.} */
    public Stream<CliOptionMirror> options() {
        return handle.options.stream();
    }
}

// Commands
// Operations // Fx, kan vi godt have flere operations

/// Commands filtering on
// Filter on Author
// Filter on Containers
// Filter on Namespaces

// Global Namespace = Global Commands + Global Operations
// Some Command namespace = Operations Applicable only for that command