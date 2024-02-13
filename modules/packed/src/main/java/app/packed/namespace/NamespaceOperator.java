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

import app.packed.extension.Extension;
import app.packed.util.TreeView.Node;
import internal.app.packed.container.NamespaceSetup;

/**
 *
 * Extensions should never expose instances of this class to non-trusted code.
 */
// Ideen bag den her er jo at det er lettest at have en klasse man kan overskrive.
// Hvor man kan gemme info direkte paa operatoren... istedet for at have en masse pinde

// Tror ikke vi kommer uden om, at have nogle callback metoder ogsaa

// onClose

public abstract class NamespaceOperator<E extends Extension<E>> {

    protected NamespaceHandle handle;

    /** The domain configuration. */
    private final NamespaceSetup namespace = NamespaceSetup.MI.initialize();

    protected abstract <N extends NamespaceConfiguration<E>> N configuration();

    public final boolean isInApplicationLifetime(Extension<?> extension) {
        return true;
    }

    public NamespaceMirror<E> mirror() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a navigator for all extensions in the namespace.
     *
     * @return a navigator for all extensions in the namespace
     */
    // Ogsaa containere hvor den ikke noedvendig er brugt890[]\

    public Node<E> navigator() {
        throw new UnsupportedOperationException();
    }

    protected void onAssemblyClose(E rootExtension, boolean isNamespaceRoot) {}

    /** {@return the root extension of this domain.} */
    @SuppressWarnings("unchecked")
    public final E rootExtension() {
        return (E) namespace.root.instance();
    }
}
