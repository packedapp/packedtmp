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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Member;
import java.util.Optional;

import app.packed.component.Component;
import app.packed.lang.Key;
import app.packed.lang.Nullable;
import app.packed.lang.reflect.VarDescriptor;

/**
 * An implementation of injection site used, when requesting a service directly through an injector, for example, via
 * {@link Injector#use(Class)}.
 */
final class PrototypeRequestImpl implements PrototypeRequest {

    /** An optional component, in case the request is via a component's private injector. */
    @Nullable
    private final Component component;

    /** The key of the service that was requested */
    private final Dependency dependency;

    PrototypeRequestImpl(Dependency dependency, @Nullable Component component) {
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
    public int parameterIndex() {
        return dependency.parameterIndex();
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
    public Optional<VarDescriptor> variable() {
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
