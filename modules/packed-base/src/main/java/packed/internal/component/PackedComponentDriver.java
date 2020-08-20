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
import app.packed.component.BeanConfiguration;
import app.packed.component.Bundle;
import app.packed.component.ClassSourcedDriver;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.InstanceSourcedDriver;
import app.packed.component.StatelessConfiguration;
import app.packed.component.WireableComponentDriver;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.Factory;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.PackedRealm;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.factory.FactoryHandle;

/**
 *
 */
public abstract class PackedComponentDriver<C> implements WireableComponentDriver<C> {

    public static final int ROLE_CONTAINER = 1;
    public static final int ROLE_EXTENSION = 32;
    public static final int ROLE_GUEST = 2;
    public static final int ROLE_HOST = 4;
    public static final int ROLE_SINGLETON = 8;
    public static final int ROLE_STATELESS = 16;

    // Statemanagement... A function is kind of just a singleton...

    private final int roles;

    protected PackedComponentDriver(int roles) {
        this.roles = roles;
    }

    public abstract C toConfiguration(ComponentConfigurationContext cnc);

    public String defaultName(PackedRealm realm) {
        if (isContainer()) {
            // I think try and move some of this to ComponentNameWirelet
            @Nullable
            Class<?> source = realm.type();
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
        }
        return "Unknown";
    }

    public final boolean hasRole(int role) {
        return (roles & role) != 0;
    }

    public final boolean hasRuntimeRepresentation() {
        return !isExtension();
    }

    /**
     * Returns whether or not this driver creates a component with container role.
     * 
     * @return whether or not this driver creates a component with container role
     */
    public final boolean isContainer() {
        return hasRole(ROLE_CONTAINER);
    }

    public final boolean isExtension() {
        return hasRole(ROLE_EXTENSION);
    }

    public final boolean isGuest() {
        return hasRole(ROLE_GUEST);
    }

    public static class ContainerComponentDriver extends PackedComponentDriver<ContainerConfiguration> {

        public static ContainerComponentDriver INSTANCE = new ContainerComponentDriver();

        ContainerComponentDriver() {
            super(ROLE_CONTAINER);
        }

        @Override
        public ContainerConfiguration toConfiguration(ComponentConfigurationContext cnc) {
            return new PackedContainerConfiguration(cnc);
        }
    }

    public static class SingletonComponentDriver<T> extends PackedComponentDriver<BeanConfiguration<T>> {
        public final ComponentModel model;
        @Nullable
        public final BaseFactory<?> factory;

        @Nullable
        public final T instance;

        public SingletonComponentDriver(PackedRealm realm, Factory<?> factory) {
            super(PackedComponentDriver.ROLE_SINGLETON);
            requireNonNull(factory, "factory is null");
            this.model = realm.componentModelOf(factory.rawType());
            this.factory = (@Nullable BaseFactory<?>) factory;
            this.instance = null;
        }

        @Override
        public String defaultName(PackedRealm realm) {
            return model.defaultPrefix();
        }

        public SingletonComponentDriver(PackedRealm realm, T instance) {
            super(PackedComponentDriver.ROLE_SINGLETON);
            this.instance = requireNonNull(instance, "instance is null");
            this.model = realm.componentModelOf(instance.getClass());
            this.factory = null;
        }

        public MethodHandle fromFactory(ComponentNodeConfiguration context) {
            FactoryHandle<?> handle = factory.factory.handle;
            return context.realm().fromFactoryHandle(handle);
        }

        @Override
        public BeanConfiguration<T> toConfiguration(ComponentConfigurationContext context) {
            ComponentNodeConfiguration cnc = (ComponentNodeConfiguration) context;
            model.invokeOnHookOnInstall(cnc);
            return new BeanConfiguration<>(cnc);
        }

        public static <T> InstanceSourcedDriver<BeanConfiguration<T>, T> driver() {
            return new InstanceSourcedDriver<BeanConfiguration<T>, T>() {

                @Override
                public WireableComponentDriver<BeanConfiguration<T>> bindToFactory(PackedRealm realm, Factory<T> factory) {
                    return new SingletonComponentDriver<>(realm, factory);
                }

                @Override
                public WireableComponentDriver<BeanConfiguration<T>> bindToInstance(PackedRealm realm, T instance) {
                    return new SingletonComponentDriver<>(realm, instance);
                }
            };
        }
    }

    public static class StatelessComponentDriver extends PackedComponentDriver<StatelessConfiguration> {
        public final ComponentModel model;

        private StatelessComponentDriver(PackedRealm lookup, Class<?> implementation) {
            super(PackedComponentDriver.ROLE_STATELESS);
            this.model = lookup.componentModelOf(requireNonNull(implementation, "implementation is null"));
            requireNonNull(implementation, "implementation is null");
        }

        @Override
        public String defaultName(PackedRealm realm) {
            return model.defaultPrefix();
        }

        @Override
        public StatelessConfiguration toConfiguration(ComponentConfigurationContext context) {
            ComponentNodeConfiguration cnc = (ComponentNodeConfiguration) context;
            model.invokeOnHookOnInstall(cnc);
            return new PackedStatelessComponentConfiguration(cnc);
        }

        public static <T> ClassSourcedDriver<StatelessConfiguration, T> driver() {
            return new ClassSourcedDriver<StatelessConfiguration, T>() {

                @Override
                public WireableComponentDriver<StatelessConfiguration> bindToClass(PackedRealm realm, Class<T> implementation) {
                    return new PackedComponentDriver.StatelessComponentDriver(realm, implementation);
                }
            };
        }
    }
}
