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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import app.packed.bundle.Bundle;
import app.packed.bundle.BundlingStage;
import app.packed.bundle.InjectorBundle;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;
import packed.internal.bundle.BundleSupport;
import packed.internal.classscan.LookupDescriptorAccessor;
import packed.internal.classscan.ServiceClassDescriptor;
import packed.internal.inject.InjectSupport;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.runtime.InternalInjector;
import packed.internal.inject.support.AtProvides;
import packed.internal.inject.support.AtProvidesGroup;
import packed.internal.invokers.InternalFunction;
import packed.internal.util.AbstractConfiguration;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * A builder of {@link Injector injectors}. Is both used via {@link InjectorBundle} and {@link InjectorConfiguration}.
 */
public class InjectorBuilder extends AbstractConfiguration implements InjectorConfiguration {

    /** The lookup object. We default to public access */
    public LookupDescriptorAccessor accessor = LookupDescriptorAccessor.PUBLIC;

    /** The bundle we are building an injector for, null for {@link Injector#of(Consumer)}. */
    @Nullable
    final Bundle bundle;

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

    HashSet<Key<?>> requiredServicesMandatory;

    HashSet<Key<?>> requiredServicesOptionally;

    /**
     * Creates a new builder.
     * 
     * @param configurationSite
     *            the configuration site
     */
    public InjectorBuilder(InternalConfigurationSite configurationSite) {
        super(configurationSite);
        this.bundle = null;
        publicNodeMap = privateNodeMap = new ServiceNodeMap();
        publicNodeList = null;
    }

    public InjectorBuilder(InternalConfigurationSite configurationSite, Bundle bundle) {
        super(configurationSite);
        this.bundle = requireNonNull(bundle);
        publicNodeMap = new ServiceNodeMap();
        privateNodeMap = new ServiceNodeMap();
        publicNodeList = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(Class<T> implementation) {
        return bindFactory(InstantiationMode.SINGLETON, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(Factory<T> factory) {
        return bindFactory(InstantiationMode.SINGLETON, requireNonNull(factory, "factory is null"));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> ServiceConfiguration<T> bind(T instance) {
        requireNonNull(instance, "instance is null");
        checkConfigurable();
        freezeLatest();
        ServiceClassDescriptor serviceDesc = accessor.getServiceDescriptor(instance.getClass());
        ServiceBuildNodeDefault<T> node = new ServiceBuildNodeDefault<>(this,
                getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND), serviceDesc, instance);
        scanForProvides(instance.getClass(), node);
        return bindNode(node).as((Class) instance.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(TypeLiteral<T> implementation) {
        return bindFactory(InstantiationMode.SINGLETON, Factory.findInjectable(implementation));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final <T> ServiceConfiguration<T> bindFactory(InstantiationMode mode, Factory<T> factory) {
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite frame = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND);
        InternalFunction<T> func = InjectSupport.toInternalFunction(factory);

        ServiceClassDescriptor serviceDesc = accessor.getServiceDescriptor(func.getReturnTypeRaw());
        ServiceBuildNodeDefault<T> node = new ServiceBuildNodeDefault<>(this, frame, serviceDesc, mode, accessor.readable(func),
                (List) factory.getDependencies());

        scanForProvides(func.getReturnTypeRaw(), node);

        return bindNode(node).as(factory.getKey());
    }

    /** {@inheritDoc} */
    @Override
    public final void bindInjector(Injector injector, BundlingStage... stages) {
        requireNonNull(injector, "injector is null");
        List<BundlingStage> listOfStages = BundleSupport.invoke().stagesExtract(stages, InjectorBundle.class);
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromInjector is = new BindInjectorFromInjector(this, cs, injector, listOfStages);
        is.importServices();
    }

    /** {@inheritDoc} */
    @Override
    public void bindInjector(InjectorBundle bundle, BundlingStage... stages) {
        requireNonNull(bundle, "bundle is null");
        List<BundlingStage> listOfStages = BundleSupport.invoke().stagesExtract(stages, InjectorBundle.class);
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromBundle is = new BindInjectorFromBundle(this, cs, bundle, listOfStages);
        is.processImport();
        if (injectorBundleBindings == null) {
            injectorBundleBindings = new ArrayList<>(1);
        }
        injectorBundleBindings.add(is);
    }

    @Override
    public final <T> ServiceConfiguration<T> bindLazy(Class<T> implementation) {
        return bindFactory(InstantiationMode.LAZY, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindLazy(Factory<T> factory) {
        return bindFactory(InstantiationMode.LAZY, requireNonNull(factory, "factory is null"));
    }

    @Override
    public final <T> ServiceConfiguration<T> bindLazy(TypeLiteral<T> implementation) {
        return bindFactory(InstantiationMode.LAZY, Factory.findInjectable(implementation));
    }

    protected final <T> ServiceBuildNode<T> bindNode(ServiceBuildNode<T> node) {
        assert privateLatestNode == null;
        privateLatestNode = node;
        return node;
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(Class<T> implementation) {
        return bindFactory(InstantiationMode.PROTOTYPE, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(Factory<T> factory) {
        return bindFactory(InstantiationMode.PROTOTYPE, requireNonNull(factory, "factory is null"));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(TypeLiteral<T> implementation) {
        return bindFactory(InstantiationMode.PROTOTYPE, Factory.findInjectable(implementation));
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
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.BUNDLE_EXPOSE);

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
            Key<?> key = privateLatestNode.getKey();
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
    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        checkConfigurable();
        this.accessor = LookupDescriptorAccessor.get(lookup);
    }

    public final void requireMandatory(Class<?> key) {
        requireMandatory(Key.of(key));
    }

    public final void requireMandatory(Key<?> key) {
        requireNonNull(key, "key is null");
        if (requiredServicesMandatory == null) {
            requiredServicesMandatory = new HashSet<>();
        }
        requiredServicesMandatory.add(key);
    }

    public final void requireOptionally(Class<?> key) {
        requireOptionally(Key.of(key));
    }

    public final void requireOptionally(Key<?> key) {
        requireNonNull(key, "key is null");
        if (requiredServicesOptionally == null) {
            requiredServicesOptionally = new HashSet<>();
        }
        requiredServicesOptionally.add(key);
    }

    protected void scanForProvides(Class<?> type, ServiceBuildNodeDefault<?> owner) {
        AtProvidesGroup provides = accessor.getServiceDescriptor(type).provides;
        if (!provides.members.isEmpty()) {
            if (owner.getInstantiationMode() == InstantiationMode.PROTOTYPE && provides.hasInstanceMembers) {
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

    /** {@inheritDoc} */
    @Override
    public InjectorBuilder setDescription(String description) {
        super.setDescription(description);
        return this;
    }
}
