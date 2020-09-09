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
package packed.internal.service.buildtime.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.InvalidDeclarationException;
import app.packed.config.ConfigSite;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.RegionAssembly;
import packed.internal.inject.resolvable.ResolvableFactory;
import packed.internal.inject.resolvable.ServiceDependency;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.runtime.IndexedEntry;
import packed.internal.service.runtime.PrototypeInjectorEntry;
import packed.internal.service.runtime.RuntimeEntry;

/**
 * An entry representing a component node. This node is used for all three binding modes mainly because it makes
 * extending it with much easier.
 */
public final class ComponentMethodHandleBuildEntry<T> extends ComponentBuildEntry<T> {

    public boolean hasInstanceMembers;

    /** The instantiation mode of this node. */
    private final ServiceMode instantionMode;

    // @Provide methods
    public ComponentMethodHandleBuildEntry(ConfigSite configSite, AtProvides atProvides, ComponentBuildEntry<?> parent) {
        super(parent.node, configSite, parent.component, new ResolvableFactory(atProvides.dependencies, atProvides.isStaticMember ? null : parent,
                atProvides.methodHandle, atProvides.instantionMode, parent.component.region.reserve()));
        this.instantionMode = atProvides.instantionMode;
    }

    public ComponentMethodHandleBuildEntry(ServiceExtensionNode injectorBuilder, ComponentNodeConfiguration component, ResolvableFactory rf,
            ServiceMode instantionMode) {
        super(injectorBuilder, component.configSite(), component, rf);
        this.instantionMode = requireNonNull(instantionMode);
        if (instantionMode == ServiceMode.PROTOTYPE) {
            if (hasInstanceMembers) {
                throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as prototypes");
            }
        }
    }

    public ComponentMethodHandleBuildEntry(ServiceExtensionNode injectorBuilder, ComponentNodeConfiguration component, ServiceMode instantionMode,
            MethodHandle mh, List<ServiceDependency> dependencies) {
        super(injectorBuilder, component.configSite(), component, new ResolvableFactory(dependencies, null, mh, instantionMode, component.source.regionIndex));
        this.instantionMode = requireNonNull(instantionMode);
        if (instantionMode == ServiceMode.PROTOTYPE) {
            if (hasInstanceMembers) {
                throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as prototypes");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return !source.dependencies.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return instantionMode;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        if (instantionMode == ServiceMode.CONSTANT) {
            return new IndexedEntry<>(this, context.region, component.source.regionIndex);
        } else {
            return new PrototypeInjectorEntry<>(this, context);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected MethodHandle newMH(RegionAssembly ra, ServiceProvidingManager context) {
        return source.newMH(ra, context);
    }

    @Override
    public String toString() {
        return "Factory ";
    }
}
