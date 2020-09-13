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
package packed.internal.inject.context;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.inject.ProvideContext;
import app.packed.introspection.MemberDescriptor;
import app.packed.introspection.VariableDescriptor;
import app.packed.service.Injector;
import packed.internal.inject.ServiceDependency;

/**
 * An implementation of injection site used, when requesting a service directly through an injector, for example, via
 * {@link Injector#use(Class)}.
 */
public final class PackedPrototypeProvideContext implements ProvideContext {

    /** An optional component, in case the request is via a component's private injector. */
    @Nullable
    private final Component component;

    /** The key of the service that was requested */
    private final ServiceDependency dependency;

    public PackedPrototypeProvideContext(ServiceDependency dependency, @Nullable Component component) {
        this.dependency = requireNonNull(dependency, "dependency is null");
        this.component = component;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Component> component() {
        return Optional.ofNullable(component);
    }

    public Key<?> key() {
        return dependency.key();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<MemberDescriptor> originMember() {
        return dependency.member();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<VariableDescriptor> originVariable() {
        return dependency.variable();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOptional() {
        return dependency.isOptional();
    }

    static ProvideContext of(ServiceDependency dependency) {
        return new PackedPrototypeProvideContext(dependency, null);
    }

    static ProvideContext of(ServiceDependency dependency, Component componenent) {
        return new PackedPrototypeProvideContext(dependency, requireNonNull(componenent, "component is null"));
    }

    /**
     * Returns a new injection site for the specified injector and key.
     * <p>
     * This method is used to create injection site for methods such as {@link Injector#use(Key)}.
     * 
     * @param key
     *            the for which injection is requested
     * @return an injection site for the specified injector and key.
     */
    public static ProvideContext of(Key<?> key) {
        return new PackedPrototypeProvideContext(ServiceDependency.of(key), null);
    }

    /**
     * Returns a new injection site for the specified injector, key and component.
     * 
     * @param key
     *            the for which injection is requested
     * @param component
     *            the component to which the injector belongs
     * @return an injection site for the specified injector and key and component.
     */
    static ProvideContext of(Key<?> key, Component component) {
        return new PackedPrototypeProvideContext(ServiceDependency.of(key), requireNonNull(component, "component is null"));
    }
}
// static {AopReady r = AOPSupport.compile(FooClass.class)}, at runtime r.newInstance(r))// Arghh grimt
