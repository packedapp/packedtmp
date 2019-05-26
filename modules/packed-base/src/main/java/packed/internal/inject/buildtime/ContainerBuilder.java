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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import app.packed.component.Install;
import app.packed.container.AnyBundle;
import app.packed.container.Bundle;
import app.packed.container.Container;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.util.Nullable;
import packed.internal.bundle.WireletList;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.container.InternalContainer;
import packed.internal.inject.Box;
import packed.internal.inject.BoxType;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.runtime.InternalInjector;

/**
 * A builder of {@link Injector injectors}. Is both used via {@link InjectorConfigurator}.
 */
public class ContainerBuilder extends DefaultContainerConfiguration {

    boolean autoRequires = true;

    public final Box box;

    public final ArrayList<BuildtimeServiceNodeExported<?>> exportedNodes = new ArrayList<>();

    /** A list of bundle bindings, as we need to post process the exports. */
    ArrayList<BindInjectorFromBundle> injectorBundleBindings = new ArrayList<>();

    InternalInjector privateInjector;

    InternalInjector publicInjector;

    public ContainerBuilder(InternalConfigurationSite configurationSite, @Nullable AnyBundle bundle, Wirelet... options) {
        super(configurationSite, bundle, options);
        box = new Box(BoxType.INJECTOR_VIA_BUNDLE);
    }

    public Container build() {
        if (bundle != null) {
            if (bundle.getClass().isAnnotationPresent(Install.class)) {
                install(bundle);
            }
            bundle.doConfigure(this);
        }

        InternalContainer container = new InternalContainer(this, buildInjector());
        return container;
    }

    public Injector buildInjector() {
        finish();
        new DependencyGraph(this).instantiate();
        return publicInjector;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void finish() {
        for (Extension<?> e : extensions.values()) {
            e.onFinish();
        }
        for (BuildtimeServiceNodeExported<?> e : exportedNodes) {
            ServiceNode<?> sn = box.services().nodes.getRecursive(e.getKey());
            if (sn == null) {
                throw new IllegalStateException("Could not find node to export " + e.getKey());
            }
            e.exposureOf = (ServiceNode) sn;
            box.services().exports.put(e);
        }
    }

    public void link(Bundle bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");
        WireletList wl = WireletList.of(wirelets);
        checkConfigurable();
        // Look in wirelets for explicit disabled/enabled stack spawn, otherwise ask parent
        InternalConfigurationSite cs = configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromBundle is = new BindInjectorFromBundle(this, cs, bundle, wl);
        is.processImport();
        injectorBundleBindings.add(is);
    }

    public void disableAutomaticRequirements() {
        autoRequires = false;
    }

    /** Small for utility class for generate a best effort unique name for containers. */
    static class InternalContainerNameGenerator {

        /** Assigns unique IDs, starting with 1 when lazy naming containers. */
        private static final AtomicLong ANONYMOUS_ID = new AtomicLong();

        private static final ClassValue<Supplier<String>> BUNDLE_NAME_SUPPLIER = new ClassValue<>() {
            private final AtomicLong L = new AtomicLong();

            @Override
            protected Supplier<String> computeValue(Class<?> type) {
                String simpleName = type.getSimpleName();
                String s = simpleName.endsWith("Bundle") && simpleName.length() > 6 ? simpleName.substring(simpleName.length() - 6) : simpleName;
                return () -> s + L.incrementAndGet();
            }
        };

        static String fromBundleType(Class<? extends Bundle> cl) {
            return BUNDLE_NAME_SUPPLIER.get(cl).get();
        }

        static String next() {
            return "Container" + ANONYMOUS_ID.incrementAndGet();
        }
    }
}

// public void scanForProvides(Class<?> type, BuildtimeServiceNodeDefault<?> owner) {
// AtProvidesGroup provides = accessor.serviceDescriptorFor(type).provides;
// if (!provides.members.isEmpty()) {
// owner.hasInstanceMembers = provides.hasInstanceMembers;
// // if (owner.instantiationMode() == InstantiationMode.PROTOTYPE && provides.hasInstanceMembers) {
// // throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as
// // prototypes");
// // }
//
// // First check that we do not have existing services with any of the provided keys
// for (Key<?> k : provides.members.keySet()) {
// if (box.services().nodes.containsKey(k)) {
// throw new IllegalArgumentException("At service with key " + k + " has already been registered");
// }
// }
//
// // AtProvidesGroup has already validated that the specified type does not have any members that provide services with
// // the same key, so we can just add them now without any verification
// for (AtProvides member : provides.members.values()) {
// box.services().nodes.put(owner.provide(member));// put them directly
// }
// }
// }

// if (root != null) {
// if (root.name == null) {
// root.name = root.descriptor().simpleName;
// }
//
// // initialize component instance array and set component names.
// root.forEachRecursively(cc -> {
// cc.instances = new Object[1 + (cc.mixins == null ? 0 : cc.mixins.size())];
// // Create a name for all children where no name have been defined
// if (cc.children != null && (cc.childrenExplicitNamed == null || cc.children.size() !=
// cc.childrenExplicitNamed.size())) {
// if (cc.childrenExplicitNamed == null) {
// cc.childrenExplicitNamed = new HashMap<>(cc.children.size());
// }
// for (InternalComponentConfiguration<?> child : cc.children) {
// String name = child.descriptor().simpleName;
// AtomicLong al = autoGeneratedComponentNames.computeIfAbsent(name, ignore -> new AtomicLong());
// String newName;
// do {
// long l = al.getAndIncrement();
// newName = l == 0 ? name : name + l;
// } while (cc.childrenExplicitNamed.putIfAbsent(newName, child) != null);
// child.name = newName;
// }
// }
// });
// }
