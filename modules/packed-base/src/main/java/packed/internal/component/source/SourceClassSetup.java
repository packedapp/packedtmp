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
package packed.internal.component.source;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentModifier;
import app.packed.inject.Factory;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.SlotTable;
import packed.internal.inject.Dependant;
import packed.internal.inject.DependencyDescriptor;
import packed.internal.inject.DependencyProvider;
import packed.internal.inject.service.build.BuildtimeService;
import packed.internal.util.MethodHandleUtil;

/** All components with a {@link ComponentModifier#SOURCED} modifier has an instance of this class. */
public final class SourceClassSetup implements DependencyProvider {

    /** A factory that can used to create instances. */
    @Nullable
    private final Factory<?> factory;

    /** An injectable, if this source needs to be created at runtime (not a constant). */
    @Nullable
    private final Dependant dependant;

    /** If the source represents an instance. */
    @Nullable
    public final Object instance;

    /** The source model. */
    public final ClassSourceModel model;

    /** The index at which to store the runtime instance, or -1 if it should not be stored. */
    public final int regionIndex;

    /** A service object if the source is provided as a service. */
    @Nullable
    public BuildtimeService service;

    private SourceClassSetup(ComponentSetup compConf, int regionIndex, Object source) {
        this.regionIndex = regionIndex;

        // The specified source is either a Class, a Factory, or an instance
        Class<?> sourceType;
        if (source instanceof Class<?> cl) {
            sourceType = cl;
            this.instance = null;
            // We need to start putting stateful on every component...
            // We need to stateful on all components...
            this.factory = compConf.modifiers().isStateful() ? null : Factory.of(sourceType);
        } else if (source instanceof Factory<?> fac) {
            this.instance = null;
            this.factory = fac;
            sourceType = factory.rawType();
        } else {
            this.instance = source;
            this.factory = null;
            sourceType = source.getClass();
        }

        this.model = compConf.realm.accessor().modelOf(sourceType);

        if (factory == null) {
            this.dependant = null;
        } else {
            MethodHandle mh = compConf.realm.accessor().toMethodHandle(factory);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<DependencyDescriptor> dependencies = (List) factory.variables();
            this.dependant = new Dependant(this, dependencies, mh);
        }
    }

    public static SourceClassSetup create(ComponentSetup compConf, PackedComponentDriver<?> driver) {
        // Reserve a place in the regions runtime memory, if the component is a singleton
        int regionIndex = compConf.modifiers().isSingleton() ? compConf.slotTable.reserve() : -1;
        // Create the source
        SourceClassSetup s = new SourceClassSetup(compConf, regionIndex, driver.data);

        if (s.instance != null) {
            compConf.slotTable.constants.add(s);
        } else if (s.dependant != null) {
            compConf.memberOfContainer.addDependant(s.dependant);
        }

        // Apply any sidecars
        s.model.register(compConf, s);
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        if (instance != null) {
            return MethodHandleUtil.insertFakeParameter(MethodHandleUtil.constant(instance), SlotTable.class); // MethodHandle()T -> MethodHandle(Region)T
        } else if (regionIndex > -1) {
            return SlotTable.readSingletonAs(regionIndex, model.type);
        } else {
            return dependant.buildMethodHandle();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Dependant dependant() {
        return dependant;
    }

    public BuildtimeService provide(ComponentSetup compConf) {
        // Maybe we should throw an exception, if the user tries to provide an entry multiple times??
        BuildtimeService s = service;
        if (s == null) {
            Key<?> key;
            if (instance != null) {
                key = Key.of(model.type); // Move to model?? What if instance has Qualifier???
            } else {
                key = factory.key();
            }
            s = service = compConf.memberOfContainer.getServiceManagerOrCreate().provideSource(compConf, key);
        }
        return s;
    }
}
