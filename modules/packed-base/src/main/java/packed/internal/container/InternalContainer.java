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
package packed.internal.container;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import app.packed.container.Component;
import app.packed.container.Container;
import app.packed.inject.Key;
import app.packed.inject.ServiceDescriptor;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.util.ConfigurationSite;
import app.packed.util.Nullable;

/**
 *
 */
public class InternalContainer implements Container {

    /** {@inheritDoc} */
    @Override
    public <T> Optional<T> get(Key<T> key) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Component> getComponent(CharSequence path) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigurationSite getConfigurationSite() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getDescription() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasService(Key<?> key) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T injectMembers(T instance, Lookup lookup) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Component root() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ServiceDescriptor> services() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<Container> shutdown() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<Container> shutdown(Throwable cause) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<Container> start() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleOperations<? extends Container> state() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> tags() {
        return null;
    }

}
