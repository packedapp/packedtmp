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

import app.packed.bundle.Bundle;
import app.packed.bundle.InjectorBundle;
import app.packed.inject.AbstractInjectorStage;
import app.packed.inject.BindingMode;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.InjectorImportStage;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Nullable;
import packed.internal.inject.InternalInjector;
import packed.internal.inject.NodeMap;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.inject.factory.InternalFactoryField;
import packed.internal.invokers.AccessibleField;
import packed.internal.invokers.ServiceClassDescriptor;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;
import packed.internal.util.descriptor.AtProvides;

/** The default implementation of {@link InjectorConfiguration}. */
public class InternalInjectorConfiguration extends AbstractInjectorConfiguration {

    /** All nodes that have been added to this builder, even those that are not exposed. */
    final ArrayList<BuildNode<?>> privateBuildNodeList = new ArrayList<>();

    final NodeMap privateBuildNodeMap = new NodeMap();

    /** A list of imports/exports from injectors or injector bundles */
    final ArrayList<BindInjector> privateImports = new ArrayList<>();

    InternalInjector privateInjector;

    /** The runtime nodes that will be available in the injector. */
    final NodeMap privateRuntimeNodes = new NodeMap();

    InternalInjector publicInjector;

    /** The runtime nodes that will be available in the injector. */
    final NodeMap publicRuntimeNodes = new NodeMap();

    final ArrayList<BuildNode<?>> publicExposedNodeList = new ArrayList<>();

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
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));

        InternalConfigurationSite ics = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_BIND);
        ServiceClassDescriptor<?> scd = super.accessor.getServiceDescriptor(instance.getClass());
        BuildNode<T> node = new BuildNodeDefault<>(this, ics, instance);

        for (AccessibleField<AtProvides> s : scd.fieldsAtProvides) {
            // take configuration from AccessibleField, and splice in ontop of
            InternalConfigurationSite icss = ics.spawnAnnotatedField(ConfigurationSiteType.INJECTOR_PROVIDE,
                    s.metadata().getAnnotatedMember().getAnnotation(Provides.class), s.descriptor());
            InternalFactoryField<?> iff = new InternalFactoryField<>(s.descriptor().getTypeLiteral(), s.descriptor(), s.varHandle(), instance);

            BuildNodeDefault<?> bnn = new BuildNodeDefault<>(this, icss, iff, s.metadata().getBindingMode());
            bindNode(bnn).as((Key) s.metadata().getKey());
            System.out.println(s.descriptor());
        }
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

    private <T> BuildNode<T> bindNode(BuildNode<T> node) {
        // WHEN YOU CALL THIS METHOD, remember the key is not automatically bound, but must use .as(xxxxx)

        // If we need to separate the scanning, just take a boolean in the method
        if (node instanceof BuildNode) {
            // BuildNodeInstanceOrFactory<?> scanNode = (BuildNodeInstanceOrFactory<?>) node;
            // if (scanNode.getMirror().methods().annotatedMethods() != null) {
            // for (AnnotationProvidesReflectionData pmm : scanNode.getMirror().methods().annotatedMethods().providesMethods()) {
            // throw new UnsupportedOperationException();
            // // Get original is gone
            // // BuildNodeProvidesMethod<Object> pm = new BuildNodeProvidesMethod<>(scanNode, pmm, scanNode.getOriginal());
            // // buildNodes.add(pm);
            // // pm.as(pm.getReturnType());
            // }
            // }
        }
        privateBuildNodeList.add(node);
        // TODO scan here for provided nodes

        return node;
    }

    @Override
    public void injectorBind(InjectorBundle bundle, AbstractInjectorStage... filters) {
        requireNonNull(bundle, "bundle is null");
        requireNonNull(filters, "filters is null");
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        privateImports.add(new BindInjectorFromBundle(this, cs, bundle, filters));
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
        freezeBindings();
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.BUNDLE_EXPOSE);
        BuildNodeExposed<T> bn = new BuildNodeExposed<>(this, cs, key);
        // Lookup and copy from original, such things as description, and tags
        // We are really in expose only mode, all imports should have been resolved.
        bn.as(key);
        publicExposedNodeList.add(bn);
        return bn;
    }

    protected void freezeBindings() {

    }

    /** {@inheritDoc} */
    @Override
    public final void injectorBind(Injector injector, InjectorImportStage... stages) {
        requireNonNull(stages, "stages is null");
        InternalConfigurationSite cs = getConfigurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
        BindInjectorFromInjector is = new BindInjectorFromInjector(this, cs, injector, stages);
        privateImports.add(is);
        is.doStuff();
    }

    public Injector finish() {
        freeze();
        new InjectorBuilder(this).build();
        return publicInjector;
    }

    HashSet<Key<?>> requiredServicesMandatory = new HashSet<>();

    HashSet<Key<?>> requiredServicesOptionally = new HashSet<>();

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

    // public void require(Predicate<? super Dependency> p);

}
