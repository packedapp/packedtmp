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
import app.packed.container.ExtensionConfiguration;
import app.packed.inject.Factory;
import packed.internal.artifact.InstantiationContext;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.ComponentLookup;
import packed.internal.container.ExtensionModel;
import packed.internal.container.PackedContainer;
import packed.internal.container.PackedContainerRole;
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
    public static final int ROLE_EXTENSION = 32;

    // Statemanagement... A function is kind of just a singleton...

    private final int roles;

    PackedComponentDriver(int roles) {
        this.roles = roles;
    }

    // Maybe create nullable if should not add??
    public abstract ComponentNode create(@Nullable ComponentNode parent, ComponentNodeConfiguration configuration, InstantiationContext ic);

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
        } else if (this instanceof ExtensionComponentDriver) {
            return ((ExtensionComponentDriver) this).descriptor.componentName;
        } else {
            return ((ModelComponentDriver<?>) this).model.defaultPrefix();
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

    public ComponentNodeConfiguration newContainConf(ComponentNodeConfiguration parent, Bundle<?> bundle, Wirelet... wirelets) {
        ConfigSite cs = ConfigSiteSupport.captureStackFrame(parent.configSite(), ConfigSiteInjectOperations.INJECTOR_OF);
        return PackedContainerRole.create(this, cs, bundle, parent, null, wirelets).component;
    }

    public static ContainerComponentDriver container(Object source) {
        return new ContainerComponentDriver();
    }

    public static class ExtensionComponentDriver extends PackedComponentDriver<ExtensionConfiguration> {

        final ExtensionModel descriptor;

        public ExtensionComponentDriver(ExtensionModel ed) {
            super(ROLE_EXTENSION);
            this.descriptor = requireNonNull(ed);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentNode create(@Nullable ComponentNode parent, ComponentNodeConfiguration configuration, InstantiationContext ic) {
            return null;
        }

    }

    public static class ContainerComponentDriver extends PackedComponentDriver<ContainerConfiguration> {

        public static ContainerComponentDriver INSTANCE = new ContainerComponentDriver();

        ContainerComponentDriver() {
            super(ROLE_CONTAINER);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentNode create(@Nullable ComponentNode parent, ComponentNodeConfiguration configuration, InstantiationContext ic) {
            return PackedContainer.create(parent, configuration.container, ic);
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
        public ComponentNode create(@Nullable ComponentNode parent, ComponentNodeConfiguration configuration, InstantiationContext ic) {
            return new ComponentNode(parent, configuration, ic);
        }

        public MethodHandle fromFactory(PackedContainerRole context) {
            FactoryHandle<?> handle = factory.factory.handle;
            return context.fromFactoryHandle(handle);
        }

        public <T> SingletonConfiguration<T> toConf(ComponentNodeConfiguration context) {
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
        public ComponentNode create(@Nullable ComponentNode parent, ComponentNodeConfiguration configuration, InstantiationContext ic) {
            return new ComponentNode(parent, configuration, ic);
        }

        public StatelessConfiguration toConf(ComponentNodeConfiguration context) {
            return new PackedStatelessComponentConfiguration(context);
        }
    }
}
