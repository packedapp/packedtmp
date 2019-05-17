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
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import app.packed.bundle.AnyBundle;
import app.packed.bundle.Bundle;
import app.packed.bundle.BundleLink;
import app.packed.bundle.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.Container;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Install;
import app.packed.inject.ExportedServiceConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.annotations.AtProvides;
import packed.internal.annotations.AtProvidesGroup;
import packed.internal.box.Box;
import packed.internal.box.BoxServices;
import packed.internal.box.BoxType;
import packed.internal.bundle.AppPackedBundleSupport;
import packed.internal.classscan.ServiceClassDescriptor;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.container.InternalContainer;
import packed.internal.inject.AppPackedInjectSupport;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.runtime.InternalInjector;
import packed.internal.invokable.InternalFunction;

/**
 * A builder of {@link Injector injectors}. Is both used via {@link InjectorConfigurator}.
 */
public class ContainerBuilder extends DefaultContainerConfiguration {

    boolean autoRequires = true;

    final Box box;

    /** A list of bundle bindings, as we need to post process the exports. */
    ArrayList<BindInjectorFromBundle> injectorBundleBindings = new ArrayList<>();

    InternalInjector privateInjector;

    InternalInjector publicInjector;

    @Nullable
    final ArrayList<BuildtimeServiceNodeExported<?>> exportedNodes = new ArrayList<>();

    public ContainerBuilder(InternalConfigurationSite configurationSite, @Nullable AnyBundle bundle, Wirelet... options) {
        super(configurationSite, bundle, options);
        box = new Box(BoxType.INJECTOR_VIA_BUNDLE);
    }

    protected final <T extends AbstractFreezableNode> T bindNode(T node) {
        // Bliver en protected method paa en extension...
        assert currentNode == null;
        currentNode = node;
        return node;
    }

    public Container build() {
        if (bundle != null) {
            if (bundle.getClass().isAnnotationPresent(Install.class)) {
                install(bundle);
            }
            bundle.doConfigure(this);
        }

        for (Wirelet wo : options) {
            AppPackedBundleSupport.invoke().process(wo, new BundleLink() {

                @Override
                public ContainerConfiguration cc() {
                    return ContainerBuilder.this;
                }

                @Override
                public Class<? extends AnyBundle> childType() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public ConfigSite configSite() {
                    return ContainerBuilder.this.configurationSite();
                }

                @Override
                public Mode mode() {
                    throw new UnsupportedOperationException();
                }
            });
        }

        InternalContainer container = new InternalContainer(this, buildInjector());
        System.out.println("Components " + components.keySet());

        return container;
    }

    public Injector buildInjector() {
        newOperation();
        new DependencyGraph(this).instantiate();
        return publicInjector;
    }

    /**
     * Exposes the specified key as a service.
     * 
     * @param key
     *            the key of the service that should be exposed
     * @return a configuration for the exposed service
     */
    public final <T> ExportedServiceConfiguration<T> export(Key<T> key) {
        requireNonNull(key, "key is null");
        newOperation();

        InternalConfigurationSite cs = configurationSite().spawnStack(ConfigurationSiteType.BUNDLE_EXPOSE);

        ServiceNode<T> node = box.services().nodes.getRecursive(key);
        if (node == null) {
            throw new IllegalArgumentException("Cannot expose non existing service, key = " + key);
        }
        BuildtimeServiceNodeExported<T> bn = new BuildtimeServiceNodeExported<>(this, cs, node);
        bn.as(key);
        exportedNodes.add(bn);

        return bindNode(new DefaultExportedServiceConfiguration<>(bn));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> ServiceConfiguration<T> provide(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        newOperation();

        InstantiationMode mode = InstantiationMode.SINGLETON;

        InternalConfigurationSite frame = configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND);
        InternalFunction<T> func = AppPackedInjectSupport.toInternalFunction(factory);
        ServiceClassDescriptor desc = accessor.serviceDescriptorFor(func.getReturnTypeRaw());

        BuildtimeServiceNodeDefault<T> node = new BuildtimeServiceNodeDefault<>(this, frame, desc, mode, accessor.readable(func),
                (List) factory.dependencies());

        scanForProvides(func.getReturnTypeRaw(), node);
        node.as(factory.defaultKey());
        return bindNode(new DefaultServiceConfiguration<>(new ComponentBuildNode(frame, this), node));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ServiceConfiguration<T> provide(T instance) {
        requireNonNull(instance, "instance is null");
        newOperation();

        InternalConfigurationSite frame = configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND);
        ServiceClassDescriptor sdesc = accessor.serviceDescriptorFor(instance.getClass());

        BuildtimeServiceNodeDefault<T> sc = new BuildtimeServiceNodeDefault<T>(this, frame, sdesc, instance);

        scanForProvides(instance.getClass(), sc);
        sc.as((Key) Key.of(instance.getClass()));

        return bindNode(new DefaultServiceConfiguration<>(new ComponentBuildNode(frame, this), sc));
    }

    protected void scanForProvides(Class<?> type, BuildtimeServiceNodeDefault<?> owner) {
        AtProvidesGroup provides = accessor.serviceDescriptorFor(type).provides;
        if (!provides.members.isEmpty()) {
            owner.hasInstanceMembers = provides.hasInstanceMembers;
            // if (owner.instantiationMode() == InstantiationMode.PROTOTYPE && provides.hasInstanceMembers) {
            // throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as
            // prototypes");
            // }

            // First check that we do not have existing services with any of the provided keys
            for (Key<?> k : provides.members.keySet()) {
                if (box.services().nodes.containsKey(k)) {
                    throw new IllegalArgumentException("At service with key " + k + " has already been registered");
                }
            }

            // AtProvidesGroup has already validated that the specified type does not have any members that provide services with
            // the same key, so we can just add them now without any verification
            for (AtProvides member : provides.members.values()) {
                box.services().nodes.put(owner.provide(member));// put them directly
            }
        }
    }

    public void serviceManualRequirements() {
        autoRequires = false;
    }

    public BoxServices services() {
        return box.services();
    }

    public void wireInjector(Bundle bundle, Wirelet... stages) {

        requireNonNull(bundle, "bundle is null");
        List<Wirelet> listOfStages = AppPackedBundleSupport.invoke().extractWiringOperations(stages, Bundle.class);
        newOperation();
        InternalConfigurationSite cs = configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromBundle is = new BindInjectorFromBundle(this, cs, bundle, listOfStages);
        is.processImport();
        injectorBundleBindings.add(is);
    }

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
