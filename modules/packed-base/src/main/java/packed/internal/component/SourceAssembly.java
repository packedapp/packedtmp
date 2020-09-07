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
import java.util.List;

import app.packed.base.Key;
import app.packed.component.ComponentModifier;
import packed.internal.component.PackedWireableComponentDriver.SingletonComponentDriver;
import packed.internal.inject.ServiceDependency;
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

    public BuildEntry<?> buildEntry;

    final PackedWireableComponentDriver<?> driver;

    final ComponentNodeConfiguration conf;

    List<ServiceDependency> dependencies;
    // We store the index, but cannot store the instance.. Because
    // The assembly can be used multiple times for initialization.
    public final int singletonIndex;

    MethodHandle mh;

    SourceAssembly(ComponentNodeConfiguration node, PackedWireableComponentDriver<?> driver) {
        this.driver = requireNonNull(driver);
        this.conf = requireNonNull(node);
        List<ServiceDependency> dependencies = null;
        if (driver instanceof SingletonComponentDriver<?>) {
            SingletonComponentDriver<?> d = (SingletonComponentDriver<?>) driver;
            singletonIndex = node.region.reserve();
            if (!hasInstance()) {
                dependencies = d.factory.factory.dependencies;
                mh = d.fromFactory(node);
                System.out.println("DEPENDENCIES " + dependencies);
            }
        } else {
            singletonIndex = -1;
        }
        this.dependencies = dependencies;
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
                ns.store(singletonIndex, instance());
            }
        }
    }

    public BuildEntry<?> provide(ServiceExtensionNode node) {
        BuildEntry<?> c = buildEntry;
        if (c == null) {
            if (hasInstance()) {
                c = new ComponentConstantBuildEntry<>(node, conf);
                c.as((Key) Key.of(conf.driver().sourceType()));
            } else {
                SingletonComponentDriver scd = (SingletonComponentDriver) driver;
                c = new ComponentMethodHandleBuildEntry<>(node, conf, ServiceMode.CONSTANT, mh, dependencies);
                c.as(scd.factory.key());
            }
        }
        return c;
    }

    // Always invoked before other provides....
    public ComponentBuildEntry<?> provideForHooks(ServiceExtensionNode node, AtProvidesHook hook) {
        ComponentBuildEntry entry;
        if (hasInstance()) {
            entry = new ComponentConstantBuildEntry<>(node, conf);
        } else {
            // ServiceMode.constant needs to reflect the driver type
            entry = new ComponentMethodHandleBuildEntry<>(node, conf, ServiceMode.CONSTANT, mh, dependencies);

            // If any of the @Provide methods are instance members the parent node needs special treatment.
            // As it needs to be constructed, before the field or method can provide services.

            // HMMM, we need to provide this to constructor I think...
            ((ComponentMethodHandleBuildEntry) entry).hasInstanceMembers = hook.hasInstanceMembers;
        }
        // Set the parent node, so it can be found from provideFactory or provideInstance
        buildEntry = entry;
        return entry;

    }

    public BuildEntry<?> providePrototype(ServiceExtensionNode node) {
        SingletonComponentDriver scd = (SingletonComponentDriver) driver;
        BuildEntry<?> c = buildEntry;
        if (c == null) {
            c = new ComponentMethodHandleBuildEntry<>(node, conf, ServiceMode.PROTOTYPE, mh, dependencies);
        }
        c.as(scd.factory.key());
        return c;
    }
}
