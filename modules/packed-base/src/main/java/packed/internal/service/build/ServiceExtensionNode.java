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
package packed.internal.service.build;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.container.BundleDescriptor;
import app.packed.container.extension.ExtensionNode;
import app.packed.container.extension.ExtensionPipelineContext;
import app.packed.hook.OnHookGroup;
import app.packed.service.Inject;
import app.packed.service.InstantiationMode;
import app.packed.service.Provide;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.service.ServiceEntry;
import packed.internal.service.build.dependencies.DependencyManager;
import packed.internal.service.build.export.ExportManager;
import packed.internal.service.build.export.ExportedBuildEntry;
import packed.internal.service.build.service.AtProvidesGroup;
import packed.internal.service.build.service.ComponentBuildEntry;
import packed.internal.service.build.service.ServiceProvidingManager;
import packed.internal.service.run.DefaultInjector;
import packed.internal.service.util.AtInject;
import packed.internal.service.util.AtInjectGroup;

/** This class records all service related information for a single box. */
public final class ServiceExtensionNode extends ExtensionNode<ServiceExtension> {

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public DependencyManager dependencies;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    private ExportManager exporter;

    private boolean hasFailed;

    /** The configuration of the container to which this builder belongs to. */
    public PackedContainerConfiguration pcc() {
        return ((PackedExtensionContext) context()).pcc;
    }

    /** A service exporter handles everything to do with exports. */
    @Nullable
    ServiceProvidingManager provider;

    public DefaultInjector publicInjector;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, ServiceEntry<?>> resolvedEntries = new LinkedHashMap<>();

    /**
     * Creates a new builder.
     * 
     * @param extension
     *            the service extension
     */
    public ServiceExtensionNode(ServiceExtension extension) {
        super(extension);
    }

    public void addErrorMessage() {
        hasFailed = true;
    }

    public void build() {
        HashMap<Key<?>, BuildEntry<?>> resolvedServices = provider().resolve();

        resolvedServices.values().forEach(e -> resolvedEntries.put(requireNonNull(e.key()), e));

        if (exporter != null) {
            exporter.resolve();
        }

        if (hasFailed) {
            return;
        }
        dependencies().analyze();

    }

    public void buildDescriptor(BundleDescriptor.Builder builder) {
        // need to have resolved successfully
        // TODO we should only have build entries here...
        for (ServiceEntry<?> n : resolvedEntries.values()) {
            if (n instanceof BuildEntry) {
                builder.addServiceDescriptor(((BuildEntry<?>) n).toDescriptor());
            }
        }
    }

    public ServiceContract newServiceContract(ExtensionPipelineContext context) {
        // requireNonNull(context);
        return ServiceContract.of(c -> {
            if (exporter != null) {
                for (ExportedBuildEntry<?> n : exporter) {
                    c.addProvides(n.key());
                }
            }
            dependencies().buildContract(c);
        });
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
    public DependencyManager dependencies() {
        DependencyManager d = dependencies;
        if (d == null) {
            d = dependencies = new DependencyManager(this);
        }
        return d;
    }

    /**
     * Returns the {@link ExportManager} for this builder.
     * 
     * @return the service exporter for this builder
     */
    public ExportManager exports() {
        ExportManager e = exporter;
        if (e == null) {
            e = exporter = new ExportManager(this);
        }
        return e;
    }

    private void instantiate() {
        // Instantiate
        for (ServiceEntry<?> node : resolvedEntries.values()) {
            if (node instanceof ComponentBuildEntry) {
                ComponentBuildEntry<?> s = (ComponentBuildEntry<?>) node;
                if (s.instantiationMode() == InstantiationMode.SINGLETON) {
                    s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                }
            }
        }

        // Okay we are finished, convert all nodes to runtime nodes.
        resolvedEntries.replaceAll((k, v) -> v.toRuntimeEntry());

        // Now inject all components...

        // if (exporter != null) {
        // if (resolvedEntries != exporter.resolvedServiceMap()) {
        // exporter.resolvedServiceMap().replaceAll((k, v) -> v.toRuntimeEntry());
        // }
        // }
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
        provider().addProvidesGroup(cc, group);
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
