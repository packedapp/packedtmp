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
import java.util.HashSet;
import java.util.function.Consumer;

import app.packed.bundle.Bundle;
import app.packed.bundle.ImportExportStage;
import app.packed.bundle.InjectorBundle;
import app.packed.bundle.InjectorImportStage;
import app.packed.container.Container;
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
public class InjectorBuilder extends AbstractInjectorBuilder {

    /**
     * The bundle we are building an injector for, null for {@link Injector#of(Consumer)} or {@link Container#of(Consumer)}.
     */
    @Nullable
    final Bundle bundle;

    /** A list of bundle bindings, as we need to post process the exports. */
    ArrayList<BindInjectorFromBundle> injectorBundleBindings;

    InternalInjector privateInjector;

    /** All nodes that have been added to this builder, even those that are not exposed. */
    BuildNode<?> privateLatestNode;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    final NodeMap privateNodeMap;

    InternalInjector publicInjector;

    @Nullable
    final ArrayList<BuildNodeExposed<?>> publicNodeList;

    /** The runtime nodes that will be available in the injector. */
    final NodeMap publicNodeMap;

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
        publicNodeMap = privateNodeMap = new NodeMap();
        publicNodeList = null;
    }

    public InjectorBuilder(InternalConfigurationSite configurationSite, Bundle bundle) {
        super(configurationSite);
        this.bundle = requireNonNull(bundle);
        publicNodeMap = new NodeMap();
        privateNodeMap = new NodeMap();
        publicNodeList = new ArrayList<>();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> ServiceConfiguration<T> bind(T instance) {
        requireNonNull(instance, "instance is null");
        checkConfigurable();
        freezeBindings();
        BuildNodeDefault<T> node = new BuildNodeDefault<>(this, getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND), instance);
        scan(instance.getClass(), node);
        return bindNode(node).as((Class) instance.getClass());
    }

    @Override
    protected final <T> ServiceConfiguration<T> bindFactory(BindingMode mode, Factory<T> factory) {
        checkConfigurable();
        freezeBindings();
        InternalConfigurationSite frame = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND);
        InternalFactory<T> f = InternalFactory.from(factory);
        f = accessor.readable(f);

        BuildNode<T> node = new BuildNodeDefault<>(this, frame, mode, f);
        return bindNode(node).as(factory.getKey());
    }

    protected <T> BuildNode<T> bindNode(BuildNode<T> node) {
        assert privateLatestNode == null;
        privateLatestNode = node;
        return node;
    }

    public Injector build() {
        freeze();
        freezeBindings();
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
        freezeBindings();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.BUNDLE_EXPOSE);
        BuildNodeExposed<T> bn = new BuildNodeExposed<>(this, cs, key);

        Node<T> node = privateNodeMap.getRecursive(key);
        if (node == null) {
            throw new IllegalArgumentException("Cannot expose non existing service, key = " + key);
        }
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

    protected void freezeBindings() {
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
        requireNonNull(stages, "stages is null");
        freezeBindings();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromInjector is = new BindInjectorFromInjector(this, cs, injector, stages);
        is.importServices();
    }

    /** {@inheritDoc} */
    @Override
    public void injectorBind(InjectorBundle bundle, ImportExportStage... stages) {
        requireNonNull(stages, "stages is null");
        freezeBindings();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromBundle is = new BindInjectorFromBundle(this, cs, bundle, stages);
        is.process();
        if (injectorBundleBindings == null) {
            injectorBundleBindings = new ArrayList<>(1);
        }
        injectorBundleBindings.add(is);
    }

    public void requireMandatory(Class<?> key) {
        requireMandatory(Key.of(key));
    }

    public void requireMandatory(Key<?> key) {
        requireNonNull(key, "key is null");
        if (requiredServicesMandatory == null) {
            requiredServicesMandatory = new HashSet<>();
        }
        requiredServicesMandatory.add(key);
    }

    public void requireOptionally(Class<?> key) {
        requireOptionally(Key.of(key));
    }

    public void requireOptionally(Key<?> key) {
        requireNonNull(key, "key is null");
        if (requiredServicesOptionally == null) {
            requiredServicesOptionally = new HashSet<>();
        }
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
