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

    public BuildEntry<?> buildEntry;

    final PackedWireableComponentDriver<?> driver;

    final ComponentNodeConfiguration conf;

    // We store the index, but cannot store the instance.. Because
    // The assembly can be used multiple times for initialization.
    int singletonIndex;

    SourceAssembly(ComponentNodeConfiguration node, PackedWireableComponentDriver<?> driver) {
        this.driver = requireNonNull(driver);
        this.conf = requireNonNull(node);
        if (driver instanceof SingletonComponentDriver<?>) {
            singletonIndex = node.region.reserve();
        } else {
            singletonIndex = -1;
        }
    }

    boolean hasInstance() {
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

    public void initSource(Region ns) {
        if (singletonIndex >= 0) {
            if (hasInstance()) {
                ns.store[singletonIndex] = instance();
            }
        }
    }

    public BuildEntry<?> provide(ServiceExtensionNode node) {
        if (hasInstance()) {
            // First see if we have already installed the node. This happens in #set if the component container any members
            // annotated with @Provides

            BuildEntry<?> c = buildEntry;
            if (c == null) {
                // No node found, components has no @Provides method, create a new node
                buildEntry = c = new ComponentConstantBuildEntry<>(node, conf);
            }

            c.as((Key) Key.of(conf.driver().sourceType()));
            return c;
        } else {
            SingletonComponentDriver scd = (SingletonComponentDriver) driver;
            BuildEntry<?> c = buildEntry;
            if (c == null) {
                List<ServiceDependency> dependencies = scd.factory.factory.dependencies;
                c = new ComponentFactoryBuildEntry<>(node, conf, ServiceMode.CONSTANT, scd.fromFactory(conf), (List) dependencies, false);
            }
            c.as(scd.factory.key());
            return c;
        }
    }

    // Always invoked before other provides....
    public AbstractComponentBuildEntry<?> provideForHooks(ServiceExtensionNode node, AtProvidesHook hook) {
        AbstractComponentBuildEntry entry;
        if (hasInstance()) {
            entry = new ComponentConstantBuildEntry<>(node, conf);
        } else {
            SingletonComponentDriver driver = (SingletonComponentDriver) conf.driver();
            BaseFactory<?> factory = driver.factory;
            List<ServiceDependency> dependencies = factory.factory.dependencies;
            entry = new ComponentFactoryBuildEntry<>(node, conf, ServiceMode.CONSTANT, driver.fromFactory(conf), dependencies, false);

            // If any of the @Provide methods are instance members the parent node needs special treatment.
            // As it needs to be constructed, before the field or method can provide services.
            ((ComponentFactoryBuildEntry) entry).hasInstanceMembers = hook.hasInstanceMembers;
        }
        // Set the parent node, so it can be found from provideFactory or provideInstance
        buildEntry = entry;
        return entry;

    }

    public BuildEntry<?> providePrototype(ServiceExtensionNode node) {
        SingletonComponentDriver scd = (SingletonComponentDriver) driver;
        BuildEntry<?> c = buildEntry;
        if (c == null) {
            List<ServiceDependency> dependencies = scd.factory.factory.dependencies;
            c = new ComponentFactoryBuildEntry<>(node, conf, ServiceMode.CONSTANT, scd.fromFactory(conf), (List) dependencies, true);
        }
        ComponentFactoryBuildEntry e = (ComponentFactoryBuildEntry) c;
        // Vi burde kunne styre det
        e.prototype();
        c.as(scd.factory.key());
        return c;
    }
}
