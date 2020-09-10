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
import app.packed.component.ClassComponentDriver;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.FactoryComponentDriver;
import app.packed.component.InstanceComponentDriver;
import app.packed.component.StatelessConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.Factory;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.PackedRealm;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.factory.FactoryHandle;

/**
 *
 */
public abstract class OldPackedComponentDriver<C> implements ComponentDriver<C> {

    public static ComponentDriver<ContainerConfiguration> CONTAINER_DRIVER = new ContainerComponentDriver();

    final int modifiers;

    protected OldPackedComponentDriver(ComponentModifier... properties) {
        this.modifiers = PackedComponentModifierSet.intOf(properties);
    }

    @Nullable
    public Class<?> sourceType() {
        return null;
    }

    public String defaultName(PackedRealm realm) {
        if (modifiers().isContainer()) {
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

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    public abstract C toConfiguration(ComponentConfigurationContext cnc);

    /** The default driver for creating new containers. */
    private static class ContainerComponentDriver extends OldPackedComponentDriver<ContainerConfiguration> {

        private ContainerComponentDriver() {
            super(ComponentModifier.CONTAINER);
        }

        @Override
        public ContainerConfiguration toConfiguration(ComponentConfigurationContext cnc) {
            return new PackedContainerConfiguration(cnc);
        }
    }

    public static class SingletonComponentDriver<T> extends OldPackedComponentDriver<BeanConfiguration<T>> {

        @Nullable
        public final BaseFactory<?> factory;

        @Nullable
        public final T instance;

        public final ComponentModel model;

        public final boolean isPrototype;

        public SingletonComponentDriver(PackedRealm realm, Factory<?> factory, boolean isPrototype) {
            super(ComponentModifier.SINGLETON, ComponentModifier.SOURCED);
            requireNonNull(factory, "factory is null");
            this.model = realm.componentModelOf(factory.rawType());
            this.factory = (@Nullable BaseFactory<?>) factory;
            this.instance = null;
            this.isPrototype = isPrototype;
        }

        public SingletonComponentDriver(PackedRealm realm, T instance) {
            super(ComponentModifier.SINGLETON, ComponentModifier.SOURCED);
            this.instance = requireNonNull(instance, "instance is null");
            this.model = realm.componentModelOf(instance.getClass());
            this.factory = null;
            this.isPrototype = false;
        }

        @Override
        public String defaultName(PackedRealm realm) {
            return model.defaultPrefix();
        }

        public MethodHandle fromFactory(ComponentNodeConfiguration context) {
            FactoryHandle<?> handle = factory.factory.handle;
            return context.realm().fromFactoryHandle(handle);
        }

        @Override
        @Nullable
        public Class<?> sourceType() {
            return model.type();
        }

        @Override
        public BeanConfiguration<T> toConfiguration(ComponentConfigurationContext context) {
            ComponentNodeConfiguration cnc = (ComponentNodeConfiguration) context;
            model.invokeOnHookOnInstall(cnc);
            return new BeanConfiguration<>(cnc);
        }

        public static <T> InstanceComponentDriver<BeanConfiguration<T>, T> driver() {
            return new InstanceComponentDriver<BeanConfiguration<T>, T>() {

                @Override
                public ComponentDriver<BeanConfiguration<T>> bindToFactory(PackedRealm realm, Factory<? extends T> factory) {
                    return new SingletonComponentDriver<>(realm, factory, false);
                }

                @Override
                public ComponentDriver<BeanConfiguration<T>> bindToInstance(PackedRealm realm, T instance) {
                    return new SingletonComponentDriver<>(realm, instance);
                }
            };
        }

        public static <T> FactoryComponentDriver<BeanConfiguration<T>, T> prototype() {
            return new FactoryComponentDriver<BeanConfiguration<T>, T>() {

                @Override
                public ComponentDriver<BeanConfiguration<T>> bindToFactory(PackedRealm realm, Factory<? extends T> factory) {
                    return new SingletonComponentDriver<>(realm, factory, true);
                }
            };
        }
    }

    public static class StatelessComponentDriver extends OldPackedComponentDriver<StatelessConfiguration> {
        public final ComponentModel model;

        private StatelessComponentDriver(PackedRealm lookup, Class<?> implementation) {
            super(ComponentModifier.UNSCOPED, ComponentModifier.SOURCED);
            this.model = lookup.componentModelOf(requireNonNull(implementation, "implementation is null"));
            requireNonNull(implementation, "implementation is null");
        }

        @Override
        public String defaultName(PackedRealm realm) {
            return model.defaultPrefix();
        }

        @Override
        @Nullable
        public Class<?> sourceType() {
            return model.type();
        }

        @Override
        public StatelessConfiguration toConfiguration(ComponentConfigurationContext context) {
            ComponentNodeConfiguration cnc = (ComponentNodeConfiguration) context;
            model.invokeOnHookOnInstall(cnc);
            return new PackedStatelessComponentConfiguration(cnc);
        }

        public static <T> ClassComponentDriver<StatelessConfiguration, T> driver() {
            return new ClassComponentDriver<StatelessConfiguration, T>() {

                @Override
                public ComponentDriver<StatelessConfiguration> bindToClass(PackedRealm realm, Class<? extends T> implementation) {
                    return new OldPackedComponentDriver.StatelessComponentDriver(realm, implementation);
                }
            };
        }
    }
}
