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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.reflect.MemberDescriptor;
import app.packed.base.reflect.VariableDescriptor;
import app.packed.component.Component;
import app.packed.service.Injector;
import app.packed.service.ProvideContext;

/**
 * An implementation of injection site used, when requesting a service directly through an injector, for example, via
 * {@link Injector#use(Class)}.
 */
public final class ProvideContextImpl implements ProvideContext {

    /** An optional component, in case the request is via a component's private injector. */
    @Nullable
    private final Component component;

    /** The key of the service that was requested */
    private final ServiceDependency dependency;

    public ProvideContextImpl(ServiceDependency dependency, @Nullable Component component) {
        this.dependency = requireNonNull(dependency, "dependency is null");
        this.component = component;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Component> component() {
        return Optional.ofNullable(component);
    }

    /** {@inheritDoc} */
    @Override
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

    // public static void main(String[] args) {
    // new Factory1<InjectionSite, Logger>(
    // site -> site.getComponent().isPresent() ? Logger.getLogger(site.getComponent().get().getPath().toString()) :
    // Logger.getAnonymousLogger()) {};
    // }
    //
    // @Provides
    // public static Logger provideLogger(InjectionSite site) {
    // if (site.getComponent().isPresent()) {
    // return Logger.getLogger(site.getComponent().get().getPath().toString());
    // } else {
    // return Logger.getAnonymousLogger();
    // }
    // }

    static ProvideContext of(ServiceDependency dependency) {
        return new ProvideContextImpl(dependency, null);
    }

    static ProvideContext of(ServiceDependency dependency, Component componenent) {
        return new ProvideContextImpl(dependency, requireNonNull(componenent, "component is null"));
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
        return new ProvideContextImpl(ServiceDependency.of(key), null);
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
        return new ProvideContextImpl(ServiceDependency.of(key), requireNonNull(component, "component is null"));
    }
    // static {AopReady r = AOPSupport.compile(FooClass.class)}, at runtime r.newInstance(r))// Arghh grimt
}
