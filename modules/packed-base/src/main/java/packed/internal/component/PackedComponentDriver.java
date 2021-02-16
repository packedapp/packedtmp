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
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentModifierSet;
import app.packed.component.drivers.ComponentDriver;
import app.packed.component.drivers.old.ComponentFactoryDriver;
import app.packed.component.drivers.old.ComponentInstanceDriver;
import app.packed.inject.Factory;
import packed.internal.classscan.InstantiatorBuilder;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class PackedComponentDriver<C extends ComponentConfiguration> implements ComponentDriver<C> {

    final Meta meta;

    final int modifiers;

    // Holds ExtensionModel for extensions, source for sourced components
    public final Object data;

    PackedComponentDriver(Meta meta, Object data) {
        this.meta = requireNonNull(meta);
        this.data = data;
        this.modifiers = PackedComponentModifierSet.intOf(meta.modifiers.toArray());
        if (modifiers == 0) {
            throw new IllegalStateException();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return meta.modifiers;
    }

    public C toConfiguration(ComponentConfigurationContext cnc) {
        // Vil godt lave den om til CNC
        try {
            return (C) meta.mh.invoke(cnc);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    public static Meta newMeta(Type type, MethodHandles.Lookup caller, boolean isSource, Class<?> driverType, Option... options) {
        requireNonNull(options, "options is null");

        // Parse all options
        int modifiers = 0;
        for (int i = 0; i < options.length; i++) {
            OptionImpl o = (OptionImpl) options[i];
            switch (o.id) {
            case OptionImpl.OPT_BUNDLE:
                modifiers |= PackedComponentModifierSet.I_BUNDLE;
                break;
            case OptionImpl.OPT_CONSTANT:
                modifiers |= PackedComponentModifierSet.I_SINGLETON;
                break;
            case OptionImpl.OPT_STATEFUL:
                modifiers |= PackedComponentModifierSet.I_STATEFUL;
                break;
            default:
                throw new IllegalStateException(o + " is not a valid option");
            }
        }
        if (isSource) {
            modifiers |= PackedComponentModifierSet.I_SOURCE;
        }
        // IDK should we just have a Function<ComponentComposer, T>???
        // Unless we have multiple composer/context objects (which it looks like we wont have)
        // Or we fx support @AttributeProvide... This makes no sense..
        // AttributeProvide could make sense... And then some way to say retain this info at runtime...
        // But maybe this is sidecars instead???

        InstantiatorBuilder ib = InstantiatorBuilder.of(caller, driverType, ComponentBuild.class);
        ib.addKey(ComponentConfigurationContext.class, 0);
        MethodHandle mh = ib.build();
        return new Meta(type, mh, modifiers);
    }

    public static <C extends ComponentConfiguration> ComponentDriver<C> of(MethodHandles.Lookup caller, Class<? extends C> driverType, Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(Type.OTHER, caller, false, driverType, options);
        return new PackedComponentDriver<>(meta, null);
    }

    public static <C extends ComponentConfiguration, I> PackedFactoryComponentDriver<C, I> ofClass(MethodHandles.Lookup caller, Class<? extends C> driverType,
            Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(Type.CLASS, caller, true, driverType, options);
        return new PackedFactoryComponentDriver<>(meta);
    }

    public static <C extends ComponentConfiguration, I> PackedFactoryComponentDriver<C, I> ofFactory(MethodHandles.Lookup caller, Class<? extends C> driverType,
            Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(Type.FACTORY, caller, true, driverType, options);
        return new PackedFactoryComponentDriver<>(meta);
    }

    public static <C extends ComponentConfiguration, I> PackedInstanceComponentDriver<C, I> ofInstance(MethodHandles.Lookup caller,
            Class<? extends C> driverType, Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(Type.INSTANCE, caller, true, driverType, options);
        return new PackedInstanceComponentDriver<>(meta);
    }

    enum Type {
        OTHER, CLASS, FACTORY, INSTANCE;
    }

    static class Meta {
        // all options
        MethodHandle mh;

        ComponentModifierSet modifiers;

        final Type type;

        Meta(Type type, MethodHandle mh, int modifiers) {
            this.type = requireNonNull(type);
            this.mh = mh;
            this.modifiers = new PackedComponentModifierSet(modifiers);
            if (this.modifiers.isEmpty()) {
                throw new IllegalStateException();
            }
        }
    }

    // And the use one big switch
    // Kunne ogsaa encode det i ComponentDriver.option..
    // Og saa bruge MethodHandles til at extract id, data?
    // Nahhh
    public static class OptionImpl implements ComponentDriver.Option {

        static final int OPT_STATEFUL = 3;
        static final int OPT_CONSTANT = 2;
        static final int OPT_BUNDLE = 1;
        public static final OptionImpl STATELESS = new OptionImpl(OPT_STATEFUL, null);
        public static final OptionImpl CONSTANT = new OptionImpl(OPT_CONSTANT, null);
        public static final OptionImpl BUNDLE = new OptionImpl(OPT_BUNDLE, null);

        @Nullable
        final Object data;
        final int id;

        OptionImpl(int id, @Nullable Object data) {
            this.id = id;
            this.data = data;
        }
    }

    static class PackedFactoryComponentDriver<C extends ComponentConfiguration, I> implements ComponentFactoryDriver<C, I> {
        final Meta meta;

        public PackedFactoryComponentDriver(Meta meta) {
            this.meta = meta;
        }

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> bind(Factory<? extends I> factory) {
            requireNonNull(factory, "factory is null");
            if (meta.type == Type.CLASS) {
                throw new UnsupportedOperationException("Can only specify a class");
            }
            return new PackedComponentDriver<>(meta, factory);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> bind(Class<? extends I> implementation) {
            requireNonNull(implementation, "implementation is null");
            return new PackedComponentDriver<>(meta, implementation);
        }
    }

    private static class PackedInstanceComponentDriver<C extends ComponentConfiguration, I> extends PackedFactoryComponentDriver<C, I>
            implements ComponentInstanceDriver<C, I> {

        private PackedInstanceComponentDriver(Meta meta) {
            super(meta);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> applyInstance(I instance) {
            requireNonNull(instance, "instance is null");
            if (instance instanceof Class) {
                throw new IllegalArgumentException("Cannot specify a Class instance, was " + instance);
            } else if (instance instanceof Factory) {
                throw new IllegalArgumentException("Cannot specify a Factory instance, was " + instance);
            }
            return new PackedComponentDriver<>(meta, instance);
        }
    }
}
