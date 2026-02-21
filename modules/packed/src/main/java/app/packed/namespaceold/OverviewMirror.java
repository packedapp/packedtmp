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
package app.packed.namespaceold;

import app.packed.extension.Extension;
import app.packed.operation.OperationMirror;

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

// ApplicationMirrror.overview
// NamespaceMirrror.overview
// ?ContainerMirror.overview
public abstract class OverviewMirror<E extends Extension<E>> {
    // ComponentPath componentPath();

    // Public???? In that case, we probably need both
    protected final OperationMirror.OfStream<OperationMirror> operations() {
        throw new UnsupportedOperationException();
    }
}
