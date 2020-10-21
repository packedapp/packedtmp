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

import app.packed.base.AnnotatedVariable;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.container.Extension;
import app.packed.inject.ProvisionContext;
import app.packed.inject.sandbox.Injector;

/** Implementation of {@link ProvisionContext}. */
public final class PackedProvisionContext implements ProvisionContext {

    /** An optional component, in case the request is via a component's private injector. */
    @Nullable
    private final Component component;

    /** The key of the service that was requested */
    private final DependencyDescriptor dependency;

    public PackedProvisionContext(DependencyDescriptor dependency, @Nullable Component component) {
        this.dependency = requireNonNull(dependency, "dependency is null");
        this.component = component;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Component> targetComponent() {
        return Optional.ofNullable(component);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<? extends Extension>> targetExtension() {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInjection() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLookup() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOptional() {
        return dependency.isOptional();
    }

    public Key<?> key() {
        return dependency.key();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Member> targetMember() {
        return dependency.member();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<AnnotatedVariable> targetVariable() {
        return dependency.variable();
    }

    static ProvisionContext of(DependencyDescriptor dependency) {
        return new PackedProvisionContext(dependency, null);
    }

    static ProvisionContext of(DependencyDescriptor dependency, Component componenent) {
        return new PackedProvisionContext(dependency, requireNonNull(componenent, "component is null"));
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
    public static ProvisionContext of(Key<?> key) {
        return new PackedProvisionContext(DependencyDescriptor.of(key), null);
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
    static ProvisionContext of(Key<?> key, Component component) {
        return new PackedProvisionContext(DependencyDescriptor.of(key), requireNonNull(component, "component is null"));
    }
}
// static {AopReady r = AOPSupport.compile(FooClass.class)}, at runtime r.newInstance(r))// Arghh grimt
