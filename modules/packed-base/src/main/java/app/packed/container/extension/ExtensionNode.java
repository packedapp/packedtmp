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

import app.packed.container.BundleDescriptor;
import packed.internal.container.extension.PackedExtensionContext;

/**
 * Extension nodes enables communication across extension instances of the same. It also enables using wirelets if X
 * interface is implemented. Finally it allows host to guest communication.
 */
public abstract class ExtensionNode<T extends Extension> {

    // Or initialized, by the runtime....
    private final PackedExtensionContext context;

    /**
     * Creates a new extension node.
     * 
     * @param context
     *            the extension node provided by the Packed runtime via {@link Extension#context()}.
     */
    protected ExtensionNode(ExtensionContext context) {

        this.context = (PackedExtensionContext) requireNonNull(context, "context is null");
    }

    public void buildDescriptor(BundleDescriptor.Builder builder) {}

    public final ExtensionContext context() {
        return context;
    }

    @SuppressWarnings("unchecked")
    public final T extension() {
        return (T) context.extension();
    }
}
