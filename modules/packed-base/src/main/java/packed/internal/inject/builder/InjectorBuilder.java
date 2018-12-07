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
import app.packed.bundle.ImportExportStage;
import app.packed.bundle.InjectorBundle;
import app.packed.bundle.InjectorImportStage;
import app.packed.inject.BindingMode;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Key;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.TypeLiteral;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.classscan.LookupDescriptorAccessor;
import packed.internal.classscan.ServiceClassDescriptor;
import packed.internal.inject.InjectSupport;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.ServiceNode;
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
     * Creates a new configuration.
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
        return bindFactory(BindingMode.SINGLETON, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(Factory<T> factory) {
        return bindFactory(BindingMode.SINGLETON, requireNonNull(factory, "factory is null"));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> ServiceConfiguration<T> bind(T instance) {
        requireNonNull(instance, "instance is null");
        checkConfigurable();
        freezeLatest();
        ServiceBuildNodeDefault<T> node = new ServiceBuildNodeDefault<>(this, getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND), instance);
        scan(instance.getClass(), node);
        return bindNode(node).as((Class) instance.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(TypeLiteral<T> implementation) {
        return bindFactory(BindingMode.SINGLETON, Factory.findInjectable(implementation));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final <T> ServiceConfiguration<T> bindFactory(BindingMode mode, Factory<T> factory) {
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite frame = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND);

        InternalFunction<T> func = InjectSupport.toInternalFunction(factory);

        ServiceBuildNodeDefault<T> node = new ServiceBuildNodeDefault<>(this, frame, mode, accessor.readable(func), (List) factory.getDependencies());

        scan(func.getRawType(), node);

        return bindNode(node).as(factory.getKey());
    }

    @Override
    public final <T> ServiceConfiguration<T> bindLazy(Class<T> implementation) {
        return bindFactory(BindingMode.LAZY, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindLazy(Factory<T> factory) {
        return bindFactory(BindingMode.LAZY, requireNonNull(factory, "factory is null"));
    }

    @Override
    public final <T> ServiceConfiguration<T> bindLazy(TypeLiteral<T> implementation) {
        return bindFactory(BindingMode.LAZY, Factory.findInjectable(implementation));
    }

    protected final <T> ServiceBuildNode<T> bindNode(ServiceBuildNode<T> node) {
        assert privateLatestNode == null;
        privateLatestNode = node;
        return node;
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(Class<T> implementation) {
        return bindFactory(BindingMode.PROTOTYPE, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(Factory<T> factory) {
        return bindFactory(BindingMode.PROTOTYPE, requireNonNull(factory, "factory is null"));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(TypeLiteral<T> implementation) {
        return bindFactory(BindingMode.PROTOTYPE, Factory.findInjectable(implementation));
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
        if (privateLatestNode != null) {
            if (!privateNodeMap.putIfAbsent(privateLatestNode)) {
                System.err.println("OOPS");
            }
            privateLatestNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void injectorBind(Injector injector, InjectorImportStage... stages) {
        requireNonNull(injector, "injector is null");
        requireNonNull(stages, "stages is null");
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromInjector is = new BindInjectorFromInjector(this, cs, injector, stages);
        is.importServices();
    }

    /** {@inheritDoc} */
    @Override
    public void injectorBind(InjectorBundle bundle, ImportExportStage... stages) {
        requireNonNull(bundle, "bundle is null");
        requireNonNull(stages, "stages is null");// We should probably validates stages for null, before freezeLatest
        checkConfigurable();
        freezeLatest();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromBundle is = new BindInjectorFromBundle(this, cs, bundle, stages);
        is.processImport();
        if (injectorBundleBindings == null) {
            injectorBundleBindings = new ArrayList<>(1);
        }
        injectorBundleBindings.add(is);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void scan(Class<?> type, ServiceBuildNodeDefault<?> parent) {
        ServiceClassDescriptor<?> serviceDesc = accessor.getServiceDescriptor(type);

        AtProvidesGroup ps = serviceDesc.provides;
        if (!ps.isEmpty()) {
            if (parent.getBindingMode() == BindingMode.PROTOTYPE && ps.hasInstanceMembers) {
                throw new InvalidDeclarationException("OOOPS");
            }

            // First check that the class does not provide services that have already been registered
            for (Key<?> k : ps.keys.keySet()) {
                if (privateNodeMap.containsKey(k)) {
                    throw new IllegalArgumentException("At service with key " + k + " has already been registered");
                }
            }

            // ProvidesSupport has already validated that the specified type does not have any members that provide services with
            // the same key, so we can just add them now without checking
            for (AtProvides field : ps.fields) {
                ServiceBuildNode<?> providedNode = parent.provide(field);
                providedNode.as((Key) field.key);
                privateNodeMap.put(providedNode);// put them directly
            }
            for (AtProvides method : ps.methods) {
                ServiceBuildNode<?> providedNode = parent.provide(method);
                providedNode.as((Key) method.key);
                privateNodeMap.put(providedNode);// put them directly
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public InjectorBuilder setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    // public void require(Predicate<? super Dependency> p);
}
