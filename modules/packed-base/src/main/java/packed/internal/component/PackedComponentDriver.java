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
import app.packed.config.ConfigSite;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.Factory;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.ComponentLookup;
import packed.internal.container.PackedContainer;
import packed.internal.container.PackedContainerConfigurationContext;
import packed.internal.inject.ConfigSiteInjectOperations;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.factory.FactoryHandle;

/**
 *
 */
public abstract class PackedComponentDriver<C> implements ComponentDriver<C> {

    public static final int ROLE_CONTAINER = 1;
    public static final int ROLE_GUEST = 2;
    public static final int ROLE_HOST = 4;
    public static final int ROLE_SINGLETON = 8;
    public static final int ROLE_STATELESS = 16;

    // Statemanagement... A function is kind of just a singleton...

    private final int roles;

    PackedComponentDriver(int roles) {
        this.roles = roles;
    }

    public abstract PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic);

    public String defaultName(Object ssss) {
        if (isContainer()) {
            // I think try and move some of this to ComponentNameWirelet
            @Nullable
            Class<?> source = ssss.getClass();
            if (Bundle.class.isAssignableFrom(source)) {
                String nnn = source.getSimpleName();
                if (nnn.length() > 6 && nnn.endsWith("Bundle")) {
                    nnn = nnn.substring(0, nnn.length() - 6);
                }
                if (nnn.length() > 0) {
                    // checkName, if not just App
                    // TODO need prefix
                    return nnn;
                }
                if (nnn.length() == 0) {
                    return "Container";
                }
            }
            // TODO think it should be named Artifact type, for example, app, injector, ...
            return "Unknown";
        } else if (this instanceof SingletonComponentDriver) {
            return ((SingletonComponentDriver) this).model.defaultPrefix();
        } else {
            return ((StatelessComponentDriver) this).model.defaultPrefix();
        }
    }

    public final boolean hasRole(int role) {
        return (roles & role) != 0;
    }
    // boolean retainAtRuntime()

    /**
     * Returns whether or not this driver creates a component with container role.
     * 
     * @return whether or not this driver creates a component with container role
     */
    public final boolean isContainer() {
        return hasRole(ROLE_CONTAINER);
    }

    public PackedComponentConfigurationContext newContainConf(PackedComponentConfigurationContext parent, Bundle<?> bundle, Wirelet... wirelets) {
        ConfigSite cs = ConfigSiteSupport.captureStackFrame(parent.configSite(), ConfigSiteInjectOperations.INJECTOR_OF);
        return PackedContainerConfigurationContext.create(this, cs, bundle, parent, null, wirelets).component;
    }

    public static ContainerComponentDriver container(Object source) {
        return new ContainerComponentDriver();
    }

    public static class ContainerComponentDriver extends PackedComponentDriver<ContainerConfiguration> {

        public static ContainerComponentDriver INSTANCE = new ContainerComponentDriver();

        ContainerComponentDriver() {
            super(ROLE_CONTAINER);
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic) {
            return new PackedContainer(parent, configuration.container, ic);
        }
    }

    public static class SingletonComponentDriver extends ModelComponentDriver<ComponentConfiguration> {
        @Nullable
        public final BaseFactory<?> factory;

        @Nullable
        public final Object instance;

        public SingletonComponentDriver(ComponentLookup lookup, Factory<?> factory) {
            super(PackedComponentDriver.ROLE_SINGLETON, lookup.componentModelOf(factory.rawType()));
            this.factory = (@Nullable BaseFactory<?>) factory;
            this.instance = null;
        }

        public SingletonComponentDriver(ComponentLookup lookup, Object instance) {
            super(PackedComponentDriver.ROLE_SINGLETON, lookup.componentModelOf(instance.getClass()));
            this.factory = null;
            this.instance = requireNonNull(instance);
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic) {
            return new PackedComponent(parent, configuration, ic);
        }

        public MethodHandle fromFactory(PackedContainerConfigurationContext context) {
            FactoryHandle<?> handle = factory.factory.handle;
            return context.fromFactoryHandle(handle);
        }

        public <T> SingletonConfiguration<T> toConf(PackedComponentConfigurationContext context) {
            return new PackedSingletonConfiguration<>(context);
        }
    }

    public static abstract class ModelComponentDriver<T> extends PackedComponentDriver<T> {
        public final ComponentModel model;

        /**
         * @param roles
         */
        ModelComponentDriver(int roles, ComponentModel model) {
            super(roles);
            this.model = model;
        }

    }

    public static class StatelessComponentDriver extends ModelComponentDriver<ComponentConfiguration> {

        public StatelessComponentDriver(ComponentLookup lookup, Class<?> implementation) {
            super(PackedComponentDriver.ROLE_STATELESS, lookup.componentModelOf(requireNonNull(implementation, "implementation is null")));
            requireNonNull(implementation, "implementation is null");
        }

        /** {@inheritDoc} */
        @Override
        public PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic) {
            return new PackedComponent(parent, configuration, ic);
        }

        public StatelessConfiguration toConf(PackedComponentConfigurationContext context) {
            return new PackedStatelessComponentConfiguration(context);
        }
    }
}
