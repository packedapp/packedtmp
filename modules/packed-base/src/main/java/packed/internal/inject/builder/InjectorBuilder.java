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
import app.packed.bundle.WiringOperation;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;
import packed.internal.annotations.AtProvides;
import packed.internal.annotations.AtProvidesGroup;
import packed.internal.box.Box;
import packed.internal.box.BoxSource;
import packed.internal.bundle.BundleSupport;
import packed.internal.classscan.ServiceClassDescriptor;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.InjectSupport;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.runtime.InternalInjector;
import packed.internal.invokers.InternalFunction;
import packed.internal.runtime.ImageBuilder;

/**
 * A builder of {@link Injector injectors}. Is both used via {@link InjectorBundle} and {@link InjectorConfigurator}.
 */
public class InjectorBuilder extends ImageBuilder implements InjectorConfigurator {

    boolean autoRequires;

    final Box box;

    /** A list of bundle bindings, as we need to post process the exports. */
    ArrayList<BindInjectorFromBundle> injectorBundleBindings;

    InternalInjector privateInjector;

    /** All nodes that have been added to this builder, even those that are not exposed. */
    ServiceBuildNode<?> privateLatestNode;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    final ServiceNodeMap privateNodeMap;

    InternalInjector publicInjector;

    @Nullable
    final ArrayList<ServiceBuildNodeExposed<?>> publicNodeList;

    /** The runtime nodes that will be available in the injector. */
    final ServiceNodeMap publicNodeMap;

    /**
     * Creates a new builder.
     * 
     * @param configurationSite
     *            the configuration site
     */
    public InjectorBuilder(InternalConfigurationSite configurationSite) {
        super(configurationSite);
        publicNodeMap = privateNodeMap = new ServiceNodeMap();
        publicNodeList = null;
        box = new Box(BoxSource.INJECTOR_VIA_CONFIGURATOR);
    }

    public InjectorBuilder(InternalConfigurationSite configurationSite, Bundle bundle) {
        super(configurationSite, bundle);
        publicNodeMap = new ServiceNodeMap();
        privateNodeMap = new ServiceNodeMap();
        publicNodeList = new ArrayList<>();
        box = new Box(BoxSource.INJECTOR_VIA_BUNDLE);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final <T> ServiceConfiguration<T> bindFactory(InstantiationMode mode, Factory<T> factory) {
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite frame = configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND);
        InternalFunction<T> func = InjectSupport.toInternalFunction(factory);

        ServiceClassDescriptor serviceDesc = accessor.serviceDescriptorFor(func.getReturnTypeRaw());
        ServiceBuildNodeDefault<T> node = new ServiceBuildNodeDefault<>(this, frame, serviceDesc, mode, accessor.readable(func), (List) factory.dependencies());

        scanForProvides(func.getReturnTypeRaw(), node);

        return bindNode(node).as(factory.defaultKey());
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
    public final <T> ServiceConfiguration<T> expose(Class<T> key) {
        return expose(Key.of(key));
    }

    public final <T> ServiceConfiguration<T> expose(Key<T> key) {
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite cs = configurationSite().spawnStack(ConfigurationSiteType.BUNDLE_EXPOSE);

        ServiceNode<T> node = privateNodeMap.getRecursive(key);
        if (node == null) {
            throw new IllegalArgumentException("Cannot expose non existing service, key = " + key);
        }
        ServiceBuildNodeExposed<T> bn = new ServiceBuildNodeExposed<>(this, cs, node);
        bn.as(key);
        publicNodeList.add(bn);
        return bn;
    }

    @SuppressWarnings("unchecked")
    public final <T> ServiceConfiguration<T> expose(ServiceConfiguration<T> configuration) {
        checkConfigurable();
        freezeLatest();
        return (ServiceConfiguration<T>) expose(configuration.getKey());
    }

    protected void freezeLatest() {
        // Skal vi egentlig ikke ogsaa frysse noden????
        if (privateLatestNode != null) {
            Key<?> key = privateLatestNode.key();
            if (key != null) {
                if (!privateNodeMap.putIfAbsent(privateLatestNode)) {
                    System.err.println("OOPS");
                }
            }
            privateLatestNode.freeze();
            privateLatestNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> provide(Class<T> implementation) {
        return bindFactory(InstantiationMode.SINGLETON, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> provide(Factory<T> factory) {
        return bindFactory(InstantiationMode.SINGLETON, requireNonNull(factory, "factory is null"));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> ServiceConfiguration<T> provide(T instance) {
        requireNonNull(instance, "instance is null");
        checkConfigurable();
        freezeLatest();
        ServiceClassDescriptor serviceDesc = accessor.serviceDescriptorFor(instance.getClass());
        ServiceBuildNodeDefault<T> node = new ServiceBuildNodeDefault<>(this, configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND),
                serviceDesc, instance);
        scanForProvides(instance.getClass(), node);
        return bindNode(node).as((Class) instance.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> provide(TypeLiteral<T> implementation) {
        return bindFactory(InstantiationMode.SINGLETON, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> providePrototype(Class<T> implementation) {
        return bindFactory(InstantiationMode.PROTOTYPE, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> providePrototype(Factory<T> factory) {
        return bindFactory(InstantiationMode.PROTOTYPE, requireNonNull(factory, "factory is null"));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> providePrototype(TypeLiteral<T> implementation) {
        return bindFactory(InstantiationMode.PROTOTYPE, Factory.findInjectable(implementation));
    }

    protected void scanForProvides(Class<?> type, ServiceBuildNodeDefault<?> owner) {
        AtProvidesGroup provides = accessor.serviceDescriptorFor(type).provides;
        if (!provides.members.isEmpty()) {
            if (owner.instantiationMode() == InstantiationMode.PROTOTYPE && provides.hasInstanceMembers) {
                throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as prototypes");
            }

            // First check that we do not have existing services with any of the provided keys
            for (Key<?> k : provides.members.keySet()) {
                if (privateNodeMap.containsKey(k)) {
                    throw new IllegalArgumentException("At service with key " + k + " has already been registered");
                }
            }

            // AtProvidesGroup has already validated that the specified type does not have any members that provide services with
            // the same key, so we can just add them now without any verification
            for (AtProvides member : provides.members.values()) {
                privateNodeMap.put(owner.provide(member));// put them directly
            }
        }
    }

    public final void serviceAutoRequire() {
        autoRequires = true;
    }

    public final void serviceRequire(Class<?> key) {
        serviceRequire(Key.of(key));
    }

    public final void serviceRequire(Key<?> key) {
        box.services().addRequires(key);
    }

    public final void serviceRequireOptionally(Class<?> key) {
        serviceRequireOptionally(Key.of(key));
    }

    public final void serviceRequireOptionally(Key<?> key) {
        box.services().addOptional(key);
    }

    /** {@inheritDoc} */
    @Override
    public InjectorBuilder setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void wireInjector(Bundle bundle, WiringOperation... stages) {
        requireNonNull(bundle, "bundle is null");
        List<WiringOperation> listOfStages = BundleSupport.invoke().extractWiringOperations(stages, Bundle.class);
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

    /** {@inheritDoc} */
    @Override
    public final void wireInjector(Injector injector, WiringOperation... operations) {
        requireNonNull(injector, "injector is null");
        List<WiringOperation> wiringOperations = BundleSupport.invoke().extractWiringOperations(operations, Bundle.class);
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite cs = configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        WireInjector is = new WireInjector(this, cs, injector, wiringOperations);
        is.importServices();
    }
}
