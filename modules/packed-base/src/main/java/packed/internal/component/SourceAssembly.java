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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentModifier;
import app.packed.inject.Factory;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.inject.dependency.Injectable;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.service.assembly.ServiceAssembly;

/** All components with a {@link ComponentModifier#SOURCED} modifier has an instance of this class. */
public final class SourceAssembly implements DependencyProvider {

    /** The component this source is a part of. */
    public final ComponentNodeConfiguration compConf;

    @Nullable
    private final BaseFactory<?> factory;

    /** An injectable, if this source needs to be created at runtime (not a constant). */
    @Nullable
    final Injectable injectable;

    /** If the source represents an instance. */
    @Nullable
    final Object instance;

    /** The source model. */
    public final SourceModel model;

    /** The index at which to store the runtime instance, or -1 if it should not be stored. */
    public final int regionIndex;

    /** Whether or not this source is provided as a service. */
    @Nullable
    public ServiceAssembly<?> service;

    SourceAssembly(ComponentNodeConfiguration compConf, RegionAssembly region, Object source) {
        this.compConf = compConf;
        this.regionIndex = compConf.modifiers().isSingleton() ? region.reserve() : -1;

        // The specified source is either a Class, a Factory, or an instance
        if (source instanceof Class) {
            Class<?> c = (Class<?>) source;
            this.instance = null;
            this.model = compConf.realm.componentModelOf(c);
            if (compConf.modifiers().isStateless()) {
                this.injectable = null;
                this.factory = null;
            } else {
                this.factory = (BaseFactory<?>) Factory.find(c);
                this.injectable = new Injectable(this, factory);
            }
        } else if (source instanceof Factory) {
            this.factory = (BaseFactory<?>) source;
            this.model = compConf.realm.componentModelOf(factory.rawType());
            this.instance = null;
            this.injectable = new Injectable(this, factory);
        } else {
            this.model = compConf.realm.componentModelOf(source.getClass());
            this.instance = source;
            this.injectable = null;
            this.factory = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        if (instance != null) {
            MethodHandle mh = MethodHandles.constant(instance.getClass(), instance);
            return MethodHandles.dropArguments(mh, 0, RuntimeRegion.class); // MethodHandle()T -> MethodHandle(Region)T
        } else if (regionIndex > -1) {
            return RuntimeRegion.readSingletonAs(regionIndex, model.modelType());
        } else {
            return injectable.buildMethodHandle();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Injectable injectable() {
        return injectable;
    }

    ServiceAssembly<?> provide() {
        // Maybe we should throw an exception, if the user tries to provide an entry multiple times??
        ServiceAssembly<?> s = service;
        if (s == null) {
            Key<?> key;
            if (instance != null) {
                key = Key.of(model.modelType()); // Move to model?? What if instance has Qualifier???
            } else {
                key = factory.key();
            }
            s = service = compConf.injectionManager().provideFromSource(compConf, key);
        }
        return s;
    }
}
