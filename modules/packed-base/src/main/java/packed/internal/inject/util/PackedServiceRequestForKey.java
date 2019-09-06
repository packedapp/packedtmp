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
package packed.internal.inject.util;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Member;
import java.util.Optional;

import app.packed.component.Component;
import app.packed.inject.Injector;
import app.packed.inject.ServiceRequest;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.VariableDescriptor;

/**
 * An implementation of injection site used, when requesting a service directly through an injector, for example, via
 * {@link Injector#use(Class)}.
 */
public class PackedServiceRequestForKey implements ServiceRequest {

    /** An optional component, in case the request is via a component's private injector. */
    @Nullable
    private final Component component;

    /** The injector from where the service was requested. */
    final Injector injector;

    /** The key of the service that was requested */
    private final Key<?> key;

    public PackedServiceRequestForKey(Injector injector, Key<?> key, @Nullable Component component) {
        this.injector = requireNonNull(injector, "injector is null");
        this.key = requireNonNull(key, "key is null");
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
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Member> member() {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<VariableDescriptor> variable() {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOptional() {
        return false;
    }
}
