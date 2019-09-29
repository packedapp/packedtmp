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
package app.packed.container.extension;

import static java.util.Objects.requireNonNull;

/**
 * Extension nodes enables communication across extension instances of the same. It also enables using wirelets if X
 * interface is implemented. Finally it allows host to guest communication.
 */
// Is the node dead....
// After we got groups via functional interface.....
// I Think so, but maybe as a new
public abstract class OldExtensionNode<T extends Extension> {

    /** The extension the node is a part of */
    private final T extension;

    /**
     * Creates a new extension node.
     * 
     * @param extension
     *            the extension this node is a part of
     */
    protected OldExtensionNode(T extension) {
        this.extension = requireNonNull(extension, "extension is null");
    }

    /**
     * Returns the extension context.
     * 
     * @return the extension context
     * @throws IllegalStateException
     *             if called from within the constructor of the extension.
     */
    // Would like to remove all this code on the node, but nice that we can get
    // the context...
    public final ExtensionContext context() {
        return extension.context();
    }

    /**
     * Returns the extension the node is a part of.
     * 
     * @return the extension the node is a part of
     */
    public final T extension() {
        return extension;
    }
}
