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

import app.packed.component.ClassComponentDriver;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifierSet;
import app.packed.component.FactoryComponentDriver;
import app.packed.inject.Factory;
import packed.internal.container.PackedRealm;
import packed.internal.inject.InstantiatorBuilder;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class PackedComponentDriver<C> implements ComponentDriver<C> {

    final Meta meta;

    final Object source;

    final SourceType sourceType;

    PackedComponentDriver(Meta meta) {
        this.meta = requireNonNull(meta);
        this.source = null;
        this.sourceType = null;
    }

    <I> PackedComponentDriver(PackedClassComponentDriver<C, I> driver, Class<? extends I> clazz) {
        this.meta = driver.meta;
        this.sourceType = SourceType.CLASS;
        this.source = clazz;
    }

    <I> PackedComponentDriver(PackedFactoryComponentDriver<C, I> driver, Factory<? extends I> factory) {
        this.meta = driver.meta;
        this.sourceType = SourceType.FACTORY;
        this.source = factory;
    }

    public static <C> PackedComponentDriver<C> of(MethodHandles.Lookup caller, Class<? extends C> driverType, Option... options) {
        InstantiatorBuilder ib = InstantiatorBuilder.of(caller, driverType, ComponentNodeConfiguration.class);
        ib.addKey(ComponentConfigurationContext.class, 0);
        MethodHandle mh = ib.build();

        return new PackedComponentDriver<>(new Meta(mh));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return meta.modifiers;
    }

    public C newConfiguration(ComponentNodeConfiguration cnc) {
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

        Meta(MethodHandle mh) {
            this.mh = requireNonNull(mh);
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

    static enum SourceType {
        CLASS, FACTORY, INSTANCE, NONE;
    }
}
