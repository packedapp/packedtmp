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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import packed.internal.inject.dependency.DependencyDescriptor;

/**
 *
 */
// Tror vi smider en instance paa Field/Executable...
public class BoundInstanceFactoryHandle<T> extends FactoryHandle<T> {
    private final FactoryHandle<T> delegate;

    final Object instance;

    BoundInstanceFactoryHandle(FactoryHandle<T> delegate, Object instance) {
        super(delegate.typeLiteral);
        this.delegate = delegate;
        this.instance = requireNonNull(instance);
    }

    /** {@inheritDoc} */
    @Override
    public List<DependencyDescriptor> dependencies() {
        return delegate.dependencies();
    }

    @Override
    public FactoryHandle<T> withLookup(Lookup lookup) {
        return new BoundInstanceFactoryHandle<>(delegate.withLookup(lookup), instance);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle(Lookup lookup) {
        MethodHandle mh = delegate.toMethodHandle(lookup);
        return mh.bindTo(instance); // Type has already been validated
    }
}
