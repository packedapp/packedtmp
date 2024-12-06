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
package app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Set;

import app.packed.binding.Key;
import app.packed.component.ComponentConfiguration;
import app.packed.extension.Extension;

/** The configuration of an operation. */
public non-sealed class OperationConfiguration extends ComponentConfiguration {

    /** The operation handle. */
    private final OperationHandle<?> handle;

    /**
     * Create a new operation configuration using the specified handle.
     *
     * @param handle
     *            the operation handle
     */
    public OperationConfiguration(OperationHandle<?> handle) {
        this.handle = requireNonNull(handle);
    }

    public <K> OperationConfiguration bindServiceInstance(Class<K> key, K instance) {
        return bindServiceInstance(Key.of(key), instance);
    }

    /**
     * Binds a (bean) service from the specified key to the specified instance.
     *
     * @param <K>
     *            type of the instance
     * @param key
     *            the key of the service
     * @param instance
     *            the instance to bind the service to
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the bean does not have binding that are resolved as a service with the specified key. What if it wants
     *             to use at runtime using service locator?
     */
    public <K> OperationConfiguration bindServiceInstance(Key<K> key, K instance) {
        checkIsConfigurable();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public OperationConfiguration tag(String... tags) {
        checkIsConfigurable();
        handle.componentTag(tags);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<String> tags() {
        return handle.componentTags();
    }

    /** {@inheritDoc} */
    @Override
    protected final OperationHandle<?> handle() {
        return handle;
    }

    /** {@return the extension that operates the operation} */
    public final Class<? extends Extension<?>> installerByExtension() {
        return handle.installedByExtension();
    }

    public OperationConfiguration named(String name) {
        checkIsConfigurable();
        handle.named(name);
        return this;
    }

    // peekBinding(int index, Consumer<?>)
    //replaceBindingWithServiceInstance??
    public void replaceBinding(int index, Object value) {
        throw new UnsupportedOperationException();
    }

    OperationConfiguration runBefore(Runnable runnable) {
        return this;
    }

    // All operations defined by the same extension
    protected final Collection<OperationConfiguration> sieblings() {
        throw new UnsupportedOperationException();
    }

    /** {@return the target of the operation.} */
    public final OperationTarget target() {
        return handle.target();
    }

    /** {@return the type of the operation} */
    public final OperationType type() {
        return handle.type();
    }
}
