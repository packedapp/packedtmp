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
 *
 */
// I don't think there is going to be any navigation, other than from

// OverviewMirror.of(ApplicationMirror, ComponentPath, boolean includeExtensions)
// ExtensionMirror.overview(SomeOverviewMirror.class)

// ApplicationMirror.allOverview(xxx) <--

// Options, includeExtensions?
//// 99% of the time you are not interested in extensions Who cares about the extensions services, nevertheless,
/// extension will find it interesting to find them in some way ServiceOverview Also
public abstract class OverviewMirror<E extends Extension<E>> {
    // ComponentPath componentPath();
    final PackedOverviewHandle<E> handle;

    protected OverviewMirror(OverviewHandle<E> overviewHandle) {
        this.handle = (PackedOverviewHandle<E>) requireNonNull(overviewHandle);
    }

    protected final OperationMirror.OfStream<OperationMirror> operations() {
        return handle.operations();
    }

    protected final <T extends OperationMirror> OperationMirror.OfStream<T> operations(Class<T> operations) {
        return handle.operations(operations);
    }
}
