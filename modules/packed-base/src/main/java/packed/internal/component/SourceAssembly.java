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
import java.util.List;

import app.packed.base.Key;
import app.packed.component.ComponentModifier;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.factory.FactoryHandle;
import packed.internal.inject.resolvable.DependencyProvider;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.inject.resolvable.ServiceDependency;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.service.SourceBuildEntry;

/**
 * All components that have a {@link ComponentModifier#SOURCED} modifier has an instance of this class.
 */
// Maaske har vi en abstract SourceAssembly.. og Saa SingletonSourceAssembly
public class SourceAssembly implements DependencyProvider {

    /** The component this source is a part of. */
    public final ComponentNodeConfiguration component;

    /** Non-null if the component needs injection (not a constant). */
    final Injectable injectable;

    /** If the source represents an instance. */
    final Object instance;

    /** The index at which to store the source instance, or -1 if it should not be stored. */
    public final int regionIndex;

    /** Whether or not the component is provided as a service. */
    public BuildtimeService<?> service;

    private final BaseFactory<?> factory;

    final ComponentModel cm;

    SourceAssembly(ComponentNodeConfiguration component, ComponentModel cm, Object instance) {
        this.component = requireNonNull(component);
        this.cm = requireNonNull(cm);

        this.instance = requireNonNull(instance);
        RegionAssembly region = component.region;
        this.regionIndex = region.reserve(); // prototype false
        this.injectable = null;
        region.resolver.sourceConstants.add(this);
        this.factory = null;
    }

    SourceAssembly(ComponentNodeConfiguration component, ComponentModel cm, BaseFactory<?> factory) {
        this.component = requireNonNull(component);
        this.cm = requireNonNull(cm);
        RegionAssembly region = component.region;

        if (isPrototype()) {
            this.regionIndex = -1;
        } else {
            this.regionIndex = region.reserve(); // prototype false
        }

        this.instance = null;
        this.factory = factory;
        if (!component.modifiers().isStateless()) {
            this.injectable = Injectable.ofFactory(this);
            region.resolver.sourceInjectables.add(this);
            region.resolver.allInjectables.add(injectable);
        } else {
            this.injectable = null;
        }
    }

    public boolean isPrototype() {
        return !component.modifiers().isSingleton();
    }

    private Key<?> defaultServiceKey() {
        if (instance != null) {
            return Key.of(cm.type());
        } else {
            return factory.key();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Injectable injectable() {
        return injectable;
    }

    // Bliver kaldt naar man koere provide();
    public BuildtimeService<?> provide() {
        // Not sure we should allow for calling provide multiple times...
        BuildtimeService<?> c = service;
        if (c == null) {
            c = new SourceBuildEntry<>(component, defaultServiceKey());
        }
        return c;
    }

    public MethodHandle fromFactory() {
        FactoryHandle<?> handle = factory.factory.handle;
        return component.realm().fromFactoryHandle(handle);
    }

    public List<ServiceDependency> dependencies() {
        return factory.factory.dependencies;
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
