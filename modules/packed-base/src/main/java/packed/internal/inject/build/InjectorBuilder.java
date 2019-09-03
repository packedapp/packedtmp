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
package packed.internal.inject.build;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.BundleDescriptor;
import app.packed.container.WireletList;
import app.packed.feature.FeatureKey;
import app.packed.inject.Factory;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.container.CoreComponentConfiguration;
import packed.internal.container.FactoryComponentConfiguration;
import packed.internal.container.InstantiatedComponentConfiguration;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.InjectorConfigSiteOperations;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.export.ServiceExporter;
import packed.internal.inject.build.requirements.ServiceDependencyManager;
import packed.internal.inject.compose.InjectorResolver;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.inject.run.AbstractInjector;
import packed.internal.inject.util.AtInjectGroup;
import packed.internal.inject.util.AtProvides;
import packed.internal.inject.util.AtProvidesGroup;

/** This class records all service related information for a single box. */
public final class InjectorBuilder {

    /** A that is used to store parent nodes */
    private static FeatureKey<BSEComponent<?>> FK = new FeatureKey<>() {};

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public ServiceDependencyManager dependencies;

    /** All provided nodes. */
    public final ArrayList<BuildEntry<?>> entries = new ArrayList<>();

    /** A service exporter handles everything to do with exports. */
    @Nullable
    public ServiceExporter exporter;

    /** The configuration of the container to which this builder belongs to. */
    public final PackedContainerConfiguration pcc;

    public final ArrayList<ProvideAllFromInjector> provideAll = new ArrayList<>(0);

    final InjectorResolver resolver = new InjectorResolver(this);

    /**
     * Creates a new builder.
     * 
     * @param pcc
     *            the configuration of the container
     */
    public InjectorBuilder(PackedContainerConfiguration pcc) {
        this.pcc = requireNonNull(pcc);
    }

    public void build(ArtifactBuildContext buildContext) {
        resolver.build(buildContext);
    }

    public void buildBundle(BundleDescriptor.Builder builder) {
        // need to have resolved successfully
        for (ServiceEntry<?> n : resolver.internalNodes) {
            if (n instanceof BuildEntry) {
                builder.addServiceDescriptor(((BuildEntry<?>) n).toDescriptor());
            }
        }

        if (exporter != null) {
            exporter.addExportsToContract(builder.contract().services());
        }
        resolver.buildContract(builder.contract().services());
    }

    public ServiceDependencyManager dependencies() {
        ServiceDependencyManager d = dependencies;
        if (d == null) {
            d = dependencies = new ServiceDependencyManager();
        }
        return d;
    }

    public ServiceExporter exporter() {
        ServiceExporter e = exporter;
        if (e == null) {
            e = exporter = new ServiceExporter(this);
        }
        return e;
    }

    public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        context.put(pcc, resolver.publicInjector); // Used by PackedContainer
    }

    /**
     * @param cc
     * @param group
     */
    public void onProvidedMembers(ComponentConfiguration cc, AtInjectGroup group) {
        new Exception().printStackTrace();

        // Hvis den er instans, Singlton Factory -> Saa skal det vel med i en liste

        // Hvis det er en ManyProvide-> Saa skal vi jo egentlig bare gemme den til den bliver instantieret.

        // Det skal ogsaa tilfoejes requires...

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onProvidedMembers(ComponentConfiguration cc, AtProvidesGroup apg) {
        BSEComponent parentNode;
        if (cc instanceof InstantiatedComponentConfiguration) {
            Object instance = ((InstantiatedComponentConfiguration) cc).instance;
            parentNode = new BSEComponent(this, cc.configSite(), instance);
        } else {
            Factory<?> factory = ((FactoryComponentConfiguration) cc).factory;
            MethodHandle mh = pcc.lookup.toMethodHandle(factory.handle());
            parentNode = new BSEComponent<>(this, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }

        // If any of the @Provide methods are instance members the parent node needs special treatment.
        // As it needs to be constructed, before the field or method can provide services.
        parentNode.hasInstanceMembers = apg.hasInstanceMembers;

        // Add each @Provide as children of the parent node
        for (AtProvides atProvides : apg.members) {
            ConfigSite configSite = parentNode.configSite().thenAnnotatedMember(InjectorConfigSiteOperations.INJECTOR_PROVIDE, atProvides.provides,
                    atProvides.member);
            BSEComponent<?> node = new BSEComponent<>(configSite, atProvides, atProvides.methodHandle, parentNode);
            node.as((Key) atProvides.key);
            entries.add(node);
        }

        // Set the parent node, so it can be found from provideFactory or provideInstance
        cc.features().set(InjectorBuilder.FK, parentNode);
    }

    public void provideAll(AbstractInjector injector, ConfigSite confitSite, WireletList wirelets) {
        provideAll.add(new ProvideAllFromInjector(this, confitSite, injector, wirelets));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideFactory(ComponentConfiguration cc, Factory<T> factory, FactoryHandle<T> function) {
        BSEComponent<?> c = cc.features().get(FK);
        if (c == null) {
            // config site???
            MethodHandle mh = pcc.lookup.toMethodHandle(function);
            c = new BSEComponent<>(this, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }
        c.as((Key) factory.key());
        entries.add(c);
        return new PackedProvidedComponentConfiguration<>(cc, (BSEComponent) c);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideInstance(ComponentConfiguration cc, T instance) {
        // First see if we have already installed the node. This happens in #set if the component container any members
        // annotated with @Provides
        BSEComponent<?> node = cc.features().get(InjectorBuilder.FK);

        if (node == null) {
            // No node found, components has no @Provides method, create a new node
            node = new BSEComponent<T>(this, cc.configSite(), instance);
        }

        node.as((Key) Key.of(instance.getClass()));
        entries.add(node);
        return new PackedProvidedComponentConfiguration<>((CoreComponentConfiguration) cc, (BSEComponent) node);
    }

}
