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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
import app.packed.util.Nullable;
import packed.internal.inject.Node;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.invokers.AccessibleExecutable;
import packed.internal.invokers.AccessibleField;
import packed.internal.invokers.ProvidesSupport.AtProvides;
import packed.internal.invokers.ServiceClassDescriptor;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** The default implementation of {@link InjectorConfiguration}. */
public class InternalInjectorConfiguration extends AbstractInjectorConfiguration {

    /** A list of imports/exports from injectors or injector bundles */
    final ArrayList<BindInjector> injectorBindings = new ArrayList<>();

    InternalInjector privateInjector;

    /** All nodes that have been added to this builder, even those that are not exposed. */
    final ArrayList<BuildNode<?>> privateNodeList = new ArrayList<>();

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    final NodeMap privateNodeMap = new NodeMap();

    InternalInjector publicInjector;

    final ArrayList<BuildNodeExposed<?>> publicNodeList = new ArrayList<>();

    /** The runtime nodes that will be available in the injector. */
    final NodeMap publicNodeMap = new NodeMap();

    final HashSet<Key<?>> requiredServicesMandatory = new HashSet<>();

    final HashSet<Key<?>> requiredServicesOptionally = new HashSet<>();

    /**
     * Creates a new configuration.
     * 
     * @param configurationSite
     *            the configuration site
     */
    public InternalInjectorConfiguration(InternalConfigurationSite configurationSite, @Nullable Bundle bundle) {
        super(configurationSite, bundle);

    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> ServiceConfiguration<T> bind(T instance) {
        requireNonNull(instance, "instance is null");
        checkConfigurable();
        BuildNodeDefault<T> node = new BuildNodeDefault<>(this, getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND), instance);
        scan(instance.getClass(), node);
        return bindNode(node).as((Class) instance.getClass());
    }

    @Override
    protected final <T> ServiceConfiguration<T> bindFactory(BindingMode mode, Factory<T> factory) {
        InternalConfigurationSite frame = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND);
        InternalFactory<T> f = InternalFactory.from(factory);
        f = accessor.readable(f);

        BuildNode<T> node = new BuildNodeDefault<>(this, frame, f, mode);
        return bindNode(node).as(factory.getKey());
    }

    protected <T> BuildNode<T> bindNode(BuildNode<T> node) {
        privateNodeList.add(node);
        return node;
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
        freezeBindings();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.BUNDLE_EXPOSE);
        BuildNodeExposed<T> bn = new BuildNodeExposed<>(this, cs, key);

        Node<T> node = privateNodeMap.getRecursive(key);
        if (node == null) {
            throw new IllegalArgumentException("Cannot expose non existing service, key = " + key);
        }

        // Lookup and copy from original, such things as description, and tags
        // We are really in expose only mode, all imports should have been resolved.
        bn.as(key);
        publicNodeList.add(bn);
        return bn;
    }

    @SuppressWarnings("unchecked")
    public final <T> ServiceConfiguration<T> expose(ServiceConfiguration<T> configuration) {
        checkConfigurable();
        freezeBindings();
        return (ServiceConfiguration<T>) expose(configuration.getKey());
    }

    public Injector finish() {
        freeze();
        freezeBindings();
        new InjectorBuilder(this).build();
        return publicInjector;
    }

    protected void freezeBindings() {
        if (!privateNodeList.isEmpty()) {
            HashMap<Key<?>, ArrayList<BuildNode<?>>> collisions = new HashMap<>();
            for (BuildNode<?> bv : privateNodeList) {
                if (bv.getKey() != null) {
                    if (!privateNodeMap.putIfAbsent(bv)) {
                        collisions.computeIfAbsent(bv.getKey(), k -> new ArrayList<>()).add(bv);
                    }
                    privateNodeMap.put(bv);
                }
            }
            privateNodeList.clear();
            if (!collisions.isEmpty()) {
                System.err.println("OOPS");
            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public final void injectorBind(Injector injector, InjectorImportStage... stages) {
        requireNonNull(stages, "stages is null");
        freezeBindings();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromInjector is = new BindInjectorFromInjector(this, cs, injector, stages);
        is.process();
        injectorBindings.add(is);

        for (BuildNodeImport<?> n : is.importedServices.values()) {
            privateNodeMap.put(n);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void injectorBind(InjectorBundle bundle, ImportExportStage... stages) {
        requireNonNull(bundle, "bundle is null");
        requireNonNull(stages, "stages is null");
        freezeBindings();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromBundle is = new BindInjectorFromBundle(this, cs, bundle, stages);
        is.process();
        injectorBindings.add(is);
        for (BuildNodeImport<?> n : is.importedServices.values()) {
            privateNodeMap.put(n);
        }
    }

    public void requireMandatory(Class<?> key) {
        requireMandatory(Key.of(key));
    }

    public void requireMandatory(Key<?> key) {
        requireNonNull(key, "key is null");
        requiredServicesMandatory.add(key);
    }

    public void requireOptionally(Class<?> key) {
        requireOptionally(Key.of(key));
    }

    public void requireOptionally(Key<?> key) {
        requireNonNull(key, "key is null");
        requiredServicesOptionally.add(key);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void scan(Class<?> type, BuildNodeDefault<?> parent) {
        ServiceClassDescriptor<?> serviceDesc = accessor.getServiceDescriptor(type);
        for (AccessibleField<AtProvides> field : serviceDesc.provides.fields) {
            BuildNodeDefault<?> providedNode = parent.provide(field);
            bindNode(providedNode).as((Key) field.metadata().getKey());
        }
        for (AccessibleExecutable<AtProvides> field : serviceDesc.provides.methods) {
            BuildNodeDefault<?> providedNode = parent.provide(field);
            bindNode(providedNode).as((Key) field.metadata().getKey());
        }
    }

    // public void require(Predicate<? super Dependency> p);

}
