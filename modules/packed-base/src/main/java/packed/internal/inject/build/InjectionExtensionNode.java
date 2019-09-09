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

import java.util.HashMap;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.container.BundleDescriptor;
import app.packed.container.extension.ExtensionContext;
import app.packed.container.extension.ExtensionNode;
import app.packed.container.extension.OnHookGroup;
import app.packed.inject.Inject;
import app.packed.inject.InjectionExtension;
import app.packed.inject.InstantiationMode;
import app.packed.inject.Provide;
import app.packed.inject.ServiceContract;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.dependencies.ServiceDependencyManager;
import packed.internal.inject.build.export.ServiceExportManager;
import packed.internal.inject.build.service.AtProvidesGroup;
import packed.internal.inject.build.service.ComponentBuildEntry;
import packed.internal.inject.build.service.ServiceProvidingManager;
import packed.internal.inject.run.DefaultInjector;
import packed.internal.inject.util.AtInject;
import packed.internal.inject.util.AtInjectGroup;
import packed.internal.inject.util.ServiceNodeMap;

/** This class records all service related information for a single box. */
public final class InjectionExtensionNode extends ExtensionNode<InjectionExtension> {

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public ServiceDependencyManager dependencies;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    public ServiceExportManager exporter;

    private boolean hasFailed;

    /** The configuration of the container to which this builder belongs to. */
    public final PackedContainerConfiguration pcc;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    ServiceProvidingManager provider;

    public DefaultInjector publicInjector;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final ServiceNodeMap resolvedEntries = new ServiceNodeMap();

    /**
     * Creates a new builder.
     * 
     * @param context
     *            the extension context
     */
    public InjectionExtensionNode(ExtensionContext context) {
        super(context);
        this.pcc = requireNonNull((PackedExtensionContext) context).pcc;
    }

    public void addErrorMessage() {

        hasFailed = true;
    }

    public void build(ArtifactBuildContext buildContext) {
        HashMap<Key<?>, BuildEntry<?>> resolvedServices = provider().resolveAndCheckForDublicates(buildContext);
        resolvedEntries.addAll(resolvedServices.values());

        if (exporter != null) {
            exporter.resolve(this, buildContext);
        }

        if (hasFailed) {
            return;
        }
        dependencies().analyze(exporter);

    }

    @Override
    public void buildDescriptor(BundleDescriptor.Builder builder) {
        // need to have resolved successfully
        for (ServiceEntry<?> n : resolvedEntries) {
            if (n instanceof BuildEntry) {
                builder.addServiceDescriptor(((BuildEntry<?>) n).toDescriptor());
            }
        }

        builder.addContract(ServiceContract.of(c -> {
            if (exporter != null) {
                exporter.buildContract(c);
            }
            dependencies().buildContract(c);
        }));
    }

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    public ServiceDependencyManager dependencies() {
        ServiceDependencyManager d = dependencies;
        if (d == null) {
            d = dependencies = new ServiceDependencyManager(this);
        }
        return d;
    }

    /**
     * Returns the {@link ServiceExportManager} for this builder.
     * 
     * @return the service exporter for this builder
     */
    public ServiceExportManager exports() {
        ServiceExportManager e = exporter;
        if (e == null) {
            e = exporter = new ServiceExportManager(this);
        }
        return e;
    }

    private void instantiate() {
        // Instantiate
        for (ServiceEntry<?> node : resolvedEntries) {
            if (node instanceof ComponentBuildEntry) {
                ComponentBuildEntry<?> s = (ComponentBuildEntry<?>) node;
                if (s.instantiationMode() == InstantiationMode.SINGLETON) {
                    s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                }
            }
        }

        // Okay we are finished, convert all nodes to runtime nodes.
        resolvedEntries.toRuntimeNodes();

        // Now inject all components...

        if (exporter != null) {
            if (resolvedEntries != exporter.resolvedServiceMap()) {
                exporter.resolvedServiceMap().toRuntimeNodes();
            }
        }
    }

    /**
     * Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Inject}.
     * 
     * @param cc
     *            the configuration of the annotated component
     * @param group
     *            a inject group object
     */
    @OnHookGroup(AtInjectGroup.Builder.class)
    public void onInjectGroup(ComponentConfiguration cc, AtInjectGroup group) {
        // new Exception().printStackTrace();
        // Hvis den er instans, Singlton Factory -> Saa skal det vel med i en liste
        // Hvis det er en ManyProvide-> Saa skal vi jo egentlig bare gemme den til den bliver instantieret.
        // Det skal ogsaa tilfoejes requires...
        // Delt op i 2 dele...
        // * Tilfoej det til requirements...
        // * Scheduler at groupen skal kaldes senere ved inject...
        for (AtInject ai : group.members) {
            System.out.println(ai);
        }
    }

    /**
     * Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Provide}.
     * 
     * @param cc
     *            the configuration of the annotated component
     * @param group
     *            a provides group object
     */
    @OnHookGroup(AtProvidesGroup.Builder.class)
    void onHookAtProvidesGroup(ComponentConfiguration cc, AtProvidesGroup group) {
        provider().onProvidesGroup(cc, group);
    }

    public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        if (context().buildContext().isInstantiating()) {
            instantiate();
        }
        context().putIntoInstantiationContext(context, publicInjector);
    }

    public ServiceProvidingManager provider() {
        ServiceProvidingManager p = provider;
        if (p == null) {
            p = provider = new ServiceProvidingManager(this);
        }
        return p;
    }
}
