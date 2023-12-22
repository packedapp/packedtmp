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
package app.packed.namespace;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentConfiguration;
import app.packed.extension.Extension;

/**
 * The configuration of a namespace.
 * <p>
 * Namespace configurations are unique per container|name configuration.
 *
 *
 */

// Hvad hvis en extension vil configure et namespace....... ARGHHHHH
// Skal vi have en Author med???

// This must be towards the user? Yes the template (maybe coupled with a NamespaceHandle.Builder) is for extensions

// But still what exactly are we configuring here???
// We cannot add beans... These must always be added on the extension.
// Well the c

public abstract class NamespaceConfiguration<E extends Extension<E>> extends ComponentConfiguration {

    private final NamespaceHandle handle;

    protected NamespaceConfiguration(NamespaceHandle handle) {
        this.handle = requireNonNull(handle);
    }

    /** {@return the extension instance for which this configuration has been created.} */
    protected final E extension() {
        throw new UnsupportedOperationException();
    }

    public String name() {
        return handle.name();
    }

    public NamespaceConfiguration<E> named(String name) {
        handle.named(name);
        return this;
    }
}
