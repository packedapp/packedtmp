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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.component.Component;
import app.packed.config.ConfigSite;
import app.packed.container.Container;
import app.packed.inject.Injector;
import app.packed.inject.ServiceDescriptor;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.util.Key;

/**
 *
 */
public class InternalContainer implements Container {

    private final Injector injector;

    /** The name of the container. */
    private final String name;

    final Map<String, InternalComponent> components;

    /** All child containers of this container. */
    // Kan vi have en child container og en component med samme navn???
    // Det var ikke muligt foer fordi vi havde en root component

    // Hmmmmmmmmm. Et problem, er at man

    final Map<String, InternalComponent> containers = Map.of();

    public InternalContainer(DefaultContainerConfiguration builder, Injector injector) {
        this.injector = requireNonNull(injector);
        // if (builder.root != null) {
        // builder.root.forEachRecursively(componentConfiguration -> componentConfiguration.init(this));
        // this.root = requireNonNull(builder.root.component);
        // } else {
        // this.root = null;
        // }
        if (builder.components.isEmpty()) {
            components = Map.of();
        } else {
            components = Map.of();
        }
        this.name = builder.getName() == null ? "App" : builder.getName();
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
    public ConfigSite configurationSite() {
        return injector.configurationSite();
    }

    @Override
    public Optional<String> description() {
        return injector.description();
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

    @Override
    public Stream<ServiceDescriptor> services() {
        return injector.services();
    }
    //
    // @Override
    // public Set<String> tags() {
    // return injector.tags();
    // }

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
    public Optional<Component> getComponent(CharSequence path) {
        throw new UnsupportedOperationException();
        // return path == ComponentPath.ROOT || path.toString().equals("/") ? Optional.of(root) : Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
        // Det eneste jeg ved er at man godt kan have 2 containere med det samme navn som sieblings.
        // installContainer(Jetty.class); //Maaske man kan bestemme root component navnet???
        // installContainer(Jetty.class);//Maaske man kan bestemme root component navnet???
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleOperations<? extends Container> state() {
        throw new UnsupportedOperationException();
    }
}
