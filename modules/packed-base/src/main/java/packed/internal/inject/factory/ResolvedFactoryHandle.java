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
final class ResolvedFactoryHandle<T> extends FactoryHandle<T> {

    private final FactoryHandle<T> delegate;

    MethodHandle methodHandle;

    ResolvedFactoryHandle(FactoryHandle<T> delegate, MethodHandle handle) {
        super(delegate.typeLiteral);
        this.delegate = delegate;
        this.methodHandle = requireNonNull(handle);
    }

    /** {@inheritDoc} */
    @Override
    public List<DependencyDescriptor> dependencies() {
        return delegate.dependencies();
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle(Lookup ignore) {
        return methodHandle;
    }
}
