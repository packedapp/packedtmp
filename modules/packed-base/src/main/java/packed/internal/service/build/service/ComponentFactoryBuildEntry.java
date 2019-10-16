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
package packed.internal.service.build.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.service.Dependency;
import app.packed.service.InstantiationMode;
import app.packed.service.ServiceComponentConfiguration;
import app.packed.util.InvalidDeclarationException;
import packed.internal.service.build.ServiceExtensionInstantiationContext;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.run.CachingPrototypeInjectorEntry;
import packed.internal.service.run.InjectorEntry;
import packed.internal.service.run.LazyInjectorEntry;
import packed.internal.service.run.PrototypeInjectorEntry;

/**
 * An entry representing a component node. This node is used for all three binding modes mainly because it makes
 * extending it with {@link ServiceComponentConfiguration} much easier.
 */
public final class ComponentFactoryBuildEntry<T> extends AbstractComponentBuildEntry<T> {

    boolean hasInstanceMembers;

    /** The instantiation mode of this node. */
    private InstantiationMode instantionMode;

    /** Is null for instance components. */
    public final MethodHandle mha;

    public ComponentFactoryBuildEntry(ConfigSite configSite, AtProvides atProvides, MethodHandle mh, AbstractComponentBuildEntry<?> parent) {
        super(parent.serviceExtension, configSite, atProvides.dependencies, parent, parent.componentConfiguration);
        this.description = atProvides.description;
        this.instantionMode = atProvides.instantionMode;
        this.mha = requireNonNull(mh);
    }

    public ComponentFactoryBuildEntry(ServiceExtensionNode injectorBuilder, ComponentConfiguration<T> cc, InstantiationMode instantionMode, MethodHandle mh,
            List<Dependency> dependencies) {
        super(injectorBuilder, cc.configSite(), dependencies, null, cc);
        this.instantionMode = requireNonNull(instantionMode);
        this.mha = requireNonNull(mh);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return !dependencies.isEmpty();
    }

    public ComponentFactoryBuildEntry<T> instantiateAs(InstantiationMode mode) {
        requireNonNull(mode, "mode is null");
        this.instantionMode = mode;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return instantionMode;
    }

    public void lazy() {
        instantiateAs(InstantiationMode.LAZY);
    }

    public boolean needsInstance() {
        return declaringEntry != null && mha.type().parameterCount() != dependencies.size();
    }

    /** {@inheritDoc} */
    @Override
    protected InjectorEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        switch (instantionMode) {
        case SINGLETON:
            return new CachingPrototypeInjectorEntry<>(this, context);
        case LAZY:
            return new LazyInjectorEntry<>(this, context);
        default:
            return new PrototypeInjectorEntry<>(this, context);
        }
    }

    public void prototype() {
        if (hasDependencyOnInjectionSite) {
            throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        }
        if (hasInstanceMembers) {
            throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as prototypes");
        }
        instantiateAs(InstantiationMode.PROTOTYPE);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return hasDependencyOnInjectionSite;
    }
}
