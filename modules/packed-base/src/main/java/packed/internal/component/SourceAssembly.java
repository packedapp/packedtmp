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

import app.packed.base.Key;
import app.packed.component.ComponentModifier;
import packed.internal.component.PackedWireableComponentDriver.SingletonComponentDriver;
import packed.internal.inject.resolvable.DependencyProvider;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.inject.resolvable.ResolvableFactory;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.buildtime.service.AtProvidesHook;
import packed.internal.service.buildtime.service.ComponentBuildEntry;
import packed.internal.service.buildtime.service.ComponentConstantBuildEntry;
import packed.internal.service.buildtime.service.ComponentMethodHandleBuildEntry;

/**
 * All components that have a {@link ComponentModifier#SOURCED} modifier has an instance of this class.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SourceAssembly {

    /** The component the source belongs to. */
    public final ComponentNodeConfiguration component;

    /** If the source represents a constant. */
    private final Object constant;

    private DependencyProvider dependencyProvider;

    /** The driver of this source. */
    public final SingletonComponentDriver<?> driver;

    /** Non-null if the component needs injection (not a constant). */
    public final Injectable injectable;

    MethodHandle mh;

    /** The index at which to store the source instance, or -1 if it should not be stored. */
    public final int regionIndex;

    final ResolvableFactory resolvable;

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
        this.resolvable = null;
    }

    public void close() {
        if (resolvable != null && service == null) {

        }
    }

    boolean hasInstance() {
        return constant != null;
    }

    public Object instance() {
        return requireNonNull(constant);
    }

    public DependencyProvider instanceAsDependencyProvider() {
        DependencyProvider d = dependencyProvider;
        if (d == null) {
            d = dependencyProvider = DependencyProvider.provideSingleton(this);
        }
        return d;
    }

    // Bliver kaldt naar man koere provide();
    public BuildEntry<?> provide(ServiceExtensionNode services) {
        BuildEntry<?> c = service;
        if (c == null) {
            if (hasInstance()) {
                c = new ComponentConstantBuildEntry<>(services, component);
                c.as((Key) Key.of(component.driver().sourceType()));
            } else {
                SingletonComponentDriver scd = driver;
                c = new ComponentMethodHandleBuildEntry<>(services, component, resolvable, ServiceMode.CONSTANT);
                c.as(scd.factory.key());
            }
        }
        return c;
    }

    // Always invoked before other provides....
    public ComponentBuildEntry<?> provideForHooks(ServiceExtensionNode services, AtProvidesHook hook) {
        ComponentBuildEntry entry;
        if (hasInstance()) {
            entry = new ComponentConstantBuildEntry<>(services, component);
        } else {
            // ServiceMode.constant needs to reflect the driver type
            entry = new ComponentMethodHandleBuildEntry<>(services, component, resolvable, ServiceMode.CONSTANT);

            // If any of the @Provide methods are instance members the parent node needs special treatment.
            // As it needs to be constructed, before the field or method can provide services.

            // HMMM, we need to provide this to constructor I think...
            ((ComponentMethodHandleBuildEntry) entry).hasInstanceMembers = hook.hasInstanceMembers;
        }
        // Set the parent node, so it can be found from provideFactory or provideInstance
        service = entry;
        return entry;

    }

    public BuildEntry<?> providePrototype(ServiceExtensionNode services) {
        SingletonComponentDriver scd = driver;
        BuildEntry<?> c = service;
        if (c == null) {
            c = new ComponentMethodHandleBuildEntry<>(services, component, resolvable, ServiceMode.PROTOTYPE);
        }
        c.as(scd.factory.key());
        return c;
    }
}
