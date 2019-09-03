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

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.container.BundleDescriptor;
import app.packed.util.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.dependencies.ServiceDependencyManager;
import packed.internal.inject.build.export.ServiceExporter;
import packed.internal.inject.build.service.ServiceProvidingManager;
import packed.internal.inject.util.AtInject;
import packed.internal.inject.util.AtInjectGroup;

/** This class records all service related information for a single box. */
public final class InjectorBuilder {

    /** Handles everything to do with dependencies, for example, explicit requirements. */
    public ServiceDependencyManager dependencies;

    /** A service exporter handles everything to do with exports. */
    @Nullable
    ServiceExporter exporter;

    /** The configuration of the container to which this builder belongs to. */
    public final PackedContainerConfiguration pcc;

    public final InjectorResolver resolver = new InjectorResolver(this);

    /** A service exporter handles everything to do with exports. */
    @Nullable
    ServiceProvidingManager provider;

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

    public void buildDescriptor(BundleDescriptor.Builder builder) {
        // need to have resolved successfully
        for (ServiceEntry<?> n : resolver.resolvedEntries) {
            if (n instanceof BuildEntry) {
                builder.addServiceDescriptor(((BuildEntry<?>) n).toDescriptor());
            }
        }

        if (exporter != null) {
            exporter.addExportsToContract(builder.contract().services());
        }
        resolver.dg.buildContract(builder.contract().services());
    }

    /**
     * Returns the dependency manager for this builder.
     * 
     * @return the dependency manager for this builder
     */
    public ServiceDependencyManager dependencies() {
        ServiceDependencyManager d = dependencies;
        if (d == null) {
            d = dependencies = new ServiceDependencyManager();
        }
        return d;
    }

    /**
     * Returns the {@link ServiceExporter} for this builder.
     * 
     * @return the service exporter for this builder
     */
    public ServiceExporter exports() {
        ServiceExporter e = exporter;
        if (e == null) {
            e = exporter = new ServiceExporter(this);
        }
        return e;
    }

    public ServiceProvidingManager provider() {
        ServiceProvidingManager p = provider;
        if (p == null) {
            p = provider = new ServiceProvidingManager(this);
        }
        return p;
    }

    public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        context.put(pcc, resolver.publicInjector); // Used by PackedContainer
    }

    /**
     * @param cc
     * @param group
     */
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

}
