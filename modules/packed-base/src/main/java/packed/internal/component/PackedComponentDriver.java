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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.component.Wirelet;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.Factory;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.container.ComponentLookup;
import packed.internal.container.PackedContainer;
import packed.internal.container.PackedContainerConfigurationContext;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.factory.FactoryHandle;

/**
 *
 */
public abstract class PackedComponentDriver<C> implements ComponentDriver<C> {

    private final int roles;

    PackedComponentDriver(int roles) {
        this.roles = roles;
    }

    public static final int ROLE_CONTAINER = 1;
    public static final int ROLE_GUEST = 2;
    public static final int ROLE_HOST = 4;

    // Statemanagement... A function is kind of just a singleton...

    public static final int ROLE_SINGLETON = 8;
    public static final int ROLE_STATELESS = 16;

    public final boolean hasContainer() {
        return hasRole(ROLE_CONTAINER);
    }

    public final boolean hasRole(int role) {
        return (roles & role) != 0;
    }
    // boolean retainAtRuntime()

    public abstract PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic);

    public PackedComponentConfigurationContext newConfiguration(PackedComponentConfigurationContext parent, Bundle<?> bundle, Wirelet... wirelets) {
        return new PackedContainerConfigurationContext(this, parent, bundle, wirelets);
    }

    public static ContainerComponentDriver container(Object source) {
        return new ContainerComponentDriver();
    }

    public static class StatelessComponentDriver extends PackedComponentDriver<ComponentConfiguration> {
        public final ComponentModel model;

        public StatelessComponentDriver(ComponentLookup lookup, Class<?> implementation) {
            super(PackedComponentDriver.ROLE_STATELESS);
            requireNonNull(implementation, "implementation is null");
            this.model = lookup.componentModelOf(implementation);
        }

        public StatelessConfiguration toConf(PackedComponentConfigurationContext context) {
            return new PackedStatelessComponentConfiguration(context);
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic) {
            return new PackedComponent(parent, configuration, ic);
        }
    }

    public static class SingletonComponentDriver extends PackedComponentDriver<ComponentConfiguration> {
        public final ComponentModel model;

        @Nullable
        public final BaseFactory<?> factory;

        @Nullable
        public final Object instance;

        public SingletonComponentDriver(ComponentLookup lookup, Factory<?> factory) {
            super(PackedComponentDriver.ROLE_SINGLETON);
            this.model = lookup.componentModelOf(factory.rawType());
            this.factory = (@Nullable BaseFactory<?>) factory;
            this.instance = null;
        }

        public SingletonComponentDriver(ComponentLookup lookup, Object instance) {
            super(PackedComponentDriver.ROLE_SINGLETON);
            this.model = lookup.componentModelOf(instance.getClass());
            this.factory = null;
            this.instance = requireNonNull(instance);
        }

        public <T> SingletonConfiguration<T> toConf(PackedComponentConfigurationContext context) {
            return new PackedSingletonConfiguration<>(context);
        }

        public MethodHandle fromFactory(PackedContainerConfigurationContext context) {
            FactoryHandle<?> handle = factory.factory.handle;
            return context.fromFactoryHandle(handle);
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic) {
            return new PackedComponent(parent, configuration, ic);
        }
    }

    public static class ContainerComponentDriver extends PackedComponentDriver<ContainerConfiguration> {

        public static ContainerComponentDriver INSTANCE = new ContainerComponentDriver();

        ContainerComponentDriver() {
            super(ROLE_CONTAINER);
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic) {
            return new PackedContainer(parent, (PackedContainerConfigurationContext) configuration, ic);
        }
    }
}
