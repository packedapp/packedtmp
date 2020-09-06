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

import java.util.List;

import app.packed.base.Key;
import app.packed.component.ComponentModifier;
import packed.internal.component.PackedWireableComponentDriver.SingletonComponentDriver;
import packed.internal.inject.ServiceDependency;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.buildtime.service.AbstractComponentBuildEntry;
import packed.internal.service.buildtime.service.AtProvidesHook;
import packed.internal.service.buildtime.service.ComponentConstantBuildEntry;
import packed.internal.service.buildtime.service.ComponentFactoryBuildEntry;

/**
 * All components that have a {@link ComponentModifier#SOURCED} modifier has an instance of this class.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SourceAssembly {

    final PackedWireableComponentDriver<?> driver;

    public BuildEntry<?> buildEntry;

    SourceAssembly(PackedWireableComponentDriver<?> driver) {
        this.driver = driver;
    }

    public boolean hasInstance() {
        if (driver instanceof SingletonComponentDriver<?>) {
            SingletonComponentDriver<?> d = (SingletonComponentDriver<?>) driver;
            return d.instance != null;
        }
        return false;
    }

    public Object instance() {
        if (driver instanceof SingletonComponentDriver<?>) {
            SingletonComponentDriver<?> d = (SingletonComponentDriver<?>) driver;
            if (d.instance != null) {
                return d.instance;
            }
        }
        throw new IllegalStateException();
    }

    // Always invoked before other provides....
    public AbstractComponentBuildEntry<?> provideForHooks(ServiceExtensionNode node, AtProvidesHook hook, ComponentNodeConfiguration cc) {
        AbstractComponentBuildEntry entry;
        if (hasInstance()) {
            entry = new ComponentConstantBuildEntry<>(node, cc);
        } else {
            SingletonComponentDriver driver = (SingletonComponentDriver) cc.driver();
            BaseFactory<?> factory = driver.factory;
            List<ServiceDependency> dependencies = factory.factory.dependencies;
            entry = new ComponentFactoryBuildEntry<>(node, cc, ServiceMode.CONSTANT, driver.fromFactory(cc), dependencies, false);

            // If any of the @Provide methods are instance members the parent node needs special treatment.
            // As it needs to be constructed, before the field or method can provide services.
            ((ComponentFactoryBuildEntry) entry).hasInstanceMembers = hook.hasInstanceMembers;
        }
        // Set the parent node, so it can be found from provideFactory or provideInstance
        buildEntry = entry;
        return entry;

    }

    public BuildEntry<?> providePrototype(ServiceExtensionNode node, ComponentNodeConfiguration cc) {
        SingletonComponentDriver scd = (SingletonComponentDriver) driver;
        BuildEntry<?> c = buildEntry;
        if (c == null) {
            List<ServiceDependency> dependencies = scd.factory.factory.dependencies;
            c = new ComponentFactoryBuildEntry<>(node, cc, ServiceMode.CONSTANT, scd.fromFactory(cc), (List) dependencies, true);
        }
        ComponentFactoryBuildEntry e = (ComponentFactoryBuildEntry) c;
        // Vi burde kunne styre det
        e.prototype();
        c.as(scd.factory.key());
        return c;
    }

    public BuildEntry<?> provide(ServiceExtensionNode node, ComponentNodeConfiguration cc) {
        if (hasInstance()) {
            return provideInstance(node, cc);
        } else {
            return provideFactory(node, cc);
        }
    }

    private BuildEntry<?> provideFactory(ServiceExtensionNode node, ComponentNodeConfiguration cc) {
        SingletonComponentDriver scd = (SingletonComponentDriver) driver;
        BuildEntry<?> c = buildEntry;
        if (c == null) {
            List<ServiceDependency> dependencies = scd.factory.factory.dependencies;
            c = new ComponentFactoryBuildEntry<>(node, cc, ServiceMode.CONSTANT, scd.fromFactory(cc), (List) dependencies, false);
        }
        c.as(scd.factory.key());
        return c;
    }

    private BuildEntry<?> provideInstance(ServiceExtensionNode node, ComponentNodeConfiguration cc) {
        // First see if we have already installed the node. This happens in #set if the component container any members
        // annotated with @Provides

        BuildEntry<?> c = buildEntry;
        if (c == null) {
            // No node found, components has no @Provides method, create a new node
            buildEntry = c = new ComponentConstantBuildEntry<>(node, cc);
        }

        c.as((Key) Key.of(cc.driver().sourceType()));
        return c;
    }
}
