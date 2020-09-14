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
import app.packed.component.Bundle;
import app.packed.component.ClassComponentDriver;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifierSet;
import app.packed.component.FactoryComponentDriver;
import app.packed.component.InstanceComponentDriver;
import app.packed.inject.Factory;
import packed.internal.container.ExtensionModel;
import packed.internal.container.PackedRealm;
import packed.internal.inject.various.InstantiatorBuilder;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class PackedComponentDriver<C> implements ComponentDriver<C> {

    final Meta meta;

    final int modifiers;

    // Holds ExtensionModel for extensions, source for sourced components
    final Object data;

    PackedComponentDriver(Meta meta, Object data) {
        this.meta = requireNonNull(meta);
        this.data = data;
        this.modifiers = PackedComponentModifierSet.intOf(meta.modifiers.toArray());
        if (modifiers == 0) {
            throw new IllegalStateException();
        }
    }

    String defaultName(PackedRealm realm) {
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
        } else if (modifiers().isExtension()) {
            return ((ExtensionModel) data).nameComponent;
        }
        return "Unknown";
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

    public static PackedComponentDriver<Void> extensionDriver(ExtensionModel em) {
        // AN EXTENSION DOES NOT HAVE A SOURCE. Sources are to be analyzed
        // And is available at runtime
        Meta meta = new Meta(null, PackedComponentModifierSet.I_EXTENSION);
        return new PackedComponentDriver<>(meta, em);
    }

    public static Meta newMeta(MethodHandles.Lookup caller, boolean isSource, Class<?> driverType, Option... options) {
        requireNonNull(options, "options is null");

        // Parse all options
        int modifiers = 0;
        for (int i = 0; i < options.length; i++) {
            OptionImpl o = (OptionImpl) options[i];
            switch (o.id) {
            case OptionImpl.OPT_CONTAINER:
                modifiers |= PackedComponentModifierSet.I_CONTAINER;
                break;
            case OptionImpl.OPT_CONSTANT:
                modifiers |= PackedComponentModifierSet.I_SINGLETON;
                break;
            case OptionImpl.OPT_STATELESS:
                modifiers |= PackedComponentModifierSet.I_STATELESS;
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
        InstantiatorBuilder ib = InstantiatorBuilder.of(caller, driverType, ComponentNodeConfiguration.class);
        ib.addKey(ComponentConfigurationContext.class, 0);
        MethodHandle mh = ib.build();
        return new Meta(mh, modifiers);
    }

    public static <C> ComponentDriver<C> of(MethodHandles.Lookup caller, Class<? extends C> driverType, Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(caller, false, driverType, options);
        return new PackedComponentDriver<>(meta, null);
    }

    public static <C, I> PackedClassComponentDriver<C, I> ofClass(MethodHandles.Lookup caller, Class<? extends C> driverType, Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(caller, true, driverType, options);
        return new PackedClassComponentDriver<>(meta);
    }

    public static <C, I> PackedInstanceComponentDriver<C, I> ofInstance(MethodHandles.Lookup caller, Class<? extends C> driverType, Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(caller, true, driverType, options);
        return new PackedInstanceComponentDriver<>(meta);
    }

    static class Meta {
        // all options
        MethodHandle mh;

        ComponentModifierSet modifiers;

        Meta(MethodHandle mh, int modifiers) {
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

        static final int OPT_STATELESS = 3;
        static final int OPT_CONSTANT = 2;
        static final int OPT_CONTAINER = 1;
        public static final OptionImpl STATELESS = new OptionImpl(OPT_STATELESS, null);
        public static final OptionImpl CONSTANT = new OptionImpl(OPT_CONSTANT, null);
        public static final OptionImpl CONTAINER = new OptionImpl(OPT_CONTAINER, null);

        @Nullable
        final Object data;
        final int id;

        OptionImpl(int id, @Nullable Object data) {
            this.id = id;
            this.data = data;
        }
    }

    static class PackedClassComponentDriver<C, I> implements ClassComponentDriver<C, I> {
        final Meta meta;

        public PackedClassComponentDriver(Meta meta) {
            this.meta = meta;
        }

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> bindToClass(PackedRealm realm, Class<? extends I> implementation) {
            requireNonNull(implementation, "implementation is null");
            return new PackedComponentDriver<>(meta, implementation);
        }
    }

    static class PackedFactoryComponentDriver<C, I> extends PackedClassComponentDriver<C, I> implements FactoryComponentDriver<C, I> {

        public PackedFactoryComponentDriver(Meta meta) {
            super(meta);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> bindToFactory(PackedRealm realm, Factory<? extends I> factory) {
            requireNonNull(factory, "factory is null");
            return new PackedComponentDriver<>(meta, factory);
        }
    }

    private static class PackedInstanceComponentDriver<C, I> extends PackedFactoryComponentDriver<C, I> implements InstanceComponentDriver<C, I> {

        private PackedInstanceComponentDriver(Meta meta) {
            super(meta);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> bindToInstance(PackedRealm realm, I instance) {
            requireNonNull(instance, "instance is null");
            if (instance instanceof Class) {
                throw new IllegalStateException("Cannot specify a Class instance, was " + instance);
            } else if (instance instanceof Factory) {
                throw new IllegalStateException("Cannot specify a Factory instance, was " + instance);
            }
            return new PackedComponentDriver<>(meta, instance);
        }
    }
}
