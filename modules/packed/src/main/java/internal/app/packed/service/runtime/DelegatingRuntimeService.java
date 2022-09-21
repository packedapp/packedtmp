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
package internal.app.packed.service.runtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;

/**
 * A delegating runtime service node.
 * <p>
 * This type is used for exported nodes as well as nodes that are imported from other containers.
 */
public record DelegatingRuntimeService(Key<?> key, RuntimeService delegate) implements RuntimeService {

    /**
     * Creates a new runtime alias node.
     *
     * @param delegate
     *            the build time alias node to create a runtime node from
     */
    public DelegatingRuntimeService {
        requireNonNull(key);
        requireNonNull(delegate);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return delegate.dependencyAccessor();
    }

    /** {@inheritDoc} */
    @Override
    public Object provideInstance() {
        return delegate.provideInstance();
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresProvisionContext() {
        return delegate.requiresProvisionContext();
    }

    @Override
    public String toString() {
        return RuntimeService.toString(this);
    }

    @Override
    public boolean isConstant() {
        return delegate.isConstant();
    }
}
