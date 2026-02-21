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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.namespace.NamespaceMirror;
import internal.app.packed.namespace.NamespaceSetup;

/**
 * Access helper for NamespaceMirror.
 */
public abstract class NamespaceMirrorAccessHandler extends AccessHelper {

    private static final Supplier<NamespaceMirrorAccessHandler> CONSTANT = StableValue.supplier(() -> init(NamespaceMirrorAccessHandler.class, NamespaceMirror.class));

    public static NamespaceMirrorAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Creates a new NamespaceMirror from a NamespaceSetup.
     *
     * @param namespace the namespace setup
     * @return the namespace mirror
     */
    public abstract NamespaceMirror newNamespaceMirror(NamespaceSetup namespace);
}
