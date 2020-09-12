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

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentModifier;
import app.packed.inject.Factory;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.resolvable.DependencyProvider;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.service.ComponentBuildEntry;

/**
 * All components that have a {@link ComponentModifier#SOURCED} modifier has an instance of this class.
 */
// Maaske har vi en abstract SourceAssembly.. og Saa SingletonSourceAssembly
public class SourceAssembly implements DependencyProvider {

    /** The component this source is a part of. */
    public final ComponentNodeConfiguration component;

    private final BaseFactory<?> factory;

    /** An injectable, if this source needs to be created at runtime (not a constant). */
    @Nullable
    final Injectable injectable;

    /** If the source represents an instance. */
    @Nullable
    final Object instance;

    /** The source model. */
    final SourceModel model;

    /** The index at which to store the runtime instance, or -1 if it should not be stored. */
    public final int regionIndex;

    /** Whether or not the component is provided as a service. */
    @Nullable
    public BuildtimeService<?> service;

    SourceAssembly(ComponentNodeConfiguration component) {
        this.component = requireNonNull(component);

        RegionAssembly region = component.region;
        this.regionIndex = component.modifiers().isSingleton() ? region.reserve() : -1;

        Object source = component.driver.data;
        if (source instanceof Class) {
            Class<?> c = (Class<?>) source;
            this.factory = (BaseFactory<?>) Factory.find(c);
            this.instance = null;
            this.model = component.realm.componentModelOf(factory.rawType());
            if (component.modifiers().isStateless()) {
                this.injectable = null;
            } else {
                this.injectable = new Injectable(this, factory);
                region.resolver.sourceInjectables.add(this);
                region.resolver.allInjectables.add(injectable);
            }
        } else if (source instanceof Factory) {
            this.factory = (BaseFactory<?>) source;
            this.model = component.realm.componentModelOf(factory.rawType());
            this.instance = null;
            this.injectable = new Injectable(this, factory);
            region.resolver.sourceInjectables.add(this);
            region.resolver.allInjectables.add(injectable);
        } else {
            this.model = component.realm.componentModelOf(source.getClass());
            this.instance = source;
            this.injectable = null;
            region.resolver.sourceConstants.add(this);
            this.factory = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Injectable injectable() {
        return injectable;
    }

    public boolean isPrototype() {
        return !component.modifiers().isSingleton();
    }

    // Bliver kaldt naar man koere provide();
    public BuildtimeService<?> provide() {
        // Not sure we should allow for calling provide multiple times...
        BuildtimeService<?> c = service;
        if (c == null) {
            Key<?> key;
            if (instance != null) {
                key = Key.of(model.type());
            } else {
                key = factory.key();
            }
            c = service = new ComponentBuildEntry<>(component, key);
        }
        return c;
    }

    @Override
    public MethodHandle toMethodHandle() {
        if (instance != null) {
            MethodHandle mh = MethodHandles.constant(instance.getClass(), instance);
            return MethodHandles.dropArguments(mh, 0, Region.class); // MethodHandle()T -> MethodHandle(Region)T
        } else if (isPrototype()) { // injectable != null
            MethodHandle mh = injectable.buildMethodHandle();
            return mh;
            // Taenker vi kun bruger den her... Hvis vi har lyst til genbrug
        } else {
            return Region.readSingletonAs(regionIndex, injectable.rawType());
        }
    }
}
