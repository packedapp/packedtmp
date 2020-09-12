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
import app.packed.component.ClassComponentDriver;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifierSet;
import app.packed.component.FactoryComponentDriver;
import app.packed.component.InstanceComponentDriver;
import app.packed.inject.Factory;
import packed.internal.container.ExtensionModel;
import packed.internal.container.PackedRealm;
import packed.internal.inject.InstantiatorBuilder;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class PackedComponentDriver<C> extends OldPackedComponentDriver<C> implements ComponentDriver<C> {

    final Meta meta;

    final Object source;

    final SourceType sourceType;

    PackedComponentDriver(Meta meta) {
        super(meta.modifiers.toArray());
        this.meta = requireNonNull(meta);
        this.source = null;
        this.sourceType = null;
    }

    <I> PackedComponentDriver(PackedClassComponentDriver<C, I> driver, Object instance) {
        this.meta = driver.meta;
        this.sourceType = SourceType.INSTANCE;
        this.source = instance;
    }

    <I> PackedComponentDriver(PackedClassComponentDriver<C, I> driver, Class<? extends I> clazz) {
        this.meta = driver.meta;
        this.sourceType = SourceType.CLASS;
        this.source = clazz;
    }

    private PackedComponentDriver(Meta meta, ExtensionModel em) {
        this.meta = meta;
        this.source = em;
        this.sourceType = SourceType.NONE;
    }

    <I> PackedComponentDriver(PackedFactoryComponentDriver<C, I> driver, Factory<? extends I> factory) {
        this.meta = driver.meta;
        this.sourceType = SourceType.FACTORY;
        this.source = factory;
    }

    public static PackedComponentDriver<Void> extensionDriver(ExtensionModel em) {
        // AN EXTENSION DOES NOT HAVE A SOURCE. Sources are to be analyzed
        // And is available at runtime
        Meta meta = new Meta(null, PackedComponentModifierSet.I_EXTENSION);
        return new PackedComponentDriver<>(meta, em);
    }

    public static <C> PackedComponentDriver<C> of(MethodHandles.Lookup caller, Class<? extends C> driverType, Option... options) {
        requireNonNull(options, "options is null");

        Meta meta = newMeta(caller, driverType, options);
        return new PackedComponentDriver<>(meta);
    }

    public static Meta newMeta(MethodHandles.Lookup caller, Class<?> driverType, Option... options) {
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
            default:
                throw new IllegalStateException(o + " is not a valid option");
            }
        }

        // Find constructor
        InstantiatorBuilder ib = InstantiatorBuilder.of(caller, driverType, ComponentNodeConfiguration.class);
        ib.addKey(ComponentConfigurationContext.class, 0);
        MethodHandle mh = ib.build();

        return new Meta(mh, modifiers);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return meta.modifiers;
    }

    @Override
    public C toConfiguration(ComponentConfigurationContext cnc) {
        // Vil godt lave den om til CNC
        try {
            return (C) meta.mh.invoke(cnc);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    static class Meta {
        // all options
        MethodHandle mh;

        ComponentModifierSet modifiers;

        Meta(MethodHandle mh, int modifiers) {
            this.mh = mh;
            this.modifiers = new PackedComponentModifierSet(modifiers);
        }
    }

    static class PackedClassComponentDriver<C, I> implements ClassComponentDriver<C, I> {
        Meta meta;

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> bindToClass(PackedRealm realm, Class<? extends I> implementation) {
            return new PackedComponentDriver<>(this, implementation);
        }
    }

    static class PackedFactoryComponentDriver<C, I> extends PackedClassComponentDriver<C, I> implements FactoryComponentDriver<C, I> {

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> bindToFactory(PackedRealm realm, Factory<? extends I> factory) {
            return new PackedComponentDriver<>(this, factory);
        }
    }

    static class PackedInstanceComponentDriver<C, I> extends PackedFactoryComponentDriver<C, I> implements InstanceComponentDriver<C, I> {

        /** {@inheritDoc} */
        @Override
        public ComponentDriver<C> bindToInstance(PackedRealm realm, I instance) {
            return new PackedComponentDriver<>(this, instance);
        }
    }

    static enum SourceType {
        CLASS, FACTORY, INSTANCE, NONE;
    }

    // And the use one big switch
    // Kunne ogsaa encode det i ComponentDriver.option..
    // Og saa bruge MethodHandles til at extract id, data?
    // Nahhh
    public static class OptionImpl implements ComponentDriver.Option {
        static final int OPT_CONTAINER = 1;
        static final int OPT_CONSTANT = 2;

        public static final OptionImpl CONTAINER = new OptionImpl(OPT_CONTAINER, null);
        public static final OptionImpl CONSTANT = new OptionImpl(OPT_CONSTANT, null);

        final int id;
        @Nullable
        final Object data;

        OptionImpl(int id, @Nullable Object data) {
            this.id = id;
            this.data = data;
        }
    }
}
