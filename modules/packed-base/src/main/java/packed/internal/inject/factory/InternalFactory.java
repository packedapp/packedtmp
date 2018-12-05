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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import app.packed.inject.Factory;
import app.packed.inject.Key;
import packed.internal.inject.InjectSupport;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.function.InternalFunction;

/**
 *
 */
public final class InternalFactory<T> {

    /** A list of all of this factory's dependencies. */
    public final List<InternalDependency> dependencies;

    /** The function used to create a new instance. */
    public final InternalFunction<T> function;

    /** The key that this factory will be registered under by default with an injector. */
    public final Key<T> key;

    public InternalFactory(InternalFunction<T> function) {
        this(function, List.of());
    }

    public InternalFactory(InternalFunction<T> function, List<InternalDependency> dependencies) {
        this.key = function.typeLiteral.toKey();
        this.dependencies = requireNonNull(dependencies, "dependencies is null");
        this.function = requireNonNull(function);
    }

    public InternalFactory(Key<T> key, List<InternalDependency> dependencies, InternalFunction<T> function) {
        this.key = requireNonNull(key, "key is null");
        this.dependencies = requireNonNull(dependencies, "dependencies is null");
        this.function = requireNonNull(function);
    }

    public InternalFactory<T> withLookup(Lookup lookup) {
        return new InternalFactory<T>(function.withLookup(lookup), dependencies);
    }

    /**
     * Converts the specified factory to an internal factory
     * 
     * @param <T>
     *            the type of elements the factory produces
     * @param factory
     *            the factory convert
     * @return the converted factory
     */
    public static <T> InternalFactory<T> from(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        return InjectSupport.toInternalFactory(factory);
    }
}
