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
import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.base.InvalidDeclarationException;
import app.packed.config.ConfigSite;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedWireableComponentDriver.SingletonComponentDriver;
import packed.internal.inject.ServiceDependency;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.runtime.Prototype2InjectorEntry;
import packed.internal.service.runtime.PrototypeInjectorEntry;
import packed.internal.service.runtime.RuntimeEntry;

/**
 * An entry representing a component node. This node is used for all three binding modes mainly because it makes
 * extending it with much easier.
 */
public final class ComponentFactoryBuildEntry<T> extends AbstractComponentBuildEntry<T> {

    boolean hasInstanceMembers;

    /** The instantiation mode of this node. */
    private ServiceMode instantionMode;

    /** Is null for instance components. */
    public final MethodHandle mha;

    // Is created for a @Provide method, uses the parent component
    public ComponentFactoryBuildEntry(ConfigSite configSite, AtProvides atProvides, MethodHandle mh, AbstractComponentBuildEntry<?> parent) {
        super(parent.node, configSite, atProvides.dependencies, atProvides.isStaticMember ? null : parent, parent.component);
        this.instantionMode = atProvides.instantionMode;
        this.mha = requireNonNull(mh);
    }

    public ComponentFactoryBuildEntry(ServiceExtensionNode injectorBuilder, ComponentNodeConfiguration cc, ServiceMode instantionMode, MethodHandle mh,
            List<ServiceDependency> dependencies) {
        super(injectorBuilder, cc.configSite(), dependencies, null, cc);
        this.instantionMode = requireNonNull(instantionMode);
        this.mha = requireNonNull(mh);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return !dependencies.isEmpty();
    }

    public ComponentFactoryBuildEntry<T> instantiateAs(ServiceMode mode) {
        requireNonNull(mode, "mode is null");
        this.instantionMode = mode;
        return this;
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
//            T instance = ((SingletonComponentDriver<T>) component.driver()).instance;
//            context.ns.storeSingleton(component, instance);
//            return new IndexedInjectorEntry<>(this, context.ns, component.storeOffset);

//            Prototype2InjectorEntry<T> e = new Prototype2InjectorEntry<>(this, context);
//            T instance = e.getInstance(null);
//            context.ns.storeSingleton(component, instance);
//            return new IndexedInjectorEntry<>(this, context.ns, component.storeOffset);

            return new Prototype2InjectorEntry(this, context);
            // Okay lortet er resolvet

        } else {
            return new PrototypeInjectorEntry<>(this, context);
        }
    }

    void prototype() {
        if (hasDependencyOnInjectionSite) {
            throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        }
        if (hasInstanceMembers) {
            throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as prototypes");
        }
        instantiateAs(ServiceMode.PROTOTYPE);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return hasDependencyOnInjectionSite;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected MethodHandle newMH(ServiceExtensionInstantiationContext context) {
        if (dependencies.isEmpty()) {
            return mha;
        }
        MethodHandle mh = mha;
        for (int i = 0; i < resolvedDependencies.length; i++) {
            BuildEntry<?> e = resolvedDependencies[i];
            if (e instanceof ComponentConstantBuildEntry) {
                ComponentConstantBuildEntry<?> c = (ComponentConstantBuildEntry<?>) e;
                Object instance = ((SingletonComponentDriver<T>) c.component.driver()).instance;
                mh = MethodHandles.insertArguments(mh, 0, instance);
            } else if (e instanceof ComponentFactoryBuildEntry) {
                ComponentFactoryBuildEntry<?> c = (ComponentFactoryBuildEntry<?>) e;
                mh = MethodHandles.collectArguments(mh, 0, c.toMH(context));
            }
        }
        // Loeb igennem alle dependencies
        // er det en constant..
        // saa array bind den til argumented

        // Er det en prototype saa slaa den op
        return mh;
    }
}
