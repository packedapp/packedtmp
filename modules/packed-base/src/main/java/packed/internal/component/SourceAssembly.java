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
import packed.internal.component.PackedWireableComponentDriver.SingletonComponentDriver;
import packed.internal.inject.resolvable.DependencyProvider;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.InjectionManager;
import packed.internal.service.buildtime.service.SingletonBuildEntry;

/**
 * All components that have a {@link ComponentModifier#SOURCED} modifier has an instance of this class.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })

// Maaske har vi en abstract SourceAssembly.. og Saa SingletonSourceAssembly

public class SourceAssembly implements DependencyProvider {

    /** The component the source belongs to. */
    public final ComponentNodeConfiguration component;

    /** If the source represents a constant. */
    private final Object constant;

    /** The driver of this source. */
    public final SingletonComponentDriver<?> driver;

    /** Non-null if the component needs injection (not a constant). */
    final Injectable injectable;

    MethodHandle mh;

    /** The index at which to store the source instance, or -1 if it should not be stored. */
    public final int regionIndex;

    /** Whether or not the component is provided as a service. */
    public BuildEntry<?> service;

    SourceAssembly(ComponentNodeConfiguration component) {
        this(component, (SingletonComponentDriver<?>) component.driver);
    }

    SourceAssembly(ComponentNodeConfiguration component, SingletonComponentDriver<?> driver) {
        this.component = requireNonNull(component);
        this.driver = requireNonNull(driver);
        this.regionIndex = component.region.reserve(); // prototype false
        this.constant = driver.instance;
        if (constant == null) {
            this.injectable = Injectable.ofFactory(this);
            component.region.resolver.sourceInjectables.add(injectable);
        } else {
            this.injectable = null;
            component.region.resolver.sourceConstants.add(this);
        }
    }

    Object instance() {
        return requireNonNull(constant);
    }

    // Bliver kaldt naar man koere provide();
    public BuildEntry<?> provide(InjectionManager services) {
        // Not sure we should allow for calling provide multiple times...
        BuildEntry<?> c = service;
        if (c == null) {
            c = new SingletonBuildEntry<>(services, this);
            if (constant != null) {
                c.as((Key) Key.of(component.driver().sourceType()));
            } else {
                SingletonComponentDriver scd = driver;
                c.as(scd.factory.key());
            }
            // service = c;
        }
        return c;
    }

    public BuildEntry<?> providePrototype(InjectionManager services) {
        SingletonComponentDriver scd = driver;
        BuildEntry<?> c = service;
        if (c == null) {
            throw new UnsupportedOperationException();
            // c = new ComponentMethodHandleBuildEntry<>(services, component, resolvable, ServiceMode.PROTOTYPE);
        }
        c.as(scd.factory.key());
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public Injectable injectable() {
        return injectable;
    }

    @Override
    public MethodHandle toMethodHandle() {
        if (constant != null) {
            MethodHandle mh = MethodHandles.constant(constant.getClass(), constant);
            return MethodHandles.dropArguments(mh, 0, Region.class); // MethodHandle()T -> MethodHandle(Region)T
        } else { // injectable != null
            // Taenker vi kun bruger den her... Hvis vi har lyst til genbrug
            return Region.readSingletonAs(regionIndex, injectable.rawType());
        }
    }
}
