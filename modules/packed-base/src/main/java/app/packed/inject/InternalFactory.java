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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.lifecycle.OnStart;
import app.packed.util.Key;
import packed.internal.inject.InternalDependency;
import packed.internal.invokable.InternalFunction;

/** An internal factory. */
final class InternalFactory<T> {

    /** A list of all of this factory's dependencies. */
    final List<InternalDependency> dependencies;

    /** The function used to create a new instance. */
    final InternalFunction<T> function;

    /** The key that this factory will be registered under by default with an injector. */
    final Key<T> defaultKey;

    InternalFactory(InternalFunction<T> function, List<InternalDependency> dependencies) {
        this.dependencies = requireNonNull(dependencies, "dependencies is null");
        this.function = requireNonNull(function);
        this.defaultKey = function.typeLiteral.toKey();
    }

    /**
     * Returns the scannable type of this factory. This is the type that will be used for scanning for annotations such as
     * {@link OnStart} and {@link Provides}.
     *
     * @return the scannable type of this factory
     */
    Class<? super T> getScannableType() {
        return function.getReturnTypeRaw();
    }
}
