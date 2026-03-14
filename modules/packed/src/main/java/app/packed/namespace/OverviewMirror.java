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
package app.packed.namespace;

import static java.util.Objects.requireNonNull;

import app.packed.extension.Extension;
import app.packed.operation.OperationMirror;
import internal.app.packed.component.PackedOverviewHandle;

/**
 * A mirror that provides a read-only, extension-scoped view of operations within a particular scope such as an
 * application, namespace, or container.
 * <p>
 * Subclasses are defined by extension authors to expose domain-specific queries over the operations that their
 * extension installs. For example, a {@code JobOverviewMirror} might expose methods like {@code daemons()} or
 * {@code jobs()} that filter and return extension-specific operation mirror types.
 * <p>
 * An overview mirror is obtained via {@link app.packed.application.ApplicationMirror#overview(Class)}.
 * <p>
 * Subclasses must declare exactly one constructor taking an {@link OverviewHandle} parameter.
 *
 * @param <E>
 *            the type of extension whose operations this overview provides access to
 *
 * @see OverviewHandle
 * @see app.packed.application.ApplicationMirror#overview(Class)
 */
public abstract class OverviewMirror<E extends Extension<E>> {

    /** The backing handle. */
    final PackedOverviewHandle<E> handle;

    /**
     * Creates a new overview mirror backed by the specified handle.
     *
     * @param overviewHandle
     *            the handle providing access to operations
     */
    protected OverviewMirror(OverviewHandle<E> overviewHandle) {
        this.handle = (PackedOverviewHandle<E>) requireNonNull(overviewHandle);
    }

    /**
     * {@return a stream of all operations within this overview's scope that were installed by extension {@code E}}
     * <p>
     * Only operations on userland beans are included; extension-owned beans are excluded.
     */
    protected final OperationMirror.OfStream<OperationMirror> operations() {
        return handle.operations();
    }

    /**
     * Returns a stream of operations within this overview's scope that were installed by extension {@code E} and match the
     * specified mirror type.
     *
     * @param <T>
     *            the type of operation mirror
     * @param operationType
     *            the class of the operation mirror type to filter by
     * @return a stream of matching operations
     */
    protected final <T extends OperationMirror> OperationMirror.OfStream<T> operations(Class<T> operationType) {
        return handle.operations(operationType);
    }
}
