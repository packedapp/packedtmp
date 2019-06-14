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
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import app.packed.container.Container;
import app.packed.inject.Injector;
import app.packed.inject.ServiceDescriptor;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.util.Key;

/** The default implementation of Container. */
public final class InternalContainer implements Container {

    /** All the components of this container. */
    final Map<String, InternalComponent> components;

    /** All child containers of this container. */
    final Map<String, InternalComponent> containers = Map.of();

    private final Injector injector;

    /** The name of the container. */
    private final String name;

    public InternalContainer(DefaultContainerConfiguration configuration, Injector injector) {
        this.injector = requireNonNull(injector);
        // if (builder.root != null) {
        // builder.root.forEachRecursively(componentConfiguration -> componentConfiguration.init(this));
        // this.root = requireNonNull(builder.root.component);
        // } else {
        // this.root = null;
        // }
        if (configuration.children.isEmpty()) {
            components = Map.of();
        } else {
            components = Map.of();
        }
        this.name = configuration.getName();
        // this.name = builder.getName() == null ? "App" : builder.getName();
    }

    @Override
    public ConfigSite configurationSite() {
        return injector.configurationSite();
    }

    @Override
    public Optional<String> description() {
        return injector.description();
    }

    @Override
    public <T> Optional<T> get(Class<T> key) {
        return injector.get(key);
    }

    @Override
    public <T> Optional<T> get(Key<T> key) {
        return injector.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Component> getComponent(CharSequence path) {
        requireNonNull(path, "path is null");
        if (path.length() == 0) {
            throw new IllegalArgumentException("Cannot specify the empty string");
        }
        // Make sure we never get parent components....
        if (path.charAt(0) == '/') {

        } else {

        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<ServiceDescriptor> getDescriptor(Class<T> serviceType) {
        return injector.getDescriptor(serviceType);
    }

    @Override
    public <T> Optional<ServiceDescriptor> getDescriptor(Key<T> key) {
        return injector.getDescriptor(key);
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

    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
    }

    @Override
    public Stream<ServiceDescriptor> services() {
        return injector.services();
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleOperations<? extends Container> state() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T use(Class<T> key) {
        return injector.use(key);
    }

    @Override
    public <T> T use(Key<T> key) {
        return injector.use(key);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream components() {
        return new InternalComponentStream(Stream.of(new ComponentWrapper()));
    }

    class ComponentWrapper implements Component {

        /** {@inheritDoc} */
        @Override
        public Collection<Component> children() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configurationSite() {
            return InternalContainer.this.configurationSite();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> description() {
            return InternalContainer.this.description();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return InternalContainer.this.name();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPath path() {
            return ComponentPath.ROOT;
        }

        /** {@inheritDoc} */
        @Override
        public ComponentStream stream() {
            return InternalContainer.this.components();
        }
    }
}
