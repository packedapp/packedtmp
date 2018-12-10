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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import app.packed.container.Component;
import app.packed.container.ComponentPath;
import app.packed.container.Container;
import app.packed.inject.Injector;
import app.packed.inject.ServiceDescriptor;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.util.ConfigurationSite;
import app.packed.util.Key;
import app.packed.util.Nullable;

/**
 *
 */
public class InternalContainer implements Container {

    private final Injector injector;

    /** The root component of the container. */
    private final InternalComponent root;

    InternalContainer(ContainerBuilder builder, Injector injector) {
        this.injector = requireNonNull(injector);

        builder.root.forEachRecursively(componentConfiguration -> componentConfiguration.init(this));

        this.root = requireNonNull(builder.root.component);
    }

    @Override
    public <T> Optional<T> get(Class<T> key) {
        return injector.get(key);
    }

    @Override
    public <T> Optional<T> get(Key<T> key) {
        return injector.get(key);
    }

    @Override
    public ConfigurationSite getConfigurationSite() {
        return injector.getConfigurationSite();
    }

    @Override
    public @Nullable String getDescription() {
        return injector.getDescription();
    }

    @Override
    public <T> @Nullable ServiceDescriptor getService(Class<T> serviceType) {
        return injector.getService(serviceType);
    }

    @Override
    public <T> @Nullable ServiceDescriptor getService(Key<T> key) {
        return injector.getService(key);
    }

    @Override
    public boolean hasService(Class<?> key) {
        return injector.hasService(key);
    }

    @Override
    public boolean hasService(Key<?> key) {
        return injector.hasService(key);
    }

    @Override
    public <T> T injectMembers(T instance, Lookup lookup) {
        return injector.injectMembers(instance, lookup);
    }

    @Override
    public void print() {
        injector.print();
    }

    @Override
    public Stream<ServiceDescriptor> services() {
        return injector.services();
    }

    @Override
    public Set<String> tags() {
        return injector.tags();
    }

    @Override
    public <T> T with(Class<T> key) {
        return injector.with(key);
    }

    @Override
    public <T> T with(Key<T> key) {
        return injector.with(key);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Component> getComponent(CharSequence path) {
        return path == ComponentPath.ROOT || path.toString().equals("/") ? Optional.of(root) : Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        // Det eneste jeg ved er at man godt kan have 2 containere med det samme navn som sieblings.
        // installContainer(Jetty.class); //Maaske man kan bestemme root component navnet???
        // installContainer(Jetty.class);//Maaske man kan bestemme root component navnet???
        // Hmmmmmmmmmmm
        return "Unknown";// name of root component????, no name //JettyWebserver.
    }

    /** {@inheritDoc} */
    @Override
    public Component root() {
        return root;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<Container> shutdown() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<Container> shutdown(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<Container> start() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleOperations<? extends Container> state() {
        throw new UnsupportedOperationException();
    }
}
