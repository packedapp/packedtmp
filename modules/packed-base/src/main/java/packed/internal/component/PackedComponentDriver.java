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
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import app.packed.base.Nullable;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.BindableComponentDriver;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentDriverDescriptor;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Wirelet;
import app.packed.inject.Factory;
import app.packed.inject.ServiceComponentConfiguration;
import packed.internal.inject.classscan.Infuser;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class PackedComponentDriver<C extends ComponentConfiguration> implements ComponentDriver<C> {

    @SuppressWarnings("rawtypes")
    public static final BindableComponentDriver INSTALL_DRIVER = PackedComponentDriver.ofInstance(MethodHandles.lookup(), ServiceComponentConfiguration.class,
            PackedComponentDriver.Option.constantSource());

    /** A driver for this configuration. */
    @SuppressWarnings("rawtypes")
    public static final BindableComponentDriver STATELESS_DRIVER = PackedComponentDriver.ofClass(MethodHandles.lookup(), BaseComponentConfiguration.class,
            PackedComponentDriver.Option.statelessSource());

    // Holds ExtensionModel for extensions, source for sourced components
    public final Object data;

    final Meta meta;

    final int modifiers;

    @Nullable
    public final Wirelet wirelet = null;

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

    /** {@inheritDoc} */
    @Override
    public ComponentDriver<C> with(Wirelet wirelet) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentDriver<C> with(Wirelet... wirelet) {
        return null;
    }

    public static Meta newMeta(Type type, MethodHandles.Lookup caller, boolean isSource, Class<?> driverType, Option... options) {
        requireNonNull(options, "options is null");

        // Parse all options
        int modifiers = 0;
        for (int i = 0; i < options.length; i++) {
            OptionImpl o = (OptionImpl) options[i];
            switch (o.id) {
            case OptionImpl.OPT_CONTAINER:
                modifiers |= PackedComponentModifierSet.I_CONTAINERNEW;
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

        Infuser infuser = Infuser.build(caller, c -> c.provide(ComponentConfigurationContext.class).adapt(), ComponentSetup.class);
        MethodHandle constructor = infuser.findConstructorFor(driverType);
        
        return new Meta(type, constructor, modifiers);
    }

    public static <C extends ComponentConfiguration> ComponentDriver<C> of(MethodHandles.Lookup caller, Class<? extends C> driverType, Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(Type.OTHER, caller, false, driverType, options);
        return new PackedComponentDriver<>(meta, null);
    }

    public static <C extends ComponentConfiguration, I> PackedBindableComponentDriver<C, I> ofClass(MethodHandles.Lookup caller, Class<? extends C> driverType,
            Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(Type.CLASS, caller, true, driverType, options);
        return new PackedBindableComponentDriver<>(meta);
    }

    public static <C extends ComponentConfiguration, I> PackedBindableComponentDriver<C, I> ofFactory(MethodHandles.Lookup caller,
            Class<? extends C> driverType, Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(Type.FACTORY, caller, true, driverType, options);
        return new PackedBindableComponentDriver<>(meta);
    }

    public static <C extends ComponentConfiguration, I> PackedBindableComponentDriver<C, I> ofInstance(MethodHandles.Lookup caller,
            Class<? extends C> driverType, Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(Type.INSTANCE, caller, true, driverType, options);
        return new PackedBindableComponentDriver<>(meta);
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
    public static class OptionImpl implements PackedComponentDriver.Option {

        static final int OPT_CONTAINER = 1;
        static final int OPT_CONSTANT = 2;
        static final int OPT_STATEFUL = 3;
        public static final OptionImpl STATELESS = new OptionImpl(OPT_STATEFUL, null);
        public static final OptionImpl CONTAINER = new OptionImpl(OPT_CONTAINER, null);
        public static final OptionImpl CONSTANT = new OptionImpl(OPT_CONSTANT, null);

        @Nullable
        final Object data;
        final int id;

        OptionImpl(int id, @Nullable Object data) {
            this.id = id;
            this.data = data;
        }
    }

    private static class PackedBindableComponentDriver<C extends ComponentConfiguration, I> implements BindableComponentDriver<C, I> {
        final Meta meta;

        private PackedBindableComponentDriver(Meta meta) {
            this.meta = meta;
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
            if (meta.type == Type.CLASS) {
                throw new UnsupportedOperationException("Can only specify a class");
            } else if (meta.type == Type.FACTORY) {
                throw new UnsupportedOperationException("Can only specify a class or factory");
            }
            return new PackedComponentDriver<>(meta, instance);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> bind(Class<? extends I> implementation) {
            requireNonNull(implementation, "implementation is null");
            return new PackedComponentDriver<>(meta, implementation);
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
        public ComponentDriver<C> bindFunction(Object function) {
            throw new UnsupportedOperationException();
        }
    }

    enum Type {
        CLASS, FACTORY, INSTANCE, OTHER;
    }

    public interface Option {

        /**
         * The component the driver will be a container.
         * <p>
         * A container that is a component cannot be sourced??? Yes It can... It can be the actor system
         * 
         * @return stuff
         * @see ComponentModifier#CONTAINER
         */
        static Option container() {
            return PackedComponentDriver.OptionImpl.CONTAINER;
        }

        /**
         * The component the driver will be a container.
         * <p>
         * A container that is a component cannot be sourced??? Yes It can... It can be the actor system
         * 
         * @return stuff
         * @see ComponentModifier#CONSTANT
         */
        // InstanceComponentDriver automatically sets the source...
        static Option constantSource() {
            return PackedComponentDriver.OptionImpl.CONSTANT;
        }

        static Option sourceAssignableTo(Class<?> rawType) {
            throw new UnsupportedOperationException();
        }

        static Option statelessSource() {
            return PackedComponentDriver.OptionImpl.STATELESS;
        }

        static Option validateParent(Predicate<? super Component> validator, String msg) {
            return validateWiring((c, d) -> {
                if (validator.test(c)) {
                    throw new IllegalArgumentException(msg);
                }
            });
        }

        static Option validateParentIsContainer() {
            return validateParent(c -> c.hasModifier(ComponentModifier.CONTAINER), "This component can only be wired to a container");
        }

        // The parent + the driver
        //

        /**
         * Returns an option that
         * 
         * @param validator
         * @return the option
         */
        // Hmm integration with vaildation
        static Option validateWiring(BiConsumer<Component, ComponentDriver<?>> validator) {
            throw new UnsupportedOperationException();
        }

        // Option serviceable()
        // Hmm Maaske er alle serviceable.. Og man maa bare lade vaere
        // at expose funktionaliteten.
    }

    /** {@inheritDoc} */
    @Override
    public ComponentDriverDescriptor descriptor() {
        throw new UnsupportedOperationException();
    }
}
