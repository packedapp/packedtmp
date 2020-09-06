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
import java.lang.invoke.MethodType;
import java.util.List;

import app.packed.base.InvalidDeclarationException;
import app.packed.config.ConfigSite;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.NodeStore;
import packed.internal.inject.ServiceDependency;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.runtime.IndexedInjectorEntry;
import packed.internal.service.runtime.PrototypeInjectorEntry;
import packed.internal.service.runtime.RuntimeEntry;

/**
 * An entry representing a component node. This node is used for all three binding modes mainly because it makes
 * extending it with much easier.
 */
public final class ComponentFactoryBuildEntry<T> extends AbstractComponentBuildEntry<T> {

    public boolean hasInstanceMembers;

    /** The instantiation mode of this node. */
    private ServiceMode instantionMode;

    /** Is null for instance components. */
    public final MethodHandle mha;

    // Is created for a @Provide method, uses the parent component
    public ComponentFactoryBuildEntry(ConfigSite configSite, AtProvides atProvides, MethodHandle mh, AbstractComponentBuildEntry<?> parent) {
        super(parent.node, configSite, atProvides.dependencies, atProvides.isStaticMember ? null : parent, parent.component,
                atProvides.instantionMode == ServiceMode.PROTOTYPE);
        this.instantionMode = atProvides.instantionMode;
        this.mha = requireNonNull(mh);
    }

    public ComponentFactoryBuildEntry(ServiceExtensionNode injectorBuilder, ComponentNodeConfiguration cc, ServiceMode instantionMode, MethodHandle mh,
            List<ServiceDependency> dependencies, boolean isPrototype) {
        super(injectorBuilder, cc.configSite(), dependencies, null, cc, isPrototype);
        this.instantionMode = requireNonNull(instantionMode);
        this.mha = requireNonNull(mh);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return !source.dependencies.isEmpty();
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
            IndexedInjectorEntry<T> ee = new IndexedInjectorEntry<>(this, context.ns, index);
            return ee;
        } else {
            return new PrototypeInjectorEntry<>(this, context);
        }
    }

    public void prototype() {
        if (source.hasDependencyOnProvidePrototypeContext) {
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
        return source.hasDependencyOnProvidePrototypeContext;
    }

    public MethodHandle newInstance;

    /** {@inheritDoc} */
    @Override
    protected MethodHandle newMH(ServiceProvidingManager context) {
        MethodHandle mh = mha;
        // System.out.println("FIXING " + mh + " Dependencies " + source.resolvedDependencies.length);

        if (source.dependencies.isEmpty()) {
            if (mh.type().parameterCount() > 0) {
                MethodHandle mhp = source.resolvedDependencies[0].toMH(context);
//            System.out.println(mhp);
                // NodeStore.readSingletonAs(index, type)
                // System.out.println(mhp);
                // System.out.println(mh);
                mh = MethodHandles.collectArguments(mh, 0, mhp);
                // System.out.println("MH " + mh);
                context.mustInstantiate.addLast(this);
                newInstance = mh;
                return mh;
            }
        }
        int adjust = 0;
        for (int i = 0; i < source.resolvedDependencies.length; i++) {
            int index = i == 0 ? 0 : i - adjust;
            BuildEntry<?> e = source.resolvedDependencies[i];
            requireNonNull(e);
            if (e instanceof ComponentConstantBuildEntry) {
                ComponentConstantBuildEntry<?> c = (ComponentConstantBuildEntry<?>) e;
                Object instance = c.component.source.instance();
                // System.out.println("INSERTING INTO " + mh);
                // MethodHandle cons = MethodHandles.constant(instance.getClass(), instance);
                mh = MethodHandles.insertArguments(mh, index, instance); // 0 is NodeStore

                // mh = MethodHandles.collectArguments(mh, i - adjust, cons);
                adjust++;
//                System.out.println("NEW MH " + mh);
//                System.out.println();
            } else if (e instanceof ComponentFactoryBuildEntry) {
                ComponentFactoryBuildEntry<?> c = (ComponentFactoryBuildEntry<?>) e;
                MethodHandle collect = c.toMH(context);
//                System.out.println("____INSERTING INTO " + mh + "  -  " + collect);

                mh = MethodHandles.collectArguments(mh, index, collect);

//                System.out.println("____NEW MH " + mh);
//                System.out.println();
            } else {
//                System.out.println("NOOOOO " + e);
            }
        }
        if (mh.type().parameterCount() == 0) {
            mh = MethodHandles.dropArguments(mh, 0, NodeStore.class);
        } else if (mh.type().parameterCount() > 1) {
            MethodType mt = MethodType.methodType(mh.type().returnType(), NodeStore.class);
            int[] ar = new int[mh.type().parameterCount()];
            for (int i = 0; i < ar.length; i++) {
                ar[i] = 0;
            }
            mh = MethodHandles.permuteArguments(mh, mt, ar);
        }
        newInstance = mh;
        // System.out.println("*********************** MUST INSTANTIATE " + component);
        context.mustInstantiate.addLast(this);
        if (instantionMode == ServiceMode.CONSTANT) {
            return NodeStore.readSingletonAs(index /* component.storeOffset + subIndex */, mh.type().returnType());
        } else {
            return mh;
            // return mh;
        }
        // Loeb igennem alle dependencies
        // er det en constant..
        // saa array bind den til argumented

        // Er det en prototype saa slaa den op
    }

    @Override
    public String toString() {
        return "Factory " + mha.type().parameterList() + " -> " + mha.type().returnType();
    }
}
