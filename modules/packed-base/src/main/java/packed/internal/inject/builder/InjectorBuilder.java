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
package packed.internal.inject.builder;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import app.packed.bundle.Bundle;
import app.packed.bundle.OldWiringOperation;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorExtension;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.SimpleInjectorConfigurator;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.annotations.AtProvides;
import packed.internal.annotations.AtProvidesGroup;
import packed.internal.box.Box;
import packed.internal.box.BoxServices;
import packed.internal.box.BoxType;
import packed.internal.bundle.BundleSupport;
import packed.internal.classscan.ServiceClassDescriptor;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.InjectSupport;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.runtime.InternalInjector;
import packed.internal.invokable.InternalFunction;

/**
 * A builder of {@link Injector injectors}. Is both used via {@link SimpleInjectorConfigurator}.
 */
public class InjectorBuilder extends AbstractContainerConfiguration {

    boolean autoRequires;

    final Box box;

    /** A list of bundle bindings, as we need to post process the exports. */
    ArrayList<BindInjectorFromBundle> injectorBundleBindings;

    InternalInjector privateInjector;

    /** All nodes that have been added to this builder, even those that are not exposed. */
    ServiceBuildNode<?> privateLatestNode;

    InternalInjector publicInjector;

    @Nullable
    final ArrayList<ServiceBuildNodeExported<?>> publicNodeList;

    /**
     * Creates a new builder.
     * 
     * @param configurationSite
     *            the configuration site
     */
    public InjectorBuilder(InternalConfigurationSite configurationSite) {
        super(configurationSite);
        publicNodeList = null;

        box = new Box(BoxType.INJECTOR_VIA_CONFIGURATOR);

        extensions.put(InjectorExtension.class, new InjectorExtension(this));
    }

    public InjectorBuilder(InternalConfigurationSite configurationSite, Bundle bundle) {
        super(configurationSite, bundle);
        publicNodeList = new ArrayList<>();
        box = new Box(BoxType.INJECTOR_VIA_BUNDLE);

        extensions.put(InjectorExtension.class, new InjectorExtension(this));
    }

    protected final <T> ServiceBuildNode<T> bindNode(ServiceBuildNode<T> node) {
        assert privateLatestNode == null;
        privateLatestNode = node;
        return node;
    }

    public Injector build() {
        freezeLatest();
        freeze();
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
    public final <T> ServiceConfiguration<T> export(Key<T> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        freezeLatest();

        InternalConfigurationSite cs = configurationSite().spawnStack(ConfigurationSiteType.BUNDLE_EXPOSE);

        ServiceNode<T> node = box.services().nodes.getRecursive(key);
        if (node == null) {
            throw new IllegalArgumentException("Cannot expose non existing service, key = " + key);
        }
        ServiceBuildNodeExported<T> bn = new ServiceBuildNodeExported<>(this, cs, node);
        bn.as(key);
        publicNodeList.add(bn);
        bindNode(bn);
        return bn;
    }

    @SuppressWarnings("unchecked")
    public final <T> ServiceConfiguration<T> export(ServiceConfiguration<T> configuration) {
        // Skal skrives lidt om, det her burde virke, f.eks. som export(provide(ddd).asNone).as(String.class)

        return (ServiceConfiguration<T>) export(configuration.getKey());
    }

    protected void freezeLatest() {
        // Skal vi egentlig ikke ogsaa frysse noden????
        if (privateLatestNode != null) {
            Key<?> key = privateLatestNode.key();
            if (key != null) {
                if (privateLatestNode instanceof ServiceBuildNodeExported) {
                    box.services().exports.put(privateLatestNode);
                } else {
                    if (!box.services().nodes.putIfAbsent(privateLatestNode)) {
                        System.err.println("OOPS");
                    }
                }
            }

            privateLatestNode.freeze();
            privateLatestNode = null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> ServiceConfiguration<T> provide(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        checkConfigurable();
        freezeLatest();

        InstantiationMode mode = InstantiationMode.SINGLETON;
        InternalConfigurationSite frame = configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND);
        InternalFunction<T> func = InjectSupport.toInternalFunction(factory);

        ServiceClassDescriptor serviceDesc = accessor.serviceDescriptorFor(func.getReturnTypeRaw());
        ServiceBuildNodeDefault<T> node = new ServiceBuildNodeDefault<>(this, frame, serviceDesc, mode, accessor.readable(func), (List) factory.dependencies());

        scanForProvides(func.getReturnTypeRaw(), node);

        return bindNode(node).as(factory.defaultKey());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> ServiceConfiguration<T> provide(T instance) {
        checkConfigurable();
        freezeLatest();
        ServiceClassDescriptor serviceDesc = accessor.serviceDescriptorFor(instance.getClass());
        ServiceBuildNodeDefault<T> node = new ServiceBuildNodeDefault<>(this, configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND),
                serviceDesc, instance);
        scanForProvides(instance.getClass(), node);
        return bindNode(node).as((Class) instance.getClass());
    }

    public void registerStatics(Class<?> staticsHolder) {
        throw new UnsupportedOperationException();
    }

    protected void scanForProvides(Class<?> type, ServiceBuildNodeDefault<?> owner) {
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

    public final void serviceAutoRequire() {
        autoRequires = true;
    }

    public BoxServices services() {
        return box.services();
    }

    public void wireInjector(Bundle bundle, OldWiringOperation... stages) {
        requireNonNull(bundle, "bundle is null");
        List<OldWiringOperation> listOfStages = BundleSupport.invoke().extractWiringOperations(stages, Bundle.class);
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite cs = configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromBundle is = new BindInjectorFromBundle(this, cs, bundle, listOfStages);
        is.processImport();
        if (injectorBundleBindings == null) {
            injectorBundleBindings = new ArrayList<>(1);
        }
        injectorBundleBindings.add(is);
    }

    public final void wireInjector(Injector injector, OldWiringOperation... operations) {
        requireNonNull(injector, "injector is null");
        List<OldWiringOperation> wiringOperations = BundleSupport.invoke().extractWiringOperations(operations, Bundle.class);
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite cs = configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        WireInjector is = new WireInjector(this, cs, injector, wiringOperations);
        is.importServices();
    }
}
