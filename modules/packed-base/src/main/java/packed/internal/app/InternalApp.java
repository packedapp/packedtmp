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
package packed.internal.app;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import app.packed.app.App;
import app.packed.config.ConfigSite;
import app.packed.container.Container;
import app.packed.inject.ServiceDescriptor;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.util.Key;

/**
 *
 */
public final class InternalApp implements App {

    final Container container;

    public InternalApp(Container container) {
        this.container = requireNonNull(container);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configurationSite() {
        return container.configurationSite();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return container.description();
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<T> get(Key<T> key) {
        return container.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasService(Key<?> key) {
        return container.hasService(key);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T injectMembers(T instance, Lookup lookup) {
        return container.injectMembers(instance, lookup);
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return container.name();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ServiceDescriptor> services() {
        return container.services();
    }

    /** {@inheritDoc} */
    @Override
    public App shutdown() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public App shutdown(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> shutdownAsync() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> shutdownAsync(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public App start() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> startAsync() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleOperations<? extends App> state() {
        throw new UnsupportedOperationException();
    }
    //
    // /** {@inheritDoc} */
    // @Override
    // public Set<String> tags() {
    // return container.tags();
    // }
}
