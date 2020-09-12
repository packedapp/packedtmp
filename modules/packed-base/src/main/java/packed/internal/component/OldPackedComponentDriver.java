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

import app.packed.base.Nullable;
import app.packed.component.BeanConfiguration;
import app.packed.component.Bundle;
import app.packed.component.ClassComponentDriver;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.InstanceComponentDriver;
import app.packed.component.StatelessConfiguration;
import app.packed.inject.Factory;
import packed.internal.container.ExtensionModel;
import packed.internal.container.PackedRealm;
import packed.internal.inject.factory.BaseFactory;

/**
 *
 */
public abstract class OldPackedComponentDriver<C> implements ComponentDriver<C> {

    final int modifiers;

    protected OldPackedComponentDriver(ComponentModifier... properties) {
        this.modifiers = PackedComponentModifierSet.intOf(properties);
    }

    public String defaultName(PackedRealm realm) {
        if (this instanceof PackedComponentDriver) {
            PackedComponentDriver<?> pcd = (PackedComponentDriver<?>) this;
            if (pcd.source instanceof ExtensionModel) {
                return ((ExtensionModel) pcd.source).defaultComponentName;
            }
        }
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

    public static class SingletonComponentDriver<T> extends OldPackedComponentDriver<BeanConfiguration<T>> {

        @Nullable
        final BaseFactory<?> factory;

        @Nullable
        final T instance;

        public SingletonComponentDriver(PackedRealm realm, Factory<?> factory) {
            super(ComponentModifier.CONSTANT, ComponentModifier.SOURCED);
            requireNonNull(factory, "factory is null");
            this.factory = (BaseFactory<?>) factory;
            this.instance = null;
        }

        public SingletonComponentDriver(PackedRealm realm, T instance) {
            super(ComponentModifier.CONSTANT, ComponentModifier.SOURCED);
            this.instance = requireNonNull(instance, "instance is null");
            this.factory = null;
        }

        @Override
        public BeanConfiguration<T> toConfiguration(ComponentConfigurationContext context) {
            return new BeanConfiguration<>(context);
        }

        public static <T> InstanceComponentDriver<BeanConfiguration<T>, T> driver() {
            return new InstanceComponentDriver<BeanConfiguration<T>, T>() {

                @Override
                public ComponentDriver<BeanConfiguration<T>> bindToFactory(PackedRealm realm, Factory<? extends T> factory) {
                    return new SingletonComponentDriver<>(realm, factory);
                }

                @Override
                public ComponentDriver<BeanConfiguration<T>> bindToInstance(PackedRealm realm, T instance) {
                    return new SingletonComponentDriver<>(realm, instance);
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
