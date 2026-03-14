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
package app.packed.component;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.operation.OperationMirror;
import internal.app.packed.component.PackedOverviewHandle;

/**
 * A handle that provides access to operations within a particular scope (such as an application or namespace) for a
 * specific extension type. Overview handles are the backing implementation for {@link OverviewMirror} subclasses.
 * <p>
 * This interface is sealed and cannot be implemented directly by user code.
 *
 * @param <E>
 *            the type of extension whose operations are accessible through this handle
 *
 * @see OverviewMirror
 */
public sealed interface OverviewHandle<E extends Extension<E>> permits PackedOverviewHandle {

    E applicationRoot();

    ExtensionHandle<E> applicationRootHandle();

    /**
     * {@return a stream of all operations within the scope of this handle that were installed by the extension type {@code E}}
     */
    OperationMirror.OfStream<OperationMirror> operations();

    /**
     * Returns a stream of all operations within the scope of this handle that were installed by the extension type
     * {@code E} and are of the specified mirror type.
     *
     * @param <T>
     *            the type of operation mirror
     * @param operationType
     *            the class of the operation mirror type to filter by
     * @return a stream of matching operations
     */
    <T extends OperationMirror> OperationMirror.OfStream<T> operations(Class<T> operationType);

    Type type();

    enum Type {
        APPLICATION, NAMESPACE, EXTENSION;
    }
}
