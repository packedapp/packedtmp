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
import app.packed.component.ComponentModifier;
import packed.internal.component.OldPackedComponentDriver.SingletonComponentDriver;
import packed.internal.inject.resolvable.DependencyProvider;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.service.PrototypeBuildEntry;
import packed.internal.service.buildtime.service.SingletonBuildEntry;

/**
 * All components that have a {@link ComponentModifier#SOURCED} modifier has an instance of this class.
 */

// Maaske har vi en abstract SourceAssembly.. og Saa SingletonSourceAssembly
public class SourceAssembly implements DependencyProvider {

    /** The component this source is a part of. */
    public final ComponentNodeConfiguration component;

    /** The driver of this source. */
    public final SingletonComponentDriver<?> driver;

    /** Non-null if the component needs injection (not a constant). */
    final Injectable injectable;

    /** If the source represents an instance. */
    final Object instance;

    /** The index at which to store the source instance, or -1 if it should not be stored. */
    public final int regionIndex;

    /** Whether or not the component is provided as a service. */
    public BuildtimeService<?> service;

    SourceAssembly(ComponentNodeConfiguration component) {
        this(component, (SingletonComponentDriver<?>) component.driver);
    }

    SourceAssembly(ComponentNodeConfiguration component, SingletonComponentDriver<?> driver) {
        this.component = requireNonNull(component);
        this.driver = requireNonNull(driver);

        RegionAssembly region = component.region;
        this.regionIndex = region.reserve(); // prototype false
        this.instance = driver.instance;
        if (instance == null) {
            this.injectable = Injectable.ofFactory(this);
            region.resolver.sourceInjectables.add(this);
            region.resolver.allInjectables.add(injectable);
        } else {
            this.injectable = null;
            region.resolver.sourceConstants.add(this);
        }
    }

    private Key<?> defaultServiceKey() {
        if (instance != null) {
            return Key.of(component.driver().sourceType());
        } else {
            return driver.factory.key();
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
            c = new SingletonBuildEntry<>(component, defaultServiceKey());
        }
        return c;
    }

    public BuildtimeService<?> providePrototype() {
        BuildtimeService<?> c = service;
        if (c == null) {
            c = new PrototypeBuildEntry<>(component, defaultServiceKey());
        }
        return c;
    }

    @Override
    public MethodHandle toMethodHandle() {
        if (instance != null) {
            MethodHandle mh = MethodHandles.constant(instance.getClass(), instance);
            return MethodHandles.dropArguments(mh, 0, Region.class); // MethodHandle()T -> MethodHandle(Region)T
        } else { // injectable != null
            // Taenker vi kun bruger den her... Hvis vi har lyst til genbrug
            return Region.readSingletonAs(regionIndex, injectable.rawType());
        }
    }
}
