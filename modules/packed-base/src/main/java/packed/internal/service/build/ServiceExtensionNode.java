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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import app.packed.component.ComponentConfiguration;
import app.packed.container.BundleDescriptor;
import app.packed.container.extension.ExtensionContext;
import app.packed.container.extension.ExtensionInstantiationContext;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.service.Inject;
import app.packed.service.InstantiationMode;
import app.packed.service.ServiceContract;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.hooks.AtInject;
import packed.internal.inject.hooks.AtInjectGroup;
import packed.internal.service.ServiceEntry;
import packed.internal.service.build.dependencies.DependencyManager;
import packed.internal.service.build.export.ExportManager;
import packed.internal.service.build.export.ExportedBuildEntry;
import packed.internal.service.build.service.ComponentBuildEntry;
import packed.internal.service.build.service.ServiceProvidingManager;
import packed.internal.service.run.DefaultInjector;

/** This class records all service related information for a single box. */
public final class ServiceExtensionNode {

    public ServiceExtensionNode parent;

    public ArrayList<ServiceExtensionNode> children;

    private final ExtensionContext context;

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public DependencyManager dependencies;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    private ExportManager exporter;

    private boolean hasFailed;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    ServiceProvidingManager provider;

    public DefaultInjector publicInjector;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final LinkedHashMap<Key<?>, ServiceEntry<?>> resolvedEntries = new LinkedHashMap<>();

    /**
     * Creates a new builder.
     * 
     * @param context
     *            the context
     */
    public ServiceExtensionNode(ExtensionContext context) {
        this.context = requireNonNull(context, "context is null");
    }

    public void addErrorMessage() {
        hasFailed = true;
    }

    public void link(ServiceExtensionNode child) {
        child.parent = this;
        if (children == null) {
            children = new ArrayList<>(5);
        }
        children.add(child);
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

    public void checkExportConfigurable() {
        // when processing wirelets
        // We should make sure some stuff is no longer configurable...
    }

    public final ExtensionContext context() {
        return context;
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

    public void instantiate() {
        //// Hmmm, det er jo altsaa lidt anderledes
        // Hvis vi vil lave et image...
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

    public ServiceContract newServiceContract(ExtensionWirelet.PipelineMap context) {
        // requireNonNull(context);
        return ServiceContract.newContract(c -> {
            if (exporter != null) {
                for (ExportedBuildEntry<?> n : exporter) {
                    c.addProvides(n.key());
                }
            }
            dependencies().buildContract(c);
        });
    }

    /**
     * Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Inject}.
     * 
     * @param cc
     *            the configuration of the annotated component
     * @param group
     *            a inject group object
     */
    public void onInjectGroup(ComponentConfiguration<?> cc, AtInjectGroup group) {
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

    public void onInstantiate(ExtensionInstantiationContext c) {
        instantiate();
        c.put(publicInjector);
    }

    public ServiceProvidingManager provider() {
        ServiceProvidingManager p = provider;
        if (p == null) {
            p = provider = new ServiceProvidingManager(this);
        }
        return p;
    }
}
