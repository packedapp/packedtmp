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

import java.lang.reflect.Member;
import java.util.Optional;
import java.util.OptionalInt;

import app.packed.container.Component;
import app.packed.inject.Dependency;
import app.packed.inject.Injector;
import app.packed.inject.ProvidesHelper;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.VariableDescriptor;

/**
 * An implementation of injection site used, when requesting a service directly through an injector, for example, via
 * {@link Injector#use(Class)}.
 */
public class InjectionSiteForDependency implements ProvidesHelper {

    /** An optional component, in case the request is via a component's private injector. */
    @Nullable
    private final Component component;

    /** The key of the service that was requested */
    private final Dependency dependency;

    /** The injector from where the service was requested. */
    private final Injector injector;

    public InjectionSiteForDependency(Injector injector, Dependency dependency, @Nullable Component component) {
        this.injector = requireNonNull(injector, "injector is null");
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
    public OptionalInt parameterIndex() {
        return dependency.parameterIndex();
    }

    /** {@inheritDoc} */
    @Override
    public Injector injector() {
        return injector;
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return dependency.key();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Member> member() {
        return dependency.member();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<VariableDescriptor> variable() {
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
}
